package com.test;

import java.util.Random;

public class TestElvisOperator {
	public static void main(String[] args) {
		var rand = new Random();
		String str = rand.nextBoolean()? null : "test";
		
		assert "test".equals(java.util.Objects.requireNonNullElse(str, "test"));
	}
}
