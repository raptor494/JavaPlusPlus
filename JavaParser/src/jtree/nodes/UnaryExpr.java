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
public class UnaryExpr extends Node implements Expression {
	protected @NonNull UnaryExpr.Op operation;
	protected @NonNull Expression operand;
	
	public UnaryExpr(UnaryExpr.Op operation, Expression operand) {
		setOperation(operation);
		setOperand(operand);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.UNARY_AND_CAST;
	}
	
	@Override
	public UnaryExpr clone() {
		return new UnaryExpr(getOperation(), getOperand().clone());
	}
	
	@Override
	public String toCode() {
		return getOperation() + wrap(getOperand()).toCode();
	}
	
	@RequiredArgsConstructor
	public static enum Op {
		NEGATE("-"),
		POSITIVE("+"),
		INVERT("~"),
		NOT("!");
		
		@Getter @Accessors(fluent = true)
		protected final String toString;
		
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitUnaryExpr(this, parent, cast(replacer))) {
			getOperand().accept(visitor, this, this::setOperand);
		}
	}
	
}
