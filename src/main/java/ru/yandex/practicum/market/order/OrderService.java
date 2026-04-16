package ru.yandex.practicum.market.order;

import java.util.List;

public interface OrderService {

	Order createOrder();

	Order findById(Long id);

	List<Order> findAll();
}
