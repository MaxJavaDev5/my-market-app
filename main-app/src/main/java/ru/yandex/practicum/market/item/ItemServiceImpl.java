package ru.yandex.practicum.market.item;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import ru.yandex.practicum.market.auth.UserRepository;
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
	private final ItemCacheService itemCacheService;
	private final UserRepository userRepository;

	public ItemServiceImpl(
			ItemRepository itemRepository,
			CartRepository cartRepository,
			ItemCacheService itemCacheService,
			UserRepository userRepository)
	{
		this.itemRepository = itemRepository;
		this.cartRepository = cartRepository;
		this.itemCacheService = itemCacheService;
		this.userRepository = userRepository;
	}

	@Override
	public Mono<List<Item>> findAll(String search, String sort)
	{
		return itemCacheService.getItemList(search, sort)
				.switchIfEmpty(loadItemsFromDbAndCache(search, sort))
				.flatMap(this::applyCartCounts);
	}

	@Override
	public Mono<Item> findById(Long id)
	{
		return itemCacheService.getItem(id)
				.switchIfEmpty(loadItemFromDbAndCache(id))
				.switchIfEmpty(Mono.error(new RuntimeException("Item not found: " + id)))
				.flatMap(this::withCartCount);
	}

	private Mono<List<Item>> loadItemsFromDbAndCache(String search, String sort)
	{
		return loadItemsFromDb(search, sort)
				.flatMap(items ->
						itemCacheService.putItemList(search, sort, items).thenReturn(items));
	}

	private Mono<Item> loadItemFromDbAndCache(Long id)
	{
		return itemRepository.findById(id)
				.flatMap(item ->
						itemCacheService.putItem(item).thenReturn(item));
	}

	private Mono<List<Item>> applyCartCounts(List<Item> items)
	{
		return Flux.fromIterable(items)
				.concatMap(this::withCartCount)
				.collectList();
	}

	private Mono<List<Item>> loadItemsFromDb(String search, String sort)
	{
		Flux<Item> flux = (search != null && !search.isBlank())
				? itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search)
				: itemRepository.findAll();

		return flux.collectList()
				.map(list -> {
					sortInPlace(list, sort);
					return list;
				});
	}

	private void sortInPlace(List<Item> items, String sort)
	{
		if ("ALPHA".equals(sort))
		{
			items.sort(Comparator.comparing(Item::getTitle, String.CASE_INSENSITIVE_ORDER));
		}
		else if ("PRICE".equals(sort))
		{
			items.sort(Comparator.comparingLong(Item::getPrice));
		}
	}

	private Mono<Item> withCartCount(Item item)
	{
		return getCurrentUserId()
				.flatMap(userId -> cartRepository.findByUserIdAndItemId(userId, item.getId()))
				.switchIfEmpty(cartRepository.findByItemId(item.getId()))
				.map(ci -> ci.getCount())
				.defaultIfEmpty(0)
				.map(count -> {
					item.setCount(count);
					return item;
				});
	}

	private Mono<Long> getCurrentUserId()
	{
		return ReactiveSecurityContextHolder.getContext()
				.filter(context -> context.getAuthentication() != null && context.getAuthentication().isAuthenticated())
				.map(context -> context.getAuthentication().getName())
				.flatMap(userRepository::findByUsername)
				.map(user -> user.getId());
	}
}
