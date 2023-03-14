package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - Connection을 파라미터로 전달받기
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        //Insert 쿼리
        String sql = "insert into member(member_id, money) values(?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            //커넥션 획득
            con = getConnection();

            //db에 전달할 sql과 파라미터로 전달할 데이터들을 준비
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            //준비된 sql문 실행
            pstmt.executeUpdate();
            return member;

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;

        } finally {
            //리소스 정리
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        //데이터 단건 조회를 위한 sql문
        String sql = "Select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, memberId);

            //실행결과를 담는 resutset
            //Resultset 내부에 있는 커서를 이동해 다음 데이터를 조회할 수 있다
            rs = stmt.executeQuery();

            //next를 호출하면 cursor를 이동해 다음데이터로 조회
            //최초의 커서는 데이터를 가리키고 있지 않기 때문에 rs.next() 를 한번 호출해야 함
            if (rs.next()) {
                Member member = new Member();
                //커서가 이동한 위치의 member_id 데이터를 String 타입으로 변환
                member.setMemberId(rs.getString("member_id"));
                //커서가 이동한 위치의 money 데이터를 int 타입으로 변환
                member.setMoney(rs.getInt("money"));

                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, stmt, rs);
        }

    }

    //커넥션을 주입받는 findById
    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "Select * from member where member_id = ?";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            //외부에서 받아온 커넥션유지
            stmt = con.prepareStatement(sql);
            stmt.setString(1, memberId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));

                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(stmt);
            //트랜잭션을 위해 커넥션 닫으면 안됨
            //JdbcUtils.closeConnection(con);
        }

    }

    public void update(String memberId, int money) throws SQLException {
        //업데이트 쿼리문
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            //커넥션 연결
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            //쿼리를 실행하고 영향받은 row 수 반환
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;

        } finally {
            //리소스 반환
            close(con, pstmt, null);
        }
    }

    //커넥션을 주입받는 update
    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;
        try {
            //외부에서 받아온 커넥션유지
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;

        } finally {
            JdbcUtils.closeStatement(pstmt);
            //커넥션은 여기서 닫지 않는다
//            JdbcUtils.closeConnection(con);
        }
    }

    public void delete(String memberId) throws SQLException {
        //삭제 쿼리문
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            //커넥션 연결
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            //쿼리 실행
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;

        } finally {
            //리소스 반환
            close(con, pstmt, null);
        }
    }


    private void close(Connection con, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);

    }

    //커넥션 획득
    private Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        log.info("get connection = {}, class = {}", connection, connection.getClass());
        return connection;
    }

}
