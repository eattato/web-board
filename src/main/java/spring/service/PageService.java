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
import java.io.File;
import java.util.*;

@Service
@Slf4j
public class PageService {
    @Autowired
    PageDao pageDao;

    @Autowired
    AccountService accountService;

    @Autowired
    FileService fileService;

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

    public String updateCategory(HttpServletRequest request, CategorySetDTO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        if (sessionData != null) {
            if (data.getId() != -1) {
                CategoryDTO categoryData = getCategoryData(data.getId());
                AccountDataDTO userData = accountService.getUserData(sessionData);
                if (categoryData != null) {
                    List<String> admins = pageDao.getCategoryAdmins(data.getId());
                    if (userData != null && (userData.isIsadmin() || admins.contains(sessionData))) {
                        List<String> availableActs = Arrays.asList(new String[] {"addAdmin", "removeAdmin", "setAdmin", "changeName", "changeAbout", "removeCategory"});
                        if (availableActs.contains(data.getAct())) {
                            String error = null;
                            if (data.getAct().equals("addAdmin") || data.getAct().equals("removeAdmin")) {
                                AccountDataDTO target = accountService.getUserData(data.getTarget());
                                if (target == null) {
                                    error = "target is null";
                                }
                            } else if (data.getAct().equals("changeName")) {
                                if (data.getTarget() != null && (data.getTarget().length() > 0 && data.getTarget().length() <= 100) == false) {
                                    error = "wrong length";
                                }
                            } else if (data.getAct().equals("changeAbout")) {
                                if (data.getTarget() != null && (data.getTarget().length() > 0 && data.getTarget().length() <= 300) == false) {
                                    error = "wrong length";
                                }
                            } else if (data.getAct().equals("removeCategory")) {
                                if (data.getId() != -1) {
                                    return removeCategory(request, data);
                                } else {
                                    error = "target not set";
                                }
                            }

                            if (error == null) {
                                int result = pageDao.updateCategory(data);
                                if (result == 1) {
                                    return "ok";
                                } else {
                                    return "failed";
                                }
                            } else {
                                return error;
                            }
                        } else {
                            return "wrong act";
                        }
                    } else {
                        return "no access";
                    }
                } else {
                    return "no such category";
                }
            } else {
                return "no such category";
            }
        } else {
            return "no session";
        }
    }

    public String createCategory(HttpServletRequest request, CategoryCreateDTO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        if (sessionData != null) {
            AccountDataDTO userData = accountService.getUserData(sessionData);
            if (userData != null && userData.isIsadmin()) {
                if (data.getCategory() != null && data.getAbout() != null) {
                    if (data.getCategory().length() >= 1 && data.getCategory().length() <= 100) {
                        if (data.getAbout().length() >= 1 && data.getAbout().length() <= 300) {
                            int result = pageDao.addCategory(data);

                            if (result == 1) {
                                if (data.getImage() != null) {
                                    File image = fileService.uploadImage(data.getImage());
                                    if (image != null && image.exists() == true) {
                                        log.info(String.format("%s uploaded profile image %s", request.getRemoteAddr(), image.getAbsolutePath()));
                                        String directory = fileService.getImageDirectory(image);
                                        int id = pageDao.getIdCurrent("categories");
                                        pageDao.setCategoryImage(id, directory);
                                    }
                                }
                                return "ok";
                            } else {
                                return "failed";
                            }
                        } else {
                            return "wrong about";
                        }
                    } else {
                        return "wrong category";
                    }
                } else {
                    return "no data";
                }
            } else {
                return "no access";
            }
        } else {
            return "no session";
        }
    }

    public String removeCategory(HttpServletRequest request, CategorySetDTO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        if (sessionData != null) {
            if (data.getId() != -1) {
                CategoryDTO categoryData = getCategoryData(data.getId());
                AccountDataDTO userData = accountService.getUserData(sessionData);
                if (categoryData != null) {
                    if (userData != null && (userData.isIsadmin())) {
                        int result = pageDao.removeCategory(data.getId());
                        if (result == 1) {
                            return "ok";
                        } else {
                            return "failed";
                        }
                    } else {
                        return "no access";
                    }
                } else {
                    return "no such category";
                }
            } else {
                return "category not found";
            }
        } else {
            return "no session";
        }
    }

    // Post
    public List<PostDTO> getPostList(PageVO data, int actType) {
        List<PostDTO> result = pageDao.getPostList(data, actType);
        for (PostDTO post : result) {
            post.setTagdataList(pageDao.getTagDatasFromPost(post));
            post.setLovers(pageDao.getRecommendersFromPost(post, 1));
            post.setHaters(pageDao.getRecommendersFromPost(post, -1));

            if (post.getContent().contains("{image}")) {
//                map.put("mainImage", );
            } else {
//                map.put("mainImage", null);
            }
        }
        return result;
    }

