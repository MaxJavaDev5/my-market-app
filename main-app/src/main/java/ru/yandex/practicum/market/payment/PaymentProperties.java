package ru.yandex.practicum.market.payment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

	private String baseUrl = "http://localhost:8081";
	private boolean enabled = true;
}
