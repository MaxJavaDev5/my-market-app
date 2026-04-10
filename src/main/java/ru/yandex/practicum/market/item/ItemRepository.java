package ru.yandex.practicum.market.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

	@Override
	Item save(Item entity);

	@Override
	Optional<Item> findById(Long id);

	@Override
	List<Item> findAll();

	@Override
	Item getReferenceById(Long id);

	List<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
