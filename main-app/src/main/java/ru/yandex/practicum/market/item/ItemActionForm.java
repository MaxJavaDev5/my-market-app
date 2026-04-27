package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.cart.CartAction;

public class ItemActionForm
{
	private CartAction action;

	public CartAction getAction()
	{
		return action;
	}

	public void setAction(CartAction action)
	{
		this.action = action;
	}
}
