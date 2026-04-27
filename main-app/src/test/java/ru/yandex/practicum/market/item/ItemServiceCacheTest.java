package ru.yandex.practicum.market.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.cart.CartRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemServiceCacheTest
{
	private ItemRepository itemRepository;
	private CartRepository cartRepository;
	private ItemCacheService itemCacheService;
	private ItemServiceImpl itemService;

	@BeforeEach
	void setUp()
	{
		itemRepository = Mockito.mock(ItemRepository.class);
		cartRepository = Mockito.mock(CartRepository.class);
		itemCacheService = Mockito.mock(ItemCacheService.class);
		itemService = new ItemServiceImpl(itemRepository, cartRepository, itemCacheService);
	}

	@Test
	void findAll_cacheMiss_readsDbAndStoresCache()
	{
		Item item = item(1L, "A", 10L);

		when(itemCacheService.getItemList(null, "NO")).thenReturn(Mono.empty());
		when(itemRepository.findAll()).thenReturn(Flux.just(item));
		when(itemCacheService.putItemList(eq(null), eq("NO"), any())).thenReturn(Mono.empty());
		when(cartRepository.findByItemId(1L)).thenReturn(Mono.empty());

		StepVerifier.create(itemService.findAll(null, "NO"))
				.expectNextMatches(items -> items.size() == 1 && items.get(0).getId().equals(1L))
				.verifyComplete();

		verify(itemRepository).findAll();
		verify(itemCacheService).putItemList(eq(null), eq("NO"), any());
	}

	@Test
	void findAll_cacheHit_skipsDb()
	{
		Item cached = item(2L, "Cached", 20L);

		when(itemCacheService.getItemList(null, "NO")).thenReturn(Mono.just(List.of(cached)));
		when(itemRepository.findAll()).thenReturn(Flux.empty());
		when(cartRepository.findByItemId(2L)).thenReturn(Mono.empty());

		StepVerifier.create(itemService.findAll(null, "NO"))
				.expectNextCount(1)
				.verifyComplete();

		verify(itemCacheService).getItemList(null, "NO");
	}

	@Test
	void findById_cacheHit_skipsDb()
	{
		Item cached = item(3L, "Card", 30L);

		when(itemCacheService.getItem(3L)).thenReturn(Mono.just(cached));
		when(itemRepository.findById(3L)).thenReturn(Mono.empty());
		when(cartRepository.findByItemId(3L)).thenReturn(Mono.empty());

		StepVerifier.create(itemService.findById(3L))
				.expectNextMatches(item -> item.getId().equals(3L))
				.verifyComplete();

		verify(itemCacheService).getItem(3L);
	}

	private Item item(Long id, String title, long price) {
		Item item = new Item();
		item.setId(id);
		item.setTitle(title);
		item.setPrice(price);
		return item;
	}
}
