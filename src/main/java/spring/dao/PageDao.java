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
import java.util.ArrayList;
import java.util.Arrays;
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

    // Category
    public List<CategoryDTO> getCategoryList(PageVO data) {
        String queryString = "SELECT categories.*, COUNT(posts.category) AS posts, SUM(IFNULL(posts.loved, 0)) - SUM(IFNULL(posts.hated, 0)) AS loved FROM categories " +
                "LEFT JOIN posts " +
                "ON (categories.id = posts.category) ";
        if (data.getSearch() != null) {
            queryString += "WHERE " + String.format("categories.category LIKE '%%%s%%' ", data.getSearch());
        }

        queryString += "GROUP BY categories.id ORDER BY id ";
        if (data.getDirection().equals("down")) {
            queryString += "DESC ";
        } else {
            queryString += "ASC ";
        }
        if (data.getEnd() != -1) {
            queryString += String.format("OFFSET %s ROWS FETCH NEXT %s ROWS ONLY;", data.getStart(), data.getEnd() - data.getStart());
        } else { // 끝이 -1이면 전부 다 로드
            queryString += String.format("OFFSET %s ROWS;", data.getStart());
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
        String queryString = "SELECT categories.*, COUNT(posts.category) AS posts, SUM(IFNULL(posts.loved, 0)) - SUM(IFNULL(posts.hated, 0)) AS loved FROM categories " +
                "LEFT JOIN posts " +
                "ON (categories.id = posts.category) ";
        queryString += String.format("WHERE categories.id = %s", id);
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
        removePostsOfCategory(id);
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
        String queryString = "SELECT id, category, postname, author, postdate, content, IFNULL(loved, 0) - IFNULL(hated, 0) AS loved, IFNULL(viewers, 0) AS viewers, IFNULL(viewers, 0) + IFNULL(loved, 0) - IFNULL(hated, 0) AS interest, taglist FROM posts ";
        boolean whereAdded = false;
        if (actType == 0) {
            whereAdded = true;
            queryString += String.format("WHERE category = %s ", data.getCategoryIndex());
        } else if (actType == 4) {
            whereAdded = true;
            queryString += String.format("WHERE (taglist LIKE '%s' OR taglist LIKE '%% %s' OR taglist LIKE '%s %%' OR taglist LIKE '%% %s %%') ", data.getCategoryIndex(), data.getCategoryIndex(), data.getCategoryIndex(), data.getCategoryIndex());
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
        queryString += "GROUP BY id ";
        if (actType == 0) {
            if (data.getSort().equals("loved")) {
                //queryString += String.format("ORDER BY loved %s, viewers %s;", data.getDirection(), data.getDirection());
                queryString += String.format("ORDER BY interest %s ", data.getDirection());
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
            queryString += String.format("OFFSET %s ROWS FETCH NEXT %s ROWS ONLY;", data.getStart(), data.getEnd() - data.getStart());
        } else { // 끝이 -1이면 전부 다 로드
            //log.info("loaded all posts");
            queryString += String.format("OFFSET %s ROWS;", data.getStart());
        }

        List<Map<String, Object>> queryResult = getRows(queryString);
        List<PostDTO> result = new ArrayList<>();
        for (Map<String, Object> map : queryResult) {
            result.add(mapper.convertValue(map, PostDTO.class));
        }
        return result;
    }

    public int getPostCountQuery(PageVO data, int actType) {
        String queryString = "SELECT id FROM posts ";
        boolean whereAdded = false;
        if (actType == 0) {
            queryString += String.format("WHERE category = %s ", data.getCategoryIndex());
            whereAdded = true;
        } else if (actType == 4) {
            whereAdded = true;
            queryString += String.format("WHERE (taglist LIKE '%s' OR taglist LIKE '%% %s' OR taglist LIKE '%s %%' OR taglist LIKE '%% %s %%') ", data.getCategoryIndex(), data.getCategoryIndex(), data.getCategoryIndex(), data.getCategoryIndex());
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
                queryString += ");";
            }
        } else {
            queryString += ";";
        }

        List<Map<String, Object>> queryResult = getRows(queryString);
        return queryResult.size();
    }

    public int getPostCount(int id) {
        if (id != -1) {
            String queryString = String.format("SELECT COUNT(id) AS csize FROM posts WHERE id = %s;", id);
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
        } else {
            String queryString = String.format("SELECT COUNT(id) AS csize FROM posts;");
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
        String queryString = "INSERT INTO posts VALUES(";
        int idCurrent = getIdCurrent("posts");
        queryString += String.format("%s, %s, '%s', '%s', '%s', '%s', 0, 0, 0, '%s', '', '');", idCurrent + 1, vo.getCategory(), vo.getTitle().replaceAll("'", "''"), vo.getAuthor(), LocalDate.now(), vo.getContent().replaceAll("'", "''"), vo.getTags());
        return jt.update(queryString);
    }

    public int updatePost(PostVO vo) {
        String queryString = "UPDATE posts SET ";
        queryString += String.format("category = %s, ", vo.getCategory());
        queryString += String.format("postname = '%s', ", vo.getTitle().replaceAll("'", "''"));
        queryString += String.format("content = '%s', ", vo.getContent().replaceAll("'", "''"));
        queryString += String.format("taglist = '%s' ", vo.getTags());
        queryString += String.format("WHERE id = %s;", vo.getId());
        return jt.update(queryString);
    }

    public int addToPost(int id, String target, int add) {
        String queryString = String.format("UPDATE posts SET %s = %s + %s WHERE id = %s", target, target, add, id);
        return jt.update(queryString);
    }

    public int pressRecommend(String email, RecommendVO vo) {
        PostDTO postData = getPostData(vo.getId());
        if (postData != null) {
            String queryString = null;
            if (vo.isLove() == true) {
                List<String> lovers = postData.getLoverList();
                if (lovers == null) {
                    log.info("lovers is null");
                } else {
                    if (lovers.contains(email)) {
                        lovers.remove(email);
                        queryString = String.format("UPDATE posts SET loved = loved - 1, lovers = '%s' WHERE id = %s", String.join(" ", lovers), vo.getId());
                    } else {
                        lovers.add(email);
                        queryString = String.format("UPDATE posts SET loved = loved + 1, lovers = '%s' WHERE id = %s", String.join(" ", lovers), vo.getId());
                    }
                }
            } else {
                List<String> haters = postData.getHaterList();
                if (haters.contains(email)) {
                    haters.remove(email);
                    queryString = String.format("UPDATE posts SET hated = hated - 1, haters = '%s' WHERE id = %s", String.join(" ", haters), vo.getId());
                } else {
                    haters.add(email);
                    queryString = String.format("UPDATE posts SET hated = hated + 1, haters = '%s' WHERE id = %s", String.join(" ", haters), vo.getId());
                }
            }
            return jt.update(queryString);
        } else {
            return 0;
        }
    }

    public int removePost(int id) {
        String queryString = String.format("DELETE FROM posts WHERE id = %s", id);
        removeCommentsOfPost(id);
        return jt.update(queryString);
    }

    public int removePostsOfCategory(int id) {
        PageVO vo = new PageVO();
        vo.setStartIndex(0);
        vo.setEndIndex(-1);
        vo.setCategory(id);
        List<PostDTO> posts = getPostList(vo, 0);
        for (PostDTO post : posts) {
            removePost(post.getId());
        }
        return 1;
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

    public int removeCommentsOfPost(int id) { // 해당 포스트의 모든 댓글을 삭제하기 때문에 대댓글 삭제 필요 없음
        String queryString = String.format("DELETE FROM comments WHERE post = %s", id);
        return jt.update(queryString);
    }

    public int removeComment(int id) { // ID로 댓글 삭제
        //log.info("remove comment " + id);
        String queryString = String.format("DELETE FROM comments WHERE id = %s", id);
        //removeReplyComment(id);
        return jt.update(queryString);
    }

    public int removeReplyComment(int id) { // 해당 ID 댓글의 대댓글 모두 삭제
        List<Map<String, Object>> queryResult = getRows(String.format("SELECT * FROM comments WHERE reply = %s", id));
        for (Map<String, Object> map : queryResult) {
            CommentDTO commentData = mapper.convertValue(map, CommentDTO.class);
            removeReplyComment(commentData.getId()); // 그 밑의 대댓글을 모두 재귀함수로 제거
        }
        String queryString = String.format("DELETE FROM comments WHERE reply = %s", id);
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