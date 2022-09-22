package spring.vo;

import java.util.HashMap;
import java.util.Map;

public class PageVO {
    private int categoryIndex;

    private int startIndex;
    private int endIndex;
    private String searchTitle = null;
    private String searchDesc = null;
    private String searchAuthor = null;

    // getter
    public int getStart() {
        return startIndex;
    }

    public int getEnd() {
        return endIndex;
    }

    public boolean hasSearch() {
        if (searchTitle != null || searchDesc != null || searchAuthor != null) {
            return true;
        } else {
            return false;
        }
    }

    public Map<String, String> getSearch() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("title", searchTitle);
        result.put("desc", searchDesc);
        result.put("author", searchAuthor);
        return result;
    }

    public int getCategory() {
        return categoryIndex;
    }

    // setter
    public void setData(int start, int end) {
        startIndex = start;
        endIndex = end;
    }

    public void setSearch(String title, String desc, String author) {
        searchTitle = title;
        searchDesc = desc;
        searchAuthor = author;
    }

    public void setCategory(int id) {
        categoryIndex = id;
    }
}
