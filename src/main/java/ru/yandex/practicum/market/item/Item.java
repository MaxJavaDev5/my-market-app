package ru.yandex.practicum.market.item;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {this.description = description;}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

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
