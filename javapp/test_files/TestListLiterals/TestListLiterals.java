package com.test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestListLiterals {
	List<String> strings = java.util.List.of("a", "b", "c", "d");
	Map<String, Integer> numbers = java.util.Map.of("one", 1, "two", 2, "three", 3);
	Set<Float> floats = java.util.Set.of(0.2f, 1.9f, -3.2f);
	ArrayList<String> strings2 = new ArrayList<String>(java.util.List.of("a", "b", "c", "d"));
	HashMap<String, Integer> numbers2 = new HashMap<>(java.util.Map.of("one", 1, "two", 2, "three", 3));
	HashSet<Float> floats2 = new HashSet<Float>(java.util.List.of(0.2f, 1.9f, -3.2f));
}
