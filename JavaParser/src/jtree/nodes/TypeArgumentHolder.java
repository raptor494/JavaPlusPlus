package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;

import lombok.NonNull;

public interface TypeArgumentHolder extends INode {
	
	@Override
	TypeArgumentHolder clone();

	List<? extends TypeArgument> getTypeArguments();
	
	void setTypeArguments(@NonNull List<? extends TypeArgument> typeArguments);
	
	void setTypeArguments(TypeArgument... typeArguments);
	
	default String typeArgumentString() {
		return typeArgumentString(getTypeArguments());
	}
	
	default String typeArgumentString(List<? extends TypeArgument> typeArguments) {
		if(typeArguments.isEmpty()) {
			return "";
		} else {
			return "<" + joinNodes(", ", typeArguments) + ">";
		}
	}
	
}
