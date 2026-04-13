package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.Paging;
import ru.yandex.practicum.market.cart.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	public String getItems(
			@RequestParam String search,
			@RequestParam(defaultValue = "NO") String sort,
			@RequestParam(defaultValue = "1") int pageNumber,
			@RequestParam(defaultValue = "5") int pageSize,
			Model model
	) {
		List<Item> allItems = itemService.findAll(search, sort);

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

		model.addAttribute("items", rows);
		model.addAttribute("search", search);
		model.addAttribute("sort", sort);

		Paging paging = new Paging(pageSize, pageNumber, pageNumber > 1, toIndex < total);
		model.addAttribute("paging", paging);

		return "items";
	}

	@PostMapping("/items")
	public String updateCartFromItems(
			@RequestParam Long id,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "NO") String sort,
			@RequestParam(defaultValue = "1") int pageNumber,
			@RequestParam(defaultValue = "5") int pageSize,
			@RequestParam String action
	) {
		cartService.updateCart(id, action);
		return "redirect:/items?search=" + (search != null ? search : "")
				+ "&sort=" + sort
				+ "&pageNumber=" + pageNumber
				+ "&pageSize=" + pageSize;
	}

	@GetMapping("/items/{id}")
	public String getItem(@PathVariable Long id, Model model) {
		model.addAttribute("item", itemService.findById(id));
		return "item";
	}

	@PostMapping("/items/{id}")
	public String updateCartFromItem(
			@PathVariable Long id,
			@RequestParam String action,
			Model model
	) {
		cartService.updateCart(id, action);
		model.addAttribute("item", itemService.findById(id));
		return "item";
	}
}
