package jtree.nodes;

import static lombok.AccessLevel.*;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.text.StringEscapeUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class Literal extends Node implements Expression {
	@Setter(NONE)
	protected Object value;
	@Getter(NONE) @Setter(NONE)
	protected @NonNull String stringRep;
	
	public Literal(int value, @NonNull String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	public Literal(long value, @NonNull String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	public Literal(float value, @NonNull String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	public Literal(double value, @NonNull String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	public Literal(char value, @NonNull String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	public Literal(@NonNull String value, @NonNull String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	public Literal(int value) {
		setValue(value);
	}
	
	public Literal(long value) {
		setValue(value);
	}
	
	public Literal(float value) {
		setValue(value);
	}
	
	public Literal(double value) {
		setValue(value);
	}
	
	public Literal(boolean value) {
		setValue(value);
	}
	
	public Literal(char value) {
		setValue(value);
	}
	
	public Literal() {
		setValue((Void)null);
	}
	
	public Literal(String value) {
		setValue(value);
	}
	
	protected Literal(Object value, String stringRep) {
		this.value = value;
		this.stringRep = stringRep;
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public Literal clone() {
		return new Literal(value, stringRep);
	}
	
	public void setValue(int value) {
		this.value = value;
		this.stringRep = Objects.toString(value);
	}
	
	public void setValue(long value) {
		this.value = value;
		this.stringRep = value + "L";
	}
	
	public void setValue(float value) {
		this.value = value;
		this.stringRep = value + "f";
	}
	
	public void setValue(double value) {
		this.value = value;
		this.stringRep = Objects.toString(value);
	}
	
	public void setValue(boolean value) {
		this.value = value;
		this.stringRep = Objects.toString(value);
	}
	
	public void setValue(char value) {
		this.value = value;
		String valueStr = Objects.toString(value);
		if(valueStr.equals("'")) {
			this.stringRep = "'\\''";
		} else {
			this.stringRep = "'" + StringEscapeUtils.escapeJava(valueStr) + "'";
		}
	}
	
	public void setValue(@NonNull String value) {
		this.value = value;
		this.stringRep = '"' + StringEscapeUtils.escapeJava(value) + '"';
	}
	
	public void setValue(Void value) {
		this.value = value;
		this.stringRep = "null";
	}
	
	@Override
	public String toCode() {
		return stringRep;
	}
	
	@Override
	public String toString() {
		return "Literal(" + toCode() + ")";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		visitor.visitLiteral(this, parent, cast(replacer));
	}
	
}
