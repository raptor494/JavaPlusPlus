package jtree.nodes;

import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class This extends Node implements Expression {
	protected @NonNull Optional<QualifiedName> qualifier;
	
	public This() {
		this(Optional.empty());
	}
	
	public This(QualifiedName qualifier) {
		this(Optional.ofNullable(qualifier));
	}
	
	public This(Optional<QualifiedName> qualifier) {
		setQualifier(qualifier);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public This clone() {
		return new This(clone(getQualifier()));
	}
	
	@Override
	public String toCode() {
		return getQualifier().map(qualifier -> qualifier.toCode() + ".").orElse("") + "this";
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
		if(visitor.visitThis(this, parent, cast(replacer))) {
			getQualifier().ifPresent(qualifier -> qualifier.<QualifiedName>accept(visitor, this, this::setQualifier));
		}
	}
	
}
