package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public abstract class TypeDecl extends GenericDecl implements REPLEntry {
	protected @NonNull List<? extends Member> members;

	public TypeDecl(Name name, List<TypeParameter> typeParameters, List<? extends Member> members, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(name, typeParameters, modifiers, annotations, docComment);
		setMembers(members);
	}
	
	@Override
	public abstract TypeDecl clone();
	
	public String bodyString() {
		var members = getMembers();
		return members.isEmpty()? "{}" : "{\n" + join("", getMembers(), member -> member.toCode().indent(4)) + "}";
	}
	
	public void setMembers(@NonNull List<? extends Member> members) {
		this.members = newList(members);
	}
	
	public final void setMembers(Member... members) {
		setMembers(List.of(members));
	}
	
	@Override
	public void setName(Name name) {
		if(name.equals("var")) {
			throw new IllegalArgumentException("\"var\" cannot be used as a type name");
		}
		super.setName(name);
	}

}
