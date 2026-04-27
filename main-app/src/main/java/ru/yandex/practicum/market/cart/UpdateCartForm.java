package ru.yandex.practicum.market.cart;

public class UpdateCartForm
{
	private Long id;
	private CartAction action;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public CartAction getAction()
	{
		return action;
	}

	public void setAction(CartAction action)
	{
		this.action = action;
	}
}
