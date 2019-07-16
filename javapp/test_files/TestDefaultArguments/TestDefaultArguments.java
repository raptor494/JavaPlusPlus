package com.test;

public class TestDefaultArguments {
	void foo(int x, double y, double z) {}
	
	void foo(int x) {
		foo(x, -2.5);	
	}
	
	void foo(int x, double y) {
		foo(x, y, 3.01);
	}
	
	void bar(int x, double y, double z, String... args) {}
	
	void bar(int x, String... args) {
		bar(x, -2.5, args);
	}
	
	void bar(int x, double y, String... args) {
		bar(x, y, 3.01, args);
	}
	
	void kaz(int x, double y, double z, String... args) {}
	
	void kaz(int x) {
		kaz(x, -2.5);
	}
	
	void kaz(int x, double y) {
		kaz(x, y, 3.01);
	}
	
	void kaz(int x, double y, double z) {
		kaz(x, y, z, new String[] {"a"});
	}
	
	void kaz(int x, String... args) {
		kaz(x, -2.5, args);
	}
	
	void kaz(int x, double y, String... args) {
		kaz(x, y, 3.01, args);
	}
}
