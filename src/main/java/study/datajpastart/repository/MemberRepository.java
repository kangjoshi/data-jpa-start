package study.datajpastart.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpastart.dto.MemberDto;
import study.datajpastart.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*
* @Repository를 따로 선언하지 않아도 된다.
* */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    /**
     * 메서드명 쿼리
     *  - JPA가 메서드명으로 쿼리를 생성한다.
     * */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * namedQuery
     *  - 애노테이션은 주석으로 해도 동작한다.
     *      JPA가 엔티티.메소드명으로 namedQuery를 찾고 있다면 namedQuery를 사용하고 없다면 메소드명 쿼리를 사용한다.
     */
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param(value = "username") String username);

    /**
     * 직접 쿼리 작성 (이름이 없는 namedQuery)
     *  - namedQuery와 동일하게 컴파일 시점에 쿼리에 이상이 없는지 검사한다.
     *  - 메서드명 쿼리는 파라미터가 두개 이상될 때 메서드명이 길어지는 단점이 있다. 직접 쿼리를 작성하여 사용하는것이 좋다.
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param(value = "username") String username, @Param("age") int age);

    /**
     * Dto로 조회
     * */
    @Query("select new study.datajpastart.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByMembers(@Param("names") Collection<String> names);

    // 리스트
    List<Member> findListByUsername(String username);
    // 단건
    Member findMemberByUsername(String username);
    // Optional
    Optional<Member> findOptionalMemberByUsername(String username);

    // count를 구하는 쿼리를 추가로 실행한다.
    Page<Member> findByAge(int age, Pageable pageable);

    // count를 구하는 쿼리를 직접 지정하여 실행한다.
    @Query(value = "select m from Member m left join m.team where m.age = :age", countQuery = "select count(m) from Member m")
    Page<Member> findWithCountByAge(@Param(value = "age") int age, Pageable pageable);

    // count를 구하는 로직을 실행하지 않는다.
    // Pageable에서 지정한 size + 1을 size로 수행한다. (다음 페이지가 있는지 여부를 확인하기 위해)
    Slice<Member> findSliceByAge(int age, Pageable pageable);

    // 벌크연산
    // 벌크 연산은 영속성 컨택스트에서 영속화 하지 않고 DB에 바로 update를 수행한다.
    // 벌크 연산 후에는 flush() clear() 하는것이 좋다.
    @Modifying(clearAutomatically = true)  // update임을 알리기 위해 사용 (clearAutomatically = flush(), clear()를 자동으로 한다. 벌크 연산 후 조회하는 로직이 있다면 꼭 붙여야할 듯)
    @Query(value = "update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param(value = "age") int age);

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param(value = "username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

}
