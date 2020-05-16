package ru.multicard.controller.rest;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.entity.RepairDetails;
import ru.multicard.service.RepairDetailsService;
import ru.multicard.service.util.HeaderUtils;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("atm")
public class RepairDetailsController {

    private final RepairDetailsService repairDetailsService;

    @Autowired
    public RepairDetailsController(RepairDetailsService repairDetailsService) {
        this.repairDetailsService = repairDetailsService;
    }

    @GetMapping
    public List<RepairDetails> getAll(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return repairDetailsService.getRepairDetailsByIds(HeaderUtils.extractSession(cookie));
    }

    @PostMapping
    public Integer saveFile(@RequestHeader("Cookie") String cookie, @RequestBody MultipartFile file) {
        return repairDetailsService.handleFile(HeaderUtils.extractSession(cookie), file);
    }

    @DeleteMapping
    public void deleteFile(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        repairDetailsService.deleteRepairDetailsByIds(HeaderUtils.extractSession(cookie));
    }

    @PutMapping("/row/edit")
    public void editRows(@RequestHeader("Cookie") String cookie,
                         @RequestBody List<RepairDetails> repairs) throws NotFoundException {
        repairDetailsService.saveEditedRows(HeaderUtils.extractSession(cookie), repairs);
    }

    @PostMapping("/row/delete")
    public void deleteRows(@RequestHeader("Cookie") String cookie,
                           @RequestBody List<Long> ids) throws NotFoundException {
        repairDetailsService.deleteRowsByIds(HeaderUtils.extractSession(cookie), ids);
    }

    @GetMapping("/top3/reason")
    public List<RepairDetails> getTop3ByReason(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return repairDetailsService.getTop3ByReason(HeaderUtils.extractSession(cookie));
    }

    @GetMapping("/top3/time")
    public List<RepairDetails> getTop3ByTime(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return repairDetailsService.getTop3ByTime(HeaderUtils.extractSession(cookie));
    }

    @GetMapping("/repair/repeated")
    public Set<RepairDetails> getRepeatedRepairs(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return repairDetailsService.getRepeatedRepairs(HeaderUtils.extractSession(cookie));
    }

}
