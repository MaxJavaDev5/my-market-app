package ru.yandex.practicum.market.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Override
	Order save(Order entity);

	@Override
	Optional<Order> findById(Long id);

	@Override
	List<Order> findAll();
}
