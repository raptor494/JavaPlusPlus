package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import jtree.util.Either;
import jtree.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ArrayCreator extends Node implements Expression, Dimensioned {
	protected @NonNull Type baseType;
	protected @NonNull Either<? extends List<Size>, ArrayInitializer<? extends Initializer>> sizesOrInitializer;
	private @NonNull List<Dimension> dimensions;
	
	public ArrayCreator(Type baseType, @NonNull List<Size> sizes) {
		this(baseType, Either.first(sizes));
	}
	
	public ArrayCreator(Type baseType, Size... sizes) {
		this(baseType, List.of(sizes));
	}
	
	public ArrayCreator(Type baseType, @NonNull ArrayInitializer<? extends Initializer> initializer) {
		this(baseType, Either.second(initializer));
	}
	
	public ArrayCreator(Type baseType, @NonNull Initializer... initializers) {
		this(baseType, Either.second(new ArrayInitializer<>(initializers)));
	}
	
	public ArrayCreator(Type baseType, List<Size> sizes, List<Dimension> dimensions) {
		this(baseType, Either.first(sizes), dimensions);
	}
	
	public ArrayCreator(Type baseType, ArrayInitializer<? extends Initializer> initializer, List<Dimension> dimensions) {
		this(baseType, Either.second(initializer), dimensions);
	}
	
	public ArrayCreator(Type baseType, Either<? extends List<Size>, ArrayInitializer<? extends Initializer>> sizesOrInitializer) {
		this(baseType, sizesOrInitializer, List.of(new Dimension()));
	}
	
	public ArrayCreator(Type baseType, Either<? extends List<Size>, ArrayInitializer<? extends Initializer>> sizesOrInitializer, List<Dimension> dimensions) {
		setBaseType(baseType);
		setSizesOrInitializer(sizesOrInitializer);
		setDimensions(dimensions);
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.PRIMARY;
	}
	
	@Override
	public ArrayCreator clone() {
		return new ArrayCreator(getBaseType().clone(), clone(getSizesOrInitializer()), clone(getDimensions()));
	}
	
	@Override
	public String toCode() {
		return "new " + getBaseType().toCode() + getSizesOrInitializer().unravel(
			sizes -> joinNodes("", sizes) + dimensionString(),
			initializer -> dimensionString() + " " + initializer.toCode()
		);
	}
	
	public void setSizesOrInitializer(@NonNull Either<? extends List<Size>, ArrayInitializer<? extends Initializer>> sizesOrInitializer) {
		this.sizesOrInitializer = sizesOrInitializer.map(Utils::newList, Objects::requireNonNull);
	}
	
	public final void setSizes(@NonNull List<Size> sizes) {
		setSizesOrInitializer(Either.first(sizes));
	}
	
	public final void setSizes(Size... sizes) {
		setSizes(List.of(sizes));
	}
	
	public final void setInitializer(@NonNull ArrayInitializer<? extends Initializer> initializer) {
		setSizesOrInitializer(Either.second(initializer));
	}
	
	public final void setInitializer(Initializer... initializers) {
		setInitializer(new ArrayInitializer<>(initializers));
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
		if(visitor.visitArrayCreator(this, parent, cast(replacer))) {
			getBaseType().accept(visitor, this, this::setBaseType);
			getSizesOrInitializer().accept(sizes -> visitList(visitor, sizes), initializer -> initializer.<ArrayInitializer<? extends Initializer>>accept(visitor, this, this::setInitializer));
			visitList(visitor, getDimensions());
		}
	}
	
}
