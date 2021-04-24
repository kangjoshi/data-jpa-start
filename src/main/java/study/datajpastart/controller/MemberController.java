package study.datajpastart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpastart.dto.MemberDto;
import study.datajpastart.entity.Item;
import study.datajpastart.entity.Member;
import study.datajpastart.repository.ItemRepository;
import study.datajpastart.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    // 도메인 클래스 컨버터는 조회용으로만 사용되어야 한다. (영속된 상태가 아니기 때문에 잘못 사용하게되면... 귀찮아질수도 있다.)
    @GetMapping("/members2/{id}")   // id는 PK이므로 스프링 JPA가 회원 엔티티를 객체로 반환한다.
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable) {   // request parameter page, sort, size를 바인딩한다.
        Page<Member> page = memberRepository.findAll(pageable);
        return page.map(MemberDto::new);
    }

    @PostConstruct
    public void init() {
        itemRepository.save(new Item("A"));
        /*
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("member" + i, 10));
        }

         */
    }


}
