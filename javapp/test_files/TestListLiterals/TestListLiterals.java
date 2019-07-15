package com.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestListLiterals {
	List<String> strings = java.util.List.of("a", "b", "c", "d");
	Map<String, Integer> numbers = java.util.Map.of("one", 1, "two", 2, "three", 3);
	Set<Float> floats = java.util.Set.of(0.2f, 1.9f, -3.2f);
}
