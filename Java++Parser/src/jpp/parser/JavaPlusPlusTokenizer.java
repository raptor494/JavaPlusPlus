package jpp.parser;

import static java.lang.Character.*;
import static jpp.parser.JavaPlusPlusParser.Feature.*;
import static jtree.parser.JavaTokenType.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import jpp.parser.JavaPlusPlusParser.Feature;
import jtree.parser.JavaTokenType;
import jtree.parser.JavaTokenType.Tag;
import jtree.parser.JavaTokenizer;
import jtree.parser.Position;
import jtree.parser.SyntaxError;
import jtree.parser.Token;
import lombok.NonNull;

public class JavaPlusPlusTokenizer extends JavaTokenizer<JavaTokenType> {
	protected JavaTokenType regexType;
	protected Token<JavaTokenType> last;
	protected int braceDepth;
	protected Stack<Scope> scope = new Stack<>();
	{
		scope.push(Scope.NORMAL);
	}
	
	protected final EnumSet<Feature> enabledFeatures, initialEnabledFeatures;
	
	public JavaPlusPlusTokenizer(CharSequence str, String filename) {
		this(str, filename, Feature.enabledByDefault());
	}
	
	public JavaPlusPlusTokenizer(CharSequence str) {
		this(str, Feature.enabledByDefault());
	}
	
	public JavaPlusPlusTokenizer(@NonNull CharSequence str, @NonNull String filename, @NonNull EnumSet<Feature> enabledFeatures) {
		super(str, filename, ENDMARKER, ERRORTOKEN, STRING, CHARACTER, NUMBER, NAME, COMMENT,
				JavaTokenType.NORMAL_TOKENS.stream()
				.collect(Collectors.toMap(token -> token.getSymbol().orElseThrow(), token -> token)));
		this.regexType = STRING;
		this.enabledFeatures = enabledFeatures;
		this.initialEnabledFeatures = enabledFeatures.clone();
	}
	
	public JavaPlusPlusTokenizer(@NonNull CharSequence str, @NonNull EnumSet<Feature> enabledFeatures) {
		super(str, ENDMARKER, ERRORTOKEN, STRING, CHARACTER, NUMBER, NAME, COMMENT,
				JavaTokenType.NORMAL_TOKENS.stream()
				.collect(Collectors.toMap(token -> token.getSymbol().orElseThrow(), token -> token)));
		this.regexType = STRING;
		this.enabledFeatures = enabledFeatures;
		this.initialEnabledFeatures = enabledFeatures.clone();
	}
	
	protected boolean enabled(Feature feature) {
		return enabledFeatures.contains(feature);
	}
	
