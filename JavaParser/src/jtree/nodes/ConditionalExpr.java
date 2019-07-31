package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ConditionalExpr extends Node implements Expression {
	protected @NonNull Expression condition, truePart, falsePart;
	
	public ConditionalExpr(Expression condition, Expression truePart, Expression falsePart) {
		setCondition(condition);
		setTruePart(truePart);
		setFalsePart(falsePart);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.TERNARY;
	}
	
	@Override
	public ConditionalExpr clone() {
		return new ConditionalExpr(getCondition().clone(), getTruePart().clone(), getFalsePart().clone());
	}
	
	@Override
	public String toCode() {
		return wrap(getCondition()).toCode() + "? " + getTruePart().toCode() + " : " + wrap(getFalsePart()).toCode();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitConditionalExpr(this, parent, cast(replacer))) {
			getCondition().accept(visitor, this, this::setCondition);
			getTruePart().accept(visitor, this, this::setTruePart);
			getFalsePart().accept(visitor, this, this::setFalsePart);
		}
	}
	
}
