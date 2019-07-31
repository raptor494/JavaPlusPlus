package jtree.parser;

import static jtree.parser.JavaTokenType.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import jtree.nodes.INode;
import jtree.nodes.Node;
import jtree.util.Either;
import jtree.util.Utils;
import lombok.SneakyThrows;

public class Tester {
	/*public static void main(String[] args) {
		new Tester().run();
	}*/
	
	protected final Map<String, Function<String, Either<? extends INode, ? extends List<? extends INode>>>> methods = getMethodMap();
	
	protected final Scanner keys = new Scanner(System.in);
	
	protected boolean loop;
	
	public void run() {
		System.out.println("Enter commands below. Type \"help\" for a list of commands.");
		loop = true;
		do {
			var commandAndArgs = splitCommand(input("test> ").stripLeading());
			String command = commandAndArgs.getLeft();
			String[] args = commandAndArgs.getRight();
			
			dispatchCommand(command, args);
		} while(loop);
	}
	
	private String lastCommand, lastArgs[];
	
	protected void dispatchCommand(String command, String[] args) {
		if(command.equals("redo") || command.equals("Redo") || command.equals("REDO")) {
			redo(args);
			return;
		}
		lastCommand = command;
		lastArgs = args;
		switch(command) {
			case "help", "Help", "HELP" -> help(args);
			case "parse", "Parse", "PARSE" -> parse(args);
			case "tokenize", "Tokenize", "TOKENIZE" -> tokenize(args);
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
			+ "help\n"
			+ "parse [[-r] <type> <code>]\n"
			+ "parse -f <search terms>\n"
			+ "tokenize <text>"
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
	
	protected void executeParseMethod(String[] args) {
		assert args.length > 0;
		
		Function<String, Either<? extends INode, ? extends List<? extends INode>>> parseMethod;
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
		} catch(NoSuchElementException e) {
			return;
		}
		
		try {
			var either = parseMethod.apply(code);
			if(repr) {
				either.accept(
    				(INode node) -> {
    					System.out.println(node.toString());
    				},
    				(List<? extends INode> nodes) -> {
    					for(int i = 0; i < nodes.size(); i++) {
    						if(i != 0) {
    							System.out.println();
    						}
    						System.out.println(nodes.get(i).toString());
    					}
    				}
    			);
			} else {
    			either.accept(
    				(INode node) -> {
    					System.out.println(node.toCode());
    				},
    				(List<? extends INode> nodes) -> {
    					for(int i = 0; i < nodes.size(); i++) {
    						if(i != 0) {
    							System.out.println();
    						}
    						System.out.println(nodes.get(i).toCode());
    					}
    				}
    			);
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
	
	@SneakyThrows
	private HashMap<String, Function<String, Either<? extends INode, ? extends List<? extends INode>>>> getMethodMap() {
		var methods = new HashMap<String, Function<String, Either<? extends INode, ? extends List<? extends INode>>>>();
		for(var parserType : getParserClasses()) {
			for(var method : parserType.getMethods()) {
				if(!Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0
						&& method.getName().startsWith("parse")) {
					if(INode.class.isAssignableFrom(method.getReturnType())) {
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
					}
				}
			}
		}
		return methods;
	}
}
