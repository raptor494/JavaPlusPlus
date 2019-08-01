package jpp.parser;

import static jpp.parser.Names.*;
import static jpp.parser.QualNames.*;
import static jpp.parser.JPPParser.Feature.*;
import static jtree.parser.JavaTokenType.*;
import static jtree.util.Utils.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;

import jpp.nodes.DefaultFormalParameter;
import jpp.nodes.JPPModifier;
import jpp.nodes.JPPModifier.Modifiers;
import jtree.nodes.*;
import jtree.parser.JavaParser;
import jtree.parser.JavaTokenType;
import jtree.parser.JavaTokenType.Tag;
import jtree.parser.JavaTokenizer;
import jtree.parser.ModsAndAnnotations;
import jtree.parser.SyntaxError;
import jtree.parser.Token;
import jtree.util.ContextStack;
import jtree.util.Either;
import lombok.Getter;
import lombok.NonNull;

public class JPPParser extends JavaParser {
	public static enum Feature {
		/** {@code converter.qualifiedNames} */
		FULLY_QUALIFIED_NAMES ("converter.qualifiedNames", false),
		/** {@code literals.collections} */
		COLLECTION_LITERALS ("literals.collections", true),
		/** {@code syntax.argumentAnnotations} */
		ARGUMENT_ANNOTATIONS ("syntax.argumentAnnotations", true),
		/** {@code syntax.optionalNewArguments} */
		OPTIONAL_CONSTRUCTOR_ARGUMENTS ("syntax.optionalNewArguments", true),
		/** {@code literals.regex} */
		REGEX_LITERALS ("literals.regex", true),
		/** {@code literals.rawStrings} */
		RAW_STRING_LITERALS ("literals.rawStrings", true),
		/** {@code literals.formatStrings} */
		FORMAT_STRINGS ("literals.formatStrings", true),
		/** {@code literals.textBlocks} */
		TEXT_BLOCKS ("literals.textBlocks", true),
		/** {@code syntax.trailingCommas} */
		TRAILING_COMMAS ("syntax.trailingCommas", false),
		/** {@code expressions.partialMethodReferences} */
		PARTIAL_METHOD_REFERENCES ("expressions.partialMethodReferences", true),
		/** {@code syntax.lastLambdaArgument} */
		LAST_LAMBDA_ARGUMENT ("syntax.lastLambdaArgument", true),
		/** {@code syntax.optionalStatementParenthesis} */
		OPTIONAL_STATEMENT_PARENTHESIS ("syntax.optionalStatementParenthesis", false),
		/** {@code statements.notCondition} */
		IF_NOT ("statements.notCondition", true),
		/** {@code statements.emptyFor} */
		EMPTY_FOR ("statements.emptyFor", true),
		/** {@code statements.simpleForEach} */
		SIMPLER_FOR ("statements.simpleForEach", true),
		/** {@code statements.emptySynchronized} */
		EMPTY_SYNCHRONIZED ("statements.emptySynchronized", true),
		/** {@code expressions.variableDeclarations} */
		VARDECL_EXPRESSIONS ("expressions.variableDeclarations", true),
		/** {@code statements.fromImport} */
		FROM_IMPORTS ("statements.fromImport", true),
		/** {@code statements.commaImports} */
		COMMA_IMPORTS ("statements.commaImports", true),
		/** {@code statements.unimport} */
		UNIMPORTS ("statements.unimport", true),
		/** {@code statements.defaultCatch} */
		DEFAULT_CATCH ("statements.defaultCatch", true),
		/** {@code statements.tryElse} */
		TRY_ELSE ("statements.tryElse", true),
		/** {@code syntax.implicitBlocks} */
		IMPLICIT_BLOCKS ("syntax.implicitBlocks", false),
		/** {@code statements.with} */
		WITH_STATEMENT ("statements.with", true),
		/** {@code syntax.implicitSemicolons} */
		IMPLICIT_SEMICOLONS ("syntax.implicitSemicolons", true),
		/** {@code statements.print} */
		PRINT_STATEMENT ("statements.print", true),
		/** {@code syntax.simpleClassBodies} */
		EMPTY_TYPE_BODIES ("syntax.simpleClassBodies", true),
		/** {@code syntax.simpleConstructorBodies} */
		EMPTY_CONSTRUCTOR_BODIES ("syntax.simpleConstructorBodies", true),
		/** {@code syntax.improvedExplicitConstructorCallArguments} */
		IMPROVED_CONSTRUCTOR_CALL_ARGUMENTS ("syntax.improvedExplicitConstructorCallArguments", true),
		/** {@code syntax.defaultArguments} */
		DEFAULT_ARGUMENTS ("syntax.defaultArguments", true),
		/** {@code statements.empty} */
		EMPTY_STATEMENTS ("statements.empty", true),
		/** {@code syntax.defaultModifiers} */
		DEFAULT_MODIFIERS ("syntax.defaultModifiers", true),
		/** {@code syntax.autoDefaultModifier} */
		AUTO_DEFAULT_MODIFIER ("syntax.autoDefaultModifier", true),
		/** {@code syntax.simpleMethodBodies} */
		SIMPLE_METHOD_BODIES ("syntax.simpleMethodBodies", true),
		/** {@code literals.optional} */
		OPTIONAL_LITERALS ("literals.optional", true),
		/** {@code syntax.betterArrowCaseBodies} */
		BETTER_ARROW_CASE_BODIES ("syntax.betterArrowCaseBodies", true),
		/** {@code syntax.altAnnotationDecl} */
		ALTERNATE_ANNOTATION_DECL ("syntax.altAnnotationDecl", true),
		/** {@code syntax.multiVarDecls} */
		MULTIPLE_VAR_DECLARATIONS ("syntax.multiVarDecls", true),
		;

		public static final Set<Feature> VALUES = Collections.unmodifiableSet(EnumSet.allOf(Feature.class));

		public final String id;
		@Getter
		private boolean enabledByDefault;
		
		Feature(String id, boolean enabledByDefault) {
			this.id = id;
			this.enabledByDefault = enabledByDefault;
		}
		
		@Override
		public String toString() {
			return id;
		}
		
		public static EnumSet<Feature> enabledByDefault() {
			var features = EnumSet.noneOf(Feature.class);
			for(var feature : VALUES) {
				if(feature.isEnabledByDefault()) {
					features.add(feature);
				}
			}
			return features;
		}
	}
	
	protected static enum Scope {
		NORMAL, CONDITION
	}
	
	protected final ContextStack<Scope> scope = new ContextStack<>(Scope.NORMAL);
	
	protected static enum Context {
		STATIC, DYNAMIC
	}
	
	protected final ContextStack<Context> context = new ContextStack<>(Context.STATIC);
	
	protected EnumSet<Feature> enabledFeatures;
	protected final Set<ImportDecl> imports = new HashSet<>();
	
	public JPPParser(CharSequence text) {
		super(text);
	}

	public JPPParser(CharSequence text, String filename) {
		super(text, filename);
	}
	
	public JPPParser(CharSequence text, Collection<Feature> features) {
		super(text);
		enabledFeatures.clear();
		enabledFeatures.addAll(features);
	}
	
	public JPPParser(CharSequence text, String filename, Collection<Feature> features) {
		super(text, filename);
		enabledFeatures.clear();
		enabledFeatures.addAll(features);
	}
	
	@Override
	protected JavaTokenizer<JavaTokenType> createTokenizer(CharSequence text, String filename) {
		return new JPPTokenizer(text, filename, enabledFeatures = Feature.enabledByDefault());
	}
	
	public boolean enabled(Feature feature) {
		return enabledFeatures.contains(feature);
	}
	
	public void enable(String feature) {
		setEnabled(feature, true);
	}
	
	public void enable(@NonNull Feature feature) {
		enabledFeatures.add(feature);
	}
	
	public void disable(String feature) {
		setEnabled(feature, false);
	}
	
	public void disable(@NonNull Feature feature) {
		enabledFeatures.remove(feature);
	}
	
	public void setEnabled(@NonNull Feature feature, boolean enabled) {
		if(enabled) {
			enabledFeatures.add(feature);
		} else {
			enabledFeatures.remove(feature);
		}
	}
	
	public void setEnabled(String featureId, boolean enabled) {
		if(featureId.equals("*")) {
			if(enabled) {
				enabledFeatures.addAll(Feature.VALUES);
			} else {
				enabledFeatures.clear();
			}
		} else if(featureId.endsWith(".*") && featureId.length() > 2) {
			String prefix = featureId.substring(0, featureId.length()-1); // removes the *
			boolean found = false;
			for(var feature : Feature.VALUES) {
				if(feature.id.startsWith(prefix)) {
					found = true;
					if(enabled) {
						enabledFeatures.add(feature);
					} else {
						enabledFeatures.remove(feature);
					}
				}
			}
			if(!found) {
				throw new IllegalArgumentException("No feature found matching '" + featureId + "'");
			}
		} else {
			for(var feature : Feature.VALUES) {
				if(feature.id.equals(featureId)) {
					if(enabled) {
						enabledFeatures.add(feature);
					} else {
						enabledFeatures.remove(feature);
					}
					return;
				}
			}
			throw new IllegalArgumentException("No feature found matching '" + featureId + "'");
		}
	}
	
	protected boolean imported(String type) {
		return imported(QualifiedName(type));
	}
	
