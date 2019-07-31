package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class PostDecrementExpr extends Node implements ChangeExpr {
	protected @NonNull Expression expression;
	
	public PostDecrementExpr(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.POST_UNARY;
	}
	
	@Override
	public PostDecrementExpr clone() {
		return new PostDecrementExpr(getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return wrap(getExpression()).toCode() + "--";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitPostDecrementExpr(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
