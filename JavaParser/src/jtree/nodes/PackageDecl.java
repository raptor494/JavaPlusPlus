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
public class PackageDecl extends Node implements Annotated, Documented, REPLEntry {
	protected @NonNull List<Annotation> annotations;
	protected @NonNull QualifiedName name;
	protected @NonNull Optional<String> docComment;
	
	public PackageDecl(QualifiedName name) {
		this(name, emptyList(), Optional.empty());
	}
	
	public PackageDecl(QualifiedName name, List<Annotation> annotations, Optional<String> docComment) {
		setName(name);
		setAnnotations(annotations);
		setDocComment(docComment);
	}
	
	@Override
	public PackageDecl clone() {
		return new PackageDecl(getName(), clone(getAnnotations()), getDocComment());
	}
	
	@Override
	public String toCode() {
		return docString() + annotationString() + "package " + getName() + ";";
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
		if(visitor.visitPackageDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getAnnotations());
		}
	}
	
}
