package jtree.parser;

import static jtree.parser.JavaTokenType.*;
import static jtree.util.Utils.*;
import static java.util.Collections.singletonList;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;

import jtree.nodes.*;
import jtree.nodes.Modifier.Modifiers;
import jtree.parser.JavaTokenType.Tag;
import jtree.util.ContextManager;
import jtree.util.ContextStack;
import jtree.util.Either;
import jtree.util.LookAheadListIterator;
import lombok.Getter;
import lombok.NonNull;

public class JavaParser {
	@Getter
	protected LookAheadListIterator<Token<JavaTokenType>> tokens;
	protected Token<JavaTokenType> token;
	@Getter
	protected String filename;
	@Getter
	protected JavaTokenizer<JavaTokenType> tokenizer;

	protected ContextStack<Name> typeNames = new ContextStack<>();

	protected class PreStmtManager implements Iterable<Statement> {
		private ContextStack<ArrayList<Statement>> stack = new ContextStack<>();

		public void append(Statement stmt) {
			stack.current().add(stmt);
		}

		public Block apply(Block stmt) {
			if(!stack.isEmpty()) {
				var stmts = stack.current();
				if(!stmts.isEmpty()) {
					stmt.getStatements().addAll(0, stmts);
				}
			}
			return stmt;
		}

		public Statement apply(Statement stmt) {
			if(stack.isEmpty()) {
				return stmt;
			}
			var stmts = stack.current();
			if(stmts.isEmpty()) {
				return stmt;
			} else if(stmt instanceof Block) {
				((Block)stmt).getStatements().addAll(0, stmts);
				return stmt;
			} else {
				stmts.add(stmt);
				return new Block(stmts);
			}
		}

		public ContextManager enter() {
			return stack.enter(new ArrayList<>());
		}

		public boolean isWithinContext() {
			return !stack.isEmpty();
		}

		public boolean isWithoutContext() {
			return stack.isEmpty();
		}

		public boolean isEmpty() {
			return stack.current().isEmpty();
		}

		@Override
		public Iterator<Statement> iterator() {
			return stack.current().iterator();
		}

		public ArrayList<Statement> get() {
			return stack.current();
		}
	}

	protected PreStmtManager preStmts = new PreStmtManager();

	public JavaParser(CharSequence text) {
		this(text, "<unknown source>");
	}

	public JavaParser(CharSequence text, String filename) {
		this.tokens = new LookAheadListIterator<>(iter(this.tokenizer = createTokenizer(text, filename)), token -> {
			this.token = token;
			if(this.token.getType() == COMMENT) {
				nextToken();
			}
		});
		this.filename = filename;
		this.token = nextToken();
	}

	protected JavaTokenizer<JavaTokenType> createTokenizer(CharSequence text, String filename) {
		return new JavaTokenizer<>(text, filename, ENDMARKER, ERRORTOKEN, STRING, CHARACTER, NUMBER, NAME, COMMENT,
				JavaTokenType.NORMAL_TOKENS.stream()
						.collect(Collectors.toMap(token -> token.getSymbol().orElseThrow(), token -> token)));
	}

	protected Token<JavaTokenType> nextToken() {
		do {
			token = tokens.next();
		} while(token.getType() == COMMENT);
		return token;
	}

	protected Optional<String> getDocComment() {
		var token = tokens.look(-2);
		if(token.getType() == COMMENT && token.getString().startsWith("/**") && token.getString().length() > 4) {
			return Optional.of(token.getString());
		} else {
			return Optional.empty();
		}
	}

	protected void nextToken(int amount) {
		if(amount <= 0) {
			throw new IllegalArgumentException("amount <= 0");
		}
		for(int i = 0; i < amount; i++) {
			nextToken();
		}
	}

	protected boolean wouldAccept(TokenPredicate<JavaTokenType> test) {
		return test.test(token);
	}

	protected boolean wouldAccept(String str) {
		return token.getString().equals(str);
	}

