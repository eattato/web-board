package spring.vo;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
    public void setEncrypted() {
        log.info("what, attempted to exploit with vo??");
    }

    public void setPassword(String target) {
        if (encrypted == false) { // 한 번만 열어줌
            encrypted = true;
            password = target;
        }
    }
}
