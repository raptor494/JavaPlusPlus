package jpp.parser;


import java.util.HashMap;

import jtree.nodes.Name;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Names {
	private static final HashMap<String,Name> normalNameMap = new HashMap<>();
	
	public static final Name // @formatter:off
                of = Name("of"),
                ofNullable = Name("ofNullable"),
                ofEntries = Name("ofEntries"),
                NonNull = Name("NonNull"),
                out = Name("out"),
                entry = Name("entry"),
                format = Name("format"),
                compile = Name("compile"),
                print = Name("print"),
                println = Name("println"),
                printf = Name("printf"),
                empty = Name("empty"),
                orElseThrow = Name("orElseThrow"),
                orElseGet = Name("orElseGet"),
                orElse = Name("orElse"),
                value = Name("value"),
                requireNonNull = Name("requireNonNull"),
                requireNonNullElse = Name("requireNonNullElse"),
                requireNonNullElseGet = Name("requireNonNullElseGet"),
                map = Name("map"),
                deepEquals = Name("deepEquals"),
                naturalOrder = Name("naturalOrder"),
                compare = Name("compare"),
                getKey = Name("getKey"),
                getValue = Name("getValue"),
                entrySet = Name("entrySet"),
                exit = Name("exit");

	// @formatter:on
	
	public static Name Name(String str) {
		return normalNameMap.computeIfAbsent(str, Name::new);
	}
}
