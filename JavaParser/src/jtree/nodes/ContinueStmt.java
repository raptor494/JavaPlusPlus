package jtree.nodes;

import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ContinueStmt extends Node implements Statement {
	protected @NonNull Optional<Name> label;
	
	public ContinueStmt() {
		this(Optional.empty());
	}
	
	public ContinueStmt(Name label) {
		this(Optional.ofNullable(label));
	}
	
	public ContinueStmt(Optional<Name> label) {
		setLabel(label);
	}
	
	@Override
	public ContinueStmt clone() {
		return new ContinueStmt(clone(getLabel()));
	}
	
	@Override
	public String toCode() {
		return "continue" + getLabel().map(label -> " " + label.toCode()).orElse("") + ";";
	}
	
	public void setLabel(@NonNull Optional<Name> label) {
		this.label = label;
	}
	
	public final void setLabel(Name label) {
		setLabel(Optional.of(label));
	}
	
	public final void setLabel() {
		setLabel(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitContinueStmt(this, parent, cast(replacer))) {
			getLabel().ifPresent(label -> label.<Name>accept(visitor, this, this::setLabel));
		}
	}
	
}
