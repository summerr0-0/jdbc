package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *  트랜젝션 - @Transactional Aop
 */
@Slf4j
//springAOP 적용하려면 스프링 컨테이너가 필요하다
//springbootTest를 통해 테스트 시작시 스프링 컨테이너를 생성한다
@SpringBootTest
class MemberServiceV3_3Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService;

    //테스트 안에서 내부 설정 클래스를 만들어서 사용한다.
    @TestConfiguration
    static class TestConfig{
        //스프링에서 기본으로 사용할 데이터소스를 스프링 빈으로 등록
        @Bean
        DataSource dataSource(){
            return new DriverManagerDataSource(URL,USERNAME,PASSWORD);
        }

        //스프링에서 제공하는 트랜잭션 AOP는 스프링 빈에 등록된 트랜잭션 매니저를
        //찾아서 사용하기 때문에 트랜잭션 매니저를 스프링 빈으로 등록한다
        @Bean
        PlatformTransactionManager transactionManager(){
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3(){
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3(){
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }


    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AOP적용확인(){
        //트랜잭션 프록시를 위해 스프링애서 새로 만든 클래스
        //class hello.jdbc.service.MemberServiceV3_3$$EnhancerBySpringCGLIB$$90f8023a
        log.info("==memberService.getClass() = {}",memberService.getClass());
        //class hello.jdbc.repository.MemberRepositoryV3
        log.info("==memberRepository.getClass() = {}",memberRepository.getClass());

        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    void 정상_이체() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        //같은 커넥션을 사용하기 때문에 Start와 End 로그 사이에는 getConnection이 나오지 않는다
        log.info("Start");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("End");

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(8000);
        Assertions.assertThat(findMemberB.getMoney()).isEqualTo(12000);

    }

    @Test
    void 이체중_예외_발생() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        //예외 발생
        //같은 커넥션을 사용하기 때문에 Start와 End 로그 사이에는 getConnection이 나오지 않는다
        log.info("Start");
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
            .isInstanceOf(IllegalStateException.class);
        log.info("End");

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());

        //에러가발생해도 트랜젝션 단위로 롤백되기 때문에 전부 원상태로 돌아간다
        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(10000);
        Assertions.assertThat(findMemberB.getMoney()).isEqualTo(10000);

    }
}
