package jtree.nodes;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

public interface Annotated extends INode {
	
	@Override
	Annotated clone();
	
	List<Annotation> getAnnotations();
	
	void setAnnotations(@NonNull List<Annotation> annotations);
	
	void setAnnotations(Annotation... annotations);
	
	default String annotationString(boolean newlines) {
		var annotations = getAnnotations();
		if(annotations.isEmpty()) {
			return "";
		} else {
			String sep = newlines? "\n" : " ";
			return annotations.stream()
							  .map(Annotation::toCode)
							  .collect(Collectors.joining(sep, "", sep));
		}
	}
	
	default String annotationString() {
		return annotationString(true);
	}
	
}
