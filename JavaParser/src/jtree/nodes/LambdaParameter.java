package jtree.nodes;

import lombok.NonNull;

public interface LambdaParameter extends INode {
	
	@Override
	LambdaParameter clone();
	
	Name getName();
	
	void setName(@NonNull Name name);

}
