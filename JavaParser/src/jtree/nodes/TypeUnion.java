package jtree.nodes;

import static jtree.util.Utils.*;
import static lombok.AccessLevel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class TypeUnion extends ReferenceType {
	@Getter(NONE) @Setter(NONE)
	protected @NonNull List<ReferenceType> types;
	
	public TypeUnion(List<? extends ReferenceType> types) {
		this(types, emptyList());
	}
	
	public TypeUnion(ReferenceType... types) {
		this(List.of(types));
	}
	
	public TypeUnion(List<? extends ReferenceType> types, List<Annotation> annotations) {
		super(annotations);
		setTypes(types);
	}
	
	@Override
	public TypeUnion clone() {
		return new TypeUnion(clone(getTypes()), clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return annotationString(false) + joinNodes(" | ", getTypes());
	}
	
	public void setTypes(@NonNull List<? extends ReferenceType> types) {
		if(types.size() < 2) {
			throw new IllegalArgumentException("Not enough types given");
		}
		this.types = new ArrayList<>(types.size());
		for(var type : types) {
			if(type instanceof TypeUnion) {
				this.types.addAll(((TypeUnion)type).getTypes());
			} else {
				this.types.add(type);
			}
		}
	}
	
	public final void setTypes(ReferenceType... types) {
		setTypes(List.of(types));
	}
	
	public List<? extends ReferenceType> getTypes() {
		return types;
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitTypeUnion(this, parent, cast(replacer))) {
			visitList(visitor, getTypes());
			visitList(visitor, getAnnotations());
		}
	}
	
}
