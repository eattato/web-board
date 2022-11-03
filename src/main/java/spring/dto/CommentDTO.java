package spring.dto;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class CommentDTO {
    private int id;
    private int reply_id;
    private int reply_level;
    private int reply_order;
    private String author;
    private String content;
    private int post;
    private String postdate;
    private AccountDataDTO authorInfo;

    public void setPostdate(final String value) {
        Date current = new Date(Long.parseLong(value));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        postdate = dateFormat.format(current).toString();
    }
}
