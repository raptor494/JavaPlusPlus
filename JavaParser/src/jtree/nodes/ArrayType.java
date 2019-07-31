package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ArrayType extends ReferenceType implements Dimensioned {
	protected @NonNull Type baseType;
	protected @NonNull List<Dimension> dimensions;
	
	public ArrayType(Type baseType, List<Dimension> dimensions) {
		this(baseType, dimensions, emptyList());		
	}
	
	public ArrayType(Type baseType, Dimension... dimensions) {
		this(baseType, List.of(dimensions));
	}
	
	public ArrayType(Type baseType, List<Dimension> dimensions, List<Annotation> annotations) {
		super(annotations);
		setBaseType(baseType);
		setDimensions(dimensions);
	}
	
	@Override
	public ArrayType clone() {
		return new ArrayType(getBaseType().clone(), clone(getDimensions()), clone(getAnnotations()));
	}

	@Override
	public String toCode() {
		return annotationString(false) + getBaseType().toCode() + dimensionString();
	}
	
	@Override
	public void setDimensions(@NonNull List<Dimension> dimensions) {
		this.dimensions = newList(dimensions);
	}
	
	@Override
	public final void setDimensions(Dimension... dimensions) {
		setDimensions(List.of(dimensions));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitArrayType(this, parent, cast(replacer))) {
			getBaseType().accept(visitor, this, this::setBaseType);
			visitList(visitor, getDimensions());
			visitList(visitor, getAnnotations());
		}
	}
	
}
