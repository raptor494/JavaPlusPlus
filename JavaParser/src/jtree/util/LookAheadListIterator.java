package jtree.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.function.Consumer;

import lombok.Setter;

public class LookAheadListIterator<T> implements ListIterator<T>, Iterable<T> {
	private List<T> items;
	private Stack<Integer> marks = new Stack<>();
	private int index;
	private Consumer<? super T> setter;
	
	public LookAheadListIterator(Iterable<? extends T> items) {
		this(items, null);
	}
	
	public LookAheadListIterator(Iterable<? extends T> items, Consumer<? super T> setter) {
		this.items = new ArrayList<>();
		for(T t : items) {
			this.items.add(t);
		}
		if(this.items.isEmpty()) {
			throw new IllegalArgumentException("No items given");
		}
		this.setter = setter;
	}
	
	@Override
	public T next() {
		if(index == items.size()) {
			return items.get(items.size()-1);
		} else {
			return items.get(index++);
		}
	}
	
	@Override
	public T previous() {
		if(index == 0) {
			return items.get(0);
		} else {
			return items.get(--index);
		}
	}
	
	public T look(int look) {
		int i = index + look;
		if(i < 0) {
			i = 0;
		} else if(i >= items.size()) {
			i = items.size()-1;
		}
		return items.get(i);
	}
	
	public class ResettableMarkContext implements AutoCloseable {
		@Setter
		private boolean reset = false;
		private boolean closed = false;
		
		public void reset() {
			reset = true;
		}
		
		public boolean willReset() {
			return reset;
		}
		
		public void reset(boolean reset) {
			this.reset = reset;
			close();
		}
		
		@Override
		public void close() {
			if(closed) {
				throw new IllegalStateException("ResettableMarkContext has already been closed");
			}
			closed = true;
			if(reset) {
				index = marks.pop();
				if(setter != null) {
					setter.accept(look(-1));
				}
			} else {
				marks.pop();
			}
		}
	}
	
	public ResettableMarkContext enter() {
		marks.push(index);
		return new ResettableMarkContext();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			int pos = index;

			@Override
			public boolean hasNext() {
				return pos < items.size();
			}

			@Override
			public T next() {
				if(pos >= items.size()) {
					throw new IllegalArgumentException();
				} else {
					return items.get(pos++);
				}
			}
		};
	}

	@Override
	public boolean hasNext() {
		return index < items.size();
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public int nextIndex() {
		return index+1;
	}

	@Override
	public int previousIndex() {
		return index-1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(T e) {
		throw new UnsupportedOperationException();
	}
	
}