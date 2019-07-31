package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class IndexExpr extends Node implements Expression {
	protected @NonNull Expression index, indexed;
	
	public IndexExpr(Expression indexed, Expression index) {
		setIndexed(indexed);
		setIndex(index);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public IndexExpr clone() {
		return new IndexExpr(getIndexed(), getIndex());
	}
	
	@Override
	public String toCode() {
		return wrap(getIndexed()).toCode() + "[" + getIndex().toCode() + "]";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitIndexExpr(this, parent, cast(replacer))) {
			getIndexed().accept(visitor, this, this::setIndexed);
			getIndex().accept(visitor, this, this::setIndex);
		}
	}
	
}
