package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ClassLiteral extends Node implements Expression {
	protected @NonNull Type type;
	
	public ClassLiteral(Type type) {
		setType(type);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public ClassLiteral clone() {
		return new ClassLiteral(getType().clone());
	}
	
	@Override
	public String toCode() {
		return getType().toCode() + ".class";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitClassLiteral(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
		}
	}
	
}
