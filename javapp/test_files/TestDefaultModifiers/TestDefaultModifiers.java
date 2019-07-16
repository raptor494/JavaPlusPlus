package com.test;

public class TestDefaultModifiers {
	public static void main(String[] args) {
		System.out.println("Hello, world!");
	}
	
	public void foo() {
		System.out.println("foo");
	}
	
	void bar() {
		System.out.println("bar");
	}
	
	public final void kaz() {
		System.out.println("kaz");
	}
	
	public void foobar() {
		System.out.println("foobar");
	}
}
