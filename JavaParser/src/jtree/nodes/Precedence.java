package jtree.nodes;

public enum Precedence {
	PRIMARY,
	POST_UNARY,
	UNARY_AND_CAST,
	MULTIPLICATIVE,
	ADDITIVE,
	BIT_SHIFT,
	RELATIONAL,
	EQUALITY,
	BIT_AND,
	BIT_XOR,
	BIT_OR,
	LOGIC_AND,
	LOGIC_OR,
	TERNARY,
	ASSIGNMENT;
	
	public boolean isLessThan(Precedence other) {
		return compareTo(other) < 0;
	}
	
	public boolean isGreaterThan(Precedence other) {
		return compareTo(other) > 0;
	}
	
	public boolean isLessThanOrEqualTo(Precedence other) {
		return compareTo(other) <= 0;
	}
	
	public boolean isGreaterThanOrEqualTo(Precedence other) {
		return compareTo(other) >= 0;
	}
}
