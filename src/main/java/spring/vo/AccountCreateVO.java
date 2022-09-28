package spring.vo;

import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
public class AccountCreateVO {
    // Properties
    private String email;
    private String password;
    private String nickname;

    private boolean encrypted = false;

    // Getter
    public Map<String, String> getData() {
        Map<String, String> result = new HashMap<>();
        result.put("email", email);
        result.put("password", password);
        result.put("nickname", nickname);
        return result;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    // Setter
    public void setData(String targetEmail, String targetPw, String targetName) {
        email = targetEmail;
        password = targetPw;
        nickname = targetName;
    }

    public void setPassword(String target) {
        if (password != null && encrypted == false) {
            encrypted = true;
            password = target;
        }
    }
}
