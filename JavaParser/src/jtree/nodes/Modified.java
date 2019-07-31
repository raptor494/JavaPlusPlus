package jtree.nodes;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;

public interface Modified extends INode  {

	@Override
	Modified clone();
	
	List<Modifier> getModifiers();
	
	void setModifiers(@NonNull List<Modifier> modifiers);
	
	void setModifiers(Modifier... modifiers);
	
	default String modifierString() {
		var modifiers = getModifiers();
		if(modifiers.isEmpty()) {
			return "";
		} else {
			return modifiers.stream()
							.map(Modifier::toCode)
							.collect(Collectors.joining(" ", "", " "));
		}
	}
	
	default boolean hasModifier(Modifier modifier) {
		return getModifiers().contains(modifier);
	}
	
	default boolean hasModifier(CharSequence modifier) {
		for(var mod : getModifiers()) {
			if(mod.toString().contentEquals(modifier)) {
				return true;
			}
		}
		return false;
	}
	
	default boolean hasVisibilityModifier() {
		for(var mod : getModifiers()) {
			switch(mod.toString()) {
				case "public", "private", "protected", "package":
					return true;
			}
		}
		return false;
	}
	
}
