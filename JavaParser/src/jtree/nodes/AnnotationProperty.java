package jtree.nodes;

import static jtree.util.Utils.*;
import static lombok.AccessLevel.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class AnnotationProperty extends Declaration implements Member {
	protected @NonNull Name name;
	protected @NonNull Type type;
	@Getter(NONE) @Setter(NONE)
	protected @NonNull Optional<? extends AnnotationValue> _default;
	
	public AnnotationProperty(Name name, Type type) {
		this(name, type, Optional.empty());
	}
	
	public AnnotationProperty(Name name, Type type, AnnotationValue _default) {
		this(name, type, Optional.ofNullable(_default));
	}
	
	public AnnotationProperty(Name name, Type type, Optional<? extends AnnotationValue> _default) {
		this(name, type, _default, emptyList(), emptyList(), Optional.empty());
	}
	
	public AnnotationProperty(Name name, Type type, Optional<? extends AnnotationValue> _default, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(modifiers, annotations, docComment);
		setName(name);
		setType(type);
		setDefault(_default);
	}
	
	@Override
	public AnnotationProperty clone() {
		return new AnnotationProperty(getName(), getType().clone(), clone(getDefault()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}
	
	@Override
	public String toCode() {
		return docString() + annotationString() + modifierString() + getType().toCode() + " " + getName() + "()"
				+ getDefault().map(_default -> " default " + _default.toCode()).orElse("") + ";";
	}
	
	public void setDefault(@NonNull Optional<? extends AnnotationValue> _default) {
		this._default = _default;
	}
	
	public final void setDefault(AnnotationValue _default) {
		setDefault(Optional.ofNullable(_default));
	}
	
	public final void setDefault() {
		setDefault(Optional.empty());
	}
	
	public Optional<? extends AnnotationValue> getDefault() {
		return _default;
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitAnnotationProperty(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			getType().accept(visitor, this, this::setType);
			getDefault().ifPresent(_default -> _default.<AnnotationValue>accept(visitor, this, this::setDefault));
		}
	}

}
