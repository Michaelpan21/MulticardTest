package ru.multicard.controller.rest;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.service.CrashDetailsService;
import ru.multicard.service.util.HeaderUtils;

@RestController
@RequestMapping("atm")
public class CrashDetailsController {

    private final CrashDetailsService crashDetailsService;

    @Autowired
    public CrashDetailsController(CrashDetailsService crashDetailsService) {
        this.crashDetailsService = crashDetailsService;
    }

    @PostMapping
    public String saveFile(@RequestHeader("Cookie") String cookie, @RequestBody MultipartFile file) {
        return String.valueOf(crashDetailsService.handleFile(cookie, file));
    }

    @DeleteMapping
    public void deleteFile(@RequestHeader("Cookie") String cookie) throws NotFoundException {
        crashDetailsService.deleteCrashDetailsByIds(HeaderUtils.extractSession(cookie));
    }
}
