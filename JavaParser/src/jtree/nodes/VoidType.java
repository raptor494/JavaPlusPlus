package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class VoidType extends Type {
	
	public VoidType() {
		super(emptyList());
	}
	
	public VoidType(List<Annotation> annotations) {
		super(annotations);
	}
	
	@Override
	public VoidType clone() {
		return new VoidType(clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return annotationString(false) + "void";
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitVoidType(this, parent, cast(replacer))) {
			visitList(visitor, getAnnotations());
		}
	}
	
}
