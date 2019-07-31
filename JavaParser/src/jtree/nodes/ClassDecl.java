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
public class ClassDecl extends TypeDecl implements Statement {
	protected @NonNull Optional<GenericType> superClass;
	protected @NonNull List<GenericType> interfaces;

	public ClassDecl(Name name, List<TypeParameter> typeParameters, List<Member> members) {
		this(name, typeParameters, members, emptyList(), emptyList(), Optional.empty());
	}

	public ClassDecl(Name name, List<? extends Member> members) {
		this(name, members, emptyList(), emptyList(), Optional.empty());
	}

	public ClassDecl(Name name, List<? extends Member> members, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, emptyList(), members, modifiers, annotations, docComment);
	}

	public ClassDecl(Name name, List<TypeParameter> typeParameters, List<? extends Member> members,
					 List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, Optional.empty(), emptyList(), members, modifiers, annotations, docComment);
	}

	public ClassDecl(Name name, List<TypeParameter> typeParameters, Optional<GenericType> superClass,
					 List<GenericType> interfaces, List<? extends Member> members) {
		this(name, typeParameters, superClass, interfaces, members, emptyList(), emptyList(), Optional.empty());
	}

	public ClassDecl(Name name, Optional<GenericType> superClass, List<GenericType> interfaces, List<? extends Member> members) {
		this(name, superClass, interfaces, members, emptyList(), emptyList(), Optional.empty());
	}

	public ClassDecl(Name name, Optional<GenericType> superClass, List<GenericType> interfaces, List<? extends Member> members,
					 List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, emptyList(), superClass, interfaces, members, modifiers, annotations, docComment);
	}

	public ClassDecl(Name name, List<TypeParameter> typeParameters, Optional<GenericType> superClass,
					 List<? extends Member> members) {
		this(name, typeParameters, superClass, emptyList(), members, emptyList(), emptyList(), Optional.empty());
	}

	public ClassDecl(Name name, Optional<GenericType> superClass, List<? extends Member> members) {
		this(name, superClass, emptyList(), members, emptyList(), emptyList(), Optional.empty());
	}

	public ClassDecl(Name name, Optional<GenericType> superClass, List<? extends Member> members, List<Modifier> modifiers,
					 List<Annotation> annotations, Optional<String> docComment) {
		this(name, emptyList(), superClass, emptyList(), members, modifiers, annotations, docComment);
	}

	public ClassDecl(Name name, List<TypeParameter> typeParameters, Optional<GenericType> superClass,
					 List<GenericType> interfaces, List<? extends Member> members, List<Modifier> modifiers,
					 List<Annotation> annotations, Optional<String> docComment) {
		super(name, typeParameters, members, modifiers, annotations, docComment);
		setSuperClass(superClass);
		setInterfaces(interfaces);
	}
	
	@Override
	public ClassDecl clone() {
		return new ClassDecl(getName(), clone(getTypeParameters()), clone(getSuperClass()), clone(getInterfaces()), clone(getMembers()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}

	@Override
	public String toCode() {
		var interfaces = getInterfaces();
		return docString() + annotationString() + modifierString() + "class " + getName() + typeParameterString()
				+ getSuperClass().map(superClass -> " extends " + superClass.toCode()).orElse("")
				+ (interfaces.isEmpty()? "" : " implements " + joinNodes(", ", interfaces))
				+ " " + bodyString();
	}

	public void setInterfaces(@NonNull List<GenericType> interfaces) {
		this.interfaces = newList(interfaces);
	}
	
	public final void setInterfaces(GenericType... interfaces) {
		setInterfaces(List.of(interfaces));
	}
	
	public void setSuperClass(@NonNull Optional<GenericType> superClass) {
		this.superClass = superClass;
	}
	
	public final void setSuperClass(GenericType superClass) {
		setSuperClass(Optional.ofNullable(superClass));
	}
	
	public final void setSuperClass() {
		setSuperClass(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitClassDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeParameters());
			getSuperClass().ifPresent(superClass -> superClass.<GenericType>accept(visitor, this, this::setSuperClass));
			visitList(visitor, getInterfaces());
			visitList(visitor, getMembers());
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}

}
