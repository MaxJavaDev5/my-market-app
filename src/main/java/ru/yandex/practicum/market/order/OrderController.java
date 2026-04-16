package ru.yandex.practicum.market.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	public String getOrders(Model model) {
		model.addAttribute("orders", orderService.findAll());
		return "orders";
	}

	@GetMapping("/orders/{id}")
	public String getOrder(
			@PathVariable Long id,
			@RequestParam(defaultValue = "false") boolean newOrder,
			Model model
	) {
		Order order = orderService.findById(id);
		long total = order.getItems().stream()
				.mapToLong(orderItem -> orderItem.getItem().getPrice() * orderItem.getCount()).sum();
		order.setTotalSum(total);
		model.addAttribute("order", order);
		model.addAttribute("newOrder", newOrder);
		return "order";
	}

	@PostMapping("/buy")
	public String buy() {
		Order order = orderService.createOrder();
		return "redirect:/orders/" + order.getId() + "?newOrder=true";
	}
}
