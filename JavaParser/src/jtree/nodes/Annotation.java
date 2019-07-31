package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import jtree.util.Either;
import jtree.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class Annotation extends Node implements AnnotationValue {
	protected GenericType type;
	protected Optional<Either<? extends List<AnnotationArgument>, ? extends AnnotationValue>> arguments;
	
	public Annotation(GenericType type) {
		this(type, Optional.empty());
	}
	
	public Annotation(GenericType type, @NonNull List<AnnotationArgument> arguments) {
		this(type, Either.first(arguments));
	}
	
	public Annotation(GenericType type, @NonNull AnnotationValue value) {
		this(type, Either.second(value));
	}
	
	public Annotation(GenericType type, Either<? extends List<AnnotationArgument>, ? extends AnnotationValue> arguments) {
		this(type, Optional.of(arguments));
	}
	
	public Annotation(GenericType type, Optional<Either<? extends List<AnnotationArgument>, ? extends AnnotationValue>> arguments) {
		setType(type);
		setArguments(arguments);
	}
	
	@Override
	public Annotation clone() {
		return new Annotation(getType().clone(), clone(getArguments()));
	}
	
	@Override
	public String toCode() {
		return "@" + getType().toCode()
				+ getArguments().map(either -> either.unravel(nodes -> "(" + joinNodes(", ", nodes) + ")",
				                                              value -> "(" + value.toCode() + ")")
				                     ).orElse("");
	}
	
	public void setArguments(@NonNull Optional<Either<? extends List<AnnotationArgument>, ? extends AnnotationValue>> arguments) {
		this.arguments = arguments.map(either -> either.map(Utils::newList, Objects::requireNonNull));
	}
	
	public final void setArguments(Either<? extends List<AnnotationArgument>, ? extends AnnotationValue> arguments) {
		setArguments(Optional.of(arguments));
	}
	
	public final void setArguments(@NonNull List<AnnotationArgument> arguments) {
		setArguments(Either.first(arguments));
	}
	
	public final void setArguments(@NonNull AnnotationValue value) {
		setArguments(Either.second(value));
	}
	
	public final void setArguments(AnnotationArgument... arguments) {
		setArguments(List.of(arguments));
	}
	
	public final void setArguments() {
		setArguments(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitAnnotation(this, parent, cast(replacer))) {
			getType().accept(visitor, this, this::setType);
			getArguments().ifPresent(either -> either.accept(
				list -> visitList(visitor, list), 
				value -> value.accept(visitor, this, (AnnotationValue replacement) -> setArguments(replacement))
			));
		}
	}

}
