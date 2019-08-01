# Java++
Java++, adding additional syntactical sugar to vanilla Java.
It's called Java++ because, like C++, it addes more syntax to a base language while still
remaining compatible.

## Features
I have organized each feature into several 'categories'. 
The main point about this is that this parser is modular - you can enable/disable most features on the fly with a special statement.

### Synta Conversion Modifiers

### Fully-Qualified Names
*Feature id:* `converter.qualifiedNames`

*Disabled by default.*

Enabling this causes the syntax converter to use fully-qualified names for generated constructs such as function calls.
So, instead of `System.out.println()`, you'd get `java.lang.System.out.println()`.

### Statements

#### Feature Enabling/Disabling
To syntactically enable/disable features, you use `enable` and `disable` statements. These statements must occurr before the import section but after the package declaration. Follow the `enable` or `disable` keyword with the feature id of a feature to turn it off/on. You can end the id with `.*` to enable/disable all features under that particular section. To enable/disable all features at once, follow the `enable`/`disable` keyword with an asterisk `*`.

Syntax:
```
EnableStmt:
    enable * ;
    enable FeatureIdList ;
DisableStmt:
    disable * ;
    disable FeatureIdList ;
FeatureIdList:
    FeatureId
    FeatureId , FeatureIdList
FeatureId:
    Identifier
    FeatureId . Identifier
    FeatureId . *
```

#### The Import Statements
*Feature id:* `syntax.commaImports`

*Enabled by default.*

The `import` statement can now contain multiple comma-separated namespaces.
```python
import java.util.List, java.util.Map;
```

#### The From-Import Statement
*Feature id:* `statements.fromImport`

*Enabled by default.*

There is a new form of import statement called the `from-import` statement. This is a new statement which I stole from Python. It begins with the contextual keyword `from`. It allows you to import multiple names from a particular package, omitting the need to write that package over and over again.
Syntax:

    from <package name> import [static] <name1>[, <name2>[, ...]];

