package ru.yandex.practicum.payment.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.ChargeResponse;

@Service
public class PaymentBalanceServiceImpl implements PaymentBalanceService 
{

	private long balance = 100_000L;

	@Override
	public synchronized BalanceResponse getBalance() 
	{
		return new BalanceResponse(balance);
	}

	@Override
	public synchronized ChargeResponse charge(long amount) {
		if (amount <= 0) 
		{
			return new ChargeResponse(false, "Amount must be positive")
					.balance(balance);
		}

		if (balance < amount) 
		{
			return new ChargeResponse(false, "Insufficient funds")
					.balance(balance);
		}

		balance = balance - amount;
		return new ChargeResponse(true, "Charge accepted")
				.balance(balance);
	}
}
