package spring.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyVO {
    private String email;
    private String vcode;
    private String password;
}
