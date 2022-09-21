package spring.controller;

import java.io.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.service.AccountService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import spring.service.FileService;
import spring.service.PageService;
import spring.vo.PageVO;

@Slf4j
@Controller
public class MainController {
    @Autowired
    AccountService accountService;

    @Autowired
    PageService pageService;

    @GetMapping("/")
    public String main(
            HttpServletRequest request,
            Model model,
            @RequestParam(required = false, defaultValue = "simple") String viewmode,
            @RequestParam(required = false, defaultValue = "1") String page,
            @RequestParam(required = false, defaultValue = "") String search
            ) {
        accountService.getProfileBySession(request, model);

        int pageIndex = Integer.parseInt(page);
        int postPerPage = 0;
        if (viewmode.equals("exact")) {
            postPerPage = 10;
        } else {
            postPerPage = 25;
        }
        PageVO vo = new PageVO();
        vo.setData(pageIndex * postPerPage, (pageIndex + 1) * postPerPage);
        if (search.equals("") == false) {
            vo.setSearch(search, null, null);
        }

        model.addAttribute("categoryData", pageService.getCategoryPage(vo));
        model.addAttribute("page", page);

        return "main";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData == null) { // 로그인 정보가 없다면 들여보내줌
            return "login";
        } else { // 이미 로그인했다면 메인페이지로 보냄
            return "redirect:";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) { // 로그인 세션이 존재하면
            // 이메일로 계정 조회해서 정보를 모델로 전송
            boolean profile = accountService.getProfileBySession(request, model);
            if (profile == true) {
                return "profile";
            } else {
                return "redirect:";
            }
        } else {
            return "redirect:login";
        }
    }
}