	protected boolean imported(QualifiedName type) {
		for(var importdecl : imports) {
			if(!importdecl.isStatic()) {
				if(importdecl.isWildcard()) {
					if(importdecl.getName().equals(type.subName(0, type.nameCount()-1))) {
						return true;
					}
				} else {
					if(importdecl.getName().equals(type)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected boolean importedNameOtherThan(String name) {
		return importedNameOtherThan(QualifiedName(name));
	}
	
	protected boolean importedNameOtherThan(QualifiedName name) {
		for(var importdecl : imports) {
			if(!importdecl.isStatic() && !importdecl.isWildcard() && importdecl.getName().endsWith(name.lastName())) {
				return !importdecl.getName().equals(name);
			}
		}
		return false;
	}
	
	@Override
	public CompilationUnit parseCompilationUnit() {
		var unit = super.parseCompilationUnit();
		var imports = unit.getImports();
		for(var importdecl : this.imports) {
			if(!imports.contains(importdecl)) {
				imports.add(importdecl);
			}
		}
		return unit;
	}

	@Override
	public ArrayList<ImportDecl> parseImportSection() {
		for(;;) {
			if(accept(ENABLE)) {
				parseFeatures(true);
			} else if(accept(DISABLE)) {
				parseFeatures(false);
			} else {
				break;
			}
		}
		var imports = new ArrayList<ImportDecl>();
		for(;;) {
			if(wouldAccept(IMPORT)) {
				var importdecl = parseImport();
				imports.addAll(importdecl);
				this.imports.addAll(importdecl);
			} else if(enabled(FROM_IMPORTS) && wouldAccept(FROM)) {
				var importdecl = parseFromImport(imports, this.imports);
				imports.addAll(importdecl);
				this.imports.addAll(importdecl);
			} else if(enabled(UNIMPORTS) && wouldAccept(UNIMPORT)) {
				parseUnimport(imports, this.imports);
			} else {
				break;
			}
		}
		return imports;
	}
	
	protected void parseFeatures(boolean enable) {
		if(accept(STAR)) {
			require(SEMI);
			if(enable) {
				enabledFeatures.addAll(Feature.VALUES);
			} else {
				enabledFeatures.clear();
			}
		} else {
			var features = new ArrayList<Pair<Token<JavaTokenType>, String>>();
			features.add(parseFeatureName());
			while(accept(COMMA)) {
				if(enabled(TRAILING_COMMAS) && wouldAccept(SEMI)) {
					break;
				}
				features.add(parseFeatureName());
			}
			require(SEMI);
			for(var feature : features) {
				try {
					setEnabled(feature.getRight(), enable);
				} catch(IllegalArgumentException e) {
					throw syntaxError(e.getMessage(), feature.getLeft());
				}
			}
		}
	}
	
	protected Pair<Token<JavaTokenType>, String> parseFeatureName() {
		var firstToken = token;
		var sb = new StringBuilder();
		sb.append(parseIdent());
		while(accept(DOT)) {
			if(accept(STAR)) {
				sb.append(".*");
				break;
			} else {
				sb.append('.').append(parseIdent());
			}
		}
		return Pair.of(firstToken, sb.toString());
	}
	
	public void parseUnimport(List<ImportDecl> imports1, Set<ImportDecl> imports2) {
		require(UNIMPORT);
		var imports = parseImportRest(true);
		if(!imports.isEmpty()) {
			Predicate<ImportDecl> inImports = decl -> {
				for(var decl2 : imports) {
					if(decl.equals(decl2)) {
						return true;
					} else if(decl.isStatic() == decl2.isStatic() && decl2.isWildcard() && !decl.isWildcard() && decl.getName().subName(0, decl.getName().nameCount()-1).equals(decl2.getName())) {
						return true;
					}
				}
				return false;
			};
			imports1.removeIf(inImports);
			imports2.removeIf(inImports);
		}
	}

	@Override
	public List<ImportDecl> parseImport() {
		require(IMPORT);
		return parseImportRest(false);
	}
	
	protected List<ImportDecl> parseImportRest(boolean unimport) {
		boolean isStatic = accept(STATIC);
		var imports = new ArrayList<ImportDecl>();
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
		imports.add(new ImportDecl(new QualifiedName(names), isStatic, wildcard));
		if(enabled(COMMA_IMPORTS)) {
    		while(accept(COMMA)) {
    			if(enabled(TRAILING_COMMAS) && wouldAccept(SEMI)) {
    				break;
    			}
				wildcard = false;
				names.clear();
	    		names.add(parseName());
	    		while(accept(DOT)) {
	    			if(accept(STAR)) {
	    				wildcard = true;
	    				break;
	    			} else {
	    				names.add(parseName());
	    			}
	    		}
	    		imports.add(new ImportDecl(new QualifiedName(names), isStatic, wildcard));
    		}
		}
		require(SEMI);
		return imports;
	}
	
	public List<ImportDecl> parseFromImport() {
		return parseFromImport(new ArrayList<>(), this.imports);
	}
	
	public List<ImportDecl> parseFromImport(List<ImportDecl> imports1, Set<ImportDecl> imports2) {
		require(FROM);
		var pckg = parseQualName();
		boolean unimport;
		if(accept(IMPORT)) {
			unimport = false;
		} else {
			if(!enabled(UNIMPORTS) && wouldAccept(UNIMPORT)) {
				throw syntaxError("unimport only allowed here if prefixed by 'java++'");
			}
			require(UNIMPORT);
			unimport = true;
		}
		boolean isStatic = accept(STATIC);
		var imports = new ArrayList<ImportDecl>();
		imports.add(parseFromImportRest(pckg, isStatic));
		while(accept(COMMA)) {
			if(enabled(TRAILING_COMMAS) && wouldAccept(SEMI)) {
				break;
			}
			imports.add(parseFromImportRest(pckg, isStatic));
		}
		require(SEMI);
		if(unimport) {
			if(!imports.isEmpty()) {
				Predicate<ImportDecl> inImports = decl -> {
					for(var decl2 : imports) {
						if(decl.equals(decl2)) {
							return true;
						} else if(decl.isStatic() == decl2.isStatic() && decl2.isWildcard() && !decl.isWildcard() && decl.getName().subName(0, decl.getName().nameCount()-1).equals(decl2.getName())) {
							return true;
						}
					}
					return false;
				};
				imports1.removeIf(inImports);
				imports2.removeIf(inImports);
			}
			return emptyList();
		} else {
			return imports;
		}
	}
	
	public ImportDecl parseFromImportRest(QualifiedName pckg, boolean isStatic) {
		if(accept(STAR)) {
			return new ImportDecl(pckg, isStatic, true);
		} else {
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
			return new ImportDecl(pckg.append(names), isStatic, wildcard);
		}
	}
	
	protected Pair<Token<JavaTokenType>, String> parseFromJavaPlusPlusImportRest() {
		var firstToken = token;
		if(accept(STAR)) {
			return Pair.of(firstToken, "*");
		} else {
			var sb = new StringBuilder();
			sb.append(parseName());
			while(accept(DOT)) {
				sb.append('.');
				if(accept(STAR)) {
					sb.append('*');
					break;
				} else {
					sb.append(parseName());
				}
			}
			return Pair.of(firstToken, sb.toString());
		}
	}

	@Override
	public TypeDecl parseTypeDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(enabled(ALTERNATE_ANNOTATION_DECL) && wouldAccept(ANNOTATION, Tag.NAMED, LBRACE.or(LPAREN))) {
			return parseAltAnnotationDecl(docComment, modsAndAnnos);
		} else {
			return super.parseTypeDecl(docComment, modsAndAnnos);
		}
	}
	
	public AnnotationDecl parseAltAnnotationDecl() {
		var docComment = getDocComment();
		var modsAndAnnos = parseClassModsAndAnnotations();
		return parseAltAnnotationDecl(docComment, modsAndAnnos);
	}
	
	public AnnotationDecl parseAltAnnotationDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeClassMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		require(ANNOTATION);
		var name = parseTypeName();
		var members = new ArrayList<Member>();
		
		if(accept(LPAREN)) {
			if(!accept(RPAREN)) {
				try(var $ = typeNames.enter(name)) {
					var docComment1 = getDocComment();
					var modsAndAnnos1 = parseAnnotationPropertyModsAndAnnotations();
					var type = parseType();
					if(accept(EQ)) {
						Optional<? extends AnnotationValue> defaultValue = Optional.of(parseAnnotationValue());
						members.add(new AnnotationProperty(Names.value, type, defaultValue, modsAndAnnos1.mods, modsAndAnnos1.annos, docComment1));
					} else if(wouldAccept(RPAREN)) {
						members.add(new AnnotationProperty(Names.value, type, Optional.empty(), modsAndAnnos1.mods, modsAndAnnos1.annos, docComment1));
					} else {
						var name1 = parseName();
						Optional<? extends AnnotationValue> defaultValue;
						if(accept(EQ)) {
							defaultValue = Optional.of(parseAnnotationValue());
						} else {
							defaultValue = Optional.empty();
						}
						members.add(new AnnotationProperty(name1, type, defaultValue, modsAndAnnos1.mods, modsAndAnnos1.annos, docComment1));
						while(accept(COMMA)) {
							if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
								break;
							}
							members.add(parseAltAnnotationProperty());
						}
					}
				}
				require(RPAREN);
			}
		}
		
		if(!accept(SEMI)) {
    		try(var $ = typeNames.enter(name)) {
    			members.addAll(parseClassBody(this::parseAltAnnotationMember));
    		}
		}
		return new AnnotationDecl(name, members, modifiers, annotations, docComment);
	}
	
	public List<Member> parseAltAnnotationMember() {
		var docComment = getDocComment();
		var modsAndAnnos = parseKeywordModsAndAnnotations();
		try(var $ = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
			return parseAltAnnotationMember(docComment, modsAndAnnos);
		}
	}
	
	public List<Member> parseAltAnnotationMember(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(wouldAccept(CLASS.or(INTERFACE).or(ENUM)) || wouldAccept(AT, INTERFACE)) {
			return List.of(parseTypeDecl(docComment, modsAndAnnos));
		} else /*if(modsAndAnnos.hasModifier("static"))*/ {
			return parseInterfaceMember(false, docComment, modsAndAnnos);
		}
	}
	
	public AnnotationProperty parseAltAnnotationProperty() {
		var docComment = getDocComment();
		return parseAltAnnotationProperty(docComment, parseAnnotationPropertyModsAndAnnotations());
	}

	public AnnotationProperty parseAltAnnotationProperty(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeMethodMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var type = parseType();
		var name = parseName();
		Optional<? extends AnnotationValue> defaultValue;
		if(accept(EQ)) {
			defaultValue = Optional.of(parseAnnotationValue());
		} else {
			defaultValue = Optional.empty();
		}
		return new AnnotationProperty(name, type, defaultValue, modifiers, annotations, docComment);
	}

	@Override
	public List<Member> parseAnnotationMember() {
		var docComment = getDocComment();
		var modsAndAnnos = parseKeywordModsAndAnnotations();
		try(var $ = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
			return parseAnnotationMember(docComment, modsAndAnnos);
		}
	}

	@Override
	public AnnotationProperty parseAnnotationProperty() {
		var docComment = getDocComment();
		var modsAndAnnos = parseKeywordModsAndAnnotations();
		try(var $ = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
			return parseAnnotationProperty(docComment, modsAndAnnos);
		}
	}

	@Override
	public List<Member> parseClassMember(boolean inInterface) {
		if(wouldAccept(STATIC, LBRACE)) {
			nextToken();
			Block body;
			try(var $1 = context.enter(Context.STATIC); var $2 = preStmts.enter()) {
				body = parseBlock();
				return applyMemberPreStmts(List.of(new ClassInitializer(true, body)));
			}
		} else if(wouldAccept(LBRACE)) {
			Block body;
			try(var $1 = context.enter(Context.DYNAMIC); var $2 = preStmts.enter()) {
				body = parseBlock();
				return applyMemberPreStmts(List.of(new ClassInitializer(false, body)));
			}
		} else {
			var docComment = getDocComment();
			// TODO
			return parseClassMember(inInterface, docComment, parseKeywordModsAndAnnotations());
		}
	}
	
	protected List<Member> applyMemberPreStmts(List<Member> members) {
		if(preStmts.isWithinContext() && !preStmts.isEmpty()) {
			var newmembers = new ArrayList<>(members);
			var stmts = preStmts.get();
			boolean isStatic = context.current() == Context.STATIC;
			for(int i = stmts.size()-1; i >= 0; i--) {
				var stmt = stmts.get(i);
				if(stmt instanceof VariableDecl) {
					var varDecl = (VariableDecl)stmt;
					if(isStatic) {
						if(!varDecl.hasModifier("static")) {
							varDecl.getModifiers().add(createModifier("static"));
						}
					}
					if(!varDecl.hasVisibilityModifier()) {
						varDecl.getModifiers().add(createModifier("private"));
					}
				} else {
					if(i < stmts.size()-1) {
						var last = stmts.get(i+1);
						if(last instanceof Block) {
							if(stmt instanceof Block) {
								((Block)last).getStatements().addAll(0, ((Block)stmt).getStatements());
							} else {
								((Block)last).getStatements().add(0, stmt);
							}
							stmts.remove(i);
							continue;
						}
					}
					if(!(stmt instanceof Block)) {
						stmts.set(i, new Block(stmt));
					}
				}
			}
			for(int i = stmts.size()-1; i >= 0; i--) {
				var stmt = stmts.get(i);
				if(stmt instanceof Block) {
					newmembers.add(0, new ClassInitializer(isStatic, (Block)stmt));
				} else {
					newmembers.add(0, (VariableDecl)stmt);
				}
			}
			return newmembers;
		} else {
			return members;
		}
	}
	
	@Override
	public List<Member> parseClassMember(boolean inInterface, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(!inInterface && modsAndAnnos.canBeConstructorMods() && wouldAccept(Tag.NAMED, LPAREN)) {
			try(var $1 = context.enter(Context.DYNAMIC); var $2 = preStmts.enter()) {
				return applyMemberPreStmts(parseConstructor(docComment, modsAndAnnos));
			}
		} else if(modsAndAnnos.canBeMethodMods() && wouldAccept(LT)) {
			var typeParameters = parseTypeParameters();
			if(!inInterface && modsAndAnnos.canBeConstructorMods() && wouldAccept(Tag.NAMED, LPAREN)) {
				try(var $1 = context.enter(Context.DYNAMIC); var $2 = preStmts.enter()) {
					return applyMemberPreStmts(parseConstructor(typeParameters, docComment, modsAndAnnos));
				}
			} else {
				var typeAnnotations = parseAnnotations();
				Type type;
				if(accept(VOID)) {
					type = new VoidType(typeAnnotations);
				} else {
					type = parseType(typeAnnotations);
				}
				try(var $1 = preStmts.enter(); var $2 = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
					return applyMemberPreStmts(parseMethod(inInterface, type, typeParameters, parseName(), docComment, modsAndAnnos));
				}
			}
		} else {
			return parseInterfaceMember(inInterface, docComment, modsAndAnnos);
		}
	}
	
	@Override
	public List<Member> parseInterfaceMember(boolean inInterface, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		if(modsAndAnnos.canBeClassMods() && (wouldAccept(CLASS.or(INTERFACE).or(ENUM)) || wouldAccept(AT, INTERFACE) || enabled(ALTERNATE_ANNOTATION_DECL) && wouldAccept(ANNOTATION, Tag.NAMED, LBRACE.or(LPAREN)))) {
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
			try(var $1 = preStmts.enter(); var $2 = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
				return applyMemberPreStmts(parseMethod(inInterface, type, typeParameters, parseName(), docComment, modsAndAnnos));
			}
		} else if(accept(VOID)) {
			try(var $1 = preStmts.enter(); var $2 = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
				return applyMemberPreStmts(parseMethod(inInterface, new VoidType(), emptyList(), parseName(), docComment, modsAndAnnos));
			}
		} else {
			var type = parseType();
			try(var $1 = preStmts.enter(); var $2 = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
    			if(wouldAccept(Tag.NAMED, LPAREN)) {
    				return applyMemberPreStmts(parseMethod(inInterface, type, emptyList(), parseName(), docComment, modsAndAnnos));
    			} else if(modsAndAnnos.canBeFieldMods()) {
    				return applyMemberPreStmts(parseFieldDecl(type, docComment, modsAndAnnos));
    			} else {
    				throw syntaxError("Invalid modifiers");
    			}
			}
		}
	}
	
	@Override
	public <M extends Member> ArrayList<M> parseClassBody(Supplier<? extends List<M>> memberParser) {
		if(enabled(EMPTY_TYPE_BODIES) && accept(SEMI)) {
			return new ArrayList<>(0);
		}
		try(var $1 = scope.enter(Scope.NORMAL); var $2 = context.enter(Context.DYNAMIC)) {
			require(LBRACE);
			var members = new ArrayList<M>();
			List<Modifier> parentModifiers = emptyList();
			while(wouldNotAccept(RBRACE)) {
				if(!accept(SEMI)) {
					List<M> parsedMembers;
					if(enabled(DEFAULT_MODIFIERS)) {
						defaultmods:
						if(wouldAccept(KEYWORD_MODIFIER)) {
    						try(var state = tokens.enter()) {
    							var mods = new ArrayList<Modifier>();
    							do {
    								mods.add(createModifier(token));
    								nextToken();
    							} while(wouldAccept(KEYWORD_MODIFIER));
    							if(accept(COLON)) {
    								parentModifiers = mods;
    								parsedMembers = memberParser.get();
    								break defaultmods;
    							} else {
    								state.reset();
    							}
    						}
    						parsedMembers = memberParser.get();
						} else {
							parsedMembers = memberParser.get();
						}
						for(var member : parsedMembers) {
							if(member instanceof Modified) {
								mergeModifiers((Modified)member, parentModifiers);
							}
						}
					} else {
						parsedMembers = memberParser.get();
					}
					members.addAll(parsedMembers);
				}
			}
			require(RBRACE);
			return members;
		}
	}
	
	@Override
	public EnumDecl parseEnumDecl(Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		try(var $1 = scope.enter(Scope.NORMAL); var $2 = context.enter(Context.DYNAMIC)) {
			return super.parseEnumDecl(docComment, modsAndAnnos);
		}
	}
	
	@Override
	public Pair<List<EnumField>, List<Member>> parseEnumBody() {
		if(enabled(EMPTY_TYPE_BODIES) && accept(SEMI)) {
			return Pair.of(emptyList(), emptyList());
		}
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
			List<Modifier> parentModifiers = emptyList();
			while(wouldNotAccept(RBRACE)) {
				if(!accept(SEMI)) {
					List<Member> parsedMembers;
					if(enabled(DEFAULT_MODIFIERS)) {
						defaultmods:
						if(wouldAccept(KEYWORD_MODIFIER)) {
    						try(var state = tokens.enter()) {
    							var mods = new ArrayList<Modifier>();
    							do {
    								mods.add(createModifier(token));
    								nextToken();
    							} while(wouldAccept(KEYWORD_MODIFIER));
    							if(accept(COLON)) {
    								parentModifiers = mods;
    								parsedMembers = parseClassMember(false);
    								break defaultmods;
    							} else {
    								state.reset();
    							}
    						}
    						parsedMembers = parseClassMember(false);
						} else {
							parsedMembers = parseClassMember(false);
						}
						for(var member : parsedMembers) {
							if(member instanceof Modified) {
								mergeModifiers((Modified)member, parentModifiers);
							}
						}
					} else {
						parsedMembers = parseClassMember(false);
					}
					members.addAll(parsedMembers);
				}
			}
		} else {
			members = emptyList();
		}
		require(RBRACE);
		return Pair.of(fields, members);
	}
	
	protected boolean isVisibilityModifier(Modifier modifier) {
		return switch(modifier.toCode()) {
			case "public", "private", "protected", "package" -> true;
			default -> false;
		};
	}
	
	protected boolean isValidConstructorModifier(Modifier modifier) {
		return isVisibilityModifier(modifier);
	}
	
	protected boolean isValidMethodModifier(Modifier modifier) {
		return switch(modifier.toCode()) {
			case "public", "private", "protected", "package",
					"static", "final", "synchronized", "strictfp",
					"native", "abstract", "default" -> true;
			default -> false;
		};
	}
	
	protected boolean isValidClassModifier(Modifier modifier) {
		return switch(modifier.toCode()) {
			case "public", "private", "protected", "package",
					"static", "final", "abstract", "strictfp" -> true;
			default -> false;
		};
	}
	
	protected boolean isValidFieldModifier(Modifier modifier) {
		return switch(modifier.toCode()) {
			case "public", "private", "protected", "package",
					"static", "final", "transient", "volatile" -> true;
			default -> false;
		};
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
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
				if(enabled(DEFAULT_MODIFIERS) && mods.contains("non-" + token.getString())) {
					throw syntaxError("Incompatible modifiers 'non-" + token.getString() + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(enabled(DEFAULT_MODIFIERS) && wouldAcceptPseudoOp(test("non"), SUB, KEYWORD_MODIFIER)) {
				nextToken(2);
				if(mods.contains(token.getString())) {
					throw syntaxError("Incompatible modifiers '" + token.getString() + "' and 'non-" + token.getString() + "'");
				}
				if(!mods.add(createModifier("non-" + token.getString()))) {
					throw syntaxError("Duplicate modifier 'non-" + token.getString() + "'");
				}
				nextToken();
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos);
			}
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
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
				if(enabled(DEFAULT_MODIFIERS) && mods.contains("non-" + token.getString())) {
					throw syntaxError("Incompatible modifiers 'non-" + token.getString() + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(enabled(DEFAULT_MODIFIERS) && wouldAcceptPseudoOp(test("non"), SUB, Tag.CLASS_MODIFIER)) {
				nextToken(2);
				if(mods.contains(token.getString())) {
					throw syntaxError("Incompatible modifiers '" + token.getString() + "' and 'non-" + token.getString() + "'");
				}
				if(!mods.add(createModifier("non-" + token.getString()))) {
					throw syntaxError("Duplicate modifier 'non-" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.CLASS));
			}
		}
	}
	
	public ModsAndAnnotations parseAnnotationPropertyModsAndAnnotations() {
		var mods = new LinkedHashSet<Modifier>(3);
		var annos = new ArrayList<Annotation>(3);
		Token<JavaTokenType> visibilityModifier = null;
		boolean foundAbstract = false;
		
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
			} else if(wouldAccept(ABSTRACT)) {
				if(foundAbstract) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				foundAbstract = true;
				mods.add(createModifier(token));
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos);
			}
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
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
				if(enabled(DEFAULT_MODIFIERS) && mods.contains("non-" + token.getString())) {
					throw syntaxError("Incompatible modifiers 'non-" + token.getString() + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(enabled(DEFAULT_MODIFIERS) && wouldAcceptPseudoOp(test("non"), SUB, Tag.METHOD_MODIFIER)) {
				nextToken(2);
				if(mods.contains(token.getString())) {
					throw syntaxError("Incompatible modifiers '" + token.getString() + "' and 'non-" + token.getString() + "'");
				}
				if(!mods.add(createModifier("non-" + token.getString()))) {
					throw syntaxError("Duplicate modifier 'non-" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.METHOD));
			}
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
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
				if(enabled(DEFAULT_MODIFIERS) && mods.contains("non-" + token.getString())) {
					throw syntaxError("Incompatible modifiers 'non-" + token.getString() + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(enabled(DEFAULT_MODIFIERS) && wouldAcceptPseudoOp(test("non"), SUB, Tag.CONSTRUCTOR_MODIFIER)) {
				nextToken(2);
				if(mods.contains(token.getString())) {
					throw syntaxError("Incompatible modifiers '" + token.getString() + "' and 'non-" + token.getString() + "'");
				}
				if(!mods.add(createModifier("non-" + token.getString()))) {
					throw syntaxError("Duplicate modifier 'non-" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.CONSTRUCTOR));
			}
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
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
				if(enabled(DEFAULT_MODIFIERS) && mods.contains("non-" + token.getString())) {
					throw syntaxError("Incompatible modifiers 'non-" + token.getString() + "' and '" + token.getString() + "'");
				}
				if(!mods.add(createModifier(token))) {
					throw syntaxError("Duplicate modifier '" + token.getString() + "'");
				}
				nextToken();
			} else if(enabled(DEFAULT_MODIFIERS) && wouldAcceptPseudoOp(test("non"), SUB, Tag.FIELD_MODIFIER)) {
				nextToken(2);
				if(mods.contains(token.getString())) {
					throw syntaxError("Incompatible modifiers '" + token.getString() + "' and 'non-" + token.getString() + "'");
				}
				if(!mods.add(createModifier("non-" + token.getString()))) {
					throw syntaxError("Duplicate modifier 'non-" + token.getString() + "'");
				}
				nextToken();
			} else if(wouldAccept(KEYWORD_MODIFIER)) {
				throw syntaxError("Modifier '" + token.getString() + "' not allowed here");
			} else {
				return new ModsAndAnnotations(new ArrayList<>(mods), annos, EnumSet.of(ModsAndAnnotations.Type.FIELD));
			}
		}
	}
	
	@Override
	public Modifier createModifier(JavaTokenType type) {
		return new JPPModifier(switch(type) {
			case PUBLIC -> Modifiers.PUBLIC;
			case PRIVATE -> Modifiers.PRIVATE;
			case PROTECTED -> Modifiers.PROTECTED;
			case PACKAGE -> Modifiers.PACKAGE;
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

	@Override
	public Modifier createModifier(String name) {
		return new JPPModifier(Modifiers.fromString(name));
	}

	protected void mergeModifiers(Modified member, List<Modifier> parentMods) {
		var memberMods = member.getModifiers();
		Predicate<Modifier> filter;
		if(member instanceof ConstructorDecl) {
			filter = this::isValidConstructorModifier;
		} else if(member instanceof FunctionDecl) {
			filter = this::isValidMethodModifier;
		} else if(member instanceof VariableDecl) {
			filter = this::isValidFieldModifier;
		} else if(member instanceof TypeDecl) {
			filter = this::isValidClassModifier;
		} else if(member instanceof AnnotationProperty) {
			filter = modifier -> switch(modifier.toCode()) {
				case "public", "abstract" -> true;
				default -> false;
			};
		} else {
			filter = modifier -> true;
		}
		boolean hasVisibility = member.hasVisibilityModifier();
		memberMods.addAll(0, parentMods.stream().filter(modifier -> {
			if(!filter.test(modifier)) {
				return false;
			}
			if(isVisibilityModifier(modifier)) {
				return !hasVisibility;
			} else {
				if(member.hasModifier("non-" + modifier.toCode())) {
					return false;
				}
				return !member.hasModifier(modifier.toCode());
			}
		}).collect(Collectors.toList()));
		memberMods.removeIf(mod -> mod.toCode().startsWith("non-"));
	}
	
	protected ContextStack<List<FormalParameter>> functionParameters = new ContextStack<>();

	@Override
	public Block parseConstructorBody(List<FormalParameter> parameters) {
		if(enabled(EMPTY_CONSTRUCTOR_BODIES)) {
			if(accept(SEMI)) {
				return new Block();
			}
			if(accept(COLON)) {
				Optional<Expression> object;
				if(wouldAccept(SUPER.or(THIS).or(LT))) {
					object = Optional.empty();
				} else {
					object = Optional.of(parseSuffix());
					require(DOT);
				}
				List<? extends TypeArgument> typeArguments = parseTypeArgumentsOpt();
				ConstructorCall.Type callType;
				if(object.isEmpty()) {
    				if(accept(SUPER)) {
    					callType = ConstructorCall.Type.SUPER;
    				} else {
    					require(THIS);
    					callType = ConstructorCall.Type.THIS;
    				}
				} else {
					require(SUPER);
					callType = ConstructorCall.Type.SUPER;
				}
				List<Expression> args;
				if(wouldAccept(LPAREN) || callType == ConstructorCall.Type.THIS) {
					try(var $ = functionParameters.enter(parameters)) {
						args = parseConstructorArguments();
					}
				} else {
					args = new ArrayList<>();
					for(var param : parameters) {
						args.add(new Variable(param.getName()));
					}
				}
				endStatement();
				return new Block(new ConstructorCall(object, typeArguments, callType, args));
			}
		}
		try(var $ = functionParameters.enter(parameters)) {
			return parseBlock();
		}
	}
	
	@Override
	public List<Expression> parseConstructorArguments() {
		if(functionParameters.isEmpty() || !enabled(IMPROVED_CONSTRUCTOR_CALL_ARGUMENTS)) {
			return parseArguments(false);
		}
		require(LPAREN);
		var args = new ArrayList<Expression>();
		var parameters = functionParameters.current();
		if(!wouldAccept(RPAREN)) {
			boolean hadStar = false;
			if(wouldAccept(UNDERSCORE)) {
				if(args.size() > parameters.size()) {
					throw syntaxError("Cannot use _ for argument #" + args.size() + " when there are only " + parameters.size() + " parameters");
				}
				nextToken();
				args.add(new Variable(parameters.get(args.size()).getName()));
			} else if(wouldAccept(STAR, COMMA.or(RPAREN))) {
				hadStar = true;
				nextToken();
				for(var param : parameters) {
					args.add(new Variable(param.getName()));
				}
			} else {
				args.add(parseArgument());
			}
			while(accept(COMMA)) {
				if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
					break;
				}
				if(wouldAccept(UNDERSCORE)) {
					if(args.size() >= parameters.size()) {
						throw syntaxError("Cannot use _ for argument #" + (args.size()+1) + " when there are only " + parameters.size() + " parameters");
					}
					nextToken();
					args.add(new Variable(parameters.get(args.size()).getName()));
				} else if(wouldAccept(STAR, COMMA.or(RPAREN))) {
					if(hadStar) {
						throw syntaxError("Cannot use * more than once in explicit constructor call");
					} else {
						hadStar = true;
					}
					nextToken();
					for(var param : parameters) {
						args.add(new Variable(param.getName()));
					}
				} else {
					args.add(parseArgument());
				}
			}
		}
		require(RPAREN);
		if(enabled(LAST_LAMBDA_ARGUMENT) && wouldAccept(LBRACE)) {
			args.add(new Lambda(Either.second(emptyList()), Either.first(parseBlock())));
		}
		return args;
	}

	@Override
	public Optional<Block> parseMethodBody(boolean isVoidMethod, List<FormalParameter> parameters) {
		if(accept(SEMI)) {
			return Optional.empty();
		} else {
			try(var $ = functionParameters.enter(parameters)) {
    			if(enabled(SIMPLE_METHOD_BODIES) && accept(ARROW)) {
    				var expr = parseExpression();
    				endStatement();
    				return Optional.of(new Block(isVoidMethod? new ExpressionStmt(expr) : new ReturnStmt(expr)));
    			} else {
    				return Optional.of(parseBlock());
    			}
			}
		}
	}
	
	@Override
	public ArrayList<FormalParameter> parseFormalParameterList() {
		var params = new ArrayList<FormalParameter>();
		var eitherParam = parseFormalParameterWithOptDefault();
		while(eitherParam.isSecond()) {
			FormalParameter param = eitherParam.second();
			params.add(param);
			if(param.isVariadic()) {
				break;
			}
			if(!accept(COMMA)) {
				break;
			}
			if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
				break;
			}
			eitherParam = parseFormalParameterWithOptDefault();
		}
		if(eitherParam.isFirst()) {
			FormalParameter param = eitherParam.first();
			params.add(param);
			while(!param.isVariadic() && accept(COMMA)) {
				if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
					break;
				}
				params.add(param = parseFormalParameterWithDefault());
			}
		}
		return params;
	}
	
	public Either<DefaultFormalParameter,FormalParameter> parseFormalParameterWithOptDefault() {
		return parseFormalParameterWithOptDefault(parseFinalAndAnnotations());
	}
	
	@SuppressWarnings("unchecked")
	public Either<DefaultFormalParameter,FormalParameter> parseFormalParameterWithOptDefault(ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeLocalVarMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var type = parseType();
		boolean variadic = accept(ELLIPSIS);
		var name = parseName();
		var dimensions = parseDimensions();
		if(enabled(DEFAULT_ARGUMENTS) && accept(EQ)) {
			Initializer initializer = parseInitializer(variadic || type instanceof ArrayType || !dimensions.isEmpty());
			Expression defaultValue;
			if(initializer instanceof ArrayInitializer) {
				var dimensions2 = dimensions.stream().map(Dimension::clone).collect(Collectors.toCollection(ArrayList<Dimension>::new));
				Type baseType;
				if(type instanceof ArrayType) {
					var arrayType = (ArrayType)type;
					baseType = arrayType.getBaseType();
					dimensions2.addAll(0, arrayType.getDimensions().stream().map(Dimension::clone).collect(Collectors.toList()));
				} else {
					baseType = type;
				}
				if(variadic) {
					dimensions2.add(new Dimension());
				}
				defaultValue = new ArrayCreator(baseType, (ArrayInitializer<? extends Initializer>)initializer, dimensions2);
			} else {
				defaultValue = (Expression)initializer;
			}
			return Either.first(new DefaultFormalParameter(type, name, variadic, dimensions, defaultValue, modifiers, annotations));
		} else {
			return Either.second(new FormalParameter(type, name, variadic, dimensions, modifiers, annotations));
		}
	}
	
	public FormalParameter parseFormalParameterWithDefault() {
		return parseFormalParameterWithDefault(parseFinalAndAnnotations());
	}
	
	@SuppressWarnings("unchecked")
	public FormalParameter parseFormalParameterWithDefault(ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeLocalVarMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		var type = parseType();
		boolean variadic = accept(ELLIPSIS);
		var name = parseName();
		var dimensions = parseDimensions();
		if(variadic) {
			if(!accept(EQ)) {
				return new FormalParameter(type, name, variadic, dimensions, modifiers, annotations);
			}
		} else {
			require(EQ);
		}
		Initializer initializer = parseInitializer(variadic || type instanceof ArrayType || !dimensions.isEmpty());
		Expression defaultValue;
		if(initializer instanceof ArrayInitializer) {
			var dimensions2 = dimensions.stream().map(Dimension::clone).collect(Collectors.toCollection(ArrayList<Dimension>::new));
			Type baseType;
			if(type instanceof ArrayType) {
				var arrayType = (ArrayType)type;
				baseType = arrayType.getBaseType();
				dimensions2.addAll(0, arrayType.getDimensions().stream().map(Dimension::clone).collect(Collectors.toList()));
			} else {
				baseType = type;
			}
			if(variadic) {
				dimensions2.add(new Dimension());
			}
			defaultValue = new ArrayCreator(baseType, (ArrayInitializer<? extends Initializer>)initializer, dimensions2);
		} else {
			defaultValue = (Expression)initializer;
		}
		return new DefaultFormalParameter(type, name, variadic, dimensions, defaultValue, modifiers, annotations);
	}
	
	protected int firstDefaultIndex(List<FormalParameter> parameters) {
		for(int firstDefaultIndex = 0; firstDefaultIndex < parameters.size(); firstDefaultIndex++) {
			if(parameters.get(firstDefaultIndex) instanceof DefaultFormalParameter) {
				return firstDefaultIndex;
			}
		}
		return -1;
	}
	
	@Override
	public List<Member> parseMethod(boolean inInterface, Type returnType, List<TypeParameter> typeParameters,
										  Name name, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		var methods = super.parseMethod(inInterface, returnType, typeParameters, name, docComment, modsAndAnnos);
		if(methods.size() == 1) {
			var method = (FunctionDecl)methods.get(0);
			if(inInterface && enabled(AUTO_DEFAULT_MODIFIER) && method.getBody().isPresent() && !method.hasModifier("static") && !method.hasModifier("default") && !method.hasModifier("non-default") && !method.hasModifier("abstract")) {
				method.getModifiers().add(createModifier("default"));
			}
			var parameters = method.getParameters();
			int firstDefaultIndex = firstDefaultIndex(parameters);
			if(firstDefaultIndex >= 0) {
				methods = new ArrayList<>();
				methods.add(method);
				var lastParam = parameters.get(parameters.size()-1);
				if(firstDefaultIndex + 1 < parameters.size() && !(lastParam instanceof DefaultFormalParameter)) {
					assert lastParam.isVariadic();
					for(int i = firstDefaultIndex; i < parameters.size()-1; i++) {
						var newparams = new ArrayList<FormalParameter>();
						var args = new ArrayList<Expression>();
						for(int j = 0; j < i; j++) {
							var param = parameters.get(j);
							if(param instanceof DefaultFormalParameter) {
								newparams.add(((DefaultFormalParameter)param).toFormalParameter());
							} else {
								newparams.add(param.clone());
							}
							args.add(new Variable(param.getName()));
						}
						for(int j = i; j < parameters.size()-1; j++) {
							args.add(((DefaultFormalParameter)parameters.get(j)).getDefaultValue());
						}
						newparams.add(lastParam.clone());
						args.add(new Variable(lastParam.getName()));
						methods.add(makeDelegateCall(inInterface, method, newparams, args));
					}
				} else {
					for(int i = firstDefaultIndex; i < parameters.size(); i++) {
						var newparams = new ArrayList<FormalParameter>();
						var args = new ArrayList<Expression>();
						for(int j = 0; j < i; j++) {
							var param = parameters.get(j);
							if(param instanceof DefaultFormalParameter) {
								newparams.add(((DefaultFormalParameter)param).toFormalParameter());
							} else {
								newparams.add(param.clone());
							}
							args.add(new Variable(param.getName()));
						}
						for(int j = i; j < parameters.size(); j++) {
							args.add(((DefaultFormalParameter)parameters.get(j)).getDefaultValue());
						}
						methods.add(makeDelegateCall(inInterface, method, newparams, args));
					}
					if(lastParam.isVariadic()) {
						for(int i = firstDefaultIndex; i < parameters.size()-1; i++) {
							var newparams = new ArrayList<FormalParameter>();
							var args = new ArrayList<Expression>();
							for(int j = 0; j < i; j++) {
								var param = parameters.get(j);
								if(param instanceof DefaultFormalParameter) {
									newparams.add(((DefaultFormalParameter)param).toFormalParameter());
								} else {
									newparams.add(param.clone());
								}
								args.add(new Variable(param.getName()));
							}
							for(int j = i; j < parameters.size()-1; j++) {
								args.add(((DefaultFormalParameter)parameters.get(j)).getDefaultValue());
							}
							newparams.add(((DefaultFormalParameter)lastParam).toFormalParameter());
							args.add(new Variable(lastParam.getName()));
							methods.add(makeDelegateCall(inInterface, method, newparams, args));
						}
					}
				}
				method.setParameters(parameters.stream().map(param -> param instanceof DefaultFormalParameter? ((DefaultFormalParameter)param).toFormalParameter() : param).collect(Collectors.toList()));
			}
		}
		return methods;
	}
	
	protected FunctionDecl makeDelegateCall(boolean inInterface, FunctionDecl base, List<FormalParameter> parameters,
											List<Expression> arguments) {
		var funcCall = new FunctionCall(base.getName(), arguments);
		var result = new FunctionDecl(base.getName(), Node.clone(base.getTypeParameters()), base.getReturnType().clone(),
				Node.clone(base.getThisParameter()), parameters, Node.clone(base.getDimensions()),
				Node.clone(base.getExceptions()),
				new Block(base.getReturnType() instanceof VoidType? new ExpressionStmt(funcCall) : new ReturnStmt(funcCall)),
				Node.clone(base.getModifiers()), Node.clone(base.getAnnotations()), base.getDocComment());
		if(inInterface && !result.hasModifier("default")) {
			result.getModifiers().add(createModifier("default"));
		}
		return result;
	}

	@Override
	public List<Member> parseConstructor(Name name, List<TypeParameter> typeParameters,
												  Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		var methods = super.parseConstructor(name, typeParameters, docComment, modsAndAnnos);
		if(methods.size() == 1) {
			var method = (ConstructorDecl)methods.get(0);
			var parameters = method.getParameters();
			int firstDefaultIndex = firstDefaultIndex(parameters);
			if(firstDefaultIndex >= 0) {
				methods = new ArrayList<>();
				methods.add(method);
				var lastParam = parameters.get(parameters.size()-1);
				if(firstDefaultIndex + 1 < parameters.size() && !(lastParam instanceof DefaultFormalParameter)) {
					assert lastParam.isVariadic();
					for(int i = firstDefaultIndex; i < parameters.size()-2; i++) {
						var newparams = new ArrayList<FormalParameter>();
						var args = new ArrayList<Expression>();
						for(int j = 0; j < i; j++) {
							var param = parameters.get(j);
							if(param instanceof DefaultFormalParameter) {
								newparams.add(((DefaultFormalParameter)param).toFormalParameter());
							} else {
								newparams.add(param.clone());
							}
							args.add(new Variable(param.getName()));
						}
						for(int j = i; j < parameters.size()-1; j++) {
							args.add(((DefaultFormalParameter)parameters.get(j)).getDefaultValue());
						}
						newparams.add(lastParam.clone());
						args.add(new Variable(lastParam.getName()));
						methods.add(makeDelegateCall(method, newparams, args));
					}
				} else {
					for(int i = firstDefaultIndex; i < parameters.size()-1; i++) {
						var newparams = new ArrayList<FormalParameter>();
						var args = new ArrayList<Expression>();
						for(int j = 0; j < i; j++) {
							var param = parameters.get(j);
							if(param instanceof DefaultFormalParameter) {
								newparams.add(((DefaultFormalParameter)param).toFormalParameter());
							} else {
								newparams.add(param.clone());
							}
							args.add(new Variable(param.getName()));
						}
						for(int j = i; j < parameters.size(); j++) {
							args.add(((DefaultFormalParameter)parameters.get(j)).getDefaultValue());
						}
						methods.add(makeDelegateCall(method, newparams, args));
					}
					if(lastParam.isVariadic()) {
						for(int i = firstDefaultIndex; i < parameters.size()-1; i++) {
							var newparams = new ArrayList<FormalParameter>();
							var args = new ArrayList<Expression>();
							for(int j = 0; j < i; j++) {
								var param = parameters.get(j);
								if(param instanceof DefaultFormalParameter) {
									newparams.add(((DefaultFormalParameter)param).toFormalParameter());
								} else {
									newparams.add(param.clone());
								}
								args.add(new Variable(param.getName()));
							}
							for(int j = i; j < parameters.size()-1; j++) {
								args.add(((DefaultFormalParameter)parameters.get(j)).getDefaultValue());
							}
							newparams.add(((DefaultFormalParameter)lastParam).toFormalParameter());
							args.add(new Variable(lastParam.getName()));
							methods.add(makeDelegateCall(method, newparams, args));
						}
					}
				}
				method.setParameters(parameters.stream().map(param -> param instanceof DefaultFormalParameter? ((DefaultFormalParameter)param).toFormalParameter() : param).collect(Collectors.toList()));
			}
		}
		return methods;
	}
	
	protected ConstructorDecl makeDelegateCall(ConstructorDecl base, List<FormalParameter> parameters,
											   List<Expression> arguments) {
		return new ConstructorDecl(base.getName(), Node.clone(base.getTypeParameters()),
				Node.clone(base.getThisParameter()), parameters, Node.clone(base.getExceptions()),
				new Block(new ConstructorCall(ConstructorCall.Type.THIS, arguments)), Node.clone(base.getModifiers()),
				Node.clone(base.getAnnotations()), base.getDocComment());
	}

	@Override
	public List<Member> parseFieldDecl() {
		var docComment = getDocComment();
		var modsAndAnnos = parseFieldModsAndAnnotations();
		try(var $ = context.enter(modsAndAnnos.hasModifier("static")? Context.STATIC : Context.DYNAMIC)) {
			return parseFieldDecl(docComment, modsAndAnnos);
		}
	}
	
	@Override
	public List<Member> parseFieldDecl(Type type, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		try(var $ = preStmts.enter()) {
			var decl = parseVariableDecl(type, docComment, modsAndAnnos);
			if(preStmts.isEmpty()) {
				return List.of(decl);
			} else {
				var members = new ArrayList<Member>();
				for(var stmt : preStmts) {
					if(stmt instanceof VariableDecl) {
						var varDecl = (VariableDecl)stmt;
						if(!varDecl.hasVisibilityModifier()) {
							varDecl.getModifiers().add(createModifier(PRIVATE));
						}
						members.add(varDecl);
					} else {
						if(members.isEmpty()) {
							members.add(new ClassInitializer(context.current() == Context.STATIC, stmt instanceof Block? (Block)stmt : new Block(stmt)));
						} else {
							var lastMember = members.get(members.size()-1);
							if(lastMember instanceof ClassInitializer) {
								var classInitializer = (ClassInitializer)lastMember;
								if(classInitializer.isStatic() == (context.current() == Context.STATIC)) {
									classInitializer.getBlock().getStatements().add(stmt);
								} else {
									members.add(new ClassInitializer(context.current() == Context.STATIC, stmt instanceof Block? (Block)stmt : new Block(stmt)));
								}
							} else {
								members.add(new ClassInitializer(context.current() == Context.STATIC, stmt instanceof Block? (Block)stmt : new Block(stmt)));
							}
						}
					}
				}
				members.add(decl);
				return members;
			}
		}
	}
	
	@Override
	public VariableDecl parseVariableDecl(Type type, Optional<String> docComment, ModsAndAnnotations modsAndAnnos) {
		assert modsAndAnnos.canBeFieldMods();
		var modifiers = modsAndAnnos.mods;
		var annotations = modsAndAnnos.annos;
		boolean isArray = type instanceof ArrayType;
		var declarators = new ArrayList<VariableDeclarator>();
		declarators.add(parseVariableDeclarator(isArray));
		while(accept(COMMA)) {
			if(enabled(TRAILING_COMMAS) && !wouldAccept(Tag.NAMED)) {
				break;
			}
			declarators.add(parseVariableDeclarator(isArray));
		}
		endStatement();
		return new VariableDecl(type, declarators, modifiers, annotations, docComment);
	}
	
	protected Expression wrapInNot(Expression expr) {
		/*if(expr instanceof TypeTest || expr instanceof BinaryExpr || expr instanceof AssignExpr 
				|| expr instanceof UnaryExpr || expr instanceof Lambda) {
			expr = new ParensExpr(expr);
		}*/
		if(expr instanceof UnaryExpr && ((UnaryExpr)expr).getOperation() == UnaryExpr.Op.NOT) {
			return ((UnaryExpr)expr).getOperand();
		} else {
			return new UnaryExpr(UnaryExpr.Op.NOT, expr);
		}
	}
	
	@Override
	public void endStatement() {
		if(enabled(IMPLICIT_SEMICOLONS) && (wouldAccept(RBRACE.or(ENDMARKER)) || tokens.look(-2).getType() == RBRACE)) {
			accept(SEMI);
		} else {
			require(SEMI);
		}
	}
	
	protected Expression makeQualifier(String fullyQualifiedName) {
		if(enabled(FULLY_QUALIFIED_NAMES)) {
			return makeMemberAccess(fullyQualifiedName);
		} else {
			return new Variable(fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.')+1));
		}
	}
	
	protected Expression makeQualifier(QualifiedName fullyQualifiedName) {
		if(enabled(FULLY_QUALIFIED_NAMES)) {
			return makeMemberAccess(fullyQualifiedName);
		} else {
			return new Variable(fullyQualifiedName.lastName());
		}
	}
	
	protected Expression makeImportedQualifier(String fullyQualifiedName) {
		if(enabled(FULLY_QUALIFIED_NAMES) && !imported(fullyQualifiedName)) {
			return makeMemberAccess(fullyQualifiedName);
		} else {
			if(importedNameOtherThan(fullyQualifiedName)) {
				return makeMemberAccess(fullyQualifiedName);
			} else {
				imports.add(new ImportDecl(QualifiedName(fullyQualifiedName)));
				return new Variable(fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.')+1));
			}
		}
	}
	
	protected Expression makeImportedQualifier(QualifiedName fullyQualifiedName) {
		if(enabled(FULLY_QUALIFIED_NAMES) && !imported(fullyQualifiedName)) {
			return makeMemberAccess(fullyQualifiedName);
		} else {
			if(importedNameOtherThan(fullyQualifiedName)) {
				return makeMemberAccess(fullyQualifiedName);
			} else {
				imports.add(new ImportDecl(fullyQualifiedName));
				return new Variable(fullyQualifiedName.lastName());
			}
		}
	}
	
	protected QualifiedName makeImportedQualifiedName(QualifiedName fullyQualifiedName) {
		if(enabled(FULLY_QUALIFIED_NAMES) && !imported(fullyQualifiedName)) {
			return fullyQualifiedName;
		} else {
			if(importedNameOtherThan(fullyQualifiedName)) {
				return fullyQualifiedName;
			} else {
				imports.add(new ImportDecl(fullyQualifiedName));
				return fullyQualifiedName.lastName().toQualifiedName();
			}
		}
	}
	
	@Override
	public Type parseType(List<Annotation> annotations) {
		var base = parseNonArrayType(annotations);
		if(wouldAccept(AT.or(LBRACKET))) {
			var dimensions = parseDimensions();
			base.setAnnotations(emptyList());
			Type type = new ArrayType(base, dimensions, annotations);
			if(enabled(OPTIONAL_LITERALS) && wouldAccept(QUES)) {
				base = type;
				var qualifier = makeImportedQualifiedName(QualNames.java_util_Optional);
				for(;;) {
        			while(accept(QUES)) {
        				type = new GenericType(qualifier, List.of(type));
        			}
        			if(wouldAccept(AT.or(LBRACKET))) {
        				base.setAnnotations(emptyList());
        				dimensions = parseDimensions();
        				type = base = new ArrayType(type, dimensions, annotations);
        				if(!wouldAccept(QUES)) {
        					break;
        				}
        			} else {
        				break;
        			}
				}
			}
			return type;
		} else {
			return base;
		}
	}
	
	@Override
	public Type parseNonArrayType(List<Annotation> annotations) {
		if(enabled(OPTIONAL_LITERALS)) {
			Type type;
			if(accept(INT, QUES)) {
				type = new GenericType(makeImportedQualifiedName(QualNames.java_util_OptionalInt), emptyList(), annotations);
			} else if(accept(DOUBLE, QUES)) {
				type = new GenericType(makeImportedQualifiedName(QualNames.java_util_OptionalDouble), emptyList(), annotations);
			} else if(accept(LONG, QUES)) {
				type = new GenericType(makeImportedQualifiedName(QualNames.java_util_OptionalLong), emptyList(), annotations);
			} else if(wouldAccept(PRIMITIVE_TYPES)) {
				var name = token.getString();
				nextToken();
				return new PrimitiveType(name, annotations);
			} else {
				type = parseGenericType(annotations);
			}
			if(wouldAccept(QUES)) {
				var qualifier = makeImportedQualifiedName(QualNames.java_util_Optional);
    			while(accept(QUES)) {
    				type = new GenericType(qualifier, List.of(type));
    			}
			}
			return type;
		} else {
			return super.parseNonArrayType(annotations);
		}
	}
	
	@Override
	public ReferenceType parseReferenceType(List<Annotation> annotations) {
		var base = parseNonArrayType(annotations);
		ReferenceType type;
		if(base instanceof PrimitiveType) {
			base.setAnnotations(emptyList());
			var dimension = parseDimension();
			var dimensions = parseDimensions();
			dimensions.add(0, dimension);
			type = new ArrayType(base, dimensions, annotations);
		} else if(wouldAccept(AT.or(LBRACKET))) {
			var dimensions = parseDimensions();
			base.setAnnotations(emptyList());
			type = new ArrayType(base, dimensions, annotations);
		} else {
			type = (GenericType)base;
		}
		base = type;
		if(enabled(OPTIONAL_LITERALS) && wouldAccept(QUES)) {
			var qualifier = makeImportedQualifiedName(QualNames.java_util_Optional);
			for(;;) {
    			while(accept(QUES)) {
    				type = new GenericType(qualifier, List.of(type));
    			}
    			if(wouldAccept(AT.or(LBRACKET))) {
    				base.setAnnotations(emptyList());
    				var dimensions = parseDimensions();
    				base = type = new ArrayType(type, dimensions, annotations);
    				if(!wouldAccept(QUES)) {
    					break;
    				}
    			} else {
    				break;
    			}
			}
		}
		return type;
	}

	protected boolean isMultiVarDecl(Statement stmt) {
		if(!(stmt instanceof VariableDecl)) {
			return false;
		}
		var varDecl = (VariableDecl)stmt;
		if(varDecl.getDeclarators().size() == 1) {
			return false;
		}
		var type = varDecl.getType();
		return type instanceof GenericType && ((GenericType)type).getName().equals(QualNames.var);
	}
	
	@Override
	public Block parseBlock() {
		try(var $1 = preStmts.enter(); var $2 = scope.enter(Scope.NORMAL)) {
			require(LBRACE);
			var stmts = new ArrayList<Statement>();
			while(wouldNotAccept(RBRACE)) {
				var stmt = parseBlockStatement();
				if(enabled(MULTIPLE_VAR_DECLARATIONS) && isMultiVarDecl(stmt)) {
					var varDecl = (VariableDecl)stmt;
					for(var declarator : varDecl.getDeclarators()) {
						stmts.add(new VariableDecl(varDecl.getType().clone(), declarator, Node.clone(varDecl.getModifiers()), Node.clone(varDecl.getAnnotations()), varDecl.getDocComment()));
					}
				} else {
					stmts.add(stmt);
				}
			}
			require(RBRACE);
			return preStmts.apply(new Block(stmts));
		}
	}

	@Override
	public Block parseBodyAsBlock() {
		if(enabled(IMPLICIT_BLOCKS)) {
			var stmt = parseBlockStatement();
			if(stmt instanceof Block) {
				return (Block)stmt;
			} else if(enabled(MULTIPLE_VAR_DECLARATIONS) && isMultiVarDecl(stmt)) {
				var varDecl = (VariableDecl)stmt;
				return new Block(varDecl.getDeclarators().stream()
						.map(declarator -> new VariableDecl(varDecl.getType().clone(), declarator, Node.clone(varDecl.getModifiers()), Node.clone(varDecl.getAnnotations()), varDecl.getDocComment()))
						.collect(Collectors.toList()));
			} else {
				return new Block(stmt);
			}
		} else {
			return parseBlock();
		}
	}
	
	@Override
	public Statement parseStatement() {
		try(var $ = preStmts.enter()) {
    		switch(token.getType()) {
    			case WITH -> {
    				if(enabled(WITH_STATEMENT)) {
    					return preStmts.apply(parseWithStmt());
    				}
    			}
    			case PRINT -> {
    				if(enabled(PRINT_STATEMENT)) {
    					return preStmts.apply(parsePrintStmt());
    				}
    			}
    			case PRINTLN -> {
    				if(enabled(PRINT_STATEMENT)) {
    					return preStmts.apply(parsePrintlnStmt());
    				}
    			}
    			case PRINTF -> {
    				if(enabled(PRINT_STATEMENT)) {
    					return preStmts.apply(parsePrintfStmt(false));
    				}
    			}
    			case PRINTFLN -> {
    				if(enabled(PRINT_STATEMENT)) {
    					return preStmts.apply(parsePrintfStmt(true));
    				}
    			}
    			default -> {}
    		}
		}
		return super.parseStatement();
	}
	
	public Statement parsePrintStmt() {
		require(PRINT);
		var args = parsePrintStmtArgs();
		var qualifier = new MemberAccess(makeQualifier(QualNames.java_lang_System), Names.out);
		var funcName = Names.print;
		switch(args.size()) {
			case 0:
				return new EmptyStmt();
			case 1:
				return new ExpressionStmt(new FunctionCall(qualifier, funcName, args));
			default:
				var stmts = new ArrayList<Statement>();
				for(int i = 0; i < args.size(); i++) {
					if(i != 0) {
						stmts.add(new ExpressionStmt(new FunctionCall(qualifier, funcName, new Literal(' '))));
					}
					stmts.add(new ExpressionStmt(new FunctionCall(qualifier, funcName, args.get(i))));
				}
				return new Block(stmts);
		}
	}
	
	public Statement parsePrintlnStmt() {
		require(PRINTLN);
		var args = parsePrintStmtArgs();
		var qualifier = new MemberAccess(makeQualifier(QualNames.java_lang_System), Names.out);
		var funcName = Names.println;
		switch(args.size()) {
			case 0:
				return new EmptyStmt();
			case 1:
				var arg = args.get(0);
				if(arg instanceof ParensExpr) {
					arg = ((ParensExpr)arg).getExpression();
				}
				return new ExpressionStmt(new FunctionCall(qualifier, funcName, arg));
			default:
				var funcName2 = Names.print;
				var stmts = new ArrayList<Statement>();
				for(int i = 0; i < args.size(); i++) {
					if(i != 0) {
						stmts.add(new ExpressionStmt(new FunctionCall(qualifier, funcName2, new Literal(' '))));
					}
					stmts.add(new ExpressionStmt(new FunctionCall(qualifier, i+1 == args.size()? funcName : funcName2, args.get(i))));
				}
				return new Block(stmts);
		}
	}
	
	protected List<Expression> parsePrintStmtArgs() {
		if(accept(SEMI)) {
			return emptyList();
		} else {
			var args = new ArrayList<Expression>();
			args.add(parseExpression());
			if(accept(COMMA)) {
				do {
					if(enabled(TRAILING_COMMAS) && wouldAccept(SEMI)) {
						break;
					}
					args.add(parseExpression());
				} while(accept(COMMA));
			} else if(!wouldAccept(SEMI) && (!enabled(IMPLICIT_SEMICOLONS) || tokens.look(-2).getType() != RBRACE)) {
				do {
					args.add(parseExpression());
				} while(!wouldAccept(SEMI) && (!enabled(IMPLICIT_SEMICOLONS) || tokens.look(-2).getType() != RBRACE));
			}
			endStatement();
			return args;
		}
	}
	
	public Statement parsePrintfStmt(boolean isPrintfln) {
		require(isPrintfln? PRINTFLN : PRINTF);
		var qualifier = new MemberAccess(makeQualifier(java_lang_System), Names.out);
		var args = new ArrayList<Expression>();
		var format = parseExpression();
		if(isPrintfln) {
			if(!(format instanceof Variable || format instanceof MemberAccess || format instanceof ParensExpr
					|| format instanceof Literal || format instanceof ClassLiteral || format instanceof ArrayCreator
					|| format instanceof ClassCreator)) {
				format = new ParensExpr(format);
			}
			format = new BinaryExpr(format, BinaryExpr.Op.PLUS, new Literal("%n"));
		}
		args.add(format);
		if(accept(COMMA)) {
			do {
				if(enabled(TRAILING_COMMAS) && wouldAccept(SEMI)) {
					break;
				}
				args.add(parseExpression());
			} while(accept(COMMA));
		} else if(!wouldAccept(SEMI) && (!enabled(IMPLICIT_SEMICOLONS) || tokens.look(-2).getType() != RBRACE)) {
			do {
				args.add(parseExpression());
			} while(!wouldAccept(SEMI) && (!enabled(IMPLICIT_SEMICOLONS) || tokens.look(-2).getType() != RBRACE));
		}
		endStatement();
		return new ExpressionStmt(new FunctionCall(qualifier, Names.printf, args));
	}
	
	public Statement parseWithStmt() {
		require(WITH);
		var resources = new ArrayList<ResourceSpecifier>();
		if(enabled(OPTIONAL_STATEMENT_PARENTHESIS) && !wouldAccept(LPAREN)) {
			try(var $ = scope.enter(Scope.CONDITION)) {
				resources.add(parseWithResource(false, 0));
			}
		} else {
			require(LPAREN);
			resources.add(parseWithResource(true, 0));
			while(wouldNotAccept(RPAREN)) {
				resources.add(parseWithResource(true, resources.size()));
			}
			require(RPAREN);
		}
		var body = parseBodyAsBlock();
		return new TryStmt(resources, body);
	}
	
	public ResourceSpecifier parseWithResource(boolean inParens, int count) {
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
    			if(inParens && !wouldAccept(RPAREN)) {
    				endStatement();
    			}
    			return new VariableDecl(type, name, dimensions, init, modsAndAnnos.mods, modsAndAnnos.annos, Optional.empty());
    		}
		}
		var expr = parseExpression();
		if(inParens && !wouldAccept(RPAREN)) {
			endStatement();
		}
		if(expr instanceof MemberAccess || expr instanceof Variable) {
			return new ExpressionStmt(expr);
		} else {
			return new VariableDecl(new GenericType(QualNames.var), Name(syntheticName("with" + count, expr)), expr);
		}
	}
	
	@Override
	public IfStmt parseIfStmt() {
		require(IF);
		Expression condition;
		if(enabled(IF_NOT) && !enabled(OPTIONAL_STATEMENT_PARENTHESIS) && accept(BANG)) {
			condition = wrapInNot(parseCondition());
		} else {
			condition = parseCondition();
		}
		var body = parseBody();
		Optional<Statement> elseBody;
		if(accept(ELSE)) {
			elseBody = Optional.of(parseBody());
		} else {
			elseBody = Optional.empty();
		}
		return new IfStmt(condition, body, elseBody);
	}


	@Override
	public WhileStmt parseWhileStmt() {
		require(WHILE);
		Expression condition;
		if(enabled(IF_NOT) && !enabled(OPTIONAL_STATEMENT_PARENTHESIS) && accept(BANG)) {
			condition = wrapInNot(parseCondition());
		} else {
			condition = parseCondition();
		}
		var body = parseBody();
		return new WhileStmt(condition, body);
	}

	@Override
	public DoStmt parseDoStmt() {
		require(DO);
		var body = parseBody();
		require(WHILE);
		Expression condition;
		if(enabled(IF_NOT) && !enabled(OPTIONAL_STATEMENT_PARENTHESIS) && accept(BANG)) {
			condition = wrapInNot(parseCondition());
		} else {
			condition = parseCondition();
		}
		endStatement();
		return new DoStmt(body, condition);
	}
	
	@Override
	public Statement parseForStmt() {
		if(enabled(EMPTY_FOR) && wouldAccept(FOR, enabled(OPTIONAL_STATEMENT_PARENTHESIS)? LBRACE : not(LPAREN))) {
			require(FOR);
			var body = parseStatement();
			return new ForStmt(Optional.empty(), Optional.empty(), emptyList(), body);
		} else if(enabled(SIMPLER_FOR) && wouldAccept(FOR, LPAREN, Tag.NAMED, IN)) {
			require(FOR, LPAREN);
			var name = parseName();
			var param = new FormalParameter(new GenericType(QualNames.var), name);
			require(IN);
			var iterable = parseExpression();
			require(RPAREN);
			var body = parseStatement();
			return new ForEachStmt(param, iterable, body);
		} else {
			return super.parseForStmt();
		}
	}
	
	@Override
	public SynchronizedStmt parseSynchronizedStmt() {
		if(wouldAccept(SYNCHRONIZED, LBRACE) || enabled(IMPLICIT_BLOCKS) && !enabled(OPTIONAL_STATEMENT_PARENTHESIS) && wouldAccept(SYNCHRONIZED, not(LPAREN)) || wouldAccept(SYNCHRONIZED, Tag.STATEMENT_KW.and(not(Tag.NAMED)))) {
			require(SYNCHRONIZED);
			var lock = context.current() == Context.STATIC && !typeNames.isEmpty()? new ClassLiteral(new GenericType(typeNames.current().toQualifiedName())) : new This();
			var body = parseBodyAsBlock();
			return new SynchronizedStmt(lock, body);
		} else {
			return super.parseSynchronizedStmt();
		}
	}
	
	@Override
	public EmptyStmt parseEmptyStmt() {
		if(enabled(EMPTY_STATEMENTS)) {
			require(SEMI);
			return new EmptyStmt();
		} else {
			throw syntaxError("Use {} for empty statements");
		}
	}
	
	@Override
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
		
		var lastToken = tokens.look(-2);
		if(lastToken.getType() != RBRACE) {
			lastToken = null;
		}
		
		if(enabled(TRY_ELSE) && wouldAccept(ELSE) && lastToken != null && token.getStart().getLine() == lastToken.getEnd().getLine()) {
		elsebody:
			try(var state = tokens.enter()) {
				require(ELSE);
				Block elseBlock;
				try {
					elseBlock = parseBodyAsBlock();
				} catch(SyntaxError e) {
					state.reset();
					break elsebody;
				}
				Optional<Block> finallyBody = parseFinally();
				var successName = Name(syntheticName("trysuccess", elseBlock));
				if(catches.isEmpty()) {
					catches.add(new Catch(new FormalParameter(new GenericType(QualifiedName(enabled(FULLY_QUALIFIED_NAMES)? "java.lang.Throwable" : "Throwable")), successName), new Block(new ExpressionStmt(new AssignExpr(new Variable(successName), new Literal(false))))));
				} else {
    				for(var catchClause : catches) {
    					catchClause.getBody().getStatements().add(0, new ExpressionStmt(new AssignExpr(new Variable(successName), new Literal(false))));
    				}
				}
				
				if(finallyBody.isEmpty()) {
    				var tryStmt = new TryStmt(resources, body, catches, finallyBody);
    				return new Block(new VariableDecl(new PrimitiveType(PrimitiveType.BOOLEAN), successName, new Literal(true)), tryStmt, new IfStmt(new Variable(successName), elseBlock));
				} else {
					var tryStmt = new TryStmt(resources, body, catches, Optional.empty());
    				var block = new Block(new VariableDecl(new PrimitiveType(PrimitiveType.BOOLEAN), successName, new Literal(true)), tryStmt, new IfStmt(new Variable(successName), elseBlock));
    				return new TryStmt(block, emptyList(), finallyBody);
					//finallyBody = Optional.of(new Block(new TryStmt(emptyList(), new Block(new IfStmt(new Variable(successName), elseBlock)), emptyList(), finallyBody)));
					//var tryStmt = new TryStmt(resources, body, catches, finallyBody);
    				//return new Block(new VariableDecl(new PrimitiveType(PrimitiveType.BOOLEAN), successName, new Literal(true)), tryStmt);
				}
			}
		}
		
		Optional<Block> finallyBody = parseFinally();
		if(resources.isEmpty() && catches.isEmpty() && finallyBody.isEmpty()) {
			throw syntaxError("expected 'catch' or 'finally' here, got " + token);
		}
		
		return new TryStmt(resources, body, catches, finallyBody);
	}
	
	@Override
	public List<Catch> parseCatches() {
		var catches = new ArrayList<Catch>();
		while(wouldAccept(CATCH)) {
			if(enabled(DEFAULT_CATCH) && wouldAccept(CATCH, not(LPAREN))) {
				require(CATCH);
				var body = parseBodyAsBlock();
				var param = new FormalParameter(new GenericType(QualifiedName(enabled(FULLY_QUALIFIED_NAMES)? "java.lang.Throwable" : "Throwable")), Name(syntheticName("defaultException", body)));
				catches.add(new Catch(param, body));
				if(wouldAccept(CATCH)) {
					throw syntaxError("default catch must be the last catch");
				}
				break;
			} else {
				catches.add(parseCatch());
			}
		}
		return catches;
	}
	
	@Override
	public Statement parseArrowCaseBody() {
		if(wouldAccept(THROW)) {
			return parseThrowStmt();
		} else if(wouldAccept(LBRACE)) {
			return parseBlock();
		} else {
			if(enabled(IMPLICIT_BLOCKS)) {
				var stmt = parseStatement();
				if(!(stmt instanceof ExpressionStmt)) {
					stmt = new Block(stmt);
				}
				return stmt;
			} else if(enabled(BETTER_ARROW_CASE_BODIES)) {
				switch(token.getType()) {
					case IF, RETURN, TRY, SYNCHRONIZED -> {
						return parseStatement();
					}
					case WITH -> {
						if(enabled(WITH_STATEMENT)) {
							return parseWithStmt();
						}
					}
					default -> {}
				}
			}
			return parseExpressionStmt();
		}
	}
	
	@Override
	public ReturnStmt parseReturnStmt() {
		require(RETURN);
		if(accept(SEMI) || enabled(IMPLICIT_SEMICOLONS) && wouldAccept(RBRACE.or(ENDMARKER))) {
			return new ReturnStmt();
		} else {
			var expr = parseExpression();
			endStatement();
			return new ReturnStmt(expr);
		}
	}

	@Override
	public ArrayList<GenericType> parseGenericTypeList() {
		var types = new ArrayList<GenericType>();
		types.add(parseGenericType());
		while(accept(COMMA)) {
			if(enabled(TRAILING_COMMAS) && !wouldAccept(Tag.NAMED)) {
				break;
			}
			types.add(parseGenericType());
		}
		return types;
	}
	
	protected Expression makeMemberAccess(String qualifier) {
		if(qualifier.indexOf('.') < 0) {
			return new Variable(Name(qualifier));
		} else {
			return makeMemberAccess(QualifiedName(qualifier));
		}
	}
	
	protected Expression makeMemberAccess(QualifiedName qualifier) {
		Expression result = null;
		for(var name : qualifier) {
			if(result == null) {
				result = new Variable(name);
			} else {
				result = new MemberAccess(result, name);
			}
		}
		return result;
	}	
	
	@Override
	public Expression parseCondition() {
		if(enabled(OPTIONAL_STATEMENT_PARENTHESIS)) {
    		try(var $ = scope.enter(Scope.CONDITION)) {
    			return super.parseExpression();
    		}
		} else {
			try(var $ = scope.enter(Scope.NORMAL)) {
				require(LPAREN);
				Expression expr;
				expr = parseExpression();
				require(RPAREN);
				return expr;
			}
		}
	}
	
	@Override
	public Expression parseConditionalExpr() {
		var expr = parseLogicalOrExpr();
		if(accept(QUES)) {
			if(enabled(OPTIONAL_LITERALS)) {
				try(var state = tokens.enter()) {
    				if(accept(LT)) {
    					var annotations = parseAnnotations();
    					if(accept(INT, GT)) {
    						var qualifier = makeImportedQualifier(QualNames.java_util_OptionalInt);
    						return new FunctionCall(qualifier, Names.of, expr);
    					} else if(accept(LONG, GT)) {
    						var qualifier = makeImportedQualifier(QualNames.java_util_OptionalLong);
    						return new FunctionCall(qualifier, Names.of, expr);
    					} else if(accept(DOUBLE, GT)) {
    						var qualifier = makeImportedQualifier(QualNames.java_util_OptionalDouble);
    						return new FunctionCall(qualifier, Names.of, expr);
    					} else {
    						var type = parseTypeArgument(annotations);
    						require(GT);
    						var qualifier = makeImportedQualifier(QualNames.java_util_Optional);
    						boolean hasNonNullAnnotation = false;
    						for(var annotation : annotations) {
    							if(annotation.getType().getName().endsWith(Names.NonNull)) {
    								hasNonNullAnnotation = true;
    							}
    						}
    						return new FunctionCall(qualifier, hasNonNullAnnotation? Names.of : Names.ofNullable, List.of(type), expr);
    					}
    				} else if(wouldAccept(RPAREN.or(RBRACE).or(RBRACKET).or(COMMA).or(SEMI))) {
    					var qualifier = makeImportedQualifier(QualNames.java_util_Optional);
    					return new FunctionCall(qualifier, Names.ofNullable, expr);
    				} else {
    					state.reset();
    				}
				}
			}
			var truepart = parseExpression();
			require(COLON);
			var falsepart = parseLambdaOr(this::parseConditionalExpr);
			return new ConditionalExpr(expr, truepart, falsepart);
		} else {
			return expr;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Expression parseParens() {
		try(var $ = scope.enter(Scope.NORMAL)) {
			require(LPAREN);
			Expression expr;
			if(enabled(VARDECL_EXPRESSIONS) && wouldAccept(AT.or(Tag.LOCAL_VAR_MODIFIER).or(Tag.NAMED).or(Tag.PRIMITIVE_TYPE))) {
				expr = null;
			vardecl:
				try(var state = tokens.enter()) {
					List<Modifier> modifiers;
					List<Annotation> annotations;
					Type type;
					Name name;
					List<Dimension> dimensions;
					try {
						var modsAndAnnos = parseFinalAndAnnotations();
						modifiers = modsAndAnnos.mods;
						annotations = modsAndAnnos.annos;
						type = parseType();
						if(type instanceof GenericType && ((GenericType)type).getName().equals("var")) {
							throw syntaxError("'var' not allowed in variable declaration expressions");
						}
						name = parseName();
						dimensions = parseDimensions();
						if(!wouldAccept(EQ)) {
							state.reset();
							break vardecl;
						}
					} catch(SyntaxError e) {
						state.reset();
						break vardecl;
					}
					var declarator = parseVariableDeclarator(type instanceof ArrayType || !dimensions.isEmpty(), name, dimensions);
					if(preStmts.isWithoutContext()) {
						throw syntaxError("variable declaration expressions are not allowed here");
					}
					var init = declarator.getInitializer().orElseThrow();
					Expression value;
					if(init instanceof ArrayInitializer) {
						Type baseType;
						if(type instanceof ArrayType) {
							var arrayType = (ArrayType)type;
							baseType = arrayType.getBaseType();
							dimensions.addAll(0, arrayType.getDimensions());
						} else {
							baseType = type;
						}
						value = new ArrayCreator(baseType, (ArrayInitializer<Initializer>)init, dimensions);
					} else {
						value = (Expression)init;
					}
					declarator.setInitializer();
					preStmts.append(new VariableDecl(type, declarator, modifiers, annotations));
					expr = new AssignExpr(new Variable(name), value);
				}
				if(expr == null) {
					expr = parseExpression();
				}
			} else {
				expr = parseExpression();
			}
			require(RPAREN);
			return new ParensExpr(expr);
		}
	}
	
	
	
	@Override
	public <T extends AnnotationValue> ArrayInitializer<? extends T> parseArrayInitializer(Supplier<? extends T> elementParser) {
		try(var $ = scope.enter(Scope.NORMAL)) {
			return super.parseArrayInitializer(elementParser);
		}
	}

	@Override
	public List<Expression> parseArguments(boolean classCreatorArguments) {
		require(LPAREN);
		if(accept(RPAREN)) {
			if(!classCreatorArguments && enabled(LAST_LAMBDA_ARGUMENT) && wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
				return List.of(new Lambda(Either.second(emptyList()), Either.first(parseBlock())));
			} else {
				return emptyList();
			}
		} else {
			var args = new ArrayList<Expression>();
			args.add(parseExpression());
			while(accept(COMMA)) {
				if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
					break;
				}
				args.add(parseExpression());
			}
			require(RPAREN);
			if(!classCreatorArguments && enabled(LAST_LAMBDA_ARGUMENT) && wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
				args.add(new Lambda(Either.second(emptyList()), Either.first(parseBlock())));
			}
			return args;
		}
	}

	@Override
	public Expression parseArgument() {
		if(enabled(ARGUMENT_ANNOTATIONS)) {
			accept(NAME, COLON);
		}
		return super.parseArgument();
	}

	public String syntheticName(String hint, Object hint2) {
		return String.format("__%s$%08x", hint, System.identityHashCode(hint2));
	}
	
	@Override
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
			} else if(enabled(OPTIONAL_LITERALS) && accept(BANG)) {
				if(accept(ELSE)) {
					if(accept(THROW)) {
						if(wouldAccept(LBRACE)) {
							var body = parseBlock();
							return new FunctionCall(expr, Names.orElseThrow, new Lambda(emptyList(), body));
						} else {
							Expression body;
							if(wouldAccept(NEW)) {
								body = parseClassCreator();
							} else if(accept(LPAREN)) {
								body = parseExpression();
								require(RPAREN);
							} else {
								body = parseSuffix();
							}
							return new FunctionCall(expr, Names.orElseThrow, new Lambda(emptyList(), body));
						}
					} else {
						if(wouldAccept(LBRACE)) {
							var body = parseBlock();
							return new FunctionCall(expr, Names.orElseGet, new Lambda(emptyList(), body));
						} else {
							Expression body;
							if(wouldAccept(NEW)) {
								body = parseClassCreator();
							} else if(accept(LPAREN)) {
								body = parseExpression();
								require(RPAREN);
							} else {
								body = parseSuffix();
							}
							return new FunctionCall(expr, isSimple(body)? Names.orElse : Names.orElseGet, new Lambda(emptyList(), body));
						}
					}
				} else {
					return new FunctionCall(expr, Names.orElseThrow);
				}
			} else {
				return expr;
			}
		}
	}
	
