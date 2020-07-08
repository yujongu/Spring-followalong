package com.yujongu.coronatracker.Controllers;

import com.yujongu.coronatracker.Models.CountryStats;
import com.yujongu.coronatracker.services.CoronaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class homeController {
    //rest controller get json and return json.
    //controller contains ui.

    @Autowired
    CoronaDataService coronaVirusDataService;

    @GetMapping("/")
    public String home(Model model){
        ArrayList<CountryStats> stats = coronaVirusDataService.getKoreaStats();
        int diff = stats.get(0).getCount() - stats.get(1).getCount();
        model.addAttribute("koreaStats", stats);
        model.addAttribute("newPatients", diff);
        return "home";
    }
}
