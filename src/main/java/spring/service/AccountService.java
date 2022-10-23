package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import spring.dao.AccountDao;
import spring.dto.AccountDataDTO;
import spring.dto.SidebarMenu;
import spring.vo.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AccountService {
    @Autowired
    AccountDao accountDao;

    @Autowired
    FileService fileService;

    @Autowired
    MailService mailService;

    private final Marker auditDone = MarkerFactory.getMarker("AUDIT_DONE");
    private final Marker auditTry = MarkerFactory.getMarker("AUDIT_TRY");
    private final Marker auditError = MarkerFactory.getMarker("AUDIT_ERROR");

    // Public Methods
    public boolean checkEmail(String check) {
        return accountDao.emailAvailable(check);
    }

    public String getSession(HttpSession session) {
        Object sessionResult = session.getAttribute("email");
        if (sessionResult != null) {
            String email = sessionResult.toString();
            if (accountDao.emailAvailable(email) == false) {
                return email;
            } else {
                // 존재하지 않거나 삭제된 유저
                return null;
            }
        } else {
            return null;
        }
    }

    public String createAccount(HttpServletRequest request, AccountCreateVO accountData) {
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
            // 유효한 이메일인지 검사
            String filterPattern = "^(?=.{1,320}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
            if (Pattern.compile(filterPattern).matcher(accountData.getEmail()).matches()) {
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
                            } else {
                                HttpSession session = request.getSession();
                                session.setAttribute("email", accountData.getEmail()); // 세션에 계정 정보 저장
                                log.info(auditDone, String.format("%s created new account %s", request.getRemoteAddr(), accountData.getEmail()));
                            }
                        } else {
                            result = "nickname is invalid";
                        }
                    }
                } else {
                    result = "email is already taken";
                }
            } else {
                result = "email is invalid";
            }
        }
        log.info(auditTry, String.format("%s account create try: %s", request.getRemoteAddr(), result));
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
                    log.info(auditDone, String.format("%s logged in to %s", request.getRemoteAddr(), loginData.getEmail()));
                } else {
                    result = "password invalid";
                }
            } else {
                result = "email not found";
            }
        }
        return result;
    }

    // DEBUG PURPOSE ONLY
    public String loginWithoutPassword(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);
        session.setAttribute("email", "eattato0804@naver.com"); // 세션에 로그인한 계정 정보 저장
        log.info(auditDone, String.format("%s logged in to master account", request.getRemoteAddr()));
        return "ok";
    }

    public String logout(HttpServletRequest request) {
        String result = null;
        HttpSession session = request.getSession();
        String sessionData = getSession(session);
        if (sessionData != null) {
            session.invalidate();
            log.info(auditDone, String.format("%s logged out from %s", request.getRemoteAddr(), sessionData));
            result = "ok";
        } else {
            result = "session not found";
        }
        log.info(auditTry, String.format("%s log out try: %s", request.getRemoteAddr(), result));
        return result;
    }

    public String updateProfile(HttpServletRequest request, ProfileVO data) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        String result = null;
        if (sessionData != null) {
            if (data.getPassword() != null) {
                if (shaEncrypt(data.getPassword()).equals(accountDao.getPassword(sessionData))) {
                    if (data.getNickname() != null && data.getAbout() != null) {
                        int nameLength = data.getNickname().length();
                        int aboutLength = data.getAbout().length();
                        if (nameLength > 0 && nameLength <= 50) {
                            if (aboutLength <= 60) {
                                if (accountDao.updateProfile(sessionData, data) == 0) {
                                    result = "failed";
                                } else {
                                    log.info(auditDone, String.format("%s changed %s nickname to %s", request.getRemoteAddr(), sessionData, data.getNickname()));
                                }
                            } else {
                                result = "about is invalid";
                            }
                        } else {
                            result = "nickname is invalid";
                        }
                    }

                    if (result == null) {
                        if (data.getImage() != null) {
                            AccountDataDTO userData = accountDao.getUserData(sessionData);
                            String faceimg = userData.getFaceimg();
                            if (faceimg != null && !faceimg.equals("profiles/default.png") && !faceimg.equals("profiles/delete.png")) {
                                File oldImage = new File("C:/board-saves/" + userData.getFaceimg());
                                oldImage.delete();
                            }

                            File image = fileService.uploadImage(data.getImage());
                            if (image != null && image.exists() == true) {
                                log.info(auditDone, String.format("%s uploaded profile image %s", request.getRemoteAddr(), image.getAbsolutePath()));
//                                log.info(image.toString());
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
                        log.info(auditDone, String.format("%s changed profile of %s", request.getRemoteAddr(), sessionData));
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
        log.info(auditTry, String.format("%s profile change try: %s", request.getRemoteAddr(), result));
        return result;
    }

    public AccountDataDTO sendProfileBySession(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        if (sessionData != null) { // 로그인 세션이 존재하면
            AccountDataDTO profile = getProfile(sessionData);
            if (profile != null) {
                // 이메일로 계정 조회해서 정보를 모델로 전송
                model.addAttribute("email", sessionData);
                model.addAttribute("profile", profile);
                return profile;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public AccountDataDTO getProfile(String email) { // 공개해도 되는 계정 정보만 공개
        AccountDataDTO userData = accountDao.getUserData(email);
        if (userData != null) {
            AccountDataDTO result = new AccountDataDTO();
            result.setEmail(userData.getEmail());
            result.setNickname(userData.getNickname());
            result.setFaceimg(userData.getFaceimg());
            result.setAbout(userData.getAbout());
            result.setVerify(userData.isVerify());
            result.setIsadmin(userData.isIsadmin());
            return result;
        } else {
            AccountDataDTO result = new AccountDataDTO();
            result.setNickname("삭제된 유저");
            result.setFaceimg("profiles/deleted.png");
            return result;
        }
    }

    public String verify(HttpServletRequest request, VerifyVO vo, Model model) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);
        String result = null;
        if (sessionData != null) {
            AccountDataDTO userData = accountDao.getUserData(sessionData);
            if (userData != null) {
                if (userData.isVerify() == false) {
                    if (vo == null) {
                        String vcode = accountDao.generateVerifyCode(userData);
                        userData.setVcode(vcode);
                        mailService.sendVerifyMessage(userData);
                        model.addAttribute("email", sessionData);
                        result = "ok";
                    } else {
                        String vcode = accountDao.getVerifyCode(userData);
                        if (vcode != null && vo.getVcode() != null) {
                            if (vcode.equals(vo.getVcode())) {
                                int success = accountDao.finishVerify(userData);
                                if (success == 1) {
                                    result = "ok";
                                } else {
                                    result = "failed";
                                }
                            } else {
                                result = "code does not match";
                            }
                        } else {
                            result = "code or code input is null";
                        }
                    }
                } else {
                    result = "user is already verified";
                }
            } else {
                result = "no user data";
            }
        } else {
            result = "no session";
        }
        log.info(auditTry, String.format("%s account verify try: %s", request.getRemoteAddr(), result));
        return result;
    }

    public String reset(HttpServletRequest request, VerifyVO vo) {
        String result = null;
        if (vo != null) {
            if (vo.getEmail() != null) {
                AccountDataDTO userData = accountDao.getUserData(vo.getEmail());
                if (userData != null) {
                    if (userData.isVerify() == true) {
                        if (vo.getVcode() == null) { // 인증 코드 없이 제출했으면 코드 발급
                            String vcode = accountDao.generateVerifyCode(userData);
                            userData.setVcode(vcode);
                            mailService.sendResetMessage(userData);
                            result = "ok";
                        } else { // 인증 코드 제출했으면 코드 인증 및 세션 발급
                            String vcode = accountDao.getVerifyCode(userData);
                            if (vcode != null) {
                                if (vcode.equals(vo.getVcode())) {
                                    HttpSession session = request.getSession();
                                    session.setAttribute("reset", userData.getEmail()); // 비밀번호를 바꿀 수 있는 세션
                                    result = "ok";
                                } else {
                                    result = "code does not match";
                                }
                            } else {
                                result = "verify code not found";
                            }
                        }
                    } else {
                        result = "email is not verified";
                    }
                } else {
                    result = "could not find user";
                }
            } else { // 이메일을 제출 안 했다면
                if (vo.getPassword() != null) { // 대신 비밀번호를 제출했으면 세션 확인
                    HttpSession session = request.getSession();
                    Object hasData = session.getAttribute("reset");
                    if (hasData != null) {
                        String pwError = checkPassword(vo.getPassword());
                        if (pwError == null) {
                            vo.setPassword(shaEncrypt(vo.getPassword()));
                            int success = accountDao.resetPassword(hasData.toString(), vo.getPassword());
                            if (success == 1) {
                                result = "ok";
                            } else {
                                result = "failed";
                            }
                        } else {
                            result = pwError;
                        }
                    } else {
                        result = "no access";
                    }
                } else {
                    result = "no email";
                }
            }
        } else {
            result = "no data";
        }
        log.info(auditTry, String.format("%s password reset try: %s", request.getRemoteAddr(), result));
        return result;
    }

    public SidebarMenu loadSidebarMenu(HttpServletRequest request, Model model, PageVO vo) {
        HttpSession session = request.getSession();
        Object sideObj = session.getAttribute("sidebar");
        SidebarMenu sidebar;
        if (sideObj != null) { // 기존 접속 시, 로드
            sidebar = (SidebarMenu) sideObj;
        } else { // 처음 접속 시
            sidebar = new SidebarMenu();
            session.setAttribute("sidebar", sidebar);
        }
        model.addAttribute("sidebar", sidebar);

        if (vo != null) {
            vo.setViewmode(sidebar.getViewmode());
            vo.setSort(sidebar.getSort());
            vo.setDirection(sidebar.getDirection());
            vo.setTitle(sidebar.titleGet());
            vo.setAuthor(sidebar.authorGet());
            vo.setContent(sidebar.contentGet());
            vo.setDate(sidebar.dateGet());
        }

        return sidebar;
    }

    public void setSidebarMenu(HttpServletRequest request, Model model, SidebarMenu menu) {
        SidebarMenu sidebar = loadSidebarMenu(request, model, null);
        HttpSession session = request.getSession();
        session.setAttribute("sidebar", menu);
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
