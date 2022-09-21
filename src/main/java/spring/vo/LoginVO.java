package spring.vo;

public class LoginVO {
    // Properties
    private String email;
    private String password;

    // Getter
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setter
    public void setData(String targetEmail, String targetPw) {
        email = targetEmail;
        password = targetPw;
    }
}
