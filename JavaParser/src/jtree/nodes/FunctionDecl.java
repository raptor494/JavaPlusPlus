package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class FunctionDecl extends GenericDecl implements Dimensioned {
	protected @NonNull Type returnType;
	protected @NonNull Optional<ThisParameter> thisParameter;
	private @NonNull List<FormalParameter> parameters;
	private @NonNull List<GenericType> exceptions;
	private @NonNull Optional<Block> body;
	private @NonNull List<Dimension> dimensions;

	public FunctionDecl(Name name, Type returnType, List<FormalParameter> parameters, Optional<Block> body) {
		this(name, returnType, parameters, emptyList(), body);
	}

	public FunctionDecl(Name name, Type returnType, List<FormalParameter> parameters, Block body) {
		this(name, returnType, parameters, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, Optional<Block> body) {
		this(name, typeParameters, returnType, parameters, emptyList(), body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, Block body) {
		this(name, typeParameters, returnType, parameters, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, Type returnType, List<FormalParameter> parameters, List<GenericType> exceptions,
						Optional<Block> body) {
		this(name, emptyList(), returnType, parameters, exceptions, body, emptyList(), emptyList());
	}

	public FunctionDecl(Name name, Type returnType, List<FormalParameter> parameters, List<GenericType> exceptions,
						Block body) {
		this(name, returnType, parameters, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<GenericType> exceptions, Optional<Block> body) {
		this(name, typeParameters, returnType, parameters, exceptions, body, emptyList(), emptyList());
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, typeParameters, returnType, parameters, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<GenericType> exceptions, Optional<Block> body,
						List<Modifier> modifiers, List<Annotation> annotations) {
		this(name, typeParameters, returnType, Optional.empty(), parameters, exceptions, body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<GenericType> exceptions, Block body,
						List<Modifier> modifiers, List<Annotation> annotations) {
		this(name, typeParameters, returnType, parameters, exceptions, Optional.ofNullable(body), modifiers,
				annotations);
	}

	public FunctionDecl(Name name, Type returnType, Optional<ThisParameter> thisParameter,
						List<FormalParameter> parameters, Optional<Block> body) {
		this(name, returnType, thisParameter, parameters, emptyList(), body);
	}

	public FunctionDecl(Name name, Type returnType, Optional<ThisParameter> thisParameter,
						List<FormalParameter> parameters, Block body) {
		this(name, returnType, thisParameter, parameters, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, Type returnType, ThisParameter thisParameter, List<FormalParameter> parameters,
						Optional<Block> body) {
		this(name, returnType, Optional.ofNullable(thisParameter), parameters, body);
	}

	public FunctionDecl(Name name, Type returnType, ThisParameter thisParameter, List<FormalParameter> parameters,
						Block body) {
		this(name, returnType, Optional.ofNullable(thisParameter), parameters, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters, Optional<Block> body) {
		this(name, typeParameters, returnType, thisParameter, parameters, emptyList(), body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters, Block body) {
		this(name, typeParameters, returnType, thisParameter, parameters, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, Optional<Block> body) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, Block body) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters,
				Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, Type returnType, Optional<ThisParameter> thisParameter,
						List<FormalParameter> parameters, List<GenericType> exceptions, Optional<Block> body) {
		this(name, emptyList(), returnType, thisParameter, parameters, exceptions, body, emptyList(), emptyList(), Optional.empty());
	}

	public FunctionDecl(Name name, Type returnType, Optional<ThisParameter> thisParameter,
						List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, returnType, thisParameter, parameters, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, Type returnType, ThisParameter thisParameter, List<FormalParameter> parameters,
						List<GenericType> exceptions, Optional<Block> body) {
		this(name, returnType, Optional.ofNullable(thisParameter), parameters, exceptions, body);
	}

	public FunctionDecl(Name name, Type returnType, ThisParameter thisParameter, List<FormalParameter> parameters,
						List<GenericType> exceptions, Block body) {
		this(name, returnType, Optional.ofNullable(thisParameter), parameters, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<GenericType> exceptions, Optional<Block> body) {
		this(name, typeParameters, returnType, thisParameter, parameters, exceptions, body, emptyList(), emptyList(), Optional.empty());
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<GenericType> exceptions, Block body) {
		this(name, typeParameters, returnType, thisParameter, parameters, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<GenericType> exceptions, Optional<Block> body) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, exceptions, body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, exceptions,
				Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<GenericType> exceptions, Block body, List<Modifier> modifiers,
						List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, thisParameter, parameters, exceptions, Optional.ofNullable(body),
				modifiers, annotations, docComment);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<GenericType> exceptions, Optional<Block> body,
						List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, exceptions, body,
				modifiers, annotations, docComment);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<GenericType> exceptions, Block body,
						List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, exceptions,
				Optional.ofNullable(body), modifiers, annotations, docComment);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<GenericType> exceptions, Optional<Block> body, List<Modifier> modifiers,
						List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, thisParameter, parameters, emptyList(), exceptions, body, modifiers,
				annotations, docComment);
	}

	public FunctionDecl(Name name, Type returnType, List<FormalParameter> parameters, List<Dimension> dimensions,
						List<GenericType> exceptions, Optional<Block> body) {
		this(name, emptyList(), returnType, parameters, dimensions, exceptions, body, emptyList(), emptyList());
	}

	public FunctionDecl(Name name, Type returnType, List<FormalParameter> parameters, List<Dimension> dimensions,
						List<GenericType> exceptions, Block body) {
		this(name, returnType, parameters, dimensions, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Optional<Block> body) {
		this(name, typeParameters, returnType, parameters, dimensions, exceptions, body, emptyList(), emptyList());
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Block body) {
		this(name, typeParameters, returnType, parameters, dimensions, exceptions, Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Optional<Block> body, List<Modifier> modifiers, List<Annotation> annotations) {
		this(name, typeParameters, returnType, Optional.empty(), parameters, dimensions, exceptions, body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Block body, List<Modifier> modifiers, List<Annotation> annotations) {
		this(name, typeParameters, returnType, parameters, dimensions, exceptions, Optional.ofNullable(body), modifiers,
				annotations);
	}

	public FunctionDecl(Name name, Type returnType, Optional<ThisParameter> thisParameter,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Block body) {
		this(name, emptyList(), returnType, thisParameter, parameters, dimensions, exceptions,
				Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, Type returnType, ThisParameter thisParameter, List<FormalParameter> parameters,
						List<Dimension> dimensions, List<GenericType> exceptions, Optional<Block> body) {
		this(name, emptyList(), returnType, Optional.ofNullable(thisParameter), parameters, dimensions, exceptions,
				body);
	}

	public FunctionDecl(Name name, Type returnType, ThisParameter thisParameter, List<FormalParameter> parameters,
						List<Dimension> dimensions, List<GenericType> exceptions, Block body) {
		this(name, emptyList(), returnType, Optional.ofNullable(thisParameter), parameters, dimensions, exceptions,
				Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<Dimension> dimensions, List<GenericType> exceptions, Optional<Block> body) {
		this(name, typeParameters, returnType, thisParameter, parameters, dimensions, exceptions, body, emptyList(),
				emptyList(), Optional.empty());
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<Dimension> dimensions, List<GenericType> exceptions, Block body) {
		this(name, typeParameters, returnType, thisParameter, parameters, dimensions, exceptions,
				Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Optional<Block> body) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, dimensions, exceptions,
				body);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Block body) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, dimensions, exceptions,
				Optional.ofNullable(body));
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<Dimension> dimensions, List<GenericType> exceptions, Block body, List<Modifier> modifiers,
						List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, thisParameter, parameters, dimensions, exceptions,
				Optional.ofNullable(body), modifiers, annotations, docComment);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Optional<Block> body, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, dimensions, exceptions,
				body, modifiers, annotations, docComment);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType, ThisParameter thisParameter,
						List<FormalParameter> parameters, List<Dimension> dimensions, List<GenericType> exceptions,
						Block body, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, returnType, Optional.ofNullable(thisParameter), parameters, dimensions, exceptions,
				Optional.ofNullable(body), modifiers, annotations, docComment);
	}

	public FunctionDecl(Name name, List<TypeParameter> typeParameters, Type returnType,
						Optional<ThisParameter> thisParameter, List<FormalParameter> parameters,
						List<Dimension> dimensions, List<GenericType> exceptions, Optional<Block> body,
						List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(name, typeParameters, modifiers, annotations, docComment);
		setReturnType(returnType);
		setExceptions(exceptions);
		setParameters(parameters);
		setBody(body);
		setThisParameter(thisParameter);
		setDimensions(dimensions);
	}
	
	@Override
	public FunctionDecl clone() {
		return new FunctionDecl(getName(), clone(getTypeParameters()), getReturnType().clone(), clone(getThisParameter()), clone(getParameters()), clone(getDimensions()), clone(getExceptions()), clone(getBody()), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}

	@Override
	public String toCode() {
		var exceptions = getExceptions();
		var parameters = getParameters();
		var typeParameterString = typeParameterString();
		if(!typeParameterString.isEmpty()) {
			typeParameterString += ' ';
		}
		var dimensionString = dimensionString();
		if(dimensionString.startsWith("@")) {
			dimensionString = ' ' + dimensionString;
		}
		return docString() + annotationString() + modifierString() + typeParameterString + getReturnType().toCode()
				+ " " + getName() + "(" + (parameters.isEmpty()
						? getThisParameter().map(ThisParameter::toCode)
								.orElse("")
						: getThisParameter().map(thisParameter -> thisParameter.toCode() + ", ")
								.orElse("") + joinNodes(", ", parameters))
				+ ")" + dimensionString
				+ (exceptions.isEmpty()? "" : " throws " + joinNodes(", ", exceptions))
				+ body.map(body -> " " + body.toCode()).orElse(";");
	}

	public void setExceptions(@NonNull List<GenericType> exceptions) {
		this.exceptions = newList(exceptions);
	}

	public final void setExceptions(GenericType... exceptions) {
		setExceptions(List.of(exceptions));
	}

	public void setParameters(@NonNull List<FormalParameter> parameters) {
		this.parameters = newList(parameters);
	}

	public final void setParameters(FormalParameter... parameters) {
		setParameters(List.of(parameters));
	}

	public void setBody(@NonNull Optional<Block> body) {
		this.body = body;
	}

	public final void setBody(Block body) {
		setBody(Optional.ofNullable(body));
	}

	public final void setBody() {
		setBody(Optional.empty());
	}

	public void setThisParameter(@NonNull Optional<ThisParameter> thisParameter) {
		this.thisParameter = thisParameter;
	}

	public final void setThisParameter(ThisParameter thisParameter) {
		setThisParameter(Optional.ofNullable(thisParameter));
	}

	public final void setThisParameter() {
		setThisParameter(Optional.empty());
	}

	@Override
	public void setDimensions(@NonNull List<Dimension> dimensions) {
		this.dimensions = newList(dimensions);
	}

	@Override
	public final void setDimensions(Dimension... dimensions) {
		setDimensions(List.of(dimensions));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitFunctionDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeParameters());
			getReturnType().accept(visitor, this, this::setReturnType);
			getThisParameter().ifPresent(thisParameter -> thisParameter.<ThisParameter>accept(visitor, this, this::setThisParameter));
			visitList(visitor, getParameters());
			visitList(visitor, getExceptions());
			getBody().ifPresent(body -> body.<Block>accept(visitor, this, this::setBody));
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}

}
