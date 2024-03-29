package study.datajpastart.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpastart.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 19, teamA);
        Member member2 = new Member("member2", 29, teamA);
        Member member3 = new Member("member3", 39, teamB);
        Member member4 = new Member("member4", 49, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();

        for (Member member : members) {
            System.out.println(member);
            System.out.println(" => team : " + member.getTeam());
        }
    }

    @Test
    void testJpaBaseEntity() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        em.flush();
        em.clear();

        Member findMember = memberRepository.findById(member.getId()).get();

        System.out.println("=====>" + findMember.getCreatedDate());
        System.out.println("=====>" + findMember.getLastModifiedDate());
        System.out.println("=====>" + findMember.getCreateBy());
        System.out.println("=====>" + findMember.getLastModifiedBy());
    }


}