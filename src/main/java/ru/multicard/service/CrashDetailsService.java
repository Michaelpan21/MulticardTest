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
import java.time.Duration;
import java.util.*;
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

    /**
     * Парсинг Excel файла
     * @param file Excel формата xslx
     * @return Список объектов CrashDetails
     */
    private List<CrashDetails> parseExcel(MultipartFile file) {
        var crashesList = new ArrayList<CrashDetails>();
        try {
            log.info("Start parsing excel file..");

            @Cleanup var in = file.getInputStream();
            @Cleanup var wb = new XSSFWorkbook(in);

            wb.forEach(sheet ->
                    sheet.forEach(row -> {
                        if (isRowCorrect(row)) {
                            crashesList.add(new CrashDetails() {{
                                    setId((long) row.getCell(0).getNumericCellValue());
                                    setAtmId(((XSSFCell) row.getCell(1)).getRawValue());
                                    setAtmSerialNumber(((XSSFCell) row.getCell(5)).getRawValue());
                                    setBankName(row.getCell(6).getStringCellValue());
                                    setReason(row.getCell(2).getStringCellValue());
                                    setBegin(row.getCell(3).getLocalDateTimeCellValue());
                                    setEnd(row.getCell(4).getLocalDateTimeCellValue());
                                    setChannel(row.getCell(7).getStringCellValue());
                            }});
                        }
                    })
            );
            log.info(String.format("Stop parsing excel file. Rows parsed: %d", crashesList.size()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return crashesList;
    }

    /**
     * Проверяется, что строка является не заголовком
     * @param row Строка
     * @return
     */
    private boolean isRowCorrect(Row row) {
        return row.getCell(0).getCellType().compareTo(CellType.NUMERIC) == 0;
    }

    /**
     * Сохранение в кэш айди ремонтов по ключи сессии
     *
     * @param session Сессия пользователя
     * @return
     * @throws NotFoundException
     */
    private void cacheIds(String session, List<CrashDetails> crashesList) {
        log.info(String.format("Cache ids for session: %s", session));
        var ids = crashesList.stream().map(CrashDetails::getId).collect(Collectors.toList());
        cacheManager.getCache("ids").put(session, new CrashesIds(session, ids));
    }

    /**
     * Получение загруженных в базу данных ремонтов
     *
     * @param session Сессия пользователя
     * @return
     * @throws NotFoundException
     */
    public List<CrashDetails> getCrashDetailsByIds (String session) throws NotFoundException {
        return crashDetailsRepo.findAllById(getFromIdsCache(session).getIds());
    }

    /**
     * Удаление загруженных в базу данных ремонтов
     *
     * @param session Сессия пользователя
     * @return
     * @throws NotFoundException
     */
    @Transactional
    public void deleteCrashDetailsByIds (String session) throws NotFoundException {
        log.info(String.format("Start deleting all for session: %s", session));
        var count = crashDetailsRepo.deleteAllByIds(getFromIdsCache(session).getIds());
        cacheManager.getCache("ids").evictIfPresent(session);
        log.info(String.format("Deleted %d rows for session: %s", count, session));
    }

    /**
     * Получения списка айди ремонтов из кеша
     *
     * @param session Сессия пользователя
     * @return
     * @throws NotFoundException
     */
    private CrashesIds getFromIdsCache(String session) throws NotFoundException {
        var crashesIds = cacheManager.getCache("ids").get(session, CrashesIds.class);
        if (Objects.isNull(crashesIds)) {
            throw new NotFoundException("Can not found ids in cache");
        }
        return crashesIds;
    }

    /**
     * Поиск 3 наиболее часто встречающихся причин неисправности
     *
     * @param session
     * @return
     * @throws NotFoundException
     */
    public List<CrashDetails> getTop3ByReason(String session) throws NotFoundException {
        var resultList = new ArrayList<CrashDetails>();
        crashDetailsRepo.findAllByIds(getFromIdsCache(session).getIds())
                .stream().collect(Collectors.groupingBy(CrashDetails::getReason))
                .values().stream().sorted((a, b) -> Integer.compare(b.size(), a.size())).limit(3)
                .forEach(resultList::addAll);
        return resultList;
    }


    /**
     * Поиск трех наиболее долгих ремонта
     *
     * @param session Сессия пользователя
     * @return
     * @throws NotFoundException
     */
    public List<CrashDetails> getTop3ByTime(String session) throws NotFoundException {
        return crashDetailsRepo.findAllByIds(getFromIdsCache(session).getIds())
                .stream().sorted(Comparator.comparing(a -> Duration.between(a.getEnd(), a.getBegin()))).limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Поиск всех ремонтов у которых причина поломки повторилась в течение 15 дней
     *
     * @param session Сессия пользователя
     * @return
     * @throws NotFoundException
     */
    public Set<CrashDetails> getRepeatedRepairs(String session) throws NotFoundException {
        var resultSet = new LinkedHashSet<CrashDetails>();
        var sortedByDate = crashDetailsRepo.findAllByIds(getFromIdsCache(session).getIds());
        sortedByDate.sort(Comparator.comparing(CrashDetails::getReason).thenComparing(CrashDetails::getAtmId)
                .thenComparing(CrashDetails::getBegin));

        for (var it = sortedByDate.listIterator(); it.hasNext(); ) {
            var repair = it.next();
            if (it.hasNext()) {
                var repairNext = it.next();
                if (Objects.equals(repair.getAtmId(), repairNext.getAtmId())
                        && Objects.equals(repair.getReason(), repairNext.getReason())
                        && repair.getEnd().isAfter(repairNext.getBegin().minusDays(15))) {
                    resultSet.add(repair);
                    resultSet.add(repairNext);
                }
            }
        }
        return resultSet;

//var resultList = new ArrayList<CrashDetails>();
        //var map = new HashMap<String, List<CrashDetails>>();

       /* crashDetailsRepo.findAllByIds(getFromIdsCache(session).getIds())
                .stream().collect(Collectors.groupingBy(CrashDetails::getReason))
                .values().forEach(list -> list.stream().collect(Collectors.groupingBy(CrashDetails::getAtmId))
                .values().stream().filter(v -> v.size() > 1).forEach(v -> v.stream().sorted(Comparator.comparing(CrashDetails::getBegin)).forEach(repair -> {
                        System.out.println(repair.getAtmId() + " " + repair.getReason() + " " + repair.getBegin() + " " + repair.getEnd());
                })));*/

        /*var repairs = crashDetailsRepo.findAllByIds(getFromIdsCache(session).getIds());
        repairs.forEach(repair -> {     if(v.get(0).getEnd().isAfter(repair.getBegin().minusDays(15)))
            repairs.stream().s
                });*/
        /*crashDetailsRepo.findAllByIds(getFromIdsCache(session).getIds()).forEach(crash ->
                map.compute(crash.getReason(), (k, v) ->
                        v == null ? new ArrayList<>() : v.get(0).getEnd().isAfter(crash.getBegin().minusDays(15)) ?
                v add(crash) : v));  List<HashMap<String, List<CrashDetails>

                forEach(v -> v.stream().skip(1).forEach(repair -> {
                    if(v.get(0).getEnd().isBefore(repair.getBegin().minusDays(15))) {
                        v.remove(repair);
                    }
                    resultList.addAll(v);
                })));*/
       // resultList.forEach(System.out.println(repair.toString() + "  -  " + Duration.between(v.get(0).getEnd(), repair.getBegin()).toDays()););

    }

}