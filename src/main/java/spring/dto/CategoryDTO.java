package spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private int id;
    private String category;
    private String about;
    private String img;
    private boolean anonymous;
    private boolean adminonly;
    private int posts;
    private int loved;

    public String getImg() {
        if (img == null) {
            return "categories/default.png";
        } else {
            return img;
        }
    }

    public void setAbout() {
        about = "글을 둘러보세요";
    }
}
