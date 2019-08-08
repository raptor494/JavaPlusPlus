package jtree.parser;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public enum JavaTokenType implements CharSequence, TokenPredicate<JavaTokenType> {
	ENDMARKER,
	ERRORTOKEN,
	STRING,
	CHARACTER,
	NUMBER,
	COMMENT,
	NAME(Tag.NAMED),
	
	// Keywords
	IF("if", Tag.STATEMENT_KW),
	WHILE("while", Tag.STATEMENT_KW),
	FOR("for", Tag.STATEMENT_KW),
	DO("do", Tag.STATEMENT_KW),
	ELSE("else", Tag.STATEMENT_KW),
	SYNCHRONIZED("synchronized", Tag.METHOD_MODIFIER, Tag.STATEMENT_KW),
	VOID("void"),
	BOOLEAN("boolean", Tag.PRIMITIVE_TYPE),
	BYTE("byte", Tag.PRIMITIVE_TYPE),
	SHORT("short", Tag.PRIMITIVE_TYPE),
	CHAR("char", Tag.PRIMITIVE_TYPE),
	INT("int", Tag.PRIMITIVE_TYPE),
	LONG("long", Tag.PRIMITIVE_TYPE),
	FLOAT("float", Tag.PRIMITIVE_TYPE),
	DOUBLE("double", Tag.PRIMITIVE_TYPE),
	NULL("null"),
	TRUE("true"),
	FALSE("false"),
	CLASS("class"),
	INTERFACE("interface"),
	ENUM("enum"),
	ASSERT("assert", Tag.STATEMENT_KW),
	RETURN("return", Tag.STATEMENT_KW),
	TRY("try", Tag.STATEMENT_KW),
	CATCH("catch", Tag.STATEMENT_KW),
	FINALLY("finally", Tag.STATEMENT_KW),
	THROW("throw", Tag.STATEMENT_KW),
	THROWS("throws"),
	SUPER("super"),
	THIS("this"),
	NEW("new"),
	PUBLIC("public", Tag.CLASS_MODIFIER, Tag.FIELD_MODIFIER, Tag.METHOD_MODIFIER, Tag.CONSTRUCTOR_MODIFIER, Tag.VISIBILITY_MODIFIER),
	PRIVATE("private", Tag.CLASS_MODIFIER, Tag.FIELD_MODIFIER, Tag.METHOD_MODIFIER, Tag.CONSTRUCTOR_MODIFIER, Tag.VISIBILITY_MODIFIER),
	PROTECTED("protected", Tag.CLASS_MODIFIER, Tag.FIELD_MODIFIER, Tag.METHOD_MODIFIER, Tag.CONSTRUCTOR_MODIFIER, Tag.VISIBILITY_MODIFIER),
	PACKAGE("package", Tag.CLASS_MODIFIER, Tag.FIELD_MODIFIER, Tag.METHOD_MODIFIER, Tag.CONSTRUCTOR_MODIFIER, Tag.VISIBILITY_MODIFIER),
	STATIC("static", Tag.CLASS_MODIFIER, Tag.FIELD_MODIFIER, Tag.METHOD_MODIFIER, Tag.REQUIRES_MODIFIER),
	NATIVE("native", Tag.METHOD_MODIFIER),
	STRICTFP("strictfp", Tag.CLASS_MODIFIER, Tag.METHOD_MODIFIER),
	ABSTRACT("abstract", Tag.CLASS_MODIFIER, Tag.METHOD_MODIFIER),
	TRANSIENT("transient", Tag.FIELD_MODIFIER),
	VOLATILE("volatile", Tag.FIELD_MODIFIER),
	FINAL("final", Tag.CLASS_MODIFIER, Tag.METHOD_MODIFIER, Tag.FIELD_MODIFIER, Tag.LOCAL_VAR_MODIFIER),
	EXTENDS("extends"),
	IMPLEMENTS("implements"),
	INSTANCEOF("instanceof"),
	BREAK("break", Tag.STATEMENT_KW),
	CONTINUE("continue", Tag.STATEMENT_KW),
	SWITCH("switch", Tag.STATEMENT_KW),
	CASE("case", Tag.STATEMENT_KW),
	DEFAULT("default", Tag.METHOD_MODIFIER, Tag.STATEMENT_KW),
	IMPORT("import", Tag.STATEMENT_KW),
	VAR("var", Tag.NAMED),
	MODULE("module", Tag.NAMED),
	REQUIRES("requires", Tag.NAMED),
	EXPORTS("exports", Tag.NAMED),
	OPENS("opens", Tag.NAMED),
	USES("uses", Tag.NAMED),
	PROVIDES("provides", Tag.NAMED),
	OPEN("open", Tag.NAMED, Tag.MODULE_MODIFIER),
	TO("to", Tag.NAMED),
	WITH("with", Tag.NAMED, Tag.STATEMENT_KW),
	TRANSITIVE("transitive", Tag.NAMED, Tag.REQUIRES_MODIFIER),
	YIELD("yield", Tag.NAMED),
	// unused by vanilla Java
	FROM("from", Tag.NAMED, Tag.STATEMENT_KW),
	IN("in", Tag.NAMED),
	IS("is", Tag.NAMED),
	AS("as", Tag.NAMED),
	UNIMPORT("unimport", Tag.NAMED, Tag.STATEMENT_KW),
	PRINT("print", Tag.NAMED),
	PRINTLN("println", Tag.NAMED),
	PRINTF("printf", Tag.NAMED),
	PRINTFLN("printfln", Tag.NAMED),
	ANNOTATION("annotation", Tag.NAMED),
	OVERRIDE("override", Tag.NAMED),
	ENABLE("enable", Tag.NAMED),
	DISABLE("disable", Tag.NAMED),
	EXIT("exit", Tag.NAMED),
	GET("get", Tag.NAMED),
	SET("set", Tag.NAMED),
	SELECT("select", Tag.NAMED),
	
	UNDERSCORE("_"),	
	
	// Other
	/** {@code +} */
	PLUS("+"),
	/** {@code -} */
	SUB("-"),
	/** {@code *} */
	STAR("*"),
	/** {@code /} */
	SLASH("/"),
	/** {@code %} */
	PERCENT("%"),
	/** {@code ^} */
	CARET("^"),
	/** {@code |} */
	BAR("|"),
	/** {@code &} */
	AMP("&"),
	/** {@code !} */
	BANG("!"),
	/** {@code ~} */
	TILDE("~"),
	/** {@code =} */
	EQ("="),
	/** {@code :} */
	COLON(":"),
	/** {@code .} */
	DOT("."),
	/** {@code <} */
	LT("<"),
	/** {@code >} */
	GT(">"),
	/** {@code ;} */
	SEMI(";"),
	/** {@code ,} */
	COMMA(","),
	/** {@code ?} */
	QUES("?"),
	/** {@code @} */
	AT("@"),
	/** {@code #} */
	HASHTAG("#"),
	/** {@code (} */
	LPAREN("("),
	/** {@code )} */
	RPAREN(")"),
	/** {@code [} */
	LBRACKET("["),
	/** {@code ]} */
	RBRACKET("]"),
	/** <code>{</code> */
	LBRACE("{"),
	/** <code>}</code> */
	RBRACE("}"),
	
	/** {@code ++} */
	PLUSPLUS("++"),
	/** {@code --} */
	SUBSUB("--"),
	/** {@code <<} */
	LTLT("<<"),
	/** {@code &&} */
	AMPAMP("&&"),
	/** {@code ||} */
	BARBAR("||"),
	/** {@code ::} */
	COLCOL("::"),
	/** {@code ==} */
	EQEQ("=="),
	/** {@code !=} */
	BANGEQ("!="),
	/** {@code +=} */
	PLUSEQ("+="),
	/** {@code -=} */
	SUBEQ("-="),
	/** {@code *=} */
	STAREQ("*="),
	/** {@code /=} */
	SLASHEQ("/="),
	/** {@code %=} */
	PERCENTEQ("%="),
	/** {@code <=} */
	LTEQ("<="),
	/** {@code >=} */
	GTEQ(">="),
	/** {@code ^=} */
	CARETEQ("^="),
	/** {@code |=} */
	BAREQ("|="),
	/** {@code &=} */
	AMPEQ("&="),
	/** {@code ->} */
	ARROW("->"),
	
	/** {@code <<=} */
	LTLTEQ("<<="),
	/** {@code >>=} */
	GTGTEQ(">>="),
	/** {@code ...} */
	ELLIPSIS("..."),
	
	/** {@code >>>=} */
	GTGTGTEQ(">>>="),
	
	;
	
	private static final Set<JavaTokenType> mutableValues = new HashSet<>(EnumSet.allOf(JavaTokenType.class));
	private static final Set<JavaTokenType> mutableNormalTokens = new HashSet<>(EnumSet.allOf(JavaTokenType.class));
	static {
		mutableNormalTokens.removeIf(type -> type.symbol.isEmpty());
	}
	
	public static final Set<JavaTokenType> VALUES = Collections.unmodifiableSet(mutableValues);
	public static final Set<JavaTokenType> NORMAL_TOKENS = Collections.unmodifiableSet(mutableNormalTokens);
	
	/*/// JavaTokenType()
	private static final Constructor<JavaTokenType> constructor1;
	/// JavaTokenType(Tag...)
	private static final Constructor<JavaTokenType> constructor2;
	/// JavaTokenType(String)
	private static final Constructor<JavaTokenType> constructor3;
	/// JavaTokenType(String, Tag...)
	private static final Constructor<JavaTokenType> constructor4;
	static {
		try {
			constructor1 = JavaTokenType.class.getDeclaredConstructor(String.class, int.class);
			constructor1.setAccessible(true);
			constructor2 = JavaTokenType.class.getDeclaredConstructor(String.class, int.class, Tag[].class);
			constructor2.setAccessible(true);
			constructor3 = JavaTokenType.class.getDeclaredConstructor(String.class, int.class, String.class);
			constructor3.setAccessible(true);
			constructor4 = JavaTokenType.class.getDeclaredConstructor(String.class, int.class, String.class, Tag[].class);
			constructor4.setAccessible(true);
		} catch(NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SneakyThrows
	public static JavaTokenType add(@NonNull String name) {
		JavaTokenType type = constructor1.newInstance(name, VALUES.size());
		mutableValues.add(type);
		return type;
	}
	
	@SneakyThrows
	public static JavaTokenType add(@NonNull String name, Tag... tags) {
		for(var tag : tags) {
			Objects.requireNonNull(tag);
		}
		JavaTokenType type = constructor2.newInstance(name, VALUES.size(), tags);
		mutableValues.add(type);
		return type;
	}
	
	@SneakyThrows
	public static JavaTokenType add(@NonNull String name, @NonNull String symbol) {
		JavaTokenType type = constructor3.newInstance(name, VALUES.size(), symbol);
		mutableValues.add(type);
		mutableNormalTokens.add(type);
		return type;
	}
	
	
	@SneakyThrows
	public static JavaTokenType add(@NonNull String name, @NonNull String symbol, Tag... tags) {
		for(var tag : tags) {
			Objects.requireNonNull(tag);
		}
		JavaTokenType type = constructor4.newInstance(name, VALUES.size(), symbol, tags);
		mutableValues.add(type);
		mutableNormalTokens.add(type);
		return type;
	}*/
	
	
	@Getter
	private final Optional<String> symbol;
	@Getter @Accessors(fluent = true)
	private final String toString;
	@Getter
	private final Set<JavaTokenType.Tag> tags;
	@Getter
	private final boolean isKeyword;
	
	JavaTokenType() {
		symbol = Optional.empty();
		toString = name();
		tags = Collections.unmodifiableSet(EnumSet.noneOf(Tag.class));
		isKeyword = false;
	}
	
	JavaTokenType(String str) {
		symbol = Optional.of(str);
		toString = "'" + str + "'";
		tags = Collections.unmodifiableSet(EnumSet.noneOf(Tag.class));
		isKeyword = isKeyword(str);
	}
	
	JavaTokenType(Tag tag) {
		symbol = Optional.empty();
		toString = name();
		tags = Collections.unmodifiableSet(EnumSet.of(tag));
		isKeyword = false;
	}
	
	JavaTokenType(String str, Tag tag) {
		symbol = Optional.of(str);
		toString = "'" + str + "'";
		tags = Collections.unmodifiableSet(EnumSet.of(tag));
		isKeyword = isKeyword(str) && !hasTag(Tag.NAMED);
	}
	
	JavaTokenType(Tag... tagsIn) {
		symbol = Optional.empty();
		toString = name();
		tags = Collections.unmodifiableSet(tagsIn.length == 0? EnumSet.noneOf(Tag.class) : EnumSet.of(tagsIn[0], tagsIn));
		isKeyword = false;
	}
	
	JavaTokenType(String str, Tag... tagsIn) {
		symbol = Optional.of(str);
		toString = "'" + str + "'";
		tags = Collections.unmodifiableSet(tagsIn.length == 0? EnumSet.noneOf(Tag.class) : EnumSet.of(tagsIn[0], tagsIn));
		isKeyword = isKeyword(str) && !hasTag(Tag.NAMED);
	}
	
	private static boolean isKeyword(String str) {
		return Character.isJavaIdentifierPart(str.charAt(str.length()-1));
	}
	
	@Override
	public int length() {
		return symbol.map(String::length).orElse(0);
	}

	@Override
	public char charAt(int index) {
		return symbol.orElse("").charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return symbol.orElse("").subSequence(start, end);
	}

	@Override
	public boolean test(Token<JavaTokenType> t) {
		return this == t.getType();
	}
	
	public boolean hasTag(@NonNull Tag tag) {
		return tags.contains(tag);
	}
	
	public static enum Tag implements TokenPredicate<JavaTokenType> {
		NAMED,
		PRIMITIVE_TYPE,
		STATEMENT_KW,
		CLASS_MODIFIER,
		LOCAL_VAR_MODIFIER,
		FIELD_MODIFIER,
		METHOD_MODIFIER,
		CONSTRUCTOR_MODIFIER,
		VISIBILITY_MODIFIER,
		MODULE_MODIFIER,
		REQUIRES_MODIFIER,
		;

		@Override
		public boolean test(Token<JavaTokenType> token) {
			return token.getType().hasTag(this);
		}
		
	}
	
}
