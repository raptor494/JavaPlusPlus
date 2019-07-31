package jtree.nodes;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.NonNull;

public final class Name extends Node implements CharSequence {
	public static final Pattern NAME_PATTERN = Pattern.compile("[\\w$&&[^\\d]][\\w$]*");

	private final String stringValue;

	/**
	 * @param string the name
	 * @throws IllegalArgumentException If the argument is not a valid name.
	 */
	public Name(@NonNull String string) {
		if(!NAME_PATTERN.matcher(string).matches()) {
			throw new IllegalArgumentException("Not a valid name: '" + string + "'");
		}
		this.stringValue = string;
	}
	
	@Override
	public Name clone() {
		return this;
	}

	public QualifiedName toQualifiedName() {
		return new QualifiedName(this);
	}
	
	public QualifiedName append(Name other) {
		return new QualifiedName(this, other);
	}
	
	public QualifiedName append(Name... names) {
		Name[] newnames = new Name[names.length+1];
		System.arraycopy(names, 0, newnames, 1, names.length);
		newnames[0] = this;
		return new QualifiedName(newnames);
	}
	
	public QualifiedName append(QualifiedName other) {
		return Stream.concat(Stream.of(this), other.stream()).collect(QualifiedName.collector());	
	}
	
	public QualifiedName append(QualifiedName... names) {
		return Stream.concat(Stream.of(this), Arrays.stream(names)).collect(QualifiedName.collector());
	}
	
	public QualifiedName append(CharSequence... names) {
		return Stream.concat(Stream.of(this), Arrays.stream(names)).collect(QualifiedName.collector());
	}

	@Override
	public String toString() {
		return stringValue;
	}

	@Override
	public String toCode() {
		return stringValue;
	}
	
	@Override
	public int hashCode() {
		return stringValue.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof CharSequence && stringValue.contentEquals((CharSequence)obj);
	}
	
	public boolean equals(CharSequence cseq) {
		return stringValue.contentEquals(cseq);
	}

	@Override
	public int length() {
		return stringValue.length();
	}

	@Override
	public char charAt(int index) {
		return stringValue.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return stringValue.subSequence(start, end);
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		visitor.visitName(this, parent, cast(replacer));
	}
	
}