package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;


import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

//	@Autowired
	@PersistenceContext
	EntityManager em;

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
