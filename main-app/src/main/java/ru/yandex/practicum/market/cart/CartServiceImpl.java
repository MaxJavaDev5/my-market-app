package ru.yandex.practicum.market.cart;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import ru.yandex.practicum.market.auth.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;

	public CartServiceImpl(CartRepository cartRepository, ItemRepository itemRepository, UserRepository userRepository)
	{
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Mono<Void> updateCart(Long itemId, CartAction action)
	{
		return getCurrentUserId()
				.flatMap(userId -> updateCartForUser(userId, itemId, action));
	}

	@Override
	public Flux<Item> getCartItems() {
		return getCurrentUserId()
				.flatMapMany(cartRepository::findAllByUserId)
				.flatMap(ci -> itemRepository.findById(ci.getItemId())
						.map(item ->
						{
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

	private Mono<Void> updateCartForUser(Long userId, Long itemId, CartAction action) {
		if (CartAction.PLUS == action) {
			return cartRepository.findByUserIdAndItemId(userId, itemId)
					.flatMap(cartItem -> {
						cartItem.setCount(cartItem.getCount() + 1);
						return cartRepository.save(cartItem);
					})
					.switchIfEmpty(Mono.defer(() -> {
						CartItem cartItem = new CartItem();
						cartItem.setUserId(userId);
						cartItem.setItemId(itemId);
						cartItem.setCount(1);
						return cartRepository.save(cartItem);
					}))
					.then();
		}
		if (CartAction.MINUS == action) {
			return cartRepository.findByUserIdAndItemId(userId, itemId)
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
			return cartRepository.findByUserIdAndItemId(userId, itemId)
					.flatMap(cartRepository::delete)
					.then();
		}
		return Mono.empty();
	}

	private Mono<Long> getCurrentUserId() {
		return ReactiveSecurityContextHolder
				.getContext()
				.map(securityContext -> securityContext.getAuthentication())
				.map(Authentication::getName)
				.flatMap(userRepository::findByUsername)
				.map(user -> user.getId())
				.defaultIfEmpty(1L);
	}
}
