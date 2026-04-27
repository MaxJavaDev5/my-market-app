package ru.yandex.practicum.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig
{

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http)
	{
		return http.authorizeExchange(exchange -> exchange
						.pathMatchers("/", "/items", "/items/**", "/images/**").permitAll()
						.pathMatchers("/css/**", "/js/**", "/webjars/**", "/favicon.ico").permitAll()
						.anyExchange().authenticated())
				.formLogin(Customizer.withDefaults())
				.logout(Customizer.withDefaults())
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
