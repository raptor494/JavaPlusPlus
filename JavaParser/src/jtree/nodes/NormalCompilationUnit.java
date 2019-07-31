package jtree.nodes;

import static jtree.util.Utils.*;
import static lombok.AccessLevel.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@EqualsAndHashCode
@Getter @Setter
public class NormalCompilationUnit extends CompilationUnit {
	@Getter(NONE) @Setter(NONE)
	protected @NonNull Optional<PackageDecl> _package;
	protected @NonNull List<? extends TypeDecl> declarations;
	
	public NormalCompilationUnit(List<? extends TypeDecl> declarations) {
		this(Optional.empty(), declarations);
	}
	
	public NormalCompilationUnit(TypeDecl... declarations) {
		this(List.of(declarations));
	}
	
	public NormalCompilationUnit(Optional<PackageDecl> _package, List<? extends TypeDecl> declarations) {
		this(_package, emptyList(), declarations);
	}
	
	public NormalCompilationUnit(PackageDecl _package, List<? extends TypeDecl> declarations) {
		this(Optional.ofNullable(_package), declarations);
	}
	
	public NormalCompilationUnit(Optional<PackageDecl> _package, TypeDecl... declarations) {
		this(_package, List.of(declarations));
	}
	
	public NormalCompilationUnit(PackageDecl _package, TypeDecl... declarations) {
		this(Optional.ofNullable(_package), List.of(declarations));
	}
	
	public NormalCompilationUnit(Optional<PackageDecl> _package, List<ImportDecl> imports, TypeDecl... declarations) {
		this(_package, imports, List.of(declarations));
	}
	
	public NormalCompilationUnit(PackageDecl _package, List<ImportDecl> imports, List<? extends TypeDecl> declarations) {
		this(Optional.ofNullable(_package), imports, declarations);
	}
	
	public NormalCompilationUnit(PackageDecl _package, List<ImportDecl> imports, TypeDecl... declarations) {
		this(Optional.ofNullable(_package), imports, List.of(declarations));
	}
	
	public NormalCompilationUnit(Optional<PackageDecl> _package, List<ImportDecl> imports, List<? extends TypeDecl> declarations) {
		super(imports);
		setPackage(_package);
		setDeclarations(declarations);
	}
	
	@Override
	public NormalCompilationUnit clone() {
		return new NormalCompilationUnit(clone(getPackage()), clone(getImports()), clone(getDeclarations()));
	}
	
	@Override
	public String toCode() {
		return (getPackage().map(pckg -> pckg.toCode() + "\n").orElse("")
				+ importString()
				+ joinNodes("\n\n", getDeclarations())).stripTrailing();
	}
	
	public Optional<PackageDecl> getPackage() {
		return _package;
	}
	
	public void setPackage(@NonNull Optional<PackageDecl> _package) {
		this._package = _package;
	}
	
	public final void setPackage(PackageDecl _package) {
		setPackage(Optional.ofNullable(_package));
	}
	
	public final void setPackage() {
		setPackage(Optional.empty());
	}
	
	public void setDeclarations(@NonNull List<? extends TypeDecl> declarations) {
		this.declarations = newList(declarations);
	}
	
	public final void setDeclarations(TypeDecl... declarations) {
		setDeclarations(List.of(declarations));
	}

	@Override
	public <N extends INode> void accept(TreeVisitor visitor, Node parent, Consumer<N> replacer) {
		if(visitor.visitNormalCompilationUnit(this, parent, cast(replacer))) {
			getPackage().ifPresent(pckg -> pckg.<PackageDecl>accept(visitor, this, this::setPackage));
			visitList(visitor, getImports());
			visitList(visitor, getDeclarations());
		}
	}
	
}
