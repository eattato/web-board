package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.dao.PageDao;
import spring.vo.PageVO;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    PageDao pageDao;

    public String getCategoryPage(PageVO data) {
        List<Map<String, Object>> result = pageDao.getCategoryPage(data);
        return JSONArray.toJSONString(result);
    }
}