###### Example:
```python
from java.util import List, ArrayList, Set, HashMap, Map;
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
    If there are multiple expressions, the statement gets wrapped in a block containing multiple calls to `System.out.print()` for each expression. The calls are separated with `System.out.print(' ');` to add a space between the expressions.
2. The `println` statement

    This statement delegates to `System.out.println()`.
    Syntax:

        println [<expression>[, <expression>[, ...]]];

    If there are multiple expressions, the statement behaves the same way as the `print` statement, except the final print call is a call to `System.out.println()`.
3. The `printf` statement

    This statement delegates to `System.out.printf()`.
    Syntax:

        printf <format string>[, <argument 1>[, <argument 2>[, ...]]];

4. The `printfln` statement

    This statement works like the `printf` statement, except
    it also appends `"%n"` to the end of the format string.
    Syntax is the same as the `printf` statement.

#### The Loop Statement
*Feature id:* `statements.emptyFor`

*Enabled by default.*

This feature adds a version of the `for` statement which is shorthand for `for(;;)`. 

Syntax:
```java
for <statement>
```

<!--
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

-->


#### If-Not Statements
*Feature id:* `statements.notCondition`

*Enabled by default.*

This feature allows you to prefix the parenthesized argument of control flow statements with a `!` to invert the condition. 

###### Example:
This:
```java
if!(x instanceof String) {
    doStuff();
}
```
becomes this:
```java
if(!(x instanceof String)) {
    doStuff();
}
```

#### Simpler For-Each
*Feature id:* `statements.simpleForEach`

*Enabled by default.*

This feature adds an even simpler way of writing a for-each statement. 
Syntax:
```
for ( <name> in <expression> ) <statement>
```

#### Empty Synchronized Lock
*Feature id:* `statements.emptySynchronized`

*Enabled by default.*

This feature allows you to omit the lock expression of a `synchronized` statement.
If the statement occurrs in a static context, the lock expression will become the containing class's class literal.
Otherwise, it becomes `this`.

###### Example:
This:
```java
synchronized {
    foo();
}
```
becomes this:
```java
synchronized(this) {
    foo();
}
```

#### Un-Imports
*Feature id:* `statements.unimport`

*Enabled by default.*

This feature will allow you to use `unimport` when not dealing with feature enabling/disabling. 
It will not work with `java.lang` classes, however.

#### Default Catch
*Feature id:* `statements.defaultCatch`

*Enabled by default.*

This feature allows you to omit the parameter of the last `catch` block in a `try` statement to catch all exceptions.

#### Try-Else
*Feature id:* `statements.tryElse`

*Enabled by default.*

This feature allows you to follow the list of `catch` clauses in a `try` statement with an `else` block, which will get executed only if the main `try` block completes without throwing an exception.
If the `finally` block is present, it must appear after the `else` block and will be executed after the `else` block finishes.

#### The With Statement
*Feature id:* `statements.with`

*Enabled by default.*

This feature is a more-readable alternative to the `try`-with-resources statement.
Additionally, if one of the resources isn't a variable declaration and is instead an expression, and the expression isn't a named expression, it will automatically create a hidden variable assigned to the result of that expression.

###### Example:
This:
```java
with(foo()) {
    doStuff();
}
```
becomes this:
```java
try(var __resource = foo()) {
    doStuff();
}
```

#### Empty Statements
*Feature id:* `statements.empty`

*Enabled by default.*

Enabling this feature makes the empty statement (a single semicolon) a syntax error, requiring you to use an empty block instead.

### Expressions

#### Variable Declaration Expression
*Feature id:* `expressions.variableDeclarations`

*Enabled by default.*

This feature allows you to put a variable declaration inside a parenthesized expression, like so:
```java
foo((int x = 5), x*2);
```

<!--
#### Null-safe Expression
*Feature id:* `expressions.nullSafe`

*Enabled by default.*

This feature adds the 'Elvis' operator from Groovy: `?:`. It has the same precedence as the conditional
operator. It returns its right argument if its left argument is `null`, otherwise it returns its left argument.
This operator delegates to either `Objects.requireNonNullElse()` or `Objects.requireNonNullElseGet()` depending on
the complexity of the right argument.
-->

<!--
#### Equality Expression
*Feature id:* `expressions.equality`

*Disabled by default.*

This feature turns the == operator into a call to `Objects.deepEquals()` and adds the `is`/`is!` operators to test for identity.
The == is only turned into the call if neither of its arguments are number, class, or null literals.
Note that `is!` is only the `!=` operator if there is no space between `is` and `!`.
-->

#### Partial Method References
*Feature id:* `expressions.partialMethodReferences`

*Enabled by default.*

This feature allows you to add arguments to a method reference expression, or explicitly specify the number of arguments to that reference.

This is best explained by example.

###### Example 1:
This:
```java
this::foo()
```
becomes this:
```java
(() -> this.foo())
```
###### Example 2:
This:
```java
this::foo(x)
```
becomes this:
```java
(() -> this.foo(x))
```
###### Example 3:
This:
```java
this::foo(_)
```
becomes this:
```java
((__arg0) -> this.foo(__arg0))
```
###### Example 4:
This:
```java
this::foo(_,5)
```
becomes this:
```java
((__arg0) -> this.foo(__arg0, 5))
```
###### Example 5:
This:
```java
Test::new("abc")
```
becomes this:
```java
(() -> new Test("abc"))
```
###### Example 6:
This:
```java
int[]::new(5)
```
becomes this:
```java
(() -> new int[5])
```
###### Example 7:
This:
```java
int[]::new{1,2,3}
```
becomes this:
```java
(() -> new int[] {1,2,3})
```
###### Example 8:
This:
```java
Object::new{
    public void foo() { ... }
}
```
becomes this:
```java
(() -> new Object() {
    public void foo() { ... }
})
```

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

If there is no argument list given to a `new` expression, you can follow the type name with a bracket-enclosed series of elements (for Collections) or key-value pairs (for Maps).
```java
List<String> list = new ArrayList<> {"a", "b", "c", "d"};
Map<String,Integer> map = new HashMap<> {"key1": 1, "key2": 2, "key3": 4};
```
This works by actually calling the type's constructor with a single argument created from the elements using either `List.of()` or `Map.of()` as appropriate.
So, the expression `new ArrayList<>{1,2,3}` gets turned into `new ArrayList<>(List.of(1,2,3))`.

One final note: For variable initializers of the form `Type varname = {...}`, if `Type` is an array type, or there are any array brackets appearing after `varname`, then the `{...}` expression will become an array initializer. Otherwise, it will become either a Map or Set literal.
So, the following two statements work as expected:
```java
int[] ints1 = {1,2,3,4};
Set<Integer> ints2 = {1,2,3,4};
```


#### Optional Literals
*Feature id:* `literals.optional`

*Enabled by default.*

This feature adds literals for `Optional`, `OptionalInt`, `OptionalDouble`, and `OptionalLong`.
The basic syntax to wrap an expression in an Optional is this:

    expression?

This expression delegates to the `Optional.ofNullable()` method. The `'?'` operator is only interpreted as an Optional literal if the token immediately after it is either `')'`, `']'`, `'}'`, `','`, or `';'`. Otherwise, the parser will assume you want to do a conditional expression. The `'?' `operator has the lowest precedence of any operator, so for example `2 + 3 & 4 || x?` gets turned into `Optional.ofNullable(2 + 3 & 4 || x)`.
The basic syntax to create an empty Optional is this:

    ?

The `'?'` is a primary expression and is basically shorthand for `Optional.empty()`.

To do a literal for one of the primitive-typed Optionals, follow the `'?'` operator with either `<int>`, `<double>`, or `<long>`. Any other angle-bracket enclosed type following the `'?'` will become the type argument to the `Optional.ofNullable()` method.

I consider it good practice to wrap an optional literal in parenthesis, like in the examples below.
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
###### Example 3:
This:
```java
(obj?<@NonNull T>)
```
becomes this:
```java
(Optional.<@NonNull T>of(obj))
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

