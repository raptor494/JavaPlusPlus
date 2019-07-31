package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@EqualsAndHashCode
@Getter @Setter
public class AssignExpr extends Node implements Expression {
	protected @NonNull Expression assigned;
	protected @NonNull AssignExpr.Op operation;
	private @NonNull Expression value;
	
	public AssignExpr(Expression assigned, Expression value) {
		this(assigned, AssignExpr.Op.NONE, value);
	}
	
	public AssignExpr(Expression assigned, AssignExpr.Op operation, Expression value) {
		setAssigned(assigned);
		setOperation(operation);
		setValue(value);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.ASSIGNMENT;
	}
	
	@Override
	public AssignExpr clone() {
		return new AssignExpr(getAssigned().clone(), getOperation(), getValue().clone());
	}
	
	@Override
	public String toCode() {
		return wrap(getAssigned()).toCode() + " " + getOperation() + " " + wrap(getValue()).toCode();
	}
	
	@RequiredArgsConstructor
	public static enum Op {
		NONE("="),
		PLUS("+="),
		MINUS("-="),
		TIMES("*="),
		DIVIDE("/="),
		MODULUS("%="),
		XOR("^="),
		AND("&="),
		OR("|="),
		LSHIFT("<<="),
		RSHIFT(">>="),
		URSHIFT(">>>=");
		
		@Getter @Accessors(fluent = true)
		protected final String toString;
		
		public static Op fromString(String op) {
			return switch(op) {
				case "=" -> NONE;
				case "+=" -> PLUS;
				case "-=" -> MINUS;
				case "*=" -> TIMES;
				case "/=" -> DIVIDE;
				case "%=" -> MODULUS;
				case "^=" -> XOR;
				case "&=" -> AND;
				case "|=" -> OR;
				case "<<=" -> LSHIFT;
				case ">>=" -> RSHIFT;
				case ">>>=" -> URSHIFT;
				default -> throw new IllegalArgumentException("No operator corresponding to " + op + " found");
			};
		}
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitAssignExpr(this, parent, cast(replacer))) {
			getAssigned().accept(visitor, this, this::setAssigned);
			getValue().accept(visitor, this, this::setValue);
		}
	}
	
}
