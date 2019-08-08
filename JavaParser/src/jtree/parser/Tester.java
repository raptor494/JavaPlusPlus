package jtree.parser;

import static jtree.parser.JavaTokenType.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;

import jtree.nodes.INode;
import jtree.nodes.Name;
import jtree.nodes.REPLEntry;
import jtree.nodes.Statement;
import jtree.util.Either;
import jtree.util.Utils;
import lombok.SneakyThrows;

public class Tester {
	/*public static void main(String[] args) {
		new Tester().run();
	}*/
	
	protected final Map<String, Function<String, Object>> methods = getMethodMap();
	
	protected final Scanner keys = new Scanner(System.in);
	
	protected boolean loop;
	
	private static final Pattern COMMAND_REGEX = Pattern.compile("^\\s*/(.+)");
	
	public void run() {
		System.out.println("Enter commands below. Type \"/help\" for a list of commands.");
		loop = true;
		do {
			String input = input("test> ");
			var matcher = COMMAND_REGEX.matcher(input);
			if(matcher.find()) {
    			var commandAndArgs = splitCommand(matcher.group(1));
    			String command = commandAndArgs.getLeft();
    			String[] args = commandAndArgs.getRight();
    			
    			dispatchCommand(command, args);
			} else {
				parseJshellEntries(input);
			}
		} while(loop);
	}
	
	private String lastCommand, lastArgs[];
	
	protected void dispatchCommand(String command, String[] args) {
		if(command.equals("redo") || command.equals("Redo") || command.equals("REDO")) {
			redo(args);
			return;
		}
		switch(command) {
			case "help", "Help", "HELP" -> {

				lastCommand = command;
				lastArgs = args;
				help(args);
			}
			case "parse", "Parse", "PARSE" -> {
				lastCommand = command;
				lastArgs = args;
				parse(args);
			}
			case "tokenize", "Tokenize", "TOKENIZE" -> {
				lastCommand = command;
				lastArgs = args;
				tokenize(args);
			}
			case "quit", "Quit", "QUIT" -> loop = false;
			default -> {
				System.out.println("Unknown command: " + command);
				return;
			}
		}
	}
	
	protected Pattern PARSE_REPR_REGEX = Pattern.compile("(?ix) ^ parse \\s+ (-r \\s+ \\w+ | \\w+ \\s+ -r) (\\s|$)"),
					  PARSE_EXPR_REGEX = Pattern.compile("(?ix) ^ parse \\s+ \\w+ (\\s|$)"),
					  PARSE_FIND_REGEX = Pattern.compile("(?ix) ^ parse \\s+ -f (\\s|$)"),
					  PARSE_LIST_REGEX = Pattern.compile("(?ix) ^ parse \\s* $"),
					  TOKENIZE_REGEX = Pattern.compile("(?ix) ^ tokenize (\\s|$)");
	
	protected Pair<String, String[]> splitCommand(String input) {
		String[] split = input.split("\\s+",
		                             PARSE_REPR_REGEX.matcher(input).find()? 4
		                             : PARSE_FIND_REGEX.matcher(input).find()? 3
		                             : PARSE_EXPR_REGEX.matcher(input).find()? 3
		                             : PARSE_LIST_REGEX.matcher(input).find()? 0
		                             : TOKENIZE_REGEX.matcher(input).find()? 2
		                             : 0);
		String[] args = new String[split.length-1];
		System.arraycopy(split, 1, args, 0, args.length);
		return Pair.of(split[0], args);
		/*String command, args[];
		int i;
		for(i = 0; i < input.length(); i++) {
			if(Character.isWhitespace(input.charAt(i))) {
				break;
			}
		}
		command = input.substring(0, i);
		while(i < input.length() && Character.isWhitespace(input.charAt(i))) {
			i++;
		}
		if(i < input.length()) {
			if(command.equals("parse")) {
				int start = i;
				for(; i < input.length(); i++) {
					if(Character.isWhitespace(input.charAt(i))) {
						break;
					}
				}
				String subcommand = input.substring(start, i);
				while(i < input.length() && Character.isWhitespace(input.charAt(i))) {
					i++;
				}
				if(i < input.length()) {
					if(subcommand.equals("-r")) {
						start = i;
						for(; i < input.length(); i++) {
							if(Character.isWhitespace(input.charAt(i))) {
								break;
							}
						}
						String subcommand2 = input.substring(start, i);
						while(i < input.length() && Character.isWhitespace(input.charAt(i))) {
							i++;
						}
						if(i < input.length()) {
							args = new String[] {subcommand, subcommand2, input.substring(i)};
						} else {
							args = new String[] {subcommand, subcommand2};
						}
					} else {
						args = new String[] {subcommand, input.substring(i)};
					}
				} else {
					args = new String[] {subcommand};
				}
			} else {
				args = new String[] {input.substring(i)};
			}
		} else {
			args = new String[0];
		}
		return Pair.of(command, args);*/
	}
	
