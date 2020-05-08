package ru.multicard.service;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.entity.CrashDetails;
import ru.multicard.repository.CrashDetailsRepo;
import ru.multicard.service.cache.CacheObject;
import ru.multicard.service.util.HeaderUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AtmService {

    @Autowired
    private CrashDetailsRepo crashDetailsRepo;


    public Integer handleFile(String cookie, MultipartFile file) {
        log.info(String.format("Handling file for session: %s", HeaderUtils.extractSession(cookie)));
        var crashes = parseExcel(file);
        if (!crashes.isEmpty()) {
           return cacheIds(HeaderUtils.extractSession(cookie), crashDetailsRepo.saveAll(crashes)).getIds().size();
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
                    if(isRowCorrect(row)) {
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

    @CachePut(cacheNames = "CacheObjects", key = "#key")
    private CacheObject cacheIds(String session, List<CrashDetails> crashes) {
        var ids = crashes.stream().map(CrashDetails::getId).collect(Collectors.toList());
        return new CacheObject(session, ids);
    }
}
