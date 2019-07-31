package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class MemberAccess extends Node implements Expression {
	protected @NonNull Name name;
	protected @NonNull Expression expression;
	
	public MemberAccess(Expression expression, Name name) {
		setExpression(expression);
		setName(name);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public MemberAccess clone() {
		return new MemberAccess(getExpression().clone(), getName());
	}
	
	@Override
	public String toCode() {
		return wrap(getExpression()).toCode() + "." + getName();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitMemberAccess(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
			getName().accept(visitor, this, this::setName);
		}
	}
	
}
