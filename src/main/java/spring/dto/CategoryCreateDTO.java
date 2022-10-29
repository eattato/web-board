package spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDTO {
    private String category;
    private String about;
    private String image;
}
