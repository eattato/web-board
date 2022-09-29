package spring.dao;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.vo.AccountCreateVO;

@Repository
@Slf4j
// db를 연동해 컨트롤러로 정보를 전송
public class AccountDao {
    @Autowired // 아래 JdbcTemplate에 bean을 알아서 넣어줌
    JdbcTemplate jt;

    // 디버깅용 - 나중에 없애기
    public List<Map<String, Object>> getMember(String userEmail) {
        if (userEmail == null) {
            userEmail = "";
        } else { // 이메일 들어가면 쿼리문에 조건문 붙임
            userEmail = String.format(" WHERE email = '%s'", userEmail);
        }

        return getRows(String.format("SELECT * FROM members%s;", userEmail));
    }

    private List<Map<String, Object>> getRows(String queryString) { // List<Map<String, Object>> 형태로 모든 선택된 열을 리턴
        return jt.queryForList(queryString);
    }

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

    public Map<String, Object> getUserProfile(String email) {
        email = email.replace(" ", "+");
        String queryString = String.format("SELECT nickname, faceimg FROM members WHERE email = '%s';", email);
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public Map<String, Object> getUserData(String email) {
        String queryString = String.format("SELECT * FROM members WHERE email = '%s';", email);
        List<Map<String, Object>> result = getRows(queryString);
        if (result.size() >= 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public int createAccount(AccountCreateVO accountData) { // 계정 생성
        String queryString = String.format(
                "INSERT INTO members VALUES ('%s', '%s', '%s', 0, null, null)",
                accountData.getEmail(), accountData.getPassword(), accountData.getNickname()
        );
        return jt.update(queryString);
    }

    public int setNickname(String email, String nickname) { // 닉네임 변경
        email = email.replace(" ", "+");
        return jt.update(String.format("UPDATE members SET nickname = '%s' WHERE email = '%s'", nickname, email));
    }

    public int setProfileImage(String email, String path) { // 프로필 이미지 변경
        return jt.update(String.format("UPDATE members SET faceimg = '%s' WHERE email = '%s'", path, email));
    }
}
