package spring.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class PostVO {
    // Properties
    private String title;
    private String content;
    private int category = -1;
    private String tags = "";
    private String author;

    private boolean remove = false;
    private int id = -1;

    // Getter
    public boolean isValid() {
        if (title != null && content != null && category != -1) {
            return true;
        } else {
            return false;
        }
    }

    public int[] getTagsAsInt() {
        if (tags.equals("")) {
            return new int[0];
        } else {
            String[] split = tags.split(" ");
            int[] result = new int[split.length];
            for (int ind = 0; ind < split.length; ind++) {
                try {
                    result[ind] = Integer.parseInt(split[ind]);
                } catch (Exception e) {
                    break;
                }
            }
            return result;
        }
    }

    // Setter
    public void setAuthor(String target) {
        if (author == null) {
            author = target;
        }
    }

    public void setTags(String target) {
        if (target != null) {
            if (target.equals("")) {
                tags = "";
            } else {
                String[] split = target.split(" ");
                if (split.length >= 1) {

                } else {
                    split = new String[1];
                    split[0] = target;
                }

                boolean available = true;
                for (String tag : split) {
                    try {
                        Integer.parseInt(tag);
                    } catch (Exception e) {
                        available = false;
                        break;
                    }
                }
                if (available == true) {
                    tags = target;
                }
            }
        }
    }
}
