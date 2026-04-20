package ru.yandex.practicum.market.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.market.payment.client.ApiClient;
import ru.yandex.practicum.market.payment.client.api.DefaultApi;

@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentClientConfig {

	@Bean
	public ApiClient paymentApiClient(PaymentProperties paymentProperties) {
		return new ApiClient().setBasePath(paymentProperties.getBaseUrl());
	}

	@Bean
	public DefaultApi paymentsApi(ApiClient paymentApiClient) {
		return new DefaultApi(paymentApiClient);
	}
}
