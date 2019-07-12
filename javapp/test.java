package com.test;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseUnsignedInt;
import static java.lang.Long.parseLong;
import static java.lang.Long.parseUnsignedLong;
import static java.lang.Short.parseShort;
import static java.lang.String.format;
import static java.lang.String.join;
import java.util.function.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TimeZone;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Test {
	public static void main(String[] args) {
		java.lang.System.out.println("Hello, world!");		
	}
}

class Program implements Runnable {
	final Scanner keys = new Scanner(System.in);
	
	static enum NumberType {
		INTEGER,
		DECIMAL;
		
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	NumberType numType = NumberType.INTEGER;
	
	public void run() {
		for(;;) {
			switch(menu("What do you want to do?",
					"sum a series of numbers",
					"multiply a series of numbers",
					"subtract a series of numbers from an initial number",
					"divide a series of numbers from an initial number",
					"change the numeric type (currently " + numType + ")",
					"exit the program")) {
				case 1 -> sum();
				case 2 -> multiply();
				case 3 -> subtract();
				case 4 -> divide();
				case 5 -> changeNumType();
				case 6 -> { return; }
			}
		}
	}
	
	void sum() {
		switch(numType) {
			case INTEGER -> {
				int[] numbers = inputIntList("Enter the list of numbers you wish to add: ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(IntStream.of(numbers).sum());
				}
			}
			case DECIMAL -> {
				double[] numbers = inputDecimalList("Enter the list of numbers you wish to add: ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(DoubleStream.of(numbers).sum());
				}
			}
		}
	}
	
	void subtract() {
		switch(numType) {
			case INTEGER -> {
				int first = inputInt("Enter the first number: ");
				int[] numbers = inputIntList("Enter the list of numbers you wish to subtract from " + first + ": ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(IntStream.concat(IntStream.of(first), IntStream.of(numbers)).reduce((x,y) -> x - y).get());
				}
			}
			case DECIMAL -> {
				double first = inputDecimal("Enter the first number: ");
				double[] numbers = inputDecimalList("Enter the list of numbers you wish to subtract from " + first + ": ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(DoubleStream.concat(DoubleStream.of(first), DoubleStream.of(numbers)).reduce((x,y) -> x - y).get());
				}
			}
		}
	}
	
	void multiply() {
		switch(numType) {
			case INTEGER -> {
				int[] numbers = inputIntList("Enter the list of numbers you wish to multiply: ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(IntStream.of(numbers).reduce((x,y) -> x * y).get());
				}
			}
			case DECIMAL -> {
				double[] numbers = inputDecimalList("Enter the list of numbers you wish to multiply: ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(DoubleStream.of(numbers).reduce((x,y) -> x * y).get());
				}
			}
		}
	}
	
	void divide() {
		switch(numType) {
			case INTEGER -> {
				int first = inputInt("Enter the first number: ");
				int[] numbers = inputIntList("Enter the list of numbers you wish to subtract from " + first + ": ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(IntStream.concat(IntStream.of(first), IntStream.of(numbers)).reduce((x,y) -> x / y).get());
				}
			}
			case DECIMAL -> {
				double first = inputDecimal("Enter the first number: ");
				double[] numbers = inputDecimalList("Enter the list of numbers you wish to subtract from " + first + ": ");
				{
					java.lang.System.out.print("The sum of your numbers is:");
					java.lang.System.out.print(' ');
					java.lang.System.out.println(DoubleStream.concat(DoubleStream.of(first), DoubleStream.of(numbers)).reduce((x,y) -> x / y).get());
				}
			}
		}
	}
	
	void changeNumType() {
		numType = switch(menu("What number type do you want to use?",
				"integer",
				"decimal")) {
			case 1 -> NumberType.INTEGER;
			case 2 -> NumberType.DECIMAL;
			default -> throw new AssertionError();
		};
		java.lang.System.out.printf("The number type was changed to %s." + "%n", numType);
	}
	
	//#region parse methods
	static int parseInt(String str) {
		boolean negative = str.startsWith("-");
		if(negative || str.startsWith("+"))
			str = str.substring(1);
			
		int result;
		if(str.startsWith("0x") || str.startsWith("0X"))
			result = Integer.parseUnsignedInt(removeUnderscores(str.substring(2)), 16);
		else if(str.startsWith("0b") || str.startsWith("0B"))
			result = Integer.parseUnsignedInt(removeUnderscores(str.substring(2)), 2);
		else if(str.startsWith("0o") || str.startsWith("0O"))
			result = Integer.parseUnsignedInt(removeUnderscores(str.substring(2)), 8);
		else result = Integer.parseUnsignedInt(removeUnderscores(str));
		
		return negative? -result : result;
	}
	
	static int parseUnsignedInt(String str) {
		if(str.startsWith("0x") || str.startsWith("0X"))
			return Integer.parseUnsignedInt(removeUnderscores(str.substring(2)), 16);
		else if(str.startsWith("0b") || str.startsWith("0B"))
			return Integer.parseUnsignedInt(removeUnderscores(str.substring(2)), 2);
		else if(str.startsWith("0o") || str.startsWith("0O"))
			return Integer.parseUnsignedInt(removeUnderscores(str.substring(2)), 8);
		else return Integer.parseUnsignedInt(removeUnderscores(str));
	}
	
	static double parseDouble(String str) {
		if((str.startsWith("0x") || str.startsWith("0X") 
				|| str.startsWith("-0x") || str.startsWith("-0X")
				|| str.startsWith("+0x") || str.startsWith("+0X")
				) && !str.contains("p") && !str.contains("P"))
			str += "p0";
		return Double.parseDouble(removeUnderscores(str));
	}
	
