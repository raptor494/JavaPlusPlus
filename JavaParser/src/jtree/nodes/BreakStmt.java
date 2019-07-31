package jtree.nodes;

import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class BreakStmt extends Node implements Statement {
	protected @NonNull Optional<Name> label;
	
	public BreakStmt() {
		this(Optional.empty());
	}
	
	public BreakStmt(Name label) {
		this(Optional.ofNullable(label));
	}
	
	public BreakStmt(Optional<Name> label) {
		setLabel(label);
	}
	
	@Override
	public BreakStmt clone() {
		return new BreakStmt(clone(getLabel()));
	}
	
	@Override
	public String toCode() {
		return "break" + getLabel().map(label -> " " + label.toCode()).orElse("") + ";";
	}
	
	public void setLabel(@NonNull Optional<Name> label) {
		this.label = label;
	}
	
	public final void setLabel(Name label) {
		setLabel(Optional.ofNullable(label));
	}
	
	public final void setLabel() {
		setLabel(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitBreakStmt(this, parent, cast(replacer))) {
			getLabel().ifPresent(label -> label.<Name>accept(visitor, this, this::setLabel));
		}
	}
	
}
