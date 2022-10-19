package spring.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Dictionary;

@Getter
@Setter
public class SidebarMenu {
    private String viewmode = "simple";
    private boolean title = true;
    private boolean author = true;
    private boolean content = true;
    private boolean date = true;
    private String sort = "new";
    private String direction = "down";

    // getter
    public String titleGet() {
        if (title) {
            return "on";
        } else {
            return "off";
        }
    }

    public String authorGet() {
        if (author) {
            return "on";
        } else {
            return "off";
        }
    }

    public String contentGet() {
        if (content) {
            return "on";
        } else {
            return "off";
        }
    }

    public String dateGet() {
        if (date) {
            return "on";
        } else {
            return "off";
        }
    }
}
