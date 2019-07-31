package jtree.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import jtree.nodes.INode;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {
	
	public <T> Iterable<T> iter(Iterator<T> iter) {
		return () -> iter;
	}
	
	public <T> List<T> emptyList() {
		return Collections.emptyList();
	}
	
	public <T> ArrayList<T> newList() {
		return new ArrayList<>();
	}

	@SafeVarargs
	public <T> ArrayList<T> newList(@NonNull T... elements) {
		var result = new ArrayList<T>(elements.length);
		for(T t : elements) {
			result.add(Objects.requireNonNull(t));
		}
		return result;
	}
	
	public <T> ArrayList<T> newList(List<? extends T> list) {
		var result = new ArrayList<T>(list.size());
		for(T t : list) {
			result.add(Objects.requireNonNull(t));
		}
		return result;
	}
	
	public String joinNodes(String sep, INode... nodes) {
		return joinNodes(sep, Arrays.asList(nodes));
	}
	
	public String joinNodes(String sep, Iterable<? extends INode> nodes) {
		return join(sep, nodes, INode::toCode);
	}
	
	public String join(String sep, Object... objects) {
		return join(sep, Arrays.asList(objects));
	}
	
	public String join(String sep, Iterable<?> objects) {
		return join(sep, objects, Object::toString);
	}
	
	public <T> String join(String sep, T[] objects, Function<? super T, ?> mapper) {
		return join(sep, Arrays.asList(objects), mapper);
	}
	
	public <T> String join(String sep, Iterable<? extends T> objects, Function<? super T, ?> mapper) {
		var sb = new StringBuilder();
		boolean first = true;
		for(var obj : objects) {
			if(first) {
				first = false;
			} else {
				sb.append(sep);
			}
			sb.append(mapper.apply(obj));
		}
		return sb.toString();
	}
	
}
