package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.item.Item;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_items")
public class OrderItem {

	@Id
	private Long id;

	private Long orderId;

	private Long itemId;

	private int count;

	@Transient
	private Item item;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
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
