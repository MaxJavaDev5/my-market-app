package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.cart.CartItem;
import ru.yandex.practicum.market.cart.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

	private final ItemRepository itemRepository;
	private final CartRepository cartRepository;

	public ItemServiceImpl(ItemRepository itemRepository, CartRepository cartRepository) {
		this.itemRepository = itemRepository;
		this.cartRepository = cartRepository;
	}

	@Override
	public List<Item> findAll(String search, String sort) {
		List<Item> items;
		if (search != null && !search.isBlank()) {
			items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
		} else {
			items = itemRepository.findAll();
		}
		if ("ALPHA".equals(sort)) {
			items.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
		} else if ("PRICE".equals(sort)) {
			items.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
		}
		items.forEach(item -> {
			CartItem cartItem = cartRepository.findByItemId(item.getId());
			item.setCount(cartItem != null ? cartItem.getCount() : 0);
		});
		return items;
	}

	@Override
	public Item findById(Long id) {
		Item item = itemRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Item not found: " + id));
		CartItem cartItem = cartRepository.findByItemId(id);
		item.setCount(cartItem != null ? cartItem.getCount() : 0);
		return item;
	}
}
