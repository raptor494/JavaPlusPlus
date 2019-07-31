package jtree.nodes;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

public interface Dimensioned extends INode {
	
	@Override
	Dimensioned clone();

	List<Dimension> getDimensions();
	
	void setDimensions(@NonNull List<Dimension> dimensions);
	
	void setDimensions(Dimension... dimensions);
	
	default String dimensionString() {
		return getDimensions().stream()
							  .map(Dimension::toCode)
							  .collect(Collectors.joining());
	}
	
}
