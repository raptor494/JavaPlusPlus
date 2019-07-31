package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class PreIncrementExpr extends Node implements ChangeExpr {
	protected @NonNull Expression expression;
	
	public PreIncrementExpr(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.UNARY_AND_CAST;
	}
	
	@Override
	public PreIncrementExpr clone() {
		return new PreIncrementExpr(wrap(getExpression()).clone());
	}
	
	@Override
	public String toCode() {
		return "++" + getExpression().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitPreIncrementExpr(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
