package spring.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import spring.dto.*;
import spring.vo.CommentVO;
import spring.vo.PageVO;
import spring.vo.PostVO;
import spring.vo.RecommendVO;

import java.time.LocalDate;
import java.util.*;

@Repository
@Slf4j
public class PageDao {
    @Autowired
    JdbcTemplate jt;

    ObjectMapper mapper = new ObjectMapper();

    private List<Map<String, Object>> getRows(String queryString) { // List<Map<String, Object>> 형태로 모든 선택된 열을 리턴
        return jt.queryForList(queryString);
    }

    private String getFirstRow(String queryString) {
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            for (String key : result.get(0).keySet()) {
                return result.get(0).get(key).toString();
            }
            return null;
        } else {
            return null;
        }
    }

    private String[] queryDict(HashMap<String, Object> target) {
        String[] result = new String[2];
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        for (String key : target.keySet()) {
            keys.add(key);
            vals.add(String.format("'%s'", target.get(key).toString()));
        }

        result[0] = String.join(", ", keys);
        result[1] = String.join(", ", vals);
        return result;
    }

    // Category
    public List<CategoryDTO> getCategoryList(PageVO data) {
        String queryString = "select c.*, count(p.id) as posts, ifnull(sum(r.recommend), 0) as loved " +
                "from categories c " +
                "left join posts p " +
                "on p.category = c.id " +
                "left join recommends r " +
                "on r.post = p.id ";

        if (data.getSearch() != null) {
            queryString += "WHERE " + String.format("c.category LIKE '%%%s%%' ", data.getSearch());
        }

        queryString += "GROUP BY c.id ORDER BY c.id ";
        if (data.getDirection().equals("down")) {
            queryString += "DESC ";
        } else {
            queryString += "ASC ";
        }
        if (data.getEnd() != -1) {
            queryString += String.format("LIMIT %s OFFSET %s;", data.getEnd() - data.getStart(), data.getStart());
        } else { // 끝이 -1이면 전부 다 로드
            queryString += String.format("LIMIT 9999999 OFFSET %s;", data.getStart());
        }

        List<Map<String, Object>> queryResult = getRows(queryString);
        List<CategoryDTO> result = new ArrayList<>();
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, CategoryDTO.class));
        }
        return result;
    }

    public int getCategoryCount() {
        String queryString = String.format("SELECT COUNT(id) AS csize FROM categories;");
        List<Map<String, Object>> queryResult = getRows(queryString);
        if (queryResult.size() >= 1) {
            if (queryResult.get(0).get("csize") != null) {
                return Integer.parseInt(queryResult.get(0).get("csize").toString());
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public CategoryDTO getCategoryData(int id) {
        String queryString = String.format(
                "select c.*, count(p.id) as posts, sum(ifnull(r.recommend, 0)) as loved " +
                "from categories c " +
                "left join posts p " +
                "on p.category = c.id " +
                "left join recommends r " +
                "on r.post = p.id " +
                "WHERE c.id = %s " +
                "group by c.id;",
                id
        );
        List<Map<String, Object>> queryResult = getRows(queryString);
        if (queryResult.size() >= 1) {
            return mapper.convertValue(queryResult.get(0), CategoryDTO.class);
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

    public int updateCategory(CategorySetDTO data) {
        CategoryDTO categoryData = getCategoryData(data.getId());
        if (categoryData != null) {
            if (data.getAct().equals("addAdmin")) {
                List<String> adminList = categoryData.getAdminList();
                adminList.add(data.getTarget());
                String admins = String.join(" ", adminList);
                return jt.update(String.format("UPDATE categories SET admins = '%s' WHERE id = %s;", admins, data.getId()));
            } else if (data.getAct().equals("removeAdmin")) {
                List<String> adminList = categoryData.getAdminList();
                adminList.remove(data.getTarget());
                String admins = String.join(" ", adminList);
                return jt.update(String.format("UPDATE categories SET admins = '%s' WHERE id = %s;", admins, data.getId()));
            } else if (data.getAct().equals("changeName")) {
                return jt.update(String.format("UPDATE categories SET category = '%s' WHERE id = %s;", data.getTarget().replaceAll("'", "''"), data.getId()));
            } else if (data.getAct().equals("changeAbout")) {
                return jt.update(String.format("UPDATE categories SET about = '%s' WHERE id = %s;", data.getTarget().replaceAll("'", "''"), data.getId()));
            }
            else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int addCategory(CategoryCreateDTO data) {
        int nextId = getIdCurrent("categories") + 1;
        return jt.update(String.format("INSERT INTO categories VALUES(%s, '%s', '%s', null, false, false, null);", nextId, data.getCategory().replaceAll("'", "''"), data.getAbout().replaceAll("'", "''")));
    }

    public int removeCategory(int id) {
        return jt.update(String.format("DELETE FROM categories WHERE id = %s;", id));
    }

    public int setCategoryImage(int id, String directory) {
        return jt.update(String.format("UPDATE categories SET img = '%s' WHERE id = %s", directory, id));
    }

    // Post
    private String queryConnect(boolean firstQuestion, boolean whereAdded) {
        String result = "";
        if (firstQuestion == true) {
            if (whereAdded == true) {
                result = "AND (";
            } else {
                result = "WHERE (";
            }
        } else {
            result = "OR ";
        }
        return result;
    }

    public List<PostDTO> getPostList(PageVO data, int actType) {
        String queryString = "SELECT p.*, "
                + "ifnull(sum(r.recommend), 0) as recommend,"
                + "ifnull(sum(if(r.recommend=1, 1, 0)), 0) as loved, "
                + "ifnull(sum(if(r.recommend=-1, -1, 0)), 0) * -1 as hated, "
                + "ifnull(sum(r.recommend), 0) * 2 + ifnull(sum(p.viewers), 0) as interest "
                + "from posts p "
                + "left join recommends r "
                + "on p.id = r.post ";
        boolean whereAdded = false;
        if (actType == 0) {
            whereAdded = true;
            queryString += String.format("WHERE category = %s ", data.getCategoryIndex());
        } else if (actType == 4) { // 태그 검색, 카테고리 인덱스로 tag id 들어옴
            whereAdded = true;
            queryString += String.format(
                    "JOIN tagref tr " +
                    "ON p.id = tr.post " +
                    "JOIN tags t " +
                    "ON tr.tag = t.id " +
                    "WHERE t.id = %s ",
                    data.getCategoryIndex()
            );
        }

        if (data.getSearch() != null) {
            boolean firstQuestion = true;
            if (data.isTitle()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("postname LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }
            if (data.isAuthor()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("author LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }
            if (data.isContent()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("content LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }
            if (data.isDate()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("postdate LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }

            if (firstQuestion == false) {
                queryString += ") ";
            }
        }
        queryString += "GROUP BY p.id ";
        if (actType == 0) {
            if (data.getSort().equals("loved")) {
                //queryString += String.format("ORDER BY loved %s, viewers %s;", data.getDirection(), data.getDirection());
                queryString += String.format("ORDER BY recommend %s ", data.getDirection());
            } else {
                queryString += String.format("ORDER BY postdate %s ", data.getDirection());
            }
        } else if (actType == 1) {
            queryString += "ORDER BY interest DESC ";
        } else if (actType == 2) {
            queryString += "ORDER BY postdate DESC ";
        }

        if (data.getEnd() != -1) {
            //log.info(String.format("loaded %s ~ %s (total %s) posts", data.getStart(), data.getEnd(), data.getEnd() - data.getStart()));
            queryString += String.format("LIMIT %s OFFSET %s;", data.getEnd() - data.getStart(), data.getStart());
        } else { // 끝이 -1이면 전부 다 로드
            //log.info("loaded all posts");
            queryString += String.format("LIMIT 9999999 OFFSET %s;", data.getStart());
        }

        List<Map<String, Object>> queryResult = getRows(queryString);
        List<PostDTO> result = new ArrayList<>();
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, PostDTO.class));
        }
        return result;
    }

    public int getPostCountQuery(PageVO data, int actType) {
        String queryString = "SELECT p.id FROM posts p ";

        boolean whereAdded = false;
        if (actType == 0) {
            queryString += String.format("WHERE category = %s ", data.getCategoryIndex());
            whereAdded = true;
        } else if (actType == 4) { // 태그 검색, 카테고리 인덱스로 tag id 들어옴
            whereAdded = true;
            queryString += String.format(
                    "JOIN tagref r " +
                    "ON p.id = r.post " +
                    "JOIN tags t " +
                    "ON r.tag = t.id " +
                    "WHERE t.id = %s ",
                    data.getCategoryIndex()
            );
        }

        if (data.getSearch() != null) {
            boolean firstQuestion = false;
            if (data.isTitle()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("postname LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }
            if (data.isAuthor()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("author LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }
            if (data.isContent()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("content LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }
            if (data.isDate()) {
                queryString += queryConnect(firstQuestion, whereAdded) + String.format("postdate LIKE '%%%s%%' ", data.getSearch());
                firstQuestion = false;
            }

            if (firstQuestion == false) {
                queryString += ");";
            }
        } else {
            queryString += ";";
        }

        List<Map<String, Object>> queryResult = getRows(queryString);
        return queryResult.size();
    }

    public PostDTO getPostData(int id) {
        String queryString = String.format("SELECT p.*, "
                + "ifnull(sum(r.recommend), 0) as recommend, "
                + "ifnull(sum(if(r.recommend=1, 1, 0)), 0) as loved, "
                + "ifnull(sum(if(r.recommend=-1, -1, 0)), 0) * -1 as hated, "
                + "ifnull(sum(r.recommend), 0) * 2 + ifnull(sum(p.viewers), 0) as interest "
                + "from posts p "
                + "left join recommends r "
                + "on p.id = r.post "
                + "WHERE p.id = %s "
                + "group by p.id;",
                id
        );
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return mapper.convertValue(result.get(0), PostDTO.class);
        } else {
            return null;
        }
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

    public int post(PostVO vo) {
        int postId = Integer.parseInt(getFirstRow("SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'board' AND TABLE_NAME = 'posts';"));
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("category", vo.getCategory());
        queryMap.put("postname", vo.getTitle());
        queryMap.put("author", vo.getAuthor());
        queryMap.put("postdate", LocalDate.now());
        queryMap.put("content", vo.getContent());
        queryMap.put("viewers", 0);

        String[] queryMapString = queryDict(queryMap);
        String queryString = String.format("INSERT INTO posts(%s) VALUES(%s);", queryMapString[0], queryMapString[1]);
        int created = jt.update(queryString);
        if (created == 1) {
            for (int tag : vo.getTagsAsInt()) {
                queryString = String.format("INSERT INTO tagref(post, tag) VALUES(%s, %s);", postId, tag);
                jt.update(queryString);
            }
        }
        return 1;
    }

    public int updatePost(PostVO vo) {
        String queryString = "UPDATE posts SET ";
        queryString += String.format("category = %s, ", vo.getCategory());
        queryString += String.format("postname = '%s', ", vo.getTitle().replaceAll("'", "''"));
        queryString += String.format("content = '%s' ", vo.getContent().replaceAll("'", "''"));
        queryString += String.format("WHERE id = %s;", vo.getId());

        int result = jt.update(queryString);
        if (result == 1) {
            queryString = String.format("DELETE FROM board.tagref WHERE post = %s;", vo.getId()); // 현재 태그 테이블 삭제
            int removed = jt.update(queryString);
            for (int tag : vo.getTagsAsInt()) { // 수정된 태그 다시 추가
                queryString = String.format("INSERT INTO tagref(post, tag) VALUES(%s, %s);", vo.getId(), tag);
                int done = jt.update(queryString);
            }
        }
        return result;
    }

    public int addToPost(int id, String target, int add) {
        String queryString = String.format("UPDATE posts SET %s = %s + %s WHERE id = %s", target, target, add, id);
        return jt.update(queryString);
    }

    public int pressRecommend(String email, RecommendVO vo) {
        PostDTO postData = getPostData(vo.getId());
        if (postData != null) {
            postData.setLovers(getRecommendersFromPost(postData, 1));
            postData.setHaters(getRecommendersFromPost(postData, -1));

            int recommendCount = 1;
            boolean alreadyPressed = false;
            if (vo.isLove() == false) {
                recommendCount = -1;
                if (postData.getHaters().contains(email)) {
                    alreadyPressed = true;
                }
            } else {
                if (postData.getLovers().contains(email)) {
                    alreadyPressed = true;
                }
            }

            int result = 1;
            String queryString = String.format( // 기존에 있던 평가를 제거하고 새 평가 추가
                    "DELETE FROM recommends WHERE post = %s and target = '%s';",
                    postData.getId(), email
            );
            result = jt.update(queryString);

            if (alreadyPressed == false) {
                queryString = String.format(
                        "INSERT INTO recommends VALUES(%s, '%s', %s);",
                        postData.getId(), email, recommendCount
                );
                result = jt.update(queryString);
            }
            return result;
        } else {
            return 0;
        }
    }

    public int removePost(int id) {
        String queryString = String.format("DELETE FROM posts WHERE id = %s", id);
        return jt.update(queryString);
    }

    // Comment
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

    public int uploadComment(CommentVO vo) {
        String queryString = "";
        if (vo.getReplyTarget() == -1) {
            queryString = String.format(
                    "insert into board.comments(reply_id, reply_level, reply_order, parent, author, content, post, postdate) values(" +
                    "(select ifnull(max(reply_id) + 1, 1) from board.comments as T1)," +
                    " 0, 0, null, '%s', '%s', %s, '%s');", vo.getAuthor(), vo.getContent().replaceAll("'", "''"), vo.getPost(), LocalDate.now());
            return jt.update(queryString);
        } else {
            queryString = String.format(
                    "update board.comments as comments, " +
                    "(select reply_id, reply_order from board.comments where id = %s) as origin " +
                    "set comments.reply_order = comments.reply_order + 1 " +
                    "where comments.reply_id = origin.reply_id and comments.reply_order > origin.reply_order;", vo.getReplyTarget());
            jt.update(queryString);
            queryString = String.format(
                    "insert into board.comments(reply_id, reply_level, reply_order, parent, author, content, post, postdate) " +
                    "select reply_id, reply_level + 1, reply_order + 1, " +
                            "%s, '%s', '%s', %s, '%s' " +
                            "from board.comments as T1 " +
                            "where id = %s;", vo.getReplyTarget(), vo.getAuthor(), vo.getContent().replaceAll("'", "''"), vo.getPost(), LocalDate.now(), vo.getReplyTarget());
            return jt.update(queryString);
        }
    }

    public List<CommentDTO> getCommentsFromPost(int id) {
        String queryString = String.format("SELECT * FROM comments WHERE post = %s ORDER BY reply_id DESC, reply_order ASC;", id);
        List<Map<String, Object>> queryResult = getRows(queryString);
        List<CommentDTO> result = new ArrayList<>();
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, CommentDTO.class));
        }
        return result;
    }

    public CommentDTO getCommentData(int id) {
        String queryString = String.format("SELECT * FROM comments WHERE id = %s", id);
        List<Map<String, Object>> queryResult = getRows(queryString);
        if (queryResult.size() >= 1) {
            return mapper.convertValue(queryResult.get(0), CommentDTO.class);
        } else {
            return null;
        }
    }

    public int removeComment(int id) { // ID로 댓글 삭제
        String queryString = String.format("DELETE FROM comments WHERE id = %s", id);
        return jt.update(queryString);
    }

    // Tags
    public List<TagDTO> getTagDatas(int[] tagIds) {
        //log.info(tagIds.toString());
        List<TagDTO> result = new ArrayList<>();
        for (int id : tagIds) {
            List<Map<String, Object>> queryResult = getRows(String.format("SELECT * FROM tags WHERE id = %s", id));
            if (queryResult.size() >= 1) {
                result.add(mapper.convertValue(queryResult.get(0), TagDTO.class));
            }
        }
        return result;
    }

    public List<TagDTO> getTagDatasFromPost(PostDTO data) {
        String queryString = String.format(
                "SELECT d.* "
                + "FROM tagref r "
                + "LEFT JOIN tags d "
                + "ON r.tag = d.id "
                + "WHERE r.post = %s;",
                data.getId()
        );

        List<TagDTO> result = new ArrayList<>();
        List<Map<String, Object>> queryResult = getRows(queryString);
        for (Map<String, Object> row : queryResult) {
            result.add(mapper.convertValue(row, TagDTO.class));
        }
        return result;
    }

    public TagDTO getTagData(int id) {
        List<Map<String, Object>> queryResult = getRows(String.format("SELECT * FROM tags WHERE id = %s", id));
        if (queryResult.size() >= 1) {
            return mapper.convertValue(queryResult.get(0), TagDTO.class);
        } else {
            return null;
        }
    }

    public List<TagDTO> getAllTags() {
        List<TagDTO> result = new ArrayList<>();
        List<Map<String, Object>> queryResult = getRows("SELECT * FROM tags;");
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, TagDTO.class));
        }
        return result;
    }

    public int updateTag(CategorySetDTO data) {
        TagDTO tagData = getTagData(data.getId());
        if (tagData != null) {
            if (data.getAct().equals("changeName")) {
                return jt.update(String.format("UPDATE tags SET tagname = '%s' WHERE id = %s;", data.getTarget().replaceAll("'", "''"), data.getId()));
            } else if (data.getAct().equals("changeAbout")) {
                return jt.update(String.format("UPDATE tags SET tagdesc = '%s' WHERE id = %s;", data.getTarget(), data.getId()));
            } else if (data.getAct().equals("changeColor")) {
                return jt.update(String.format("UPDATE tags SET tagcolor = '%s' WHERE id = %s;", data.getTarget(), data.getId()));
            } else if (data.getAct().equals("changeAdmin")) {
                return jt.update(String.format("UPDATE tags SET adminonly = %s WHERE id = %s;", data.isBoolTarget(), data.getId()));
            } else if (data.getAct().equals("removeTag")) {
                return jt.update(String.format("DELETE FROM tags WHERE id = %s;", data.getId()));
            }
            else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int addTag(TagCreateDTO data) {
        return jt.update(String.format("INSERT INTO tags(tagname, tagdesc, adminonly, tagcolor) VALUES('%s', '%s', %s, '%s');", data.getTag().replaceAll("'", "''"), data.getAbout().replaceAll("'", "''"), data.isAdmin(), data.getColor()));
    }

    // Recommends
    public List<String> getRecommendersFromPost(PostDTO data, int finding) {
        String queryString = String.format("SELECT target FROM recommends WHERE post = %s and recommend = %s;", data.getId(), finding);
        List<Map<String, Object>> queryResult = getRows(queryString);
        List<String> result = new ArrayList<>();
        for (Map<String, Object> row : queryResult) {
            result.add(row.get("target").toString());
        }
        return result;
    }

    // Misc
    public int getIdCurrent(String search) {
        int idCurrent = 0;
        List<Map<String, Object>> result = getRows(String.format("SELECT MAX(id) AS maxid FROM %s", search));
        if (result.size() >= 1 && result.get(0).get("maxid") != null) {
            idCurrent = (int)result.get(0).get("maxid");
        }
        return idCurrent;
    }
}