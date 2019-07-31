package jtree.nodes;

import static jtree.util.Utils.*;
import static lombok.AccessLevel.*;

import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import jtree.util.Either;
import lombok.Getter;
import lombok.SneakyThrows;

public abstract class Node implements INode {
	
	@Override
	public abstract String toCode();
	
	@Override
	public abstract Node clone();
	
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> clone(Optional<T> optional) {
		return (Optional<T>)optional.map(Node::clone);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> clone(List<T> list) {
		return (List<T>)list.stream().map(Node::clone).collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	public static <F,S> Either<F,S> clone(Either<F,S> either) {
		return (Either<F,S>)either.map(Node::clone, Node::clone);
	}
	
	private static <T> Object clone(T t) {
		if(t instanceof Optional) {
			return clone((Optional<?>)t);
		} else if(t instanceof List) {
			return clone((List<?>)t);
		} else if(t instanceof INode) {
			return ((INode)t).clone();
		} else {
			return t;
		}
	}
	
	@SneakyThrows
	private static Class<?> resolve(Type type) {
		if(type instanceof Class) {
			return (Class<?>)type;
		} else if(type instanceof GenericArrayType) {
			return Class.forName("[" + resolve(((GenericArrayType)type).getGenericComponentType()));
		} else if(type instanceof ParameterizedType) {
			return resolve(((ParameterizedType)type).getRawType());
		} else if(type instanceof WildcardType) {
			return resolve(((WildcardType)type).getUpperBounds()[0]);
		} else if(type instanceof TypeVariable) {
			return resolve(((TypeVariable<?>)type).getBounds()[0]);
		} else {
			throw new IllegalArgumentException("Don't know how to resolve " + type.getClass().getName() + " " + type.getTypeName());
		}
	}
	
	private static Type resolveType(Type type) {
		if(type instanceof TypeVariable) {
			return resolveType(((TypeVariable<?>)type).getBounds()[0]);
		} else if(type instanceof WildcardType) {
			return resolveType(((WildcardType)type).getUpperBounds()[0]);
		} else {
			return type;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Function<?, String> selectToStringFuncFromType(Type type) {
		if(type == null) {
			return obj -> obj == null? "null" : ((Function<Object,String>)selectToStringFuncFromType(obj.getClass())).apply(obj);
		}
		type = resolveType(type);
		Class<?> normalType = resolve(type);
		if(List.class.isAssignableFrom(normalType)) {
			Type componentType;
			if(type instanceof ParameterizedType) {
				componentType = ((ParameterizedType)type).getActualTypeArguments()[0];
			} else {
				componentType = null;
			}
			var componentToStringFunc = (Function<Object,String>)selectToStringFuncFromType(componentType);
			return (List<?> list) -> {
				var sb = new StringBuilder("[");
				boolean first = true;
				for(var elem : list) {
					if(first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(componentToStringFunc.apply(elem));
				}
				sb.append(']');
				return sb.toString();
			};
		} else if(Set.class.isAssignableFrom(normalType)) {
			Type componentType;
			if(type instanceof ParameterizedType) {
				componentType = ((ParameterizedType)type).getActualTypeArguments()[0];
			} else {
				componentType = null;
			}
			var componentToStringFunc = (Function<Object,String>)selectToStringFuncFromType(componentType);
			return (Set<?> list) -> {
				var sb = new StringBuilder("{");
				boolean first = true;
				for(var elem : list) {
					if(first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(componentToStringFunc.apply(elem));
				}
				sb.append('}');
				return sb.toString();
			};
		} else if(normalType.isArray()) {
			Type componentType;
			if(type instanceof GenericArrayType) {
				componentType = ((GenericArrayType)type).getGenericComponentType();
			} else {
				componentType = normalType.getComponentType();
			}
			var componentToStringFunc = (Function<Object,String>)selectToStringFuncFromType(componentType);
			return (Object array) -> {
				var sb = new StringBuilder("[");
				int length = Array.getLength(array);
				for(int i = 0; i < length; i++) {
					if(i != 0) {
						sb.append(", ");
					}
					sb.append(componentToStringFunc.apply(Array.get(array, i)));
				}
				sb.append(']');
				return sb.toString();
			};
		} else if(Map.class.isAssignableFrom(normalType)) {
			Type keyType, valueType;
			if(type instanceof ParameterizedType) {
				var typeArgs = ((ParameterizedType)type).getActualTypeArguments();
				keyType = typeArgs[0];
				valueType = typeArgs[1];
			} else {
				keyType = Object.class;
				valueType = null;
			}
			Function<Object,String> keyToStringFunc, valueToStringFunc;
			if(keyType == String.class) {
				keyToStringFunc = obj -> {
					String key = obj.toString();
					if(key.matches("[\\w$]+")) {
						return key;
					} else {
						return '"' + StringEscapeUtils.escapeJava(key) + '"';
					}
				};
			} else {
				keyToStringFunc = (Function<Object,String>)selectToStringFuncFromType(keyType);
			}
			valueToStringFunc = (Function<Object,String>)selectToStringFuncFromType(valueType);
			return (Map<Object,Object> map) -> {
				var sb = new StringBuilder("{");
				boolean first = true;
				for(var entry : map.entrySet()) {
					if(first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(keyToStringFunc.apply(entry.getKey())).append("=").append(valueToStringFunc.apply(entry.getValue()));
				}
				sb.append("}");
				return sb.toString();
			};
		} else if(normalType == Optional.class) {
			Type componentType;
			if(type instanceof ParameterizedType) {
				componentType = ((ParameterizedType)type).getActualTypeArguments()[0];
			} else {
				componentType = null;
			}
			var valueToStringFunc = (Function<Object,String>)selectToStringFuncFromType(componentType);
			return (Optional<?> opt) -> opt.map(value -> "Optional[" + valueToStringFunc.apply(value) + "]").orElse("Optional.empty()");
		} else if(INode.class.isAssignableFrom(normalType)) {
			return Object::toString;
		} else if(CharSequence.class.isAssignableFrom(normalType)) {
			return (CharSequence cseq) -> '"' + StringEscapeUtils.escapeJava(cseq.toString()) + '"';
		} else if(normalType == char.class || normalType == Character.class) {
			return (Character ch) -> "'" + (ch == '\''? "\\'" : StringEscapeUtils.escapeJava(ch.toString())) + "'";
		} else if(normalType == byte.class || normalType == Byte.class) {
			return (Byte b) -> "(byte)" + b;
		} else if(normalType == short.class || normalType == Short.class) {
			return (Short s) -> "(short)" + s;
		} else if(normalType == long.class || normalType == Long.class) {
			return (Long l) -> l + "L";
		} else if(normalType == float.class || normalType == Float.class) {
			return (Float f) -> f + "f";
		} else {
			return Object::toString;
		}
	}
	
	private class ToStringFieldGetter<T> {
		private final Method method;
		@Getter
		private final String name;
		private final Function<? super T, String> toStringFunc;
		
		@SuppressWarnings("unchecked")
		protected ToStringFieldGetter(Method method) {
			this.method = method;
			String name = method.getName();
			if(name.startsWith("get")) {
				this.name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
			} else {
				this.name = name;
			}
			this.toStringFunc = (Function<? super T,String>)selectToStringFuncFromType(method.getReturnType());
		}
		
		@SuppressWarnings("unchecked")
		@SneakyThrows
		public T getValue() {
			try {
				return (T)method.invoke(Node.this);
			} catch(InvocationTargetException e) {
				throw e.getCause();
			}
		}
		
		public String getValueString() {
			return toStringFunc.apply(getValue());
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof ToStringFieldGetter && ((ToStringFieldGetter<?>)obj).name.equals(name);
		}
		
		@Override
		public String toString() {
			return getName() + "=" + getValueString();
		}
	}
	
	@Getter(value = PRIVATE, lazy = true)
	private final Set<ToStringFieldGetter<?>> fieldGetters = getFields();
	
	private Set<ToStringFieldGetter<?>> getFields() {
		var fields = new HashSet<ToStringFieldGetter<?>>();
		findFields(fields, getClass());
		return fields;
	}
	
	@SuppressWarnings({ "unchecked" })
	private void findFields(HashSet<ToStringFieldGetter<?>> fields, Class<? extends Node> type) {
		fields.addAll(Arrays.stream(type.getMethods())
		              .filter(method -> !java.lang.reflect.Modifier.isStatic(method.getModifiers())
		                      			&& method.getParameterCount() == 0
		                      			&& method.getReturnType() != void.class
		                      			&& !method.getName().equals("getClass")
		                      			&& method.getName().matches("(get|is|has)[A-Z]\\w*"))
		              .map(ToStringFieldGetter::new)
		              .collect(Collectors.toList()));
		type = (Class<? extends Node>)type.getSuperclass();
		if(type != Node.class) {
			findFields(fields, type);
		}
	}
	
	@Override
	public String toString() {
		Set<ToStringFieldGetter<?>> getters = getFieldGetters();
		return getClass().getSimpleName() + "(" + join(", ", getters, ToStringFieldGetter::toString) + ")";
	}
	
	protected static Iterable<Integer> range(int end) {
		return () -> new PrimitiveIterator.OfInt() {
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < end;
			}
			
			@Override
			public int nextInt() {
				return index++;
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	protected static final <N extends INode, R extends INode> Consumer<R> cast(Consumer<N> consumer) {
		return (Consumer<R>)consumer;
	}
	
	protected final <N extends INode> void visitList(TreeVisitor visitor, List<N> list) {
		for(int i : range(list.size())) {
			list.get(i).accept(visitor, this, (N node) -> list.set(i, node));
		}
	}
	
	/*public Stream<Node> stream() {
		var methods = Arrays.stream(getClass().getMethods());
		return methods
				.filter(method -> {
					if(isStatic(method.getModifiers())) {
						return false;
					}
					
					return true;
				})
				.map(method -> {
					try {
						return (Node)method.invoke(this);
					} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
	}
	
	
	@Override
	public final Iterator<Node> iterator() {
		return this.stream().iterator();
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	protected static Stream<Node> stream(@NonNull Object... children) {
		Stream<Node> stream = Stream.empty();
		int start = 0;
		for(int i = 0; i < children.length; i++) {
			var child = children[i];
			Objects.requireNonNull(child);
			if(!(child instanceof Node)) {
				if(child instanceof List) {
					var list = (List<Node>)child;
					for(Node child2 : list) {
						Objects.requireNonNull(child2);
					}
					if(i != start) {
						if(i == start+1) {
							stream = Stream.concat(stream, Stream.of((Node)children[start]));
						} else {
							stream = Stream.concat(stream, (Stream<Node>)(Stream<?>)Arrays.stream(children, start, i));
						}
					}
					stream = Stream.concat(stream, list.stream());
					start = i+1;
				} else {
					throw new ClassCastException(child.getClass() + " cannot be cast to " + Node.class + " or " + List.class);
				}
			}
		}
		if(start != children.length) {
			if(start+1 == children.length) {
				stream = Stream.concat(stream, Stream.of((Node)children[start]));
			} else {
				stream = Stream.concat(stream, (Stream<Node>)(Stream<?>)Arrays.stream(children, start, children.length));
			}
		}
		return stream;
	}*/
	
}
