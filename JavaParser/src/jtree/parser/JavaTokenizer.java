package jtree.parser;

import static java.lang.Character.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jtree.util.LookAheadListIterator;
import lombok.NonNull;

public class JavaTokenizer<TokenType> implements Iterator<Token<TokenType>> {
	protected CharSequence str;
	protected int pos, line, column;
	protected char ch;
	protected boolean returnedEndmarker = false;
	protected CharSequence currentLine;
	protected TokenType defaultType, stringType, numberType, charType, wordType, errorType, commentType;
	protected Map<String, TokenType> tokens;
	protected List<String> sortedTokens, wordTokens;
	protected String filename;
	
	public JavaTokenizer(@NonNull CharSequence str, TokenType defaultType, TokenType errorType, TokenType stringType,
						 TokenType charType, TokenType numberType, TokenType wordType, TokenType commentType,
						 @NonNull Map<String,TokenType> tokens) {
		this(str, "<unknown source>", defaultType, errorType, stringType, charType, numberType, wordType, commentType, tokens);
	}

	public JavaTokenizer(@NonNull CharSequence str, @NonNull String filename, TokenType defaultType,
						 TokenType errorType, TokenType stringType,
						 TokenType charType, TokenType numberType, TokenType wordType, TokenType commentType,
						 @NonNull Map<String,TokenType> tokens) {
		this.str = str;
		this.pos = 0;
		this.line = this.column = 1;
		if(str.length() > 0) {
			this.ch = str.charAt(0);
		}
		this.filename = filename;
		this.defaultType = defaultType;
		this.errorType = errorType;
		this.stringType = stringType;
		this.numberType = numberType;
		this.charType = charType;
		this.wordType = wordType;
		this.commentType = commentType;
		int i = 0;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		this.currentLine = i+1 > str.length()? "" : str.subSequence(0, i+1);
		this.tokens = tokens;
		/*this.sortedTokens = new TreeSet<>((token1, token2) -> Integer.compare(token2.length(), token1.length()));
		this.wordTokens = new TreeSet<>((token1, token2) -> Integer.compare(token2.length(), token1.length()));
		for(var key : tokens.keySet()) {
			if(isJavaIdentifierPart(key.charAt(key.length()-1))) {
				boolean added = wordTokens.add(key);
				assert added;
			} else {
				boolean added = sortedTokens.add(key);
				assert added;
			}
		}*/
		this.sortedTokens = tokens.keySet().stream()
										   .filter(token -> !isJavaIdentifierPart(token.charAt(token.length()-1)))
										   .sorted((token1, token2) -> Integer.compare(token2.length(), token1.length()))
										   .collect(Collectors.toList());
		this.wordTokens = tokens.keySet().stream()
										 .filter(token -> isJavaIdentifierPart(token.charAt(token.length()-1)))
										 .sorted((token1, token2) -> Integer.compare(token2.length(), token1.length()))
										 .collect(Collectors.toList());
		eatWhite();
	}
	
	protected void nextChar() {
		if(pos+1 >= str.length()) {
			ch = 0;
			pos = str.length();
		} else {
			if(ch == '\n') {
				line++;
				column = 1;
				int i = pos+1;
				while(i+1 < str.length() && str.charAt(i+1) != '\n') {
					i++;
				}
				currentLine = str.subSequence(pos+1, i+1);
			} else {
				column += 1;
			}
			do {
				ch = str.charAt(++pos);
			} while(ch == '\r' && pos+1 < str.length());
		}
	}
	
	protected void setPos(int newpos) {
		if(pos < newpos) {
			for(int i = 0, end = newpos - pos; i < end && pos < str.length(); i++) {
				nextChar();
			}
		} else if(pos > newpos) {
			for(int i = 0, end = pos - newpos; i < end && pos > 0; i++) {
				ch = str.charAt(--pos);
				if(ch == '\n') {
					int j = pos-1;
					while(j > 0 && str.charAt(j) != '\n') {
						j--;
					}
					if(str.charAt(j) == '\n') {
						j++;
					}
					currentLine = str.subSequence(j, pos+1);
					line--;
					column = currentLine.length();
				} else {
					column--;
				}
			}
		}
	}
	
