package jtree.util;

import java.util.Stack;
import java.util.function.Supplier;

public final class ContextStack<T> implements ContextManager {
	private Stack<T> stack = new Stack<>();
	
	public ContextStack() {}
	
	public ContextStack(T initialElement) {
		stack.push(initialElement);
	}
	
	public boolean isEmpty() {
		return stack.isEmpty();
	}
	
	public int size() {
		return stack.size();
	}
	
	public T get(int index) {
		return stack.get(index);
	}
	
	public T current() {
		return stack.peek();
	}
	
	public T currentOrElse(T defaultValue) {
		if(stack.isEmpty()) {
			return defaultValue;
		} else {
			return stack.peek();
		}
	}
	
	public T currentOrElseGet(Supplier<? extends T> defaultValueSupplier) {
		if(stack.isEmpty()) {
			return defaultValueSupplier.get();
		} else {
			return stack.peek();
		}
	}
	
	public ContextStack<T> enter(T element) {
		stack.push(element);
		return this;
	}
	
	@Override
	public void exit() {
		stack.pop();
	}
	
	@Override
	public String toString() {
		return stack.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ContextStack && stack.equals(((ContextStack<?>)obj).stack) || stack.equals(obj);
	}

}
