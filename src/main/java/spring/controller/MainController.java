package spring.controller;

import java.io.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.dto.AccountDataDTO;
import spring.dto.CategoryDTO;
import spring.dto.PostDTO;
import spring.service.AccountService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import spring.service.FileService;
import spring.service.PageService;
import spring.vo.PageVO;
import spring.vo.ProfileVO;

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
        accountService.sendProfileBySession(request, model);

        int pageIndex = Integer.parseInt(page);
        int postPerPage = 0;
        if (viewmode.equals("exact")) {
            postPerPage = 10;
        } else {
            postPerPage = 25;
        }
        PageVO vo = new PageVO();
        vo.setData((pageIndex - 1) * postPerPage, pageIndex * postPerPage);
        if (search.equals("") == false) {
            vo.setSearch(search, null, null);
        }

        List<CategoryDTO> result = pageService.getCategoryList(vo);
        // 카테고리에서 클라이언트가 볼 필요 없는 내용 삭제, 근데 다 공개해도 상관 없는 내용이라 그냥 줌
        model.addAttribute("categoryList", result);
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
            boolean profile = accountService.sendProfileBySession(request, model);
            if (profile == true) {
                return "profile";
            } else {
                return "redirect:";
            }
        } else {
            return "redirect:login";
        }
    }

    @GetMapping("/category/{id}")
    public String category(HttpServletRequest request,
                           Model model,
                           @RequestParam(required = false, defaultValue = "simple") String viewmode,
                           @RequestParam(required = false, defaultValue = "1") String page,
                           @RequestParam(required = false, defaultValue = "") String search,
                           @PathVariable String id
    ) {
        accountService.sendProfileBySession(request, model);
//        try {
            int intid = Integer.parseInt(id);

            PageVO vo = new PageVO();
            vo.setCategory(intid);
            int pageIndex = Integer.parseInt(page);
            int postPerPage = 0;
            if (viewmode.equals("exact")) {
                postPerPage = 10;
            } else {
                postPerPage = 25;
            }
            vo.setData((pageIndex - 1) * postPerPage, pageIndex * postPerPage);
            if (search.equals("") == false) {
                vo.setSearch(search, null, null);
            }

            CategoryDTO categoryData = pageService.getCategoryData(intid);
            if (categoryData != null) {
                List<PostDTO> posts = pageService.getPostList(vo);
                for (PostDTO post : posts) {
                    AccountDataDTO authorInfo = accountService.getProfile(post.getAuthor());
                    post.setAuthorInfo(authorInfo);
                }
                model.addAttribute("posts", posts);
                model.addAttribute("categoryData", categoryData);
                return "category";
            } else {
                log.info("redirect - no category data");
                return "redirect:/";
            }
//        } catch (Exception e) {
//            log.info("redirect - couldn't get id, got " + id);
//            return "redirect:/";
//        }
    }

    @GetMapping("/posts/{id}")
    public String posts(HttpServletRequest request, Model model, @PathVariable String id) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            boolean profile = accountService.sendProfileBySession(request, model);
        }

        try {
            int intid = Integer.parseInt(id);
            boolean result = pageService.getPost(request, intid, model);
            if (result == true) {
                pageService.addView(intid);
                return "posts";
            } else {
                return "redirect:/";
            }
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @GetMapping("/editor")
    public String editor(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) { // 로그인 세션이 존재하면
            // 이메일로 계정 조회해서 정보를 모델로 전송
            boolean profile = accountService.sendProfileBySession(request, model);

            PageVO vo = new PageVO();
            vo.setData(0, -1);
            model.addAttribute("categoryList", pageService.getCategoryList(vo));

            if (profile == true) {
                return "editor";
            } else {
                return "redirect:";
            }
        } else {
            return "redirect:login";
        }
    }
}
