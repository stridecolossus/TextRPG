package org.sarge.textrpg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sarge.lib.util.ToString;

/**
 * Iterator that continually cycles backwards-and-forwards through a list.
 * @author Sarge
 * @param <T>
 */
public class CycleIterator<T> implements Iterator<T> {
	private final List<T> list;

	private int index = -1;
	private boolean forwards = true;

	/**
	 * Constructor.
	 * @param list List
	 * @throws NullPointerException if the list is <tt>null</tt>
	 */
	public CycleIterator(List<T> list) {
		this.list = new ArrayList<>(list);
	}
	
	@Override
	public boolean hasNext() {
		return !list.isEmpty();
	}
	
	@Override
	public T next() {
		if(forwards) {
			if(index + 1 >= list.size()) {
				forwards = false;
				--index;
			}
			else {
				++index;
			}
		}
		else {
			if(index == 0) {
				forwards = true;
				++index;
			}
			else {
				--index;
			}
		}
		
		return list.get(index);
	}
	
	/**
	 * @return Current element
	 * @throws IndexOutOfBoundsException if the list is empty
	 */
	public T current() {
		return list.get(index);
	}
	
	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("index", index);
		ts.append("forwards", forwards);
		return ts.toString();
	}
}
