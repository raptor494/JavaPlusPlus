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
public class EnumDecl extends TypeDecl {
	protected @NonNull List<GenericType> interfaces;
	protected @NonNull List<EnumField> constants;

	public EnumDecl(Name name, List<EnumField> constants, List<? extends Member> members) {
		this(name, emptyList(), constants, members);
	}

	public EnumDecl(Name name, List<GenericType> interfaces, List<EnumField> constants, List<? extends Member> members) {
		this(name, interfaces, constants, members, emptyList(), emptyList(), Optional.empty());
	}

	public EnumDecl(Name name, List<EnumField> constants, List<? extends Member> members, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, emptyList(), constants, members, modifiers, annotations, docComment);
	}

	public EnumDecl(Name name, List<GenericType> interfaces,
	                List<EnumField> constants, List<? extends Member> members, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(name, emptyList(), members, modifiers, annotations, docComment);
		setInterfaces(interfaces);
		setConstants(constants);
	}
	
	@Override
	public EnumDecl clone() {
		return new EnumDecl(getName(), clone(getInterfaces()), clone(getConstants()), clone(getMembers()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}

	@Override
	public String toCode() {
		var interfaces = getInterfaces();
		var constants = getConstants();
		var members = getMembers();
		var result = docString() + annotationString() + modifierString() + "enum " + getName()
				+ (interfaces.isEmpty()? "" : " implements " + joinNodes(", ", interfaces))
				+ " {";
		if(!constants.isEmpty() || !members.isEmpty()) {
			if(!constants.isEmpty()) {
				result += "\n" + join("", constants, constant -> constant.toCode().indent(4));
			}
			if(!members.isEmpty()) {
				if(!constants.isEmpty() && result.endsWith("\n")) {
					result = result.substring(0, result.length()-1);
				} else {
					result += "\n    ";
				}
				result += ";\n\n" + join("", members, member -> member.toCode().indent(4));
			}
		}
		return result + "}";
	}

	public void setInterfaces(@NonNull List<GenericType> interfaces) {
		this.interfaces = newList(interfaces);
	}
	
	public final void setInterfaces(GenericType... interfaces) {
		setInterfaces(List.of(interfaces));
	}
	
	public void setConstants(@NonNull List<EnumField> constants) {
		this.constants = newList(constants);
	}
	
	public final void setConstants(EnumField... constants) {
		setConstants(List.of(constants));
	}
	
	@Override
	public void setTypeParameters(@NonNull List<TypeParameter> typeParameters) {
		if(!typeParameters.isEmpty()) {
			throw new IllegalArgumentException("Enums cannot have type parameters");
		}
	}
	
	@Override
	public List<TypeParameter> getTypeParameters() {
		return emptyList();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitEnumDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getInterfaces());
			visitList(visitor, getConstants());
			visitList(visitor, getMembers());
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}

}
