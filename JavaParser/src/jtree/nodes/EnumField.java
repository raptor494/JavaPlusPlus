package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import jtree.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class EnumField extends Node implements Member, Annotated, Documented {
	protected @NonNull Name name;
	protected @NonNull Optional<? extends List<? extends Expression>> arguments;
	protected @NonNull Optional<? extends List<? extends Member>> members;
	protected @NonNull List<Annotation> annotations;
	protected @NonNull Optional<String> docComment;
	
	public EnumField(Name name) {
		this(name, Optional.empty());
	}
	
	public EnumField(Name name, Optional<? extends List<? extends Expression>> arguments) {
		this(name, arguments, Optional.empty());
	}
	
	public EnumField(Name name, List<? extends Expression> arguments) {
		this(name, Optional.ofNullable(arguments));
	}
	
	public EnumField(Name name, Expression... arguments) {
		this(name, List.of(arguments));
	}
	
	public EnumField(Name name, Optional<? extends List<? extends Expression>> arguments, Optional<? extends List<? extends Member>> members) {
		this(name, arguments, members, emptyList(), Optional.empty());
	}
	
	public EnumField(Name name, List<? extends Expression> arguments, Optional<? extends List<? extends Member>> members) {
		this(name, Optional.ofNullable(arguments), members);
	}
	
	public EnumField(Name name, List<? extends Expression> arguments, List<? extends Member> members) {
		this(name, Optional.ofNullable(arguments), Optional.ofNullable(members));
	}
	
	public EnumField(Name name, List<? extends Expression> arguments, Optional<? extends List<? extends Member>> members, List<Annotation> annotations, Optional<String> docComment) {
		this(name, Optional.ofNullable(arguments), members, annotations, docComment);
	}
	
	public EnumField(Name name, List<? extends Expression> arguments, List<? extends Member> members, List<Annotation> annotations, Optional<String> docComment) {
		this(name, Optional.ofNullable(arguments), Optional.ofNullable(members), annotations, docComment);
	}
	
	public EnumField(Name name, Optional<? extends List<? extends Expression>> arguments, Optional<? extends List<? extends Member>> members, List<Annotation> annotations, Optional<String> docComment) {
		setName(name);
		setArguments(arguments);
		setMembers(members);
		setAnnotations(annotations);
		setDocComment(docComment);
	}
	
	@Override
	public EnumField clone() {
		return new EnumField(getName(), clone(getArguments()), clone(getMembers()), clone(getAnnotations()), getDocComment());
	}
	
	@Override
	public String toCode() {
		return docString() + annotationString() + getName()
			+ getArguments().map(arguments -> "(" + joinNodes(", ", arguments) + ")")
							.orElse("")
			+ getMembers().map(members -> members.isEmpty()? " {}" : " {\n" + join("", members, member -> member.toCode().indent(4)) + "}")
						  .orElse("");
	}
	
	public void setArguments(@NonNull Optional<? extends List<? extends Expression>> arguments) {
		this.arguments = arguments.map(Utils::newList);
	}
	
	public final void setArguments(List<? extends Expression> arguments) {
		setArguments(Optional.ofNullable(arguments));
	}
	
	public final void setArguments(Expression... arguments) {
		setArguments(List.of(arguments));
	}
	
	public void setMembers(@NonNull Optional<? extends List<? extends Member>> members) {
		this.members = members.map(Utils::newList);
	}
	
	public final void setMembers(List<? extends Member> members) {
		setMembers(Optional.ofNullable(members));
	}
	
	public final void setMembers(Member... members) {
		setMembers(List.of(members));
	}
	
	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		this.annotations = newList(annotations);
	}
	
	@Override
	public final void setAnnotations(Annotation... annotations) {
		setAnnotations(List.of(annotations));
	}
	
	@Override
	public void setDocComment(@NonNull Optional<String> docComment) {
		this.docComment = docComment;
	}
	
	@Override
	public final void setDocComment(String docComment) {
		setDocComment(Optional.ofNullable(docComment));
	}
	
	@Override
	public final void setDocComment() {
		setDocComment(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitEnumField(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			getArguments().ifPresent(arguments -> visitList(visitor, arguments));
			getMembers().ifPresent(members -> visitList(visitor, members));
			visitList(visitor, getAnnotations());
		}
	}
	
}
