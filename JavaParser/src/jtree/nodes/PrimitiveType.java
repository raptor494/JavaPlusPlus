package jtree.nodes;

import static java.util.Collections.*;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class PrimitiveType extends Type {
	public static final Name BOOLEAN = new Name("boolean"),
							 BYTE = new Name("byte"),
							 SHORT = new Name("short"),
							 CHAR = new Name("char"),
							 INT = new Name("int"),
							 LONG = new Name("long"),
							 FLOAT = new Name("float"),
							 DOUBLE = new Name("double");
	
	public static final Set<Name> VALUES = Set.of(BOOLEAN, BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE);
	
	protected @NonNull Name name;
	
	public PrimitiveType(String name) {
		this(new Name(name));
	}
	
	public PrimitiveType(Name name) {
		this(name, emptyList());
	}
	
	public PrimitiveType(String name, List<Annotation> annotations) {
		this(new Name(name), annotations);
	}
	
	public PrimitiveType(Name name, List<Annotation> annotations) {
		super(annotations);
		setName(name);
	}
	
	@Override
	public PrimitiveType clone() {
		return new PrimitiveType(getName(), clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return annotationString(false) + getName();
	}
	
	public void setName(@NonNull Name name) {
		if(!VALUES.contains(name)) {
			throw new IllegalArgumentException("'" + name + "' is not a primitive type");
		}
		this.name = name;
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitPrimitiveType(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
		}
	}
	
}
