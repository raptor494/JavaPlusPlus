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
public class AnnotationDecl extends TypeDecl {
	
	public AnnotationDecl(Name name, List<? extends Member> members) {
		this(name, members, emptyList(), emptyList(), Optional.empty());
	}
	
	public AnnotationDecl(Name name, Member... members) {
		this(name, List.of(members));
	}

	public AnnotationDecl(Name name, List<? extends Member> members,
						  List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(name, emptyList(), members, modifiers, annotations, docComment);
	}
	
	@Override
	public AnnotationDecl clone() {
		return new AnnotationDecl(getName(), clone(getMembers()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}
	
	@Override
	public String toCode() {
		return docString() + annotationString() + modifierString() + "@interface " + getName() + " " + bodyString();
	}
	
	@Override
	public void setTypeParameters(@NonNull List<TypeParameter> typeParameters) {
		if(!typeParameters.isEmpty()) {
			throw new IllegalArgumentException("Annotation declarations cannot have type parameters");
		}
	}
	
	@Override
	public List<TypeParameter> getTypeParameters() {
		return emptyList();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitAnnotationDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getMembers());
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}

}
