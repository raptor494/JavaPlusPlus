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
public class TryStmt extends Node implements CompoundStmt {
	protected @NonNull List<? extends ResourceSpecifier> resources;
	protected @NonNull Block body;
	private @NonNull List<Catch> catches;
	private @NonNull Optional<Block> finallyBody;
	
	public TryStmt(List<? extends ResourceSpecifier> resources, Block body) {
		this(resources, body, emptyList(), Optional.empty());
	}
	
	public TryStmt(Block body, List<Catch> catches) {
		this(body, catches, Optional.empty());
	}
	
	public TryStmt(Block body, List<Catch> catches, Optional<Block> finallyBody) {
		this(emptyList(), body, catches, finallyBody);
	}
	
	public TryStmt(Block body, List<Catch> catches, Block finallyBody) {
		this(body, catches, Optional.ofNullable(finallyBody));
	}
	
	public TryStmt(List<? extends ResourceSpecifier> resources, Block body, List<Catch> catches, Block finallyBody) {
		this(resources, body, catches, Optional.ofNullable(finallyBody));
	}
	
	public TryStmt(List<? extends ResourceSpecifier> resources, Block body, List<Catch> catches, Optional<Block> finallyBody) {
		setResources(resources);
		setBody(body);
		setCatches(catches);
		setFinallyBody(finallyBody);
	}
	
	@Override
	public TryStmt clone() {
		return new TryStmt(clone(getResources()), getBody().clone(), clone(getCatches()), clone(getFinallyBody()));
	}
	
	@Override
	public String toCode() {
		var resources = getResources();
		String result = "try";
		if(!resources.isEmpty()) {
			String joined = joinNodes(" ", resources);
			joined = joined.substring(0, joined.length() - 1); // removes trailing ';'
			result += "(" + joined + ")";
		}
		return (result + " " + getBody().toCode() + " " + joinNodes(" ", getCatches())).stripTrailing()
			+ getFinallyBody().map(finallyBody -> " finally " + finallyBody.toCode()).orElse("");
	}
	
	public void setResources(@NonNull List<? extends ResourceSpecifier> resources) {
		this.resources = newList(resources);
	}
	
	public final void setResources(ResourceSpecifier... resources) {
		setResources(List.of(resources));
	}
	
	public void setCatches(@NonNull List<Catch> catches) {
		this.catches = newList(catches);
	}
	
	public final void setCatches(Catch... catches) {
		setCatches(List.of(catches));
	}
	
	public void setFinallyBody(@NonNull Optional<Block> finallyBody) {
		this.finallyBody = finallyBody;
	}
	
	public final void setFinallyBody(Block finallyBody) {
		setFinallyBody(Optional.ofNullable(finallyBody));
	}
	
	public final void setFinallyBody() {
		setFinallyBody(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitTryStmt(this, parent, cast(replacer))) {
			visitList(visitor, getResources());
			getBody().accept(visitor, this, this::setBody);
			visitList(visitor, getCatches());
			getFinallyBody().ifPresent(body -> body.<Block>accept(visitor, this, this::setFinallyBody));
		}
	}
	
}
