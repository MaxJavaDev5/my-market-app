package ru.yandex.practicum.payment.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.ChargeResponse;

@Service
public class PaymentBalanceServiceImpl implements PaymentBalanceService
{

	private static final long INITIAL_BALANCE = 100_000L;

	private final Map<String, Long> userBalances = new HashMap<>();

	@Override
	public BalanceResponse getBalance(String accountId)
	{
		long balance = userBalances.computeIfAbsent(accountId, k -> INITIAL_BALANCE);
		return new BalanceResponse(balance);
	}

	@Override
	public ChargeResponse charge(String accountId, long amount)
	{
		long currentBalance = userBalances.computeIfAbsent(accountId, k -> INITIAL_BALANCE);

		if (amount <= 0)
		{
			return new ChargeResponse(false, "Amount must be positive").balance(currentBalance);
		}

		if (currentBalance < amount)
		{
			return new ChargeResponse(false, "Insufficient funds").balance(currentBalance);
		}

		long newBal = currentBalance - amount;
		userBalances.put(accountId, newBal);
		return new ChargeResponse(true, "Charge accepted").balance(newBal);
	}
}
