package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import jtree.util.Either;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class MethodReference extends Node implements Expression, TypeArgumentHolder {
	protected @NonNull Either<? extends Expression, ? extends Type> object;
	protected @NonNull Name name;
	private @NonNull List<? extends TypeArgument> typeArguments;
	
	public MethodReference(Expression object, List<? extends TypeArgument> typeArguments, Name name) {
		this(Either.first(object), typeArguments, name);
	}
	
	public MethodReference(Type object, List<? extends TypeArgument> typeArguments, Name name) {
		this(Either.second(object), typeArguments, name);
	}
	
	public MethodReference(Either<? extends Expression, ? extends Type> object, List<? extends TypeArgument> typeArguments, Name name) {
		setObject(object);
		setName(name);
		setTypeArguments(typeArguments);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public MethodReference clone() {
		return new MethodReference(clone(getObject()), clone(getTypeArguments()), getName());
	}
	
	@Override
	public String toCode() {
		return getObject().unravel(expr -> wrap(expr).toCode(), Type::toCode)
				+ "::" + typeArgumentString() + getName();
	}
	
	public void setObject(@NonNull Either<? extends Expression, ? extends Type> object) {
		Objects.requireNonNull(object.getValue());
		this.object = object;
	}
	
	public final void setObject(Expression object) {
		setObject(Either.first(object));
	}
	
	public final void setObject(Type object) {
		setObject(Either.second(object));
	}
	
	@Override
	public void setTypeArguments(@NonNull List<? extends TypeArgument> typeArguments) {
		this.typeArguments = newList(typeArguments);
	}
	
	@Override
	public final void setTypeArguments(TypeArgument... typeArguments) {
		setTypeArguments(List.of(typeArguments));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitMethodReference(this, parent, cast(replacer))) {
			getObject().accept(expr -> expr.<Expression>accept(visitor, this, this::setObject), type -> type.<Type>accept(visitor, this, this::setObject));
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeArguments());
		}
	}
	
}