	protected void help(String[] args) {
		if(args.length == 0) {
			printHelp();
		} else {
			System.out.println("Too many arguments to command 'help'");
		}
	}

	protected void printHelp() {
		System.out.println(
			"Commands:\n"
			+ "/help\n"
			+ "/parse [[-r] <type> <code>]\n"
			+ "/parse -f <search terms>\n"
			+ "/tokenize <text>"
		);
	}
	
	protected void redo(String[] args) {
		if(args.length == 0) {
			redo();
		} else {
			System.out.println("Too many arguments to command 'redo'");
		}
	}
	
	protected void redo() {
		if(lastCommand == null) {
			System.out.println("No previous command");
		} else {
			dispatchCommand(lastCommand, lastArgs);
		}
	}
	
	protected void parse(String[] args) {
		if(args.length == 0) {
			listParseMethods(methods.keySet());
		} else if(args[0].equals("-f")) {
			searchForParseMethods(args);
		} else {
			executeParseMethod(args);
		}
	}
	
	protected void listParseMethods(Collection<String> parseMethods) {
		int count = 0;
		for(var word : parseMethods) {
			System.out.print(word);
			if(count == 5) {
				System.out.println();
				count = 0;
			} else {
				count++;
				System.out.print("  ");
			}
		}
		if(count != 0) {
			System.out.println();
		}
	}
	
	protected void searchForParseMethods(String[] args) {
		assert args.length >= 1;
		assert args[0].equals("-f");
		
		var searchTerms = new ArrayList<String>();
		if(args.length == 1) {
			var arg = input("... > ").strip();
			searchTerms.add(arg);
			lastArgs = new String[args.length+1];
			System.arraycopy(args, 0, lastArgs, 0, args.length);
			lastArgs[args.length] = arg;
		} else {
			for(int i = 1; i < args.length; i++) {
				searchTerms.add(args[i]);
			}
		}
		var results = new ArrayList<String>();
		for(var name : methods.keySet()) {
			boolean matches = false;
			for(var searchTerm : searchTerms) {
				if(searchTerm.startsWith("-")) {
					if(name.toLowerCase().contains(searchTerm.substring(1).toLowerCase())) {
						matches = false;
						break;
					}
				} else if(name.toLowerCase().contains(searchTerm.toLowerCase())) {
					matches = true;
					break;
				}
			}
			if(matches) {
				results.add(name);
			}
		}
		if(results.isEmpty()) {
			System.out.println("No parse methods matched your search terms.");
		} else {
			listParseMethods(results);
		}
	}
	
	static class UnfinishedLineManager {
		Stack<Character> stack = new Stack<>();
		char stringChar;
		boolean inString, inMLString, escape, inSLC, inMLC;
		
		boolean isUnfinished() {
			return inString || inMLString || inMLC || !stack.isEmpty();
		}
		
