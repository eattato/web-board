package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.dao.PageDao;
import spring.vo.PageVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    PageDao pageDao;

    public List<Map<String, Object>> getCategoryList(PageVO data) {
        return pageDao.getCategoryList(data);
    }

    public List<Map<String, Object>> getPostList(PageVO data) {
        List<Map<String, Object>> result = pageDao.getPostList(data);
        for (Map<String, Object> map : result) {
            if (map.get("content").toString().contains("{image}")) {
//                map.put("mainImage", );
            } else {
//                map.put("mainImage", null);
            }
        }
        return result;
    }

    public Map<String, Object> getCategoryData(int id) {
        return pageDao.getCategoryData(id).get(0);
    }

    public Map<String, Object> getPage(int id) {
        return pageDao.getPageData(id).get(0);
    }
}
