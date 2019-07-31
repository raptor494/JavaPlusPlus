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
public class ThisParameter extends Declaration {
	protected @NonNull Type type;
	protected @NonNull Optional<Name> qualifier;
	
	public ThisParameter(Type type) {
		this(type, emptyList());
	}
	
	public ThisParameter(Type type, List<Annotation> annotations) {
		this(type, Optional.empty(), annotations);
	}
	
	public ThisParameter(Type type, Optional<Name> qualifier) {
		this(type, qualifier, emptyList());
	}
	
	public ThisParameter(Type type, Name qualifier) {
		this(type, Optional.ofNullable(qualifier));
	}
	
	public ThisParameter(Type type, Optional<Name> qualifier, List<Annotation> annotations) {
		super(emptyList(), annotations, Optional.empty());
		setType(type);
		setQualifier(qualifier);
	}
	
	@Override
	public ThisParameter clone() {
		return new ThisParameter(getType().clone(), clone(getQualifier()), clone(getAnnotations()));
	}
	
	@Override
	public String toCode() {
		return annotationString(false) + getType().toCode() + " " + getQualifier().map(qualifier -> qualifier.toCode() + ".").orElse("") + "this";
	}
	
	@Override
	public List<Modifier> getModifiers() {
		return emptyList();
	}
	
	public void setQualifier(@NonNull Optional<Name> qualifier) {
		this.qualifier = qualifier;
	}
	
	public final void setQualifier(Name qualifier) {
		setQualifier(Optional.ofNullable(qualifier));
	}
	
	public final void setQualifier() {
		setQualifier(Optional.empty());
	}
	
	@Override
	public void setDocComment(@NonNull Optional<String> docComment) {
		if(!docComment.isEmpty()) {
			throw new IllegalArgumentException("Formal parameters cannot have doc comments");
		}
	}
	
	@Override
	public Optional<String> getDocComment() {
		return Optional.empty();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitThisParameter(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
			getQualifier().ifPresent(qualifier -> qualifier.<Name>accept(visitor, this, this::setQualifier));
			visitList(visitor, getAnnotations());
		}
	}
	
}
