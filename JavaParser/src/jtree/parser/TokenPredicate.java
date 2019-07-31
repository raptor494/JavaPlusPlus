package jtree.parser;

import java.util.function.Predicate;

import org.apache.commons.text.StringEscapeUtils;

import lombok.NonNull;

@FunctionalInterface
public interface TokenPredicate<TokenType> extends Predicate<Token<TokenType>> {

	@Override
	default TokenPredicate<TokenType> or(@NonNull Predicate<? super Token<TokenType>> pred) {
		var oldThis = this;
		var str = oldThis.toString() + " || " + pred.toString();
		return new TokenPredicate<>() {
			
			public boolean test(Token<TokenType> token) {
				return oldThis.test(token) || pred.test(token);
			}
			
			public String toString() {
				return str; 
			}
			
		};
	}
	
	default TokenPredicate<TokenType> or(@NonNull String str) {
		var oldThis = this;
		var toString = oldThis.toString() + " || \"" + StringEscapeUtils.escapeJava(str) + '"';
		return new TokenPredicate<>() {
			
			public boolean test(Token<TokenType> token) {
				return oldThis.test(token) || token.getString().equals(str);
			}
			
			public String toString() {
				return toString;
			}
			
		};
	}
	
	@Override
	default TokenPredicate<TokenType> and(@NonNull Predicate<? super Token<TokenType>> pred) {
		var oldThis = this;
		String toString1 = oldThis.toString(), toString2 = pred.toString();
		int depth;
		boolean inString, escape;
		char stringChar;
		depth = stringChar = 0;
		inString = escape = false;
	loop:
		for(int i = 0; i < toString1.length(); i++) {
			char c = toString1.charAt(i);
			if(inString) {
				if(escape) {
					escape = false;
				} else if(c == stringChar) {
    				inString = false;
				} else if(c == '\\') {
					escape = true;
				}
			} else {
    			switch(c) {
    				case '(' -> depth++;
    				case ')' -> depth--;
    				case '"', '\'' -> {
    					inString = true;
    					stringChar = c;
    				}
    				case '|' -> {
    					if(depth == 0 && i + 1 < toString1.length() && toString1.charAt(i+1) == '|') {
    						toString1 = '(' + toString1 + ')';
    						break loop;
    					}
    				}
    			}
			}
		}
		depth = stringChar = 0;
		inString = escape = false;
	loop:
		for(int i = 0; i < toString2.length(); i++) {
			char c = toString2.charAt(i);
			if(inString) {
				if(escape) {
					escape = false;
				} else if(c == stringChar) {
    				inString = false;
				} else if(c == '\\') {
					escape = true;
				}
			} else {
    			switch(c) {
    				case '(' -> depth++;
    				case ')' -> depth--;
    				case '"', '\'' -> {
    					inString = true;
    					stringChar = c;
    				}
    				case '|' -> {
    					if(depth == 0 && i + 1 < toString2.length() && toString2.charAt(i+1) == '|') {
    						toString2 = '(' + toString2 + ')';
    						break loop;
    					}
    				}
    			}
			}
		}
		var toString = toString1 + " && " + toString2;
		return new TokenPredicate<>() {
			
			public boolean test(Token<TokenType> token) {
				return oldThis.test(token) && pred.test(token);
			}
			
			public String toString() {
				return toString;
			}
			
		};
	}
	
	default TokenPredicate<TokenType> and(@NonNull String str) {
		var oldThis = this;
		String toString1 = oldThis.toString();
		int depth;
		boolean inString, escape;
		char stringChar;
		depth = stringChar = 0;
		inString = escape = false;
	loop:
		for(int i = 0; i < toString1.length(); i++) {
			char c = toString1.charAt(i);
			if(inString) {
				if(escape) {
					escape = false;
				} else if(c == stringChar) {
    				inString = false;
				} else if(c == '\\') {
					escape = true;
				}
			} else {
    			switch(c) {
    				case '(' -> depth++;
    				case ')' -> depth--;
    				case '"', '\'' -> {
    					inString = true;
    					stringChar = c;
    				}
    				case '|' -> {
    					if(depth == 0 && i + 1 < toString1.length() && toString1.charAt(i+1) == '|') {
    						toString1 = '(' + toString1 + ')';
    						break loop;
    					}
    				}
    			}
			}
		}
		var toString = toString1 + " && \"" + StringEscapeUtils.escapeJava(str) + '"';
		return new TokenPredicate<>() {
			
			public boolean test(Token<TokenType> token) {
				return oldThis.test(token) && token.getString().equals(str);
			}
			
			public String toString() {
				return toString;
			}
			
		};
	}
	
	@Override
	default TokenPredicate<TokenType> negate() {
		class NotTokenPredicate implements TokenPredicate<TokenType> {
			private final TokenPredicate<TokenType> pred;
			private final String toString;
			
			public NotTokenPredicate(TokenPredicate<TokenType> pred) {
				this.pred = pred;
				String toString1 = pred.toString();
				int depth;
				boolean inString, escape;
				char stringChar;
				depth = stringChar = 0;
				inString = escape = false;
			loop:
				for(int i = 0; i < toString1.length(); i++) {
					char c = toString1.charAt(i);
					if(inString) {
						if(escape) {
							escape = false;
						} else if(c == stringChar) {
		    				inString = false;
						} else if(c == '\\') {
							escape = true;
						}
					} else {
		    			switch(c) {
		    				case '(' -> depth++;
		    				case ')' -> depth--;
		    				case '"', '\'' -> {
		    					inString = true;
		    					stringChar = c;
		    				}
		    				case '&' -> {
		    					if(depth == 0 && i + 1 < toString1.length() && toString1.charAt(i+1) == '&') {
		    						toString1 = '(' + toString1 + ')';
		    						break loop;
		    					}
		    				}
		    				case '|' -> {
		    					if(depth == 0 && i + 1 < toString1.length() && toString1.charAt(i+1) == '|') {
		    						toString1 = '(' + toString1 + ')';
		    						break loop;
		    					}
		    				}
		    			}
					}
				}
				toString = "!" + toString1;
			}
			
			@Override
			public boolean test(Token<TokenType> t) {
				return !pred.test(t);
			}
			
			@Override
			public String toString() {
				return toString;
			}
			
			@Override
			public TokenPredicate<TokenType> negate() {
				return pred;
			}
			
		}
		
		return new NotTokenPredicate(this);
	}
	
	static <TokenType> TokenPredicate<TokenType> not(TokenPredicate<TokenType> pred) {
		return pred.negate();
	}
	
	static <TokenType> TokenPredicate<TokenType> ofString(@NonNull String str) {
		var toString = '"' + StringEscapeUtils.escapeJava(str) + '"';
		return new TokenPredicate<>() {

			@Override
			public boolean test(Token<TokenType> t) {
				return t.getString().equals(str);
			}
			
			public String toString() {
				return toString;
			}
			
		};
	}
	
	static <TokenType> TokenPredicate<TokenType> not(@NonNull String str) {
		return TokenPredicate.<TokenType>ofString(str).negate();
	}
	
}
