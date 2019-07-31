package jtree.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public final class QualifiedName extends Node implements Iterable<Name>, CharSequence {
	private final Name[] names;
	private final String stringValue;
	@Getter(lazy = true) @Accessors(fluent = true)
	private final int hashCode = Objects.hash(names, stringValue);

	/**
	 * @param string the qualified name
	 * @throws IllegalArgumentException If the argument is not a valid qualified
	 *                                  name.
	 */
	public QualifiedName(@NonNull String string) {
		String[] strs = string.split("\\.");
		this.names = Arrays.stream(strs).map(Name::new).toArray(Name[]::new);
		this.stringValue = string;
	}

	/**
	 * @param strings the names
	 * @throws IllegalArgumentException If the argument is not a valid qualified
	 *                                  name.
	 */
	public QualifiedName(@NonNull String... strings) {
		this.names = Arrays.stream(strings).map(Name::new).toArray(Name[]::new);
		this.stringValue = String.join(".", strings);
	}

	public QualifiedName(@NonNull Name... names) {
		if(names.length == 0) {
			throw new IllegalArgumentException("No names given");
		}
		for(var name : names) {
			Objects.requireNonNull(name);
		}
		this.names = names;
		this.stringValue = String.join(".", names);
	}
	
	public QualifiedName(@NonNull CharSequence... stuffs) {
		if(stuffs.length == 0) {
			throw new IllegalArgumentException("No names given");
		}
		var names = new ArrayList<Name>(stuffs.length);
		for(var stuff : stuffs) {
			Objects.requireNonNull(stuff);
			if(stuff instanceof Name) {
				names.add((Name)stuff);
			} else {
				String str = stuff.toString();
				if(str.indexOf('.') == -1) {
					names.add(new Name(str));
				} else {
					for(String name : str.split("\\.")) {
						names.add(new Name(name));
					}
				}
			}
		}
		this.names = names.toArray(new Name[names.size()]);
		this.stringValue = String.join(".", names);
	}
	
	public QualifiedName(Collection<Name> names) {
		this(names.toArray(new Name[names.size()]));
	}
	
	@Override
	public QualifiedName clone() {
		return this;
	}
	
	public static final Collector<? extends CharSequence, ArrayList<Name>, QualifiedName> TO_QUALIFIED_NAME = 
			Collector.of(ArrayList<Name>::new, 
                         QualifiedName::addToList,
                         (list1, list2) -> {
                    	     list1.addAll(list2);
                    	     return list1;
                         },
                         list -> new QualifiedName(list.toArray(new Name[list.size()])));
	
	private static ArrayList<Name> addToList(ArrayList<Name> list, Object obj) {
		if(obj instanceof Name) {
			list.add((Name)obj);
		} else if(obj instanceof QualifiedName) {
			for(var name : (QualifiedName)obj) {
				list.add(name);
			}
		} else if(obj instanceof Name[]) {
			for(var name : (Name[])obj) {
				list.add(name);
			}
		} else if(obj instanceof QualifiedName[]) {
			for(var qualname : (QualifiedName[])obj) {
				for(var name : qualname) {
					list.add(name);
				}
			}
		} else if(obj instanceof CharSequence) {
			var str = obj.toString();
			if(str.indexOf('.') == -1) {
				list.add(new Name(str));
			} else {
				for(var substr : str.split("\\.")) {
					list.add(new Name(substr));
				}
			}
		} else if(obj instanceof CharSequence[]) {
			for(var cseq : (CharSequence[])obj) {
				addToList(list, cseq);
			}
		} else if(obj instanceof Iterable) {
			for(var elem : (Iterable<?>)obj) {
				addToList(list, elem);
			}
		} else if(obj instanceof Object[]) {
			for(var elem : (Object[])obj) {
				addToList(list, elem);
			}
		} else {
			throw new IllegalArgumentException("Don't know how to convert " + obj.getClass().getSimpleName() + " to Name");
		}
		return list;
	}	
	
	@SuppressWarnings("unchecked")
	public static <T extends CharSequence> Collector<? super T, ArrayList<Name>, QualifiedName> collector() {
		return (Collector<? super T,ArrayList<Name>,QualifiedName>)TO_QUALIFIED_NAME;
	}
	
	public QualifiedName append(Name other) {
		return new QualifiedName(this, other);
	}
	
	public QualifiedName append(Name... names) {
		return Stream.concat(Arrays.stream(this.names), Arrays.stream(names)).collect(QualifiedName.collector());
	}
	
	public QualifiedName append(List<? extends CharSequence> names) {
		return Stream.concat(Arrays.stream(this.names), names.stream()).collect(QualifiedName.collector());
	}
	
	public QualifiedName append(QualifiedName other) {
		return Stream.concat(Stream.of(this), other.stream()).collect(QualifiedName.collector());	
	}
	
	public QualifiedName append(QualifiedName... names) {
		return Stream.concat(Stream.of(this), Arrays.stream(names)).collect(QualifiedName.collector());
	}
	
	public QualifiedName append(CharSequence... names) {
		return Stream.concat(Stream.of(this), Arrays.stream(names)).collect(QualifiedName.collector());
	}
	
	public Stream<Name> stream() {
		return Arrays.stream(names);
	}
	
	public Stream<Name> parallelStream() {
		return stream().parallel();
	}

	public boolean contains(String name) {
		return stringValue.contains(name);
	}

	public boolean contains(Name name) {
		return Arrays.stream(names).anyMatch(name::equals);
	}

	public int nameCount() {
		return names.length;
	}

	public int charIndexOf(String substr) {
		return stringValue.indexOf(substr);
	}

	public int nameIndexOf(@NonNull Name subname) {
		for(int i = 0; i < names.length; i++) {
			if(names[i].equals(subname)) {
				return i;
			}
		}
		return -1;
	}

	public Name getName(int index) {
		return names[index];
	}
	
	public Name firstName() {
		return names[0];
	}
	
	public Name lastName() {
		return names[names.length-1];
	}

	public QualifiedName subName(int start) {
		Objects.checkIndex(start, names.length);
		Name[] newnames = new Name[names.length - start];
		System.arraycopy(names, start, newnames, 0, newnames.length);
		return new QualifiedName(newnames);
	}

	public QualifiedName subName(int start, int end) {
		Objects.checkFromToIndex(start, end, names.length);
		if(start == end) {
			throw new IndexOutOfBoundsException("start == end");
		}
		Name[] newnames = new Name[end - start];
		System.arraycopy(names, start, newnames, 0, newnames.length);
		return new QualifiedName(newnames);
	}

	public boolean isQualified() {
		return names.length > 1;
	}

	/**
	 * @throws UnsupportedOperationException If this object is not
	 *                                       {@linkplain #isQualified() qualified}.
	 */
	public Name toName() {
		if(isQualified()) {
			return names[0];
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<Name> iterator() {
		return Arrays.stream(names).iterator();
	}

	public Name[] toArray() {
		return names.clone();
	}

	@Override
	public String toString() {
		return stringValue;
	}

	@Override
	public String toCode() {
		return stringValue;
	}

	@Override
	public int length() {
		return stringValue.length();
	}

	@Override
	public char charAt(int index) {
		return stringValue.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return stringValue.substring(start, end);
	}
	
	public boolean startsWith(String str) {
		return stringValue.startsWith(str);
	}
	
	public boolean endsWith(String str) {
		return stringValue.endsWith(str);
	}
	
	public boolean startsWith(Name name) {
		return names[0].equals(name);
	}
	
	public boolean endsWith(Name name) {
		return names[names.length-1].equals(name);
	}
	
	public boolean startsWith(QualifiedName name) {
		if(nameCount() < name.nameCount()) {
			return false;
		}
		for(int i = 0; i < name.nameCount(); i++) {
			if(!getName(i).equals(name.getName(i))) {
				return false;
			}
		}
		return true;
	}
	
	public boolean endsWith(QualifiedName name) {
		if(nameCount() < name.nameCount()) {
			return false;
		}
		for(int i = name.nameCount(), j = nameCount(); i >= 0; i--, j--) {
			if(!getName(j).equals(name.getName(i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if(obj instanceof QualifiedName) {
			var name = (QualifiedName)obj;
			return Arrays.equals(names, name.names) && stringValue.equals(name.stringValue); 
		} else {
			return obj instanceof CharSequence && stringValue.contentEquals((CharSequence)obj);
		}
	}
	
	public boolean equals(CharSequence cseq) {
		return stringValue.contentEquals(cseq);
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		visitor.visitQualifiedName(this, parent, cast(replacer));
	}
	

}