It can optionally be followed by the `else` keyword to call a different method:

If the `else` keyword is followed by `throw`, the method called will be `orElseThrow` and the expression to the right of the `throw` keyword becomes the body of the no-args lambda argument to the method call.
If the expression to the right of the `else` keyword is either a literal, a variable, or a parenthesized expression of either, the method called is `orElse` and the expression becomes the argument of the method call.
Otherwise, the expression to the right of the `else` keyword becomes the body of the no-args lambda argument to the method call.

Syntax:
```
OptionalForceUnwrapExpr:
    SuffixExpr !
    SuffixExpr ! else throw OptionalForceUnwrapExprLambdaBody
    SuffixExpr ! else OptionalForceUnwrapExprSimple
    SuffixExpr ! else OptionalForceUnwrapExprLambdaBody

OptionalForceUnwrapExprSimple:
    ( OptionalForceUnwrapExprSimple )
    Literal
    Identifier
    MethodReference
    ClassLiteral

OptionalForceUnwrapExprLambdaBody:
    Block
    ( Expression )
    ClassCreator
    SuffixExpr
```

###### Example 1:
This:
```java
var opt = "test"?;
println opt! else null;
```
becomes this:
```java
var opt = Optional.ofNullable("test");
System.out.println(opt.orElse(null));
```
###### Example 2:
This:
```java
var opt = "test"?;
println opt! else getDefaultForOpt();
```
becomes this:
```java
var opt = Optional.ofNullable("test");
System.out.println(opt.orElseGet(() -> getDefaultForOpt()));
```
###### Example 3:
This:
```java
var opt = "test"?;
println opt! else throw new NoSuchElementException;
```
becomes this:
```java
var opt = Optional.ofNullable("test");
System.out.println(opt.orElseThrow(() -> new NoSuchElementException()));
```

##### Optional Types
This feature also adds a quicker syntax to specify an Optional type: simply suffix the type with a `'?'`.

###### Example 1:
This:
```java
String? str = ("test"?);
```
becomes this:
```java
Optional<String> str = Optional.ofNullable("test");
```
###### Example 2:
This:
```java
int? x = 5?;
```
becomes this:
```java
OptionalInt x = OptionalInt.of(5);
```

Note that only `int`, `long`, and `double` types can become Optional types. Something like `float?` will not work.

<!--
#### Byte-Array Literals
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
-->

#### String Literals
*Feature id:* `literals.textBlocks`

*Enabled by default.*

*Feature id:* `literals.rawStrings`

*Enabled by default.*

I've stolen Python's multi-line and raw strings.
```python
""" A multi-
line string """

R"A raw string, \ has no power here!"
```

#### Format Strings
*Feature id:* `literals.formatStrings`

*Enabled by default.*

After much consideration, I decided to go a completely
new way when implementing interpolated strings.
Format strings ("f-strings") in Java++ are indicated by prefixing a string with either `f` or `F`. Within an f-string, the `%` character indicates the beginning of an interpolated expression. This has two forms: `%<name>` and `%{<expression>}`. In the second form, you can optionally follow the closing `}` with formatting flags/conversion characters and the block's content will automatically be formatted appropriately.

The way this works is, each interpolated expression is ripped from the format string and added as an argument to `String.format()`, with a specific formatting expression put in its place.

