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
public class SuperMethodReference extends Node implements Expression, TypeArgumentHolder {
	protected @NonNull Optional<QualifiedName> qualifier;
	protected @NonNull List<? extends TypeArgument> typeArguments;
	private @NonNull Name name;
	
	public SuperMethodReference(Name name) {
		this(Optional.empty(), name);
	}
	
	public SuperMethodReference(List<? extends TypeArgument> typeArguments, Name name) {
		this(Optional.empty(), typeArguments, name);
	}
	
	public SuperMethodReference(QualifiedName qualifier, Name name) {
		this(Optional.ofNullable(qualifier), name);
	}
	
	public SuperMethodReference(QualifiedName qualifier, List<? extends TypeArgument> typeArguments, Name name) {
		this(Optional.ofNullable(qualifier), typeArguments, name);
	}
	
	public SuperMethodReference(Optional<QualifiedName> qualifier, Name name) {
		this(qualifier, emptyList(), name);
	}
	
	public SuperMethodReference(Optional<QualifiedName> qualifier, List<? extends TypeArgument> typeArguments, Name name) {
		setQualifier(qualifier);
		setName(name);
		setTypeArguments(typeArguments);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public SuperMethodReference clone() {
		return new SuperMethodReference(clone(getQualifier()), clone(getTypeArguments()), getName());
	}
	
	@Override
	public String toCode() {
		return getQualifier().map(qualifier -> qualifier.toCode() + ".").orElse("")
				+ "super::" + typeArgumentString() + getName();
	}
	
	@Override
	public void setTypeArguments(@NonNull List<? extends TypeArgument> typeArguments) {
		this.typeArguments = newList(typeArguments);
	}
	
	@Override
	public final void setTypeArguments(TypeArgument... typeArguments) {
		setTypeArguments(List.of(typeArguments));
	}
	
	public void setQualifier(@NonNull Optional<QualifiedName> qualifier) {
		this.qualifier = qualifier;
	}
	
	public final void setQualifier(QualifiedName qualifier) {
		setQualifier(Optional.ofNullable(qualifier));
	}
	
	public final void setQualifier() {
		setQualifier(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitSuperMethodReference(this, parent, cast(replacer))) {
			getQualifier().ifPresent(qualifier -> qualifier.<QualifiedName>accept(visitor, this, this::setQualifier));
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeArguments());
		}
	}
	
}
