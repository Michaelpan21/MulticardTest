package ru.muilticard.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.muilticard.entity.AtmRepair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("atm")
public class AtmController {

    @PostMapping
    public void saveFile(@RequestBody Map<String, List<List<String>>> fileData) {

    };
}
