package jtree.parser;

import static jtree.parser.ModsAndAnnotations.Type.*;
import static jtree.util.Utils.emptyList;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jtree.nodes.Annotation;
import jtree.nodes.Modifier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class ModsAndAnnotations {
	public final @NonNull List<Modifier> mods;
	public final @NonNull List<Annotation> annos;
	public final @NonNull EnumSet<ModsAndAnnotations.Type> types;
	
	public ModsAndAnnotations() {
		this(emptyList(), emptyList(), EnumSet.allOf(ModsAndAnnotations.Type.class));
	}
	
	public ModsAndAnnotations(@NonNull List<Modifier> mods, @NonNull List<Annotation> annos) {
		this.mods = mods;
		this.annos = annos;
		this.types = EnumSet.allOf(ModsAndAnnotations.Type.class);
	 
		for(var mod : mods) {
			var validTypes = MOD_TYPES.get(mod.toCode());
			if(validTypes != null) {
				types.retainAll(validTypes);
			}
		}
	}
	
	public boolean isEmpty() {
		return mods.isEmpty() && annos.isEmpty();
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public boolean hasModifier(String modifier) {
		for(var mod : mods) {
			if(mod.equals(modifier)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasModifier(Modifier modifier) {
		return mods.contains(modifier);
	}
	
	public boolean canBeLocalVarMods() {
		return types.contains(LOCAL_VAR);
	}
	
	public boolean canBeClassMods() {
		return types.contains(CLASS);
	}
	
	public boolean canBeMethodMods() {
		return types.contains(METHOD);
	}
	
	public boolean canBeConstructorMods() {
		assert this.canBeMethodMods();
		return types.contains(CONSTRUCTOR);
	}
	
	public boolean canBeFieldMods() {
		return types.contains(FIELD);
	}
	
	public static enum Type {
		LOCAL_VAR,
		CLASS,
		METHOD,
		CONSTRUCTOR,
		FIELD;
		
		private static final EnumSet<Type> NONE = EnumSet.noneOf(Type.class); 
		
		public static EnumSet<Type> fromModifier(Modifier modifier) {
			return EnumSet.copyOf(MOD_TYPES.getOrDefault(modifier.toCode(), NONE));
		}
	}
	
	/*
	 * Modifier    |class?|method?|field?|var? |constructor?|
	 * ------------+------+-------+------+-----+------------+
	 * public*     |true  |true   |true  |false|true        |
	 * private*    |true  |true   |true  |false|true        |
	 * protected*  |true  |true   |true  |false|true        |
	 * package**   |true  |true   |true  |false|true        |
	 * static      |true  |true   |true  |false|false       |
	 * final       |true  |true   |true  |true |false       |
	 * abstract    |true  |true   |false |false|false       |
	 * strictfp    |true  |true   |false |false|false       |
	 * native      |false |true   |false |false|false       |
	 * synchronized|false |true   |false |false|false       |
	 * default     |false |true   |false |false|false       |
	 * transient   |false |false  |true  |false|false       |
	 * volatile    |false |false  |true  |false|false       |
	 * ------------+------+-------+------+-----+------------+
	 * *: modifier can NOT be prepended with 'non-'
	 * **: not a modifier in vanilla Java, also can NOT be prepended with 'non-'
	 */  
	
	private static final Map<String, EnumSet<ModsAndAnnotations.Type>> MOD_TYPES;
	
	static {
		var map = new HashMap<String, EnumSet<ModsAndAnnotations.Type>>(22);
		
		put(map, EnumSet.of(CLASS, METHOD, FIELD, CONSTRUCTOR), "public", "private", "protected", "package");
		put(map, EnumSet.of(CLASS, METHOD, FIELD), "static", "non-static");
		put(map, EnumSet.of(CLASS, METHOD, FIELD, LOCAL_VAR), "final", "non-final");
		put(map, EnumSet.of(CLASS, METHOD), "abstract", "non-abstract", "strictfp", "non-strictfp");
		put(map, EnumSet.of(METHOD), "native", "non-native", "synchronized", "non-synchronized", "default", "non-default");
		put(map, EnumSet.of(FIELD), "transient", "non-transient", "volatile", "non-volatile");
		
		MOD_TYPES = Collections.unmodifiableMap(map);
	}
	
	private static void put(Map<String, EnumSet<ModsAndAnnotations.Type>> map, EnumSet<ModsAndAnnotations.Type> set, String... keys) {
		for(String key : keys) {
			map.put(key, set);
		}
	}
}
