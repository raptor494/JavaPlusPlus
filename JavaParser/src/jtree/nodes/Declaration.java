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
public abstract class Declaration extends Node implements Annotated, Modified, Documented {
	protected @NonNull List<Annotation> annotations;
	protected @NonNull List<Modifier> modifiers;
	protected @NonNull Optional<String> docComment;
	
	public Declaration(List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		setAnnotations(annotations);
		setModifiers(modifiers);
		setDocComment(docComment);
	}
	
	@Override
	public abstract Declaration clone();
	
	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		this.annotations = newList(annotations);
	}
	
	@Override
	public final void setAnnotations(Annotation... annotations) {
		setAnnotations(List.of(annotations));
	}
	
	@Override
	public void setModifiers(@NonNull List<Modifier> modifiers) {
		this.modifiers = newList(modifiers);
	}
	
	@Override
	public final void setModifiers(Modifier... modifiers) {
		setModifiers(List.of(modifiers));
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
	
}
