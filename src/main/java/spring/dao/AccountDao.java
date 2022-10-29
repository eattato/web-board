package spring.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.dto.AccountDataDTO;
import spring.dto.CategoryDTO;
import spring.dto.CategorySetDTO;
import spring.dto.PostDTO;
import spring.vo.AccountCreateVO;
import spring.vo.ProfileVO;
import spring.vo.VerifyVO;

@Repository
@Slf4j
// db를 연동해 컨트롤러로 정보를 전송
public class AccountDao {
    @Autowired // 아래 JdbcTemplate에 bean을 알아서 넣어줌
    JdbcTemplate jt;

    ObjectMapper mapper = new ObjectMapper();

    private List<Map<String, Object>> getRows(String queryString) { // List<Map<String, Object>> 형태로 모든 선택된 열을 리턴
        return jt.queryForList(queryString);
    }

    private Random random = new Random();

    // 이메일 중복 확인 함수
    public boolean emailAvailable(String email) {
        email = email.replace(" ", "+");
        String queryString = String.format("SELECT * FROM members WHERE email = '%s';", email);
        int userCount = getRows(queryString).size();
        if (userCount >= 1) {
            return false;
        } else {
            return true;
        }
    }

    public String getPassword(String email) { // 데이터베이스에 저장한 암호화된 비밀번호를 리턴
        email = email.replace(" ", "+");
        String queryString = String.format("SELECT pw FROM members WHERE email = '%s';", email); // pw 컬럼만 가져옴
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return result.get(0).get("pw").toString();
        } else {
            return null;
        }
    }

    public AccountDataDTO getUserData(String email) {
        String queryString = String.format("SELECT * FROM members WHERE email = '%s';", email);
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return mapper.convertValue(result.get(0), AccountDataDTO.class);
        } else {
            return null;
        }
    }

    public int createAccount(AccountCreateVO accountData) { // 계정 생성
        String queryString = String.format(
                "INSERT INTO members VALUES ('%s', '%s', '%s', false, null, null, false, null)",
                accountData.getEmail(), accountData.getPassword(), accountData.getNickname()
        );
        return jt.update(queryString);
    }

    public int setNickname(String email, String nickname) { // 닉네임 변경
        email = email.replace(" ", "+");
        return jt.update(String.format("UPDATE members SET nickname = '%s' WHERE email = '%s'", nickname, email));
    }

    public int updateProfile(String email, ProfileVO vo) { // 닉네임 & 프로필 소개 변경
        return jt.update(String.format("UPDATE members SET nickname = '%s', about = '%s' WHERE email = '%s'", vo.getNickname(), vo.getAbout(), email));
    }

    public int setProfileImage(String email, String path) { // 프로필 이미지 변경
        return jt.update(String.format("UPDATE members SET faceimg = '%s' WHERE email = '%s'", path, email));
    }

    public String generateVerifyCode(AccountDataDTO userData) {
        String vcode = "";
        for (int ind = 1; ind <= 6; ind++) {
            int textOrInt = random.nextInt(2);
            if (textOrInt == 0) {
                vcode += (char)(random.nextInt(26) + 97);
            } else {
                vcode += (char)(random.nextInt(10) + 48);
            }
        }

        jt.update(String.format("UPDATE members SET vcode = '%s' WHERE email = '%s'", vcode, userData.getEmail()));
        return vcode;
    }

    public String getVerifyCode(AccountDataDTO userData) {
        List<Map<String, Object>> queryResult = getRows(String.format("SELECT vcode FROM members WHERE email = '%s'", userData.getEmail()));
        if (queryResult.size() >= 1) {
            return queryResult.get(0).get("vcode").toString();
        } else {
            return null;
        }
    }

    public int finishVerify(AccountDataDTO userData) {
        return jt.update(String.format("UPDATE members SET verify = true, vcode = NULL WHERE email = '%s'", userData.getEmail()));
    }

    public int resetPassword(String email, String password) {
        return jt.update(String.format("UPDATE members SET pw = '%s' WHERE email = '%s'", password, email));
    }
}
