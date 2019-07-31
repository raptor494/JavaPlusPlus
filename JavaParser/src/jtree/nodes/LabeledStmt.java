package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class LabeledStmt extends Node implements CompoundStmt {
	protected @NonNull Name label;
	protected @NonNull Statement statement;
	
	public LabeledStmt(Name label, Statement statement) {
		setLabel(label);
		setStatement(statement);
	}
	
	@Override
	public LabeledStmt clone() {
		return new LabeledStmt(getLabel(), getStatement().clone());
	}
	
	@Override
	public String toCode() {
		return getLabel() + ":" + bodyString(getStatement());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitLabeledStmt(this, parent, cast(replacer))) {
			getLabel().accept(visitor, this, this::setLabel);
			getStatement().accept(visitor, this, this::setStatement);
		}
	}
	
}
