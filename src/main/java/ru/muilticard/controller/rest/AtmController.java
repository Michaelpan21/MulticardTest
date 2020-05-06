package ru.muilticard.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.muilticard.service.AtmService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("atm")
public class AtmController {

    @Autowired
    private AtmService atmService;

    @PostMapping
    public String saveFile(@RequestBody Map<String, List<List<String>>> fileData) {
        return String.valueOf(atmService.parseFile(fileData));
    }
}
