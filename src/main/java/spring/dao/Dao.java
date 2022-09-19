package spring.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

@Repository
@Slf4j
// db를 연동해 컨트롤러로 정보를 전송
public class Dao {
    @Autowired // 아래 JdbcTemplate에 bean을 알아서 넣어줌
    JdbcTemplate jt;

    // 디버깅용 - 나중에 없애기
    public List<Map<String, ?>> getMember(String userEmail) {
        if (userEmail == null) {
            userEmail = "";
        } else { // 이메일 들어가면 쿼리문에 조건문 붙임
            userEmail = String.format(" WHERE email = '%s'", userEmail);
        }

        return jt.query(String.format("SELECT * FROM members%s;", userEmail), (ResultSet rs, int rowNum) -> {
            ResultSetMetaData rsmd = rs.getMetaData();
            Map<String, Object> map = new HashMap<>();
            map.put("email", rs.getString(1));
            map.put("pw", rs.getString(2));
            map.put("nickname", rs.getString(3));
            map.put("verify", rs.getInt(4));
            map.put("faceimg", rs.getString(5));
            map.put("about", rs.getString(6));
//            for (int ind = 0; ind < rsmd.getColumnCount(); ind++) {
//                String columnName = rsmd.getColumnName(ind);
//                int columnType = rsmd.getColumnType(ind);
//                switch (columnType) {
//                    case Types.INTEGER:
//                        map.put(columnName, rs.getInt(ind)); break;
//                    case Types.FLOAT:
//                        map.put(columnName, rs.getFloat(ind)); break;
//                    case Types.DOUBLE:
//                        map.put(columnName, rs.getDouble(ind)); break;
//                    case Types.DATE:
//                        map.put(columnName, rs.getDate(ind)); break;
//                    default:
//                        map.put(columnName, rs.getString(ind));
//                }
//            }
            return map;
        });
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

    public int createAccount(String email, String password, String nickname) { // 계정 생성
        return jt.update(String.format("INSERT INTO members VALUES ('%s', '%s', '%s', 0, null, null)", email, password, nickname));
    }
}
