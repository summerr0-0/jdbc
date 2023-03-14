package hello.jdbc.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

public class CheckedAppTest {
    /**
     * 체크 예외 대부분이 복구 불가능한 예외
     * ex)db에 문제가 생긴 예외 <- 어차피 복구가 불가능한 예외
     * 일관성있게 공통으로 처리하고 개발자가 오류를 빠르게 인지하는게 중요
     * ex) 서블릿 필터. 스프링 인터셉터, 스프링 controllerAdvice 등으로..
     *
     * 의존 관계에 대한 문제
     *  체크예외는 의존관계 문제
     *  대부분 복구가 불가능한 예외기 때문에
     *  컨트롤러나 서비스 입장에서 처리 불가능
     *  계속 throws를 던지는 행위 발생
     *
     *  SQLException을 의존할 경우
     *  만약 Jdbc가 아니라 jpa를 바꾼다면 관련 exception 다 수정해야함
     *
     *  SQLException을 없애겠다고 최상위 예외 Exception을 던진다면?
     *  모든 체크예외를 밖으로 던지게 된다
     *  다른 체크 예외를 놓치게 된다
     *  => Exception 밖으로 던지지 말기
   *  */

    @Test
    void checked(){
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(()-> controller.request())
            .isInstanceOf(Exception.class);
    }

    static class Controller{
        Service service = new Service();
        public void request() throws SQLException, ConnectException{
            service.logic();
        }
    }

    static class Service{
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        //모든 Exception을 throws해주어야 한다
        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }
    }
    static class NetworkClient{
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }
    static class Repository{
        public void call() throws SQLException {
            throw new SQLException("ex");
        }
    }
}
