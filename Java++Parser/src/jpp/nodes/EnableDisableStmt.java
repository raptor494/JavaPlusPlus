package jpp.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.function.Consumer;

import jtree.nodes.INode;
import jtree.nodes.Node;
import jtree.nodes.QualifiedName;
import jtree.nodes.REPLEntry;
import jtree.nodes.TreeVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter @Setter
public class EnableDisableStmt extends Node implements REPLEntry {
	protected boolean isDisable;
	protected @NonNull List<FeatureId> features;
	
	public EnableDisableStmt(boolean isDisable, List<FeatureId> features) {
		setDisable(isDisable);
		setFeatures(features);
	}
	
	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitNode(this, parent, cast(replacer))) {
			visitList(visitor, getFeatures());
		}
	}

	@Override
	public EnableDisableStmt clone() {
		return new EnableDisableStmt(isDisable(), clone(getFeatures()));
	}

	@Override
	public String toCode() {
		var features = getFeatures();
		return (isDisable()? "disable " : "enable ") + (features.isEmpty()? "*" : joinNodes(", ", features)) + ";";
	}
	
	public boolean isEnable() {
		return !isDisable();
	}
	
	public void setFeatures(@NonNull List<FeatureId> features) {
		this.features = newList(features);
	}
	
	public final void setFeatures(FeatureId... features) {
		setFeatures(List.of(features));
	}
	
	@EqualsAndHashCode(callSuper = true)
	@Getter @Setter
	public static class FeatureId extends Node {
		protected @NonNull QualifiedName name;
		protected boolean wildcard;
		
		public FeatureId(QualifiedName name, boolean wildcard) {
			setName(name);
			setWildcard(wildcard);
		}
		
		@Override
		public FeatureId clone() {
			return new FeatureId(getName(), isWildcard());
		}
		
		@Override
		public String toCode() {
			return isWildcard()? getName().toCode() + ".*" : getName().toCode();
		}

		@Override
		public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
			if(visitor.visitNode(this, parent, cast(replacer))) {
				getName().accept(visitor, this, this::setName);
			}
		}
	}
}
