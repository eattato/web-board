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
    private int verify = 0;
    private String faceimg;
    private String about;
    private boolean isadmin;

    public String getNickname() {
        if (nickname != null) {
            return nickname;
        } else {
            return "익명";
        }
    }

    public String getFaceimg() {
        log.info("face get");
        if (faceimg != null) {
            return faceimg;
        } else {
            return "profiles/default.png";
        }
    }
}