###### Example 1:
This:
```python
f"Hello, my name is %name"
```
becomes this:
```java
String.format("Hello, my name is %1$s", name)
```
###### Example 2:
This:
```python
f"Hello, my name is %{getName()}"
```
becomes this:
```java
String.format("Hello, my name is %1$s", getName())
```
###### Example 3:
This:
```python
f"You have $%{money}1.2f"
```
becomes this:
```java
String.format("You have $%1$1.2f", money)
```

Format strings are compatible with both multi-line and raw strings.

#### Regex Literals
*Feature id:* `literals.regex`

*Enabled by default*

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

### Syntax

#### Trailing Commas
*Feature id:* `syntax.trailingCommas`

*Disabled by default.*

This feature allows you to add a trailing comma anywhere comma-separated lists are used.
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
- Argument lists
- Parameter lists
- Import declaration namespace lists
- Field/local variable declarations
- `print` statement argument lists
- Normal `for` loop update lists
- `case` labels
- Type arguments/parameters
- `implements` type lists for classes/enums
- `extends` type lists for interfaces
- `throws` exception lists

#### Argument Annotations
*Feature id:* `syntax.argumentAnnotations`

*Enabled by default.*

This feature simply allows you to add a name to a function call argument. The name doesn't have to match the declared parameter's name, it can be anything.
###### Example:
```java
foo(5, arg2: false, "test", arg4: null)
```

<!--
#### Multiple Import Sections
*Feature id:* `syntax.multiple_import_sections`

*Enabled by default.*

This feature simply allows you to add more imports after any top-level type declarations.

-->

#### Optional 'new' Arguments
*Feature id:* `syntax.optionalNewArguments`

*Enabled by default.*

This feature changes makes the argument list optional in `new` expressions.
```java
var sb = new StringBuilder;
```

#### Default Arguments
*Feature id:* `syntax.defaultArguments`

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

#### Default Modifiers
*Feature id:* `syntax.defaultModifiers`

*Enabled by default.*

This feature allows you to assign default modifiers/annotations to anything which can accept modifiers.
To do this, simply end a modifier list with a colon (:). Anything member after that will have those modifiers,
plus whatever modifiers were explicitly declared beside it. Modifiers are merged such that if a member declares a 
visibility modifier, any default visibility modifier won't be applied, and if a member declares a modifier prefixed with 'non-',
then its opposite will not be applied.
It also adds an explicit way to declare a member as package-private by using the modifier 'package'.
###### Example:
```java
class Test {

public static:
    void main(String[] args) {

    }

    non-static void foo() {

    }

    package void bar() {

    }

}
```
declares `public static void main` and `public void foo` and `static void bar`.

#### Empty Class Body
*Feature id:* `syntax.simpleClassBodies`

*Enabled by default.*

This feature allows you to use a semicolon instead of empty brackets in type bodies.
```java
public class Empty;
```

#### Last Lambda Argument
*Feature id:* `syntax.lastLambdaArgument`

*Enabled by default.*

With this feature enabled, following an argument list
of a function call with a block will add an additional, no-args lambda to that argument list.

###### Example:
This:
```java
foo(1,2) {
    System.out.println("Hello, world!");
}
```
becomes this:
```java
foo(1, 2, () -> {
    System.out.println("Hello, world!");
})
```

#### Optional Condition Parenthesis
*Feature id:* `syntax.optionalStatementParenthesis`

*Disabled by default.*

With this feature enabled, you no longer need to wrap
the arguments of `if`, `while`, `do-while`, `synchronized`, or `with` statements in parenthesis.

###### Example:
This:
```java
if condition {
    doStuff();
}
```
becomes this:
```java
if(condition) {
    doStuff();
}
```

This feature takes precedence over the `if-not` feature.

#### Implicit Blocks
*Feature id:* `syntax.implicitBlocks`

*Disabled by default.*

This feature allows you to use normal statements instead of blocks when a statement would normally require a block, such as in `try`, `synchronized`, and arrow-case body statements.

#### Implicit Semicolons
*Feature id:* `syntax.implicitSemicolons`

*Enabled by default.*

This feature allows you to omit a semicolon ending a statement if the previous token was a right-brace `}`.

###### Example:
```java
IntFunction<String> itoa = (int x) -> {
    return Integer.toString(x);
}
```

#### Quick Constructor Bodies
*Feature id:* `syntax.simpleConstructorBodies`

*Enabled by default.*

