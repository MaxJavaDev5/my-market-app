package ru.yandex.practicum.market.item;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import ru.yandex.practicum.market.cart.CartItem;
import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.order.OrderItemRepository;
import ru.yandex.practicum.market.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ItemServiceTest {

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	CartRepository cartRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	@Autowired
	ItemService itemService;

	@BeforeEach
	void cleanDb() {
		orderItemRepository.deleteAll().block();
		orderRepository.deleteAll().block();
		cartRepository.deleteAll().block();
		itemRepository.deleteAll().block();
	}

	@Test
	void findById_returnsSavedItem() {
		Item saved = new Item();
		saved.setTitle("Product");
		saved.setPrice(10000L);
		saved = itemRepository.save(saved).block();

		Item found = itemService.findById(saved.getId()).block();
		assertNotNull(found);
		assertThat(found.getTitle()).isEqualTo("Product");
	}

	@Test
	void findById_notFound_throws() {
		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> itemService.findById(999_999L).block());
		assertThat(ex.getMessage()).contains("Item not found");
	}

	@Test
	void findById_whenCartEmpty_countIsZero() {
		Item saved = itemRepository.save(item("Product_One", 100L, null)).block();

		Item found = itemService.findById(saved.getId()).block();

		assertThat(found.getCount()).isZero();
	}

	@Test
	void findById_whenInCart_returnsCartCount() {
		Item saved = itemRepository.save(item("Product_Two", 200L, null)).block();
		CartItem cartItem = new CartItem();
		cartItem.setItemId(saved.getId());
		cartItem.setCount(4);
		cartRepository.save(cartItem).block();

		Item found = itemService.findById(saved.getId()).block();

		assertThat(found.getCount()).isEqualTo(4);
	}

	@Test
	void findAll_nullSearch_returnsAllItems() {
		itemRepository.save(item("Product_Three", 1L, null)).block();
		itemRepository.save(item("Product_Four", 2L, null)).block();

		List<Item> items = itemService.findAll(null, "NO").block();

		assertThat(items).hasSize(2);
		assertThat(items.stream().map(Item::getTitle)).containsExactlyInAnyOrder("Product_Three", "Product_Four");
	}

	@Test
	void findAll_search_matchesTitleCaseInsensitive() {
		itemRepository.save(item("Product_For_Search", 100L, "Product_Description")).block();
		itemRepository.save(item("Product_For_Delete", 200L, null)).block();

		List<Item> items = itemService.findAll("search", "NO").block();

		assertThat(items).extracting(Item::getTitle).containsExactly("Product_For_Search");
	}

	@Test
	void findAll_search_matchesDescription() {
		itemRepository.save(item("Product_Title", 100L, "Product_Unique_Description_Token")).block();

		List<Item> items = itemService.findAll("Unique_Description", "NO").block();

		assertThat(items).hasSize(1);
		assertThat(items.get(0).getTitle()).isEqualTo("Product_Title");
	}

	@Test
	void findAll_sortAlpha_ordersByTitleIgnoreCase() {
		itemRepository.save(item("Product_Charlie", 1L, null)).block();
		itemRepository.save(item("Product_alpha", 2L, null)).block();
		itemRepository.save(item("Product_Bravo", 3L, null)).block();

		List<Item> items = itemService.findAll(null, "ALPHA").block();

		assertThat(items.stream().map(Item::getTitle).toList())
				.containsExactly("Product_alpha", "Product_Bravo", "Product_Charlie");
	}

	@Test
	void findAll_sortPrice_ordersByPriceAscending() {
		itemRepository.save(item("Product_Price_Expensive", 300L, null)).block();
		itemRepository.save(item("Product_Price_Cheap", 50L, null)).block();
		itemRepository.save(item("Product_Price_Mid", 150L, null)).block();

		List<Item> items = itemService.findAll(null, "PRICE").block();

		assertThat(items.stream().mapToLong(Item::getPrice).toArray())
				.containsExactly(50L, 150L, 300L);
	}

	private static Item item(String title, long price, String description) {
		Item item = new Item();
		item.setTitle(title);
		item.setPrice(price);
		item.setDescription(description);
		return item;
	}
}
