package study.datajpastart.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpastart.dto.MemberDto;
import study.datajpastart.entity.Member;
import study.datajpastart.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {
        Member member = new Member("memberA", 19, new Team("TeamA"));
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void testNamedQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);

        assertThat(result.size()).isEqualTo(1);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    void testQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        Member findMember = result.get(0);

        assertThat(result.size()).isEqualTo(1);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member1 = new Member("AAA", 10, team);
        memberRepository.save(member1);

        List<MemberDto> memberDtos = memberRepository.findMemberDto();
        for (MemberDto memberDto : memberDtos) {
            System.out.println(memberDto);
        }
    }

    @Test
    void testQueryParameterArray() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        Set<String> names = new HashSet<>();
        names.add("AAA");
        names.add("BBB");

        List<Member> result = memberRepository.findByMembers(names);

        for (Member member : result) {
            System.out.println("=====> " + member);
        }
    }

    @Test
    void returnType() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findListByUsername("AAA");
        for (Member member : members) {
            System.out.println("=====> list : " + member);
        }

        Member member = memberRepository.findMemberByUsername("AAA");
        System.out.println("=====> member : " + member);

        Optional<Member> optionalMember = memberRepository.findOptionalMemberByUsername("AAA");
        System.out.println("=====> optional : " + optionalMember);
    }

    @Test
    void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findWithCountByAge(age, pageRequest);

        List<Member> content = page.getContent();
        long totalcount = page.getTotalElements();

        System.out.println("totalcount : " + totalcount);
        System.out.println("hasNext : " + page.hasNext());
        for (Member member : content) {
            System.out.println(member);
        }
    }

    @Test
    void slice() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 20));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        List<Member> content = page.getContent();

        System.out.println("hasNext : " + page.hasNext());
        for (Member member : content) {
            System.out.println(member);
        }
    }


    @Test
    void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);

        em.flush();
        em.clear();

        assertThat(resultCount).isEqualTo(3);
    }


    @Test
    void queryHint() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findById(member.getId()).get();
        findMember.changeUsername("member2");   // 변경 감지를 하기위해 원본이 필요하고 그로인해 추가적이 메모리가 소요된다.

        em.flush();

        Member findReadOnlyMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.changeUsername("member2");   // 읽기 전용으로 불러왔으므로 변경 감지가 일어나지 않는다.

        em.flush();
    }

    @Test
    void lock() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();

        List<Member> findMember = memberRepository.findLockByUsername("member1");
    }

}