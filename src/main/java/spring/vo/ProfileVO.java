package spring.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileVO {
    // Properties
    private String password;
    private String nickname;
    private String image;
    private String about;

    // Setter
    public void setData(String targetPw, String targetName, String targetImage) {
        password = targetPw;
        nickname = targetName;
        image = targetImage;
    }
}
