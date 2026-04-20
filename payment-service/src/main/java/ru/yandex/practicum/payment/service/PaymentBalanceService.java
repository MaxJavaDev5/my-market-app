package ru.yandex.practicum.payment.service;

import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.ChargeResponse;

public interface PaymentBalanceService {

	BalanceResponse getBalance();

	ChargeResponse charge(long amount);
}
