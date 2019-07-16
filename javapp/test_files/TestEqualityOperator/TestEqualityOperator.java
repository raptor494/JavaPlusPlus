package com.test;

public class TestEqualityOperator {
	public static void main(String[] args) {
		String test = new String("test");
		
		assert java.util.Objects.deepEquals(test, "test");
		assert test != null;
		assert java.util.Objects.deepEquals(test, test);
		assert test == test;
		assert test != null;
	}
}
