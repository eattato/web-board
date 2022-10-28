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
import spring.vo.*;

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
            if (sessionData != null) {
                List<String> lovers = postData.getLoverList();
                List<String> haters = postData.getHaterList();
                model.addAttribute("loved", lovers.contains(sessionData));
                model.addAttribute("hated", haters.contains(sessionData));
            } else {
                model.addAttribute("loved", false);
                model.addAttribute("hated", false);
            }
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
                            if (data.getId() == -1) {
                                //log.info("new post request!");
                                int result = pageDao.post(data);
                                if (result == 1) {
                                    //log.info("successfully posted! id: " + pageDao.getIdCurrent("posts"));
                                    return Integer.toString(pageDao.getIdCurrent("posts"));
                                } else {
                                    //log.info("failed posting!!!");
                                    return "data save failed";
                                }
                            } else {
                                //log.info("post edit request!");
                                PostDTO postData = pageDao.getPostData(data.getId());
                                if (postData != null) {
                                    if (postData.getAuthor().equals(sessionData)) {
                                        int result = pageDao.updatePost(data);
                                        if (result == 1) {
                                            return Integer.toString(data.getId());
                                        } else {
                                            return "data save failed";
                                        }
                                    } else {
                                        return "no access";
                                    }
                                } else {
                                    return "post not found";
                                }
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

    public String editor(HttpServletRequest request, Model model, String id) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) { // 로그인 세션이 존재하면
            int editing = -1;
            try {
                int intid = Integer.parseInt(id);
                boolean result = getPost(request, intid, model);
                if (result == true) {
                    editing = intid;
                }
            } catch (Exception e) {}

            // 이메일로 계정 조회해서 정보를 모델로 전송
            AccountDataDTO profile = accountService.sendProfileBySession(request, model);
            if (profile != null) {
                PageVO vo = new PageVO();
                vo.setStartIndex(0);
                vo.setEndIndex(-1);
                model.addAttribute("categoryList", getCategoryList(vo));
                model.addAttribute("tags", getAllTags());

                if (editing != -1) {
                    PostDTO postData = pageDao.getPostData(editing);
                    if (postData.getAuthor().equals(sessionData)) {
                        postData.setTagdataList(pageDao.getTagData(postData.getTaglist()));
                        model.addAttribute("pastData", postData);
                    } else {
                        return "redirect:";
                    }
                }
                return "editor";
            } else {
                return "redirect:";
            }
        } else {
            return "redirect:/login";
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

    public String pressRecommend(HttpServletRequest request, RecommendVO vo) { // 좋아요 누르기
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        if (sessionData != null) {
            AccountDataDTO userProfile = accountService.getProfile(sessionData);
            if (userProfile != null) {
                if (userProfile.isVerify() == true) {
                    PostDTO postData = pageDao.getPostData(vo.getId());
                    if (postData != null) {
                        int result = pageDao.pressRecommend(sessionData, vo);
                        if (result == 1) {
                            return "ok";
                        } else {
                            return "failed";
                        }
                    } else {
                        return "post not found";
                    }
                } else {
                    return "user not verified";
                }
            } else {
                return "user not found";
            }
        } else {
            return "no session";
        }
    }

    public String removePost(HttpServletRequest request, int id) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        if (sessionData != null) {
            AccountDataDTO userData = accountService.getProfile(sessionData);
            PostDTO postData = pageDao.getPostData(id);
            if (postData != null) {
                CategoryDTO categoryData = pageDao.getCategoryData(postData.getId());
                if (categoryData != null) {
                    if (postData.getAuthor().equals(sessionData) || userData.isIsadmin() == true || categoryData.getAdminList().contains(sessionData)) {
                        pageDao.removePost(id);
                        pageDao.removeCommentsOfPost(id);
                        return "ok";
                    } else {
                        return "no access";
                    }
                } else {
                    return "category is removed";
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
            AccountDataDTO userProfile = accountService.getProfile(sessionData);
            if (userProfile != null) {
                if (userProfile.isVerify() == true) {
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
                    return "user not verified";
                }
            } else {
                return "user not found";
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
                PostDTO postData = pageDao.getPostData(id);
                if (postData != null) {
                    CategoryDTO categoryData = pageDao.getCategoryData(postData.getId());
                    if (categoryData != null) {
                        if (commentData.getAuthor().equals(sessionData) || userData.isIsadmin() == true || categoryData.getAdminList().contains(sessionData)) {
                            pageDao.removeComment(id);
                            pageDao.removeReplyComment(id);
                            return "ok";
                        } else {
                            return "no access";
                        }
                    } else {
                        return "couldn't find category";
                    }
                } else {
                    return "couldn't find post";
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
