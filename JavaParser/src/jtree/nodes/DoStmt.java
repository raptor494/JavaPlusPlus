package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class DoStmt extends Node implements CompoundStmt {
	protected @NonNull Statement body;
	protected @NonNull Expression condition;
	
	public DoStmt(Statement body, Expression condition) {
		setBody(body);
		setCondition(condition);
	}
	
	@Override
	public DoStmt clone() {
		return new DoStmt(getBody().clone(), getCondition().clone());
	}
	
	@Override
	public String toCode() {
		var body = getBody();
		return "do" + bodyString(body) + (body instanceof Block? " " : "\n") + "while(" + getCondition().toCode() + ");";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitDoStmt(this, parent, cast(replacer))) {
			getBody().accept(visitor, this, this::setBody);
			getCondition().accept(visitor, this, this::setCondition);
		}
	}
	
}
