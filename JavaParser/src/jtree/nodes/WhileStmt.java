package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class WhileStmt extends Node implements CompoundStmt {
	protected @NonNull Expression condition;
	protected @NonNull Statement body;
	
	public WhileStmt(Expression condition, Statement body) {
		setCondition(condition);
		setBody(body);
	}
	
	@Override
	public WhileStmt clone() {
		return new WhileStmt(getCondition().clone(), getBody().clone());
	}
	
	@Override
	public String toCode() {
		return "while(" + getCondition().toCode() + ")" + bodyString(getBody());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitWhileStmt(this, parent, cast(replacer))) {
			getCondition().accept(visitor, this, this::setCondition);
			getBody().accept(visitor, this, this::setBody);
		}
	}
	
}
