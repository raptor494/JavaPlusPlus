package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class VariableDeclarator extends Node implements Dimensioned {
	protected @NonNull List<Dimension> dimensions;
	protected @NonNull Name name;
	private @NonNull Optional<? extends Initializer> initializer;

	public VariableDeclarator(Name name) {
		this(name, emptyList());
	}

	public VariableDeclarator(Name name, List<Dimension> dimensions) {
		this(name, dimensions, Optional.empty());
	}

	public VariableDeclarator(Name name, Dimension... dimensions) {
		this(name, List.of(dimensions));
	}

	public VariableDeclarator(Name name, Optional<? extends Initializer> initializer) {
		this(name, emptyList(), initializer);
	}

	public VariableDeclarator(Name name, Initializer initializer) {
		this(name, Optional.ofNullable(initializer));
	}

	public VariableDeclarator(Name name, List<Dimension> dimensions, Initializer initializer) {
		this(name, dimensions, Optional.ofNullable(initializer));
	}

	public VariableDeclarator(Name name, List<Dimension> dimensions, Optional<? extends Initializer> initializer) {
		setName(name);
		setDimensions(dimensions);
		setInitializer(initializer);
	}
	
	@Override
	public VariableDeclarator clone() {
		return new VariableDeclarator(getName(), clone(getDimensions()), clone(getInitializer()));
	}

	@Override
	public String toCode() {
		var dimensionStr = dimensionString();
		return getName() + (dimensionStr.startsWith("@")? " " : "") + dimensionStr
				+ initializer.map(initializer -> " = " + initializer.toCode()).orElse("");
	}

	@Override
	public void setDimensions(@NonNull List<Dimension> dimensions) {
		this.dimensions = newList(dimensions);
	}

	@Override
	public final void setDimensions(Dimension... dimensions) {
		setDimensions(List.of(dimensions));
	}

	public void setInitializer(@NonNull Optional<? extends Initializer> initializer) {
		this.initializer = initializer;
	}

	public final void setInitializer(Initializer initializer) {
		setInitializer(Optional.ofNullable(initializer));
	}

	public final void setInitializer() {
		setInitializer(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitVariableDeclarator(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getDimensions());
			getInitializer().ifPresent(init -> init.<Initializer>accept(visitor, this, this::setInitializer));
		}
	}

}
