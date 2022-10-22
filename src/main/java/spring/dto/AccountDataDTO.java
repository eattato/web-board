package spring.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class AccountDataDTO {
    private String email;
    private String pw;
    private String nickname;
    private boolean verify;
    private String vcode;
    private String faceimg;
    private String about = "BOARDARC 유저";
    private boolean isadmin;

    public String getNickname() {
        if (nickname != null) {
            return nickname;
        } else {
            return "익명";
        }
    }

    public String getFaceimg() {
        if (faceimg != null) {
            return faceimg;
        } else {
            return "profiles/default.png";
        }
    }
}
