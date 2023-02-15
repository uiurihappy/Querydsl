package study.querydsl.entity;
import lombok.*;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 본인 필드만!
@ToString(of = {"id", "name"})
public class Team {

	@Id @GeneratedValue
	@Column(name = "team_id")
	private Long id;

	private String name;

	// 양방향 연관관계이기에 연관관계 주인 설정
	@OneToMany(mappedBy = "team")
	private List<Member> members = new ArrayList<>();

	public Team(String name) {
		this.name = name;
	}
}
