package ru.multicard.service;

import javassist.NotFoundException;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.entity.CrashDetails;
import ru.multicard.repository.CrashDetailsRepo;
import ru.multicard.service.cache.CrashesIds;
import ru.multicard.service.util.HeaderUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CrashDetailsService {

    private final CrashDetailsRepo crashDetailsRepo;
    private final CacheManager cacheManager;


    @Autowired
    public CrashDetailsService(CrashDetailsRepo crashDetailsRepo, CacheManager cacheManager) {
        this.crashDetailsRepo = crashDetailsRepo;
        this.cacheManager = cacheManager;
    }


    public Integer handleFile(String cookie, MultipartFile file) {
        log.info(String.format("Handling file for session: %s", HeaderUtils.extractSession(cookie)));
        var crashes = parseExcel(file);
        if (!crashes.isEmpty()) {
            var crashesFromDb = crashDetailsRepo.saveAll(crashes);
            if (!crashesFromDb.isEmpty()) {
                cacheIds(HeaderUtils.extractSession(cookie), crashesFromDb);
            }
            return crashesFromDb.size();
        }
        return 0;
    }

    private List<CrashDetails> parseExcel(MultipartFile file) {

        var crashesList = new ArrayList<CrashDetails>();
        try {
            log.info("Start parsing excel file..");

            @Cleanup var in = file.getInputStream();
            @Cleanup var wb = new XSSFWorkbook(in);

            wb.forEach(sheet ->
                    sheet.forEach(row -> {
                        if (isRowCorrect(row)) {
                            var crashDetails = new CrashDetails();
                            crashDetails.setId((long) row.getCell(0).getNumericCellValue());
                            crashDetails.setAtmId(((XSSFCell) row.getCell(1)).getRawValue());
                            crashDetails.setAtmSerialNumber(((XSSFCell) row.getCell(5)).getRawValue());
                            crashDetails.setBankName(row.getCell(6).getStringCellValue());
                            crashDetails.setReason(row.getCell(2).getStringCellValue());
                            crashDetails.setBegin(row.getCell(3).getLocalDateTimeCellValue());
                            crashDetails.setEnd(row.getCell(4).getLocalDateTimeCellValue());
                            crashDetails.setChannel(row.getCell(7).getStringCellValue());
                            crashesList.add(crashDetails);
                        }
                    })
            );
            log.info(String.format("Stop parsing excel file. Rows parsed: %d", crashesList.size()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return crashesList;
    }

    private boolean isRowCorrect(Row row) {
        return row.getCell(0).getCellType().compareTo(CellType.NUMERIC) == 0;
    }

    public void cacheIds(String session, List<CrashDetails> crashesList) {
        log.info(String.format("Cache ids for session: %s", session));
        var ids = crashesList.stream().map(CrashDetails::getId).collect(Collectors.toList());
        cacheManager.getCache("ids").put(session, new CrashesIds(session, ids));
    }

    public List<CrashDetails> getCrashDetailsByIds (String session) throws NotFoundException {
        var crashesFromDb = crashDetailsRepo.findAllById(getFromIdsCache(session).getIds());
        return crashesFromDb;
    }

    @Transactional
    public void deleteCrashDetailsByIds (String session) throws NotFoundException {
        log.info(String.format("Start deleting all for session: %s", session));
        var count = crashDetailsRepo.deleteAllByIds(getFromIdsCache(session).getIds());
        cacheManager.getCache("ids").evictIfPresent(session);
        log.info(String.format("Deleted %d session: %s", count, session));
    }

    private CrashesIds getFromIdsCache(String session) throws NotFoundException {
        var crashesIds = cacheManager.getCache("ids").get(session, CrashesIds.class);
        if (Objects.isNull(crashesIds)) {
            throw new NotFoundException("Can not found ids in cache");
        }
        return crashesIds;
    }
}