package jtree.parser;

import lombok.ToString;
import lombok.Value;

@ToString(includeFieldNames = false)
public @Value class Position {
	int line, column;
}
