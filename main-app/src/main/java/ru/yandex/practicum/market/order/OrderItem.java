package ru.yandex.practicum.market.order;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.market.item.Item;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "order_items")
public class OrderItem {

	@Id
	private Long id;

	private Long orderId;

	private Long itemId;

	private int count;

	@Transient
	private Item item;

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
