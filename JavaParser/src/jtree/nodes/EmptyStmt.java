package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class EmptyStmt extends Node implements Statement {
	
	@Override
	public EmptyStmt clone() {
		return this;
	}
	
	@Override
	public String toCode() {
		return ";";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		visitor.visitEmptyStmt(this, parent, cast(replacer));
	}
	
}
