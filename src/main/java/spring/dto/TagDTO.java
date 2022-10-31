package spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagDTO {
    private int id;
    private String tagname;
    private String tagdesc;
    private Boolean adminonly = false;
    private String tagcolor;

    // Getter
    public String getTagcolor() {
        if (tagcolor == null) {
            return "000000";
        } else {
            return tagcolor;
        }
    }
}
