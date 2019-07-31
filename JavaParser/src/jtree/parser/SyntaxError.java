package jtree.parser;

import lombok.Setter;

public class SyntaxError extends RuntimeException {
	private int lineNumber, column;
	private CharSequence line;
	@Setter
	private String filename;

	public SyntaxError(String message, String filename, int lineNumber, int column, CharSequence line) {
		super(message);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.column = column;
		this.line = line;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + "\n" + (filename == null? "" : "in file " + filename + " ") + "on line " + lineNumber + ":" + formatLine(line, column);
	}
	
	private static String formatLine(CharSequence line, int column) {
		var sb = new StringBuilder("\n  ");
		for(int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if(c == '\t') {
				sb.append("    ");
			} else if(Character.isWhitespace(c)) {
				sb.append(' ');
			} else {
				sb.append(c);
			}
		}
		sb.append("\n  ");
		if(column > line.length()) {
			column = line.length();
		}
		for(int i = 1; i < column; i++) {
			if(line.charAt(i) == '\t') {
				sb.append("    ");
			} else {
				sb.append(' ');
			}
		}
		sb.append('^');
		return sb.toString();
	}
	
}
