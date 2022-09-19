package spring.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spring.dao.Dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RestController
public class ApiController {
    @Autowired
    Dao dao;

    @GetMapping("/check")
    public boolean email(@RequestParam(value = "target") String check) {
        return dao.emailAvailable(check);
    }

    @PostMapping("/account")
    public String createAccount(HttpServletRequest request) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String[] essentialParameters = new String[] {"email", "password", "nickname"};
        log.info("email: " + email + ", password: " + password + ", nickname: " + nickname);

        String result = null;
        for (int ind = 0; ind < essentialParameters.length; ind++) {
            String param = essentialParameters[ind];
            if (request.getParameter(param) == null) {
                result = param + " is null";
                break;
            }
        }

        if (result == null) {
            // 이메일 안 겹치면
            if (dao.emailAvailable(email) == true) {
                // 비밀번호 검사
                result = checkPassword(password);

                if (result == null) {
                    int nameLength = request.getParameter("nickname").length();
                    if (nameLength > 0 && nameLength < 50) {
                        result = "ok";
                        password = shaEncrypt(password);
                        if (dao.createAccount(email, password, nickname) == 0) {
                            result = "failed";
                        }
                    } else {
                        result = "nickname is invalid";
                    }
                }
            } else {
                result = "email invalid";
            }
        }
        return result;
    }

    @PostMapping("/access")
    public String loginAccount(HttpServletRequest request) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String error = null;
        if (dao.emailAvailable(email) == false) {
            if (shaEncrypt(password) == dao.getPassword(email)) {
                error = "ok";
                HttpSession session = request.getSession();
                session.setAttribute("email", email);
            } else {
                error = "password invalid";
            }
        } else {
            error = "email not found";
        }
        return error;
    }

    // 비밀번호 보안 확인 함수
    private String checkPassword(String pw) {
        String result = null;
        boolean hasEng = false;
        boolean hasNum = false;
        boolean hasSpc = false;
        for (int ind = 0; ind < pw.length(); ind++) {
            int word = (int)pw.charAt(ind);
            if ((word >= 65 && word <= 90) || (word >= 97 && word <= 122)) {
                hasEng = true;
            } else if (word >= 48 && word <= 57) {
                hasNum = true;
            } else if (word >= 33 && word <= 38) {
                hasSpc = true;
            } else {
                result = "password is invalid";
                break;
            }
        }
        if (!hasEng || !hasNum || !hasSpc) {
            result = "password is invalid";
        }
        return result;
    }

    private String shaEncrypt(String target) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(target.getBytes());
        StringBuilder builder = new StringBuilder();
        for (byte b: md.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @GetMapping("/members")
    public List<Map<String, ?>> members() {
        return dao.getMember(null);
    }
}
