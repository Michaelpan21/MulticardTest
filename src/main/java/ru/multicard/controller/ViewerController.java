package ru.multicard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.multicard.service.RepairDetailsService;
import ru.multicard.service.util.HeaderUtils;

@Controller
@RequestMapping("/view")
public class ViewerController {

    private final RepairDetailsService repairDetailsService;

    @Autowired
    ViewerController(RepairDetailsService repairDetailsService) {
        this.repairDetailsService = repairDetailsService;
    }

    @GetMapping
    public String getViewerPage(Model model, @RequestHeader("Cookie") String cookie) {
        if (repairDetailsService.checkFileUploaded(HeaderUtils.extractSession(cookie))) {
            return "viewer";
        }
        return "redirect:/";
    }

}
