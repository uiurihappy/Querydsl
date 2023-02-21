package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

	private String username;
	private int age;

	// querydsl 에 너무 의존적이며, 사용할 거면 하부기술에 영향이 가지 않는 선에서 가능
	@QueryProjection
	public MemberDto(String username, int age) {
		this.username = username;
		this.age = age;
	}
}
