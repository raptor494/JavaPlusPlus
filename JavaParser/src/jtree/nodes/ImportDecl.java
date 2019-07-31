package jtree.nodes;

import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ImportDecl extends Node {
	protected @NonNull QualifiedName name;
	protected boolean isStatic, wildcard;
	
	public ImportDecl(QualifiedName name) {
		this(name, false, false);
	}
	
	public ImportDecl(QualifiedName name, boolean isStatic, boolean wildcard) {
		setName(name);
		setStatic(isStatic);
		setWildcard(wildcard);
	}
	
	@Override
	public ImportDecl clone() {
		return new ImportDecl(getName(), isStatic(), isWildcard());
	}
	
	@Override
	public String toCode() {
		return "import " + (isStatic()? "static " : "") + getName() + (isWildcard()? ".*;" : ";");
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitImportDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
		}
	}
	
}
