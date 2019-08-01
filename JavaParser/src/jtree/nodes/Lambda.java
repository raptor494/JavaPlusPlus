package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import jtree.util.Either;
import jtree.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class Lambda extends Node implements Expression {
	protected @NonNull Either<? extends List<FormalParameter>, ? extends List<InformalParameter>> parameters;
	protected @NonNull Either<Block, ? extends Expression> body;
	
	public Lambda(List<? extends LambdaParameter> parameters, Block body) {
		this(makeParameters(parameters), body);
	}
	
	public Lambda(List<? extends LambdaParameter> parameters, Expression body) {
		this(makeParameters(parameters), body);
	}
	
	public Lambda(List<? extends LambdaParameter> parameters, Either<Block, ? extends Expression> body) {
		this(makeParameters(parameters), body);
	}
	
	public Lambda(Either<? extends List<FormalParameter>, ? extends List<InformalParameter>> parameters, Block body) {
		this(parameters, Either.first(body));
	}
	
	public Lambda(Either<? extends List<FormalParameter>, ? extends List<InformalParameter>> parameters, Expression body) {
		this(parameters, Either.second(body));
	}
	
	public Lambda(Either<? extends List<FormalParameter>, ? extends List<InformalParameter>> parameters, Either<Block, ? extends Expression> body) {
		setParameters(parameters);
		setBody(body);
	}
	
	@SuppressWarnings("unchecked")
	private static Either<? extends List<FormalParameter>, ? extends List<InformalParameter>> makeParameters(List<? extends LambdaParameter> parameters) {
		if(parameters.isEmpty()) {
			return Either.second(emptyList());
		} else {
			var first = parameters.get(0);
			if(first instanceof InformalParameter) {
				for(int i = 1; i < parameters.size(); i++) {
					InformalParameter.class.cast(parameters.get(i));
				}
				return Either.second((List<InformalParameter>)parameters);
			} else {
				for(var param : parameters) {
					FormalParameter.class.cast(param);
				}
				return Either.first((List<FormalParameter>)parameters);
			}
		}
	}
	
	@Override
	public Precedence precedence() {
		return Precedence.ASSIGNMENT;
	}
	
	@Override
	public Lambda clone() {
		return new Lambda(clone(getParameters()), clone(getBody()));
	}
	
	@Override
	public String toCode() {
		String result;
		var eitherParameters = getParameters();
		if(eitherParameters.isFirst()) {
			result = "(" + joinNodes(", ", eitherParameters.first()) + ")";
		} else {
			var parameters = eitherParameters.second();
			if(parameters.size() == 1) {
				result = parameters.get(0).toCode();
			} else {
				result = "(" + joinNodes(", ", parameters) + ")";
			}
		}
		result += " -> ";
		var body = getBody();
		if(body.isFirst()) {
			var block = body.first();
			result += block.isEmpty()? "{}" : block.toCode(); 
		} else {
			result += body.second().toCode();
		}
		return result;
	}
	
	public void setParameters(@NonNull Either<? extends List<FormalParameter>, ? extends List<InformalParameter>> parameters) {
		this.parameters = parameters.map(Utils::newList, Utils::newList);
	}
	
	public void setBody(@NonNull Either<Block, ? extends Expression> body) {
		Objects.requireNonNull(body.getValue());
		this.body = body;
	}
	
	public final void setBody(Block body) {
		setBody(Either.first(body));
	}
	
	public final void setBody(Expression body) {
		setBody(Either.second(body));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitLambda(this, parent, cast(replacer))) {
			getParameters().accept(params -> visitList(visitor, params), params -> visitList(visitor, params));
			getBody().accept(block -> block.<Block>accept(visitor, this, this::setBody), expr -> expr.<Expression>accept(visitor, this, this::setBody));
		}
	}
	
}
