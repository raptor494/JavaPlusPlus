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
public class RequiresDirective extends Directive implements Modified {
	protected @NonNull List<Modifier> modifiers;
	
	public RequiresDirective(QualifiedName moduleName) {
		this(moduleName, emptyList());
	}
	
	public RequiresDirective(QualifiedName moduleName, List<Modifier> modifiers) {
		super(moduleName);
		setModifiers(modifiers);
	}
	
	@Override
	public RequiresDirective clone() {
		return new RequiresDirective(getName(), clone(getModifiers()));
	}
	
	@Override
	public String toCode() {
		return "requires " + modifierString() + getName() + ";";
	}
	
	@Override
	public void setModifiers(@NonNull List<Modifier> modifiers) {
		this.modifiers = newList(modifiers);
	}
	
	@Override
	public final void setModifiers(Modifier... modifiers) {
		setModifiers(List.of(modifiers));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitRequiresDirective(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getModifiers());
		}
	}
	
}
