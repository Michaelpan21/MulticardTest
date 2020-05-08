package ru.multicard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ViewerController {

    @GetMapping
    public String getViewerPage(Model model) {
        return "viewer";
    }

}
