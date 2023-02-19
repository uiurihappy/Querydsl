package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
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

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;

	//
	@PersistenceUnit
	EntityManagerFactory emf;


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

	/**
	 * teamA에 소속된 모든 회원
	 *
	 */
	@Test
	public void joinTest() {
		List<Member> result = queryFactory
				.selectFrom(member)
				// inner join team
				.leftJoin(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();

		assertThat(result)
				.extracting("username")
				.containsExactly("member1", "member2");
	}

	/**
	 * 세타 조인
	 * 회원의 이름이 팀 이름과 같은 회원 조회
	 */
	@Test
	public void thetaJoinTest() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));

		// 외부 조인이 불가능
		List<Member> result = queryFactory
				.select(member)
					.from(member, team)
						.where(member.username.eq(team.name))
								.fetch();

		assertThat(result)
				.extracting("username")
				.containsExactly("teamA", "teamB");
	}

	/**
	 * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	 * JPQL: select m, t from Member m left join Team m.team t on t.name = 'teamA'
	 * SQL:
	 SELECT m.*, t.*
	 FROM Member m
	 LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
	 */
	@Test
	public void join_on_filtering() {
		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				/*
				select
					m1_0.member_id,
					m1_0.age,
					m1_0.team_id,
					m1_0.username,
					t1_0.team_id,
					t1_0.name
				from
					member m1_0
				left join
					team t1_0
						on t1_0.team_id=m1_0.team_id
						and t1_0.name=?
				 */
				.leftJoin(member.team, team).on(team.name.eq("teamA"))
				/*
				select
					m1_0.member_id,
					m1_0.age,
					m1_0.team_id,
					m1_0.username,
					t1_0.team_id,
					t1_0.name
				from
					member m1_0
				join
					team t1_0
						on t1_0.team_id=m1_0.team_id
						and t1_0.name=?
				 */
//				.join(member.team, team)
//				.on(team.name.eq("teamA"))
				.where(team.name.eq("teamA"))
				.fetch();

		/*
		t=[Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
		t=[Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
		t=[Member(id=5, username=member3, age=30), null]
		t=[Member(id=6, username=member4, age=40), null]
		 */
		for (Tuple tuple : result) {
			System.out.println("member tuple = " + tuple);
		}

	}

	/**
	 * 연관관계 없는 엔티티 외부 조인
	 * 회원이름과 팀 이름이 같은 회원 조회 (member.teamA = team.teamA)
	 * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
	 * SQL:
	 SELECT m.*, t.*
	 FROM Member m
	 LEFT JOIN Team t ON m.username = t.name
	 */
	@Test
	public void join_on_no_relation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));

		/*
		select
			m1_0.member_id,
			m1_0.age,
			m1_0.team_id,
			m1_0.username,
			t1_0.team_id,
			t1_0.name
		from
			member m1_0
		left join
			team t1_0
				on m1_0.username=t1_0.name
		 */
		// 외부 조인이 불가능
		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(team).on(member.username.eq(team.name))
				.fetch();

		/*
		member tuple = [Member(id=1, username=member1, age=10), null]
		member tuple = [Member(id=2, username=member2, age=20), null]
		member tuple = [Member(id=3, username=member3, age=30), null]
		member tuple = [Member(id=4, username=member4, age=40), null]
		member tuple = [Member(id=5, username=teamA, age=0), Team(id=1, name=teamA)]
		member tuple = [Member(id=6, username=teamB, age=0), Team(id=2, name=teamB)]
		member tuple = [Member(id=7, username=teamC, age=0), null]
		 */
		for (Tuple tuple : result) {
			System.out.println("member tuple = " + tuple);
		}
	}

	// Fetch join 미적용
	// 지연로딩으로 Member, Team SQL 쿼리 각각 실행
	@Test
	public void fetchJoinNo() {
		em.flush();
		em.clear();

		// 기본적으로 지연로딩이기에 team은 조회가 안된다.
		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1"))
				.fetchOne();

		// 이미 로딩된 엔티티인지 확인하고자 선언
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		System.out.println(loaded);
		assertThat(loaded).as("페치 조인 미적용").isFalse();

	}

	// Fetch join 적용
	// 즉시로딩으로 Member, Team SQL 쿼리 조인으로 한번에 조회
	@Test
	public void fetchJoinUse() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				// join(), leftJoin() 등 조인 기능 뒤에 fetchJoin() 이라고 추가하면 된다.
				.join(member.team, team).fetchJoin()
				.where(member.username.eq("member1"))
				.fetchOne();

		boolean loaded =
				emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		System.out.println(loaded);
		assertThat(loaded).as("페치 조인 적용").isTrue();

	}

	// SubQuery (서브 쿼리)

	/**
	 * 나이가 가장 많은 회원 조회
	 */
	@Test
	public void subQuery_ex1() {

		// subQuery 이기에 바깥에 있는 alias랑 겹치면 안됨
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(
						select(memberSub.age.max())
								.from(memberSub)
				))
				.fetch();

		System.out.println("result = " + result);

		assertThat(result).extracting("age")
				.containsExactly(40);
	}

	/**
	 * 나이가 평균 이상인 회원 조회
	 */
	@Test
	public void subQuery_ex2() {

		// subQuery 이기에 바깥에 있는 alias랑 겹치면 안됨
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				// goe = (>=)
				.where(member.age.goe(
						select(memberSub.age.avg())
								.from(memberSub)
				))
				.fetch();

		List<Double> ageAvg = queryFactory
				.select(memberSub.age.avg())
				.from(memberSub)
				.fetch();
		System.out.println("ageAvg = " + ageAvg); // 25.0
		System.out.println("result = " + result); // [Member(id=3, username=member3, age=30), Member(id=4, username=member4, age=40)]

		assertThat(result).extracting("age")
				.containsExactly(30, 40);
	}

	/**
	 * WHERE IN 처리
	 */
	@Test
	public void subQuery_ex3() {

		// subQuery 이기에 바깥에 있는 alias랑 겹치면 안됨
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				// goe = (>=)
				.where(member.age.in(
						select(memberSub.age)
								.from(memberSub)
								// gt = ( > )
								.where(memberSub.age.gt(10))
				))
				.fetch();

		assertThat(result).extracting("age")
				.containsExactly(20, 30, 40);
	}

	/**
	 * select SubQuery
	 */
	@Test
	public void subQuery_ex4() {

		QMember memberSub = new QMember("memberSub");

		List<Tuple> result = queryFactory
				.select(member.username,
						select(member.age.avg())
								.from(member))
				.from(member)
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}

	}

	@Test
	public void basicCase() {

		List<String> result = queryFactory
				.select(member.age
						// 나이가 10살이면 열살, 20살이면 스무살, 아니면 기타
						.when(10).then("열살")
						.when(20).then("스무살")
						.otherwise("기타"))
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	@Test
	public void complexCase() {

		/*
		select
			case
				when (m1_0.age between ? and ?) then cast(? as char)
				when (m1_0.age between ? and ?) then cast(? as char)
				else '기타'
			end
		from
			member m1_0
		 */
		List<String> result = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20살")
						.when(member.age.between(21, 30)).then("21~30살")
						.otherwise("기타")
				)
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	// 상수(Constant)가 필요할 때
	@Test
	public void constant() {

		// 상수 구하기
		List<Tuple> result = queryFactory
				.select(member.username, Expressions.constant("A"))
				.from(member)
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	// 문자 더하기 (concat)
	@Test
	public void concat() {

		// {username_age}
		/*
		select concat(concat(m1_0.username,'_'),cast(m1_0.age as char))
		from member m1_0 where m1_0
		 */
		List<String> result = queryFactory
				// enum 타입 같이 string인데 string을 인식 못할 때, stringValue()를 사용하면 유용하다.
				.select(member.username.concat("_").concat(member.age.stringValue()))
				.from(member)
				.where(member.username.eq("member1"))
				.fetch();
		// s = member1_10
		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

}
