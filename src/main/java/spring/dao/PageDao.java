package spring.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import spring.dto.CategoryDTO;
import spring.dto.PostDTO;
import spring.vo.CommentVO;
import spring.vo.PageVO;
import spring.vo.PostVO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class PageDao {
    @Autowired
    JdbcTemplate jt;

    ObjectMapper mapper = new ObjectMapper();

    private List<Map<String, Object>> getRows(String queryString) { // List<Map<String, Object>> 형태로 모든 선택된 열을 리턴
        return jt.queryForList(queryString);
    }

    public List<CategoryDTO> getCategoryList(PageVO data) {
        String queryString = "SELECT categories.*, COUNT(posts.category) AS posts, IFNULL(SUM(posts.loved), 0) AS loved FROM categories " +
                "LEFT JOIN posts " +
                "ON (categories.id = posts.category) ";
        if (data.hasSearch()) {
            Map<String, String> search = data.getSearch();
            if (search.get("title") != null) {
                queryString += "WHERE " + String.format("category LIKE '%%%s%%' ", search.get("title"));
            }
        }

        if (data.getEnd() != -1) {
            queryString += "GROUP BY categories.id " +
                    String.format("ORDER BY id OFFSET %s ROWS FETCH NEXT %s ROWS ONLY;", data.getStart(), data.getEnd() - data.getStart());
        } else { // 끝이 -1이면 전부 다 로드
            queryString += "GROUP BY categories.id " +
                    String.format("ORDER BY id OFFSET %s ROWS;", data.getStart());
        }

        List<Map<String, Object>> queryResult = getRows(queryString);
        List<CategoryDTO> result = new ArrayList<>();
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, CategoryDTO.class));
        }
        return result;
    }

    public List<PostDTO> getPostList(PageVO data) {
        String queryString = "SELECT id, category, postname, author, postdate, content, IFNULL(loved, 0) - IFNULL(hated, 0) AS loved, IFNULL(viewers, 0) AS viewers, taglist FROM posts ";
        queryString += String.format("WHERE category = %s ", data.getCategory());
        if (data.hasSearch() == true) {
            Map<String, String> search = data.getSearch();
            boolean firstQuestion = true;
            for (String key : search.keySet()) {
                if (search.get(key) != null) {
                    if (firstQuestion == true) {
                        queryString += "AND (";
                    } else {
                        queryString += "OR ";
                    }
                }
                if (key == "title") {
                    queryString += String.format("postname LIKE '%%%s%%' ", search.get(key));
                } else if (key == "desc") {
                    queryString += String.format("content LIKE '%%%s%%' ", search.get(key));
                } else if (key == "author") {
                    queryString += String.format("author LIKE '%%%s%%' ", search.get(key));
                }
            }
            if (firstQuestion == false) {
                queryString += ") ";
            }
        }
        queryString += "GROUP BY id;";

        List<Map<String, Object>> queryResult = getRows(queryString);
        List<PostDTO> result = new ArrayList<>();
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, PostDTO.class));
        }
        return result;
    }

    public CategoryDTO getCategoryData(int id) {
        String queryString = String.format("SELECT * FROM categories WHERE id = %s", id);
        List<Map<String, Object>> queryResult = getRows(queryString);
        if (queryResult.size() >= 1) {
            return mapper.convertValue(queryResult.get(0), CategoryDTO.class);
        } else {
            return null;
        }
    }

    public PostDTO getPostData(int id) {
        String queryString = String.format("SELECT * FROM posts WHERE id = %s", id);
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return mapper.convertValue(result.get(0), PostDTO.class);
        } else {
            return null;
        }
    }

    public boolean hasCategory(int id) {
        boolean result = false;
        String queryString = "SELECT id FROM categories;";
        List<Map<String, Object>> query = getRows(queryString);
        for (Map<String, Object> map : query) {
            if ((int)map.get("id") == id) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean hasPost(int id) {
        boolean result = false;
        String queryString = "SELECT id FROM posts;";
        List<Map<String, Object>> query = getRows(queryString);
        for (Map<String, Object> map : query) {
            if ((int)map.get("id") == id) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean hasComment(int id) {
        boolean result = false;
        String queryString = "SELECT id FROM comments;";
        List<Map<String, Object>> query = getRows(queryString);
        for (Map<String, Object> map : query) {
            if ((int)map.get("id") == id) {
                result = true;
                break;
            }
        }
        return result;
    }

    public int getIdCurrent(String search) {
        int idCurrent = 0;
        List<Map<String, Object>> result = getRows(String.format("SELECT MAX(id) AS maxid FROM %s", search));
        if (result.size() >= 1 && result.get(0).get("maxid") != null) {
            idCurrent = (int)result.get(0).get("maxid");
        }
        return idCurrent;
    }

    public int post(PostVO vo) {String queryString = "INSERT INTO posts VALUES(";
        int idCurrent = getIdCurrent("posts");
        queryString += String.format("%s, %s, '%s', '%s', '%s', '%s', %s, %s, %s, %s);", idCurrent + 1, vo.getCategory(), vo.getTitle(), vo.getAuthor(), LocalDate.now(), vo.getContent(), 0, 0, 0, vo.getTags());
        return jt.update(queryString);
    }

    public int uploadComment(CommentVO vo) {
        String queryString = "INSERT INTO comments VALUES(";
        int idCurrent = getIdCurrent("comments");
        queryString += String.format("%s, %s, '%s', %s, '%s', '%s'", idCurrent + 1, vo.getPost(), vo.getAuthor(), vo.getReplyTarget(), vo.getContent(), LocalDate.now());
        return jt.update(queryString);
    }
}