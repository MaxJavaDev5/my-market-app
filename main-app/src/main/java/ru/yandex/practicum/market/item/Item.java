package ru.yandex.practicum.market.item;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "items")
public class Item {

	@Id
	private Long id;

	private String title;

	private String description;

	private String imgPath;

	private long price;

	@Transient
	private int count;

	public Long id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public String imgPath() {
		return imgPath;
	}

	public long price() {
		return price;
	}

	public int count() {
		return count;
	}
}