    public int getPostCountQuery(PageVO vo, int actType) {
        return pageDao.getPostCountQuery(vo, actType);
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
            postData.setTagdataList(pageDao.getTagDatasFromPost(postData));

            List<String> lovers = pageDao.getRecommendersFromPost(postData, 1);
            List<String> haters = pageDao.getRecommendersFromPost(postData, -1);
            postData.setLovers(lovers);
            postData.setHaters(haters);
            if (sessionData != null) {
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
            AccountDataDTO userData = accountService.getUserData(sessionData);
            if (userData != null) {
                if (data.isValid() == true) {
                    if (data.getTitle().length() > 0 && data.getTitle().length() <= 100) {
                        if (pageDao.hasCategory(data.getCategory()) == true) {
                            boolean tagNormal = true;
                            if (data.getTags().length() >= 1) {
                                List<TagDTO> tagDataList = getAllTags();
                                for (int tagId : data.getTagsAsInt()) {
                                    boolean foundTag = false;
                                    for (TagDTO tagData : tagDataList) {
                                        if (tagData.getId() == tagId) {
                                            foundTag = true;
                                            if (tagData.getAdminonly() == true && userData.isIsadmin() == false) {
                                                tagNormal = false;
                                            }
                                            break;
                                        }
                                    }
                                    if (foundTag == false || tagNormal == false) {
                                        tagNormal = false;
                                        break;
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
                return "user not found";
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
                        postData.setTagdataList(pageDao.getTagDatasFromPost(postData));
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
                CategoryDTO categoryData = pageDao.getCategoryData(postData.getCategory());
                if (categoryData != null) {
                    List<String> admins = pageDao.getCategoryAdmins(categoryData.getId());
                    if (userData != null && (postData.getAuthor().equals(sessionData) || userData.isIsadmin() == true || admins.contains(sessionData))) {
                        int result = pageDao.removePost(id);
                        if (result == 1) {
                            return "ok";
                        } else {
                            return "failed";
                        }
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
                PostDTO postData = pageDao.getPostData(commentData.getPost());
                if (postData != null) {
                    CategoryDTO categoryData = pageDao.getCategoryData(postData.getId());
                    if (categoryData != null) {
                        if (userData != null && (commentData.getAuthor().equals(sessionData) || userData.isIsadmin() == true || categoryData.getAdmins().contains(sessionData))) {
                            pageDao.removeComment(id);
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

    // page access
    public String controlPage(HttpServletRequest request, Model model, String menu, Integer page) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        AccountDataDTO userData = accountService.getUserData(sessionData);
        if (userData != null && userData.isIsadmin() == true) {
            accountService.sendProfileBySession(request, model);
            if (menu.equals("category")) {
                PageVO vo = new PageVO();
                vo.setStartIndex(0);
                vo.setEndIndex(-1);
                List<CategoryDTO> result = getCategoryList(vo);
                for (CategoryDTO category : result) {
                    category.setAdmins(pageDao.getCategoryAdmins(category.getId()));
                }
                model.addAttribute("categories", result);
                return "control/category";
            } else if (menu.equals("tags")) {
                model.addAttribute("tags", getAllTags());
                return "control/tags";
            } else if (menu.equals("members")) {
                int userCountPerPage = 10;
                model.addAttribute("members", accountService.getUsersPage(page, userCountPerPage));
                model.addAttribute("page", page);
                model.addAttribute("pageCount", accountService.getUsersPageCount(userCountPerPage));
                return "control/members";
            }
        }
        return "redirect:/";
    }

    // Tag
    public List<TagDTO> getAllTags() {
        return pageDao.getAllTags();
    }

    public TagDTO getTagData(int id) {
        return pageDao.getTagData(id);
    }

    public String updateTag(HttpServletRequest request, CategorySetDTO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        if (sessionData != null) {
            if (data.getId() != -1) {
                TagDTO tagData = pageDao.getTagData(data.getId());
                AccountDataDTO userData = accountService.getUserData(sessionData);
                if (tagData != null) {
                    if (userData != null && userData.isIsadmin()) {
                        List<String> availableActs = Arrays.asList(new String[] {"changeName", "changeAbout", "changeColor", "removeTag", "changeAdmin"});
                        if (availableActs.contains(data.getAct())) {
                            String error = null;
                            if (data.getAct().equals("changeName")) {
                                if (data.getTarget() != null && (data.getTarget().length() > 0 && data.getTarget().length() <= 30) == false) {
                                    error = "wrong length";
                                }
                            } else if (data.getAct().equals("changeAbout")) {
                                if (data.getTarget() != null && (data.getTarget().length() > 0 && data.getTarget().length() <= 300) == false) {
                                    error = "wrong length";
                                }
                            } else if (data.getAct().equals("changeColor")) {
                                if (data.getTarget() != null && data.getTarget().length() != 6) {
                                    error = "wrong color";
                                }
                            }

                            if (error == null) {
                                int result = pageDao.updateTag(data);
                                if (result == 1) {
                                    return "ok";
                                } else {
                                    return "failed";
                                }
                            } else {
                                return error;
                            }
                        } else {
                            return "wrong act";
                        }
                    } else {
                        return "no access";
                    }
                } else {
                    return "no such category";
                }
            } else {
                return "no such category";
            }
        } else {
            return "no session";
        }
    }

    public String createTag(HttpServletRequest request, TagCreateDTO data) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);

        if (sessionData != null) {
            AccountDataDTO userData = accountService.getUserData(sessionData);
            if (userData != null && userData.isIsadmin()) {
                if (data.getTag() != null && data.getAbout() != null && data.getColor() != null) {
                    if (data.getTag().length() >= 1 && data.getTag().length() <= 30) {
                        if (data.getAbout().length() >= 1 && data.getAbout().length() <= 300) {
                            if (data.getColor().length() == 6) {
                                int result = pageDao.addTag(data);
                                if (result == 1) {
                                    return "ok";
                                } else {
                                    return "failed";
                                }
                            } else {
                                return "wrong color";
                            }
                        } else {
                            return "wrong about";
                        }
                    } else {
                        return "wrong category";
                    }
                } else {
                    return "no data";
                }
            } else {
                return "no access";
            }
        } else {
            return "no session";
        }
    }
}
