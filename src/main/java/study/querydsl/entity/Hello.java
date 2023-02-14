package study.querydsl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Hello {

	/*
	해당 프로젝트 경로 cli에서
	./gradlew clean : 빌드 파일 clean
	./gradlew compileQuerydsl : querydsl 컴파일
	 */
	@Id @GeneratedValue
	private Long id;
}
