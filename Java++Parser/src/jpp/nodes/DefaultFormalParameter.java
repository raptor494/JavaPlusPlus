package jpp.nodes;

import java.util.List;
import java.util.function.Consumer;

import jtree.nodes.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter @Setter
public class DefaultFormalParameter extends FormalParameter {
	private @NonNull Expression defaultValue;
	
	public DefaultFormalParameter(Type type, Name name, boolean variadic, List<Dimension> dimensions, Expression defaultValue,
								  List<Modifier> modifiers, List<Annotation> annotations) {
		super(type, name, variadic, dimensions, modifiers, annotations);
		setDefaultValue(defaultValue);
	}

	public DefaultFormalParameter(Type type, Name name, boolean variadic, List<Dimension> dimensions, Expression defaultValue) {
		super(type, name, variadic, dimensions);
		setDefaultValue(defaultValue);
	}

	public DefaultFormalParameter(Type type, Name name, boolean variadic, Expression defaultValue) {
		super(type, name, variadic);
		setDefaultValue(defaultValue);
	}

	public DefaultFormalParameter(Type type, Name name, List<Dimension> dimensions, Expression defaultValue, List<Modifier> modifiers,
								  List<Annotation> annotations) {
		super(type, name, dimensions, modifiers, annotations);
		setDefaultValue(defaultValue);
	}

	public DefaultFormalParameter(Type type, Name name, List<Dimension> dimensions, Expression defaultValue) {
		super(type, name, dimensions);
		setDefaultValue(defaultValue);
	}

	public DefaultFormalParameter(Type type, Name name, Expression defaultValue, List<Modifier> modifiers, List<Annotation> annotations) {
		super(type, name, modifiers, annotations);
		setDefaultValue(defaultValue);
	}

	public DefaultFormalParameter(Type type, Name name, Expression defaultValue) {
		super(type, name);
		setDefaultValue(defaultValue);
	}
	
	@Override
	public DefaultFormalParameter clone() {
		return new DefaultFormalParameter(getType().clone(), getName(), isVariadic(), clone(getDimensions()), getDefaultValue().clone(), clone(getModifiers()), clone(getAnnotations()));
	}
	
	@Override
	public String toCode() {
		return super.toCode() + " = " + getDefaultValue().toCode();
	}
	
	public FormalParameter toFormalParameter() {
		return new FormalParameter(getType().clone(), getName(), isVariadic(), clone(getDimensions()), clone(getModifiers()), clone(getAnnotations()));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitFormalParameter(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getDimensions());
			getDefaultValue().accept(visitor, this, this::setDefaultValue);
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}

}
