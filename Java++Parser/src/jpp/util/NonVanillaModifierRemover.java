package jpp.util;

import java.util.function.Consumer;

import jtree.nodes.AbstractTreeVisitor;
import jtree.nodes.Modifier;
import jtree.nodes.Node;

public class NonVanillaModifierRemover extends AbstractTreeVisitor {

	@Override
	public boolean visitNode(Node node, Node parent, Consumer<Node> replacer) {
		return true;
	}
	
	@Override
	public boolean visitModifier(Modifier node, Node parent, Consumer<Modifier> replacer) {
		if(node.toCode().startsWith("non-") || node.equals("package")) {
			replacer.accept(null); // removes it
		}
		return true;
	}
	
}
