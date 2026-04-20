package ru.yandex.practicum.market.order;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService)
	{
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	public Mono<Rendering> getOrders()
	{
		return orderService.findAll()
				.collectList()
				.map(orders -> Rendering.view("orders")
						.modelAttribute("orders", orders)
						.build());
	}

	@GetMapping("/orders/{id}")
	public Mono<Rendering> getOrder(
			@PathVariable Long id,
			@RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder
	)
	{
		return orderService.findById(id)
				.map(order -> Rendering.view("order")
						.modelAttribute("order", order)
						.modelAttribute("newOrder", newOrder)
						.build());
	}

	@PostMapping("/buy")
	public Mono<String> buy()
	{
		return orderService.createOrder()
				.map(order ->
						"redirect:/orders/" + order.getId() + "?newOrder=true")
				.onErrorResume(IllegalArgumentException.class, e ->
						Mono.just("redirect:/cart?paymentError=insufficient"))
				.onErrorResume(IllegalStateException.class, e ->
						Mono.just("redirect:/cart?paymentError=unavailable"));
	}
}
