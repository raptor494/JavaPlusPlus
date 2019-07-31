package jtree.nodes;

import java.util.List;

public abstract class Type extends TypeArgument {

	protected Type(List<Annotation> annotations) {
		super(annotations);
	}
	
	@Override
	public abstract Type clone();
	
}