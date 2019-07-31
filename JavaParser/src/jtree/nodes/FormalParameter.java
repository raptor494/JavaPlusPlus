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
public class FormalParameter extends Declaration implements LambdaParameter, Dimensioned {
	protected @NonNull Name name;
	protected @NonNull Type type;
	private @NonNull List<Dimension> dimensions;
	private boolean variadic;
	
	public FormalParameter(Type type, Name name) {
		this(type, name, false);
	}
	
	public FormalParameter(Type type, Name name, boolean variadic) {
		this(type, name, variadic, emptyList());
	}
	
	public FormalParameter(Type type, Name name, List<Modifier> modifiers, List<Annotation> annotations) {
		this(type, name, emptyList(), modifiers, annotations);
	}
	
	public FormalParameter(Type type, Name name, List<Dimension> dimensions) {
		this(type, name, false, dimensions);
	}
	
	public FormalParameter(Type type, Name name, boolean variadic, List<Dimension> dimensions) {
		this(type, name, variadic, dimensions, emptyList(), emptyList());
	}
	
	public FormalParameter(Type type, Name name, List<Dimension> dimensions, List<Modifier> modifiers, List<Annotation> annotations) {
		this(type, name, false, dimensions, modifiers, annotations);
	}
	
	public FormalParameter(Type type, Name name, boolean variadic, List<Dimension> dimensions, List<Modifier> modifiers, List<Annotation> annotations) {
		super(modifiers, annotations, Optional.empty());
		setType(type);
		setName(name);
		setDimensions(dimensions);
		setVariadic(variadic);
	}
	
	@Override
	public FormalParameter clone() {
		return new FormalParameter(getType().clone(), getName(), isVariadic(), clone(getDimensions()), clone(getModifiers()), clone(getAnnotations()));
	}
	
	@Override
	public String toCode() {
		var dimensionString = dimensionString();
		if(dimensionString.startsWith("@")) {
			dimensionString = " " + dimensionString;
		}
		return annotationString(false) + modifierString() + getType().toCode()
				+ (isVariadic()? "... " : " ")
				+ getName() + dimensionString;
	}
	
	@Override
	public void setDimensions(@NonNull List<Dimension> dimensions) {
		this.dimensions = newList(dimensions);
	}
	
	@Override
	public final void setDimensions(Dimension... dimensions) {
		setDimensions(List.of(dimensions));
	}
	
	@Override
	public void setDocComment(@NonNull Optional<String> docComment) {
		if(!docComment.isEmpty()) {
			throw new IllegalArgumentException("Formal parameters cannot have doc comments");
		}
	}
	
	@Override
	public Optional<String> getDocComment() {
		return Optional.empty();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitFormalParameter(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getDimensions());
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}
	
}
