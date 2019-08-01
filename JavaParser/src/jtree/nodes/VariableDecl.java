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
public class VariableDecl extends Declaration implements ResourceSpecifier, Member {
	protected @NonNull Type type;
	protected @NonNull List<VariableDeclarator> declarators;
	
	public VariableDecl(Type type, Name name) {
		this(type, new VariableDeclarator(name));
	}
	
	public VariableDecl(Type type, Name name, Optional<? extends Initializer> initializer) {
		this(type, new VariableDeclarator(name, initializer));
	}
	
	public VariableDecl(Type type, Name name, Initializer initializer) {
		this(type, name, Optional.ofNullable(initializer));
	}
	
	public VariableDecl(Type type, Name name, List<Dimension> dimensions) {
		this(type, new VariableDeclarator(name, dimensions));
	}
	
	public VariableDecl(Type type, Name name, Dimension... dimensions) {
		this(type, name, List.of(dimensions));
	}
	
	public VariableDecl(Type type, Name name, List<Dimension> dimensions, Optional<? extends Initializer> initializer) {
		this(type, name, dimensions, initializer, emptyList(), emptyList(), Optional.empty());
	}
	
	public VariableDecl(Type type, Name name, List<Dimension> dimensions, Optional<? extends Initializer> initializer, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(type, List.of(new VariableDeclarator(name, dimensions, initializer)), modifiers, annotations, docComment);
	}
	
	public VariableDecl(Type type, Name name, List<Dimension> dimensions, Initializer initializer) {
		this(type, name, dimensions, Optional.ofNullable(initializer));
	}
	
	public VariableDecl(Type type, VariableDeclarator... declarators) {
		this(type, List.of(declarators));
	}
	
	public VariableDecl(Type type, List<VariableDeclarator> declarators) {
		this(type, declarators, emptyList(), emptyList(), Optional.empty());
	}
	
	public VariableDecl(Type type, VariableDeclarator declarator, List<Modifier> modifiers, List<Annotation> annotations) {
		this(type, List.of(declarator), modifiers, annotations, Optional.empty());
	}
	
	public VariableDecl(Type type, VariableDeclarator declarator, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(type, List.of(declarator), modifiers, annotations, docComment);
	}
	
	public VariableDecl(Type type, List<VariableDeclarator> declarators, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(modifiers, annotations, docComment);
		setType(type);
		setDeclarators(declarators);
	}
	
	@Override
	public VariableDecl clone() {
		return new VariableDecl(getType().clone(), clone(getDeclarators()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}
	
	public String toCode(boolean newlines) {
		return (newlines? docString() : "") + annotationString(newlines) + modifierString() + getType().toCode() + " " + joinNodes(", ", getDeclarators()) + ";";
	}

	@Override
	public String toCode() {
		return toCode(true);
	}
	
	public void setDeclarators(@NonNull List<VariableDeclarator> declarators) {
		if(declarators.isEmpty()) {
			throw new IllegalArgumentException("No declarators given");
		}
		this.declarators = newList(declarators);
	}
	
	public final void setDeclarators(VariableDeclarator... declarators) {
		setDeclarators(List.of(declarators));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitVariableDecl(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
			visitList(visitor, getDeclarators());
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}
	
}
