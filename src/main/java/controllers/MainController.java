package controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class MainController {
    @RequestMapping("/")
    public String main(Model model) {
        return "index";
    }

    @GetMapping("/home")
    public String home(Model model) {
        return "contents/home";
    }
}
