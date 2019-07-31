package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class InformalParameter extends Node implements LambdaParameter {
	protected @NonNull Name name;
	
	public InformalParameter(Name name) {
		setName(name);
	}
	
	@Override
	public InformalParameter clone() {
		return new InformalParameter(getName());
	}
	
	@Override
	public String toCode() {
		return getName().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitInformalParameter(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
		}
	}

}
