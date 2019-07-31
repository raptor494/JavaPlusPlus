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
public class BinaryExpr extends Node implements Expression {
	protected @NonNull Expression left;
	protected @NonNull BinaryExpr.Op operation;
	private @NonNull Expression right;
	
	public BinaryExpr(Expression left, BinaryExpr.Op operation, Expression right) {
		setOperation(operation);
		setLeft(left);
		setRight(right);
	}
	
	@Override
	public Precedence precedence() {
		return operation.precedence;
	}
	
	@Override
	public BinaryExpr clone() {
		return new BinaryExpr(getLeft().clone(), getOperation(), getRight().clone());
	}
	
	@Override
	public String toCode() {
		return wrap(getLeft()).toCode() + " " + getOperation() + " " + wrap(getRight()).toCode(); 
	}
	
	@RequiredArgsConstructor
	public static enum Op { // @formatter:off
		OR      ("||",  Precedence.LOGIC_OR),
		AND     ("&&",  Precedence.LOGIC_AND),
		BIT_OR  ("|",   Precedence.BIT_OR),
		XOR     ("^",   Precedence.BIT_XOR),
		BIT_AND ("&",   Precedence.BIT_AND),
		EQUAL   ("==",  Precedence.EQUALITY),
		NEQUAL  ("!=",  Precedence.EQUALITY),	
		LTHAN   ("<",   Precedence.RELATIONAL),
		GTHAN   (">",   Precedence.RELATIONAL),
		LEQUAL  ("<=",  Precedence.RELATIONAL),
		GEQUAL  (">=",  Precedence.RELATIONAL),
		LSHIFT  ("<<",  Precedence.BIT_SHIFT),
		RSHIFT  (">>",  Precedence.BIT_SHIFT),
		URSHIFT (">>>", Precedence.BIT_SHIFT),
		PLUS    ("+",   Precedence.ADDITIVE),
		MINUS   ("-",   Precedence.ADDITIVE),
		TIMES   ("*",   Precedence.MULTIPLICATIVE),
		DIVIDE  ("/",   Precedence.MULTIPLICATIVE),
		MODULUS ("%",   Precedence.MULTIPLICATIVE);
		
		// @formatter:on 
		
		@Getter @Accessors(fluent = true)
		private final String toString;
		
		public final Precedence precedence;
		
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitBinaryExpr(this, parent, cast(replacer))) {
			getLeft().accept(visitor, this, this::setLeft);
			getRight().accept(visitor, this, this::setRight);
		}
	}
	
}
