package com.test;

public class TestPrintStatement {
	public static void main(String[] args) {
		java.lang.System.out.println("Hello, world!");
		{
			java.lang.System.out.print("Hello,");
			java.lang.System.out.print(' ');
			java.lang.System.out.println("world!");
		}
		{
			java.lang.System.out.print("Hello,");
			java.lang.System.out.print(' ');
			java.lang.System.out.println("world!");
		}
		java.lang.System.out.print("Hello, ");
		java.lang.System.out.print("world!\n");
		{
			java.lang.System.out.print("Hello,");
			java.lang.System.out.print(' ');
			java.lang.System.out.print("world!");
		}
		java.lang.System.out.println();
		;
	}
}
