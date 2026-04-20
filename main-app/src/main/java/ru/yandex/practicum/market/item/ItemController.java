package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.Paging;
import ru.yandex.practicum.market.cart.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ItemController {

	private final ItemService itemService;
	private final CartService cartService;

	public ItemController(ItemService itemService, CartService cartService)
	{
		this.itemService = itemService;
		this.cartService = cartService;
	}

	@GetMapping({"/", "/items"})
	public Mono<Rendering> getItems(
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "sort", defaultValue = "NO") String sort,
			@RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "5") int pageSize
	) {
		return itemService.findAll(search, sort)
				.map(allItems ->
				{
					int total = allItems.size();
					int fromIndex = Math.min((pageNumber - 1) * pageSize, total);
					int toIndex = Math.min(fromIndex + pageSize, total);
					List<Item> pageItems = allItems.subList(fromIndex, toIndex);

					List<List<Item>> rows = new ArrayList<>();
					for (int i = 0; i < pageItems.size(); i += 3) {
						List<Item> row = new ArrayList<>(pageItems.subList(i, Math.min(i + 3, pageItems.size())));
						while (row.size() < 3) {
							Item stub = new Item();
							stub.setId(-1L);
							row.add(stub);
						}
						rows.add(row);
					}

					Paging paging = new Paging(pageSize, pageNumber, pageNumber > 1, toIndex < total);
					return Rendering.view("items")
							.modelAttribute("items", rows)
							.modelAttribute("search", search)
							.modelAttribute("sort", sort)
							.modelAttribute("paging", paging)
							.build();
				});
	}

	@PostMapping("/items")
	public Mono<String> updateCartFromItems(
			@ModelAttribute ItemsUpdateForm form
	)
	{
		String sort = form.getSort() != null ? form.getSort() : "NO";

		int pageNumber = form.getPageNumber() != null ? form.getPageNumber() : 1;
		int pageSize = form.getPageSize() != null ? form.getPageSize() : 5;

		return cartService.updateCart(form.getId(), form.getAction())
				.then(Mono.fromCallable(() -> "redirect:" + itemsQuery(form.getSearch(), sort, pageNumber, pageSize)));
	}

	private static String itemsQuery(String search, String sort, int pageNumber, int pageSize) {
		return UriComponentsBuilder.fromPath("/items")
				.queryParam("search", search != null ? search : "")
				.queryParam("sort", sort)
				.queryParam("pageNumber", pageNumber)
				.queryParam("pageSize", pageSize)
				.encode()
				.build()
				.toUriString();
	}

	@GetMapping("/items/{id}")
	public Mono<Rendering> getItem(@PathVariable Long id) {
		return itemService.findById(id)
				.map(item -> Rendering.view("item")
						.modelAttribute("item", item)
						.build());
	}

	@PostMapping("/items/{id}")
	public Mono<Rendering> updateCartFromItem(
			@PathVariable Long id,
			@ModelAttribute ItemActionForm form
	) {
		return cartService.updateCart(id, form.getAction())
				.then(itemService.findById(id))
				.map(item -> Rendering.view("item")
						.modelAttribute("item", item)
						.build());
	}
}
