package com.test;

import java.util.regex.Pattern;

public class TestStringLiterals {
	String str1 = "\\r\\n\\t\\b\\f";
	String str2 = "\\r\\n\\t\\b\\f";
	String str3 = "\n\t\tline 1\n\t\tline 2\n\t\t";
	String str4 = " \"abcdef\" ";
	Pattern regex1 = java.util.regex.Pattern.compile("(ab)*/cd?\"\"");
	byte[] bytes1 = new byte[] {97, 98, 99};
}
