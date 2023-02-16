package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;


import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
// 테스트에 기본적으로 Transactional이 존재하면 롤백된다.
@Transactional
@Commit
class QuerydslApplicationTests {

//	@Autowired
	@PersistenceContext
	EntityManager em;

	// 자동화된 테스트를 사용할 땐 메모리에서 돌려야한다. (테이블이 남으면 안되니)
	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);

		QHello qHello = new QHello("h");

		// query 와 관련된 거는 q타입을 넣는다.
		Hello result = query
				.selectFrom(qHello)
				.fetchOne();

		assertEquals(result, hello);
		// Lombok Test
		assertEquals(result.getId(), hello.getId());
	}

}
