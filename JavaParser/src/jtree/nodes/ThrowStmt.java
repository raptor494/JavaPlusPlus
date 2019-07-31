package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ThrowStmt extends Node implements Statement {
	protected @NonNull Expression expression;
	
	public ThrowStmt(Expression expression) {
		setExpression(expression);
	}
	
	@Override
	public ThrowStmt clone() {
		return new ThrowStmt(getExpression().clone());
	}
	
	@Override
	public String toCode() {
		return "throw " + expression.toCode() + ";";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitThrowStmt(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
		}
	}
	
}
