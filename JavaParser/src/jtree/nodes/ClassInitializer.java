package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ClassInitializer extends Node implements Member {
	protected @NonNull Block block;
	protected boolean isStatic;
	
	public ClassInitializer(boolean isStatic, Block block) {
		setStatic(isStatic);
		setBlock(block);
	}
	
	@Override
	public ClassInitializer clone() {
		return new ClassInitializer(isStatic(), getBlock());
	}
	
	@Override
	public String toCode() {
		return (isStatic()? "static " : "") + getBlock().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitClassInitializer(this, parent, cast(replacer))) {
			getBlock().accept(visitor, this, this::setBlock);
		}
	}
	
}
