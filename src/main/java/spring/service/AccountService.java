package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import spring.dao.AccountDao;
import spring.dto.AccountDataDTO;
import spring.vo.AccountCreateVO;
import spring.vo.LoginVO;
import spring.vo.ProfileVO;
import spring.vo.VerifyVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
@Slf4j
public class AccountService {
    @Autowired
    AccountDao accountDao;

    @Autowired
    FileService fileService;

    @Autowired
    MailService mailService;

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

    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);
        if (sessionData != null) {
            session.invalidate();
            return "ok";
        } else {
            return "session not found";
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
                            AccountDataDTO userData = accountDao.getUserData(sessionData);
                            String faceimg = userData.getFaceimg();
                            if (faceimg != null && !faceimg.equals("profiles/default.png") && !faceimg.equals("profiles/delete.png")) {
                                File oldImage = new File("C:/board-saves/" + userData.getFaceimg());
                                oldImage.delete();
                            }

                            File image = fileService.uploadImage(data.getImage());
                            if (image != null && image.exists() == true) {
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

    public boolean sendProfileBySession(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        String sessionData = getSession(session);

        if (sessionData != null) { // 로그인 세션이 존재하면
            AccountDataDTO profile = getProfile(sessionData);
            if (profile != null) {
                // 이메일로 계정 조회해서 정보를 모델로 전송
                model.addAttribute("email", sessionData);
                model.addAttribute("profile", profile);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public AccountDataDTO getProfile(String email) { // 공개해도 되는 계정 정보만 공개
        AccountDataDTO userData = accountDao.getUserData(email);
        if (userData != null) {
            AccountDataDTO result = new AccountDataDTO();
            result.setEmail(userData.getEmail());
            result.setNickname(userData.getNickname());
            result.setFaceimg(userData.getFaceimg());
            result.setVerify(userData.isVerify());
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
        if (sessionData != null) {
            AccountDataDTO userData = accountDao.getUserData(sessionData);
            if (userData != null) {
                if (userData.isVerify() == false) {
                    if (vo == null) {
                        String vcode = accountDao.generateVerifyCode(userData);
                        userData.setVcode(vcode);
                        mailService.sendVerifyMessage(userData);
                        model.addAttribute("email", sessionData);
                        return "ok";
                    } else {
                        String vcode = accountDao.getVerifyCode(userData);
                        if (vcode != null && vo.getVcode() != null) {
                            if (vcode.equals(vo.getVcode())) {
                                int success = accountDao.finishVerify(userData);
                                if (success == 1) {
                                    return "ok";
                                } else {
                                    return "failed";
                                }
                            } else {
                                return "code does not match";
                            }
                        } else {
                            return "code or code input is null";
                        }
                    }
                } else {
                    return "user is already verified";
                }
            } else {
                return "no user data";
            }
        } else {
            return "no session";
        }
    }

    public String reset(HttpServletRequest request, VerifyVO vo) {
        if (vo != null) {
            if (vo.getEmail() != null) {
                AccountDataDTO userData = accountDao.getUserData(vo.getEmail());
                if (userData != null) {
                    if (userData.isVerify() == true) {
                        if (vo.getVcode() == null) { // 인증 코드 없이 제출했으면 코드 발급
                            String vcode = accountDao.generateVerifyCode(userData);
                            userData.setVcode(vcode);
                            mailService.sendResetMessage(userData);
                            return "ok";
                        } else { // 인증 코드 제출했으면 코드 인증 및 세션 발급
                            String vcode = accountDao.getVerifyCode(userData);
                            if (vcode != null) {
                                if (vcode.equals(vo.getVcode())) {
                                    HttpSession session = request.getSession();
                                    session.setAttribute("reset", userData.getEmail()); // 비밀번호를 바꿀 수 있는 세션
                                    return "ok";
                                } else {
                                    return "code does not match";
                                }
                            } else {
                                return "verify code not found";
                            }
                        }
                    } else {
                        return "email is not verified";
                    }
                } else {
                    return "could not find user";
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
                                return "ok";
                            } else {
                                return "failed";
                            }
                        } else {
                            return pwError;
                        }
                    } else {
                        return "no access";
                    }
                } else {
                    return "no email";
                }
            }
        } else {
            return "no data";
        }
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
