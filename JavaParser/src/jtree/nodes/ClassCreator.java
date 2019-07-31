package jtree.nodes;

import static jtree.util.Utils.*;
import static lombok.AccessLevel.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import jtree.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@EqualsAndHashCode
@Getter @Setter
public class ClassCreator extends Node implements Expression, TypeArgumentHolder {
	protected @NonNull Optional<? extends Expression> object;
	protected @NonNull List<? extends TypeArgument> typeArguments;
	private @NonNull GenericType type;
	@Setter(NONE) @Accessors(fluent = true)
	protected boolean hasDiamond;
	protected @NonNull List<? extends Expression> arguments;
	private @NonNull Optional<? extends List<? extends Member>> members;
	
	public ClassCreator(GenericType type, List<? extends Expression> arguments) {
		this(type, false, arguments);
	}
	
	public ClassCreator(GenericType type, Expression... arguments) {
		this(type, List.of(arguments));
	}
	
	public ClassCreator(GenericType type, boolean hasDiamond, List<? extends Expression> arguments) {
		this(emptyList(), type, hasDiamond, arguments);
	}
	
	public ClassCreator(GenericType type, boolean hasDiamond, Expression... arguments) {
		this(type, hasDiamond, List.of(arguments));
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments) {
		this(Optional.empty(), typeArguments, type, hasDiamond, arguments);
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments, List<? extends Member> members) {
		this(typeArguments, type, hasDiamond, arguments, Optional.ofNullable(members));
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments, Optional<? extends List<? extends Member>> members) {
		this(Optional.empty(), typeArguments, type, hasDiamond, arguments, members);
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, List<? extends Expression> arguments) {
		this(typeArguments, type, false, arguments);
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, List<? extends Expression> arguments, Optional<? extends List<? extends Member>> members) {
		this(Optional.empty(), typeArguments, type, false, arguments, members);
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, Expression... arguments) {
		this(typeArguments, type, hasDiamond, List.of(arguments));
	}
	
	public ClassCreator(List<? extends TypeArgument> typeArguments, GenericType type, Expression... arguments) {
		this(typeArguments, type, List.of(arguments));
	}
	
	public ClassCreator(Expression object, List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments) {
		this(Optional.ofNullable(object), typeArguments, type, hasDiamond, arguments);
	}
	
	public ClassCreator(Expression object, List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, Expression... arguments) {
		this(object, typeArguments, type, hasDiamond, List.of(arguments));
	}
	
	public ClassCreator(Expression object, List<? extends TypeArgument> typeArguments, GenericType type, List<? extends Expression> arguments) {
		this(object, typeArguments, type, false, arguments);
	}
	
	public ClassCreator(Expression object, List<? extends TypeArgument> typeArguments, GenericType type, Expression... arguments) {
		this(object, typeArguments, type, false, List.of(arguments));
	}
	
	public ClassCreator(Expression object, GenericType type, boolean hasDiamond, List<? extends Expression> arguments) {
		this(object, emptyList(), type, hasDiamond, arguments);
	}
	
	public ClassCreator(Expression object, GenericType type, boolean hasDiamond, Expression... arguments) {
		this(object, emptyList(), type, hasDiamond, List.of(arguments));
	}
	
	public ClassCreator(Expression object, GenericType type, List<? extends Expression> arguments) {
		this(object, emptyList(), type, false, arguments);
	}
	
	public ClassCreator(Expression object, GenericType type, Expression... arguments) {
		this(object, emptyList(), type, false, List.of(arguments));
	}
	
	public ClassCreator(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, Expression... arguments) {
		this(object, typeArguments, type, hasDiamond, List.of(arguments));
	}
	
	public ClassCreator(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, GenericType type, List<? extends Expression> arguments) {
		this(object, typeArguments, type, false, arguments);
	}
	
	public ClassCreator(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, GenericType type, Expression... arguments) {
		this(object, typeArguments, type, false, List.of(arguments));
	}
	
	public ClassCreator(Optional<? extends Expression> object, GenericType type, boolean hasDiamond, List<? extends Expression> arguments) {
		this(object, emptyList(), type, hasDiamond, arguments);
	}
	
	public ClassCreator(Optional<? extends Expression> object, GenericType type, boolean hasDiamond, Expression... arguments) {
		this(object, emptyList(), type, hasDiamond, List.of(arguments));
	}
	
	public ClassCreator(Optional<? extends Expression> object, GenericType type, List<? extends Expression> arguments) {
		this(object, emptyList(), type, false, arguments);
	}
	
	public ClassCreator(Optional<? extends Expression> object, GenericType type, Expression... arguments) {
		this(object, emptyList(), type, false, List.of(arguments));
	}
	
	public ClassCreator(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments) {
		this(object, typeArguments, type, hasDiamond, arguments, Optional.empty());
	}
	
	public ClassCreator(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments, List<? extends Member> members) {
		this(object, typeArguments, type, hasDiamond, arguments, Optional.ofNullable(members));
	}
	
	public ClassCreator(Optional<? extends Expression> object, List<? extends TypeArgument> typeArguments, GenericType type, boolean hasDiamond, List<? extends Expression> arguments, Optional<? extends List<? extends Member>> members) {
		setObject(object);
		setTypeArguments(typeArguments);
		setType(type);
		setHasDiamond(hasDiamond);
		setArguments(arguments);
		setMembers(members);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public ClassCreator clone() {
		return new ClassCreator(clone(getObject()), clone(getTypeArguments()), getType().clone(), hasDiamond(), clone(getArguments()), clone(getMembers()));
	}
	
	@Override
	public String toCode() {
		var typeArguments = getTypeArguments();
		var type = getType();
		return getObject().map(object -> wrap(object).toCode() + ".").orElse("")
				+ "new " + (typeArguments.isEmpty()? "" : typeArgumentString(typeArguments) + " ")
				+ type.toCode() + (hasDiamond() && type.getTypeArguments().isEmpty()? "<>" : "")
				+ "(" + joinNodes(", ", getArguments()) + ")"
				+ getMembers().map(members -> members.isEmpty()? " {}" : " {\n" + join("", members, member -> member.toCode().indent(4)) + "}").orElse("");
	}
	
	public void setHasDiamond(boolean hasDiamond) {
		this.hasDiamond = hasDiamond;
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
	
	public void setMembers(@NonNull Optional<? extends List<? extends Member>> members) {
		this.members = members.map(Utils::newList);
	}
	
	public final void setMembers(List<? extends Member> members) {
		setMembers(Optional.ofNullable(members));
	}
	
	public final void setMembers() {
		setMembers(Optional.empty());
	}
	
	public final void setMembers(Member... members) {
		setMembers(List.of(members));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitClassCreator(this, parent, cast(replacer))) {
			getObject().ifPresent(object -> object.<Expression>accept(visitor, this, this::setObject));
			visitList(visitor, getTypeArguments());
			getType().accept(visitor, this, this::setType);
			visitList(visitor, getArguments());
			getMembers().ifPresent(members -> visitList(visitor, members));
		}
	}
	
}
