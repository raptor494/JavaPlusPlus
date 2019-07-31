package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;

import lombok.NonNull;

public interface TypeParameterHolder extends INode {
	
	@Override
	TypeParameterHolder clone();

	List<TypeParameter> getTypeParameters();
	
	void setTypeParameters(@NonNull List<TypeParameter> typeParameters);
	
	void setTypeParameters(TypeParameter... typeParameters);
	
	default String typeParameterString() {
		var typeParameters = getTypeParameters();
		if(typeParameters.isEmpty()) {
			return "";
		} else {
			return "<" + joinNodes(", ", typeParameters) + ">";
		}
	}
	
}
