package com.yujongu.coronatracker.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {
    //rest controller get json and return json.
    //controller contains ui.

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("testName", "TEST");
        return "home";
    }
}