	protected void setEnabled(String featureId, boolean enabled) {
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
	
	protected static enum Scope {
		NORMAL, 
		FSTRING_END, 
		FSTRING_BRACES,
		FSTRING_BEGIN(FSTRING_END, FSTRING_BRACES), 
		RAW_FSTRING_END, 
		RAW_FSTRING_BRACES,
		RAW_FSTRING_BEGIN(RAW_FSTRING_END, RAW_FSTRING_BRACES),
		ML_FSTRING_END, 
		ML_FSTRING_BRACES,
		ML_FSTRING_BEGIN(ML_FSTRING_END, ML_FSTRING_BRACES), 
		RAW_ML_FSTRING_END, 
		RAW_ML_FSTRING_BRACES,
		RAW_ML_FSTRING_BEGIN(RAW_ML_FSTRING_END, RAW_ML_FSTRING_BRACES);
		
		private Supplier<Scope> fstringEndScopeSupplier, fstringBracesScopeSupplier;
		
		Scope() {
			this.fstringEndScopeSupplier = this.fstringBracesScopeSupplier = () -> {throw new NoSuchElementException();};
		}
		
		Scope(Scope fstringEnd, Scope fstringBraces) {
			this.fstringEndScopeSupplier = () -> fstringEnd;
			this.fstringBracesScopeSupplier = () -> fstringBraces;
		}
		
		public Scope getFStringEndScope() {
			return fstringEndScopeSupplier.get();
		}
		
		public Scope getFStringBracesScope() {
			return fstringBracesScopeSupplier.get();
		}
	}
	
	protected ArrayList<Pair<Token<JavaTokenType>, String>> importsToBeAdded = new ArrayList<>();
	protected Token<JavaTokenType> firstImportNameToken;
	protected StringBuilder importNameBuilder;
	protected boolean disable;
	
	protected void doFeatureImports() {
		boolean enabled = !disable;
		for(var pair : importsToBeAdded) {
			try {
				setEnabled(pair.getRight(), enabled);
			} catch(IllegalArgumentException e) {
				var token = pair.getLeft();
				throw new SyntaxError(e.getMessage(), filename, token.getStart().getLine(), token.getStart().getColumn(), token.getLine());
			}
		}
		importsToBeAdded.clear();
	}
	
	protected class States {
		protected Consumer<Token<JavaTokenType>> PrePackage = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case PACKAGE -> state = WaitUntilSemiAfterPackage;
					case ENABLE -> {
						disable = false;
						state = ParseName0;
					}
					case DISABLE -> {
						disable = true;
						state = ParseName0;
					}
					default -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
						state = Normal;
					}
				}
			}
			
			public String toString() {
				return "PrePackage";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> WaitUntilSemiAfterPackage = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case SEMI -> state = TryAcceptEnableDisable;
					default -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
						state = Normal;
					}
				}
			}

			public String toString() {
				return "WaitUntilSemiAfterPackage";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> TryAcceptEnableDisable = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case ENABLE -> {
						disable = false;
						state = ParseName0;
					}
					case DISABLE -> {
						disable = true;
						state = ParseName0;
					}
					case COMMENT -> {}
					default -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
						state = Normal;
					}
				}
			}
			
			public String toString() {
				return "TryAcceptEnableDisable";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParseName0 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					firstImportNameToken = token;
					importNameBuilder = new StringBuilder(token.getString());
					state = ParseDotName;
				} else {
					switch(token.getType()) {
						case STAR -> {
							importsToBeAdded.add(Pair.of(token, "*"));
							state = ParsePostStar;
						}
						case SEMI -> state = TryAcceptEnableDisable;
						case COMMENT -> {}
						default -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
							state = Normal;
						}
					}
				}
			}
			
			public String toString() {
				return "ParseName0"; 
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParseDotName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case DOT -> state = ParseNameRest;
					case COMMA -> {
						importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
						firstImportNameToken = null;
						importNameBuilder = null;
						state = ParseName1;
					}
					case SEMI -> {
						importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
						firstImportNameToken = null;
						importNameBuilder = null;
						doFeatureImports();
						state = TryAcceptEnableDisable;
					}
					case COMMENT -> {}
					default -> {
						firstImportNameToken = null;
						importNameBuilder = null;
						importsToBeAdded.clear();
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
						state = Normal;
					}
				}
			}
			
			public String toString() {
				return "";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParseNameRest = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					importNameBuilder.append('.').append(token.getString());
					state = ParseDotName;
				} else {
					switch(token.getType()) {
						case SEMI -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
							state = Normal;
						}
						case STAR -> {
							importNameBuilder.append(".*");
							importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
							firstImportNameToken = null;
							importNameBuilder = null;
							state = ParseCommaName;
						}
						case COMMENT -> {}
						default -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
							state = Normal;
						}
					}
				}
			}
			
			public String toString() {
				return "ParseNameRest";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParseName1 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					firstImportNameToken = token;
					importNameBuilder = new StringBuilder(token.getString());
					state = ParseDotName;
				} else {
					switch(token.getType()) {
						case SEMI -> {
							if(enabled(TRAILING_COMMAS)) {
								doFeatureImports();
								state = TryAcceptEnableDisable;
							} else {
								state = Normal;
							}
						}
						case COMMENT -> {}
						default -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
							state = Normal;
						}
					}
				}
			}
			
			public String toString() {
				return "ParseName1";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParseCommaName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case SEMI -> {
						doFeatureImports();
						state = TryAcceptEnableDisable;
					}
					case COMMA -> state = ParseName1;
					case COMMENT -> {}
					default -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
						state = Normal;
					}
				}
			}
			
			public String toString() {
				return "ParseCommaName";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParsePostStar = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case SEMI -> {
						doFeatureImports();
						state = TryAcceptEnableDisable;
					}
					default -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
						state = Normal;
					}
				}
			}
			
			public String toString() {
				return "ParsePostStar";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> Normal = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType() == ENDMARKER) {
					enabledFeatures.clear();
					enabledFeatures.addAll(initialEnabledFeatures);
				}
			}

			public String toString() {
				return "Normal";
			}
		};
	}
	
	/*protected class States {
		protected Consumer<Token<JavaTokenType>> PrePackage = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case PACKAGE -> state = WaitUntilSemiAfterPackage;
					case FROM -> state = ParseFromImport_Java;
					case IMPORT -> {
						disable = false;
						state = ParseNormalImport_Java0;
					}
					case UNIMPORT -> {
						disable = true;
						state = ParseNormalImport_Java0;
					}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {}
				}
			}
	
			public String toString() {
				return "PrePackage";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> WaitUntilSemiAfterPackage = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case SEMI -> state = TryAcceptImport;
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {}
				}
			}
	
			public String toString() {
				return "WaitUntilSemiAfterPackage";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> TryAcceptImport = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case FROM -> {
						if(enabled(FROM_IMPORTS)) {
							state = ParseFromImport_Java;
						} else {
							state = Normal;
						}
					}
					case IMPORT -> {
						disable = false;
						state = ParseNormalImport_Java0;
					}
					case UNIMPORT -> {
						disable = true;
						state = ParseNormalImport_Java0;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> state = Normal;
				}
			}
	
			public String toString() {
				return "TryAcceptImport";
			}
		};
		
		protected Consumer<Token<JavaTokenType>> ParseFromImport_Java = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType() == NAME && token.getString().equals("java")) {
					state = ParseFromImport_PlusPlus;
				} else {
					switch(token.getType()) {
						case SEMI -> state = TryAcceptImport;
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseFromImport_Java";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseFromImport_PlusPlus = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case PLUSPLUS -> state = ParseFromImport_Import;
					case SEMI -> state = TryAcceptImport;
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> state = WaitUntilSemiAfterImport;
				}
			}
	
			public String toString() {
				return "ParseFromImport_PlusPlus";
			}
		};
		
	
		protected Consumer<Token<JavaTokenType>> ParseFromImport_Import = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case IMPORT -> {
						disable = false;
						state = ParseFromImport_Name0;
					}
					case UNIMPORT -> {
						disable = true;
						state = ParseFromImport_Name0;
					}
					case SEMI -> state = TryAcceptImport;
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> state = WaitUntilSemiAfterImport;
				}
			}
	
			public String toString() {
				return "ParseFromImport_Import";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseFromImport_Name0 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					firstImportNameToken = token;
					importNameBuilder = new StringBuilder(token.getString());
					state = ParseFromImport_DotName;
				} else {
					switch(token.getType()) {
						case STAR -> {
							importsToBeAdded.add(Pair.of(token, "*"));
							state = ParseFromImport_CommaName;
						}
						case SEMI -> state = TryAcceptImport;
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseFromImport_Name0";
			}
		};
		
	
		protected Consumer<Token<JavaTokenType>> ParseFromImport_DotName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case DOT -> state = ParseFromImport_NameRest;
					case COMMA -> {
						importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
						firstImportNameToken = null;
						importNameBuilder = null;
						state = ParseFromImport_Name1;
					}
					case SEMI -> {
						importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
						firstImportNameToken = null;
						importNameBuilder = null;
						doFeatureImports();
						state = TryAcceptImport;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {
						firstImportNameToken = null;
						importNameBuilder = null;
						importsToBeAdded.clear();
						state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseFromImport_DotName";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseFromImport_Name1 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					firstImportNameToken = token;
					importNameBuilder = new StringBuilder();
					state = ParseFromImport_DotName;
				} else {
					switch(token.getType()) {
						case SEMI -> {
							if(enabled(TRAILING_COMMAS)) {
								doFeatureImports();
								state = TryAcceptImport;
							} else {
								firstImportNameToken = null;
								importNameBuilder = null;
								importsToBeAdded.clear();
								state = TryAcceptImport;
							}
						}
						case STAR -> {
							importsToBeAdded.add(Pair.of(token, "*"));
							state = ParseFromImport_CommaName;
						}
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
				}
			}
	
			public String toString() {
				return "ParseFromImport_Name1";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseFromImport_NameRest = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					importNameBuilder.append('.').append(token.getString());
					state = ParseFromImport_DotName;
				} else {
					switch(token.getType()) {
						case STAR -> {
							importNameBuilder.append(".*");
							importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
							firstImportNameToken = null;
							importNameBuilder = null;
							state = ParseFromImport_CommaName;
						}
						case SEMI -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							state = TryAcceptImport;
						}
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
				}
			}
	
			public String toString() {
				return "ParseFromImport_NameRest";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseFromImport_CommaName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case COMMA -> state = ParseFromImport_Name1;
					case SEMI -> {
						doFeatureImports();
						state = TryAcceptImport;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {
						importsToBeAdded.clear();
						state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseFromImport_CommaName";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_Java0 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType() == NAME && token.getString().equals("java")) {
					state = ParseNormalImport_PlusPlus;
				} else if(token.getType().hasTag(Tag.NAMED)) {
					state = ParseRegularImport_DotName;
				} else {
					switch(token.getType()) {
						case SEMI -> state = TryAcceptImport;
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_Java0";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_PlusPlus = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case PLUSPLUS -> state = ParseNormalImport_DotName0;
					case DOT -> state = ParseRegularImport_Name1;
					case SEMI -> {
						doFeatureImports();
						state = TryAcceptImport;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> state = WaitUntilSemiAfterImport;
				}
			}
	
			public String toString() {
				return "ParseNormalImport_PlusPlus";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_DotName0 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case DOT -> state = ParseNormalImport_Name0;
					case SEMI -> {
						importsToBeAdded.clear();
						state = TryAcceptImport;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {
						importsToBeAdded.clear();
						state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_DotName0";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_Name0 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					firstImportNameToken = token;
					importNameBuilder = new StringBuilder(token.getString());
					state = ParseNormalImport_DotName;
				} else {
					switch(token.getType()) {
						case STAR -> {
							importsToBeAdded.add(Pair.of(token, "*"));
							state = ParseNormalImport_CommaName;
						}
						case SEMI -> {
							importsToBeAdded.clear();
							state = TryAcceptImport;
						}
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> {
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_Name0";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_DotName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case COMMA -> {
						if(enabled(COMMA_IMPORTS)) {
							importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
							firstImportNameToken = null;
							importNameBuilder = null;
							state = ParseNormalImport_Java1;
						} else {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
					case SEMI -> {
						importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
						firstImportNameToken = null;
						importNameBuilder = null;
						doFeatureImports();
						state = TryAcceptImport;
					}
					case DOT -> state = ParseNormalImport_Name1;
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {
						importsToBeAdded.clear();
						firstImportNameToken = null;
						importNameBuilder = null;
						state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_DotName";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_Name1 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					importNameBuilder.append('.').append(token.getString());
					state = ParseNormalImport_DotName;
				} else {
					switch(token.getType()) {
						case SEMI -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							state = TryAcceptImport;
						}
						case STAR -> {
							importNameBuilder.append(".*");
							importsToBeAdded.add(Pair.of(firstImportNameToken, importNameBuilder.toString()));
							firstImportNameToken = null;
							importNameBuilder = null;
							state = ParseNormalImport_CommaName;
						}
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> {
							firstImportNameToken = null;
							importNameBuilder = null;
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_Name1";
			}
		};
	
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_CommaName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case COMMA -> {
						if(enabled(COMMA_IMPORTS)) {
							state = ParseNormalImport_Java1;
						} else {
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
					case SEMI -> {
						doFeatureImports();
						state = TryAcceptImport;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {
						importsToBeAdded.clear();
						state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_CommaName";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseNormalImport_Java1 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType() == NAME && token.getString().equals("java")) {
					state = ParseNormalImport_PlusPlus;
				} else if(token.getType().hasTag(Tag.NAMED)) {
					state = ParseRegularImport_DotName;
				} else {
					switch(token.getType()) {
						case SEMI -> {
							if(enabled(TRAILING_COMMAS)) {
								doFeatureImports();
							} else {
								importsToBeAdded.clear();
							}
							state = TryAcceptImport;
						}
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> {
							importsToBeAdded.clear();
							state = TryAcceptImport;
						}
					}
				}
			}
	
			public String toString() {
				return "ParseNormalImport_Java1";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseRegularImport_Name1 = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType().hasTag(Tag.NAMED)) {
					state = ParseRegularImport_DotName;
				} else {
					switch(token.getType()) {
						case SEMI -> {
							importsToBeAdded.clear();
							state = TryAcceptImport;
						}
						case COMMENT -> {}
						case ENDMARKER -> {
							enabledFeatures.clear();
							enabledFeatures.addAll(initialEnabledFeatures);
						}
						default -> {
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
				}
			}
	
			public String toString() {
				return "ParseRegularImport_Name1";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> ParseRegularImport_DotName = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case DOT -> state = ParseRegularImport_Name1;
					case COMMA -> {
						if(enabled(COMMA_IMPORTS)) {
							state = ParseNormalImport_Java1;
						} else {
							importsToBeAdded.clear();
							state = WaitUntilSemiAfterImport;
						}
					}
					case SEMI -> {
						doFeatureImports();
						state = TryAcceptImport;
					}
					case COMMENT -> {}
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {
						importsToBeAdded.clear();
						state = WaitUntilSemiAfterImport;
					}
				}
			}
	
			public String toString() {
				return "ParseRegularImport_DotName";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> WaitUntilSemiAfterImport = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				switch(token.getType()) {
					case SEMI -> state = TryAcceptImport;
					case ENDMARKER -> {
						enabledFeatures.clear();
						enabledFeatures.addAll(initialEnabledFeatures);
					}
					default -> {}
				}
			}
	
			public String toString() {
				return "WaitUntilSemiAfterImport";
			}
		};
	
		
		protected Consumer<Token<JavaTokenType>> Normal = new Consumer<>() {
			public void accept(Token<JavaTokenType> token) {
				if(token.getType() == ENDMARKER) {
					enabledFeatures.clear();
					enabledFeatures.addAll(initialEnabledFeatures);
				}
			}
	
			public String toString() {
				return "Normal";
			}
		};
	}*/

	//protected States states = new States();
	protected States states = new States();
	protected Consumer<Token<JavaTokenType>> state = states.PrePackage;
	
	@Override
	public Token<JavaTokenType> next() {
		var token = next0();
		state.accept(token);
		return token;
	}
	
	protected Token<JavaTokenType> next0() {
		if(pos >= str.length()) {
			if(returnedEndmarker) {
				throw new NoSuchElementException();
			} else {
				returnedEndmarker = true;
				var start = new Position(line, column);
				var end = new Position(line, column);
				return last = new Token<>(defaultType, "", start, end, currentLine);
			}
		}
		
		Scope peeked = scope.peek();
		switch(peeked) {
			case FSTRING_BEGIN, ML_FSTRING_BEGIN, RAW_FSTRING_BEGIN, RAW_ML_FSTRING_BEGIN -> {
				if(Character.isJavaIdentifierStart(ch)) {
					scope.pop();
					scope.push(peeked.getFStringEndScope());
					return defaultNext();
				} else if(ch == '{') {
					scope.pop();
					scope.push(peeked.getFStringBracesScope());
					var start = new Position(line, column);
					nextChar();
					var end = new Position(line, column);
					eatWhite();
					return new Token<>(tokens.get("{"), "{", start, end, currentLine);
				} else {
					throw new SyntaxError("invalid string literal", filename, line, column, currentLine);
				}
			}
			case FSTRING_BRACES, ML_FSTRING_BRACES, RAW_FSTRING_BRACES, RAW_ML_FSTRING_BRACES -> {
				if(ch == '}') {
					if(braceDepth == 0) {
						scope.pop();
						scope.push(peeked.getFStringEndScope());
						var start = new Position(line, column);
						nextChar();
						var end = new Position(line, column);
						return new Token<>(tokens.get("}"), "}", start, end, currentLine);
					} else {
						braceDepth--;
					}
				} else if(ch == '{') {
					braceDepth++;
				}
			}
			case FSTRING_END -> {
				Token<JavaTokenType> result = eatFStringRest();
				eatWhite();
				return result;
			}
			case ML_FSTRING_END -> {
				Token<JavaTokenType> result = eatMultilineFStringRest();
				eatWhite();
				return result;
			}
			case RAW_FSTRING_END -> {
				Token<JavaTokenType> result = eatRawFStringRest();
				eatWhite();
				return result;
			}
			case RAW_ML_FSTRING_END -> {
				Token<JavaTokenType> result = eatMultilineRawFStringRest();
				eatWhite();
				return result;
			}
			default -> {}
		}
		
		Token<JavaTokenType> result = switch(ch) {
			case '"' -> eatString();
			case '\'' -> eatChar();
			case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> eatNumber();
			case '/' -> eatRegexLiteralOrCommentOrDefault();
			case 'r', 'R', 'f', 'F' -> {
				if(hasNextString()) {
					break eatString();
				} else {
					break defaultNext();
				}
			}
			default -> defaultNext();
		};
		eatWhite();
		return last = result;
	}
	
	protected boolean hasNextString() {
		switch(ch) {
			case 'f', 'F':
				if(enabled(FORMAT_STRINGS)) {
					int npos = pos+1;
					if(npos < str.length()) {
						switch(str.charAt(npos)) {
							case '"':
								return true;
							case 'r', 'R':
								if(enabled(RAW_STRING_LITERALS)) {
									npos++;
									return npos < str.length() && str.charAt(npos) == '"';
								}
							default:
								return false;
						}
					}
				} else {
					return false;
				}
			case 'r', 'R':
				if(enabled(RAW_STRING_LITERALS)) {
					int npos = pos+1;
					if(npos < str.length()) {
						switch(str.charAt(npos)) {
							case '"':
								return true;
							case 'f', 'F':
								if(enabled(FORMAT_STRINGS)) {
									npos++;
									return npos < str.length() && str.charAt(npos) == '"';
								}
							default:
								return false;
						}
					}
				} else {
					return false;
				}
			case '"':
				return true;
			default:
				return false;
		}
	}
	
	protected Token<JavaTokenType> eatRegexLiteralOrCommentOrDefault() {
		boolean valid;
		if(enabled(REGEX_LITERALS)) {
			if(pos == 0) {
				valid = true;
			} else {
				int i = pos-1;
				while(i > 0 && Character.isWhitespace(str.charAt(i))) {
					i--;
				}
				char c = str.charAt(i);
				if(i == 0 && Character.isWhitespace(c)) {
					valid = true;
				} else {
					valid = switch(c) {
						case ')', ']', '}', '.' -> false;
						default -> !Character.isJavaIdentifierPart(c);
					};
				}
			}
		} else {
			valid = false;
		}
		if(valid) {
			if(pos + 1 < str.length() && "/*".indexOf(str.charAt(pos+1)) >= 0) {
				return eatCommentOrDefault();
			}
			int temp = pos;
			try {
				return eatString('/', regexType);
			} catch(SyntaxError e) {
				if(e.getMessage().startsWith("unterminated string")) {
					setPos(temp);
					return eatCommentOrDefault();
				} else {
					throw e;
				}
			}
		} else {
			return eatCommentOrDefault();
		}
	}
	
	@Override
	protected Token<JavaTokenType> eatString() {
		if(enabled(RAW_STRING_LITERALS) && (ch == 'r' || ch == 'R')) {
			char startChar = ch;
			var start = new Position(line, column);
			int startPos = pos - column + 1;
			nextChar();
			var sb = new StringBuilder();
			sb.append(startChar);
			if(enabled(FORMAT_STRINGS) && (ch == 'f' || ch == 'F')) {
				sb.append(ch);
				nextChar();
				if(!eat('"')) {
    				throw new SyntaxError("invalid string literal", filename, line, column, currentLine);
    			}
				sb.append('"');
				if(eat('"')) {
    				sb.append('"');
    				if(enabled(TEXT_BLOCKS) && eat('"')) {
    					sb.append('"');
    					parseMultilineRawFStringContents(sb);
    				}
    			} else {
    				parseRawFStringContents(sb);
    			}
			} else {
    			if(!eat('"')) {
    				throw new SyntaxError("invalid string literal", filename, line, column, currentLine);
    			}
    			sb.append('"');
    			if(eat('"')) {
    				sb.append('"');
    				if(enabled(TEXT_BLOCKS) && eat('"')) {
    					sb.append('"');
    					parseMultilineRawStringContents(sb);
    				}
    			} else {
    				parseRawStringContents(sb);
    			}
			}
			var end = new Position(line, column);
			int i = pos;
			while(i+1 < str.length() && str.charAt(i) != '\n') {
				i++;
			}
			if(i+1 > str.length()) {
				i = str.length()-1;
			}
			return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
		} else if(enabled(FORMAT_STRINGS) && (ch == 'f' || ch == 'F')) {
			char startChar = ch;
			var start = new Position(line, column);
			int startPos = pos - column + 1;
			nextChar();
			var sb = new StringBuilder();
			sb.append(startChar);
			if(enabled(RAW_STRING_LITERALS) && (ch == 'r' || ch == 'R')) {
				sb.append(ch);
				nextChar();
				if(!eat('"')) {
    				throw new SyntaxError("invalid string literal", filename, line, column, currentLine);
    			}
				sb.append('"');
				if(eat('"')) {
    				sb.append('"');
    				if(enabled(TEXT_BLOCKS) && eat('"')) {
    					sb.append('"');
    					parseMultilineRawFStringContents(sb);
    				}
    			} else {
    				parseRawFStringContents(sb);
    			}
			} else {
    			if(!eat('"')) {
    				throw new SyntaxError("invalid string literal", filename, line, column, currentLine);
    			}
    			sb.append('"');
    			if(eat('"')) {
    				sb.append('"');
    				if(enabled(TEXT_BLOCKS) && eat('"')) {
    					sb.append('"');
    					parseMultilineFStringContents(sb);
    				}
    			} else {
        			parseFStringContents(sb);
    			}
			}
			var end = new Position(line, column);
			int i = pos;
			while(i+1 < str.length() && str.charAt(i) != '\n') {
				i++;
			}
			if(i+1 > str.length()) {
				i = str.length()-1;
			}
			return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
		} else {
			var start = new Position(line, column);
			int startPos = pos - column + 1;
			var sb = new StringBuilder();
			if(!eat('"')) {
				throw new SyntaxError("invalid string literal", filename, line, column, currentLine);
			}
			sb.append('"');
			if(eat('"')) {
				sb.append('"');
				if(enabled(TEXT_BLOCKS) && eat('"')) {
					sb.append('"');
					parseMultilineStringContents(sb);
				}
			} else {
				parseStringContents(sb);
			}
			var end = new Position(line, column);
			int i = pos;
			while(i+1 < str.length() && str.charAt(i) != '\n') {
				i++;
			}
			if(i+1 > str.length()) {
				i = str.length()-1;
			}
			return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
		}
	}
	
	protected void parseRawFStringContents(StringBuilder sb) {
		boolean escape = false;
		while(pos < str.length() && (ch != '"' && ch != '\n' || escape)) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
			} else {
				sb.append(ch);
				if(ch == '%') {
					if(pos + 1 < str.length()) {
						char next = str.charAt(pos+1);
						if(next == '{' || Character.isJavaIdentifierStart(next)) {
							break;
						}
					}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			scope.push(Scope.RAW_FSTRING_BEGIN);
		} else {
			if(!eat('"')) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
			sb.append('"');
		}
	}
	
	protected void parseMultilineRawFStringContents(StringBuilder sb) {
		int endCounter = 0;
		boolean escape = false;
		while(pos < str.length() && endCounter < 3) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
				endCounter = 0;
			} else {
				sb.append(ch);
				if(ch == '"') {
					endCounter++;
				} else {
					endCounter = 0;
					if(ch == '%') {
						if(pos + 1 < str.length()) {
							char next = str.charAt(pos+1);
							if(next == '{' || Character.isJavaIdentifierStart(next)) {
								break;
							}
						}
					}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			scope.push(Scope.RAW_ML_FSTRING_BEGIN);
		} else {
			if(endCounter != 3) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
		}
	}
	
	protected void parseRawStringContents(StringBuilder sb) {
		boolean escape = false;
		while(pos < str.length() && (ch != '"' && ch != '\n' || escape)) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
			} else {
				sb.append(ch);
			}
			nextChar();
		}
		if(!eat('"')) {
			throw new SyntaxError("unterminated string", filename, line, column, currentLine);
		}
		sb.append('"');
	}
	
	protected void parseMultilineRawStringContents(StringBuilder sb) {
		int endCounter = 0;
		boolean escape = false;
		while(pos < str.length() && endCounter < 3) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
				endCounter = 0;
			} else {
				sb.append(ch);
				if(ch == '"') {
					endCounter++;
				} else {
					endCounter = 0;
				}
			}
			nextChar();
		}
		if(endCounter != 3) {
			throw new SyntaxError("unterminated string", filename, line, column, currentLine);
		}
	}
	
	protected void parseFStringContents(StringBuilder sb) {
		boolean escape = false;
		while(pos < str.length() && (ch != '"' && ch != '\n' || escape)) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
			} else {
				sb.append(ch);
				if(ch == '%') {
					if(pos + 1 < str.length()) {
						char next = str.charAt(pos+1);
						if(next == '{' || Character.isJavaIdentifierStart(next)) {
							break;
						}
					}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			scope.push(Scope.FSTRING_BEGIN);
		} else {
			if(!eat('"')) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
			sb.append('"');
		}
	}
	
	protected void parseMultilineFStringContents(StringBuilder sb) {
		int endCounter = 0;
		boolean escape = false;
		while(pos < str.length() && endCounter < 3) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
				endCounter = 0;
			} else {
				sb.append(ch);
				if(ch == '"') {
					endCounter++;
				} else {
					endCounter = 0;
					if(ch == '%') {
						if(pos + 1 < str.length()) {
							char next = str.charAt(pos+1);
							if(next == '{' || Character.isJavaIdentifierStart(next)) {
								break;
							}
						}
					}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			scope.push(Scope.ML_FSTRING_BEGIN);
		} else {
			if(endCounter != 3) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
		}
	}
	
	protected void parseStringContents(StringBuilder sb) {
		boolean escape = false;
		while(pos < str.length() && (ch != '"' && ch != '\n' || escape)) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
			} else {
				sb.append(ch);
			}
			nextChar();
		}
		if(!eat('"')) {
			throw new SyntaxError("unterminated string", filename, line, column, currentLine);
		}
		sb.append('"');
	}
	
	protected void parseMultilineStringContents(StringBuilder sb) {
		int endCounter = 0;
		boolean escape = false;
		while(pos < str.length() && endCounter < 3) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
				endCounter = 0;
			} else {
				sb.append(ch);
				if(ch == '"') {
					endCounter++;
				} else {
					endCounter = 0;
				}
			}
			nextChar();
		}
		if(endCounter != 3) {
			throw new SyntaxError("unterminated string", filename, line, column, currentLine);
		}
	}
	
	protected Token<JavaTokenType> eatFStringRest() {
		var start = new Position(line, column);
		int startPos = pos - column + 1;
		var sb = new StringBuilder();
		boolean escape = false;
		while(pos < str.length() && (ch != '"' && ch != '\n' || escape)) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
			} else {
				sb.append(ch);
				if(ch == '%') {
					if(pos + 1 < str.length()) {
						char next = str.charAt(pos+1);
						if(next == '{' || Character.isJavaIdentifierStart(next)) {
							break;
						}
					}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			assert scope.peek() == Scope.FSTRING_END;
			scope.pop();
			scope.push(Scope.FSTRING_BEGIN);
		} else {
			if(!eat('"')) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
			sb.append('"');
			assert scope.peek() == Scope.FSTRING_END;
			scope.pop();
		}
		var end = new Position(line, column);
		int i = pos;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		if(i+1 > str.length()) {
			i = str.length()-1;
		}
		return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
	}
	
	protected Token<JavaTokenType> eatMultilineFStringRest() {
		var start = new Position(line, column);
		int startPos = pos - column + 1;
		var sb = new StringBuilder();
		boolean escape = false;
		int endCounter = 0;
		while(pos < str.length() && endCounter < 3) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
				endCounter = 0;
			} else {
				sb.append(ch);
				if(ch == '"') {
					endCounter++;
				} else {
					endCounter = 0;
    				if(ch == '%') {
    					if(pos + 1 < str.length()) {
    						char next = str.charAt(pos+1);
    						if(next == '{' || Character.isJavaIdentifierStart(next)) {
    							break;
    						}
    					}
    				}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			assert scope.peek() == Scope.ML_FSTRING_END;
			scope.pop();
			scope.push(Scope.ML_FSTRING_BEGIN);
		} else {
			if(endCounter != 3) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
			assert scope.peek() == Scope.ML_FSTRING_END;
			scope.pop();
		}
		var end = new Position(line, column);
		int i = pos;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		if(i+1 > str.length()) {
			i = str.length()-1;
		}
		return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
	}
	
	protected Token<JavaTokenType> eatRawFStringRest() {
		var start = new Position(line, column);
		int startPos = pos - column + 1;
		var sb = new StringBuilder();
		boolean escape = false;
		while(pos < str.length() && (ch != '"' && ch != '\n' || escape)) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
			} else {
				sb.append(ch);
				if(ch == '%') {
					if(pos + 1 < str.length()) {
						char next = str.charAt(pos+1);
						if(next == '{' || Character.isJavaIdentifierStart(next)) {
							break;
						}
					}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			assert scope.peek() == Scope.RAW_FSTRING_END;
			scope.pop();
			scope.push(Scope.RAW_FSTRING_BEGIN);
		} else {
			if(!eat('"')) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
			sb.append('"');
			assert scope.peek() == Scope.RAW_FSTRING_END;
			scope.pop();
		}
		var end = new Position(line, column);
		int i = pos;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		if(i+1 > str.length()) {
			i = str.length()-1;
		}
		return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
	}
	
	protected Token<JavaTokenType> eatMultilineRawFStringRest() {
		var start = new Position(line, column);
		int startPos = pos - column + 1;
		var sb = new StringBuilder();
		boolean escape = false;
		int endCounter = 0;
		while(pos < str.length() && endCounter < 3) {
			if(escape) {
				escape = false;
				if(ch == '\n') {
					nextChar();
					while(ch != '\n' && isWhitespace(ch)) {
						nextChar();
					}
					continue;
				} else {
					sb.append('\\').append(ch);
				}
			} else if(ch == '\\') {
				escape = true;
				endCounter = 0;
			} else {
				sb.append(ch);
				if(ch == '"') {
					endCounter++;
				} else {
					endCounter = 0;
    				if(ch == '%') {
    					if(pos + 1 < str.length()) {
    						char next = str.charAt(pos+1);
    						if(next == '{' || Character.isJavaIdentifierStart(next)) {
    							break;
    						}
    					}
    				}
				}
			}
			nextChar();
		}
		if(eat('%')) {
			assert ch == '{' || Character.isJavaIdentifierStart(ch);
			assert scope.peek() == Scope.RAW_ML_FSTRING_END;
			scope.pop();
			scope.push(Scope.RAW_ML_FSTRING_BEGIN);
		} else {
			if(endCounter != 3) {
				throw new SyntaxError("unterminated string", filename, line, column, currentLine);
			}
			assert scope.peek() == Scope.RAW_ML_FSTRING_END;
			scope.pop();
		}
		var end = new Position(line, column);
		int i = pos;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		if(i+1 > str.length()) {
			i = str.length()-1;
		}
		return new Token<>(stringType, sb.toString(), start, end, str.subSequence(startPos, i+1));
	}

}
