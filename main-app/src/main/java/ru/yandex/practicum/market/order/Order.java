package ru.yandex.practicum.market.order;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "orders")
public class Order {

	@Id
	private Long id;

	@Transient
	private List<OrderItem> items = new ArrayList<>();

	@Transient
	private long totalSum;

	public Long id() {
		return id;
	}

	public List<OrderItem> items() {
		return items;
	}

	public long totalSum() {
		return totalSum;
	}
}
