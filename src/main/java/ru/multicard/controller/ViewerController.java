package ru.multicard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.multicard.entity.User;
import ru.multicard.service.RepairDetailsService;

@Controller
@RequestMapping("/view")
public class ViewerController {

    private final RepairDetailsService repairDetailsService;

    @Autowired
    ViewerController(RepairDetailsService repairDetailsService) {
        this.repairDetailsService = repairDetailsService;
    }

    @GetMapping
    public String getViewerPage(Model model, @AuthenticationPrincipal User user) {
        if (repairDetailsService.checkFileUploaded(user.getUsername())) {
            return "viewer";
        }
        return "redirect:/";
    }

}