	static String removeUnderscores(String str) {
		return str.replaceAll("(?<=\\d)_+(?=\\d)", "");
	}
	//#endregion parse methods
	
	//#region input methods
	String input(String prompt) {
		return input(prompt, false);
	}
	
	String input(String prompt, boolean allowEmpty) {
		if(prompt != null)
			java.lang.System.out.print(prompt);
		int noSuchElementCount = 0;
		for(;;) {
			try {
				String input = keys.nextLine().strip();
				if(!allowEmpty && input.isEmpty())
					java.lang.System.out.print("Empty input not allowed. Try again: ");
				else return input;
			} catch(NoSuchElementException e) {
				if(allowEmpty) {
					return "";
				} else if(noSuchElementCount++ > 3) {
					throw e;
				}
			}			
		}
	}
	
	int inputInt(String prompt) {
		String input = input(prompt, false);
		for(;;) {
			try {
				return parseInt(input);
			} catch(NumberFormatException e) {
				input = input("Error: '" + input + "' is not a valid integer. Try again: ", false);
			}
		}
	}
	
	int inputInt(String prompt, int defaultValue) {
		String input = input(prompt, true);
		for(;;) {
			if(input.isEmpty())
				return defaultValue;
			try {
				return parseInt(input);
			} catch(NumberFormatException e) {
				input = input("Error: '" + input + "' is not a valid integer. Try again: ", true);
			}
		}
	}
	
	int inputUnsignedInt(String prompt) {
		String input = input(prompt, false);
		for(;;) {
			try {
				return parseUnsignedInt(input);
			} catch(NumberFormatException e) {
				try {
					parseInt(input);
					input = input("Error: signed numbers not allowed. Try again: ", false);
				} catch(NumberFormatException e2) {
					input = input("Error: '" + input + "' is not a valid integer. Try again: ", false);
				}
			}
		}
	}
	
	int inputUnsignedInt(String prompt, int defaultValue) {
		String input = input(prompt, true);
		for(;;) {
			if(input.isEmpty())
				return defaultValue;
			try {
				return parseUnsignedInt(input);
			} catch(NumberFormatException e) {
				try {
					parseInt(input);
					input = input("Error: signed numbers not allowed. Try again: ", true);
				} catch(NumberFormatException e2) {
					input = input("Error: '" + input + "' is not a valid integer. Try again: ", true);
				}
			}
		}
	}
	
	double inputDecimal(String prompt) {
		String input = input(prompt, false);
		for(;;) {
			try {
				return parseDouble(input);
			} catch(NumberFormatException e) {
				input = input("Error: '" + input + "' is not a valid number. Try again: ", false);
			}
		}
	}
	
	double inputDecimal(String prompt, double defaultValue) {
		String input = input(prompt, true);
		for(;;) {
			if(input.isEmpty())
				return defaultValue;
			try {
				return parseDouble(input);
			} catch(NumberFormatException e) {
				input = input("Error: '" + input + "' is not a valid number. Try again: ", true);
			}
		}
	}
	
	double inputUnsignedDecimal(String prompt) {
		String input = input(prompt, false);
		for(;;) {
			try {
				double result = parseDouble(input);
				if(Math.copySign(1.0, result) == -1.0)
					input = input("Error: signed numbers not allowed. Try again: ", false);
				else return result;
			} catch(NumberFormatException e) {
				input = input("Error: '" + input + "' is not a valid number. Try again: ", false);
			}
		}
	}
	
	double inputUnsignedDecimal(String prompt, double defaultValue) {
		String input = input(prompt, true);
		for(;;) {
			if(input.isEmpty())
				return defaultValue;
			try {
				double result = parseDouble(input);
				if(Math.copySign(1.0, result) == -1.0)
					input = input("Error: signed numbers not allowed. Try again: ", true);
				else return result;
			} catch(NumberFormatException e) {
				input = input("Error: '" + input + "' is not a valid number. Try again: ", true);
			}
		}
	}
	
	int[] inputIntList(String prompt) {
		String input = input(prompt);
	forloop:
		for(;;) {
			String[] inputSplit = input.split(R"\s+");
			int[] numbers = new int[inputSplit.length];
			for(int i = 0; i < numbers.length; i++) {
				try {
					numbers[i] = parseInt(inputSplit[i]);
				} catch(NumberFormatException e) {
					java.lang.System.out.println("Error: '" + inputSplit[i] + "' is not a valid integer.");
					input = input("Please re-enter your list of numbers after fixing it: ");
					continue forloop;
				}
			}
			return numbers;
		}
	}
	
	double[] inputDecimalList(String prompt) {
		String input = input(prompt);
	forloop:
		for(;;) {
			String[] inputSplit = input.split(R"\s+");
			double[] numbers = new double[inputSplit.length];
			for(int i = 0; i < numbers.length; i++) {
				try {
					numbers[i] = parseDouble(inputSplit[i]);
				} catch(NumberFormatException e) {
					java.lang.System.out.println("Error: '" + inputSplit[i] + "' is not a valid number.");
					input = input("Please re-enter your list of numbers after fixing it: ");
					continue forloop;
				}
			}
			return numbers;
		}
	}
	
	//#endregion input methods
	
	int menu(String title, String... options) {
		if(title != null)
			java.lang.System.out.println(title);
		for(int index = 0; index < options.length; index++) {
			java.lang.System.out.printf("  %d. %s" + "%n", index+1, options[i]);
		}
		int selection = inputInt("Enter the number of your choice: ");
		while(selection <= 0 || selection > options.length) {
			selection = inputInt("That is not a valid option. Try again: ");
		}
		return selection;
	}
}