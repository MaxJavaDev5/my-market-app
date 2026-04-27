package ru.yandex.practicum.market.cart;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("cart_items")
public class CartItem {

	@Id
	private Long id;

	private Long itemId;

	private int count;
}
