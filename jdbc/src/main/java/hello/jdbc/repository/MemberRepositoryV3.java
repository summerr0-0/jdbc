package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 메니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */
@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
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
        //트랜잭션 동기화를 사용하려면 DataSourceUtils 사용해야 한다.
        //DataSourceUtils.releaseConnection
        //트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지한다
        //트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션을 닫는다
        DataSourceUtils.releaseConnection(con,dataSource);

    }

    //커넥션 획득
    private Connection getConnection() throws SQLException {
        //트랜잭 동기화를 사용하려면 DataSourceUtils를 이용해야 한다
        //DataSourceUtils.getConnection
        //트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환한다
        //트랜잭션 동기화 매니저가 관리하는 커넥션이 없으면 새로운 커넥션을 생성해서 반환한다.
        Connection connection = DataSourceUtils.getConnection(dataSource);
        log.info("get connection = {}, class = {}", connection, connection.getClass());
        return connection;
    }

}
