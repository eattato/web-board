package spring.dto;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class CommentDTO {
    private int id;
    private int post;
    private String author;
    private int reply;
    private String content;
    private String postdate;
    private AccountDataDTO authorInfo;

    public void setPostdate(final String value) {
        Date current = new Date(Long.parseLong(value));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        postdate = dateFormat.format(current).toString();
    }
}
