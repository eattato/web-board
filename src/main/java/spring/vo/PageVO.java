package spring.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PageVO {
    private int categoryIndex;
    private int startIndex;
    private int endIndex;

    private int page;
    private String search;
    private String viewmode = "simple";
    private boolean title = false;
    private boolean author = false;
    private boolean content = false;
    private boolean date = false;

    // getter
    public int getStart() {
        return startIndex;
    }

    public int getEnd() {
        return endIndex;
    }

    public boolean hasSearch() {
        if (title || author || content || date) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> getSearchTargets() {
        List<String> targets = new ArrayList<>();
        if (title) {
            targets.add("title");
        }
        if (author) {
            targets.add("author");
        }
        if (content) {
            targets.add("content");
        }
        if (date) {
            targets.add("date");
        }
        return targets;
    }

    // setter
    public void setTitle(String to) {
        if (to.equals("on")) {
            title = true;
        } else {
            title = false;
        }
    }

    public void setAuthor(String to) {
        if (to.equals("on")) {
            author = true;
        } else {
            author = false;
        }
    }

    public void setContent(String to) {
        if (to.equals("on")) {
            content = true;
        } else {
            content = false;
        }
    }

    public void setDate(String to) {
        if (to.equals("on")) {
            date = true;
        } else {
            date = false;
        }
    }

    public void setCategory(int id) {
        categoryIndex = id;
    }

    public void setViewmode(String to) {
        viewmode = to;
    }
}
