package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class SynchronizedStmt extends Node implements CompoundStmt {
	protected @NonNull Expression lock;
	protected @NonNull Block body;
	
	public SynchronizedStmt(Expression lock, Block body) {
		setLock(lock);
		setBody(body);
	}
	
	@Override
	public SynchronizedStmt clone() {
		return new SynchronizedStmt(getLock().clone(), getBody().clone());
	}
	
	@Override
	public String toCode() {
		return "synchronized(" + getLock().toCode() + ") " + getBody().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitSynchronizedStmt(this, parent, cast(replacer))) {
			getLock().accept(visitor, this, this::setLock);
			getBody().accept(visitor, this, this::setBody);
		}
	}
	
}
