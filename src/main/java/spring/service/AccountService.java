package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import spring.dao.AccountDao;
import spring.vo.AccountCreateVO;
import spring.vo.LoginVO;
import spring.vo.ProfileVO;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AccountService {
    @Autowired
    AccountDao accountDao;

    @Autowired
    FileService fileService;

    // Public Methods
    public boolean checkEmail(String check) {
        return accountDao.emailAvailable(check);
    }

    public String getSession(HttpSession session) {
        Object sessionResult = session.getAttribute("email");
        if (sessionResult != null) {
            return sessionResult.toString();
        } else {
            return null;
        }
    }

    public String createAccount(AccountCreateVO accountData) {
        String result = null;
        Map<String, String> acData = accountData.getData();
        for (String key : acData.keySet()) {
            String data = acData.get(key);
            if (data == null) {
                result = key + " is null";
                break;
            }
        }

        if (result == null) {
            // 이메일 안 겹치면
            if (accountDao.emailAvailable(accountData.getEmail()) == true) {
                // 비밀번호 검사
                result = checkPassword(accountData.getPassword());

                if (result == null) {
                    int nameLength = accountData.getNickname().length();
                    if (nameLength > 0 && nameLength < 50) {
                        result = "ok";
                        accountData.setPassword(shaEncrypt(accountData.getPassword()));
                        if (accountDao.createAccount(accountData) == 0) {
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

    public String login(HttpServletRequest request, LoginVO loginData) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        String result = null;
        if (sessionData == null) { // 로그인 되어 있지 않을때에만 로그인 시도 가능
            if (accountDao.emailAvailable(loginData.getEmail()) == false) {
                if (shaEncrypt(loginData.getPassword()).equals(accountDao.getPassword(loginData.getEmail()))) {
                    result = "ok";
                    session.setAttribute("email", loginData.getEmail()); // 세션에 로그인한 계정 정보 저장
                } else {
                    result = "password invalid";
                }
            } else {
                result = "email not found";
            }
        }
        return result;
    }

    public String getLoginData(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        if (sessionData != null) {
            Map<String, Object> profile = accountDao.getUserProfile(sessionData);
            if (profile != null) {
                JSONObject json = new JSONObject();
                json.put("name", profile.get("nickname"));
                json.put("img", profile.get("faceimg"));
                json.put("email", sessionData);
                return json.toJSONString();
            } else {
                return "no profile";
            }
        } else {
            return "no session";
        }
    }

    public String updateProfile(HttpServletRequest request, ProfileVO data) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        String result = null;
        if (sessionData != null) {
            if (data.getPassword() != null) {
                if (shaEncrypt(data.getPassword()).equals(accountDao.getPassword(sessionData))) {
                    if (data.getNickname() != null) {
                        int nameLength = data.getNickname().length();
                        if (nameLength > 0 && nameLength < 50) {
                            if (accountDao.setNickname(sessionData, data.getNickname()) == 0) {
                                result = "failed";
                            }
                        } else {
                            result = "nickname is invalid";
                        }
                    }

                    if (result == null) {
                        if (data.getImage() != null) {
                            File image = fileService.uploadImage(data.getImage());
                            if (image != null) {
                                log.info(image.toString());
                                String[] fullLink = image.toString().split("\\\\");
                                String directory = "";
                                for (int ind = 2; ind < fullLink.length; ind++) {
                                    if (ind == 2) {
                                        directory = fullLink[ind];
                                    } else {
                                        directory = directory + "/" + fullLink[ind];
                                    }
                                }

                                if (accountDao.setProfileImage(sessionData, directory) == 0) {
                                    result = "failed";
                                }
                            }
                        }
                    }

                    if (result == null) {
                        result = "ok";
                    }
                } else {
                    result = "password wrong";
                }
            } else {
                result = "required field is not filled";
            }
        } else {
            result = "no session";
        }
        return result;
    }

    public boolean getProfileBySession(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        if (sessionData != null) { // 로그인 세션이 존재하면
            Map<String, Object> profile = accountDao.getUserProfile(sessionData);
            if (profile != null) {
                // 이메일로 계정 조회해서 정보를 모델로 전송
                model.addAttribute("name", profile.get("nickname").toString());
                if (profile.get("faceimg") != null) {
                    model.addAttribute("img", profile.get("faceimg").toString());
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // DEBUG PURPOSE
    public List<Map<String, Object>> getMembers() {
        return accountDao.getMember(null);
    }

    // Private Methods
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
}
