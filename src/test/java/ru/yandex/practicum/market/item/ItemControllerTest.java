package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.cart.CartAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	ItemService itemService;

	@MockBean
	CartService cartService;

	@Test
	void getItems_returnsItemsView() throws Exception {
		when(itemService.findAll(null, "NO")).thenReturn(List.of());

		mockMvc.perform(get("/items"))
				.andExpect(status().isOk())
				.andExpect(view().name("items"));
	}

	@Test
	void getRoot_returnsItemsView() throws Exception {
		when(itemService.findAll(null, "NO")).thenReturn(List.of());

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("items"));

		verify(itemService).findAll(null, "NO");
	}

	@Test
	void getItems_passesSearchAndSortToService() throws Exception {
		when(itemService.findAll("Product_Query", "PRICE")).thenReturn(List.of());

		mockMvc.perform(get("/items")
						.param("search", "Product_Query")
						.param("sort", "PRICE"))
				.andExpect(status().isOk())
				.andExpect(view().name("items"));

		verify(itemService).findAll("Product_Query", "PRICE");
	}

	@Test
	void getItem_returnsItemView() throws Exception {
		Item item = new Item();
		item.setId(1L);
		item.setTitle("Product");
		item.setPrice(500L);
		when(itemService.findById(1L)).thenReturn(item);

		mockMvc.perform(get("/items/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("item"));
	}

	@Test
	void postItems_redirectWhenSearchMissing_usesEmptySearchInUrl() throws Exception {
		mockMvc.perform(post("/items")
						.param("id", "1")
						.param("action", "MINUS")
						.param("sort", "NO")
						.param("pageNumber", "1")
						.param("pageSize", "5"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

		verify(cartService).updateCart(1L, CartAction.MINUS);
	}

	@Test
	void postItemById_updatesCartAndReturnsItemView() throws Exception {
		Item item = new Item();
		item.setId(7L);
		item.setTitle("Product_Seven");
		item.setPrice(100L);
		when(itemService.findById(7L)).thenReturn(item);

		mockMvc.perform(post("/items/7").param("action", "PLUS"))
				.andExpect(status().isOk())
				.andExpect(view().name("item"));

		verify(cartService).updateCart(7L, CartAction.PLUS);
		verify(itemService).findById(7L);
	}
}
