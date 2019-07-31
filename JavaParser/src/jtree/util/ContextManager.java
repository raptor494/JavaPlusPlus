package jtree.util;

public interface ContextManager extends AutoCloseable {
	void exit();
	
	@Override
	default void close() {
		exit();
	}
}