package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public class ProvidesDirective extends Directive {
	protected @NonNull List<QualifiedName> serviceProviders;
	
	public ProvidesDirective(QualifiedName serviceName, List<QualifiedName> serviceProviders) {
		super(serviceName);
		setServiceProviders(serviceProviders);
	}
	
	@Override
	public ProvidesDirective clone() {
		return new ProvidesDirective(getName(), clone(getServiceProviders()));
	}
	
	@Override
	public String toCode() {
		return "provides " + getName() + " with " + joinNodes(", ", getServiceProviders()) + ";";
	}
	
	@Override
	public void setName(@NonNull QualifiedName typeName) {
		if(typeName.lastName().equals("var")) {
			throw new IllegalArgumentException("\"var\" cannot be used as a type name");
		}
		super.setName(typeName);
	}
	
	public void setServiceProviders(@NonNull List<QualifiedName> serviceProviders) {
		if(serviceProviders.isEmpty()) {
			throw new IllegalArgumentException("No service providers given");
		}
		this.serviceProviders = new ArrayList<>(serviceProviders.size());
		for(var serviceProvider : serviceProviders) {
			if(serviceProvider.lastName().equals("var")) {
				throw new IllegalArgumentException("\"var\" cannot be used as a type name");
			} else {
				this.serviceProviders.add(serviceProvider);
			}
		}
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitProvidesDirective(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getServiceProviders());
		}
	}
	
	
}
