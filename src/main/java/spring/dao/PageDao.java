package spring.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import spring.vo.PageVO;
import spring.vo.PostVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class PageDao {
    @Autowired
    JdbcTemplate jt;

    private List<Map<String, Object>> getRows(String queryString) { // List<Map<String, Object>> 형태로 모든 선택된 열을 리턴
        return jt.queryForList(queryString);
    }

    public List<Map<String, Object>> getCategoryList(PageVO data) {
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

        return getRows(queryString);
    }

    public List<Map<String, Object>> getPostList(PageVO data) {
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

        return getRows(queryString);
    }

    public List<Map<String, Object>> getCategoryData(int id) {
        String queryString = String.format("SELECT category, IFNULL(about, '글을 둘러보세요.') AS about, img, anonymous, adminonly FROM categories WHERE id = %s", id);
        return getRows(queryString);
    }

    public Map<String, Object> getPostData(int id) {
        String queryString = String.format("SELECT * FROM posts WHERE id = %s", id);
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public int getCategoryCount() {
        String queryString = "SELECT id FROM categories;";
        List<Map<String, Object>> result = getRows(queryString);
        return result.size();
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

    public int getPostIdCurrent() {
        int idCurrent = 0;
        List<Map<String, Object>> result = getRows("SELECT MAX(id) AS maxid FROM posts");
        if (result.size() >= 1 && result.get(0).get("maxid") != null) {
            idCurrent = (int)result.get(0).get("maxid");
        }
        return idCurrent;
    }

    public int post(PostVO vo) {String queryString = "INSERT INTO posts VALUES(";
        int idCurrent = getPostIdCurrent();
        queryString += String.format("%s, %s, '%s', '%s', '%s', '%s', %s, %s, %s, %s);", idCurrent + 1, vo.getCategory(), vo.getTitle(), vo.getAuthor(), LocalDate.now(), vo.getContent(), 0, 0, 0, vo.getTags());
        return jt.update(queryString);
    }
}