	protected boolean isSimple(Expression expr) {
		while(expr instanceof ParensExpr) {
			expr = ((ParensExpr)expr).getExpression();
		}
		return expr instanceof Literal || expr instanceof ClassLiteral || expr instanceof Variable
				|| expr instanceof This || expr instanceof MethodReference || expr instanceof SuperMethodReference;
	}

	@Override
	public Expression parseIndexRest(Expression indexed) {
		try(var $ = scope.enter(Scope.NORMAL)) {
			return super.parseIndexRest(indexed);
		}
	}

	@Override
	public Expression parseExpressionMethodReferenceRest(Expression object, List<? extends TypeArgument> typeArguments, Name name) {
		if(enabled(PARTIAL_METHOD_REFERENCES) && accept(LPAREN)) {
			var params = new ArrayList<InformalParameter>();
			var args = new ArrayList<Expression>();
			if(!wouldAccept(RPAREN)) {
				try(var $ = scope.enter(Scope.NORMAL)) {
        			parsePartialMethodReferenceArgument(params, args);
        			while(accept(COMMA)) {
        				if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
        					break;
        				}
        				parsePartialMethodReferenceArgument(params, args);
        			}
				}
			}
			require(RPAREN);
			if(enabled(LAST_LAMBDA_ARGUMENT) && wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
				args.add(new Lambda(Either.second(emptyList()), Either.first(parseBlock())));
			}
			Expression functionCall = new FunctionCall(object, name, typeArguments, args);
			return new ParensExpr(new Lambda(Either.second(params), Either.second(functionCall)));
		}
		return super.parseExpressionMethodReferenceRest(object, typeArguments, name);
	}
	
	@Override
	public Expression parseTypeMethodReferenceRest(Either<ArrayType,GenericType> type, List<? extends TypeArgument> typeArguments) {
		if(enabled(PARTIAL_METHOD_REFERENCES)) {
			if(accept(LPAREN)) {
				var params = new ArrayList<InformalParameter>();
				var args = new ArrayList<Expression>();
				var comma = token;
				if(!wouldAccept(RPAREN)) {
					try(var $ = scope.enter(Scope.NORMAL)) {
						parsePartialMethodReferenceArgument(params, args);
						comma = token;
						while(accept(COMMA)) {
							if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
								break;
							}
							parsePartialMethodReferenceArgument(params, args);
						}
					}
				}
				require(RPAREN);
				Expression functionCall;
				if(type.isFirst()) {
					var arrayType = type.first();
					if(args.size() != arrayType.getDimensions().size()) {
						if(args.size() == 0 && wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
							var initializer = parseArrayInitializer(() -> parseInitializer(true));
							functionCall = new ArrayCreator(arrayType.getBaseType(), initializer, arrayType.getDimensions());
						} else {
							throw new SyntaxError("array[]::new can only accept exactly 1 argument", filename,
									comma.getStart().getLine(), comma.getStart().getColumn(), comma.getLine());
						}
					} else {
						functionCall = new ArrayCreator(arrayType.getBaseType(), args.stream().map(Size::new).collect(Collectors.toList()), emptyList());
					}
				} else {
					Optional<List<Member>> members;
					if(wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
						members = Optional.of(parseClassBody(() -> parseClassMember(false)));
					} else {
						members = Optional.empty();
					}
					functionCall = new ClassCreator(typeArguments, type.second(), args, members);
				}
				return new ParensExpr(new Lambda(Either.second(params), Either.second(functionCall)));
			} else if(scope.current() != Scope.CONDITION && wouldAccept(LBRACE) && (type.isFirst() || enabled(OPTIONAL_CONSTRUCTOR_ARGUMENTS))) {
				Expression functionCall;
				if(type.isFirst()) {
					var arrayType = type.first();
					var initializer = parseArrayInitializer(() -> parseInitializer(true));
					functionCall = new ArrayCreator(arrayType.getBaseType(), initializer, arrayType.getDimensions());
				} else {
					var members = Optional.of(parseClassBody(() -> parseClassMember(false)));
					functionCall = new ClassCreator(typeArguments, type.second(), emptyList(), members);
				}
				return new ParensExpr(new Lambda(Either.second(emptyList()), Either.second(functionCall)));
			}
		}
		return super.parseTypeMethodReferenceRest(type, typeArguments);
	}
	
	@Override
	public Expression parseSuperMethodReferenceRest(Optional<QualifiedName> qualifier, List<? extends TypeArgument> typeArguments, Name name) {
		if(enabled(PARTIAL_METHOD_REFERENCES) && accept(LPAREN)) {
			var params = new ArrayList<InformalParameter>();
			var args = new ArrayList<Expression>();
			if(!wouldAccept(RPAREN)) {
				try(var $ = scope.enter(Scope.NORMAL)) {
        			parsePartialMethodReferenceArgument(params, args);
        			while(accept(COMMA)) {
        				if(enabled(TRAILING_COMMAS) && wouldAccept(RPAREN)) {
        					break;
        				}
        				parsePartialMethodReferenceArgument(params, args);
        			}
				}
			}
			require(RPAREN);
			if(enabled(LAST_LAMBDA_ARGUMENT) && wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
				args.add(new Lambda(Either.second(emptyList()), Either.first(parseBlock())));
			}
			Expression functionCall = new SuperFunctionCall(qualifier, name, typeArguments, args);
			return new ParensExpr(new Lambda(Either.second(params), Either.second(functionCall)));
		}
		return super.parseSuperMethodReferenceRest(qualifier, typeArguments, name);
	}
	
	protected void parsePartialMethodReferenceArgument(ArrayList<InformalParameter> params, ArrayList<Expression> args) {
		if(accept(UNDERSCORE)) {
			var name = Name(syntheticName("arg" + args.size(), args.isEmpty()? args : args.get(args.size()-1)));
			params.add(new InformalParameter(name));
			args.add(new Variable(name));
		} else {
			args.add(parseExpression());
		}
	}
	
	
	
	@Override
	public Expression parsePrimary() {
		return switch(token.getType()) {
			case LBRACKET -> parseListLiteral();
			case LBRACE -> parseMapOrSetLiteral();
			case HASHTAG -> parseParameterLiteral();
			case QUES -> parseEmptyOptionalLiteral();
			default -> super.parsePrimary();
		};
	}
	
	public Expression parseEmptyOptionalLiteral() {
		if(enabled(OPTIONAL_LITERALS)) {
			require(QUES);
			if(accept(LT)) {
				var annotations = parseAnnotations();
				if(accept(INT, GT)) {
					var qualifier = makeImportedQualifier(QualNames.java_util_OptionalInt);
					return new FunctionCall(qualifier, Names.empty);
				} else if(accept(LONG, GT)) {
					var qualifier = makeImportedQualifier(QualNames.java_util_OptionalLong);
					return new FunctionCall(qualifier, Names.empty);
				} else if(accept(DOUBLE, GT)) {
					var qualifier = makeImportedQualifier(QualNames.java_util_OptionalDouble);
					return new FunctionCall(qualifier, Names.empty);
				} else {
					var type = parseTypeArgument(annotations);
					require(GT);
					var qualifier = makeImportedQualifier(QualNames.java_util_Optional);
					return new FunctionCall(qualifier, Names.empty, List.of(type));
				}
			} else {
				var qualifier = makeImportedQualifier(QualNames.java_util_Optional);
				return new FunctionCall(qualifier, Names.empty);
			}
		} else {
			throw syntaxError("invalid start of expression");
		}
	}
	
	public Expression parseParameterLiteral() {
		if(functionParameters.isEmpty()) {
			throw syntaxError("invalid start of expression");
		} else {
			require(HASHTAG);
			var parameters = functionParameters.current();
			if(!wouldAccept(NUMBER) || !token.getString().matches("\\d+(_+\\d+)*")) {
				throw syntaxError("Expected argument index after #, got " + token);
			}
			var argIndex = Integer.parseUnsignedInt(token.getString().replace("_", ""));
			if(argIndex == 0 || argIndex > parameters.size()) {
				throw syntaxError("Invalid argument index " + argIndex + ", valid indices range from 1 to " + parameters.size());
			}
			nextToken();
			return new Variable(parameters.get(argIndex-1).getName());
		}
	}
	
	
	
	protected Expression makeListCall(List<Expression> elements) {
		return new FunctionCall(makeImportedQualifier(QualNames.java_util_List), Names.of, elements);
	}
	
	protected Expression parseListLiteral() {
		if(enabled(COLLECTION_LITERALS)) {
			require(LBRACKET);
			
			var elements = new ArrayList<Expression>();
			
			if(!wouldAccept(RBRACKET)) {
				try(var $ = scope.enter(Scope.NORMAL)) {
    				elements.add(parseExpression());
    				while(accept(COMMA)) {
    					if(wouldAccept(RBRACKET)) {
    						break;
    					}
    					elements.add(parseExpression());
    				}
				}
			}
			
			require(RBRACKET);
			
			return makeListCall(elements);			
		} else {
			throw syntaxError("invalid start of expression");
		}
	}
	
	protected Expression makeSetCall(List<Expression> elements) {
		return new FunctionCall(makeImportedQualifier(QualNames.java_util_Set), Names.of, elements);
	}
	
	protected Expression makeMapCall(List<Pair<Expression, Expression>> pairs) {
		var qualifier = makeImportedQualifier(QualNames.java_util_Map);
		if(pairs.size() <= 10) {
			var args = new ArrayList<Expression>(pairs.size()*2);
			for(var pair : pairs) {
				args.add(pair.getLeft());
				args.add(pair.getRight());
			}
			return new FunctionCall(qualifier, Names.of, args);
		} else {
			return new FunctionCall(qualifier, Names.ofEntries, pairs.stream().map(pair -> new FunctionCall(qualifier, Names.entry, pair.getLeft(), pair.getRight())).collect(Collectors.toList()));
		}
	}
	
	protected Expression parseMapOrSetLiteral() {
		if(enabled(COLLECTION_LITERALS)) {
			require(LBRACE);
			
			if(accept(RBRACE)) {
				return makeMapCall(emptyList());
			} else {
				Expression expr;
				try(var $ = scope.enter(Scope.NORMAL)) {
					expr = parseExpression();
				}
				if(accept(COLON)) {
					var pairs = new ArrayList<Pair<Expression,Expression>>();
					try(var $ = scope.enter(Scope.NORMAL)) {
    					pairs.add(Pair.of(expr, parseExpression()));
    					while(accept(COMMA)) {
    						if(wouldAccept(RBRACE)) {
    							break;
    						}
    						expr = parseExpression();
    						require(COLON);
    						pairs.add(Pair.of(expr, parseExpression()));
    					}
					}
					require(RBRACE);
					return makeMapCall(pairs);
				} else {
					var elements = new ArrayList<Expression>();
					elements.add(expr);
					try(var $ = scope.enter(Scope.NORMAL)) {
    					while(accept(COMMA)) {
    						if(wouldAccept(RBRACE)) {
    							break;
    						}
    						elements.add(parseExpression());
    					}
					}
					require(RBRACE);
					return makeSetCall(elements);
				}
			}
		} else {
			throw syntaxError("invalid start of expression");
		}
	}

	protected Expression makeFStringExpression(String format, List<Expression> args, boolean isRaw) {
		if(args.isEmpty() && format.indexOf('%') == -1) {
			try {
				return new Literal(isRaw? format : StringEscapeUtils.unescapeJava(format));
			} catch(Exception e) {
    			throw new SyntaxError("invalid string literal", filename, token.getStart().getLine(), token.getStart().getColumn(), token.getLine());
    		}
		} else {
			var qualifier = makeQualifier(QualNames.java_lang_String);
			try {
				args.add(0, new Literal(isRaw? format : StringEscapeUtils.unescapeJava(format)));
			} catch(Exception e) {
    			var error = new SyntaxError("invalid string literal", filename, token.getStart().getLine(), token.getStart().getColumn(), token.getLine());
    			error.addSuppressed(e);
    			throw error;
    		}
			return new FunctionCall(qualifier, Names.format, args);
		}
	}
	
	private static final Pattern formatFlagsRegex = Pattern.compile("^(?<flags>[-+# 0,(]{0,7})([1-9]\\d*)?(\\.\\d+)?([bBhHsScCdoxXeEfgGaA%n]|[tT][HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc])");
	private static final Set<Character> formatFlags = Set.of('+', '-', '#', ' ', '0', '(', ',');
	
	private static final Pattern rawStringRegex = Pattern.compile("^[fF]?[rR]");
	
	@Override
	public Expression parseStringLiteral() {
		var startToken = this.token;
		var str = startToken.getString();
		require(STRING);
		if(enabled(RAW_STRING_LITERALS) && rawStringRegex.matcher(str).find()) {
			if(str.startsWith("f") || str.startsWith("F") || "fF".indexOf(str.charAt(1)) >= 0) {
				if(enabled(FORMAT_STRINGS)) {
					return parseFormatStringLiteral(startToken, str);
				} else {
					throw syntaxError("invalid string literal", startToken);
				}
			} else {
				if(str.endsWith("\"\"\"")) {
					if(enabled(TEXT_BLOCKS)) {
						str = str.substring(4, str.length()-3);
					} else {
						throw syntaxError("invalid string literal", startToken);
					}
				} else {
					str = str.substring(2, str.length()-1);
				}
    			try {
    				return new Literal(str);
    			} catch(Exception e) {
        			throw syntaxError("invalid string literal", startToken);
        		}
			}
		} else if(enabled(FORMAT_STRINGS) && (str.startsWith("f") || str.startsWith("F"))) {
			return parseFormatStringLiteral(startToken, str);
		} else if(enabled(REGEX_LITERALS) && str.startsWith("/")) {
			return parseRegexLiteral(startToken, str);
		} else {
			if(!str.startsWith("\"")) {
				throw syntaxError("invalid start of expression");
			}
			if(str.startsWith("\"\"\"")) {
				if(enabled(TEXT_BLOCKS)) {
					str = str.substring(3, str.length()-3).replace("\r\n", "\\n").replace("\n", "\\n");
					try {
						return new Literal(StringEscapeUtils.unescapeJava(str));
					} catch(Exception e) {
						throw syntaxError("invalid string literal", startToken);
					}
				} else {
					throw syntaxError("invalid string literal", startToken);
				}
			} else {
				str = str.substring(1, str.length()-1);
				try {
	    			return new Literal(StringEscapeUtils.unescapeJava(str), startToken.getString());
	    		} catch(Exception e) {
	    			throw syntaxError("invalid string literal", startToken);
	    		}
			}
		}
	}
	
	protected Expression parseRegexLiteral(Token<JavaTokenType> startToken, String str) {
		var sb = new StringBuilder();
		boolean escape = false;
		for(int i = 1; i < str.length() - 1; i++) {
			char c = str.charAt(i);
			if(escape) {
				if(c != '/') {
					sb.append('\\');
				}
				sb.append(c);
				escape = false;
			} else if(c == '\\') {
				escape = true;
			} else {
				sb.append(c);
			}
		}
		str = sb.toString();
		Literal literal;
		try {
			literal = new Literal(str);
		} catch(Exception e) {
			throw syntaxError("invalid string literal", startToken);
		}
		return new FunctionCall(makeImportedQualifier(QualNames.java_util_regex_Pattern), Names.compile, literal);
	}
	
	protected Expression parseFormatStringLiteral(Token<JavaTokenType> startToken, String str) {
		var args = new ArrayList<Expression>();
		String format;
		boolean isRaw = "rR".indexOf(str.charAt(1)) >= 0 || str.startsWith("r") || str.startsWith("R");
		if(str.endsWith("\"\"\"")) {
			if(!enabled(TEXT_BLOCKS)) {
				throw syntaxError("invalid string literal", startToken);
			}
			format = str.substring(isRaw? 5 : 4, str.length()-3).replace("%", "%%").replaceAll("\r?\n", "%n");
		} else if(str.endsWith("\"")) {
			format = str.substring(isRaw? 3 : 2, str.length()-1).replace("%", "%%");
		} else {
			assert str.endsWith("%");
			//var formatBuilder = new StringBuilder();
			var elements = new ArrayList<String>();
			boolean isMultiline = str.startsWith("\"\"\"", isRaw? 2 : 1);
			if(isMultiline && !enabled(TEXT_BLOCKS)) {
				throw syntaxError("invalid string literal", startToken);
			}
			elements.add(str.substring(isMultiline? isRaw? 5 : 4 : isRaw ? 3 : 2, str.length()-1).replace("%", "%%") + "%");
			if(accept(LBRACE)) {
				try(var $ = scope.enter(Scope.NORMAL)) {
					args.add(parseExpression());
				}
				require(RBRACE);
				elements.add(args.size() + "$");
				elements.add(null);
			} else {
				args.add(new Variable(parseName()));
				elements.add(args.size() + "$s");
			}
			String endStr = isMultiline? "\"\"\"" : "\"";
			while(wouldAccept(STRING) && !token.getString().endsWith(endStr) && switch(token.getString().charAt(0)) { case '"' -> token.getString().equals(endStr); case 'r', 'R', 'f', 'F' -> token.getString().length() == 1 || token.getString().charAt(1) != '"'; default -> true; }) {
				str = token.getString();
				nextToken();
				assert str.endsWith("%");
				elements.add(str.substring(0, str.length()-1).replace("%", "%%") + "%");
				if(accept(LBRACE)) {
					try(var $ = scope.enter(Scope.NORMAL)) {
						args.add(parseExpression());
					}
					require(RBRACE);
					elements.add(args.size() + "$");
					elements.add(null);
				} else {
					args.add(new Variable(parseName()));
					elements.add(args.size() + "$s");
				}
			}
			if(!wouldAccept(STRING) || !token.getString().endsWith("\"") || switch(token.getString().charAt(0)) { case '"' -> !token.getString().equals(endStr); case 'r', 'R', 'f', 'F' -> token.getString().length() == 1 || token.getString().charAt(1) == '"'; default -> false; }) {
				throw syntaxError("expected format string end here, got " + token);
			}
			str = token.getString();
			elements.add(str.substring(0, str.length()-endStr.length()).replace("%", "%%"));
			nextToken();
			var formatBuilder = new StringBuilder();
			for(int i = 0; i < elements.size(); i++) {
				String element = elements.get(i);
				if(element == null) {
					var matcher = formatFlagsRegex.matcher(elements.get(i+1));
					if(!matcher.find()) {
						formatBuilder.append('s');
					} else {
						String flags = matcher.group("flags");
						if(!flags.isEmpty()) {
							var remainingFlags = new HashSet<>(formatFlags);
							for(int j = 0; j < flags.length(); j++) {
								if(!remainingFlags.remove(flags.charAt(j))) {
									formatBuilder.append('s');
									break;
								}
							}
						}
					}
				} else {
					formatBuilder.append(element);
				}
			}
			format = formatBuilder.toString();
			if(isMultiline) {
				format = format.replaceAll("\r?\n", "%n");
			}
		}
		return makeFStringExpression(format, args, false);
	}

	@Override
	public ClassCreator parseClassCreatorRest(List<? extends TypeArgument> typeArguments, GenericType type) {
		boolean hasDiamond = type.getTypeArguments().isEmpty() && accept(LT, GT);
		if(enabled(COLLECTION_LITERALS) && wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
			Expression arg;
			require(LBRACE);
			if(accept(RBRACE)) {
				arg = makeMapCall(emptyList());
			} else {
				var expr = parseExpression();
				if(accept(COLON)) {
					var pairs = new ArrayList<Pair<Expression,Expression>>();
					pairs.add(Pair.of(expr, parseExpression()));
					while(accept(COMMA)) {
						if(wouldAccept(RBRACE)) {
							break;
						}
						expr = parseExpression();
						require(COLON);
						pairs.add(Pair.of(expr, parseExpression()));
					}
					require(RBRACE);
					arg = makeMapCall(pairs);
				} else {
					var elements = new ArrayList<Expression>();
					elements.add(expr);
					while(accept(COMMA)) {
						if(wouldAccept(RBRACE)) {
							break;
						}
						elements.add(parseExpression());
					}
					require(RBRACE);
					arg = makeListCall(elements);
				}
			}
			return new ClassCreator(typeArguments, type, hasDiamond, arg);
		}
		var args = enabled(OPTIONAL_CONSTRUCTOR_ARGUMENTS)? parseArgumentsOpt(true) : parseArguments(true);
		Optional<? extends List<Member>> members;
		if(wouldAccept(LBRACE) && scope.current() != Scope.CONDITION) {
			members = Optional.of(parseClassBody(() -> parseClassMember(false)));
		} else {
			members = Optional.empty();
		}
		return new ClassCreator(typeArguments, type, hasDiamond, args, members);
	}
	
}
