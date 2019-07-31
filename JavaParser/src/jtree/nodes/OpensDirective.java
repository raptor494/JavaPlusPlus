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
public class OpensDirective extends Directive {
	protected @NonNull List<QualifiedName> friendModules;
	
	public OpensDirective(QualifiedName packageName) {
		this(packageName, emptyList());
	}
	
	public OpensDirective(QualifiedName packageName, QualifiedName... friendModules) {
		this(packageName, List.of(friendModules));
	}
	
	public OpensDirective(QualifiedName packageName, List<QualifiedName> friendModules) {
		super(packageName);
		setFriendModules(friendModules);
	}
	
	@Override
	public OpensDirective clone() {
		return new OpensDirective(getName(), clone(getFriendModules()));
	}
	
	@Override
	public String toCode() {
		var friendModules = getFriendModules();
		return "exports " + getName() + (friendModules.isEmpty()? ";" : " to " + joinNodes(", ", friendModules) + ";");
	}
	
	public void setFriendModules(@NonNull List<QualifiedName> friendModules) {
		this.friendModules = newList(friendModules);
	}
	
	public final void setFriendModules(QualifiedName... friendModules) {
		setFriendModules(List.of(friendModules));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitOpensDirective(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getFriendModules());
		}
	}
	
}
