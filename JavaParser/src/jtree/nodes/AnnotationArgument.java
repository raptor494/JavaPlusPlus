package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class AnnotationArgument extends Node {
	protected Name name;
	protected AnnotationValue value;
	
	public AnnotationArgument(Name name, AnnotationValue value) {
		setName(name);
		setValue(value);
	}
	
	@Override
	public AnnotationArgument clone() {
		return new AnnotationArgument(getName(), getValue().clone());
	}
	
	@Override
	public String toCode() {
		return getName() + " = " + getValue().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitAnnotationArgument(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			getValue().accept(visitor, this, this::setValue);
		}
	}
	
}
