package ru.multicard.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.multicard.service.AtmService;

@RestController
@RequestMapping("atm")
public class AtmController {

    private final AtmService atmService;

    @Autowired
    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping
    public String saveFile(@RequestHeader("Cookie") String cookie, @RequestBody MultipartFile file) {
        return String.valueOf(atmService.handleFile(cookie, file));
    }
}
