package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class InterfaceDecl extends TypeDecl {
	protected @NonNull List<GenericType> superInterfaces;

	public InterfaceDecl(Name name, List<? extends Member> members) {
		this(name, emptyList(), members);
	}
	
	public InterfaceDecl(Name name, Member... members) {
		this(name, List.of(members));
	}

	public InterfaceDecl(Name name, List<GenericType> superInterfaces, List<? extends Member> members) {
		this(name, emptyList(), superInterfaces, members, emptyList(), emptyList(), Optional.empty());
	}
	
	public InterfaceDecl(Name name, List<GenericType> superInterfaces, Member... members) {
		this(name, superInterfaces, List.of(members));
	}

	public InterfaceDecl(Name name, List<? extends Member> members, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, emptyList(), members, modifiers, annotations, docComment);
	}

	public InterfaceDecl(Name name, List<TypeParameter> typeParameters, List<? extends Member> members, List<Modifier> modifiers,
						 List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, emptyList(), members, modifiers, annotations, docComment);
	}

	public InterfaceDecl(Name name, List<TypeParameter> typeParameters, List<GenericType> superInterfaces,
						 List<? extends Member> members, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(name, typeParameters, members, modifiers, annotations, docComment);
		setSuperInterfaces(superInterfaces);
	}
	
	@Override
	public InterfaceDecl clone() {
		return new InterfaceDecl(getName(), clone(getTypeParameters()), clone(getSuperInterfaces()), clone(getMembers()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}

	@Override
	public String toCode() {
		var superInterfaces = getSuperInterfaces();
		return docString() + annotationString() + modifierString() + "interface " + getName() + typeParameterString()
				+ (superInterfaces.isEmpty()? "" : " extends " + joinNodes(", ", superInterfaces))
				+ " " + bodyString();
	}

	public void setSuperInterfaces(@NonNull List<GenericType> superInterfaces) {
		this.superInterfaces = newList(superInterfaces);
	}
	
	public final void setSuperInterfaces(GenericType... superInterfaces) {
		setSuperInterfaces(List.of(superInterfaces));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitInterfaceDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeParameters());
			visitList(visitor, getSuperInterfaces());
			visitList(visitor, getMembers());
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}

}
