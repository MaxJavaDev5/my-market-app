package ru.yandex.practicum.market.cart;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping("/cart/items")
	public Mono<Rendering> getCart() {
		return cartService.getCartItems()
				.collectList()
				.zipWith(cartService.getTotalSum())
				.map(tuple -> Rendering.view("cart")
						.modelAttribute("items", tuple.getT1())
						.modelAttribute("total", tuple.getT2())
						.build());
	}

	@PostMapping("/cart/items")
	public Mono<Rendering> updateCart(
			@ModelAttribute UpdateCartForm form
	) {
		return cartService.updateCart(form.getId(), form.getAction())
				.then(cartService.getCartItems().collectList()
						.zipWith(cartService.getTotalSum()))
				.map(tuple -> Rendering.view("cart")
						.modelAttribute("items", tuple.getT1())
						.modelAttribute("total", tuple.getT2())
						.build());
	}
}
