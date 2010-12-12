package org.valgog.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

public class HStoreIterator implements Iterator<Map.Entry<String, String>> {
	
	private final String rawValue;
	private int currentRawValuePosition;
	private Entry<String,String> lastReturned;
	private Entry<String,String> nextEntry;

	public HStoreIterator(String rawValue) {
		this.rawValue = rawValue;
		this.currentRawValuePosition = -1;
		advance();
	}

	@Override
	public boolean hasNext() {
		return nextEntry != null;
	}

	@Override
	public Entry<String, String> next() {
		if (nextEntry == null)
			throw new NoSuchElementException();
		lastReturned = nextEntry;
		advance();
		return lastReturned;
	}
	
	/**
	 * Advance in parsing the rawValue and assign the nextValue
	 */
	private void advance() {
		// TODO: to be implemented
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
}
