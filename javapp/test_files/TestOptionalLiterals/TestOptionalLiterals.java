package com.test;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalDouble;
import java.util.OptionalLong;

public class TestOptionalLiterals {
	public static void main(String[] args) {
		Optional<String[]> opt1 = java.util.Optional.ofNullable(args);
		Optional<String[]> opt2 = java.util.Optional.<String>empty();
		assert args == opt1.orElseThrow();
		
		OptionalInt opt3 = java.util.OptionalInt.of(5);
		assert 5 == opt3.orElseThrow();
		
		OptionalInt opt4 = java.util.OptionalInt.of((int)5);
		assert 5 == opt4.orElseThrow();
	}
}
