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
public class TypeIntersection extends ReferenceType {
	@Getter(NONE) @Setter(NONE)
	protected @NonNull List<ReferenceType> types;
	
	public TypeIntersection(List<? extends ReferenceType> types) {
		this(types, emptyList());
	}
	
	public TypeIntersection(ReferenceType... types) {
		this(List.of(types));
	}
	
	public TypeIntersection(List<? extends ReferenceType> types, List<Annotation> annotations) {
		super(annotations);
		setTypes(types);
	}
	
	@Override
	public TypeIntersection clone() {
		return new TypeIntersection(clone(getTypes()), clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return annotationString(false) + joinNodes(" & ", getTypes());
	}
	
	public void setTypes(@NonNull List<? extends ReferenceType> types) {
		if(types.size() < 2) {
			throw new IllegalArgumentException("Not enough types given");
		}
		this.types = new ArrayList<>(types.size());
		for(var type : types) {
			if(type instanceof TypeIntersection) {
				this.types.addAll(((TypeIntersection)type).getTypes());
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
		if(visitor.visitTypeIntersection(this, parent, cast(replacer))) {
			visitList(visitor, getTypes());
			visitList(visitor, getAnnotations());
		}
	}
	
}
