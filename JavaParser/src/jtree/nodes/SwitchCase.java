package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.function.Consumer;

import jtree.util.Either;
import jtree.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class SwitchCase extends Node {
	protected @NonNull List<? extends Expression> labels;
	protected @NonNull Either<? extends List<? extends Statement>, ? extends Statement> body;
	
	public SwitchCase(Either<? extends List<? extends Statement>, ? extends Statement> body) {
		this(emptyList(), body);
	}
	
	public SwitchCase(@NonNull List<? extends Statement> body) {
		this(Either.first(body));
	}
	
	public SwitchCase(@NonNull Statement body) {
		this(Either.second(body));
	}
	
	public SwitchCase(List<? extends Expression> labels, @NonNull List<? extends Statement> body) {
		this(labels, Either.first(body));
	}
	
	public SwitchCase(List<? extends Expression> labels, @NonNull Statement body) {
		this(labels, Either.second(body));
	}
	
	public SwitchCase(List<? extends Expression> labels, Either<? extends List<? extends Statement>, ? extends Statement> body) {
		setLabels(labels);
		setBody(body);
	}
	
	@Override
	public SwitchCase clone() {
		return new SwitchCase(clone(getLabels()), clone(getBody()));
	}
	
	@Override
	public String toCode() {
		var labels = getLabels();
		return (labels.isEmpty()? "default" : "case " + joinNodes(", ", labels))
				+ body.unravel(stmts -> ":" + (stmts.isEmpty()
												? "" 
												: stmts.size() == 1 && stmts.get(0) instanceof Block
													? " " + stmts.get(0).toCode() 
													: "\n" + join("", stmts, stmt -> stmt.toCode().indent(4))),
				               stmt -> " -> " + stmt.toCode());
	}
	
	public void setBody(Either<? extends List<? extends Statement>, ? extends Statement> body) {
		this.body = body.map(Utils::newList, (@NonNull var stmt) -> {
			if(!(stmt instanceof Block || stmt instanceof ThrowStmt || stmt instanceof ExpressionStmt)) {
				throw new IllegalArgumentException("single-statement case bodies can only be blocks, throw statements, or expression statements.");
			}
			return stmt;
		});
	}
	
	public final void setBody(@NonNull List<? extends Statement> body) {
		setBody(Either.first(body));
	}
	
	public final void setBody(@NonNull Block body) {
		setBody(Either.second(body));
	}
	
	public final void setBody(@NonNull ThrowStmt body) {
		setBody(Either.second(body));
	}
	
	public final void setBody(@NonNull ExpressionStmt body) {
		setBody(Either.second(body));
	}
	
	public final void setBody(Expression body) {
		setBody(Either.second(new ExpressionStmt(body)));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitSwitchCase(this, parent, cast(replacer))) {
			visitList(visitor, getLabels());
			getBody().accept(stmts -> visitList(visitor, stmts), stmt -> stmt.<Statement>accept(visitor, this, newstmt -> setBody(Either.second(newstmt))));
		}
	}
	
}
