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
public class Block extends Node implements Statement {
	protected @NonNull List<Statement> statements;
	
	public Block(List<Statement> statements) {
		setStatements(statements);
	}
	
	public Block() {
		this(emptyList());
	}
	
	public Block(Statement... statements) {
		this(List.of(statements));
	}
	
	@Override
	public Block clone() {
		return new Block(clone(getStatements()));
	}
	
	@Override
	public String toCode() {
		return "{\n" + join("", getStatements(), statement -> statement.toCode().indent(4)) + "}";
	}
	
	public void setStatements(@NonNull List<? extends Statement> statements) {
		this.statements = newList(statements);
	}
	
	public final void setStatements(Statement... statements) {
		setStatements(List.of(statements));
	}
	
	public boolean isEmpty() {
		return getStatements().isEmpty();
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitBlock(this, parent, cast(replacer))) {
			visitList(visitor, getStatements());
		}
	}
	
}
