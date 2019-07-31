package jtree.nodes;

import java.util.Optional;

import lombok.NonNull;

public interface Documented extends INode {
	
	@Override
	Documented clone();
	
	Optional<String> getDocComment();
	
	void setDocComment(@NonNull Optional<String> docComment);
	
	void setDocComment(String docComment);
	
	void setDocComment();
	
	default String docString() {
		return getDocComment().orElse("");
	}
	
}
