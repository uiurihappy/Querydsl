package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

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

	@Test
	public void search() {
		// member1 이면서 age가 10에서 30 사이인 사람 조회
		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1")
						.and(member.age.between(10, 30)))
				.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void searchAndParam() {
		// member1 이면서 age가 10인 사람 조회
		// and 조건인 경우 ,(chain) 를 활용하여 사용하는 것을 권장
		Member findMember = queryFactory
				.selectFrom(member)
				.where(
						member.username.eq("member1"),
						member.age.eq(10), null
				)
				.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void resultFetch() {
//		// list 조회
//		List<Member> fetch = queryFactory
//				.selectFrom(member)
//				.fetch();
//
//		// 단건 조회
//		Member fetchOne = queryFactory
//				.selectFrom(member)
//				.fetchOne();
//
//		Member fetchFirst = queryFactory
//				.selectFrom(member)
////				.limit(1).fetchOne(); // 아래와 동일
//				.fetchFirst();

		QueryResults<Member> results = queryFactory
				.selectFrom(member)
				.fetchResults();

		results.getTotal();
		List<Member> content = results.getResults();

		long total = queryFactory
				.selectFrom(member)
				.fetchCount();
	}

	/**
	 * 회원 정렬 순서
	 * 1. 회원의 나이 내림차순 (desc)
	 * 2. 회원 이름 올림차순 (asc)
	 * 단, 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
	 */
	@Test
	public void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> results = queryFactory
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();

		Member member5 = results.get(0);
		Member member6 = results.get(1);
		Member nullMember = results.get(2);

		assertEquals(member5.getUsername(), "member5");
		assertEquals(member6.getUsername(), "member6");
		assertNull(nullMember.getUsername(),"member null");

	}

	// Paging
	@Test
	public void paging1() {
		List<Member> results = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)      // index는 0이니까 앞에 한 개 skip하고 정렬
				.limit(2)
				.fetch();

		assertEquals(results.size(), 2);
	}

	@Test
	public void paging2() {
		QueryResults<Member> queryResults = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)      // index는 0이니까 앞에 한 개 skip하고 정렬
				.limit(2)
				.fetchResults();

		assertEquals(queryResults.getTotal(), 4);
		assertEquals(queryResults.getLimit(), 2);
		assertEquals(queryResults.getOffset(), 1);
		assertEquals(queryResults.getResults().size(), 2);


	}

	@Test
	public void aggregation() {
		// querydsl Tuple:
		List<Tuple> result = queryFactory
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min()
				)
				.from(member)
				.fetch();

		Tuple tuple = result.get(0);

		assertEquals(tuple.get(member.count()), 4);
		assertEquals(tuple.get(member.age.sum()), 100);
		assertEquals(tuple.get(member.age.avg()), 25);
		assertEquals(tuple.get(member.age.max()), 40);
		assertEquals(tuple.get(member.age.min()), 10);

	}

	/**
	 * 팀의 이름과 각 팀의 평균 연령을 구해라.
	 */
	@Test
	public void groupby() {
		List<Tuple> result = queryFactory
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				.fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertEquals(teamA.get(team.name), "teamA");
		assertEquals(teamA.get(member.age.avg()), 20);

		assertEquals(teamB.get(team.name), "teamB");
		assertEquals(teamB.get(member.age.avg()), 30);

	}
}
