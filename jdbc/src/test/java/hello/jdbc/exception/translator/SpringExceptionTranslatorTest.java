package hello.jdbc.exception.translator;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SpringExceptionTranslatorTest {
    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode() {
        //문법이 잘못된 query
        String sql = "select bad grammer";
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            //에러코드를 하나하나 입력해서 확인하는 것은 현실성이 없다
            assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode = {}", errorCode);
            ////org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
        }
    }

    @Test
    void exceptionTranslator() {
        //문법이 잘못된 query
        String sql = "select bad grammer";
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);

            //org.springframework.jdbc.support.sql-error-codes.xml
            //위에 에러코드들이 정의되어있다
            SQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);

            //org.springframework.jdbc.BadSqlGrammarException
            //스프링데이터 접근계층의 예외로 변환시켜 준다
            DataAccessException resultEx = exTranslator.translate("select", sql, e);
            log.info("resultEx", resultEx);

            assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}


