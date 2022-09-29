package spring.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@Slf4j
public class PostDTO {
    private int id;
    private int category;
    private String postname;
    private String author;
    private String postdate;
    private String content;
    private int loved;
    private int hated;
    private int viewers;
    private String taglist;
    private AccountDataDTO authorInfo;

    public int getRecommend() {
        return loved - hated;
    }

    public String[] getTaglist() {
        if (taglist != null) {
            String[] result = taglist.split(" ");;
            if (result.length == 0) {
                return null;
            } else {
                return result;
            }
        } else {
            return null;
        }
    }

    public void setPostdate(final String value) {
        Date current = new Date(Long.parseLong(value));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        postdate = dateFormat.format(current).toString();
    }
}
