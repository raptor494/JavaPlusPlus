package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class Variable extends Node implements Expression {
	protected @NonNull Name name;
	
	public Variable(String name) {
		this(new Name(name));
	}
	
	public Variable(Name name) {
		setName(name);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public Variable clone() {
		return new Variable(getName());
	}
	
	@Override
	public String toCode() {
		return getName().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitVariable(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
		}
	}
	
}
