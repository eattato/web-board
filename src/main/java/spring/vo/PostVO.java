package spring.vo;

import lombok.Setter;

@Setter
public class PostVO {
    // Properties
    private String title;
    private String content;
    private int category = -1;

    private String tags;

    private String author;

    // Setter
    public void setAuthor(String target) {
        if (author == null) {
            author = target;
        }
    }

    // Getter
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getCategory() {
        return category;
    }

    public boolean isValid() {
        if (title != null && content != null && category != -1) {
            return true;
        } else {
            return false;
        }
    }

    public String getTags() {
        return tags;
    }

    public String getAuthor() {
        return author;
    }
}
