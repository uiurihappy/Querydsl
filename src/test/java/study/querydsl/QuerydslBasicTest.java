package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;
	JPAQueryFactory queryFactory;

	// 각 테스트 실행 전에 데이터를 세팅하고 들어간다.
	@BeforeEach
	public void before() {
		queryFactory = new JPAQueryFactory(em);
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
//		em.flush();
//		em.clear();
	}

	@Test
	public void startJPQL() {
		// member1을 찾아라
		String qlString =
				"select m from Member m " +
				"where m.username = :username";

		Member findMember = em.createQuery(qlString, Member.class)
				.setParameter("username", "member1")
				.getSingleResult();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void startQuerydsl(){
		/*
		1. JPAQueryFactory로 em를 생성자를 넘겨주어 시작
		2. Q Model 빌드
		3. QModel를 변수 명에 aliasing 한다.
		4. 일반 sql 처럼 작성하면 된다.
		5. 파라미터 바인딩을 안해도 자동으로 넣으면 된다.
		- 런타임 오류가 아닌 자바 컴파일 시점부터 에러를 잡아준다.
		 */
//		QMember m = new QMember("m");
//		QMember m = QMember.member;

//		Member findMember = queryFactory
//				.select(m)
//				.from(m)
//				.where(m.username.eq("member1")) // 자동으로 파라미터 바인딩 처리
//				.fetchOne();

		// static import (권장 사항)
		// Querydsl은 JPQL의 Builder 역할 수행
		Member findMember = queryFactory
				.select(member)
				.from(member)
				.where(member.username.eq("member1")) // 자동으로 파라미터 바인딩 처리
				.fetchOne();

		// 같은 테이블 join할 때 유용
//		QMember m1 = new QMember("m1"); // JPQL의 alias가 m1으로 바뀜
//		Member findMember = queryFactory
//				.select(member)
//				.from(member)
//				.where(member.username.eq("member1")) // 자동으로 파라미터 바인딩 처리
//				.fetchOne();


		assertEquals(findMember.getUsername(), "member1");
	}

}
