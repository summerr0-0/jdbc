package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {
    @Test
    void driverManager() throws SQLException {
        //drivemanager : 연결을 획득할 때마다 user, username, password 전달
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        //connection=conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class org.h2.jdbc.JdbcConnection
        //connection=conn1: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class org.h2.jdbc.JdbcConnection
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDatasource - 항상 새로운 커넥션을 획득
        //data source : datasource 객체 전달할 때만 url, username, password 전달
        //커넥션을 획득할 땐 파라미터가 필요없다
        //설정과 사용이 분리됨
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        //connection=conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class org.h2.jdbc.JdbcConnection
        //connection=conn1: jdbc:h2:tcp://localhost/~/test user=SA, class=class org.h2.jdbc.JdbcConnection
        useDataSource(dataSource);
    }

    @Test
    void dataSourceConnection() throws SQLException, InterruptedException {
        HikariDataSource dataSource = new HikariDataSource();

        //com.zaxxer.hikari.HikariConfig - jdbcUrl................................jdbc:h2:tcp://localhost/~/test
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        //풀 최대 사이즈
        //com.zaxxer.hikari.HikariConfig - maximumPoolSize................................10
        dataSource.setMaximumPoolSize(10);
        //풀 이름
        //com.zaxxer.hikari.HikariConfig - poolName................................"Mypool"
        dataSource.setPoolName("Mypool");

        //커넥션 풀 전용 쓰레드가 커넥션을 10개 채운다
        //[Mypool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - Mypool - Adde  connection conn0: url=jdbc:h2:tcp://localhost/~/test user=SA
        //[Mypool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - Mypool - Added connection conn1: url=jdbc:h2:tcp://localhost/~/test user=SA
        //[Mypool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - Mypool - Added connection conn2: url=jdbc:h2:tcp://localhost/~/test user=SA
        //...

        //커넥션 풀에서 커넥션 획득
        //connection=HikariProxyConnection@1927963027 wrapping conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class com.zaxxer.hikari.pool.HikariProxyConnection
        //connection=HikariProxyConnection@833240229 wrapping conn1: url=jdbc:h2:tcp://localhost/~/test user=SA, class=class com.zaxxer.hikari.pool.HikariProxyConnection
        useDataSource(dataSource);
        Thread.sleep(1000);

        //커넥션 현재 상황
        //[Mypool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - Mypool - After adding stats (total=10, active=2, idle=8, waiting=0)
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();

        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());

    }
}
