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
public class ConstructorCall extends Node implements Statement, TypeArgumentHolder {
	protected @NonNull Optional<? extends Expression> object;
	protected @NonNull List<? extends TypeArgument> typeArguments;
	private @NonNull ConstructorCall.Type type;
	private @NonNull List<? extends Expression> arguments;

	public ConstructorCall(ConstructorCall.Type type, List<? extends Expression> arguments) {
		this(emptyList(), type, arguments);
	}
	
	public ConstructorCall(ConstructorCall.Type type, Expression... arguments) {
		this(type, List.of(arguments));
	}
	
	public ConstructorCall(List<? extends TypeArgument> typeArguments, ConstructorCall.Type type, List<? extends Expression> arguments) {
		this(Optional.empty(), typeArguments, type, arguments);
	}
	
	public ConstructorCall(List<? extends TypeArgument> typeArguments, ConstructorCall.Type type, Expression... arguments) {
		this(Optional.empty(), typeArguments, type, List.of(arguments));
	}
	
	public ConstructorCall(Expression object, List<? extends TypeArgument> typeArguments, ConstructorCall.Type type, List<? extends Expression> arguments) {
		this(Optional.ofNullable(object), typeArguments, type, arguments);
	}
	
	public ConstructorCall(Expression object, List<? extends TypeArgument> typeArguments, ConstructorCall.Type type, Expression... arguments) {
		this(object, typeArguments, type, List.of(arguments));
	}
	
	public ConstructorCall(Optional<? extends Expression> object, ConstructorCall.Type type, List<? extends Expression> arguments) {
		this(object, emptyList(), type, arguments);
	}
	
	public ConstructorCall(Optional<? extends Expression> object, ConstructorCall.Type type, Expression... arguments) {
		this(object, type, List.of(arguments));
	}
	
	public ConstructorCall(Expression object, ConstructorCall.Type type, List<? extends Expression> arguments) {
		this(Optional.ofNullable(object), type, arguments);
	}
	
	public ConstructorCall(Expression object, ConstructorCall.Type type, Expression... arguments) {
		this(object, type, List.of(arguments));
	}
	
	public ConstructorCall(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, ConstructorCall.Type type, List<? extends Expression> arguments) {
		setObject(object);
		setTypeArguments(typeArguments);
		setType(type);
		setArguments(arguments);
	}
	
	@Override
	public ConstructorCall clone() {
		return new ConstructorCall(clone(getObject()), clone(getTypeArguments()), getType(), clone(getArguments()));
	}
	
	@Override
	public String toCode() {
		return getObject().map(object -> object.toCode() + ".").orElse("") + typeArgumentString() 
				+ getType().toString() + "(" + joinNodes(", ", getArguments()) + ");";
	}
	
	public void setArguments(@NonNull List<? extends Expression> arguments) {
		this.arguments = newList(arguments);
	}
	
	public final void setArguments(Expression... arguments) {
		setArguments(List.of(arguments));
	}
	
	@Override
	public void setTypeArguments(@NonNull List<? extends TypeArgument> typeArguments) {
		this.typeArguments = newList(typeArguments);
	}
	
	@Override
	public final void setTypeArguments(TypeArgument... typeArguments) {
		setTypeArguments(List.of(typeArguments));
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
	
	public static enum Type {
		THIS, SUPER;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitConstructorCall(this, parent, cast(replacer))) {
			getObject().ifPresent(object -> object.<Expression>accept(visitor, this, this::setObject));
			visitList(visitor, getTypeArguments());
			visitList(visitor, getArguments());
		}
	}
	
}
