package jtree.nodes;

import static jtree.util.Utils.*;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@EqualsAndHashCode
@Getter @Setter
public abstract class CompilationUnit extends Node implements REPLEntry {
	protected @NonNull List<ImportDecl> imports;
	
	public CompilationUnit(List<ImportDecl> imports) {
		setImports(imports);
	}
	
	@Override
	public abstract CompilationUnit clone();
	
	public String importString() {
		var imports = getImports();
		return imports.isEmpty()? "" : joinNodes("\n", imports) + "\n\n";
	}
	
	public void setImports(@NonNull List<ImportDecl> imports) {
		this.imports = newList(imports);
	}
	
	public final void setImports(ImportDecl... imports) {
		setImports(List.of(imports));
	}
	
}
