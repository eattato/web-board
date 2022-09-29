package spring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import spring.dao.PageDao;
import spring.dto.AccountDataDTO;
import spring.dto.CategoryDTO;
import spring.dto.PostDTO;
import spring.vo.CommentVO;
import spring.vo.PageVO;
import spring.vo.PostVO;
import spring.vo.ProfileVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    public List<CategoryDTO> getCategoryList(PageVO data) {
        return pageDao.getCategoryList(data);
    }

    public List<PostDTO> getPostList(PageVO data) {
        List<PostDTO> result = pageDao.getPostList(data);
        for (PostDTO post : result) {
            if (post.getContent().contains("{image}")) {
//                map.put("mainImage", );
            } else {
//                map.put("mainImage", null);
            }
        }
        return result;
    }

    public CategoryDTO getCategoryData(int id) {
        return pageDao.getCategoryData(id);
    }

    public boolean getPost(HttpServletRequest request, int id, Model model) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        PostDTO postData = pageDao.getPostData(id);
        if (postData != null) {
            model.addAttribute("post", postData);
            model.addAttribute("category", getCategoryData(postData.getCategory()));
            model.addAttribute("tags", postData.getTaglist());
            model.addAttribute("author", accountService.getProfile(postData.getAuthor()));
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
                        data.setAuthor(sessionData);
                        int result = pageDao.post(data);
                        if (result == 1) {
                            return Integer.toString(pageDao.getIdCurrent("posts"));
                        } else {
                            return "data save failed";
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

//    public String removeComment(HttpServletRequest request, int id) {
//        HttpSession session = request.getSession();
//        String sessionData = accountService.getSession(session);
//        if (sessionData != null) {
//            Map<String, Object> commentData = pageDao.getCommentData();
//            Map<String, Object> accountData = accountService.accountDao.getUserData(sessionData);
//            if ( || (Boolean) accountData.get("isadmin") == true) {
//
//            } else {
//                return "no access";
//            }
//        } else {
//            return "no session";
//        }
//
//        if (pageDao.hasComment(id) == true) {
//
//        } else {
//            return "comment does not exist";
//        }
//    }
}
