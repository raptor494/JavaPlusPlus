package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class PostIncrementExpr extends Node implements ChangeExpr {
	protected @NonNull Expression expression;
	
	public PostIncrementExpr(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.POST_UNARY;
	}
	
	@Override
	public PostIncrementExpr clone() {
		return new PostIncrementExpr(getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return wrap(getExpression()).toCode() + "++";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitPostIncrementExpr(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
