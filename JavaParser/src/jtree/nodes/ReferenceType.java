package jtree.nodes;

import java.util.List;

public abstract class ReferenceType extends Type {

	protected ReferenceType(List<Annotation> annotations) {
		super(annotations);
	}
	
	@Override
	public abstract ReferenceType clone();
	
}
