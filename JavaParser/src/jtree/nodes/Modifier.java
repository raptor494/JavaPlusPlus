package jtree.nodes;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public class Modifier extends Node {
	public static enum Modifiers implements CharSequence {
		PUBLIC,
		PRIVATE,
		PROTECTED,
		STATIC,
		FINAL,
		TRANSIENT,
		VOLATILE,
		STRICTFP,
		NATIVE,
		SYNCHRONIZED,
		DEFAULT,
		ABSTRACT,
		TRANSITIVE;
		
		@Getter @Accessors(fluent = true)
		private final String toString = name().toLowerCase();
		
		@Override
		public int length() {
			return toString.length();
		}

		@Override
		public char charAt(int index) {
			return toString.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return toString.subSequence(start, end);
		}
		
		private static final Map<String, Modifiers> NAME_TO_MODIFIER = Arrays.stream(values()).collect(Collectors.toMap(Modifiers::toString, m -> m));
		
		public static Modifiers fromString(String str) {
			var result = NAME_TO_MODIFIER.get(str);
			if(result == null) {
				throw new IllegalArgumentException("'" + str + "' is not a valid Modifier");
			}
			return result;
		}
	}
	
	protected final String modifier;
	
	public Modifier(Modifiers modifier) {
		this(modifier.toString());
	}
	
	protected Modifier(@NonNull String value) {
		if(value.isBlank() || Character.isWhitespace(value.codePointAt(0)) || Character.isWhitespace(value.codePointAt(value.codePointCount(0, value.length()) - 1))) {
			throw new IllegalArgumentException("'" + value + "' is not a valid modifier");
		}
		this.modifier = value;
	}
	
	@Override
	public Modifier clone() {
		return this;
	}
	
	@Override
	public String toCode() {
		return modifier;
	}

	@Override
	public String toString() {
		return modifier;
	}

	@Override
	public int hashCode() {
		return modifier.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof Modifier && modifier.equals(((Modifier)obj).modifier)
				|| obj instanceof CharSequence && modifier.contentEquals((CharSequence)obj);
	}
	
	public boolean equals(CharSequence cseq) {
		return modifier.contentEquals(cseq);
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		visitor.visitModifier(this, parent, cast(replacer));
	}

}
