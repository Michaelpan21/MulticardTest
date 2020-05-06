package ru.muilticard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class MainController {

    public String getMainPage() {
        return "main";
    }

    @RequestMapping("{name}")
    public String getViewerPage() {
        return "viewer";
    }
}
