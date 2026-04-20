package ru.yandex.practicum.market.cart;

import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;

	public CartServiceImpl(CartRepository cartRepository, ItemRepository itemRepository) {
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
	}

	@Override
	public Mono<Void> updateCart(Long itemId, CartAction action) {
		if (CartAction.PLUS == action) {
			return cartRepository.findByItemId(itemId)
					.flatMap(cartItem -> {
						cartItem.setCount(cartItem.getCount() + 1);
						return cartRepository.save(cartItem);
					})
					.switchIfEmpty(Mono.defer(() -> {
						CartItem cartItem = new CartItem();
						cartItem.setItemId(itemId);
						cartItem.setCount(1);
						return cartRepository.save(cartItem);
					}))
					.then();
		}
		if (CartAction.MINUS == action) {
			return cartRepository.findByItemId(itemId)
					.flatMap(cartItem -> {
						if (cartItem.getCount() <= 1) {
							return cartRepository.delete(cartItem);
						}
						cartItem.setCount(cartItem.getCount() - 1);
						return cartRepository.save(cartItem).then();
					})
					.then();
		}
		if (CartAction.DELETE == action) {
			return cartRepository.findByItemId(itemId)
					.flatMap(cartRepository::delete)
					.then();
		}
		return Mono.<Void>empty();
	}

	@Override
	public Flux<Item> getCartItems() {
		return cartRepository.findAll()
				.flatMap(ci -> itemRepository.findById(ci.getItemId())
						.map(item -> {
							item.setCount(ci.getCount());
							return item;
						}));
	}

	@Override
	public Mono<Long> getTotalSum() {
		return getCartItems()
				.map(item -> item.getPrice() * item.getCount())
				.reduce(0L, Long::sum);
	}
}
