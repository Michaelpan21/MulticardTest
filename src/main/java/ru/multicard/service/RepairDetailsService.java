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
import java.util.*;
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
     * Обрабатывает приходящий файл Excel, сохраняет строки в базу данных, сохраняет айди ремонтов в кэше
     *
     * @param sessionId куки из запроса
     * @param file файл Excel
     * @return Количество сохраненных строк
     */
    public Integer handleFile(String sessionId, MultipartFile file) {
        log.info(String.format("Handling file for session: %s", sessionId));
        var repairs = parseExcel(file);
        if (!repairs.isEmpty()) {
            var repairsDto = repairDetailsRepo.saveAll(repairs);
            if (!repairsDto.isEmpty()) {
                cacheIds(sessionId, repairsDto);
            }
            return repairsDto.size();
        }
        return 0;
    }

    /**
     * Парсинг Excel файла
     *
     * @param file Excel формата xslx
     * @return Список объектов CrashDetails
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
     * Проверяется, что строка является не заголовком
     * @param row Строка
     * @return true если первая ячейка стобеца содержит число
     */
    private boolean notHeaderRow(Row row) {
        return row.getCell(0).getCellType().equals(CellType.NUMERIC);
    }

    /**
     * Сохранение в кэш айди ремонтов по ключи сессии
     *
     * @param session Сессия пользователя
     * @throws NotFoundException
     */
    private void cacheIds(String session, List<RepairDetails> crashesList) {
        log.info(String.format("Cache ids for session: %s", session));
        var ids = crashesList.stream().map(RepairDetails::getId).collect(Collectors.toList());
        cacheManager.getCache("ids").put(session, new RepairIds(session, ids));
    }

    /**
     * Получение загруженных в базу данных ремонтов
     *
     * @param session Сессия пользователя
     * @return Список объектов CrashDetails
     * @throws NotFoundException
     */
    public List<RepairDetails> getRepairDetailsByIds(String session) throws NotFoundException {
        return repairDetailsRepo.findAllById(getFromIdsCache(session).getIds());
    }

    /**
     * Удаление загруженных в базу данных ремонтов
     *
     * @param session Сессия пользователя
     * @throws NotFoundException
     */
    @Transactional
    public void deleteRepairDetailsByIds(String session) throws NotFoundException {
        log.info(String.format("Start deleting all for session: %s", session));
        var count = repairDetailsRepo.deleteAllByIds(getFromIdsCache(session).getIds());
        cacheManager.getCache("ids").evictIfPresent(session);
        log.info(String.format("Deleted %d rows for session: %s", count, session));
    }

    /**
     * Получения списка айди ремонтов из кеша
     *
     * @param session Сессия пользователя
     * @return Объект, содержщий в себе айди ремонтов
     * @throws NotFoundException
     */
    private RepairIds getFromIdsCache(String session) throws NotFoundException {
        log.info(String.format("Get cached ids for session: %s", session));
        var crashesIds = cacheManager.getCache("ids").get(session, RepairIds.class);
        if (Objects.isNull(crashesIds)) {
            throw new NotFoundException("Can not found ids in cache");
        }
        return crashesIds;
    }

    /**
     * Поиск 3 наиболее часто встречающихся причин неисправности
     *
     * @param session Сессия пользователя
     * @return Список объектов CrashDetails
     * @throws NotFoundException
     */
    public List<RepairDetails> getTop3ByReason(String session) throws NotFoundException {
        var resultList = new ArrayList<RepairDetails>();
        repairDetailsRepo.findAllByIds(getFromIdsCache(session).getIds())
                .stream().collect(Collectors.groupingBy(RepairDetails::getReason))
                .values().stream().sorted((a, b) -> Integer.compare(b.size(), a.size())).limit(3)
                .forEach(resultList::addAll);
        return resultList;
    }

    /**
     * Поиск трех наиболее долгих ремонта
     *
     * @param session Сессия пользователя
     * @return Список объектов CrashDetails
     * @throws NotFoundException
     */
    public List<RepairDetails> getTop3ByTime(String session) throws NotFoundException {
        return repairDetailsRepo.findAllByIds(getFromIdsCache(session).getIds())
                .stream().sorted(Comparator.comparing(a -> Duration.between(a.getEnd(), a.getBegin()))).limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Поиск всех ремонтов у которых причина поломки повторилась в течение 15 дней
     *
     * @param session Сессия пользователя
     * @return Список объектов CrashDetails
     * @throws NotFoundException
     */
    public Set<RepairDetails> getRepeatedRepairs(String session) throws NotFoundException {
        var resultSet = new LinkedHashSet<RepairDetails>();
        var repairList = repairDetailsRepo.findAllByIds(getFromIdsCache(session).getIds());
        repairList.sort(Comparator.comparing(RepairDetails::getReason).thenComparing(RepairDetails::getAtmId)
                .thenComparing(RepairDetails::getBegin));

        for (var it = repairList.listIterator(); it.hasNext(); ) {
            var repair = it.next();
            if (it.hasNext()) {
                var repairNext = it.next();
                if (Objects.equals(repair.getAtmId(), repairNext.getAtmId())
                        && Objects.equals(repair.getReason(), repairNext.getReason())
                        && repair.getEnd().isAfter(repairNext.getBegin().minusDays(15))) {
                    resultSet.add(repair);
                }
            }
        }
        return resultSet;
    }

    public void saveEditedRows(String session, List<RepairDetails> repairs) throws NotFoundException {
        var ids = getFromIdsCache(session).getIds();
        repairs.removeIf(v -> !ids.contains(v.getId()));
        if (!repairs.isEmpty()) {
            repairDetailsRepo.saveAll(repairs);
        }
    }

    @Transactional
    public void deleteRowsByIds(String session, List<Long> repairIds) throws NotFoundException {
        var ids = getFromIdsCache(session).getIds();
        repairIds.removeIf(v -> !ids.contains(v));
        if (!repairIds.isEmpty()) {
            repairDetailsRepo.deleteAllByIds(repairIds);
        }
    }

    public boolean checkFileUploaded(String session) {
        return Objects.nonNull(cacheManager.getCache("ids").get(session, RepairIds.class));
    }
}