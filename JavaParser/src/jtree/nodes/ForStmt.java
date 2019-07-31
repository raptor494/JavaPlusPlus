package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import jtree.util.Either;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ForStmt extends Node implements CompoundStmt {
	protected @NonNull Optional<Either<VariableDecl,ExpressionStmt>> initializer;
	protected @NonNull Optional<? extends Expression> condition;
	private @NonNull List<? extends Expression> updates;
	private @NonNull Statement body;

	public ForStmt(VariableDecl initializer, Optional<? extends Expression> condition,
				   List<? extends Expression> updates, Statement body) {
		this(Either.first(initializer), condition, updates, body);
	}
	
	public ForStmt(ExpressionStmt initializer, Optional<? extends Expression> condition,
	               List<? extends Expression> updates, Statement body) {
		this(Either.second(initializer), condition, updates, body);
	}
	
	public ForStmt(Either<VariableDecl,ExpressionStmt> initializer, Optional<? extends Expression> condition,
	               List<? extends Expression> updates, Statement body) {
		this(Optional.ofNullable(initializer), condition, updates, body);
	}
	
	public ForStmt(VariableDecl initializer, Expression condition,
				   List<? extends Expression> updates, Statement body) {
		this(Either.first(initializer), condition, updates, body);
	}
	
	public ForStmt(ExpressionStmt initializer, Expression condition,
	               List<? extends Expression> updates, Statement body) {
		this(Either.second(initializer), condition, updates, body);
	}
	
	public ForStmt(Either<VariableDecl,ExpressionStmt> initializer, Expression condition,
	               List<? extends Expression> updates, Statement body) {
		this(Optional.ofNullable(initializer), condition, updates, body);
	}

	public ForStmt(Optional<Either<VariableDecl,ExpressionStmt>> initializer, Expression condition,
				   List<? extends Expression> updates, Statement body) {
		this(initializer, Optional.ofNullable(condition), updates, body);
	}

	public ForStmt(Optional<Either<VariableDecl,ExpressionStmt>> initializer, Optional<? extends Expression> condition,
				   List<? extends Expression> updates, Statement body) {
		setInitializer(initializer);
		setCondition(condition);
		setUpdates(updates);
		setBody(body);
	}
	
	@Override
	public ForStmt clone() {
		return new ForStmt(clone(getInitializer()), clone(getCondition()), clone(getUpdates()), getBody().clone());
	}

	@Override
	public String toCode() {
		var updates = getUpdates();
		return "for(" + getInitializer().map(either -> ((Node)either.getValue()).toCode()).orElse(";")
				+ getCondition().map(condition -> " " + condition.toCode()).orElse("") + ";"
				+ (updates.isEmpty()? "" : " " + joinNodes(", ", updates))
				+ ")" + bodyString(getBody());
	}

	public void setUpdates(@NonNull List<? extends Expression> updates) {
		this.updates = newList(updates);
	}
	
	public final void setUpdates(Expression... updates) {
		setUpdates(List.of(updates));
	}

	public void setInitializer(@NonNull Optional<Either<VariableDecl,ExpressionStmt>> initializer) {
		initializer.ifPresent(either -> Objects.requireNonNull(either.getValue()));
		this.initializer = initializer;
	}
	
	public final void setInitializer(Either<VariableDecl,ExpressionStmt> initializer) {
		setInitializer(Optional.ofNullable(initializer));
	}
	
	public final void setInitializer(VariableDecl initializer) {
		setInitializer(Either.first(initializer));
	}
	
	public final void setInitializer(ExpressionStmt initializer) {
		setInitializer(Either.second(initializer));
	}
	
	public final void setInitializer() {
		setInitializer(Optional.empty());
	}
	
	public void setCondition(@NonNull Optional<? extends Expression> condition) {
		this.condition = condition;
	}
	
	public final void setCondition(Expression condition) {
		setCondition(Optional.ofNullable(condition));
	}
	
	public final void setCondition() {
		setCondition(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitForStmt(this, parent, cast(replacer))) {
			getInitializer().ifPresent(either -> either.accept(vardecl -> vardecl.<VariableDecl>accept(visitor, this, this::setInitializer), exprstmt -> exprstmt.<ExpressionStmt>accept(visitor, this, this::setInitializer)));
			getCondition().ifPresent(condition -> condition.<Expression>accept(visitor, this, this::setCondition));
			visitList(visitor, getUpdates());
			getBody().accept(visitor, this, this::setBody);
		}
	}

}
