# JavaPP
Java++, adding additional syntactical sugar to vanilla Java.

This project requires that you have installed my [python Java parser](https://github.com/raptor4694/JavaParser).

## Features
I have organized each feature into several 'categories'. 
The main point about this is that this parser is modular - you can enable/disable most features on the fly with a special statement.
### Statements
#### The Import Statements
The `import` statement can now contain multiple comma-separated namespaces.
```java
import java.util.List, java.util.Map;
```
There is a new form of import statement called the `from-import` statement. This is a new statement which I stole from Python. It begins with the contextual keyword `from`. It allows you to import multiple names from a particular package, omitting the need to write that package over and over again.
Syntax:

    from <package name> import [static] <name1>[, <name2>[, ...]];

###### Example:
```python
from java.util import List, ArrayList, Set, HashMap, Map;
```
##### Feature enabling/disabling
A special form of the import statements can be used to enable/disable specific features. To do this, the package name should be `java++` and the type name(s) should be the feature identifiers (listed below the header for all supported features).
To enable a feature, use the `import` keyword. To disable a feature, replace the `import` keyword with the `unimport` contextual keyword.

###### Example 1:
```python
from java++ import statements.print;
```
###### Example 2:
```python
from java++.syntax import default_arguments, multiple_import_sections;
```
###### Example 3:
```java
unimport java++.auto_imports.*;
```
#### The Print Statement
*Feature id:* `statements.print`
*Enabled by default*
This is a simple statement which I also stole from Python (specifically Python 2). It is actually 4 separate statements, as outlined below:
1. The `print` statement
    This statement delegates to `System.out.print()`.
    Syntax:

        print [<expression>[, <expression>[, ...]]];

    Note: `print;` by itself literally does nothing.
    If there are multiple expressions, the statement gets 
2. The `println` statement
    This statement delegates to `System.out.println()`.
    Syntax:

        println [<expression>[, <expression>[, ...]]];

3. The `printf` statement
    This statement delegates to `System.out.printf()`.
    Syntax:

        printf <format string>[, <argument 1>[, <argument 2>[, ...]]];

4. The `printfln` statement
    This statement works like the `printf` statement, except
    it also appends `"%n"` to the end of the format string.
    Syntax is the same as the `printf` statement.

### Trailing Commas
#### Trailing Argument Commas
*Feature id:* `trailing_commas.argument`
*Enabled by default.*
This feature simply allows you to add a trailing comma to a function call's argument list or a function definition's parameter list.
###### Example 1:
```java
public static int sum(int arg1,
                      int arg2,
                      int arg3,
                      ) {
    return arg1 + arg2 + arg3;
}
```
###### Example 2:
```java
int x = sum(1156112,
            183103,
            -513581,
            );
```
#### Other Trailing Commas
*Feature id:* `trailing_commas.other`
*Disabled by default.*
This feature allows you to add a trailing comma anywhere else that comma-separated lists are used.
###### Example 1:
```java
public static final int FIELD_001 = 1 << 0,
                        FIELD_002 = 1 << 1,
                        FIELD_003 = 1 << 2,
                        FIELD_004 = 1 << 4,
                        ;
```
###### Example 2:
```java
public class Example implements Interface1,
                                Interface2,
                                Interface3,
                                Interface4,
                                {
    // stuff goes here
}
```
Full list of where trailing commas are supported:
- Import declaration namespace lists
- Field/local variable declarations
- `print` statement argument lists
- Normal `for` loop update lists
- `case` labels
- Type arguments/parameters
- `implements` type lists for classes/enums
- `extends` type lists for interfaces
- `throws` exception lists

### Auto Imports
#### Type Imports
*Feature id:* `auto_imports.types`
*Disabled by default.*
This feature automatically adds a whole bunch of imports at the beginning of the file.
A full list of types imported by this feature can be found at the bottom of the page.

#### Static Imports
*Feature id:* `auto_imports.statics`
*Disabled by default.*
This feature automatically adds a whole bunch of static imports at the beginning of the file.
A full list of members imported by this feature can be found at the bottom of the page.
### Expressions
#### The Class Creator (new) Expression
*Feature id:* `expressions.class_creator`
*Enabled by default.*
This feature changes two things about the `new` expression. Firstly, it makes the argument list optional.
```java
var sb = new StringBuilder;
```
Secondly, if there is no argument list given, you can follow the type name with a bracket-enclosed series of elements (for Collections) or key-value pairs (for Maps).
```java
List<String> list = new ArrayList<> {"a", "b", "c", "d"};
Map<String,Integer> map = new HashMap<> {"key1": 1, "key2": 2, "key3": 4};
```
This works by actually calling the type's constructor with a single argument created from the elements using either `List.of()` or `Map.of()` as appropriate.
So, the expression `new ArrayList<>{1,2,3}` gets turned into `new ArrayList<>(List.of(1,2,3))`.

#### Variable Declaration Expression
*Feature id:* `expressions.vardecl`
*Enabled by default.*
This feature allows you to put a variable declaration inside a parenthesized expression, like so:
```java
foo((int x = 5), x*2);
```

#### Null-safe Expression
*Feature id:* `expressions.elvisoperator`
*Enabled by default.*
This feature adds the 'Elvis' operator from Groovy: `?:`. It has the same precedence as the conditional
operator. It returns `null` if its left argument is `null`, otherwise it returns its right argument.

### Equality Expression
*Feature id:* `expressions.equalityoperator`
*Disabled by default.*
This feature turns the == operator into a call to `Objects.deepEquals()` and adds the `is`/`is!` operators to test for identity.
The == is only turned into the call if neither of its arguments are number, class, or null literals.
Note that `is!` is only the `!=` operator if there is no space between `is` and `!`.

### Literals
#### Collection Literals
*Feature id:* `literals.collections`
*Enabled by default.*
This feature adds 3 collection literals.
* First and most importantly, the sorely-needed List literal:
    ```java
    List<String> strs = ["a", "b", "c"];
    ```
    This literal delegates to the `List.of()` method.
* Second, the Map literal:
    ```java
    Map<String, Integer> map = {"key1": 1, "key2": 2, "key3": 4};
    ```
    This literal delegates to the `Map.of()` method if there are no more than 10 key/value pairs, otherwise it delegates to the `Map.ofEntries()` method, with each key/value pair being put inside a call to `Map.entry()`.
* Last, but not least, the Set literal:
    ```java
    Set<String> strs = {"a", "b", "c"};
    ```
    This literal delegates to the `Set.of()` method. Note that the expression `{}` is actually an empty Map literal, not a Set literal, as Maps are used more than Sets (in my experience).

One final note: For variable initializers of the form `Type varname = {...}`, if `Type` is an array type, or there are any array brackets appearing after `varname`, then the `{...}` expression will become an array initializer. Otherwise, it will become either a Map or Set literal.
So, the following two statements work as expected:
```java
int[] ints1 = {1,2,3,4};
List<Integer> ints2 = {1,2,3,4};
```
#### Optional Literals
*Feature id:* literals.optional
*Enabled by default.*
This feature adds literals for `Optional`, `OptionalInt`, `OptionalDouble`, and `OptionalLong`.
The basic syntax to wrap an expression in an Optional is this:

    expression?

This expression delegates to the `Optional.ofNullable()` method. The `'?'` operator is only interpreted as an Optional literal if the token immediately after it is either `')'`, `']'`, `'}'`, `','`, or `';'`. Otherwise, the parser will assume you want to do a conditional expression. The `'?' `operator has the lowest precedence of any operator, so for example `2 + 3 & 4 || x?` gets turned into `Optional.ofNullable(2 + 3 & 4 || x)`.
The basic syntax to create an empty Optional is this:

    ?

The `'?'` is a primary expression and is basically shorthand for `Optional.empty()`.

To do a literal for one of the primitive-typed Optionals, follow the `'?'` operator with either `<int>`, `<double>`, or `<long>`. Any other angle-bracket enclosed type following the `'?'` will become the type argument to the `Optional.ofNullable()` method.

It is considered standard good practice to wrap an optional literal in parenthesis, like in the examples below.
###### Example 1:
This:
```java
return (str?);
```
becomes this:
```java
return (Optional.ofNullable(str));
```
###### Example 2:
This:
```java
return (x?<int>);
```
becomes this:
```java
return (OptionalInt.of(x));
```
and gets essentially the same result as this:
```java
return ((int)x?);
```
###### Example 3:
This:
```java
(str?<@NonNull String>);
```
becomes this:
```java
(Optional.<@NonNull String>of(str);)
```
if the type parameter is annotated with any annotation named `NonNull`.
###### Example 4:
This:
```java
(?)
```
becomes this:
```java
(Optional.empty())
```
###### Example 5:
This:
```java
(?<String>)
```
becomes this:
```java
(Optional.<String>empty())
```
###### Example 6:
This:
```java
(?<int>)
```
becomes this:
```java
(OptionalInt.empty())
```

##### Optional force-unwrapping
If the Optional literals feature is enabled, then a new suffix operator is added: `!`.
This will call the `orElseThrow()` method on its argument.
###### Example:
This:
```java
var opt = "test"?;
println opt!;
```
becomes this:
```java
var opt = Optional.ofNullable("test");
java.lang.System.out.println(opt.orElseThrow());
```

### Byte-Array Literals
Prefixing a string with either 'b' or 'B' will make it a byte-array literal.
###### Example:
This:
```python
b"abc"
```
becomes this:
```java
new byte[] {97, 98, 99}
```
### String Literals
I've stolen Python's multi-line and raw strings.
```python
""" A multi-
line string """

R"A raw string, \ has no power here!"
```
### Regex Literals
I've stolen JavaScript's regex literals, but minus the flags suffix.
###### Example:
This:
```javascript
/(abc)?d*ef{1,2}/
```
becomes this:
```java
java.util.regex.Pattern.compile("(abc)?d*ef{1,2}")
```

## Syntax
Miscellaneous syntax additions.
### Argument Annotations
*Feature id:* `syntax.argument_annotations`
*Enabled by default.*
This feature simply allows you to add a name to a function call argument. The name doesn't have to match the declared parameter's name, it can be anything.
###### Example:
```java
foo(5, arg2: false, "test", arg4: null)
```
### Multiple Import Sections
*Feature id:* `syntax.multiple_import_sections`
*Enabled by default.*
This feature simply allows you to add more imports after any top-level type declarations.
### Default Arguments
*Feature id:* `syntax.default_arguments`
*Enabled by default.*
This feature allows you to add default values to parameters.
All parameters after the first parameter with a default argument must have a default argument, or be the variadic parameter.
This works by actually creating a new function with the same modifiers, annotations, and name, but with less parameters, which just calls the main function.
###### Example:
This:
```java
void foo(int x, double y = 0.0, float z = 0.5f) { ... }
```
becomes this:
```java
void foo(int x, double y, float z) { ... }

void foo(int x) {
    foo(x, 0.0);
}

void foo(int x, double y) {
    foo(x, y, 0.5f);
}
```

## Auto Imported Types List
##### From package java.util:
- `List`
- `ArrayList`
- `Map`
- `HashMap`
- `EnumMap`
- `Set`
- `HashSet`
- `EnumSet`
- `Iterator`
- `Collection`
- `Arrays`
- `Calendar`
- `Collections`
- `ConcurrentModificationException`
- `Date`
- `GregorianCalendar`
- `Locale`
- `NoSuchElementException`
- `Objects`
- `Optional`
- `OptionalDouble`
- `OptionalInt`
- `OptionalLong`
- `Properties`
- `Random`
- `Scanner`
- `SimpleTimeZone`
- `Spliterator`
- `Spliterators`
- `Timer`
- `TimeZone`
- `UUID`
##### From package java.util.stream:
- `Collector`
- `Collectors`
- `Stream`
- `DoubleStream`
- `IntStream`
- `LongStream`
- `StreamSupport`
##### From package java.io:
- `Closeable`
- `Serializable`
- `File`
- `Console`
- `FileNotFoundException`
- `IOException`
- `IOError`
- `InputStream`
- `FileInputStream`
- `BufferedInputStream`
- `ByteArrayInputStream`
- `OutputStream`
- `FileOutputStream`
- `BufferedOutputStream`
- `ByteArrayOutputStream`
- `PrintStream`
- `Reader`
- `FileReader`
- `BufferedReader`
- `CharArrayReader`
- `InputStreamReader`
- `StringReader`
- `Writer`
- `FileWriter`
- `BufferedWriter`
- `CharArrayWriter`
- `OutputStreamWriter`
- `PrintWriter`
- `StringWriter`
##### From package java.nio.file:
- `Paths`
- `Files`
- `Path`
- `StandardCopyOption`
- `StandardOpenOption`
##### From package java.nio.charser:
- `StandardCharsets`
##### From package java.math:
- `BigInteger`
- `BigDecimal`
- `MathContext`
- `RoundingMode`
##### From package java.util.concurrent:
- `Callable`
- `Executors`
- `TimeUnit`
##### From package java.util.regex:
- `Pattern`
##### From package java.util.function:
- (everything)

## Auto Static Imports List
##### From java.lang.String:
- `format`
- `join`
##### From java.lang.Boolean:
- `parseBoolean`
##### From java.lang.Byte:
- `parseByte`
##### From java.lang.Short:
- `parseShort`
##### From java.lang.Integer:
- `parseInt`
- `parseUnsignedInt`
##### From java.lang.Long:
- `parseLong`
- `parseUnsignedLong`
##### From java.lang.Float:
- `parseFloat`
##### From java.lang.Double:
- `parseDouble`