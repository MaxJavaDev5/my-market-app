package ru.yandex.practicum.market.item;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 128)
	private String title;

	@Column(length = 2048)
	private String description;

	@Column(name = "img_path")
	private String imgPath;

	@Column(nullable = false)
	private long price;

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
