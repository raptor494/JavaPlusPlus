package jpp.nodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import jtree.nodes.Modifier;
import lombok.Getter;
import lombok.experimental.Accessors;

public class JPPModifier extends Modifier {
	public static enum Modifiers implements CharSequence {
		PUBLIC,
		PRIVATE,
		PROTECTED,
		PACKAGE,
		STATIC, NON_STATIC,
		FINAL, NON_FINAL,
		TRANSIENT, NON_TRANSIENT,
		VOLATILE, NON_VOLATILE,
		STRICTFP, NON_STRICTFP,
		NATIVE, NON_NATIVE,
		SYNCHRONIZED, NON_SYNCHRONIZED,
		DEFAULT, NON_DEFAULT,
		ABSTRACT, NON_ABSTRACT;
		
		@Getter @Accessors(fluent = true)
		private final String toString = name().toLowerCase().replace('_', '-');
		
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
	
	public JPPModifier(Modifiers value) {
		super(value.toString());
	}

}
