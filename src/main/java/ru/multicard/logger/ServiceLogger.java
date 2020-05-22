package ru.multicard.logger;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLogger {

    @Pointcut("execution(* ru.multicard.service.RepairDetailsService.handleFile(..)) && args(username,..))")
    public void handleFile(String username) {
    }

    @Before("handleFile(username)")
    public void beforeHandleFile(String username) {
        log.info(String.format("Handle file for (%s)", username));
    }

    @AfterReturning(value = "handleFile(username)", returning = "count")
    public void afterHandleFile(String username, Integer count) {
        log.info(String.format("Save (%d) rows for (%s)", count, username));
    }

    @Pointcut("execution(* ru.multicard.service.RepairDetailsService.deleteRepairDetailsByIds(..)) && args(username,..))")
    public void deleteRepairDetailsByIds(String username) {
    }

    @AfterReturning(value = "deleteRepairDetailsByIds(username)", returning = "count")
    public void afterDeleteRepairDetailsByIds(String username, Integer count) {
        log.info(String.format("Deleted (%d) rows by user: (%s)", count, username));
    }

    @AfterThrowing(pointcut = "execution(* ru.multicard.service.RepairDetailsService.*(..))", throwing = "ex")
    public void sendToGraylog(Exception ex) {
        //TODO log to Graylog
    }
}
