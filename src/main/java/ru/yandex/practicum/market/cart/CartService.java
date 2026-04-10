package ru.yandex.practicum.market.cart;

import ru.yandex.practicum.market.item.Item;

import java.util.List;

public interface CartService {

	void updateCart(Long itemId, String action);

	List<Item> getCartItems();

	long getTotalSum();
}
