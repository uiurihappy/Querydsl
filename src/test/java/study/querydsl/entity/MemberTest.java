package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

	@Autowired
	EntityManager em;

	@Test
	public void testEntity() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");

		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamB);

		Member member3 = new Member("member3", 30, teamA);
		Member member4 = new Member("member4", 40, teamB);


		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

		// clear
		em.flush();
		em.clear();

		List<Member> members = em.createQuery("select m from Member m", Member.class)
				.getResultList();

		for (Member member : members) {
			// 실무에선 자동화 테스트이니 assert로 해야 한다.
			System.out.println("member = " + member);
			System.out.println("-> member.team = " + member.getTeam());
		}

	}

}