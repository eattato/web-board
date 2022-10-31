package spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagCreateDTO {
    private String tag;
    private String about;
    private String color;
    private boolean admin = false;
}
