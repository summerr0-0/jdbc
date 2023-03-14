package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

    //트랜잭션 메니저는 외에서 주입받는다
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    //계좌이체
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작 기본 속성으로 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            //비즈니스 로직 수행
            bizLogic(fromId, toId, money);

            //정상동작시 커밋
            transactionManager.commit(status);

        } catch (Exception e) {
            //예외 발생시 롤백
            transactionManager.rollback(status);
            throw new IllegalStateException(e);
        }
        //자원정리를 transactionManager가 알아서 해준다

    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
