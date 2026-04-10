package ru.yandex.practicum.market.order;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items = new ArrayList<>();

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