#### Improved Explicit Constructor Call Arguments
*Feature id:* `syntax.improvedExplicitConstructorCallArguments`

*Enabled by default.*

This feature is kinda complicated.
It allows you to quickly call an explicit constructor (via `this` or `super`) using the parameters defined in the enclosing constructor, by replacing an argument with an underscore `_`, or use all parameters by replacing an argument with an asterisk `*`.

###### Example 1:
This:
```java
public Test(String name, int id) {
    this(_, _, null);
}
```
becomes this:
```java
public Test(String name, int id) {
    this(name, id, null);
}
```
###### Example 2:
This:
```java
public Test(String name, int id) {
    this(*, null);
}
```
becomes this:
```java
public Test(String name, int id) {
    this(name, id, null);
}
```

#### Simple Method Bodies
*Feature id:* `syntax.simpleMethodBodies`

*Enabled by default.*

This feature allows you to use the `-> expression` syntax from lambdas as a method body.

###### Example:
This:
```java
int foo(int x) -> 2*x;
```
becomes this:
```java
int foo(int x) {
    return 2*x;
}
```

#### Simple Constructor Bodies
*Feature id:* `syntax.simpleConstructorBodies`

*Enabled by default.*

This feature allows you to declare an empty constructor body by using a semicolon instead of an empty block.

It also adds a way to call an explicit constructor without using the block format.
The syntax is:
```
{ConstructorModifiers} Identifier ( [ParameterList] ) : SimpleExplicitConstructorCall ;
```
Where `SimpleConstructorCall` is defined as:
```
SimpleConstructorCall:
    [Primary .] super [ConstructorArgumentList] 
    this ConstructorArgumentList
```

Omitting the constructor argument list will just call the super constructor with all the parameters of the current constructor.

#### Automatic 'default' Modifier
*Feature id:* `syntax.autoDefaultModifier`

*Enabled by default.*

This feature adds the `default` modifier to any non-static method within an interface that has a body.

#### Better Arrow-Case Bodies
*Feature id:* `syntax.betterArrowCaseBodies`

*Enabled by default.*

This feature allows you to use more types of statements in an arrow-case body.
Currently, vanilla Java only allows you to use either a `throw` statement, a block, or an expression statement as the body of an arrow-case. This feature additionally allows you to use `if`, `try`, and `return` statements, plus the `with` statement if it is enabled.

###### Example:
This:
```java
public boolean isWeekend(Day day) {
    switch(day) {
        case SATURDAY, SUNDAY -> return true;
        default -> return false;
    }
}
```
becomes this:
```java
public boolean isWeekend(Day day) {
    switch(day) {
        case SATURDAY, SUNDAY -> {
            return true;
        }
        default -> {
            return false;
        }
    }
}
```

#### Alternative Annotation Declarations
*Feature id:* `syntax.altAnnotationDecl`

*Enabled by default.*

This feature adds a more concise way to define annotations using the `annotation` contextual keyword.

###### Example 1:
This:
```java
public annotation Named(String name, Class<?> type = Object.class) {
    public static final String NAME = "name";
}
```
becomes this:
```java
public @interface Named {
    String name();
    Class<?> type() default Object.class;
    public static final String NAME = "name";
}
```
###### Example 2:
This:
```java
public annotation Named(String);
```
becomes this:
```java
public @interface Named {
    String value();
}
```

Syntax:
```
AltAnnotationDecl:
    {ClassModifier} annotation TypeName [AltAnnotationProperties] AltAnnotationBody
AltAnnotationBody:
    ;
    InterfaceBody
AltAnnotationProperties:
    ( {AnnotationPropertyModifier} Type [AltAnnotationPropertyDefault] )
    ( [AltAnnotationPropertyList] )
AltAnnotationPropertyList:
    AltAnnotationProperty
    AltAnnotationProperty , AltAnnotationPropertyList
AltAnnotationProperty:
    {AnnotationPropertyModifier} Type Identifier [AltAnnotationPropertyDefault]
AnnotationPropertyModifier:
    (one of)
    Annotation public abstract
AltAnnotationPropertyDefault:
    = AnnotationValue
```

#### The var Statement
*Feature id:* `syntax.multiVarDecls`

*Enabled by default.*

This feature allows you to declare multiple variables in a `var` statement.
The declarations are separated out later.
###### Example:
This:
```java
var x = 5, y = 2;
```
becomes this:
```java
var x = 5; var y = 2;
```
