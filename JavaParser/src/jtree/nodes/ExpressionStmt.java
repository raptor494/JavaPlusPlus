package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ExpressionStmt extends Node implements ResourceSpecifier {
	protected @NonNull Expression expression;
	
	public ExpressionStmt(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public ExpressionStmt clone() {
		return new ExpressionStmt(getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return getExpression().toCode() + ";";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitExpressionStmt(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}

}
