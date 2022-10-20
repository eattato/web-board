package spring.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private int loved;
    private int hated;
    private int viewers;
    private String taglist;

    private AccountDataDTO authorInfo;
    private List<TagDTO> tagdataList;
    private int interest;

    public int getRecommend() {
        return loved - hated;
    }

    public int[] getTaglist() {
        if (taglist != null) {
            String[] tags = taglist.split(" ");;
            int[] result = new int[tags.length];
            for (int ind = 0; ind < tags.length; ind++) {
                try {
                    result[ind] = Integer.parseInt(tags[ind]);
                } catch (Exception e) {

                }
            }

            if (result.length == 0) {
                try {
                    result = new int[1];
                    result[0] = Integer.parseInt(taglist);
                    return result;
                } catch (Exception e) {
                    return new int[0];
                }
            } else {
                return result;
            }
        } else {
            return new int[0];
        }
    }

    public void setPostdate(final String value) {
        Date current = new Date(Long.parseLong(value));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        postdate = dateFormat.format(current).toString();
    }

//    public void setTaglist(String target) {
//        if (target != null) {
//            String[] split = target.split(" ");
//            if (split.length >= 1) {
//
//            } else {
//                split = new String[1];
//                split[0] = target;
//            }
//
//            boolean available = true;
//            for (String tag : split) {
//                try {
//                    Integer.parseInt(tag);
//                } catch (Exception e) {
//                    available = false;
//                    break;
//                }
//            }
//            if (available == true) {
//                taglist = target;
//            }
//        }
//    }
}
