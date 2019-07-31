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
public class Size extends Node implements Annotated {
	protected @NonNull List<Annotation> annotations;
	protected @NonNull Expression expression;
	
	public Size(Expression expression) {
		this(expression, emptyList());
	}
	
	public Size(Expression expression, List<Annotation> annotations) {
		setExpression(expression);
		setAnnotations(annotations);
	}
	
	@Override
	public Size clone() {
		return new Size(getExpression().clone(), clone(getAnnotations()));
	}
	
	@Override
	public String toCode() {
		return annotationString() + "[" + getExpression().toCode() + "]";
	}
	
	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		this.annotations = newList(annotations);
	}
	
	@Override
	public final void setAnnotations(Annotation... annotations) {
		setAnnotations(List.of(annotations));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitSize(this, parent, cast(replacer))) {
			getExpression().accept(visitor, this, this::setExpression);
			visitList(visitor, getAnnotations());
		}
	}
	
}
