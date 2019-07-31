package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class Catch extends Node {
	protected @NonNull FormalParameter parameter;
	protected @NonNull Block body;
	
	public Catch(FormalParameter parameter, Block body) {
		setParameter(parameter);
		setBody(body);
	}
	
	@Override
	public Catch clone() {
		return new Catch(getParameter().clone(), getBody().clone());
	}
	
	@Override
	public String toCode() {
		return "catch(" + getParameter().toCode() + ") " + body.toCode(); 
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitCatch(this, parent, cast(replacer))) {
			getParameter().accept(visitor, this, this::setParameter);
			getBody().accept(visitor, this, this::setBody);
		}
	}
	
}
