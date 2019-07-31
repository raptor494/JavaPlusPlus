package jtree.nodes;

public interface Expression extends Initializer {
	@Override
	Expression clone();
	
	Precedence precedence();
	
	default Expression wrap(Expression expr) {
		if(expr.precedence().isGreaterThan(this.precedence())) {
			return new ParensExpr(expr);
		} else {
			return expr;
		}
	}
	
	default Expression unwrap(Expression expr) {
		if(expr instanceof ParensExpr) {
			var expr2 = ((ParensExpr)expr).getExpression();
			if(expr2.precedence().isLessThanOrEqualTo(this.precedence())) {
				return expr2;
			}
		} else if(expr.precedence().isGreaterThan(this.precedence())) {
			return new ParensExpr(expr);
		}
		return expr;
	}
}
