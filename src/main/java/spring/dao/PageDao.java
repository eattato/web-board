package spring.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import spring.vo.PageVO;

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

    public List<Map<String, Object>> getCategoryPage(PageVO data) {
        String queryString = "SELECT * FROM categories ";
        if (data.hasSearch()) {
            Map<String, String> search = data.getSearch();
            boolean firstQuestion = true;
            if (search.get("title") != null) {
                if (firstQuestion == true) {
                    queryString += "WHERE ";
                } else {
                    queryString += "AND ";
                }
                queryString += String.format("category LIKE '%%%s%%' ", search.get("title"));
            }
        }
        queryString += String.format("ORDER BY id OFFSET %s ROWS FETCH NEXT %s ROWS ONLY", data.getStart(), data.getEnd() - data.getStart());
        log.info(queryString);
        return getRows(queryString);
    }
}
