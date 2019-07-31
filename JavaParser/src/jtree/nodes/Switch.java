package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class Switch extends Node implements Expression, CompoundStmt {
	protected @NonNull Expression expression;
	protected @NonNull List<SwitchCase> cases;
	
	public Switch(Expression expression, SwitchCase... cases) {
		this(expression, List.of(cases));
	}
	
	public Switch(Expression expression, List<SwitchCase> cases) {
		setExpression(expression);
		setCases(cases);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public Switch clone() {
		return new Switch(getExpression().clone(), clone(getCases()));
	}
	
	@Override
	public String toCode() {
		return "switch(" + expression.toCode() + ") {\n" + join("", getCases(), aCase -> aCase.toCode().indent(4)) + "}";
	}
	
	public void setCases(@NonNull List<SwitchCase> cases) {
		this.cases = newList(cases);
	}
	
	public final void setCases(SwitchCase... cases) {
		setCases(List.of(cases));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitSwitch(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
			visitList(visitor, getCases());
		}
	}
	
}
