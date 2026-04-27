package ru.yandex.practicum.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class Paging {

	private final int pageSize;
	private final int pageNumber;
	private final boolean hasPrevious;
	private final boolean hasNext;
}
