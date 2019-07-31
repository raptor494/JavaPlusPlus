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
public class ArrayInitializer<V extends AnnotationValue> extends Node implements Initializer {
	protected @NonNull List<? extends V> elements;
	
	public ArrayInitializer(List<? extends V> elements) {
		setElements(elements);
	}
	
	@SafeVarargs
	public ArrayInitializer(V... elements) {
		setElements(elements);
	}
	
	@Override
	public ArrayInitializer<V> clone() {
		return new ArrayInitializer<>(clone(elements));
	}
	
	@Override
	public String toCode() {
		return "{" + joinNodes(", ", getElements()) + "}";
	}
	
	public void setElements(@NonNull List<? extends V> elements) {
		this.elements = newList(elements);
	}
	
	@SafeVarargs
	public final void setElements(V... elements) {
		setElements(List.of(elements));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitArrayInitializer(this, parent, (Consumer<ArrayInitializer<V>>)replacer)) {
			visitList(visitor, getElements());
		}
	}
	
}
