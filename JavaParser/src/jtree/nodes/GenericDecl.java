package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public abstract class GenericDecl extends Declaration implements Member, TypeParameterHolder, Documented {
	protected @NonNull List<TypeParameter> typeParameters;
	protected @NonNull Name name;
	
	public GenericDecl(Name name, List<TypeParameter> typeParameters, List<Modifier> modifiers, List<Annotation> annotations, Optional<String> docComment) {
		super(modifiers, annotations, docComment);
		setName(name);
		setTypeParameters(typeParameters);
	}
	
	@Override
	public abstract GenericDecl clone();
	
	@Override
	public void setTypeParameters(@NonNull List<TypeParameter> typeParameters) {
		this.typeParameters = newList(typeParameters);
	}
	
	@Override
	public final void setTypeParameters(TypeParameter... typeParameters) {
		setTypeParameters(List.of(typeParameters));
	}
	
}
