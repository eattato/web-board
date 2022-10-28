package spring.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Slf4j
public class CategoryDTO {
    private int id;
    private String category;
    private String about;
    private String img;
    private boolean anonymous;
    private boolean adminonly;
    private int posts;
    private int loved;
    private String admins;

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

    public List<String> getAdminList() {
        return getStringAsList(admins);
    }

    // private methods
    private List<String> getStringAsList(String target) {
        String[] list = target.split(" ");
        if (list.length == 0 && target != null) {
            list = new String[] {target};
        }
        return new ArrayList<>(Arrays.asList(list));
    }
}
