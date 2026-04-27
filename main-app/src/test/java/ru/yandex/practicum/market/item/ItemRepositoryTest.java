package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.order.OrderItemRepository;
import ru.yandex.practicum.market.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemRepositoryTest {

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	CartRepository cartRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	@BeforeEach
	void cleanDb() {
		orderItemRepository.deleteAll().block();
		orderRepository.deleteAll().block();
		cartRepository.deleteAll().block();
		itemRepository.deleteAll().block();
	}

	@Test
	void findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase_findsByTitle() {
		saveItem("Product_Repository", 1500L, "Product_Description");

		List<Item> found = itemRepository
				.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Repository", "Repository")
				.collectList()
				.block();

		assertThat(found).hasSize(1);
		assertThat(found.get(0).getTitle()).isEqualTo("Product_Repository");
	}

	@Test
	void findByTitleContaining_findsByDescriptionWhenTitleDoesNotMatch() {
		saveItem("Product_TitleOnly", 100L, "Product_UniqueSearchToken_In_Description");

		List<Item> found = itemRepository
				.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("UniqueSearchToken", "UniqueSearchToken")
				.collectList()
				.block();

		assertThat(found).extracting(Item::getTitle).containsExactly("Product_TitleOnly");
	}

	@Test
	void findByTitleContaining_noMatches_returnsEmptyList() {
		saveItem("Product_NoMatch", 1L, "Product_Desc");

		List<Item> found = itemRepository
				.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("NonExistent", "NonExistent")
				.collectList()
				.block();

		assertThat(found).isEmpty();
	}

	@Test
	void findByTitleContaining_multipleItems_matchTitleOrDescription() {
		saveItem("Product_HasNeedleInTitle", 10L, null);
		saveItem("Product_OtherTitle", 20L, "Product_Needle_In_Description");

		List<Item> found = itemRepository
				.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Needle", "Needle")
				.collectList()
				.block();

		assertThat(found).extracting(Item::getTitle)
				.containsExactlyInAnyOrder("Product_HasNeedleInTitle", "Product_OtherTitle");
	}

	@Test
	void saveAndFindById_returnsSameProduct() {
		Item saved = saveItem("Product_RT", 500L, "Product_RT_Desc");

		var found = itemRepository.findById(saved.getId()).blockOptional();

		assertThat(found).isPresent();
		assertThat(found.get().getTitle()).isEqualTo("Product_RT");
		assertThat(found.get().getPrice()).isEqualTo(500L);
		assertThat(found.get().getDescription()).isEqualTo("Product_RT_Desc");
	}

	@Test
	void findAll_returnsAllSavedProducts() {
		saveItem("Product_All_One", 1L, null);
		saveItem("Product_All_Two", 2L, null);

		List<Item> all = itemRepository.findAll().collectList().block();

		assertThat(all).hasSize(2);
		assertThat(all).extracting(Item::getTitle).containsExactlyInAnyOrder("Product_All_One", "Product_All_Two");
	}

	private Item saveItem(String title, long price, String description) {
		Item item = new Item();
		item.setTitle(title);
		item.setPrice(price);
		item.setDescription(description);
		return itemRepository.save(item).block();
	}
}
