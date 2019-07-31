package jtree.nodes;

import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class AssertStmt extends Node implements Statement {
	protected @NonNull Expression condition;
	protected @NonNull Optional<? extends Expression> message;
	
	public AssertStmt(Expression condition) {
		this(condition, Optional.empty());
	}
	
	public AssertStmt(Expression condition, Expression message) {
		this(condition, Optional.ofNullable(message));
	}
	
	public AssertStmt(Expression condition, Optional<? extends Expression> message) {
		setCondition(condition);
		setMessage(message);
	}
	
	@Override
	public AssertStmt clone() {
		return new AssertStmt(getCondition().clone(), clone(getMessage()));
	}
	
	@Override
	public String toCode() {
		return "assert " + getCondition().toCode() + getMessage().map(message -> " : " + message.toCode()).orElse("") + ";";
	}
	
	public void setMessage(@NonNull Optional<? extends Expression> message) {
		this.message = message;
	}
	
	public final void setMessage(Expression message) {
		setMessage(Optional.ofNullable(message));
	}
	
	public final void setMessage() {
		setMessage(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitAssertStmt(this, parent, cast(replacer))) {
			getCondition().accept(visitor, this, this::setCondition);
			getMessage().ifPresent(message -> message.<Expression>accept(visitor, this, this::setMessage));
		}
	}
	
}
