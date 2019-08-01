package jpp.parser;

import java.util.HashMap;

import jtree.nodes.QualifiedName;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QualNames {
	private static final HashMap<String,QualifiedName> qualNameMap = new HashMap<>();
	
	public static final QualifiedName // @formatter:off
        java_util_Optional = QualifiedName("java.util.Optional"),
        java_util_OptionalInt = QualifiedName("java.util.OptionalInt"),
        java_util_OptionalLong = QualifiedName("java.util.OptionalLong"),
        java_util_OptionalDouble = QualifiedName("java.util.OptionalDouble"),
        java_lang_System = QualifiedName("java.lang.System"),
        java_util_List = QualifiedName("java.util.List"),
        java_util_Set = QualifiedName("java.util.Set"),
        java_util_Map = QualifiedName("java.util.Map"),
        java_lang_String = QualifiedName("java.lang.String"),
        java_util_regex_Pattern = QualifiedName("java.util.regex.Pattern"),
        var = QualifiedName("var");
	
	// @formatter:on
	
	public static QualifiedName QualifiedName(String str) {
		return qualNameMap.computeIfAbsent(str, QualifiedName::new);
	}
}
