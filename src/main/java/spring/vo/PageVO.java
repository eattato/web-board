package spring.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter//(makeFinal = true)
@Slf4j
public class PageVO {
    private int categoryIndex;
    private int startIndex;
    private int endIndex;

    private int page = -1;
    private String search;
    private String[] searchParams;
    private String viewmode = "simple";
    private boolean title = false;
    private boolean author = false;
    private boolean content = false;
    private boolean date = false;
    private String sort = "loved";
    private String direction = "down";

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

    public String getDirection() {
        if (direction.equals("up")) {
            return "ASC";
        } else {
            return "DESC";
        }
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

    public void setDirection(String target) {
        if (target.equals("up") || target.equals("down")) {
            direction = target;
        }
    }

    public void setSearch(String target) {
        searchParams = target.split(" ");
        if (searchParams.length == 0) {
            searchParams = new String[] {target};
        }
        search = target;
    }
}
