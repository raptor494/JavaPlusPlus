package jtree.nodes;

import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class IfStmt extends Node implements CompoundStmt {
	protected @NonNull Expression condition;
	protected @NonNull Statement body;
	private @NonNull Optional<? extends Statement> elseBody;
	
	public IfStmt(Expression condition, Statement body) {
		this(condition, body, Optional.empty());
	}
	
	public IfStmt(Expression condition, Statement body, Statement elseBody) {
		this(condition, body, Optional.ofNullable(elseBody));
	}
	
	public IfStmt(Expression condition, Statement body, Optional<? extends Statement> elseBody) {
		setCondition(condition);
		setBody(body);
		setElseBody(elseBody);
	}
	
	@Override
	public IfStmt clone() {
		return new IfStmt(getCondition().clone(), getBody().clone(), clone(getElseBody()));
	}
	
	@Override
	public String toCode() {
		return "if(" + condition.toCode() + ")" + bodyString(body)
				+ elseBody.map(body -> (getBody() instanceof Block? " else" : "\nelse") 
				               + (body instanceof IfStmt? " " + body.toCode() : bodyString(body)))
						  .orElse("");
	}
	
	public void setElseBody(@NonNull Optional<? extends Statement> elseBody) {
		this.elseBody = elseBody;
	}
	
	public final void setElseBody(Statement elseBody) {
		setElseBody(Optional.ofNullable(elseBody));
	}
	
	public final void setElseBody() {
		setElseBody(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitIfStmt(this, parent, cast(replacer))) {
			getCondition().accept(visitor, this, this::setCondition);
			getBody().accept(visitor, this, this::setBody);
			getElseBody().ifPresent(body -> body.<Statement>accept(visitor, this, this::setElseBody));
		}
	}
	
}
