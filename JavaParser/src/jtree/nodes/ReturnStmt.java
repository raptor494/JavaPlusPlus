package jtree.nodes;

import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ReturnStmt extends Node implements Statement {
	protected @NonNull Optional<? extends Expression> expression;
	
	public ReturnStmt() {
		this(Optional.empty());
	}
	
	public ReturnStmt(Expression expression) {
		this(Optional.ofNullable(expression));
	}
	
	public ReturnStmt(Optional<? extends Expression> expression) {
		setExpression(expression);
	}
	
	@Override
	public ReturnStmt clone() {
		return new ReturnStmt(clone(getExpression()));
	}
	
	@Override
	public String toCode() {
		return "return" + getExpression().map(expression -> " " + expression.toCode()).orElse("") + ";";
	}
	
	public void setExpression(@NonNull Optional<? extends Expression> expression) {
		this.expression = expression;
	}
	
	public final void setExpression(Expression expression) {
		setExpression(Optional.ofNullable(expression));
	}
	
	public final void setExpression() {
		setExpression(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitReturnStmt(this, parent, cast(replacer))) {
			getExpression().ifPresent(expr -> expr.<Expression>accept(visitor, this, this::setExpression));
		}
	}
	
}
