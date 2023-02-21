package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
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

	// 프로젝션 (Projection) : select 절에 어떤 걸 가져올 지 대상을 지정하는 행위
	@Test
	public void simpleProjection() {

		List<String> result = queryFactory
				.select(member.username)
				.from(member)
				.fetch();

		for (String answer : result) {
			System.out.println("answer = " + answer);
		}
	}

	// Tuple Projection
	@Test
	public void tupleProjection() {

		List<Tuple> result = queryFactory
				.select(member.username, member.age)
				.from(member)
				.fetch();
		/*
		username = member1, age = 10
		username = member2, age = 20
		username = member3, age = 30
		username = member4, age = 40
		 */
		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);

			System.out.println("username = " + username + ", age = " + age);
		}
		/*
		1. Tuple은 repository 계층까진 괜찮다만, service 계층이나 controller까지 넘어가는 건 좋지 않다.
		2. jdbc같은 걸 사용할 때, repository나 dao 계층에서 사용하도록 하지, 나머지에 의존해서는 안된다.
		 */
	}

	@Test
	public void findDtoByJPQL() {
		// 지저분함
		List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
						" from Member m", MemberDto.class)
				.getResultList();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// Querydsl 빈 생성
	@Test
	public void findDtoBySetter() {
		// 깔끔
		// Setter를 활용한 프로퍼티 접근 방법
		List<MemberDto> result = queryFactory
				// 주의: Instance 생성이 안되기에 기본 생성자를 추가해줘야 한다.
				.select(Projections.bean(MemberDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
		System.out.println();

		// 필드 접근 방법
		List<MemberDto> resultField = queryFactory
				// Getter, Setter가 없어도 됨
				.select(Projections.fields(MemberDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : resultField) {
			System.out.println("memberDto = " + memberDto);
		}

	}

	@Test
	public void findDtoByConstructor() {
		// 생성자 접근 방법
		List<MemberDto> result = queryFactory
				// Type을 맞춰야 함
				// 실제로 생성자를 호출함
				.select(Projections.constructor(MemberDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	public void findUserDto() {
		QMember memberSub = new QMember("memberSub");
		// name이 null로 들어감
		// alias을 활용하여 필드명을 맞춘다.
		List<UserDto> result = queryFactory
				// Type을 맞춰야 함
				// 실제로 생성자를 호출함
				.select(Projections.fields(UserDto.class,
						member.username.as("name"),
						// subQuery 같은 경우 ExpressionUtils를 사용할 수 밖에 없다.
						ExpressionUtils.as(
								JPAExpressions
								.select(memberSub.age.max())
								.from(memberSub), "age")
						)
				)
				.from(member)
				.fetch();

		for (UserDto userDto : result) {
			System.out.println("userDto = " + userDto);
		}
	}


	@Test
	public void findDtoByQueryProjection() {
		List<MemberDto> result = queryFactory
				// 실제로 생성자를 호출함
				.select(new QMemberDto(member.username, member.age))
//				.select(new QMemberDto(member.username, member.age, member.id)) // 는 안됨
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	/**	동적 쿼리
	 1. Boolean Builder
	 2. Where 다중 파라미터 사용 (실무에서 많이 사용)
	 select
		 m1_0.member_id,
		 m1_0.age,
		 m1_0.team_id,
		 m1_0.username
	 from
		 member m1_0
	 where
		 m1_0.username=?
		 and m1_0.age=?
 	 */
	@Test
	public void dynamic_query_BooleanBuilder() {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember1(usernameParam, ageParam);

		assertThat(result.size()).isEqualTo(1);


	}

	// 동적 쿼리 Boolean Builder 메소드
	private List<Member> searchMember1(String usernameCond, Integer ageCond) {

		// 기본 초기 조건도 넣을 수 있다.
		BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond));

		// 조건 예외처리 하고
		if (usernameCond != null) {
			builder.and(member.username.eq(usernameCond));
		}

		if (ageCond != null) {
			builder.and(member.age.eq(ageCond));
		}

		return queryFactory
				.selectFrom(member)
				// 이후 builder에서 또 조건을 추가할 수 있다.
				.where(
						builder
				)
				.fetch();
	}

	@Test
	public void dynamicQuery_WhereParam() {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember2(usernameParam, ageParam);

		assertThat(result.size()).isEqualTo(1);
	}

	// 동적 쿼리 Boolean Builder 메소드
	private List<Member> searchMember2(String usernameCond, Integer ageCond) {
		return queryFactory
				.selectFrom(member)
				// param이 null이면 where 조건에서 아예 무시해버린다.
				.where(usernameEq(usernameCond), ageEq(ageCond))
				// 이렇게 조립하여 합칠 수도 있다. (재활용성과 가독성이 높아짐)
//				.where(allEq(usernameCond, ageCond))
				.fetch();
	}

	private BooleanExpression usernameEq(String usernameCond) {
		// 삼항 연산자
		return usernameCond == null ? null : member.username.eq(usernameCond);
	}

	private BooleanExpression ageEq(Integer ageCond) {
		if (ageCond == null) return null;
		return member.age.eq(ageCond);
	}

	// 조립할려면 BooleanExpression 사용
	private BooleanExpression allEq(String usernameCond, Integer ageCond) {
		return usernameEq(usernameCond).and(ageEq(ageCond));
	}
}
