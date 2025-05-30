package org.knock.knock_back.dto.document.category;

import java.util.List;
import java.util.Objects;

import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(indexName = "category-level-two-index")
public class CATEGORY_LEVEL_TWO_INDEX {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("_id") //
	private String id;                                                  // 장르 ID

	@Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
	@Column(nullable = false)
	private String nm;                                                  // 장르 이름 (EX; 공포, 미스테리, 뮤지컬 등

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
	private CategoryLevelOne parentNm;                                  // 상위 장르 이름 (EX; MOVIE, PERFORMING_ARTS 등)

	@ManyToMany
	private List<String> favoriteUsers;                             // 해당 장르 선호 인원

	@Builder
	@PersistenceCreator
	public CATEGORY_LEVEL_TWO_INDEX(String id, String nm, CategoryLevelOne parentNm, List<String> favoriteUsers) {
		this.id = id;
		this.nm = nm;
		this.parentNm = parentNm;
		this.favoriteUsers = favoriteUsers;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CATEGORY_LEVEL_TWO_INDEX that))
			return false;
		return Objects.equals(id, that.id) && Objects.equals(nm, that.nm);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, nm);
	}
}
