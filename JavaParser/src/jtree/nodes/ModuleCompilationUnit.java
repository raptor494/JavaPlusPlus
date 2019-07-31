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
public class ModuleCompilationUnit extends CompilationUnit implements Annotated, Documented {
	protected @NonNull QualifiedName name;
	protected boolean open;
	private @NonNull List<Annotation> annotations;
	private @NonNull List<? extends Directive> directives;
	private @NonNull Optional<String> docComment;
	
	public ModuleCompilationUnit(QualifiedName name, List<? extends Directive> directives) {
		this(name, false, directives);
	}
	
	public ModuleCompilationUnit(QualifiedName name, boolean open, List<? extends Directive> directives) {
		this(name, open, directives, emptyList(), Optional.empty());
	}
	
	public ModuleCompilationUnit(QualifiedName name, boolean open, List<? extends Directive> directives, List<Annotation> annotations, Optional<String> docComment) {
		this(emptyList(), name, open, directives, annotations, docComment);
	}
	
	public ModuleCompilationUnit(List<ImportDecl> imports, QualifiedName name, boolean open, List<? extends Directive> directives, List<Annotation> annotations, Optional<String> docComment) {
		super(imports);
		setName(name);
		setOpen(open);
		setDirectives(directives);
		setAnnotations(annotations);
		setDocComment(docComment);
	}
	
	@Override
	public ModuleCompilationUnit clone() {
		return new ModuleCompilationUnit(clone(getImports()), getName(), isOpen(), clone(getDirectives()), clone(getAnnotations()), getDocComment());
	}
	
	@Override
	public String toCode() {
		var directives = getDirectives();
		return importString() + docString() + annotationString() + (isOpen()? "open module " : "module ")
				+ getName() + " {" + (directives.isEmpty()? "}" : "\n" + join("", directives, directive -> directive.toCode().indent(4)) + "}" );
	}
	
	public void setDirectives(@NonNull List<? extends Directive> directives) {
		this.directives = newList(directives);
	}
	
	public final void setDirectives(Directive... directives) {
		setDirectives(List.of(directives));
	}
	
	@Override
	public void setAnnotations(@NonNull List<Annotation> annotations) {
		this.annotations = newList(annotations);
	}
	
	@Override
	public final void setAnnotations(Annotation... annotations) {
		setAnnotations(List.of(annotations));
	}
	
	@Override
	public void setDocComment(@NonNull Optional<String> docComment) {
		this.docComment = docComment;
	}
	
	@Override
	public final void setDocComment(String docComment) {
		setDocComment(Optional.ofNullable(docComment));
	}
	
	@Override
	public final void setDocComment() {
		setDocComment(Optional.empty());
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitModuleCompilationUnit(this, parent, cast(replacer))) {
			getName().accept(visitor, this, this::setName);
			visitList(visitor, getImports());
			visitList(visitor, getDirectives());
			visitList(visitor, getAnnotations());
		}
	}
	
}
