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
public class WildcardTypeArgument extends TypeArgument {
	protected @NonNull Optional<WildcardTypeArgument.Bound> bound;
	
	public WildcardTypeArgument() {
		this(Optional.empty());
	}
	
	public WildcardTypeArgument(Optional<WildcardTypeArgument.Bound> bound) {
		this(bound, emptyList());
	}
	
	public WildcardTypeArgument(List<Annotation> annotations) {
		this(Optional.empty(), annotations);
	}
	
	public WildcardTypeArgument(Optional<WildcardTypeArgument.Bound> bound, List<Annotation> annotations) {
		super(annotations);
		setBound(bound);
	}
	
	@Override
	public WildcardTypeArgument clone() {
		return new WildcardTypeArgument(clone(getBound()), clone(getAnnotations()));
	}
	
	@Override
	public String toCode() {
		return getBound().map(bound -> "? " + bound.toCode()).orElse("?");
	}
	
	public void setBound(@NonNull Optional<WildcardTypeArgument.Bound> bound) {
		this.bound = bound;
	}
	
	public final void setBound(WildcardTypeArgument.Bound bound) {
		setBound(Optional.ofNullable(bound));
	}
	
	public final void setBound() {
		setBound(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitWildcardTypeArgument(this, parent, cast(replacer))) {
			getBound().ifPresent(bound -> bound.<WildcardTypeArgument.Bound>accept(visitor, this, this::setBound));
			visitList(visitor, getAnnotations());
		}
	}
	
	@EqualsAndHashCode
	@Getter @Setter
	public static class Bound extends Node {
		protected @NonNull Bound.Kind kind;
		protected @NonNull ReferenceType type;
		
		public Bound(Bound.Kind kind, ReferenceType type) {
			setKind(kind);
			setType(type);
		}
		
		@Override
		public Bound clone() {
			return new Bound(getKind(), getType().clone());
		}
		
		@Override
		public String toCode() {
			return getKind() + " " + getType().toCode();
		}
		
		public static enum Kind {
			SUPER, EXTENDS;
			
			@Override
			public String toString() {
				return name().toLowerCase();
			}
			
		}

		@Override
		public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
			if(visitor.visitWildcardTypeArgumentBound(this, parent, cast(replacer))) {
				getType().accept(visitor, this, this::setType);
			}
		}
		
	}
	
}
