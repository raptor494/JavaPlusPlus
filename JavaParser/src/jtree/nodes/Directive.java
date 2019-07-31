package jtree.nodes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public abstract class Directive extends Node {
	protected @NonNull QualifiedName name;
	
	public Directive(QualifiedName name) {
		setName(name);
	}
	
	@Override
	public abstract Directive clone();
	
}
