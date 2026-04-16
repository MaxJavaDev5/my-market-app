package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.item.Item;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@Column(nullable = false)
	private int count;

	public void setOrder(Order order) {
		this.order = order;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String title() {
		return item.getTitle();
	}

	public long price() {
		return item.getPrice();
	}

	public int count() {
		return count;
	}
}
