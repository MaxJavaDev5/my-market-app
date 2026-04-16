package ru.yandex.practicum.market.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

	@Override
	CartItem save(CartItem entity);

	@Override
	void delete(CartItem entity);

	@Override
	List<CartItem> findAll();

	@Override
	void deleteAll();

	void deleteByItemId(Long itemId);

	CartItem findByItemId(Long itemId);
}
