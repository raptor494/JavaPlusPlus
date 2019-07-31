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
public class GenericType extends ReferenceType implements TypeArgumentHolder {
	protected @NonNull QualifiedName name;
	protected @NonNull List<? extends TypeArgument> typeArguments;
	private @NonNull Optional<GenericType> container;
	
	public GenericType(QualifiedName name) {
		this(name, emptyList());
	}
	
	public GenericType(QualifiedName name, Optional<GenericType> container) {
		this(name, emptyList(), container);
	}
	
	public GenericType(QualifiedName name, GenericType container) {
		this(name, Optional.ofNullable(container));
	}
	
	public GenericType(QualifiedName name, List<? extends TypeArgument> typeArguments) {
		this(name, typeArguments, emptyList());
	}
	
	public GenericType(QualifiedName name, List<? extends TypeArgument> typeArguments, Optional<GenericType> container) {
		this(name, typeArguments, container, emptyList());
	}
	
	public GenericType(QualifiedName name, List<? extends TypeArgument> typeArguments, GenericType container) {
		this(name, typeArguments, Optional.ofNullable(container));
	}
	
	public GenericType(QualifiedName name, List<? extends TypeArgument> typeArguments, List<Annotation> annotations) {
		this(name, typeArguments, Optional.empty(), annotations);
	}
	
	public GenericType(QualifiedName name, List<? extends TypeArgument> typeArguments, GenericType container, List<Annotation> annotations) {
		this(name, typeArguments, Optional.ofNullable(container), annotations);
	}
	
	public GenericType(QualifiedName name, List<? extends TypeArgument> typeArguments, Optional<GenericType> container, List<Annotation> annotations) {
		super(annotations);
		setName(name);
		setContainer(container);
		setTypeArguments(typeArguments);
	}
	
	@Override
	public GenericType clone() {
		return new GenericType(getName(), clone(getTypeArguments()), clone(getContainer()), clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return getContainer().map(container -> container.toCode() + ".").orElse("")
				+ annotationString(false) + getName() + typeArgumentString();
	}
	
	@Override
	public void setTypeArguments(@NonNull List<? extends TypeArgument> typeArguments) {
		this.typeArguments = newList(typeArguments);
	}
	
	@Override
	public final void setTypeArguments(TypeArgument... typeArguments) {
		setTypeArguments(List.of(typeArguments));
	}
	
	public void setName(QualifiedName name) {
		/*if(name.lastName().equals("var")) {
			throw new IllegalArgumentException("\"var\" cannot be used as a type name");
		}*/
		this.name = name;
	}
	
	public void setContainer(@NonNull Optional<GenericType> container) {
		this.container = container;
	}
	
	public final void setContainer(GenericType container) {
		setContainer(Optional.ofNullable(container));
	}
	
	public final void setContainer() {
		setContainer(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitGenericType(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			getContainer().ifPresent(container -> container.<GenericType>accept(visitor, this, this::setContainer));
			visitList(visitor, getTypeArguments());
			visitList(visitor, getAnnotations());
		}
	}
	
}
