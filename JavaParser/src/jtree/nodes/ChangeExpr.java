package jtree.nodes;

public interface ChangeExpr extends Expression {

	@Override
	default Expression wrap(Expression expr) {
		if(expr instanceof CastExpr || expr instanceof ClassCreator || expr instanceof ArrayCreator || expr.precedence().isGreaterThan(this.precedence())) {
			return new ParensExpr(expr);
		} else {
			return expr;
		}
	}
	
}
