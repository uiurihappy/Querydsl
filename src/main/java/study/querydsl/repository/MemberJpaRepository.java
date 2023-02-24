package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.*;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
class MemberJpaRepository {
	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	public MemberJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
		this.em = em;
		// Spring Bean 에 등록하여 주입받아 사용가능하다.
		this.queryFactory = queryFactory;
	}

	// Member save
	public void save(Member member) {
		em.persist(member);
	}

	// select Member By id
	public Optional<Member> findById(Long id) {
		Member findMember = em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}

	// select Member list (JPA)
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}

	// select Member list (Querydsl)
	public List<Member> findAll_Querydsl() {
		return queryFactory
				.selectFrom(member)
				.fetch();
	}


	// select Member By username (JPA)
	public List<Member> findByUsername(String username) {
		return em.createQuery(
				"select m from Member m " +
						"where m.username = :username", Member.class)
				.setParameter("username", username)
				.getResultList();
	}

	//  select Member By username (Querydsl)
	public List<Member> findByUsername_Querydsl(String username) {
		return queryFactory
				.selectFrom(member)
				.where(member.username.eq(username))
				.fetch();
	}

	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

		// Builder
		BooleanBuilder builder = new BooleanBuilder();
		if (hasText(condition.getUsername())) {
			builder.and(member.username.eq(condition.getUsername()));
		}

		if (hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}

		if (condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}

		if (condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}

		return queryFactory
				.select(new QMemberTeamDto(
						member.id.as("memberId"),
						member.username,
						member.age,
						team.id.as("teamId"),
						team.name
				))
				.from(member)
				.leftJoin(member.team, team)
				.where(builder)
				.fetch();

	}

}
