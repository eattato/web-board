package spring.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import spring.dto.SidebarMenu;
import spring.service.AccountService;
import spring.service.FileService;
import spring.service.MailService;
import spring.service.PageService;
import spring.vo.*;

@Slf4j
@RestController
public class ApiController {
    @Autowired
    AccountService accountService;

    @Autowired
    FileService fileService;

    @Autowired
    PageService pageService;

    @Autowired
    MailService mailService;

    @GetMapping("/check")
    public boolean email(@RequestParam(value = "target") String check) {
        return accountService.checkEmail(check);
    }

    @PostMapping("/account")
    public String createAccount(HttpServletRequest request, @RequestBody AccountCreateVO vo) {
//        String email = request.getParameter("email");
//        String password = request.getParameter("password");
//        String nickname = request.getParameter("nickname");
//        AccountCreateVO vo = new AccountCreateVO();
//        vo.setData(email, password, nickname);
//        mailService.sendVerifyMessage(email);
        return accountService.createAccount(request, vo);
    }

    @PostMapping("/access")
    public String loginAccount(HttpServletRequest request, @RequestBody LoginVO vo) {
        return accountService.login(request, vo);
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        return accountService.logout(request);
    }

//    @GetMapping("/logindata")
//    public String loginSession(HttpServletRequest request) {
//        return accountService.getLoginData(request);
//    }

    @PostMapping("/setprofile")
    public String setProfile(HttpServletRequest request, @RequestBody ProfileVO vo) {
        String result = null;
//        String password = request.getParameter("password");
//        String nickname = request.getParameter("nickname");
//        String image = request.getParameter("image");
//        ProfileVO vo = new ProfileVO();
//        vo.setData(password, nickname, image);
        return accountService.updateProfile(request, vo);
    }

    //@RequestMapping(value = "/images/{path}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @GetMapping("/images/**")
    public ResponseEntity<?> getImage(HttpServletRequest request) throws IOException {
        String[] fullLink = request.getRequestURI().split("/");
        String directory = "";
        for (int ind = 2; ind < fullLink.length; ind++) {
            directory = directory + "/" + fullLink[ind];
        }
        String basicPath = "C:/board-saves";
        directory = basicPath + directory;
        File imageFile = new File(directory);

        if (imageFile.exists() == true && imageFile.isFile() == true) {
            //log.info("returning " + path);
            InputStream input = new FileInputStream(imageFile);
            byte[] result = new byte[(int) imageFile.length()];
            input.read(result);
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.IMAGE_PNG);
            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(result, header, HttpStatus.OK);
            return responseEntity;
        } else {
            //log.info("image not found");
            String error = "image not found";
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.TEXT_PLAIN);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(error, header, HttpStatus.OK);
            return responseEntity;
        }
    }

    @PostMapping("/tempupload")
    public ResponseEntity<?> tempUpload(HttpServletRequest request) {
        String result = fileService.addTempImage(request);
        if (result != "session not found" || result != "no image") {
            byte[] image = result.getBytes();
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.IMAGE_PNG);
            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(image, header, HttpStatus.OK);
            return responseEntity;
        } else {
            String error = result;
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.TEXT_PLAIN);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(error, header, HttpStatus.OK);
            return responseEntity;
        }
    }

    @PostMapping("/post")
    public String uploadPost(HttpServletRequest request, @RequestBody PostVO vo) {
        if (vo.isRemove() == false) {
            return pageService.post(request, vo);
        } else {
            return pageService.removePost(request, vo.getId());
        }
    }

    @PostMapping("/recommend")
    public String recommend(HttpServletRequest request, @RequestBody RecommendVO vo) {
        return pageService.pressRecommend(request, vo);
    }

    @PostMapping("/comment")
    public String uploadComment(HttpServletRequest request, @RequestBody CommentVO vo) {
        if (vo.isRemove() == false) {
            return pageService.comment(request, vo);
        } else {
            return pageService.removeComment(request, vo.getId());
        }
    }

    @PostMapping("/verify")
    public String verify(HttpServletRequest request, @RequestBody VerifyVO vo) {
        return accountService.verify(request, vo, null);
    }

    @PostMapping("/reset")
    public String reset(HttpServletRequest request, @RequestBody VerifyVO vo) {
        return accountService.reset(request, vo);
    }

    @PostMapping("/cancel")
    public String resetCancel(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object hasData = session.getAttribute("reset");
        if (hasData != null) {
            session.setAttribute("reset", null);
        }
        return "ok";
    }

    @PostMapping("/sidebar")
    public String updateSidebar(HttpServletRequest request, Model model, @RequestBody SidebarMenu target) {
        accountService.setSidebarMenu(request, model, target);
        return "ok";
    }
}
