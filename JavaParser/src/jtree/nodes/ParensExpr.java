package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ParensExpr extends Node implements Expression {
	protected @NonNull Expression expression;
	
	public ParensExpr(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public ParensExpr clone() {
		return new ParensExpr(getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return "(" + getExpression().toCode() + ")";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitParensExpr(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
