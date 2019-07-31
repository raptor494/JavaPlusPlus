package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class CastExpr extends Node implements Expression {
	protected @NonNull Type type;
	protected @NonNull Expression expression;
	
	public CastExpr(Type type, Expression expression) {
		setType(type);
		setExpression(expression);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.UNARY_AND_CAST;
	}
	
	@Override
	public CastExpr clone() {
		return new CastExpr(getType().clone(), getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return "(" + getType().toCode() + ")" + wrap(getExpression()).toCode();
	}
	
	@Override
	public Expression wrap(Expression expr) {
		if(expr instanceof Lambda) {
			var lambda = (Lambda)expr;
			if(lambda.getParameters().isSecond() && lambda.getParameters().second().size() == 1 || lambda.getBody().isSecond()) {
				return new ParensExpr(lambda);
			} else {
				return expr;
			}
		} else if(expr instanceof Switch || expr.precedence().isGreaterThan(this.precedence())) {
			return new ParensExpr(expr);
		} else {
			return expr;
		}
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitCastExpr(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
