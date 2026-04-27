package ru.yandex.practicum.market.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "users")
public class UserEntity
{
	@Id
	private Long id;

	private String username;

	private String password;

	private String role;

	private boolean enabled;
}
