package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public abstract class TypeArgument extends Node implements Annotated {
	protected @NonNull List<Annotation> annotations;
	
	protected TypeArgument(List<Annotation> annotations) {
		setAnnotations(annotations);
	}
	
	@Override
	public abstract TypeArgument clone();
	
	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		this.annotations = newList(annotations);
	}
	
	@Override
	public final void setAnnotations(Annotation... annotations) {
		setAnnotations(List.of(annotations));
	}
	
}
