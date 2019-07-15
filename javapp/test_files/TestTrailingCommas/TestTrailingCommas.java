package com.test;

import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestTrailingCommas {
	public static void main(String[] args) {
		java.lang.System.out.println(IntStream.of(1, 2, 3, 4).sum());		
	}
}

class TestTrailingCommas2 implements AutoCloseable, Serializable {
	HashMap<String, Integer> map;
	public static final int FIELD_001 = 1, FIELD_002 = 2, FIELD_003 = 3, FIELD_004 = 4;
}