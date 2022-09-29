package spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDataDTO {
    private String email;
    private String pw;
    private String nickname;
    private int verify = 0;
    private String faceimg;
    private String about;
    private String isadmin;

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
