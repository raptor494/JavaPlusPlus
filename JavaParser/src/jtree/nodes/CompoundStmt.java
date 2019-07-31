package jtree.nodes;

public interface CompoundStmt extends Statement {
	
	@Override
	CompoundStmt clone();

	default String bodyString(Statement body) {
		if(body instanceof Block) {
			return " " + body.toCode();
		} else {
			return "\n" + body.toCode().indent(4).stripTrailing();
		}
	}
	
}
