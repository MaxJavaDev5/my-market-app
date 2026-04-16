package ru.yandex.practicum.market.item;

import java.util.List;

public interface ItemService {

	List<Item> findAll(String search, String sort);

	Item findById(Long id);
}