	protected boolean eat(char c) {
		if(ch == c) {
			nextChar();
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean eat(String sub) {
		if(pos + sub.length() > str.length()) {
			return false;
		} else if(sub.contentEquals(str.subSequence(pos, pos + sub.length()))) {
			setPos(pos + sub.length());
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean eatWord(String sub) {
		if(pos + sub.length() > str.length()) {
			return false;
		} else if(sub.contentEquals(str.subSequence(pos, pos + sub.length())) && (pos + sub.length() == str.length() || !isJavaIdentifierPart(str.charAt(pos + sub.length())))) {
			setPos(pos + sub.length());
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean hasNext() {
		return pos < str.length() || !returnedEndmarker;
	}

	@Override
	public Token<TokenType> next() {
		if(pos >= str.length()) {
			if(returnedEndmarker) {
				throw new NoSuchElementException();
			} else {
				returnedEndmarker = true;
				var start = new Position(line, column);
				var end = new Position(line, column);
				return new Token<>(defaultType, "", start, end, currentLine);
			}
		}
		
		Token<TokenType> result = switch(ch) {
			case '"' -> eatString();
			case '\'' -> eatChar();
			case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> eatNumber();
			case '/' -> eatCommentOrDefault();
			default -> defaultNext();
		};
		eatWhite();
		return result;
	}
	
	protected Token<TokenType> defaultNext() {
		if(ch == '.' && pos + 1 < str.length() && isDigit(str.charAt(pos+1))) {
			return eatNumber();
		}
		var start = new Position(line, column);
		for(String word : wordTokens) {
			if(eatWord(word)) {
				var end = new Position(line, column);
				return new Token<>(tokens.get(word), word, start, end, currentLine);
			}
		}
		for(String symbol : sortedTokens) {
			if(eat(symbol)) {
				var end = new Position(line, column);
				return new Token<>(tokens.get(symbol), symbol, start, end, currentLine);
			}
		}
		
		int startPos = pos;
		
		if(isJavaIdentifierStart(ch)) {
			nextChar();
			while(pos < str.length() && isJavaIdentifierPart(ch)) {
				nextChar();
			}
			var end = new Position(line, column);
			return new Token<>(wordType, str.subSequence(startPos, pos).toString(), start, end, currentLine);
		}

		nextChar();
		var end = new Position(line, column);
		return new Token<>(errorType, str.subSequence(startPos, pos).toString(), start, end, currentLine);
	}
	
	protected void eatWhite() {
		while(pos < str.length() && isWhitespace(ch)) {
			nextChar();
		}
	}
	
	protected Token<TokenType> eatCommentOrDefault() {
		var start = new Position(line, column);
		var startPos = pos;
		var startPos2 = pos - column + 1;
		if(eat("//")) {							// single-line comment
			while(pos < str.length() && !eat('\n')) {
				nextChar();
			}
		} else if(eat("/*")) {					// multi-line comment
			while(pos < str.length() && !eat("*/")) {
				nextChar();
			}
		} else {								// not a comment
			return defaultNext();
		}
		var end = new Position(line, column);
		int i = pos;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		if(i+1 > str.length()) {
			i = str.length()-1;
		}
		return new Token<>(commentType, str.subSequence(startPos, pos).toString().indent(-100), start, end, str.subSequence(startPos2, i+1));
	}
	
	protected Token<TokenType> eatString() {
		return eatString('"', stringType);
	}
	
	protected Token<TokenType> eatChar() {
		return eatString('\'', charType); 
	}

	protected Token<TokenType> eatString(char ends, TokenType type) {
		assert ch == ends;
		var start = new Position(line, column);
		int startPos = pos - column + 1;
		nextChar();
		var sb = new StringBuilder();
		sb.append(ends);
		boolean escape = false;
		while(pos < str.length() && (ch != ends && ch != '\n' || escape)) {
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
		if(!eat(ends)) {
			throw new SyntaxError("unterminated string", filename, line, column, currentLine);
		}
		sb.append(ends);
		var end = new Position(line, column);
		int i = pos;
		while(i+1 < str.length() && str.charAt(i) != '\n') {
			i++;
		}
		if(i+1 > str.length()) {
			i = str.length()-1;
		}
		return new Token<>(type, sb.toString(), start, end, str.subSequence(startPos, i+1));
	}
	
	protected Token<TokenType> eatNumber() {
		assert ch == '.' || isDigit(ch);
		
		int startPos = pos;
		var start = new Position(line, column);
		
		if(eat("0x") || eat("0X")) {
			if(!isHexDigit(ch)) {
				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
			}
			boolean first = false;
			while(isHexDigit(ch)) {
				first = true;
    			nextChar();
    			if(ch == '_') {
    				while(eat('_')) {
    					nextChar();
    				}
    				if(!isHexDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    			}
    		}
		
    		if(ch == '.' && (!first || pos+1 < str.length() && isHexDigit(str.charAt(pos+1)))) {
    			nextChar();
    			if(!first && !isHexDigit(ch)) {
    				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    			}
    			while(isHexDigit(ch)) {
        			nextChar();
        			if(ch == '_') {
        				while(eat('_')) {
        					nextChar();
        				}
        				if(!isHexDigit(ch)) {
        					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
        				}
        			}
        		}
    			
    			if(eat('p') || eat('P')) {
    				if(!eat('+')) {
    					eat('-');
    				}
    				
    				if(!isDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    				while(isDigit(ch)) {
            			nextChar();
            			if(ch == '_') {
            				while(eat('_')) {
            					nextChar();
            				}
            				if(!isDigit(ch)) {
            					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
            				}
            			}
            		}
    			} else {
    				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    			}
    			
    		} else {
    			if(!first) {
    				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    			}
    			if(eat('p') || eat('P')) {
    				if(!eat('+')) {
    					eat('-');
    				}
    				
    				if(!isDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    				while(isDigit(ch)) {
            			nextChar();
            			if(ch == '_') {
            				while(eat('_')) {
            					nextChar();
            				}
            				if(!isDigit(ch)) {
            					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
            				}
            			}
            		}
    				
    				if(!(eat('f') || eat('F') || eat('d'))) {
    					eat('D');
    				}
    			} else if(!eat('l')) {
    				eat('L');
    			}
    		}
		} else if(eat("0b") || eat("0B")) {
			do {
    			if(ch != '1' && ch != '0') {
    				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    			}
    			nextChar();
    			if(ch == '_') {
    				while(eat('_')) {
    					nextChar();
    				}
    				if(!isDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    			}
			} while(isDigit(ch));
		} else {
			boolean first = false;
    		while(isDigit(ch)) {
    			first = true;
    			nextChar();
    			if(ch == '_') {
    				while(eat('_')) {
    					nextChar();
    				}
    				if(!isDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    			}
    		}
		
    		if(ch == '.' && (!first || pos+1 < str.length() && isDigit(str.charAt(pos+1)))) {
    			nextChar();
    			if(!first && !isDigit(ch)) {
    				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    			}
    			while(isDigit(ch)) {
        			nextChar();
        			if(ch == '_') {
        				while(eat('_')) {
        					nextChar();
        				}
        				if(!isDigit(ch)) {
        					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
        				}
        			}
        		}
    			
    			if(eat('e') || eat('E')) {
    				if(!eat('+')) {
    					eat('-');
    				}
    				
    				if(!isDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    				while(isDigit(ch)) {
            			nextChar();
            			if(ch == '_') {
            				while(eat('_')) {
            					nextChar();
            				}
            				if(!isDigit(ch)) {
            					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
            				}
            			}
            		}
    			} else if(!eat('f') || eat('F') || eat('d')) {
    				eat('D');
    			}
    			
    		} else {
    			if(!first) {
    				throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    			}
    			if(eat('e') || eat('E')) {
    				if(!eat('+')) {
    					eat('-');
    				}
    				
    				if(!isDigit(ch)) {
    					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
    				}
    				while(isDigit(ch)) {
            			nextChar();
            			if(ch == '_') {
            				while(eat('_')) {
            					nextChar();
            				}
            				if(!isDigit(ch)) {
            					throw new SyntaxError("invalid number literal", filename, line, column, currentLine);
            				}
            			}
            		}
    				
    				if(!(eat('f') || eat('F') || eat('d'))) {
    					eat('D');
    				}
    			} else if(!(eat('F') || eat('d') || eat('D') || eat('l'))) {
    				eat('L');
    			}
    		}
    		
		}
		
		var end = new Position(line, column);
		return new Token<>(numberType, str.subSequence(startPos, pos).toString(), start, end, currentLine);
	}
	
	public static boolean isOctalDigit(char c) {
		return switch(c) {
			case '0', '1', '2', '3', '4', '5', '6', '7' -> true;
			default -> false;
		};
	}
	
	public static boolean isHexDigit(char c) {
		return isDigit(c) || switch(c) {
			case 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f' -> true;
			default -> false;
		};
	}
	
	public static void main(String[] args) {
		String text = "\"a b c\\r\\n\\\\d e f\" x \n" +
					  "y //test\n" +
					  "z++=2 2.3 2.4e6 0xAf";
		
		var tokenizer = new JavaTokenizer<>(text, JavaTokenType.ENDMARKER, JavaTokenType.ERRORTOKEN, JavaTokenType.STRING,
										JavaTokenType.CHARACTER, JavaTokenType.NUMBER, JavaTokenType.NAME, JavaTokenType.COMMENT,
										JavaTokenType.NORMAL_TOKENS.stream().collect(Collectors.toMap(token -> token.getSymbol().orElseThrow(), token -> token)));
		var tokens = new LookAheadListIterator<>(() -> tokenizer);
		
		for(var token : tokens) {
			System.out.println(token);
		}
		
	}

}
