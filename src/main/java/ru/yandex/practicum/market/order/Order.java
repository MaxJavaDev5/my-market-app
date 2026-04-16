package ru.yandex.practicum.market.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Table(name = "orders")
public class Order {

	@Id
	private Long id;

	@Transient
	private List<OrderItem> items = new ArrayList<>();

	@Transient
	private long totalSum;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public void setTotalSum(long totalSum) {
		this.totalSum = totalSum;
	}

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
