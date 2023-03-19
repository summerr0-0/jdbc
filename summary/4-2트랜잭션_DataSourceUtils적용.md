# 적용하기 - DataSourceUtils



- 트랜잭션 매니저를 적용한 MemberRepositoryV3
  - `getConnection()`
    - getConnection(dataSource);
    - 트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션 반환
    - 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 새로운 커넥션을 생성해서 반환
  - `close()`
    - DataSourceUtils.releaseConnection(con,dataSource);
    - 커넥션이 바로 닫히는게 아님
    - 트랜잭션을 사용하기 위해 동기화된 커넥션은 닫지 않고 그대로 유지해준다
    - 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션을 닫음

```java
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

```



- 트랜잭션 매니저를 사용하는 MemberServiceV3_1

  - `private final PlatformTransactionManager transactionManager`

    - 추상화된 트랜잭션 매니저

  - `transactionManager.getTransaction(..);`

    - 트랜잭션 시작
    - `TransactionStatus`를 반환한다 현재 트랜잭션의 상태정보가 포함됨
    - `DefaultTransactionDefinition()` 트랜잭션과 관련된 옵션을 지정할 수 있다

    

  - `transactionManager.commit(status);`

    - 트랜잭션이 성공하면 커밋

    - 현재 트랜잭션의 상태정보가 포함되어 있다(TransactionStatus) 

    

  - `transactionManager.rollback(status);`

    - 트랜잭션이 실패하면 커밋
    - 현재 트랜잭션의 상태정보가 포함되어 있다(TransactionStatus) 

```java
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

```





- 서비스를 테스트하는 MemberServiceV3_1Test
  - ` PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);`
    - 트랜잭션 매니저는 데이터소스를 통해 커넥션을 생성하기 때문에 `DataSource`가 필요하다
    - `new DataSourceTransactionManager(dataSource)` 
      - JDBC용 트랜잭션 매니저를 선택해서 서비스에 주입한다

```Java
/**
 *  트랜젝션 - 커넥션 파라미터 전달 방식
 */
@Slf4j
class MemberServiceV3_1Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_1 memberService;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);

        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        memberService = new MemberServiceV3_1(transactionManager,memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
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

```

