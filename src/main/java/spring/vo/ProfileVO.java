package spring.vo;

import lombok.Setter;

@Setter
public class ProfileVO {
    // Properties
    private String password;
    private String nickname;
    private String image;

    // Getter
    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getImage() {
        return image;
    }

    // Setter
    public void setData(String targetPw, String targetName, String targetImage) {
        password = targetPw;
        nickname = targetName;
        image = targetImage;
    }
}
