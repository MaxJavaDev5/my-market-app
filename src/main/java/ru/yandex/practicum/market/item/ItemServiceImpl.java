package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.cart.CartRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
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
	public Mono<List<Item>> findAll(String search, String sort) {
		Flux<Item> flux = (search != null && !search.isBlank())
				? itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search)
				: itemRepository.findAll();
		return flux.collectList()
				.map(list -> {
					sortInPlace(list, sort);
					return list;
				})
				.flatMapMany(Flux::fromIterable)
				.concatMap(this::withCartCount)
				.collectList();
	}

	@Override
	public Mono<Item> findById(Long id) {
		return itemRepository.findById(id)
				.switchIfEmpty(Mono.error(new RuntimeException("Item not found: " + id)))
				.flatMap(this::withCartCount);
	}

	private void sortInPlace(List<Item> items, String sort) {
		if ("ALPHA".equals(sort)) {
			items.sort(Comparator.comparing(Item::getTitle, String.CASE_INSENSITIVE_ORDER));
		} else if ("PRICE".equals(sort)) {
			items.sort(Comparator.comparingLong(Item::getPrice));
		}
	}

	private Mono<Item> withCartCount(Item item) {
		return cartRepository.findByItemId(item.getId())
				.map(ci -> {
					item.setCount(ci.getCount());
					return item;
				})
				.switchIfEmpty(Mono.fromCallable(() -> {
					item.setCount(0);
					return item;
				}));
	}
}
