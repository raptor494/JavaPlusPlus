package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class TypeTest extends Node implements Expression {
	protected @NonNull Expression expression;
	protected @NonNull Type type;
	
	public TypeTest(Expression expression, Type type) {
		setExpression(expression);
		setType(type);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.RELATIONAL;
	}
	
	@Override
	public TypeTest clone() {
		return new TypeTest(getExpression().clone(), getType().clone());
	}
	
	@Override
	public String toCode() {
		return getExpression().toCode() + " instanceof " + getType().toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitTypeTest(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
			getType().accept(visitor, this, this::setType);
		}
	}
	
}
