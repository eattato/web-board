package spring.vo;

import lombok.Setter;

@Setter
public class CommentVO {
    // Properties
    private String content;
    private int at;
    private int to = -1;
    private String author;


    // Getter
    public boolean isValid() {
        if (content != null && content.length() > 0 && content.length() <= 200) {
            return true;
        } else {
            return false;
        }
    }

    public String getContent() {
        return content;
    }

    public int getPost() {
        return at;
    }

    public int getReplyTarget() {
        return to;
    }

    public String getAuthor() {
        return author;
    }

    // Setter
    public void setAuthor(String target) {
        author = target;
    }
}
