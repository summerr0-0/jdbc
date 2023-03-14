package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Exception을 상속받는 예외는 체크 예외가 된다
 */
@Slf4j
public class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throws() {
        Service service = new Service();
        Assertions.assertThatThrownBy(() -> service.callThrow())
            .isInstanceOf(MyCheckedException.class);
    }

    //Exception 상속
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }

        /**
         * checked 예외는
         * 예외를 잡아서 처리하거나 던지거나 해야 한다
         */
    }

    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                //예외처리 로직
                log.info("예외처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 체크예외를 잡아서 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던진다
         * throws를 메서드에 필수로 선언해야 한다
         *
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        //레포지토리에서 Exception 을 터뜨림
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }
}

