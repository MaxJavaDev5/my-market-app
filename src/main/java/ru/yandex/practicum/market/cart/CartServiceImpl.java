package ru.yandex.practicum.market.cart;

import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;

	public CartServiceImpl(CartRepository cartRepository, ItemRepository itemRepository) {
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
	}

	@Override
	@Transactional
	public void updateCart(Long itemId, CartAction action)
	{
		CartItem cartItem = cartRepository.findByItemId(itemId);

		if (CartAction.PLUS == action) {
			if (cartItem == null) {
				cartItem = new CartItem();
				cartItem.setItemId(itemId);
				cartItem.setCount(1);
			} else {
				cartItem.setCount(cartItem.getCount() + 1);
			}
			cartRepository.save(cartItem);

		} else if (CartAction.MINUS == action) {
			if (cartItem != null) {
				if (cartItem.getCount() <= 1) {
					cartRepository.delete(cartItem);
				} else {
					cartItem.setCount(cartItem.getCount() - 1);
					cartRepository.save(cartItem);
				}
			}

		} else if (CartAction.DELETE == action) {
			if (cartItem != null) {
				cartRepository.delete(cartItem);
			}
		}
	}

	@Override
	public List<Item> getCartItems() {
		return cartRepository.findAll().stream()
				.map(ci -> {
					Item item = itemRepository.findById(ci.getItemId()).orElseThrow();
					item.setCount(ci.getCount());
					return item;
				})
				.collect(Collectors.toList());
	}

	@Override
	public long getTotalSum() {
		return getCartItems().stream()
				.mapToLong(item -> item.getPrice() * item.getCount())
				.sum();
	}
}
