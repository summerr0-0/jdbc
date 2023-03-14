package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Exception을 상속받는 예외는 체크 예외가 된다
 */
@Slf4j
public class UnCheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();

        Assertions.assertThatThrownBy(() -> service.callThrow())
            .isInstanceOf(MyUnCheckedException.class);
    }

    /**
     * RuntimeException을 상속받은 이외는 ㅇ언체크 예외가 된다
     */
    static class MyUnCheckedException extends RuntimeException {
        public MyUnCheckedException(String message) {
            super(message);
        }

        /**
         * Unchecked 예외는
         * 예외를 잡아서 처리하거나 던지지 않아도 된다
         * 예외를 잡지 않으면 자동으로 밖으로 던진다
         */
    }

    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우에
         * 예외를 잡아서 처리하는 코드
         * 예외 안잡아도 됨
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUnCheckedException e) {
                //예외처리 로직
                log.info("예외처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 체크예외와 다르게
         * 예외를 잡지 않으면 자연스럽게 상위로 넘어간다
         * <p>
         * 이 service에서는 MyUnCheckedException에 대해 알 필요가 없게 된다
         */
        public void callThrow() {
            repository.call();
        }

    }

    static class Repository {
        //레포지토리에서 언체크드 익셉션을 터뜨린다
        //메서드에 throws Exception을 붙이지 않아도 된다
        public void call() {
            throw new MyUnCheckedException("ex");
        }
    }
}

