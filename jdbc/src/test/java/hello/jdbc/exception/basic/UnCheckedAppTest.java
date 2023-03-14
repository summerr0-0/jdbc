package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;
@Slf4j
public class UnCheckedAppTest {
    @Test
    void unchecked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(() -> controller.request())
            .isInstanceOf(Exception.class);
    }

    @Test
    void printEx(){
        Controller controller = new Controller();

        try {
            controller.request();
        }catch (Exception e){
            log.info("ex",e);
        }

    }

    //런타임 예외기 때문에 컨트롤러나 서비스가
    //예외를 처리할 수 없다면 throws를 생략할 수 있다
    //컨트롤러와 서비스에서 의존관계가 사라진다
    //
    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    //SqlException 이 발생하면 런타임 에러로 전환헤서 예외 던짐
    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                //예외 던지기
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    //cause : 이전예외를 포함한 할수있다
    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
