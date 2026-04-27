package ru.yandex.practicum.market.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import ru.yandex.practicum.market.payment.client.ApiClient;
import ru.yandex.practicum.market.payment.client.api.DefaultApi;

@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentClientConfig {

	private static final String PAYMENT_CLIENT_REGISTRATION_ID = "keycloak";

	@Bean
	public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(
			ReactiveClientRegistrationRepository clientRegistrationRepository)
	{
		var authorizedClientService =
				new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);

		var authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
				.clientCredentials()
				.build();

		var authorizedClientManager =
				new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
						clientRegistrationRepository,
						authorizedClientService);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
		return authorizedClientManager;
	}

	@Bean
	public WebClient paymentWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
				new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		oauthFilter.setDefaultClientRegistrationId(PAYMENT_CLIENT_REGISTRATION_ID);
		return WebClient.builder().filter(oauthFilter).build();
	}

	@Bean
	public ApiClient paymentApiClient(PaymentProperties paymentProperties, WebClient paymentWebClient) {
		return new ApiClient(paymentWebClient).setBasePath(paymentProperties.getBaseUrl());
	}

	@Bean
	public DefaultApi paymentsApi(ApiClient paymentApiClient) {
		return new DefaultApi(paymentApiClient);
	}
}
