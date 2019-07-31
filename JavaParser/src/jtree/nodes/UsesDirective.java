package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class UsesDirective extends Directive {
	
	public UsesDirective(QualifiedName typeName) {
		super(typeName);
	}
	
	@Override
	public UsesDirective clone() {
		return new UsesDirective(getName());
	}
	
	@Override
	public String toCode() {
		return "uses " + getName() + ";";
	}
	
	@Override
	public void setName(@NonNull QualifiedName typeName) {
		if(typeName.lastName().equals("var")) {
			throw new IllegalArgumentException("\"var\" cannot be used as a type name");
		}
		super.setName(typeName);
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitUsesDirective(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
		}
	}
	
}
