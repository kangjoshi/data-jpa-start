package study.datajpastart.repository;

import study.datajpastart.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();

}
