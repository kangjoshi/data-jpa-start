package study.datajpastart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpastart.dto.MemberDto;
import study.datajpastart.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*
* @Repository를 따로 선언하지 않아도 된다.
* */
public interface MemberRepository extends JpaRepository<Member, Long> {

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
}
