package com.test;

import java.util.Random;

public class TestInlineVardecl {
	public static void main(String[] args) {
		var rand = new Random();
		
		{
			int x;
			if((x = rand.nextInt()) < 50) {
				System.out.printf("%d < 50%n", x);
			} else {
				System.out.printf("%d >= 50%n", x);
			}
		}
	}
}
