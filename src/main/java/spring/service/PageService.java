package spring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import spring.dao.PageDao;
import spring.dto.*;
import spring.vo.CommentVO;
import spring.vo.PageVO;
import spring.vo.PostVO;
import spring.vo.ProfileVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    PageDao pageDao;

    @Autowired
    AccountService accountService;

    ObjectMapper mapper = new ObjectMapper();

    // Category
    public List<CategoryDTO> getCategoryList(PageVO data) {
        return pageDao.getCategoryList(data);
    }

    public int getCategoryCount() {
        return pageDao.getCategoryCount();
    }

    public CategoryDTO getCategoryData(int id) {
        return pageDao.getCategoryData(id);
    }

    // Post
    public List<PostDTO> getPostList(PageVO data, int actType) {
        List<PostDTO> result = pageDao.getPostList(data, actType);
        for (PostDTO post : result) {
            post.setTagdataList(pageDao.getTagData(post.getTaglist()));


            if (post.getContent().contains("{image}")) {
//                map.put("mainImage", );
            } else {
//                map.put("mainImage", null);
            }
        }
        return result;
    }

    public int getPostCount() {
        return pageDao.getPostCount();
    }

    public boolean getPost(HttpServletRequest request, int id, Model model) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        PostDTO postData = pageDao.getPostData(id);
        List<CommentDTO> commentData = getCommentData(id);
        if (postData != null) {
            model.addAttribute("post", postData);
            model.addAttribute("category", getCategoryData(postData.getCategory()));
            model.addAttribute("author", accountService.getProfile(postData.getAuthor()));
            model.addAttribute("comments", commentData);
            postData.setTagdataList(pageDao.getTagData(postData.getTaglist()));
            return true;
        } else {
            return false;
        }
    }

    public String post(HttpServletRequest request, PostVO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            if (data.isValid() == true) {
                if (data.getTitle().length() > 0 && data.getTitle().length() <= 100) {
                    if (pageDao.hasCategory(data.getCategory()) == true) {
                        boolean tagNormal = true;
                        if (data.getTags().length() >= 1) {
                            List<TagDTO> tagDataList = getAllTags();
                            List<Integer> tags = new ArrayList<>();
                            for (int ind = 0; ind < tagDataList.size(); ind++) {
                                tags.add(tagDataList.get(ind).getId());
                            }
                            for (int tag : data.getTagsAsInt()) {
                                if (tags.contains(tag) == false) {
                                    tagNormal = false;
                                }
                            }
                        }

                        if (tagNormal == true) {
                            data.setAuthor(sessionData);
                            int result = pageDao.post(data);
                            if (result == 1) {
                                return Integer.toString(pageDao.getIdCurrent("posts"));
                            } else {
                                return "data save failed";
                            }
                        } else {
                            return "tag not found";
                        }
                    } else {
                        return "category does not exist";
                    }
                } else {
                    return "title not valid";
                }
            } else {
                return "post not valid";
            }
        } else {
            return "no session";
        }
    }

    public boolean addView(int id) { // 조회수 추가
        PostDTO postData = pageDao.getPostData(id);
        if (postData != null) {
            pageDao.addToPost(id, "viewers", 1);
            return true;
        } else {
            return false;
        }
    }

    public String removePost(HttpServletRequest request, int id) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            AccountDataDTO userData = accountService.getProfile(sessionData);
            PostDTO postData = pageDao.getPostData(id);
            if (postData != null) {
                if (postData.getAuthor().equals(sessionData) || userData.isIsadmin() == true) {
                    pageDao.removePost(id);
                    pageDao.removeCommentsOfPost(id);
                    return "ok";
                } else {
                    return "no access";
                }
            } else {
                return "couldn't find post";
            }
        } else {
            return "no session";
        }
    }

    // Comment
    public String comment(HttpServletRequest request, CommentVO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            data.setAuthor(sessionData);
            if (data.isValid() == true) {
                if (pageDao.hasPost(data.getPost()) == true) {
                    if (data.getReplyTarget() != -1) {
                        if (pageDao.hasComment(data.getReplyTarget()) == false) {
                            return "target comment not exist";
                        }
                    }
                    int result = pageDao.uploadComment(data);
                    if (result == 1) {
                        return "ok";
                    } else {
                        return "failed";
                    }
                } else {
                    return "post does not exist";
                }
            } else {
                return "data not valid";
            }
        } else {
            return "no session";
        }
    }

    public List<CommentDTO> getCommentData(int id) {
        List<CommentDTO> result = pageDao.getCommentsFromPost(id);
        for (CommentDTO comment : result) {
            comment.setAuthorInfo(accountService.getProfile(comment.getAuthor()));
        }
        return result;
    }

    public String removeComment(HttpServletRequest request, int id) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            AccountDataDTO userData = accountService.getProfile(sessionData);
            CommentDTO commentData = pageDao.getCommentData(id);
            if (commentData != null) {
                if (commentData.getAuthor().equals(sessionData) || userData.isIsadmin() == true) {
                    pageDao.removeComment(id);
                    pageDao.removeReplyComment(id);
                    return "ok";
                } else {
                    return "no access";
                }
            } else {
                return "couldn't find comment";
            }
        } else {
            return "no session";
        }
    }

    // Tag
    public List<TagDTO> getAllTags() {
        return pageDao.getAllTags();
    }
}
