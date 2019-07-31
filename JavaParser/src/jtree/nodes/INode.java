package jtree.nodes;

import java.util.function.Consumer;

public interface INode {
	String toCode();
	
	INode clone();
	
	/**
	 * @param visitor the TreeVisitor
	 * @param parent this Node's parent
	 * @param replacer a function to replace this node with another in its parent
	 */
	<N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer);
}
