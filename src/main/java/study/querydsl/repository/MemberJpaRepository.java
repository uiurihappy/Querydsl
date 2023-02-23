package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

import java.util.*;

@Repository
class MemberJpaRepository {
	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	public MemberJpaRepository(EntityManager em) {
		this.em = em;
		this.queryFactory = new JPAQueryFactory(em);
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

	// select Member list
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}

	public List<Member> findByUsername(String username) {
		return em.createQuery(
				"select m from Member m " +
						"where m.username = :username", Member.class)
				.setParameter("username", username)
				.getResultList();
	}

}