		void traverseLine(String line) {
			for(int i = 0; i < line.length(); i++) {
				char ch = line.charAt(i);
				if(inString) {
					if(escape) {
						escape = false;
					} else if(ch == stringChar || ch == '\n') {
						inString = false;
						stringChar = 0;
					} else if(ch == '\\') {
						escape = true;
					}
				} else if(inMLString) {
					if(escape) {
						escape = false;
					} else if(ch == stringChar && line.startsWith(Character.toString(stringChar).repeat(3), i)) {
						inMLString = false;
						stringChar = 0;
					} else if(ch == '\\') {
						escape = true;
					}
				} else if(inSLC) {
					if(ch == '\n') {
						inSLC = false;
					} 
				} else if(inMLC) {
					if(line.startsWith("*/", i)) {
						inMLC = false;
						i++;
					}
				} else {
					if(!stack.isEmpty() && ch == stack.peek()) {
						stack.pop();
					} else {
						switch(ch) {
							case '(' -> stack.push(')');
							case '[' -> stack.push(']');
							case '{' -> stack.push('}');
							case '"' -> {
								if(line.startsWith("\"\"\"", i)) {
									inMLString = true;
								} else {
									inString = true;
								}
								stringChar = ch;
							}
							case '\'' -> {
								inString = true;
								stringChar = ch;
							}
							case '/' -> {
								if(line.startsWith("//")) {
									inSLC = true;
									i++;
								} else if(line.startsWith("/*")) {
									inMLC = true;
									i++;
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected void parseJshellEntries(String input) {
		var lineManager = new UnfinishedLineManager();
		lineManager.traverseLine(input);
		if(lineManager.isUnfinished() || isBlank(input) || input.charAt(input.length()-1) == '\\') {
			var sb = new StringBuilder(input);
			try {
    			do {
    				String line = '\n' + input("... > ");
    				if(sb.length() != 0 && sb.charAt(sb.length()-1) == '\\') {
    					line = line.stripLeading();
    					sb.delete(sb.length()-1, sb.length());
    				}
    				sb.append(line);
    				lineManager.traverseLine(line);
    			} while(lineManager.isUnfinished() || isBlank(sb) || sb.charAt(sb.length()-1) == '\\');
			} catch(NoSuchElementException e) {
				return;
			}
			input = sb.toString();
		}
		lastCommand = "parse";
		if(lastArgs != null && lastArgs.length == 2) {
			lastArgs[0] = "jshellEntries";
			lastArgs[1] = input;
		} else {
			lastArgs = new String[] {"jshellEntries", input};
		}
		var parser = createParser(input, "<string>");
		try {
			printJshellEntries(parser.parseJshellEntries());
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	protected void printJshellEntries(List<REPLEntry> jshellEntries) {
		boolean first = true;
		for(var elem : jshellEntries) {
			if(first) {
				first = false;
			} else {
				System.out.println();
			}
			printNodeString(elem);
		}
	}

	protected void executeParseMethod(String[] args) {
		assert args.length > 0;
		
		Function<String, Object> parseMethod;
		String code;
		boolean repr = false;
		try {
			int index = 0;
			if(args[0].equals("-r")) {
				repr = true;
				index = 1;
			}
			String parseMethodName = args[index];
			if(parseMethodName.equals("statement")) {
				parseMethodName = "blockStatement";
			}
			parseMethod = methods.get(parseMethodName);
			if(parseMethod == null) {
				System.out.println("Unknown parse method: " + args[index]);
				return;
			}
			var sb = new StringBuilder();
			var lineManager = new UnfinishedLineManager();
			if(args.length-index > 1) {
				for(int i = index+1; i < args.length; i++) {
					if(i != index+1) {
						sb.append(' ');
					}
					sb.append(args[i]);
					lineManager.traverseLine(args[i]);
				}
			} else {
				String line = input("... > ");
				sb.append(line);
				lineManager.traverseLine(line);
				lastArgs = new String[args.length+1];
				System.arraycopy(args, 0, lastArgs, 0, args.length);
			}
			while(lineManager.isUnfinished() || isBlank(sb) || sb.charAt(sb.length()-1) == '\\') {
				String line = '\n' + input("... > ");
				if(sb.length() != 0 && sb.charAt(sb.length()-1) == '\\') {
					line = line.stripLeading();
					sb.delete(sb.length()-1, sb.length());
				}
				sb.append(line);
				lineManager.traverseLine(line);
			}
			lastArgs[lastArgs.length-1] = code = sb.toString();
			
			if(parseMethodName.equals("jshellEntries") && !repr) {
				parseJshellEntries(code);
				return;
			}
		} catch(NoSuchElementException e) {
			return;
		}
		
		
		try {
			var result = parseMethod.apply(code);
			if(repr) {
				printNodeRepr(result);
			} else {
    			printNodeString(result);
			}
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	protected static boolean isBlank(CharSequence cseq) {
		for(int i = 0; i < cseq.length(); i++) {
			if(!Character.isWhitespace(cseq.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	protected void tokenize(String[] args) {	
		var sb = new StringBuilder();
		if(args.length > 0) {
			for(int i = 0; i < args.length; i++) {
				if(i != 0) {
					sb.append(' ');
				}
				sb.append(args[i]);
			}
		} else {
			sb.append(input("... > ").stripTrailing());
		}
		while(sb.charAt(sb.length()-1) == '\\') {
			sb.delete(sb.length()-1, sb.length());
			sb.append(input("... > ").stripLeading());
		}
		
		String text = sb.toString();
		lastArgs = new String[] {"tokenize", text};
		
		var tkzr = createTokenizer(text, "<string>");
		for(var token : Utils.iter(tkzr)) {
			System.out.println(token);
		}
	}
	
	protected String input(String prompt) {
		System.out.print(prompt);
		return keys.nextLine();
	}
	
	protected JavaParser createParser(CharSequence text, String filename) {
		return new JavaParser(text, filename);
	}
	
	protected JavaTokenizer<JavaTokenType> createTokenizer(CharSequence text, String filename) {
		return new JavaTokenizer<>(text, filename, ENDMARKER, ERRORTOKEN, STRING, CHARACTER, NUMBER, NAME, COMMENT,
				   JavaTokenType.NORMAL_TOKENS.stream()
				   							  .collect(Collectors.toMap(token -> token.getSymbol().orElseThrow(), token -> token)));
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends JavaParser>[] getParserClasses() {
		return new Class[] { JavaParser.class };
	}
	
	protected void printNodeString(Object obj) {
		if(obj instanceof INode) {
			System.out.println(((INode)obj).toCode());
		} else if(obj instanceof List) {
			boolean first = true;
			for(var elem : (List<?>)obj) {
				if(first) {
					first = false;
				} else {
					System.out.println();
				}
				printNodeString(elem);
			}
		} else if(obj instanceof Either) {
			printNodeString(((Either<?,?>)obj).getValue());
		} else if(obj instanceof Pair) {
			var pair = (Pair<?,?>)obj;
			printNodeString(pair.getLeft());
			System.out.println();
			printNodeString(pair.getRight());
		} else if(obj instanceof String) {
			System.out.print('"');
			System.out.print(StringEscapeUtils.escapeJava((String)obj));
			System.out.println('"');
		} else {
			System.out.println(obj);
		}
	}
	
	protected void printNodeRepr(Object obj) {
		if(obj instanceof INode) {
			System.out.println(((INode)obj).toString());
		} else if(obj instanceof List) {
			boolean first = true;
			for(var elem : (List<?>)obj) {
				if(first) {
					first = false;
				} else {
					System.out.println();
				}
				printNodeRepr(elem);
			}
		} else if(obj instanceof Either) {
			printNodeRepr(((Either<?,?>)obj).getValue());
		} else if(obj instanceof Pair) {
			var pair = (Pair<?,?>)obj;
			printNodeRepr(pair.getLeft());
			System.out.println();
			printNodeRepr(pair.getRight());
		} else if(obj instanceof String) {
			System.out.print('"');
			System.out.print(StringEscapeUtils.escapeJava((String)obj));
			System.out.println('"');
		} else {
			System.out.println(obj);
		}
	}
	
	@SneakyThrows
	private HashMap<String, Function<String,Object>> getMethodMap() {
		var methods = new HashMap<String, Function<String, Object>>();
		for(var parserType : getParserClasses()) {
			for(var method : parserType.getMethods()) {
				if(!Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0
						&& method.getName().startsWith("parse") && method.getReturnType() != void.class) {
					methods.put(Character.toLowerCase(method.getName().charAt(5)) + method.getName().substring(6),
							new Function<>() {
								@SuppressWarnings({ "unchecked", "rawtypes" })
								@SneakyThrows
								public Object apply(String str) {
									var parser = createParser(str, "<string>");
									Object result;
									try(var $1 = parser.preStmts.enter();
											var $2 = parser.typeNames.enter(new Name("$Shell"))) {
										var obj = method.invoke(parser);
										if(parser.preStmts.isEmpty()) {
											result = obj;
										} else {
											List list = parser.preStmts.get();
											if(obj instanceof List) {
												list.addAll((List)obj);
												result = list;
											} else if(obj instanceof Statement) {
												result = parser.preStmts.apply((Statement)obj);
											} else {
												list.add(obj);
												result = list;
											}
										}
									} catch(InvocationTargetException e) {
										throw e.getCause();
									}
									if(!parser.accept(ENDMARKER)) {
										throw parser.syntaxError("unexpected token " + parser.token, parser.token);
									}
									return result;
								}
							});
					/*if(INode.class.isAssignableFrom(method.getReturnType())) {
						methods.put(Character.toLowerCase(method.getName().charAt(5)) + method.getName().substring(6),
						            new Function<>() {
										@SneakyThrows
										public Either<? extends INode, ? extends List<? extends INode>> apply(String str) {
											var parser = createParser(str, "<string>");
											Either<? extends INode, ? extends List<? extends INode>> result;
											try(var $ = parser.preStmts.enter()) {
												var node = (Node)method.invoke(parser);
												if(parser.preStmts.isEmpty()) {
													result = Either.first(node);
												} else {
													@SuppressWarnings({ "unchecked", "rawtypes" })
													ArrayList<INode> list = (ArrayList)parser.preStmts.get();
													list.add(node);
													result = Either.second(list);
												}
											} catch(InvocationTargetException e) {
												throw e.getCause();
											}
											if(parser.getTokens().hasNext()) {
												var token = parser.getTokens().look(0);
												if(token.getType() != ENDMARKER) {
													throw new SyntaxError("unexpected token " + token, parser.getFilename(), token.getStart().getLine(), token.getStart().getColumn(), token.getLine());
												}
											}
											return result;
										}
									});
					} else if(List.class.isAssignableFrom(method.getReturnType())) {
						var type = method.getGenericReturnType();
						if(type instanceof ParameterizedType) {
							var ptype = (ParameterizedType)type;
							if(ptype.getActualTypeArguments().length == 1) {
								var arg = ptype.getActualTypeArguments()[0];
								if(arg instanceof Class && INode.class.isAssignableFrom((Class<?>)arg)) {
									methods.put(Character.toLowerCase(method.getName().charAt(5)) + method.getName().substring(6),
									            new Function<>() {
													@SuppressWarnings("unchecked")
													@SneakyThrows
													public Either<? extends INode, ? extends List<? extends INode>> apply(String str) {
														var parser = createParser(str, "<string>");
														Either<? extends INode, ? extends List<? extends INode>> result;
														try(var $ = parser.preStmts.enter()) {
															List<INode> list = (List<INode>)method.invoke(parser);
															if(!parser.preStmts.isEmpty()) {
																if(!(list instanceof ArrayList)) {
																	list = new ArrayList<>(list);
																}
																list.addAll(0, parser.preStmts.get());
															}
															result = Either.second(list);
														} catch(InvocationTargetException e) {
															throw e.getCause();
														}
														if(parser.getTokens().hasNext()) {
															var token = parser.getTokens().next();
															if(token.getType() != ENDMARKER) {
																throw new SyntaxError("unexpected token " + token, parser.getFilename(), token.getStart().getLine(), token.getStart().getColumn(), token.getLine());
															}
														}
														return result;
													}
												});
								}
							}
						}
					}*/
				}
			}
		}
		return methods;
	}
}
