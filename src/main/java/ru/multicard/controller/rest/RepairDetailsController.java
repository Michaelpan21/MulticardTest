package ru.multicard.controller.rest;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.entity.RepairDetails;
import ru.multicard.entity.User;
import ru.multicard.service.RepairDetailsService;

import java.util.List;

@RestController
@RequestMapping("atm")
public class RepairDetailsController {

    private final RepairDetailsService repairDetailsService;

    @Autowired
    public RepairDetailsController(RepairDetailsService repairDetailsService) {
        this.repairDetailsService = repairDetailsService;
    }

    @GetMapping
    public List<RepairDetails> getAll(@AuthenticationPrincipal User user) throws NotFoundException {
        return repairDetailsService.getRepairDetailsByIds(user.getUsername());
    }

    @PostMapping
    public Integer saveFile(@AuthenticationPrincipal User user, @RequestBody MultipartFile file) {
        return repairDetailsService.handleFile(user.getUsername(), file);
    }

    @DeleteMapping
    public void deleteFile(@AuthenticationPrincipal User user) throws NotFoundException {
        repairDetailsService.deleteRepairDetailsByIds(user.getUsername());
    }

    @PutMapping("/row/edit")
    public void editRows(@AuthenticationPrincipal User user,
                         @RequestBody List<RepairDetails> repairs) throws NotFoundException {
        repairDetailsService.saveEditedRows(user.getUsername(), repairs);
    }

    @PostMapping("/row/delete")
    public void deleteRows(@AuthenticationPrincipal User user,
                           @RequestBody List<Long> ids) throws NotFoundException {
        repairDetailsService.deleteRowsByIds(user.getUsername(), ids);
    }

    @GetMapping("/top3/reason")
    public List<RepairDetails> getTop3ByReason(@AuthenticationPrincipal User user) throws NotFoundException {
        return repairDetailsService.getTop3ByReason(user.getUsername());
    }

    @GetMapping("/top3/time")
    public List<RepairDetails> getTop3ByTime(@AuthenticationPrincipal User user) throws NotFoundException {
        return repairDetailsService.getTop3ByTime(user.getUsername());
    }

    @GetMapping("/repair/repeated")
    public List<RepairDetails> getRepeatedRepairs(@AuthenticationPrincipal User user) throws NotFoundException {
        return repairDetailsService.getRepeatedRepairs(user.getUsername());
    }

}
