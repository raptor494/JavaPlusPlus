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
public class FunctionCall extends Node implements Expression, TypeArgumentHolder {
	protected @NonNull Optional<? extends Expression> object;
	protected @NonNull Name name;
	private @NonNull List<? extends Expression> arguments;
	private @NonNull List<? extends TypeArgument> typeArguments;
	
	public FunctionCall(Name name, List<? extends Expression> arguments) {
		this(name, emptyList(), arguments);
	}
	
	public FunctionCall(Name name, Expression... arguments) {
		this(name, List.of(arguments));
	}
	
	public FunctionCall(Name name, List<? extends TypeArgument> typeArguments, List<? extends Expression> arguments) {
		this(Optional.empty(), name, typeArguments, arguments);
	}
	
	public FunctionCall(Name name, List<? extends TypeArgument> typeArguments, Expression... arguments) {
		this(name, typeArguments, List.of(arguments));
	}
	
	public FunctionCall(Optional<? extends Expression> object, Name name, List<? extends Expression> arguments) {
		this(object, name, emptyList(), arguments);
	}
	
	public FunctionCall(Optional<? extends Expression> object, Name name, Expression... arguments) {
		this(object, name, List.of(arguments));
	}
	
	public FunctionCall(Expression object, Name name, List<? extends Expression> arguments) {
		this(Optional.ofNullable(object), name, arguments);
	}
	
	public FunctionCall(Expression object, Name name, Expression... arguments) {
		this(object, name, List.of(arguments));
	}
	
	public FunctionCall(Optional<? extends Expression> object, Name name, List<? extends TypeArgument> typeArguments, Expression... arguments) {
		this(object, name, typeArguments, List.of(arguments));
	}
	
	public FunctionCall(Expression object, Name name, List<? extends TypeArgument> typeArguments, List<? extends Expression> arguments) {
		this(Optional.ofNullable(object), name, typeArguments, arguments);
	}
	
	public FunctionCall(Expression object, Name name, List<? extends TypeArgument> typeArguments, Expression... arguments) {
		this(object, name, typeArguments, List.of(arguments));
	}
	
	public FunctionCall(Optional<? extends Expression> object, Name name, List<? extends TypeArgument> typeArguments, List<? extends Expression> arguments) {
		setObject(object);
		setName(name);
		setTypeArguments(typeArguments);
		setArguments(arguments);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public FunctionCall clone() {
		return new FunctionCall(clone(getObject()), getName(), clone(getTypeArguments()), clone(getArguments()));
	}
	
	@Override
	public String toCode() {
		return getObject().map(object -> wrap(object).toCode() + ".").orElse("")
				+ typeArgumentString()
				+ getName() + "(" + joinNodes(", ", getArguments()) + ")";
	}
	
	@Override
	public void setTypeArguments(@NonNull List<? extends TypeArgument> typeArguments) {
		this.typeArguments = newList(typeArguments);
	}
	
	@Override
	public final void setTypeArguments(TypeArgument... typeArguments) {
		setTypeArguments(List.of(typeArguments));
	}
	
	public void setArguments(@NonNull List<? extends Expression> arguments) {
		this.arguments = newList(arguments);
	}
	
	public final void setArguments(Expression... arguments) {
		setArguments(List.of(arguments));
	}
	
	public void setObject(@NonNull Optional<? extends Expression> object) {
		this.object = object;
	}
	
	public final void setObject(Expression object) {
		setObject(Optional.ofNullable(object));
	}
	
	public final void setObject() {
		setObject(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitFunctionCall(this, parent, cast(replacer))) {
			getObject().ifPresent(object -> object.<Expression>accept(visitor, this, this::setObject));
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeArguments());
			visitList(visitor, getArguments());
		}
	}
	
}
