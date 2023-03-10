package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        // 저장
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);

        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);


//		Member member5 = new Member("MemberA", 50, teamA);
//		Member member6 = new Member("MemberB", 60, teamB);


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
//		em.persist(member5);
//		em.persist(member6);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(20);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

//		List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        List<MemberTeamDto> result = memberRepository.search(condition);

        assertThat(result).extracting("username").containsExactly("member2", "member4");

    }

    @Test
    public void searchTestSimple() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);

        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);


//		Member member5 = new Member("MemberA", 50, teamA);
//		Member member6 = new Member("MemberB", 60, teamB);


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
//		em.persist(member5);
//		em.persist(member6);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
//		List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);


        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");

    }

    @Test
    public void querydslPredicateExecutorTest() {

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);

        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);


//		Member member5 = new Member("MemberA", 50, teamA);
//		Member member6 = new Member("MemberB", 60, teamB);


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
//		em.persist(member5);
//		em.persist(member6);

        QMember member = QMember.member;
        /*
         querydslPredicateExecutor
         1. 조인이 안됨 (left join)
         2. 마찬가지로 querydsl에 의존한다.
         3. 복잡한 연관관계 매핑하고 query를 작성하기 어렵다.
         */

        Iterable<Member> result1 = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));

        for (Member findMember : result1) {
            System.out.println("member = " + member);
        }
    }

}