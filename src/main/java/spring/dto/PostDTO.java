package spring.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private int viewers;

    // sql joined property
    private int recommend;
    private int loved;
    private int hated;

    // custom property
    private AccountDataDTO authorInfo;
    private List<TagDTO> tagdataList;
    private List<String> lovers;
    private List<String> haters;

    // setter
    public void setPostdate(final String value) {
        Date current = new Date(Long.parseLong(value));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        postdate = dateFormat.format(current).toString();
    }
}
