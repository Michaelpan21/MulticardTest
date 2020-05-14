package ru.multicard.controller.rest;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.entity.CrashDetails;
import ru.multicard.service.CrashDetailsService;
import ru.multicard.service.util.HeaderUtils;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("atm")
public class CrashDetailsController {

    private final CrashDetailsService crashDetailsService;

    @Autowired
    public CrashDetailsController(CrashDetailsService crashDetailsService) {
        this.crashDetailsService = crashDetailsService;
    }

    @GetMapping
    public List<CrashDetails> getAll(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return crashDetailsService.getCrashDetailsByIds(HeaderUtils.extractSession(cookie));
    }

    @PostMapping
    public String saveFile(@RequestHeader("Cookie") String cookie, @RequestBody MultipartFile file) {
        return String.valueOf(crashDetailsService.handleFile(cookie, file));
    }

    @DeleteMapping
    public void deleteFile(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        crashDetailsService.deleteCrashDetailsByIds(HeaderUtils.extractSession(cookie));
    }

    @PutMapping
    public void editRows(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        crashDetailsService.deleteCrashDetailsByIds(HeaderUtils.extractSession(cookie));
    }

    @GetMapping("/top3/reason")
    public List<CrashDetails> getTop3ByReason(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return crashDetailsService.getTop3ByReason(HeaderUtils.extractSession(cookie));
    }

    @GetMapping("/top3/time")
    public List<CrashDetails> getTop3ByTime(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return crashDetailsService.getTop3ByTime(HeaderUtils.extractSession(cookie));
    }

    @GetMapping("/repair/repeated")
    public Set<CrashDetails> getRepeatedRepairs(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        return crashDetailsService.getRepeatedRepairs(HeaderUtils.extractSession(cookie));
    }

}
