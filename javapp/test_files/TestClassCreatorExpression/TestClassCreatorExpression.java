package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestClassCreatorExpression {
	public static void main(String[] args) {
		ArrayList<String> strings = new ArrayList<String>(java.util.List.of("a", "b", "c", "d"));
		HashMap<String, Integer> numbers = new HashMap<>(java.util.Map.of("one", 1, "two", 2, "three", 3));
		HashSet<Float> floats = new HashSet<Float>(java.util.List.of(0.2f, 1.9f, -3.2f));
		
		var objects = new ArrayList<Object>();
		objects.add(strings);
		objects.add(numbers);
		
		java.lang.System.out.println(join(", ", objects));
	}
	
	static String join(String separator, Collection<?> objects) {
		var sb = new StringBuilder();
		boolean first = true;
		for(var obj : objects) {
			if(first)
				first = false;
			else
				sb.append(separator);
			sb.append(obj);
		}
		return sb.toString();
	}
}
