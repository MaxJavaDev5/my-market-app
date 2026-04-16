package ru.yandex.practicum.market;

public class Paging {

	private final int pageSize;
	private final int pageNumber;
	private final boolean hasPrevious;
	private final boolean hasNext;

	public Paging(int pageSize, int pageNumber, boolean hasPrevious, boolean hasNext) {
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
		this.hasPrevious = hasPrevious;
		this.hasNext = hasNext;
	}

	public boolean hasPrevious() {
		return hasPrevious;
	}

	public boolean hasNext() {
		return hasNext;
	}

	public int pageSize() {
		return pageSize;
	}

	public int pageNumber() {
		return pageNumber;
	}
}
