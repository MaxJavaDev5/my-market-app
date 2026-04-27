package ru.yandex.practicum.market.cart;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.payment.PaymentGateway;

@Controller
public class CartController {

	private final CartService cartService;
	private final PaymentGateway paymentGateway;

	public CartController(CartService cartService, PaymentGateway paymentGateway)
	{
		this.cartService = cartService;
		this.paymentGateway = paymentGateway;
	}

	@GetMapping("/cart/items")
	public Mono<Rendering> getCart(@RequestParam(name = "paymentError", required = false) String paymentError)
	{
		return cartService.getCartItems().collectList()
				.zipWith(cartService.getTotalSum(), CartViewData::new)
				.flatMap(cart -> resolvePurchaseState(cart.total()).map(st ->
							Rendering.view("cart")
									.modelAttribute("items", cart.items())
									.modelAttribute("total", cart.total())
									.modelAttribute("canBuy", st.canBuy())
									.modelAttribute("paymentAvailable", st.paymentAvailable())
									.modelAttribute("paymentError", paymentError)
									.build()));
	}

	private Mono<PurchaseState> resolvePurchaseState(Long total)
	{
		if (total == null || total <= 0)
		{
			return Mono.just(new PurchaseState(false, true));
		}

		return paymentGateway.getBalance()
				.map(bal -> new PurchaseState(bal >= total, true))
				.onErrorResume(err -> Mono.just(new PurchaseState(false, false)));
	}



	@PostMapping("/cart/items")
	public Mono<Rendering> updateCart(@ModelAttribute UpdateCartForm form)
	{
		return cartService.updateCart(form.getId(), form.getAction())
				.then(cartService.getCartItems().collectList()
						.zipWith(cartService.getTotalSum(), CartViewData::new)
						.flatMap(cart -> resolvePurchaseState(cart.total()).map(st -> Rendering.view("cart")
									.modelAttribute("items", cart.items())
									.modelAttribute("total", cart.total())
									.modelAttribute("canBuy", st.canBuy())
									.modelAttribute("paymentAvailable", st.paymentAvailable())
									.modelAttribute("paymentError", null)
									.build())));
	}

	private record PurchaseState(boolean canBuy, boolean paymentAvailable) { }

	private record CartViewData(List<Item> items, Long total) { }
}
