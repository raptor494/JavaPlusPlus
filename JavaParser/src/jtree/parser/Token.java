package jtree.parser;

import org.apache.commons.text.StringEscapeUtils;

import lombok.NonNull;
import lombok.Value;

public @Value class Token<TokenType> {
	TokenType type;
	@NonNull String string;
	@NonNull Position start, end;
	@NonNull CharSequence line;
	
	@Override
	public String toString() {
		var type = this.type.toString();
		return String.format("%s(type=%s, string=%s, start=%s, end=%s, line=%s)",
		                     getClass().getSimpleName(),
		                     Character.isJavaIdentifierPart(type.charAt(0)) && Character.isJavaIdentifierPart(type.charAt(type.length()-1)) || type.charAt(0) == '\'' && type.charAt(type.length()-1) == '\'' && type.length() > 1? type : " " + type + " ",
		                     '"' + StringEscapeUtils.escapeJava(string) + '"',
		                     start, end,
		                     '"' + StringEscapeUtils.escapeJava(line.toString()) + '"');
		                     
	}
}
