package spring.controller;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.dto.*;
import spring.service.AccountService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;

import spring.service.FileService;
import spring.service.MailService;
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

    @Autowired
    MailService mailService;

    @Autowired
    FileService fileService;

    @GetMapping("/")
    public String main(HttpServletRequest request, Model model, PageVO vo) {
        accountService.sendProfileBySession(request, model);
        SidebarMenu sidebar = accountService.loadSidebarMenu(request, model, vo);
        //model.addAttribute("sidebarMode", "default");

        if (vo.getPage() < 1) {
            vo.setPage(1);
        }
        int postPerPage = markPageListToVO(vo);

        List<CategoryDTO> result = pageService.getCategoryList(vo);
        // 카테고리에서 클라이언트가 볼 필요 없는 내용 삭제, 근데 다 공개해도 상관 없는 내용이라 그냥 줌
        model.addAttribute("categoryList", result);
        model.addAttribute("page", vo.getPage());
        model.addAttribute("page", vo.getPage());
        int pageCount = (int)Math.ceil((float)pageService.getCategoryCount() / postPerPage);
        if (pageCount == 0) {
            pageCount = 1;
        }
        model.addAttribute("pageCount", pageCount);
        return "main";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        // DEBUG PURPOSE ONLY
        //accountService.loginWithoutPassword(request);

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
            AccountDataDTO profile = accountService.sendProfileBySession(request, model);
            if (profile != null) {
                return "profile";
            } else {
                return "redirect:";
            }
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/category/{id}")
    public String category(HttpServletRequest request, Model model, PageVO vo, @PathVariable String id) {
        accountService.sendProfileBySession(request, model);
        SidebarMenu sidebar = accountService.loadSidebarMenu(request, model, vo);
        model.addAttribute("sidebarMode", "category");

        if (vo.getPage() < 1) {
            vo.setPage(1);
        }
        try {
            int intid = Integer.parseInt(id);
            vo.setCategory(intid);
            int postPerPage = markPageListToVO(vo);

            CategoryDTO categoryData = pageService.getCategoryData(intid);
            if (categoryData != null) {
                categoryData.setPosts(pageService.getPostCountQuery(vo, 0));
                List<PostDTO> posts = pageService.getPostList(vo, 0);
                markPageListToView(model, posts, categoryData, vo, postPerPage);
                return "category";
            } else {
                log.info("redirect - no category data");
                return "redirect:/";
            }
        } catch (NumberFormatException e) {
            log.info(e.toString());
            log.info("redirect - couldn't get id, got " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/popular")
    public String popularPosts(HttpServletRequest request, Model model, PageVO vo) {
        accountService.sendProfileBySession(request, model);
        SidebarMenu sidebar = accountService.loadSidebarMenu(request, model, vo);
        model.addAttribute("sidebarMode", "popular");
        int postPerPage = markPageListToVO(vo);

        CategoryDTO categoryData = new CategoryDTO();
        categoryData.setCategory("인기글");
        categoryData.setPosts(pageService.getPostCountQuery(vo, 1));
        categoryData.setAbout("카테고리 구분없이 좋아요와 조회수가 높은 게시물들을 모았습니다.");
        List<PostDTO> posts = pageService.getPostList(vo, 1);
        markPageListToView(model, posts, categoryData, vo, postPerPage);
        return "category";
    }

    @GetMapping("/new")
    public String newPosts(HttpServletRequest request, Model model, PageVO vo) {
        accountService.sendProfileBySession(request, model);
        SidebarMenu sidebar = accountService.loadSidebarMenu(request, model, vo);
        model.addAttribute("sidebarMode", "category");
        int postPerPage = markPageListToVO(vo);

        CategoryDTO categoryData = new CategoryDTO();
        categoryData.setCategory("최신글");
        categoryData.setPosts(pageService.getPostCountQuery(vo, 2));
        categoryData.setAbout("카테고리 구분없이 최근에 올라온 게시물들을 모았습니다.");
        List<PostDTO> posts = pageService.getPostList(vo, 2);
        markPageListToView(model, posts, categoryData, vo, postPerPage);
        model.addAttribute("sidebarMode", "new");
        return "category";
    }

    @GetMapping(value = {"/control/{menu}", "/control/{menu}/{page}"})
    public String control(HttpServletRequest request, Model model, @PathVariable String menu, @PathVariable(required = false) Integer page) {
        return pageService.controlPage(request, model, menu, page);
    }

    @GetMapping("/tag/{id}")
    public String tags(HttpServletRequest request, Model model, PageVO vo, @PathVariable String id) {
        accountService.sendProfileBySession(request, model);
        SidebarMenu sidebar = accountService.loadSidebarMenu(request, model, vo);
        model.addAttribute("sidebarMode", "category");

        if (vo.getPage() < 1) {
            vo.setPage(1);
        }
        //try {
            int intid = Integer.parseInt(id);
            vo.setCategory(intid);
            int postPerPage = markPageListToVO(vo);

            TagDTO tagData = pageService.getTagData(intid);
            if (tagData != null) {
                CategoryDTO categoryData = new CategoryDTO();
                categoryData.setCategory("태그: " + tagData.getTagname());
                categoryData.setPosts(pageService.getPostCountQuery(vo, 4));
                categoryData.setAbout("해당 태그가 사용된 글입니다.");
                List<PostDTO> posts = pageService.getPostList(vo, 4);
                markPageListToView(model, posts, categoryData, vo, postPerPage);
                return "category";
            } else {
                log.info("no tag");
                return "redirect:/";
            }
//        } catch (Exception e) {
//            log.info(e.toString());
//            log.info("redirect - couldn't get id, got " + id);
//            return "redirect:/";
//        }
    }

    @GetMapping("/posts/{id}")
    public String posts(HttpServletRequest request, Model model, @PathVariable String id) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            AccountDataDTO profile = accountService.sendProfileBySession(request, model);
        }

        try {
            int intid = Integer.parseInt(id);
            boolean result = pageService.getPost(request, intid, model);
            if (result == true) {
                pageService.addView(intid);
                return "posts";
            } else {
                log.info("no post id: " + intid);
                return "redirect:/";
            }
        } catch (Exception e) {
            log.info(e.toString());
            return "redirect:/";
        }
    }

    @GetMapping(value = {"/editor", "/editor/{id}"})
    public String editor(HttpServletRequest request, Model model, @PathVariable(required = false) String id) {
        return pageService.editor(request, model, id);
    }

    @GetMapping("/verify")
    public String verify(HttpServletRequest request, Model model) {
        String result = accountService.verify(request, null, model);
        if (result.equals("ok")) {
            return "verify";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/reset")
    public String reset(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Object hasData = session.getAttribute("reset");
        if (hasData != null) {
            model.addAttribute("email", hasData.toString());
            return "pwreset";
        } else {
            return "reset";
        }
    }

    @GetMapping("/imgupload")
    public String uploadImage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            log.info("img upload request!");
            //fileService.uploadImage(data.getImage());
            return "redirect:/images/profiles/default.png";
        } else {
            return "no session";
        }
    }

    // Private Methods
    private int markPageListToVO(PageVO vo) {
        if (vo.getPage() < 1) {
            vo.setPage(1);
        }

        int postPerPage = 0;
        if (vo.getViewmode().equals("exact")) {
            postPerPage = 4;
        } else {
            postPerPage = 9;
        }
        vo.setStartIndex((vo.getPage() - 1) * postPerPage);
        vo.setEndIndex(vo.getPage() * postPerPage);
        return postPerPage;
    }

    private void markPageListToView(Model model, List<PostDTO> posts, CategoryDTO categoryData, PageVO vo, int postPerPage) {
        for (PostDTO post : posts) {
            AccountDataDTO authorInfo = accountService.getProfile(post.getAuthor());
            post.setAuthorInfo(authorInfo);
        }
        model.addAttribute("posts", posts);
        model.addAttribute("categoryData", categoryData);
        model.addAttribute("id", vo.getCategoryIndex());
        model.addAttribute("page", vo.getPage());
        int pageCount = (int)Math.ceil((float)categoryData.getPosts() / postPerPage);
        if (pageCount == 0) {
            pageCount = 1;
        }
        model.addAttribute("pageCount", pageCount);
    }
}
