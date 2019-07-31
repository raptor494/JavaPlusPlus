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
public class Dimension extends Node implements Annotated {
	protected @NonNull List<Annotation> annotations;
	
	public Dimension() {
		this.annotations = newList();
	}
	
	public Dimension(Annotation... annotations) {
		this(List.of(annotations));
	}
	
	public Dimension(List<Annotation> annotations) {
		setAnnotations(annotations);
	}
	
	@Override
	public Dimension clone() {
		return new Dimension(clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return annotationString(false) + "[]";
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
		if(visitor.visitDimension(this, parent, cast(replacer))) {
			visitList(visitor, getAnnotations());
		}
	}
	
}