	@SafeVarargs
	protected final boolean wouldAccept(TokenPredicate<JavaTokenType>... tests) {
		if(tests.length == 0) {
			throw new IllegalArgumentException("no tests given");
		}
		if(tests[0].test(token)) {
			for(int i = 1; i < tests.length; i++) {
				if(!tests[i].test(tokens.look(i - 1))) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@SafeVarargs
	protected final boolean wouldAcceptPseudoOp(TokenPredicate<JavaTokenType>... tests) {
		if(tests.length == 0) {
			throw new IllegalArgumentException("no tests given");
		}
		if(tests[0].test(token)) {
			var last = token;
			for(int i = 1; i < tests.length; i++) {
				var nextTok = tokens.look(i - 1);
				if(!tests[i].test(nextTok) || !last.getEnd().equals(nextTok.getStart())) {
					return false;
				}
				last = nextTok;
			}
			return true;
		} else {
			return false;
		}
	}

	protected boolean wouldAccept(String... strs) {
		if(strs.length == 0) {
			throw new IllegalArgumentException("no tests given");
		}
		if(token.getString().equals(strs[0])) {
			for(int i = 1; i < strs.length; i++) {
				if(!tokens.look(i - 1).getString().equals(strs[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	protected boolean wouldNotAccept(TokenPredicate<JavaTokenType> test) {
		return token.getType() != ENDMARKER && !test.test(token);
	}

	protected boolean wouldNotAccept(String str) {
		return token.getType() != ENDMARKER && !token.getString().equals(str);
	}

	@SafeVarargs
	protected final boolean wouldNotAccept(TokenPredicate<JavaTokenType>... tests) {
		return token.getType() != ENDMARKER && !wouldAccept(tests);
	}

	protected boolean accept(TokenPredicate<JavaTokenType> test) {
		if(wouldAccept(test)) {
			nextToken();
			return true;
		} else {
			return false;
		}
	}

	protected boolean accept(String symbol) {
		if(wouldAccept(symbol)) {
			nextToken();
			return true;
		} else {
			return false;
		}
	}

	@SafeVarargs
	protected final boolean accept(TokenPredicate<JavaTokenType>... tests) {
		if(wouldAccept(tests)) {
			nextToken(tests.length);
			return true;
		} else {
			return false;
		}
	}

	@SafeVarargs
	protected final boolean acceptPseudoOp(TokenPredicate<JavaTokenType>... tests) {
		if(wouldAcceptPseudoOp(tests)) {
			nextToken(tests.length);
			return true;
		} else {
			return false;
		}
	}

	protected boolean accept(String... strs) {
		if(wouldAccept(strs)) {
			nextToken(strs.length);
			return true;
		} else {
			return false;
		}
	}
	
	protected SyntaxError syntaxError(String message) {
		return syntaxError(message, token);
	}

	protected SyntaxError syntaxError(String message, Token<JavaTokenType> token) {
		throw new SyntaxError(message, filename, token.getStart().getLine(), token.getStart().getColumn(),
				token.getLine());
	}

	protected void require(TokenPredicate<JavaTokenType> test) {
		if(!accept(test)) {
			throw syntaxError("expected '" + test + "' here, got " + token);
		}
	}

	protected void require(String test) {
		if(!accept(test)) {
			throw syntaxError("expected '" + test + "' here, got " + token);
		}
	}

	@SafeVarargs
	protected final void require(TokenPredicate<JavaTokenType>... tests) {
		if(!accept(tests)) {
			throw syntaxError("expected " + Arrays.toString(tests) + " here, got " + token);
		}
	}

	protected final TokenPredicate<JavaTokenType> notInterface = not(INTERFACE);

	protected static TokenPredicate<JavaTokenType> test(@NonNull TokenPredicate<JavaTokenType> pred) {
		return pred;
	}

	protected static TokenPredicate<JavaTokenType> test(@NonNull String str) {
		return token -> token.getString().equals(str);
	}

	protected static TokenPredicate<JavaTokenType> not(TokenPredicate<JavaTokenType> pred) {
		return TokenPredicate.not(pred);
	}

	protected static TokenPredicate<JavaTokenType> not(@NonNull String str) {
		return TokenPredicate.not(str);
	}

	public <T> ArrayList<T> listOf(Supplier<T> parser) {
		return listOf(COMMA, parser);
	}

	public <T> ArrayList<T> listOf(TokenPredicate<JavaTokenType> separator, Supplier<T> parser) {
		var list = new ArrayList<T>();
		do {
			list.add(parser.get());
		} while(accept(separator));
		return list;
	}

	private static final HashMap<String,Name> normalNameMap = new HashMap<>();

	protected static Name Name(String str) {
		return normalNameMap.computeIfAbsent(str, Name::new);
	}

	private static final HashMap<String,QualifiedName> qualNameMap = new HashMap<>();

	protected static QualifiedName QualifiedName(String str) {
		return qualNameMap.computeIfAbsent(str, QualifiedName::new);
	}

	public Name parseName() {
		var token = this.token;
		require(Tag.NAMED);
		return Name(token.getString());
	}

	public QualifiedName parseQualName() {
		return new QualifiedName(listOf(DOT, this::parseName));
	}

	public Name parseTypeName() {
		if(wouldAccept(VAR)) {
			throw syntaxError("'var' is not allowed as a type name");
		}
		return parseName();
	}

	public QualifiedName parseQualTypeName() {
		var names = new ArrayList<Name>();
		Token<JavaTokenType> last;
		do {
			last = token;
			names.add(parseName());
		} while(accept(DOT));
		if(names.get(names.size() - 1).equals("var") && names.size() > 1) {
			throw new SyntaxError("'var' is not allowed as a type name", filename, last.getStart().getLine(),
					last.getStart().getColumn(), last.getLine());
		}
		return new QualifiedName(names);
	}

	public CompilationUnit parseCompilationUnit() {
		Optional<String> docComment = getDocComment();
		var modsAndAnnos = new ModsAndAnnotations(emptyList(), parseAnnotations());

		Optional<PackageDecl> pckg;

		if(wouldAccept(PACKAGE, Tag.NAMED)) {
			pckg = Optional.of(parsePackageDecl(docComment, modsAndAnnos.annos));
			docComment = getDocComment();
			modsAndAnnos = parseClassModsAndAnnotations();
		} else {
			pckg = Optional.empty();
			var modsAndAnnos2 = parseClassModsAndAnnotations();
			modsAndAnnos2.annos.addAll(0, modsAndAnnos.annos);
			modsAndAnnos = modsAndAnnos2;
		}

		List<ImportDecl> imports;
		if(modsAndAnnos.isEmpty()) {
			imports = parseImportSection();
			docComment = getDocComment();
			modsAndAnnos = parseClassModsAndAnnotations();
		} else {
			imports = emptyList();
		}

		if(modsAndAnnos.mods.isEmpty() && pckg.isEmpty() && wouldAccept(OPEN.or(MODULE))) {
			return parseModuleCompilationUnit(imports, docComment, modsAndAnnos.annos);
		} else {
			return parseNormalCompilationUnit(pckg, imports, docComment, modsAndAnnos);
		}
	}

	public ModuleCompilationUnit parseModuleCompilationUnit() {
		return parseModuleCompilationUnit(parseImportSection());
	}

	public ModuleCompilationUnit parseModuleCompilationUnit(List<ImportDecl> imports) {
		return parseModuleCompilationUnit(imports, getDocComment(), parseAnnotations());
	}

	public ModuleCompilationUnit parseModuleCompilationUnit(List<ImportDecl> imports, Optional<String> docComment, List<Annotation> annotations) {
		boolean open = accept(OPEN);
		require(MODULE);
		var name = parseQualName();
		require(LBRACE);
		var directives = new ArrayList<Directive>();
		while(wouldNotAccept(RBRACE)) {
			directives.add(parseDirective());
		}
		require(RBRACE);
		return new ModuleCompilationUnit(imports, name, open, directives, annotations, docComment);
	}

	public NormalCompilationUnit parseNormalCompilationUnit() {
		var docComment = getDocComment();
		var modsAndAnnos = new ModsAndAnnotations(emptyList(), parseAnnotations());

		Optional<PackageDecl> pckg;

		if(wouldAccept(PACKAGE, Tag.NAMED)) {
			pckg = Optional.of(parsePackageDecl(docComment, modsAndAnnos.annos));
			docComment = getDocComment();
			modsAndAnnos = parseClassModsAndAnnotations();
		} else {
			pckg = Optional.empty();
			var modsAndAnnos2 = parseClassModsAndAnnotations();
			modsAndAnnos2.annos.addAll(0, modsAndAnnos.annos);
			modsAndAnnos = modsAndAnnos2;
		}

		List<ImportDecl> imports;
		if(modsAndAnnos.isEmpty()) {
			imports = parseImportSection();
			docComment = getDocComment();
			modsAndAnnos = parseClassModsAndAnnotations();
		} else {
			imports = emptyList();
		}

		return parseNormalCompilationUnit(pckg, imports, docComment, modsAndAnnos);
	}

	public NormalCompilationUnit parseNormalCompilationUnit(Optional<PackageDecl> pckg) {
		return parseNormalCompilationUnit(pckg, parseImportSection());
	}

	public NormalCompilationUnit parseNormalCompilationUnit(Optional<PackageDecl> pckg, List<ImportDecl> imports) {
		var docComment = getDocComment();
		return parseNormalCompilationUnit(pckg, imports, docComment, parseClassModsAndAnnotations());
	}

	public NormalCompilationUnit parseNormalCompilationUnit(Optional<PackageDecl> pckg, List<ImportDecl> imports,
															Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		var types = new ArrayList<TypeDecl>();
		if(!modsAndAnnos.isEmpty()) {
			types.add(parseTypeDecl(docComment, modsAndAnnos));
		}
		while(!wouldAccept(ENDMARKER)) {
			if(!accept(SEMI)) {
				types.add(parseTypeDecl());
			}
		}
		return new NormalCompilationUnit(pckg, imports, types);
	}

	public PackageDecl parsePackageDecl() {
		return parsePackageDecl(getDocComment(), emptyList());
	}

	public PackageDecl parsePackageDecl(Optional<String> docComment, List<Annotation> annotations) {
		require(PACKAGE);
		QualifiedName name = parseQualName();
		require(SEMI);
		return new PackageDecl(name, annotations, docComment);
	}

	public ArrayList<ImportDecl> parseImportSection() {
		var imports = new ArrayList<ImportDecl>();
		while(wouldAccept(IMPORT)) {
			imports.addAll(parseImport());
		}
		return imports;
	}

	public List<ImportDecl> parseImport() {
		require(IMPORT);
		boolean isStatic = accept(STATIC);
		boolean wildcard = false;
		var names = new ArrayList<Name>();
		names.add(parseName());
		while(accept(DOT)) {
			if(accept(STAR)) {
				wildcard = true;
				break;
			} else {
				names.add(parseName());
			}
		}
		require(SEMI);
		return Collections.singletonList(new ImportDecl(new QualifiedName(names), isStatic, wildcard));
	}

	public ArrayList<Annotation> parseAnnotations() {
		var annos = new ArrayList<Annotation>();
		while(wouldAccept(AT, notInterface)) {
			annos.add(parseAnnotation());
		}
		return annos;
	}
	
	public ModsAndAnnotations parseKeywordModsAndAnnotations() {
		var mods = new LinkedHashSet<Modifier>(3);
		var annos = new ArrayList<Annotation>(3);
		Token<JavaTokenType> visibilityModifier = null;
		
		for(;;) {
			if(wouldAccept(AT, not(INTERFACE))) {
				annos.add(parseAnnotation());
			} else if(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
				if(visibilityModifier != null) {
					throw syntaxError("Incompatible modifiers '" + visibilityModifier + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(visibilityModifier = token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos);
			}
		}
	}

	public ModsAndAnnotations parseFinalAndAnnotations() {
		ArrayList<Annotation> annos = parseAnnotations();
		List<Modifier> mods;
		if(wouldAccept(FINAL)) {
			mods = singletonList(createModifier(token));
			nextToken();
			if(wouldAccept(AT)) {
				annos.addAll(parseAnnotations());
			}
			if(wouldAccept(FINAL)) {
				throw syntaxError("Duplicate modifier 'final'");
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
    			throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
    		}
		} else if(wouldAccept(KEYWORD_MODIFIER)) {
			throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
		} else {
			mods = emptyList();
		}
		return new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.LOCAL_VAR));
	}

	public ModsAndAnnotations parseClassModsAndAnnotations() {
		var mods = new LinkedHashSet<Modifier>(3);
		var annos = new ArrayList<Annotation>(3);
		Token<JavaTokenType> visibilityModifier = null;
		
		for(;;) {
			if(wouldAccept(AT, not(INTERFACE))) {
				annos.add(parseAnnotation());
			} else if(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
				if(visibilityModifier != null) {
					throw syntaxError("Incompatible modifiers '" + visibilityModifier + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(visibilityModifier = token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(Tag.CLASS_MODIFIER)) {
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.CLASS));
			}
		}
	}
	
	public ModsAndAnnotations parseMethodModsAndAnnotations() {
		var mods = new LinkedHashSet<Modifier>(3);
		var annos = new ArrayList<Annotation>(3);
		Token<JavaTokenType> visibilityModifier = null;
		
		for(;;) {
			if(wouldAccept(AT, not(INTERFACE))) {
				annos.add(parseAnnotation());
			} else if(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
				if(visibilityModifier != null) {
					throw syntaxError("Incompatible modifiers '" + visibilityModifier + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(visibilityModifier = token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(Tag.METHOD_MODIFIER)) {
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.METHOD));
			}
		}
	}
	
	public ModsAndAnnotations parseConstructorModsAndAnnotations() {
		var mods = new LinkedHashSet<Modifier>(3);
		var annos = new ArrayList<Annotation>(3);
		Token<JavaTokenType> visibilityModifier = null;
		
		for(;;) {
			if(wouldAccept(AT, not(INTERFACE))) {
				annos.add(parseAnnotation());
			} else if(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
				if(visibilityModifier != null) {
					throw syntaxError("Incompatible modifiers '" + visibilityModifier + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(visibilityModifier = token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(Tag.CONSTRUCTOR_MODIFIER)) {
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.CONSTRUCTOR));
			}
		}
	}
	
	public ModsAndAnnotations parseFieldModsAndAnnotations() {
		var mods = new LinkedHashSet<Modifier>(3);
		var annos = new ArrayList<Annotation>(3);
		Token<JavaTokenType> visibilityModifier = null;
		
		for(;;) {
			if(wouldAccept(AT, not(INTERFACE))) {
				annos.add(parseAnnotation());
			} else if(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
				if(visibilityModifier != null) {
					throw syntaxError("Incompatible modifiers '" + visibilityModifier + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(visibilityModifier = token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(Tag.FIELD_MODIFIER)) {
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.FIELD));
			}
		}
	}
	
	/*public Pair<ArrayList<Modifier>, ArrayList<Annotation>> parseModsAndAnnotations() {
		ArrayList<Modifier> mods = new ArrayList<>(3);
		ArrayList<Annotation> annos = new ArrayList<>(3);
	
		for(;;) {
			if(wouldAccept(AT)) {
				annos.add(parseAnnotation());
			} else if(wouldAccept(Tag.MODIFIER)) {
				mods.add(createModifier(token));
				nextToken();
			} else if(wouldAccept(test("non"), SUB, isNonVisibilityModifier)) {
				var next1 = tokens.look(1);
				var next2 = tokens.look(2);
				if(token.getEnd().equals(next1.getStart()) && next1.getEnd().equals(next2.getStart())) {
					nextToken(2);
					var mod = "non-" + token.getString();
					mods.add(new Modifier(mod));
					nextToken();
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		return Pair.of(mods, annos);
	}*/

	public Modifier createModifier(Token<JavaTokenType> token) {
		try {
			return createModifier(token.getType());
		} catch(IllegalArgumentException e) {
			throw syntaxError(e.getMessage(), token);
		}
	}

	public Modifier createModifier(JavaTokenType type) {
		return new Modifier(switch(type) {
			case PUBLIC -> Modifiers.PUBLIC;
			case PRIVATE -> Modifiers.PRIVATE;
			case PROTECTED -> Modifiers.PROTECTED;
			case STATIC -> Modifiers.STATIC;
			case STRICTFP -> Modifiers.STRICTFP;
			case TRANSIENT -> Modifiers.TRANSIENT;
			case VOLATILE -> Modifiers.VOLATILE;
			case NATIVE -> Modifiers.NATIVE;
			case FINAL -> Modifiers.FINAL;
			case SYNCHRONIZED -> Modifiers.SYNCHRONIZED;
			case DEFAULT -> Modifiers.DEFAULT;
			default -> throw new IllegalArgumentException(type + " is not a modifier");
		});
	}

	public Modifier createModifier(String name) {
		return new Modifier(Modifiers.fromString(name));
	}

	public Annotation parseAnnotation() {
		require(AT);
		Optional<Either<? extends List<AnnotationArgument>,? extends AnnotationValue>> arguments;
		var type = new GenericType(parseQualName());
		if(accept(LPAREN)) {
			if(wouldAccept(RPAREN)) {
				arguments = Optional.of(Either.first(emptyList()));
			} else if(wouldAccept(NAME, EQ)) {
				var args = new ArrayList<AnnotationArgument>();
				do {
					args.add(parseAnnotationArgument());
				} while(accept(COMMA));
				arguments = Optional.of(Either.first(args));
			} else {
				arguments = Optional.of(Either.second(parseAnnotationValue()));
			}
			require(RPAREN);
		} else {
			arguments = Optional.empty();
		}
		return new Annotation(type, arguments);
	}

	public AnnotationArgument parseAnnotationArgument() {
		var name = parseName();
		require(EQ);
		var value = parseAnnotationValue();
		return new AnnotationArgument(name, value);
	}

	public AnnotationValue parseAnnotationValue() {
		if(wouldAccept(LBRACE)) {
			return parseArrayInitializer(this::parseAnnotationValue);
		} else if(wouldAccept(AT)) {
			return parseAnnotation();
		} else {
			return parseExpression();
		}
	}

	public <T extends AnnotationValue> ArrayInitializer<? extends T> parseArrayInitializer(Supplier<? extends T> elementParser) {
		require(LBRACE);
		var elems = new ArrayList<T>();
		if(!wouldAccept(RBRACE)) {
			elems.add(elementParser.get());
			while(accept(COMMA)) {
				if(wouldAccept(RBRACE)) {
					break;
				} else {
					elems.add(elementParser.get());
				}
			}
		}
		require(RBRACE);
		return new ArrayInitializer<>(elems);
	}

	public Directive parseDirective() {
		if(wouldAccept(REQUIRES)) {
			return parseRequiresDirective();
		} else if(wouldAccept(EXPORTS)) {
			return parseExportsDirective();
		} else if(wouldAccept(OPENS)) {
			return parseOpensDirective();
		} else if(wouldAccept(USES)) {
			return parseUsesDirective();
		} else if(wouldAccept(PROVIDES)) {
			return parseProvidesDirective();
		} else {
			throw syntaxError("expected 'requires', 'exports', 'opens', 'uses', or 'provides' here, got " + token);
		}
	}

	public RequiresDirective parseRequiresDirective() {
		require(REQUIRES);
		var modifiers = new ArrayList<Modifier>();
		while(wouldAccept(Tag.REQUIRES_MODIFIER)) {
			if(wouldAccept(Tag.NAMED, DOT.or(SEMI))) { // case where it's provides ... transitive.blah; in which case
													   // transitive is not a modifier
				break;
			}
			modifiers.add(createModifier(token));
			nextToken();
		}
		var name = parseQualName();
		require(SEMI);
		return new RequiresDirective(name, modifiers);
	}

	public ExportsDirective parseExportsDirective() {
		require(EXPORTS);
		var name = parseQualName();
		if(accept(TO)) {
			var friends = listOf(this::parseQualName);
			require(SEMI);
			return new ExportsDirective(name, friends);
		} else {
			require(SEMI);
			return new ExportsDirective(name);
		}
	}

	public OpensDirective parseOpensDirective() {
		require(OPENS);
		var name = parseQualName();
		if(accept(TO)) {
			var friends = listOf(this::parseQualName);
			require(SEMI);
			return new OpensDirective(name, friends);
		} else {
			require(SEMI);
			return new OpensDirective(name);
		}
	}

	public UsesDirective parseUsesDirective() {
		require(USES);
		var name = parseQualTypeName();
		require(SEMI);
		return new UsesDirective(name);
	}

	public ProvidesDirective parseProvidesDirective() {
		require(PROVIDES);
		var name = parseQualTypeName();
		require(WITH);
		var providers = listOf(this::parseQualTypeName);
		require(SEMI);
		return new ProvidesDirective(name, providers);
	}

	public TypeDecl parseTypeDecl() {
		var docComment = getDocComment();
		return parseTypeDecl(docComment, parseClassModsAndAnnotations());
	}

	public TypeDecl parseTypeDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(wouldAccept(CLASS)) {
			return parseClassDecl(docComment, modsAndAnnos);
		} else if(wouldAccept(INTERFACE)) {
			return parseInterfaceDecl(docComment, modsAndAnnos);
		} else if(wouldAccept(ENUM)) {
			return parseEnumDecl(docComment, modsAndAnnos);
		} else if(wouldAccept(AT, INTERFACE)) {
			return parseAnnotationDecl(docComment, modsAndAnnos);
		} else {
			throw syntaxError("expected 'class', 'interface', '@interface', or 'enum' here, got " + token);
		}
	}

	public ClassDecl parseClassDecl() {
		var docComment = getDocComment();
		return parseClassDecl(docComment, parseClassModsAndAnnotations());
	}

	public ClassDecl parseClassDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeClassMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		require(CLASS);
		var name = parseTypeName();
		var typeParameters = parseTypeParametersOpt();
		Optional<GenericType> superClass;
		if(accept(EXTENDS)) {
			superClass = Optional.of(parseGenericType());
		} else {
			superClass = Optional.empty();
		}
		List<GenericType> interfaces;
		if(accept(IMPLEMENTS)) {
			interfaces = parseGenericTypeList();
		} else {
			interfaces = emptyList();
		}
		List<Member> members;
		try(var $ = typeNames.enter(name)) {
			members = parseClassBody(() -> this.parseClassMember(false));
		}
		return new ClassDecl(name, typeParameters, superClass, interfaces, members, modifiers, annotations, docComment);
	}

	public InterfaceDecl parseInterfaceDecl() {
		var docComment = getDocComment();
		return parseInterfaceDecl(docComment, parseClassModsAndAnnotations());
	}

	public InterfaceDecl parseInterfaceDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeClassMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		require(INTERFACE);
		var name = parseTypeName();
		var typeParameters = parseTypeParametersOpt();
		List<GenericType> superInterfaces;
		if(accept(EXTENDS)) {
			superInterfaces = parseGenericTypeList();
		} else {
			superInterfaces = emptyList();
		}
		List<Member> members;
		try(var $ = typeNames.enter(name)) {
			members = parseClassBody(() -> this.parseClassMember(true));
		}
		return new InterfaceDecl(name, typeParameters, superInterfaces, members, modifiers, annotations, docComment);
	}

	public EnumDecl parseEnumDecl() {
		var docComment = getDocComment();
		return parseEnumDecl(docComment, parseClassModsAndAnnotations());
	}

	public EnumDecl parseEnumDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeClassMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		require(ENUM);
		var name = parseTypeName();
		List<GenericType> interfaces;
		if(accept(IMPLEMENTS)) {
			interfaces = parseGenericTypeList();
		} else {
			interfaces = emptyList();
		}
		List<EnumField> fields;
		List<Member> members;
		try(var $ = typeNames.enter(name)) {
			var fieldsAndMembers = parseEnumBody();
			fields = fieldsAndMembers.getLeft();
			members = fieldsAndMembers.getRight();
		}
		return new EnumDecl(name, interfaces, fields, members, modifiers, annotations, docComment);
	}

	public Pair<List<EnumField>,List<Member>> parseEnumBody() {
		require(LBRACE);
		List<EnumField> fields;
		List<Member> members;
		if(wouldAccept(AT.or(Tag.NAMED))) {
			fields = new ArrayList<>();
			fields.add(parseEnumField());
			while(accept(DOT)) {
				if(wouldAccept(SEMI.or(RBRACE))) {
					break;
				}
				fields.add(parseEnumField());
			}
		} else {
			fields = emptyList();
		}
		if(accept(SEMI)) {
			members = new ArrayList<>();
			while(wouldNotAccept(RBRACE)) {
				if(!accept(SEMI)) {
					members.addAll(parseClassMember(false));
				}
			}
		} else {
			members = emptyList();
		}
		require(RBRACE);
		return Pair.of(fields, members);
	}

	public EnumField parseEnumField() {
		return parseEnumField(getDocComment(), parseAnnotations());
	}

	public EnumField parseEnumField(Optional<String> docComment, List<Annotation> annotations) {
		var name = parseName();
		Optional<? extends List<? extends Expression>> arguments;
		if(wouldAccept(LPAREN)) {
			arguments = Optional.of(parseArguments(true));
		} else {
			arguments = Optional.empty();
		}
		Optional<? extends List<Member>> members;
		if(wouldAccept(LBRACE)) {
			members = Optional.of(parseClassBody(() -> this.parseClassMember(false)));
		} else {
			members = Optional.empty();
		}
		return new EnumField(name, arguments, members, annotations, docComment);
	}

	public AnnotationDecl parseAnnotationDecl() {
		var docComment = getDocComment();
		return parseAnnotationDecl(docComment, parseClassModsAndAnnotations());
	}

	public AnnotationDecl parseAnnotationDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeClassMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		require(AT, INTERFACE);
		var name = parseTypeName();
		List<Member> members;
		try(var $ = typeNames.enter(name)) {
			members = parseClassBody(this::parseAnnotationMember);
		}
		return new AnnotationDecl(name, members, modifiers, annotations, docComment);
	}

	public List<Member> parseAnnotationMember() {
		var docComment = getDocComment();
		/*var modifiers = new LinkedHashSet<Modifier>(3);
		var annotations = parseAnnotations();
		while(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
			if(!modifiers.add(createModifier(token))) {
				throw syntaxError("Duplicate modifier '" + token.getString() + "'");
			}
			nextToken();
			if(wouldAccept(AT)) {
				annotations.addAll(parseAnnotations());
			}
		}
		if(wouldAccept(STATIC)) {
			
		} else {
			if(wouldAccept(ABSTRACT)) {
				modifiers.add(createModifier(token));
				nextToken();
				if(wouldAccept(AT)) {
					annotations.addAll(parseAnnotations());
				}
			}
			if(wouldAccept(Tag.NAMED, LPAREN)) {
				return List.of(parseAnnotationProperty(docComment, new ModsAndAnnotations(new ArrayList<>(modifiers), annotations)));
			} else {
				
			}
		}*/
		return parseAnnotationMember(docComment, parseKeywordModsAndAnnotations());
	}
	
	public List<Member> parseAnnotationMember(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(wouldAccept(CLASS.or(INTERFACE).or(ENUM)) || wouldAccept(AT, INTERFACE)) {
			return List.of(parseTypeDecl(docComment, modsAndAnnos));
		} else if(modsAndAnnos.hasModifier("static")) {
			return parseInterfaceMember(false, docComment, modsAndAnnos);
		} else {
			return List.of(parseAnnotationProperty(docComment, modsAndAnnos));
		}
	}

	public AnnotationProperty parseAnnotationProperty() {
		var docComment = getDocComment();
		return parseAnnotationProperty(docComment, parseMethodModsAndAnnotations());
	}

	public AnnotationProperty parseAnnotationProperty(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeMethodMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var type = parseType();
		var name = parseName();
		require(LPAREN, RPAREN);
		Optional<? extends AnnotationValue> defaultValue;
		if(accept(DEFAULT)) {
			defaultValue = Optional.of(parseAnnotationValue());
		} else {
			defaultValue = Optional.empty();
		}
		endStatement();
		return new AnnotationProperty(name, type, defaultValue, modifiers, annotations, docComment);
	}

	public List<TypeParameter> parseTypeParametersOpt() {
		return wouldAccept(LT)? parseTypeParameters() : emptyList();
	}

	public List<TypeParameter> parseTypeParameters() {
		require(LT);
		var params = listOf(this::parseTypeParameter);
		require(GT);
		return params;
	}

	public TypeParameter parseTypeParameter() {
		var annotations = parseAnnotations();
		var name = parseTypeName();
		Optional<? extends ReferenceType> bound;
		if(accept(EXTENDS)) {
			bound = Optional.of(parseTypeIntersection());
		} else {
			bound = Optional.empty();
		}
		return new TypeParameter(name, bound, annotations);
	}

	public <M extends Member> ArrayList<M> parseClassBody(Supplier<? extends List<M>> memberParser) {
		require(LBRACE);
		var members = new ArrayList<M>();
		while(wouldNotAccept(RBRACE)) {
			if(!accept(SEMI)) {
				members.addAll(memberParser.get());
			}
		}
		require(RBRACE);
		return members;
	}

	public List<Member> parseClassMember() {
		return parseClassMember(false);
	}

	public List<Member> parseClassMember(boolean inInterface) {
		if(wouldAccept(STATIC, LBRACE)) {
			nextToken();
			var body = parseBlock();
			return List.of(new ClassInitializer(true, body));
		} else if(wouldAccept(LBRACE)) {
			var body = parseBlock();
			return List.of(new ClassInitializer(false, body));
		} else {
			var docComment = getDocComment();
			/*var modifiers = new LinkedHashSet<Modifier>();
			var annotations = parseAnnotations();
			while(wouldAccept(Tag.VISIBILITY_MODIFIER)) {
				if(!modifiers.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
				if(wouldAccept(AT)) {
					annotations.addAll(parseAnnotations());
				}
			}
			if(!inInterface && wouldAccept(Tag.NAMED, LPAREN)) {
				return parseConstructor(docComment, new ModsAndAnnotations(new ArrayList<>(modifiers), annotations));
			}
			while(wouldAccept(STATIC.or(FINAL))) {
				if(!modifiers.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
				if(wouldAccept(AT)) {
					annotations.addAll(parseAnnotations());
				}
			}
			if(wouldAccept(TRANSIENT.or(VOLATILE))) {
				do {
					if(!modifiers.add(createModifier(token))) {
						throw syntaxError("Duplicate modifier '" + token.getString() + "'");
					}
					nextToken();
					if(wouldAccept(AT)) {
						annotations.addAll(parseAnnotations());
					}
				} while(wouldAccept(Tag.FIELD_MODIFIER));
				if(wouldAccept(KEYWORD_MODIFIERS)) {
					throw syntaxError("Illegal field modifier");
				}
				return parseFieldDecl(docComment, new ModsAndAnnotations(new ArrayList<>(modifiers), annotations));
			}
			if(wouldAccept(ABSTRACT)) {
				do {
					if(!modifiers.add(createModifier(token))) {
						throw syntaxError("Duplicate modifier '" + token.getString() + "'");
					}
					nextToken();
					if(wouldAccept(AT)) {
						annotations.addAll(parseAnnotations());
					}
				} while(wouldAccept(Tag.CLASS_MODIFIER.and(Tag.METHOD_MODIFIER)));
				if(wouldAccept(Tag.METHOD_MODIFIER)) {
					do {
						if(!modifiers.add(createModifier(token))) {
							throw syntaxError("Duplicate modifier '" + token.getString() + "'");
						}
						nextToken();
						if(wouldAccept(AT)) {
							annotations.addAll(parseAnnotations());
						}
					} while(wouldAccept(Tag.METHOD_MODIFIER));
					if(wouldAccept(KEYWORD_MODIFIERS)) {
						throw syntaxError("Illegal field modifier");
					}
					return parseMethod(inInterface, docComment, new ModsAndAnnotations(new ArrayList<>(modifiers), annotations));
				} else if(accept(VOID)) {
					return parseMethod(inInterface, new VoidType(), emptyList(), docComment, new ModsAndAnnotations(new ArrayList<>(modifiers), annotations));
				} else if()
				} else {
					while(wouldAccept(Tag.METHOD_MODIFIER)) {
						if(!modifiers.add(createModifier(token))) {
							throw syntaxError("Duplicate modifier '" + token.getString() + "'");
						}
						nextToken();
						if(wouldAccept(AT)) {
							annotations.addAll(parseAnnotations());
						}
					}
					if(wouldAccept(KEYWORD_MODIFIERS)) {
						throw syntaxError("Illegal field modifier");
					}
					
				}
			}*/
			return parseClassMember(inInterface, docComment, parseKeywordModsAndAnnotations());
		}
	}

	public List<Member> parseClassMember(boolean inInterface, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(!inInterface && modsAndAnnos.canBeConstructorMods() && wouldAccept(Tag.NAMED, LPAREN)) {
			return parseConstructor(docComment, modsAndAnnos);
		} else if(modsAndAnnos.canBeMethodMods() && wouldAccept(LT)) {
			var typeParameters = parseTypeParameters();
			if(!inInterface && modsAndAnnos.canBeConstructorMods() && wouldAccept(Tag.NAMED, LPAREN)) {
				return parseConstructor(typeParameters, docComment, modsAndAnnos);
			} else {
				var typeAnnotations = parseAnnotations();
				Type type;
				if(accept(VOID)) {
					type = new VoidType(typeAnnotations);
				} else {
					type = parseType(typeAnnotations);
				}
				return parseMethod(inInterface, type, typeParameters, parseName(), docComment, modsAndAnnos);
			}
		} else {
			return parseInterfaceMember(inInterface, docComment, modsAndAnnos);
		}
	}

	public List<Member> parseInterfaceMember() {
		return parseInterfaceMember(true);
	}

	public List<Member> parseInterfaceMember(boolean inInterface) {
		var docComment = getDocComment();
		return parseInterfaceMember(inInterface, docComment, parseKeywordModsAndAnnotations());
	}

	public List<Member> parseInterfaceMember(boolean inInterface, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(modsAndAnnos.canBeClassMods() && wouldAccept(CLASS.or(INTERFACE).or(ENUM).or(AT /* only way AT is possible here is if it is followed by INTERFACE*/))) {
			return List.of(parseTypeDecl(docComment, modsAndAnnos));
		} else if(modsAndAnnos.canBeMethodMods() && wouldAccept(LT)) {
			var typeParameters = parseTypeParameters();
			var typeAnnotations = parseAnnotations();
			Type type;
			if(accept(VOID)) {
				type = new VoidType(typeAnnotations);
			} else {
				type = parseType(typeAnnotations);
			}
			return parseMethod(inInterface, type, typeParameters, parseName(), docComment, modsAndAnnos);
		} else if(modsAndAnnos.canBeMethodMods() && accept(VOID)) {
			return parseMethod(inInterface, new VoidType(), emptyList(), parseName(), docComment, modsAndAnnos);
		} else {
			var type = parseType();
			if(modsAndAnnos.canBeMethodMods() && wouldAccept(Tag.NAMED, LPAREN)) {
				return parseMethod(inInterface, type, emptyList(), parseName(), docComment, modsAndAnnos);
			} else if(modsAndAnnos.canBeFieldMods()) {
				return parseFieldDecl(type, docComment, modsAndAnnos);
			} else {
				throw syntaxError("Invalid modifiers");
			}
		}
	}

	public List<Member> parseFieldDecl() {
		var docComment = getDocComment();
		return parseFieldDecl(docComment, parseFieldModsAndAnnotations());
	}

	public List<Member> parseFieldDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return parseFieldDecl(parseType(), docComment, modsAndAnnos);
	}

	public List<Member> parseFieldDecl(Type type, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return List.of(parseVariableDecl(type, docComment, modsAndAnnos));
	}

	public List<Member> parseMethod() {
		return parseMethod(false);
	}

	public List<Member> parseMethod(boolean inInterface) {
		var docComment = getDocComment();
		return parseMethod(inInterface, docComment, parseMethodModsAndAnnotations());
	}

	public List<Member> parseMethod(boolean inInterface, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return parseMethod(inInterface, parseTypeParametersOpt(), docComment, modsAndAnnos);
	}

	public List<Member> parseMethod(boolean inInterface, List<TypeParameter> typeParameters,
										  Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		var typeAnnotations = parseAnnotations();
		Type type;
		if(accept(VOID)) {
			type = new VoidType(typeAnnotations);
		} else {
			type = parseType(typeAnnotations);
		}
		return parseMethod(inInterface, type, typeParameters, docComment, modsAndAnnos);
	}

	public List<Member> parseMethod(boolean inInterface, Type returnType, List<TypeParameter> typeParameters,
										  Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return parseMethod(inInterface, returnType, typeParameters, parseName(), docComment, modsAndAnnos);
	}

	public List<Member> parseMethod(boolean inInterface, Type returnType, List<TypeParameter> typeParameters,
										  Name name, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeMethodMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var thisParameterAndParameters = parseParameters();
		var thisParameter = thisParameterAndParameters.getLeft();
		var parameters = thisParameterAndParameters.getRight();
		List<Dimension> dimensions = returnType instanceof VoidType? emptyList() : parseDimensions();
		List<GenericType> exceptions;
		if(accept(THROWS)) {
			exceptions = parseGenericTypeList();
		} else {
			exceptions = emptyList();
		}
		Optional<Block> body = parseMethodBody(returnType instanceof VoidType, parameters);
		return List.of(new FunctionDecl(name, typeParameters, returnType, thisParameter, parameters, dimensions,
				exceptions, body, modifiers, annotations, docComment));
	}

	public List<Member> parseConstructor() {
		var docComment = getDocComment();
		return parseConstructor(docComment, parseConstructorModsAndAnnotations());
	}

	public List<Member> parseConstructor(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return parseConstructor(parseTypeParametersOpt(), docComment, modsAndAnnos);
	}

	public List<Member> parseConstructor(List<TypeParameter> typeParameters, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return parseConstructor(parseTypeName(), typeParameters, docComment, modsAndAnnos);
	}

	public List<Member> parseConstructor(Name name, List<TypeParameter> typeParameters,
												  Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeConstructorMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var thisParameterAndParameters = parseParameters();
		var thisParameter = thisParameterAndParameters.getLeft();
		var parameters = thisParameterAndParameters.getRight();
		List<GenericType> exceptions;
		if(accept(THROWS)) {
			exceptions = parseGenericTypeList();
		} else {
			exceptions = emptyList();
		}
		Block body = parseConstructorBody(parameters);
		return List.of(new ConstructorDecl(name, typeParameters, thisParameter, parameters, exceptions, body, modifiers,
				annotations, docComment));
	}

	public Block parseConstructorBody(List<FormalParameter> parameters) {
		return parseBlock();
	}

	public Optional<Block> parseMethodBody(boolean isVoidMethod, List<FormalParameter> parameters) {
		if(accept(SEMI)) {
			return Optional.empty();
		} else {
			return Optional.of(parseBlock());
		}
	}

	public Pair<Optional<ThisParameter>,List<FormalParameter>> parseParameters() {
		require(LPAREN);
		Optional<ThisParameter> thisParameter;
		List<FormalParameter> parameters;
		if(wouldAccept(RPAREN)) {
			thisParameter = Optional.empty();
			parameters = emptyList();
		} else {
			try(var state = tokens.enter()) {
				try {
					thisParameter = Optional.of(parseThisParameter());
				} catch(SyntaxError e) {
					state.reset();
					thisParameter = Optional.empty();
				}
			}
			if(thisParameter.isPresent()) {
				if(accept(COMMA)) {
					parameters = parseFormalParameterList();
				} else {
					parameters = emptyList();
				}
			} else {
				parameters = parseFormalParameterList();
			}
		}
		require(RPAREN);
		return Pair.of(thisParameter, parameters);
	}

	public ArrayList<FormalParameter> parseFormalParameterList() {
		var params = new ArrayList<FormalParameter>();
		var param = parseFormalParameter();
		params.add(param);
		while(!param.isVariadic() && accept(COMMA)) {
			params.add(param = parseFormalParameter());
		}
		return params;
	}

	public FormalParameter parseFormalParameter() {
		return parseFormalParameter(parseFinalAndAnnotations());
	}

	public FormalParameter parseFormalParameter(ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeLocalVarMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var type = parseType();
		boolean variadic = accept(ELLIPSIS);
		var name = parseName();
		var dimensions = parseDimensions();
		return new FormalParameter(type, name, variadic, dimensions, modifiers, annotations);
	}

	public ThisParameter parseThisParameter() {
		return parseThisParameter(parseAnnotations());
	}

	public ThisParameter parseThisParameter(List<Annotation> annotations) {
		var type = parseGenericType();
		Optional<Name> qualifier;
		if(wouldAccept(Tag.NAMED)) {
			qualifier = Optional.of(parseName());
			require(DOT);
		} else {
			qualifier = Optional.empty();
		}
		require(THIS);
		return new ThisParameter(type, qualifier, annotations);
	}

	public VariableDecl parseVariableDecl() {
		var docComment = getDocComment();
		return parseVariableDecl(docComment, parseFinalAndAnnotations());
	}

	public VariableDecl parseVariableDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		return parseVariableDecl(parseType(), docComment, modsAndAnnos);
	}

	public VariableDecl parseVariableDecl(Type type, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeFieldMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var declarators = listOf(type instanceof ArrayType? () -> parseVariableDeclarator(true) : () -> parseVariableDeclarator(false));
		endStatement();
		return new VariableDecl(type, declarators, modifiers, annotations, docComment);
	}

	public VariableDeclarator parseVariableDeclarator(boolean isArray) {
		var name = parseName();
		var dimensions = parseDimensions();
		return parseVariableDeclarator(isArray, name, dimensions);
	}

	public VariableDeclarator parseVariableDeclarator(boolean isArray, Name name, List<Dimension> dimensions) {
		Optional<? extends Initializer> initializer;
		if(accept(EQ)) {
			initializer = Optional.of(parseInitializer(isArray || !dimensions.isEmpty()));
		} else {
			initializer = Optional.empty();
		}
		return new VariableDeclarator(name, dimensions, initializer);
	}

	public List<Dimension> parseDimensions() {
		var dimensions = new ArrayList<Dimension>();
		while(wouldAccept(AT.or(LBRACKET))) {
			dimensions.add(parseDimension());
		}
		return dimensions;
	}

	public Dimension parseDimension() {
		var annotations = parseAnnotations();
		require(LBRACKET, RBRACKET);
		return new Dimension(annotations);
	}

	public ArrayList<GenericType> parseGenericTypeList() {
		return listOf(this::parseGenericType);
	}

	public Type parseType() {
		return parseType(parseAnnotations());
	}

	protected static final TokenPredicate<JavaTokenType> PRIMITIVE_TYPES = (Token<JavaTokenType> token) -> switch(token.getType()) {
		case BOOLEAN, BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE -> true;
		default -> false;
	};
	
	protected static final TokenPredicate<JavaTokenType> KEYWORD_MODIFIER = (Token<JavaTokenType> token) -> {
		var type = token.getType();
		return type.isKeyword() && 
				(type.hasTag(Tag.CLASS_MODIFIER) 
				|| type.hasTag(Tag.METHOD_MODIFIER)
				|| type.hasTag(Tag.CONSTRUCTOR_MODIFIER) 
				|| type.hasTag(Tag.FIELD_MODIFIER)
				|| type.hasTag(Tag.LOCAL_VAR_MODIFIER));
	};

	public Type parseType(List<Annotation> annotations) {
		var base = parseNonArrayType(annotations);
		if(wouldAccept(AT.or(LBRACKET))) {
			var dimensions = parseDimensions();
			base.setAnnotations(emptyList());
			return new ArrayType(base, dimensions, annotations);
		} else {
			return base;
		}
	}

	public Type parseNonArrayType() {
		return parseNonArrayType(parseAnnotations());
	}

	public Type parseNonArrayType(List<Annotation> annotations) {
		if(wouldAccept(PRIMITIVE_TYPES)) {
			var name = token.getString();
			nextToken();
			return new PrimitiveType(name, annotations);
		} else {
			return parseGenericType(annotations);
		}
	}

	public ReferenceType parseReferenceType() {
		return parseReferenceType(parseAnnotations());
	}

	public ReferenceType parseReferenceType(List<Annotation> annotations) {
		var base = parseNonArrayType(annotations);
		if(base instanceof PrimitiveType) {
			base.setAnnotations(emptyList());
			var dimension = parseDimension();
			var dimensions = parseDimensions();
			dimensions.add(0, dimension);
			return new ArrayType(base, dimensions, annotations);
		} else if(wouldAccept(AT.or(LBRACKET))) {
			var dimensions = parseDimensions();
			base.setAnnotations(emptyList());
			return new ArrayType(base, dimensions, annotations);
		} else {
			return (GenericType)base;
		}
	}

	public GenericType parseGenericType() {
		return parseGenericType(parseAnnotations());
	}

	public GenericType parseGenericType(List<Annotation> annotations) {
		var name = parseQualTypeName();
		var typeArgs = parseTypeArgumentsOpt();
		if(wouldAccept(DOT, Tag.NAMED)) {
			nextToken();
			var result = new GenericType(name, typeArgs);
			do {
				name = parseQualTypeName();
				typeArgs = parseTypeArgumentsOpt();
				result = new GenericType(name, typeArgs, result);
			} while(accept(DOT));
			result.setAnnotations(annotations);
			return result;
		} else {
			return new GenericType(name, typeArgs, annotations);
		}
	}

	public ReferenceType parseTypeIntersection() {
		return parseTypeIntersection(parseAnnotations());
	}

	public ReferenceType parseTypeIntersection(List<Annotation> annotations) {
		var type = parseReferenceType(annotations);
		if(accept(AMP)) {
			type.setAnnotations(emptyList());
			var types = listOf(AMP, this::parseReferenceType);
			types.add(0, type);
			return new TypeIntersection(types, annotations);
		} else {
			return type;
		}
	}

	public ReferenceType parseTypeUnion() {
		return parseTypeUnion(parseAnnotations());
	}

	public ReferenceType parseTypeUnion(List<Annotation> annotations) {
		var type = parseReferenceType(annotations);
		if(accept(BAR)) {
			type.setAnnotations(emptyList());
			var types = listOf(BAR, this::parseReferenceType);
			types.add(0, type);
			return new TypeUnion(types, annotations);
		} else {
			return type;
		}
	}

	public List<? extends TypeArgument> parseTypeArgumentsOpt() {
		return wouldAccept(LT, not(GT))? parseTypeArguments() : emptyList();
	}

	public List<? extends TypeArgument> parseTypeArguments() {
		require(LT);
		var args = listOf(this::parseTypeArgument);
		require(GT);
		return args;
	}

	public TypeArgument parseTypeArgument() {
		return parseTypeArgument(parseAnnotations());
	}

	public TypeArgument parseTypeArgument(List<Annotation> annotations) {
		return wouldAccept(QUES)? parseWildcardTypeArgument(annotations) : parseReferenceType(annotations);
	}

	public WildcardTypeArgument parseWildcardTypeArgument() {
		return parseWildcardTypeArgument(parseAnnotations());
	}

	public WildcardTypeArgument parseWildcardTypeArgument(List<Annotation> annotations) {
		require(QUES);
		Optional<WildcardTypeArgument.Bound> bound;
		if(wouldAccept(SUPER.or(EXTENDS))) {
			WildcardTypeArgument.Bound.Kind kind;
			if(accept(SUPER)) {
				kind = WildcardTypeArgument.Bound.Kind.SUPER;
			} else {
				require(EXTENDS);
				kind = WildcardTypeArgument.Bound.Kind.EXTENDS;
			}
			var type = parseTypeIntersection();
			bound = Optional.of(new WildcardTypeArgument.Bound(kind, type));
		} else {
			bound = Optional.empty();
		}
		return new WildcardTypeArgument(bound, annotations);
	}

	public EmptyStmt parseEmptyStmt() {
		require(SEMI);
		return new EmptyStmt();
	}

	public Block parseBlock() {
		try(var $ = preStmts.enter()) {
			require(LBRACE);
			var stmts = new ArrayList<Statement>();
			while(wouldNotAccept(RBRACE)) {
				stmts.add(parseBlockStatement());
			}
			require(RBRACE);
			return preStmts.apply(new Block(stmts));
		}
	}

	public Statement parseBlockStatement() {
		if(wouldAccept(AT)) {
			var docComment = getDocComment();
			var mods = new ArrayList<Modifier>();
			var annos = parseAnnotations();
			while(wouldAccept(Tag.LOCAL_VAR_MODIFIER)) {
				mods.add(createModifier(token));
				nextToken();
				if(wouldAccept(AT)) {
					annos.addAll(parseAnnotations());
				}
			}
			if(wouldAccept(Tag.CLASS_MODIFIER)) {
				do {
					mods.add(createModifier(token));
					nextToken();
					if(wouldAccept(AT)) {
						annos.addAll(parseAnnotations());
					}
				} while(wouldAccept(Tag.CLASS_MODIFIER));
				try(var $ = preStmts.enter()) {
					return preStmts.apply(parseClassDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.CLASS))));
				}
			} else if(wouldAccept(CLASS)) {
				try(var $ = preStmts.enter()) {
					return preStmts.apply(parseClassDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.CLASS))));
				}
			} else {
				try(var $ = preStmts.enter()) {
					return preStmts.apply(parseVariableDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.LOCAL_VAR))));
				}
			}
		}
		if(wouldAccept(Tag.LOCAL_VAR_MODIFIER)) {
			var docComment = getDocComment();
			var mods = new ArrayList<Modifier>();
			var annos = new ArrayList<Annotation>();
			while(wouldAccept(Tag.LOCAL_VAR_MODIFIER)) {
				mods.add(createModifier(token));
				nextToken();
				if(wouldAccept(AT)) {
					annos.addAll(parseAnnotations());
				}
			}
			if(wouldAccept(Tag.CLASS_MODIFIER)) {
				do {
					mods.add(createModifier(token));
					nextToken();
					if(wouldAccept(AT)) {
						annos.addAll(parseAnnotations());
					}
				} while(wouldAccept(Tag.CLASS_MODIFIER));
				try(var $ = preStmts.enter()) {
					return preStmts.apply(parseClassDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.CLASS))));
				}
			} else if(wouldAccept(CLASS)) {
				try(var $ = preStmts.enter()) {
					return preStmts.apply(parseClassDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.CLASS))));
				}
			} else {
				try(var $ = preStmts.enter()) {
					return preStmts.apply(parseVariableDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.LOCAL_VAR))));
				}
			}
		}
		if(wouldAccept(Tag.CLASS_MODIFIER)) {
			var docComment = getDocComment();
			var mods = new ArrayList<Modifier>();
			var annos = new ArrayList<Annotation>();
			do {
				mods.add(createModifier(token));
				nextToken();
				if(wouldAccept(AT)) {
					annos.addAll(parseAnnotations());
				}
			} while(wouldAccept(Tag.CLASS_MODIFIER));
			try(var $ = preStmts.enter()) {
				return preStmts.apply(parseClassDecl(docComment, new ModsAndAnnotations(mods, annos, EnumSet.of(ModsAndAnnotations.Type.CLASS))));
			}
		}
		vardecl: if(wouldAccept(Tag.NAMED.or(Tag.PRIMITIVE_TYPE))) {
			var docComment = getDocComment();
			Type type;
			try(var $ = preStmts.enter(); var state = tokens.enter()) {
				try {
					type = parseType();
				} catch(SyntaxError e) {
					state.reset();
					break vardecl;
				}
				if(!wouldAccept(Tag.NAMED)) {
					state.reset();
					break vardecl;
				}
				return preStmts.apply(parseVariableDecl(type, docComment, new ModsAndAnnotations()));
			}
		}
		return parseStatement();
	}

	public Statement parseStatement() {
		try(var $ = preStmts.enter()) {
			return preStmts.apply(switch(token.getType()) {
				case IF -> parseIfStmt();
				case DO -> parseDoStmt();
				case FOR -> parseForStmt();
				case WHILE -> parseWhileStmt();
				case SYNCHRONIZED -> parseSynchronizedStmt();
				case TRY -> parseTryStmt();
				case SWITCH -> parseSwitch();
				case RETURN -> parseReturnStmt();
				case BREAK -> parseBreakStmt();
				case CONTINUE -> parseContinueStmt();
				case YIELD -> parseYieldStmt();
				case THROW -> parseThrowStmt();
				case ASSERT -> parseAssertStmt();
				case LBRACE -> parseBlock();
				case SEMI -> parseEmptyStmt();
				case LT -> {
					var typeArguments = parseTypeArguments();
					ConstructorCall.Type type;
					if(accept(SUPER)) {
						type = ConstructorCall.Type.SUPER;
					} else {
						require(THIS);
						type = ConstructorCall.Type.THIS;
					}
					var args = parseConstructorArguments();
					endStatement();
					break new ConstructorCall(typeArguments, type, args);
				}
				case THIS -> {
					if(wouldAccept(THIS, LPAREN)) {
						nextToken();
						var args = parseConstructorArguments();
						endStatement();
						break new ConstructorCall(ConstructorCall.Type.THIS, args);
					} else {
						break statementDefault();
					}
				}
				case SUPER -> {
					assert tokens.look(0).equals(token);
					assert !tokens.look(1).equals(token);
					if(wouldAccept(SUPER, LPAREN)) {
						nextToken();
						var args = parseConstructorArguments();
						endStatement();
						break new ConstructorCall(ConstructorCall.Type.SUPER, args);
					} else {
						break statementDefault();
					}
				}
				case ELSE -> throw syntaxError("'else' without 'if'");
				case CATCH -> throw syntaxError("'catch' without 'try'");
				case FINALLY -> throw syntaxError("'finally' without 'try'");
				case CASE -> throw syntaxError("'case' without 'switch'");
				case DEFAULT -> throw syntaxError("'default' without 'switch'");
				default -> statementDefault();
			});
		}
	}

	protected Statement statementDefault() {
		if(wouldAccept(NAME, COLON)) {
			return parseLabeledStmt();
		} else {
			call: try(var state = tokens.enter()) {
				Expression object;
				try {
					object = parseSuffix();
					require(DOT);
				} catch(SyntaxError e) {
					state.reset();
					break call;
				}
				List<? extends TypeArgument> typeArguments;
				if(wouldAccept(LT)) {
					typeArguments = parseTypeArguments();
				} else {
					typeArguments = emptyList();
				}
				if(!accept(SUPER)) {
					state.reset();
					break call;
				}
				if(wouldAccept(LPAREN)) {
					var args = parseConstructorArguments();
					endStatement();
					return new ConstructorCall(object, typeArguments, ConstructorCall.Type.SUPER, args);
				} else {
					state.reset();
					break call;
				}
			}
			return parseExpressionStmt();
		}
	}

	public void endStatement() {
		if(tokens.look(-1).getType() == RBRACE) {
			accept(SEMI);
		} else {
			require(SEMI);
		}
	}

	public Expression parseCondition() {
		require(LPAREN);
		var expr = parseExpression();
		require(RPAREN);
		return expr;
	}

	public Block parseBodyAsBlock() {
		return parseBlock();
	}

	public ExpressionStmt parseExpressionStmt() {
		var expr = parseExpression();
		endStatement();
		return new ExpressionStmt(expr);
	}

	public LabeledStmt parseLabeledStmt() {
		var name = parseName();
		require(COLON);
		var stmt = parseStatement();
		return new LabeledStmt(name, stmt);
	}

	public Statement parseBody() {
		return parseStatement();
	}

	public IfStmt parseIfStmt() {
		require(IF);
		var condition = parseCondition();
		var body = parseBody();
		Optional<Statement> elseBody;
		if(accept(ELSE)) {
			elseBody = Optional.of(parseBody());
		} else {
			elseBody = Optional.empty();
		}
		return new IfStmt(condition, body, elseBody);
	}

	public WhileStmt parseWhileStmt() {
		require(WHILE);
		var condition = parseCondition();
		var body = parseBody();
		return new WhileStmt(condition, body);
	}

	public DoStmt parseDoStmt() {
		require(DO);
		var body = parseBody();
		require(WHILE);
		var condition = parseCondition();
		endStatement();
		return new DoStmt(body, condition);
	}

	public Statement parseForStmt() {
		require(FOR, LPAREN);
		FormalParameter vardecl = null;
		boolean mayHaveVariable = wouldAccept(AT.or(Tag.NAMED).or(Tag.PRIMITIVE_TYPE).or(Tag.LOCAL_VAR_MODIFIER));
		if(mayHaveVariable) {
			foreach: try(var state = tokens.enter()) {
				var modsAndAnnos = parseFinalAndAnnotations();
				Type type;
				Name name;
				List<Dimension> dimensions;
				try {
					type = parseType();
					name = parseName();
					dimensions = parseDimensions();
					require(COLON);
				} catch(SyntaxError e) {
					state.reset();
					break foreach;
				}
				vardecl = new FormalParameter(type, name, dimensions, modsAndAnnos.mods, modsAndAnnos.annos);
			}
		}

		if(vardecl == null) {
			Optional<Either<VariableDecl,ExpressionStmt>> initializer = Optional.empty();
			if(mayHaveVariable) {
				vardecl: 
				try(var state = tokens.enter()) {
					var modsAndAnnos = parseFinalAndAnnotations();
					Type type;
					Name name;
					List<Dimension> dimensions;
					try {
						type = parseType();
						name = parseName();
						dimensions = parseDimensions();
					} catch(SyntaxError e) {
						state.reset();
						break vardecl;
					}

					boolean isArray = type instanceof ArrayType || !dimensions.isEmpty();
					Optional<? extends Initializer> init;
					if(accept(EQ)) {
						init = Optional.of(parseInitializer(isArray));
					} else {
						init = Optional.empty();
					}

					var declarators = new ArrayList<VariableDeclarator>();
					declarators.add(new VariableDeclarator(name, dimensions, init));

					while(accept(COMMA)) {
						declarators.add(parseVariableDeclarator(isArray));
					}

					endStatement();

					initializer = Optional.of(Either.first(new VariableDecl(type, declarators, modsAndAnnos.mods, modsAndAnnos.annos, Optional.empty())));
				}
				if(initializer.isEmpty()) {
					initializer = Optional.of(Either.second(parseExpressionStmt()));
				}
			} else if(!accept(SEMI)) {
				initializer = Optional.of(Either.second(parseExpressionStmt()));
			}
			Optional<? extends Expression> condition;
			if(accept(SEMI)) {
				condition = Optional.empty();
			} else {
				condition = Optional.of(parseExpression());
				endStatement();
			}
			List<? extends Expression> updates;
			if(wouldAccept(RPAREN)) {
				updates = emptyList();
			} else {
				updates = listOf(this::parseExpression);
			}
			require(RPAREN);
			var body = parseBody();
			return new ForStmt(initializer, condition, updates, body);
		} else {
			var iterable = parseExpression();
			require(RPAREN);
			var body = parseBody();
			return new ForEachStmt(vardecl, iterable, body);
		}
	}

	public SynchronizedStmt parseSynchronizedStmt() {
		require(SYNCHRONIZED);
		var lock = parseCondition();
		var body = parseBodyAsBlock();
		return new SynchronizedStmt(lock, body);
	}

	public Statement parseTryStmt() {
		require(TRY);
		List<ResourceSpecifier> resources;
		if(accept(LPAREN)) {
			resources = new ArrayList<>();
			resources.add(parseResourceSpecifier());
			while(wouldNotAccept(RPAREN)) {
				endStatement();
				if(wouldAccept(RPAREN)) {
					break;
				}
				resources.add(parseResourceSpecifier());
			}
			require(RPAREN);
		} else {
			resources = emptyList();
		}
		var body = parseBodyAsBlock();
		var catches = parseCatches();
		Optional<Block> finallyBody = parseFinally();
		if(resources.isEmpty() && catches.isEmpty() && finallyBody.isEmpty()) {
			throw syntaxError("expected 'catch' or 'finally' here, got " + token);
		}
		return new TryStmt(resources, body, catches, finallyBody);
	}

	public Optional<Block> parseFinally() {
		if(accept(FINALLY)) {
			return Optional.of(parseBodyAsBlock());
		} else {
			return Optional.empty();
		}
	}

	public List<Catch> parseCatches() {
		var catches = new ArrayList<Catch>();
		while(wouldAccept(CATCH)) {
			catches.add(parseCatch());
		}
		return catches;
	}

	public Catch parseCatch() {
		require(CATCH, LPAREN);
		var modsAndAnnos = parseFinalAndAnnotations();
		var type = parseTypeUnion();
		var name = parseName();
		require(RPAREN);
		var body = parseBodyAsBlock();
		return new Catch(new FormalParameter(type, name, modsAndAnnos.mods, modsAndAnnos.annos), body);
	}

	public ResourceSpecifier parseResourceSpecifier() {
		if(wouldAccept(AT.or(Tag.NAMED).or(Tag.PRIMITIVE_TYPE).or(Tag.LOCAL_VAR_MODIFIER))) {
			vardecl:
			try(var state = tokens.enter()) {
				var modsAndAnnos = parseFinalAndAnnotations();
				Type type;
				Name name;
				try {
					type = parseType();
					name = parseName();
				} catch(SyntaxError e) {
					state.reset();
					break vardecl;
				}

				var dimensions = parseDimensions();

				boolean isArray = type instanceof ArrayType || !dimensions.isEmpty();
				require(EQ);
				var init = Optional.of(parseInitializer(isArray));
				return new VariableDecl(type, name, dimensions, init, modsAndAnnos.mods, modsAndAnnos.annos,
						Optional.empty());
			}
		}
		return new ExpressionStmt(parseExpression());
	}

	public Switch parseSwitch() {
		require(SWITCH);
		var expression = parseCondition();
		require(LBRACE);
		var cases = new ArrayList<SwitchCase>();
		while(wouldNotAccept(RBRACE)) {
			cases.add(parseSwitchCase());
		}
		require(RBRACE);
		return new Switch(expression, cases);
	}

	public SwitchCase parseSwitchCase() {
		List<Expression> labels;

		if(accept(DEFAULT)) {
			labels = emptyList();
		} else {
			require(CASE);
			labels = listOf(this::parseSwitchLabel);
		}

		Either<List<Statement>,Statement> body;

		if(accept(ARROW)) {
			Statement stmt = parseArrowCaseBody();
			body = Either.second(stmt);
		} else {
			require(COLON);
			var stmts = new ArrayList<Statement>();
			while(wouldNotAccept(CASE.or(DEFAULT).or(RBRACE))) {
				stmts.add(parseBlockStatement());
			}
			body = Either.first(stmts);
		}

		return new SwitchCase(labels, body);
	}

	public Statement parseArrowCaseBody() {
		if(wouldAccept(THROW)) {
			return parseThrowStmt();
		} else if(wouldAccept(LBRACE)) {
			return parseBlock();
		} else {
			return parseExpressionStmt();
		}
	}

	public Expression parseSwitchLabel() {
		if(wouldAccept(Tag.NAMED, ARROW)) {
			return new Variable(parseName());
		} else if(wouldAccept(LPAREN, Tag.NAMED, RPAREN, ARROW)) {
			require(LPAREN);
			var result = new Variable(parseName());
			require(RPAREN);
			return result;
		} else {
			return parseExpression();
		}
	}

	public ThrowStmt parseThrowStmt() {
		require(THROW);
		var expr = parseExpression();
		endStatement();
		return new ThrowStmt(expr);
	}

	public ReturnStmt parseReturnStmt() {
		require(RETURN);
		if(accept(SEMI)) {
			return new ReturnStmt();
		} else {
			var expr = parseExpression();
			endStatement();
			return new ReturnStmt(expr);
		}
	}

	public BreakStmt parseBreakStmt() {
		require(BREAK);
		if(accept(SEMI)) {
			return new BreakStmt();
		} else {
			var label = parseName();
			endStatement();
			return new BreakStmt(label);
		}
	}

	public ContinueStmt parseContinueStmt() {
		require(BREAK);
		if(accept(SEMI)) {
			return new ContinueStmt();
		} else {
			var label = parseName();
			endStatement();
			return new ContinueStmt(label);
		}
	}

	public Statement parseYieldStmt() {
		parse: try(var state = tokens.enter()) {
			require(YIELD);
			Expression expr;
			try {
				expr = parseExpression();
			} catch(SyntaxError e) {
				state.reset();
				break parse;
			}
			endStatement();
			return new YieldStmt(expr);
		}
		return parseExpressionStmt();
	}

	public AssertStmt parseAssertStmt() {
		require(ASSERT);
		var condition = parseExpression();
		Optional<? extends Expression> message;
		if(accept(COLON)) {
			message = Optional.of(parseExpression());
		} else {
			message = Optional.empty();
		}
		endStatement();
		return new AssertStmt(condition, message);
	}

	public Initializer parseInitializer(boolean isArray) {
		if(wouldAccept(LBRACE) && isArray) {
			return parseArrayInitializer(() -> parseInitializer(true));
		} else {
			return parseExpression();
		}
	}

	public Expression parseExpression() {
		return parseLambdaOr(this::parseAssignExpr);
	}

	public Expression parseLambdaOr(Supplier<? extends Expression> parser) {
		parse: if(wouldAccept(Tag.NAMED, ARROW) || wouldAccept(LPAREN)) {
			Either<? extends List<FormalParameter>,? extends List<InformalParameter>> parameters;
			try(var state = tokens.enter()) {
				try {
					parameters = parseLambdaParameters();
					require(ARROW);
				} catch(SyntaxError e) {
					state.reset();
					break parse;
				}
				return new Lambda(parameters, parseLambdaBody());
			}
		}
		return parser.get();
	}

	public Either<? extends List<FormalParameter>,? extends List<InformalParameter>> parseLambdaParameters() {
		if(wouldAccept(Tag.NAMED)) {
			return Either.second(List.of(parseInformalParameter()));
		}
		require(LPAREN);
		if(accept(RPAREN)) {
			return Either.first(emptyList());
		}
		try {
			if(wouldAccept(Tag.NAMED, COMMA.or(RPAREN))) {
				return Either.second(listOf(this::parseInformalParameter));
			} else {
				return Either.first(listOf(this::parseFormalParameter));
			}
		} finally {
			require(RPAREN);
		}
	}

	public InformalParameter parseInformalParameter() {
		return new InformalParameter(parseName());
	}

	public Either<Block,? extends Expression> parseLambdaBody() {
		return wouldAccept(LBRACE)? Either.first(parseBlock()) : Either.second(parseExpression());
	}

	public Expression parseAssignExpr() {
		var expr = parseConditionalExpr();
		if(expr instanceof Variable || expr instanceof IndexExpr || expr instanceof MemberAccess) {
			switch(token.getType()) {
				case EQ, PLUSEQ, SUBEQ, STAREQ, SLASHEQ, PERCENTEQ, CARETEQ, LTLTEQ, GTGTEQ, GTGTGTEQ, AMPEQ, BAREQ:
					var op = AssignExpr.Op.fromString(token.getString());
					nextToken();
					expr = new AssignExpr(expr, op, parseExpression());
				default:
			}
		}
		return expr;
	}

	public Expression parseConditionalExpr() {
		var expr = parseLogicalOrExpr();
		if(accept(QUES)) {
			var truepart = parseExpression();
			require(COLON);
			var falsepart = parseLambdaOr(this::parseConditionalExpr);
			return new ConditionalExpr(expr, truepart, falsepart);
		} else {
			return expr;
		}
	}

	public Expression parseLogicalOrExpr() {
		var expr = parseLogicalAndExpr();
		while(accept(BARBAR)) {
			expr = new BinaryExpr(expr, BinaryExpr.Op.OR, parseLogicalAndExpr());
		}
		return expr;
	}

	public Expression parseLogicalAndExpr() {
		var expr = parseBitOrExpr();
		while(accept(AMPAMP)) {
			expr = new BinaryExpr(expr, BinaryExpr.Op.AND, parseBitOrExpr());
		}
		return expr;
	}

	public Expression parseBitOrExpr() {
		var expr = parseXorExpr();
		while(accept(BAR)) {
			expr = new BinaryExpr(expr, BinaryExpr.Op.BIT_OR, parseXorExpr());
		}
		return expr;
	}

	public Expression parseXorExpr() {
		var expr = parseBitAndExpr();
		while(accept(CARET)) {
			expr = new BinaryExpr(expr, BinaryExpr.Op.XOR, parseBitAndExpr());
		}
		return expr;
	}

	public Expression parseBitAndExpr() {
		var expr = parseEqualityExpr();
		while(accept(AMP)) {
			expr = new BinaryExpr(expr, BinaryExpr.Op.BIT_AND, parseEqualityExpr());
		}
		return expr;
	}

	public Expression parseEqualityExpr() {
		var expr = parseRelExpr();
		for(;;) {
			if(accept(EQEQ)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.EQUAL, parseRelExpr());
			} else if(accept(BANGEQ)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.NEQUAL, parseRelExpr());
			} else {
				return expr;
			}
		}
	}

	public Expression parseRelExpr() {
		var expr = parseShiftExpr();
		for(;;) {
			if(accept(LT)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.LTHAN, parseShiftExpr());
			} else if(accept(GT)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.GTHAN, parseShiftExpr());
			} else if(accept(LTEQ)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.LEQUAL, parseShiftExpr());
			} else if(accept(GTEQ)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.GEQUAL, parseShiftExpr());
			} else if(accept(INSTANCEOF)) {
				expr = new TypeTest(expr, parseReferenceType());
			} else {
				return expr;
			}
		}
	}

	public Expression parseShiftExpr() {
		var expr = parseAddExpr();
		for(;;) {
			if(accept(LTLT)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.LSHIFT, parseAddExpr());
			} else if(acceptPseudoOp(GT, GT, GT)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.URSHIFT, parseAddExpr());
			} else if(acceptPseudoOp(GT, GT)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.RSHIFT, parseAddExpr());
			} else {
				return expr;
			}
		}
	}

	public Expression parseAddExpr() {
		var expr = parseMulExpr();
		for(;;) {
			if(accept(PLUS)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.PLUS, parseMulExpr());
			} else if(accept(SUB)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.MINUS, parseMulExpr());
			} else {
				return expr;
			}
		}
	}

	public Expression parseMulExpr() {
		var expr = parseUnaryExpr();
		for(;;) {
			if(accept(STAR)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.TIMES, parseUnaryExpr());
			} else if(accept(SLASH)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.DIVIDE, parseUnaryExpr());
			} else if(accept(PERCENT)) {
				expr = new BinaryExpr(expr, BinaryExpr.Op.MODULUS, parseUnaryExpr());
			} else {
				return expr;
			}
		}
	}

	public Expression parseUnaryExpr() {
		if(accept(PLUSPLUS)) {
			return new PreIncrementExpr(parseUnaryExpr());
		} else if(accept(SUBSUB)) {
			return new PreDecrementExpr(parseUnaryExpr());
		} else if(accept(PLUS)) {
			return new UnaryExpr(UnaryExpr.Op.POSITIVE, parseUnaryExpr());
		} else if(accept(SUB)) {
			return new UnaryExpr(UnaryExpr.Op.NEGATE, parseUnaryExpr());
		} else {
			return parseUnaryExprNotPlusMinus();
		}
	}

	public Expression parseUnaryExprNotPlusMinus() {
		if(accept(TILDE)) {
			return new UnaryExpr(UnaryExpr.Op.INVERT, parseUnaryExpr());
		} else if(accept(BANG)) {
			return new UnaryExpr(UnaryExpr.Op.NOT, parseUnaryExpr());
		} else {
			return parseCastExpr();
		}
	}

	public Expression parseCastExpr() {
		cast: if(wouldAccept(LPAREN)) {
			try(var state = tokens.enter()) {
				try {
					require(LPAREN);
					Type type;
					var annotations = parseAnnotations();
					if(wouldAccept(PRIMITIVE_TYPES, RPAREN)) {
						type = new PrimitiveType(token.getString(), annotations);
						nextToken();
					} else {
						type = parseTypeIntersection(annotations);
					}
					require(RPAREN);
					Expression expr;
					if(type instanceof PrimitiveType) {
						expr = parseUnaryExpr();
					} else {
						expr = parseLambdaOr(this::parseUnaryExprNotPlusMinus);
					}
					return new CastExpr(type, expr);
				} catch(SyntaxError e) {
					state.reset();
					break cast;
				}
			}
		}
		return parsePostfixExpr();
	}

	public Expression parsePostfixExpr() {
		var expr = parseSuffix();
		for(;;) {
			if(accept(PLUSPLUS)) {
				expr = new PostIncrementExpr(expr);
			} else if(accept(SUBSUB)) {
				expr = new PostDecrementExpr(expr);
			} else {
				return expr;
			}
		}
	}

	public Expression parseSuffix() {
		var expr = parsePrimary();
		for(;;) {
			if(wouldAccept(COLCOL)) {
				expr = parseMethodReferenceRest(expr);
			} else if(wouldAccept(DOT)
					&& (!wouldAccept(DOT, SUPER.or(THIS)) || wouldAccept(DOT, SUPER.or(THIS), not(LPAREN)))) {
				expr = parseMemberAccessRest(expr);
			} else if(wouldAccept(LBRACKET)) {
				expr = parseIndexRest(expr);
			} else {
				return expr;
			}
		}
	}

	public Expression parseMethodReferenceRest(Expression object) {
		require(COLCOL);
		var typeArguments = parseTypeArgumentsOpt();
		var name = parseName();
		return parseExpressionMethodReferenceRest(object, typeArguments, name);
	}

	public Expression parseExpressionMethodReferenceRest(Expression object, List<? extends TypeArgument> typeArguments,
														 Name name) {
		return new MethodReference(object, typeArguments, name);
	}

	public Expression parseTypeMethodReferenceRest(Either<ArrayType,GenericType> type,
												   List<? extends TypeArgument> typeArguments) {
		return new MethodReference((Type)type.getValue(), typeArguments, Name("new"));
	}

	public Expression parseSuperMethodReferenceRest(Optional<QualifiedName> qualifier,
													List<? extends TypeArgument> typeArguments, Name name) {
		return new SuperMethodReference(qualifier, typeArguments, name);
	}

	public Expression parseMemberAccessRest(Expression object) {
		require(DOT);
		if(wouldAccept(LT)) {
			var typeArguments = parseTypeArguments();
			var name = parseName();
			var args = parseArguments(false);
			return new FunctionCall(object, name, typeArguments, args);
		} else if(wouldAccept(NEW)) {
			var creator = parseClassCreator();
			creator.setObject(object);
			return creator;
		}
		var name = parseName();
		if(wouldAccept(LPAREN)) {
			var args = parseArguments(false);
			return new FunctionCall(object, name, args);
		}
		return new MemberAccess(object, name);
	}

	public Expression parseIndexRest(Expression indexed) {
		require(LBRACKET);
		var index = parseExpression();
		require(RBRACKET);
		return new IndexExpr(indexed, index);
	}

	public Expression parsePrimary() {
		return switch(token.getType()) {
			case NUMBER -> parseNumberLiteral();
			case STRING -> parseStringLiteral();
			case CHARACTER -> parseCharLiteral();
			case NULL -> parseNullLiteral();
			case TRUE, FALSE -> parseBooleanLiteral();
			case THIS -> parseThis();
			case SUPER -> parseSuper();
			case NEW -> parseCreator();
			case LPAREN -> parseParens();
			case SWITCH -> parseSwitch();
			case VOID, BOOLEAN, BYTE, SHORT, CHAR, INT, FLOAT, LONG, DOUBLE -> parseClassLiteralOrMethodReference();
			default -> {
				if(wouldAccept(Tag.NAMED)) {
					break parsePrimaryName();
				} else {
					throw syntaxError("invalid start of expression");
				}
			}
		};
	}

	public Expression parseStringLiteral() {
		var token = this.token;
		var str = token.getString();
		require(STRING);
		str = str.substring(1, str.length() - 1);
		try {
			return new Literal(StringEscapeUtils.unescapeJava(str), token.getString());
		} catch(Exception e) {
			throw new SyntaxError("invalid string literal", filename, token.getStart().getLine(),
					token.getStart().getColumn(), token.getLine());
		}
	}

	public Expression parseCharLiteral() {
		var token = this.token;
		var str = token.getString();
		require(CHARACTER);
		str = str.substring(1, str.length() - 1);
		try {
			str = StringEscapeUtils.unescapeJava(str);
			if(str.length() != 1) {
				throw new IllegalArgumentException();
			}
			return new Literal(str.charAt(0), token.getString());
		} catch(Exception e) {
			throw new SyntaxError("invalid char literal", filename, token.getStart().getLine(),
					token.getStart().getColumn(), token.getLine());
		}
	}

	public Expression parseNullLiteral() {
		require(NULL);
		return new Literal();
	}

	public Expression parseBooleanLiteral() {
		if(accept(TRUE)) {
			return new Literal(true);
		} else {
			require(FALSE);
			return new Literal(false);
		}
	}

	public Expression parseNumberLiteral() {
		var token = this.token;
		var str = token.getString();
		require(NUMBER);
		assert !token.equals(this.token);
		try {
			str = str.replace("_", "");
			if(str.endsWith("f") || str.endsWith("F")) {
				if((str.startsWith("0x") || str.startsWith("0X")) && !str.contains(".") && !str.contains("p")
						&& !str.contains("P")) {
					return new Literal(Integer.parseInt(str.substring(2), 16), token.getString());
				}
				return new Literal(Float.parseFloat(str.substring(0, str.length() - 1)), token.getString());
			}
			if(str.endsWith("d") || str.endsWith("D")) {
				if((str.startsWith("0x") || str.startsWith("0X")) && !str.contains(".") && !str.contains("p")
						&& !str.contains("P")) {
					return new Literal(Integer.parseInt(str.substring(2), 16), token.getString());
				}
				return new Literal(Double.parseDouble(str.substring(0, str.length() - 1)), token.getString());
			}
			if(str.contains(".")) {
				return new Literal(Double.parseDouble(str), token.getString());
			}
			int base = 10;
			if(str.startsWith("0x") || str.startsWith("0X")) {
				base = 16;
				str = str.substring(2);
			} else if(str.startsWith("0b") || str.startsWith("0B")) {
				base = 2;
				str = str.substring(2);
			}
			if(str.endsWith("l") || str.endsWith("L")) {
				return new Literal(Long.parseLong(str.substring(0, str.length() - 1), base), token.getString());
			} else {
				return new Literal(Integer.parseInt(str, base), token.getString());
			}
		} catch(NumberFormatException e) {
			throw new SyntaxError("invalid number literal", filename, token.getStart().getLine(),
					token.getStart().getColumn(), token.getLine());
		}
	}

	public Expression parsePrimaryName() {
		try(var state = tokens.enter()) {
			try {
				var names = new ArrayList<Name>();
				names.add(parseName());
				while(wouldAccept(DOT, not(THIS.or(SUPER).or(CLASS)))) {
					require(DOT);
					names.add(parseName());
				}
				require(DOT);
				if(accept(SUPER)) {
					if(accept(DOT)) {
						var typeArguments = parseTypeArgumentsOpt();
						var name = parseName();
						var args = parseArguments(false);
						return new SuperFunctionCall(new QualifiedName(names), name, typeArguments, args);
					} else {
						require(COLCOL);
						var typeArguments = parseTypeArgumentsOpt();
						var name = parseName();
						return parseSuperMethodReferenceRest(Optional.of(new QualifiedName(names)), typeArguments,
															 name);
					}
				} else if(accept(CLASS)) {
					return new ClassLiteral(new GenericType(new QualifiedName(names)));
				} else {
					require(THIS);
					return new This(new QualifiedName(names));
				}
			} catch(SyntaxError e) {
				state.reset();
			}
		}
		try(var state = tokens.enter()) {
			try {
				var type = parseType(emptyList());
				if(accept(COLCOL)) {
					if(type instanceof ArrayType) {
						require(NEW);
						return parseTypeMethodReferenceRest(Either.first((ArrayType)type), emptyList());
					} else {
						if(!(type instanceof GenericType)) {
							throw syntaxError("can only create references to class types or array types");
						}
						var typeArguments = parseTypeArgumentsOpt();
						require(NEW);
						return parseTypeMethodReferenceRest(Either.second((GenericType)type), typeArguments);
					}
				}
				require(DOT, CLASS);
				return new ClassLiteral(type);
			} catch(SyntaxError e) {
				state.reset();
			}
		}
		var name = parseName();
		if(wouldAccept(LPAREN)) {
			var args = parseArguments(false);
			return new FunctionCall(name, args);
		} else {
			return new Variable(name);
		}
	}

	public Expression parseSuper() {
		require(SUPER);
		if(accept(COLCOL)) {
			var typeArguments = parseTypeArgumentsOpt();
			var name = parseName();
			return parseSuperMethodReferenceRest(Optional.empty(), typeArguments, name);
		} else {
			require(DOT);
			var typeArguments = parseTypeArgumentsOpt();
			var name = parseName();
			var args = parseArguments(false);
			return new SuperFunctionCall(name, typeArguments, args);
		}
	}

	public Expression parseThis() {
		require(THIS);
		return new This();
	}

	public Expression parseParens() {
		require(LPAREN);
		var expr = parseExpression();
		require(RPAREN);
		return new ParensExpr(expr);
	}

	public Expression parseClassLiteralOrMethodReference() {
		if(accept(VOID)) {
			require(DOT, CLASS);
			return new ClassLiteral(new VoidType());
		}
		var type = parseType(emptyList());
		if(type instanceof ArrayType && accept(COLCOL)) {
			require(NEW);
			return parseTypeMethodReferenceRest(Either.first((ArrayType)type), emptyList());
		}
		require(DOT, CLASS);
		return new ClassLiteral(type);
	}

	public List<? extends Expression> parseConstructorArguments() {
		return parseArguments(false);
	}

	public List<? extends Expression> parseArgumentsOpt(boolean classCreatorArguments) {
		return wouldAccept(LPAREN)? parseArguments(classCreatorArguments) : emptyList();
	}

	public List<? extends Expression> parseArguments(boolean classCreatorArguments) {
		require(LPAREN);
		if(accept(RPAREN)) {
			return emptyList();
		} else {
			var args = listOf(this::parseArgument);
			require(RPAREN);
			return args;
		}
	}

	public Expression parseArgument() {
		return parseExpression();
	}

	public Expression parseCreator() {
		require(NEW);
		if(wouldAccept(LT)) {
			var typeArguments = parseTypeArguments();
			var type = parseGenericType();
			return parseClassCreatorRest(typeArguments, type);
		}
		var typeAnnotations = parseAnnotations();
		if(wouldAccept(PRIMITIVE_TYPES)) {
			var base = new PrimitiveType(token.getString(), typeAnnotations);
			nextToken();
			var annotations = parseAnnotations();
			require(LBRACKET);
			if(accept(RBRACKET)) {
				var dimensions = new ArrayList<Dimension>();
				dimensions.add(new Dimension(annotations));
				while(wouldAccept(AT.or(LBRACKET))) {
					dimensions.add(parseDimension());
				}
				var initializer = parseArrayInitializer(() -> parseInitializer(true));
				return new ArrayCreator(base, initializer, dimensions);
			} else {
				var sizes = new ArrayList<Size>();
				sizes.add(new Size(parseExpression(), annotations));
				var dimensions = new ArrayList<Dimension>();
				require(RBRACKET);
				while(wouldAccept(AT.or(LBRACKET))) {
					annotations = parseAnnotations();
					require(LBRACKET);
					if(accept(RBRACKET)) {
						dimensions.add(new Dimension(annotations));
						break;
					} else {
						sizes.add(new Size(parseExpression(), annotations));
						require(RBRACKET);
					}
				}
				while(wouldAccept(AT.or(LBRACKET))) {
					dimensions.add(parseDimension());
				}
				return new ArrayCreator(base, sizes, dimensions);
			}
		} else {
			var type = parseGenericType(typeAnnotations);
			if(type.getTypeArguments().isEmpty() && wouldAccept(LT, GT)) {
				return parseClassCreatorRest(emptyList(), type);
			}
			if(wouldAccept(AT.or(LBRACKET))) {
				var annotations = parseAnnotations();
				require(LBRACKET);
				if(accept(RBRACKET)) {
					var dimensions = new ArrayList<Dimension>();
					dimensions.add(new Dimension(annotations));
					while(wouldAccept(AT.or(LBRACKET))) {
						dimensions.add(parseDimension());
					}
					var initializer = parseArrayInitializer(() -> parseInitializer(true));
					return new ArrayCreator(type, initializer, dimensions);
				} else {
					var sizes = new ArrayList<Size>();
					sizes.add(new Size(parseExpression(), annotations));
					var dimensions = new ArrayList<Dimension>();
					require(RBRACKET);
					while(wouldAccept(AT.or(LBRACKET))) {
						annotations = parseAnnotations();
						require(LBRACKET);
						if(accept(RBRACKET)) {
							dimensions.add(new Dimension(annotations));
							break;
						} else {
							sizes.add(new Size(parseExpression(), annotations));
							require(RBRACKET);
						}
					}
					while(wouldAccept(AT.or(LBRACKET))) {
						dimensions.add(parseDimension());
					}
					return new ArrayCreator(type, sizes, dimensions);
				}
			} else {
				return parseClassCreatorRest(emptyList(), type);
			}
		}
	}

	public ClassCreator parseClassCreator() {
		require(NEW);
		var typeArguments = parseTypeArgumentsOpt();
		var type = parseGenericType();
		return parseClassCreatorRest(typeArguments, type);
	}

	public ClassCreator parseClassCreatorRest(List<? extends TypeArgument> typeArguments, GenericType type) {
		boolean hasDiamond = type.getTypeArguments().isEmpty() && accept(LT, GT);
		var args = parseArguments(true);
		Optional<? extends List<Member>> members;
		if(wouldAccept(LBRACE)) {
			members = Optional.of(parseClassBody(() -> parseClassMember(false)));
		} else {
			members = Optional.empty();
		}
		return new ClassCreator(typeArguments, type, hasDiamond, args, members);
	}

}