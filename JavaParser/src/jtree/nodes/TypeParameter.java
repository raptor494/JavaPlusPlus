package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class TypeParameter extends Node implements Annotated {
	protected @NonNull Name name;
	protected @NonNull Optional<? extends ReferenceType> bound;
	private @NonNull List<Annotation> annotations;
	
	public TypeParameter(Name name) {
		this(name, Optional.empty());
	}
	
	public TypeParameter(Name name, Optional<? extends ReferenceType> bound) {
		this(name, bound, emptyList());
	}
	
	public TypeParameter(Name name, ReferenceType bound) {
		this(name, Optional.ofNullable(bound));
	}
	
	public TypeParameter(Name name, ReferenceType bound, List<Annotation> annotations) {
		this(name, Optional.ofNullable(bound), annotations);
	}

	public TypeParameter(Name name, Optional<? extends ReferenceType> bound, List<Annotation> annotations) {
		setName(name);
		setBound(bound);
		setAnnotations(annotations);
	}
	
	@Override
	public TypeParameter clone() {
		return new TypeParameter(getName(), clone(getBound()), clone(getAnnotations()));
	}
	
	@Override
	public String toCode() {
		return annotationString(false) + getName()
			+ bound.map(bound -> " extends " + bound.toCode()).orElse("");
	}
	
	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		this.annotations = newList(annotations);
	}
	
	@Override
	public final void setAnnotations(Annotation... annotations) {
		setAnnotations(List.of(annotations));
	}
	
	public void setBound(@NonNull Optional<? extends ReferenceType> bound) {
		this.bound = bound;
	}
	
	public final void setBound(ReferenceType bound) {
		setBound(Optional.ofNullable(bound));
	}
	
	public final void setBound() {
		setBound(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitTypeParameter(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			getBound().ifPresent(bound -> bound.<ReferenceType>accept(visitor, this, this::setBound));
			visitList(visitor, getAnnotations());
		}
	}
	
}
