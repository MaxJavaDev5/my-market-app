package ru.yandex.practicum.market.cart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping("/cart/items")
	public String getCart(Model model) {
		model.addAttribute("items", cartService.getCartItems());
		model.addAttribute("total", cartService.getTotalSum());
		return "cart";
	}

	@PostMapping("/cart/items")
	public String updateCart(
			@RequestParam Long id,
			@RequestParam CartAction action,
			Model model
	) {
		cartService.updateCart(id, action);
		model.addAttribute("items", cartService.getCartItems());
		model.addAttribute("total", cartService.getTotalSum());
		return "cart";
	}
}
