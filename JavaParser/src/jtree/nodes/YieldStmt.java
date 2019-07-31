package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class YieldStmt extends Node implements Statement {
	protected @NonNull Expression expression;
	
	public YieldStmt(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public YieldStmt clone() {
		return new YieldStmt(getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return "yield " + expression.toCode() + ";";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitYieldStmt(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
