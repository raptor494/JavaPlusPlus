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
public class ConstructorDecl extends GenericDecl implements REPLEntry {
	protected @NonNull Optional<ThisParameter> thisParameter;
	protected @NonNull List<FormalParameter> parameters;
	private @NonNull List<GenericType> exceptions;
	private @NonNull Block body;
	
	public ConstructorDecl(Name name, List<FormalParameter> parameters, Block body) {
		this(name, parameters, emptyList(), body);
	}
	
	public ConstructorDecl(Name name, List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, emptyList(), parameters, exceptions, body, emptyList(), emptyList());
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, typeParameters, parameters, exceptions, body, emptyList(), emptyList());
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, List<FormalParameter> parameters, List<GenericType> exceptions, Block body, List<Modifier> modifiers, List<Annotation> annotations) {
		this(name, typeParameters, Optional.empty(), parameters, exceptions, body);
	}
	
	public ConstructorDecl(Name name, Optional<ThisParameter> thisParameter, List<FormalParameter> parameters, Block body) {
		this(name, thisParameter, parameters, emptyList(), body);
	}
	
	public ConstructorDecl(Name name, ThisParameter thisParameter, List<FormalParameter> parameters, Block body) {
		this(name, Optional.ofNullable(thisParameter), parameters, body);
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, Optional<ThisParameter> thisParameter, List<FormalParameter> parameters, Block body) {
		this(name, typeParameters, thisParameter, parameters, emptyList(), body);
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, ThisParameter thisParameter, List<FormalParameter> parameters, Block body) {
		this(name, typeParameters, Optional.ofNullable(thisParameter), parameters, body);
	}
	
	public ConstructorDecl(Name name, Optional<ThisParameter> thisParameter, List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, emptyList(), thisParameter, parameters, exceptions, body, emptyList(), emptyList(), Optional.empty());
	}
	
	public ConstructorDecl(Name name, ThisParameter thisParameter, List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, Optional.ofNullable(thisParameter), parameters, exceptions, body);
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, Optional<ThisParameter> thisParameter, List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, typeParameters, thisParameter, parameters, exceptions, body, emptyList(), emptyList(), Optional.empty());
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, ThisParameter thisParameter, List<FormalParameter> parameters, List<GenericType> exceptions, Block body) {
		this(name, typeParameters, Optional.ofNullable(thisParameter), parameters, exceptions, body);
	}
	
	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, ThisParameter thisParameter, List<FormalParameter> parameters, List<GenericType> exceptions, Block body, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		this(name, typeParameters, Optional.ofNullable(thisParameter), parameters, exceptions, body, modifiers, annotations, docComment);
	}

	public ConstructorDecl(Name name, List<TypeParameter> typeParameters, Optional<ThisParameter> thisParameter,
						   List<FormalParameter> parameters, List<GenericType> exceptions, Block body,
						   List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(name, typeParameters, modifiers, annotations, docComment);
		setExceptions(exceptions);
		setParameters(parameters);
		setBody(body);
		setThisParameter(thisParameter);
	}
	
	@Override
	public ConstructorDecl clone() {
		return new ConstructorDecl(getName(), clone(getTypeParameters()), clone(getThisParameter()), clone(getParameters()), clone(getExceptions()), getBody().clone(), clone(getModifiers()), clone(getAnnotations()), getDocComment());
	}
	
	@Override
	public String toCode() {
		var exceptions = getExceptions();
		var parameters = getParameters();
		return docString() + annotationString() + modifierString() + getName() 
				+ "(" + (parameters.isEmpty()
						? getThisParameter().map(ThisParameter::toCode)
											.orElse("")
						: getThisParameter().map(thisParameter -> thisParameter.toCode() + ", ")
											.orElse("") + joinNodes(", ", parameters)) + ")"
				+ (exceptions.isEmpty()? "" : " throws " + joinNodes(", ", exceptions))
				+ " " + getBody().toCode();
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
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitConstructorDecl(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getTypeParameters());
			getThisParameter().ifPresent(thisParameter -> thisParameter.<ThisParameter>accept(visitor, this, this::setThisParameter));
			visitList(visitor, getParameters());
			visitList(visitor, getExceptions());
			getBody().accept(visitor, this, this::setBody);
			visitList(visitor, getModifiers());
			visitList(visitor, getAnnotations());
		}
	}
	
}
