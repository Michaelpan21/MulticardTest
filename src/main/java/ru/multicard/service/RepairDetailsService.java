package ru.multicard.service;

import javassist.NotFoundException;
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
import ru.multicard.entity.RepairDetails;
import ru.multicard.repository.RepairDetailsRepo;
import ru.multicard.service.cache.RepairIds;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RepairDetailsService {

    private final RepairDetailsRepo repairDetailsRepo;
    private final CacheManager cacheManager;


    @Autowired
    public RepairDetailsService(RepairDetailsRepo repairDetailsRepo, CacheManager cacheManager) {
        this.repairDetailsRepo = repairDetailsRepo;
        this.cacheManager = cacheManager;
    }


    /**
     * Handles Excel file.
     *
     * <p>Uses for parse Excel file, then save data in database.
     * Also caches ids of repairs to provide operations with parsed file</p>
     *
     * @param username username
     * @param file  Excel file
     * @return count of saved rows
     */
    public Integer handleFile(String username, MultipartFile file) {
        log.info(String.format("Handling file for user: %s", username));
        var repairs = parseExcel(file);
        if (!repairs.isEmpty()) {
            var repairsDto = repairDetailsRepo.saveAll(repairs);
            if (!repairsDto.isEmpty()) {
                cacheIds(username, repairsDto);
            }
            return repairsDto.size();
        }
        return 0;
    }

    /**
     * Parses Excel file from {@link MultipartFile}.
     *
     * <p>Creates list of {@link RepairDetails} objects from rows.
     * Uses Apache POI library {@see XSSFWorkbook} for this operation</p>
     *
     * @param file Excel file .xslx
     * @return list of {@link RepairDetails} objects
     */
    private List<RepairDetails> parseExcel(MultipartFile file) {
        var repairsList = new ArrayList<RepairDetails>();
        try (var wb = new XSSFWorkbook(file.getInputStream())) {
            log.info("Start parsing excel file..");
            wb.forEach(sheet ->
                    sheet.forEach(row -> {
                        if (notHeaderRow(row)) {
                            var repair = new RepairDetails();
                            repair.setId((long) row.getCell(0).getNumericCellValue());
                            repair.setAtmId(((XSSFCell) row.getCell(1)).getRawValue());
                            repair.setAtmSerialNumber(((XSSFCell) row.getCell(5)).getRawValue());
                            repair.setBankName(row.getCell(6).getStringCellValue());
                            repair.setReason(row.getCell(2).getStringCellValue());
                            repair.setBegin(row.getCell(3).getLocalDateTimeCellValue());
                            repair.setEnd(row.getCell(4).getLocalDateTimeCellValue());
                            repair.setChannel(row.getCell(7).getStringCellValue());
                            repairsList.add(repair);
                        }
                    })
            );
            log.info(String.format("Stop parsing excel file. Rows parsed: %d", repairsList.size()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return repairsList;
    }

    /**
     * Check that first column is a number.
     * Use for skip header row
     *
     * @param row row from file
     * @return true if its not a header row
     */
    private boolean notHeaderRow(Row row) {
        return row.getCell(0).getCellType().equals(CellType.NUMERIC);
    }

    /**
     * Save ids of repairs to cache in {@link RepairIds} object
     *
     * @param username username, uses as key in cache map
     */
    private void cacheIds(String username, List<RepairDetails> repairsList) {
        log.info(String.format("Cache ids for user: %s", username));
        var ids = repairsList.stream().map(RepairDetails::getId).collect(Collectors.toList());
        cacheManager.getCache("ids").put(username, new RepairIds(username, ids));
    }

    /**
     * Gets repairs from database.
     *
     * <p>Uses username for getting saved ids of parsed file in cache
     * and finds rows with this ids in database</p>
     *
     * @param username username
     * @return list of {@link RepairDetails} from db
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    public List<RepairDetails> getRepairDetailsByIds(String username) throws NotFoundException {
        return repairDetailsRepo.findAllById(getFromIdsCache(username).getIds());
    }

    /**
     * Deletes loaded repairs from database.
     *
     * <p>Uses username for getting saved ids of parsed file in cache
     * and finds rows with this ids in database</p>
     *
     * @param username username
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    @Transactional
    public void deleteRepairDetailsByIds(String username) throws NotFoundException {
        log.info(String.format("Start deleting all for user: %s", username));
        var count = repairDetailsRepo.deleteAllByIds(getFromIdsCache(username).getIds());
        cacheManager.getCache("ids").evictIfPresent(username);
        log.info(String.format("Deleted %d rows for user: %s", count, username));
    }

    /**
     * Gets {@link RepairIds} with repairs ids by username
     *
     * <p>Gets special object from cache by username,
     * uses for matching database rows with rows parsed from saved file </p>
     *
     * @param username username uses as key in cache map
     * @return Object with cached ids {@link RepairIds}
     * @throws NotFoundException if saved object wasn't found in cache
     */
    private RepairIds getFromIdsCache(String username) throws NotFoundException {
        log.info(String.format("Get cached ids for user: %s", username));
        var crashesIds = cacheManager.getCache("ids").get(username, RepairIds.class);
        if (Objects.isNull(crashesIds)) {
            throw new NotFoundException("Can not found ids in cache");
        }
        return crashesIds;
    }

    /**
     * Finds three the most common reasons of crashes.
     *
     * <p>Groups rows by reasons, then finds three groups having largest size</p>
     *
     * @param username username
     * @return list of {@link RepairDetails} from db
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    public List<RepairDetails> getTop3ByReason(String username) throws NotFoundException {
        var resultList = new ArrayList<RepairDetails>();
        repairDetailsRepo.findAllByIds(getFromIdsCache(username).getIds())
                .stream().collect(Collectors.groupingBy(RepairDetails::getReason))
                .values().stream().sorted((a, b) -> Integer.compare(b.size(), a.size())).limit(3)
                .forEach(resultList::addAll);
        return resultList;
    }

    /**
     * Finds three the longest cases of repairs.
     *
     * <p>Sorts rows by repairs time desc and gets first three</p>
     *
     * @param username username
     * @return list of {@link RepairDetails} from db
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    public List<RepairDetails> getTop3ByTime(String username) throws NotFoundException {
        return repairDetailsRepo.findAllByIds(getFromIdsCache(username).getIds())
                .stream().sorted(Comparator.comparing(a -> Duration.between(a.getEnd(), a.getBegin()))).limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Finds all repairs where repair's reason was repeated in 15 days.
     *
     * <p>Sorts rows by reason, atm's id and begin time of repairing acs,
     * then compares row with next to detect that time between repairs is less than 15 days</p>
     *
     * @param username username
     * @return list of {@link RepairDetails} from db
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    public List<RepairDetails> getRepeatedRepairs(String username) throws NotFoundException {
        var resultList = new ArrayList<RepairDetails>();
        var repairList = repairDetailsRepo.findAllByIds(getFromIdsCache(username).getIds());
        repairList.sort(Comparator.comparing(RepairDetails::getReason).thenComparing(RepairDetails::getAtmId)
                .thenComparing(RepairDetails::getBegin));

        for (var it = repairList.listIterator(); it.hasNext(); ) {
            var repair = it.next();
            if (it.hasNext()) {
                var repairNext = it.next();
                if (Objects.equals(repair.getAtmId(), repairNext.getAtmId())
                        && Objects.equals(repair.getReason(), repairNext.getReason())
                        && repair.getEnd().isAfter(repairNext.getBegin().minusDays(15))) {
                    resultList.add(repair);
                }
            }
        }
        return resultList;
    }

    /**
     * Saves edited repairs in database.
     *
     * <p>Checks cached ids of parsed file, that user is editing now.
     * This check prevents editing rows not from current file</p>
     *
     * @param username username
     * @param repairs list of edited repairs for save {@link RepairDetails}
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    public void saveEditedRows(String username, List<RepairDetails> repairs) throws NotFoundException {
        var ids = getFromIdsCache(username).getIds();
        repairs.removeIf(v -> !ids.contains(v.getId()));
        if (!repairs.isEmpty()) {
            repairDetailsRepo.saveAll(repairs);
        }
    }

    /**
     * Deletes repairs from database
     *
     * <p>Checks cached ids of parsed file, that user is editing now.
     * This check prevents deleting rows not from current file</p>
     *
     * @param username username
     * @param repairIds list of repair's ids for delete
     * @throws NotFoundException {@link RepairDetailsService#getFromIdsCache}
     */
    @Transactional
    public void deleteRowsByIds(String username, List<Long> repairIds) throws NotFoundException {
        var ids = getFromIdsCache(username).getIds();
        repairIds.removeIf(v -> !ids.contains(v));
        if (!repairIds.isEmpty()) {
            repairDetailsRepo.deleteAllByIds(repairIds);
        }
    }

    /**
     * Checks that user uploaded file and app can show him overview page.
     *
     * @param username username
     * @return true if file was uploaded
     */
    public boolean checkFileUploaded(String username) {
        return Objects.nonNull(cacheManager.getCache("ids").get(username, RepairIds.class));
    }
}