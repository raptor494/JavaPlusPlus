package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ForEachStmt extends Node implements CompoundStmt {
	protected @NonNull FormalParameter variable;
	protected @NonNull Expression iterable;
	private @NonNull Statement body;
	
	public ForEachStmt(FormalParameter variable, Expression iterable, Statement body) {
		setVariable(variable);
		setIterable(iterable);
		setBody(body);
	}
	
	@Override
	public ForEachStmt clone() {
		return new ForEachStmt(getVariable().clone(), getIterable().clone(), getBody().clone());
	}
	
	@Override
	public String toCode() {
		return "for(" + getVariable().toCode() + " : " + getIterable().toCode() + ")" + bodyString(getBody());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitForEachStmt(this, parent, cast(replacer))) {
			getVariable().accept(visitor, this, this::setVariable);
			getIterable().accept(visitor, this, this::setIterable);
			getBody().accept(visitor, this, this::setBody);
		}
	}
	
}
