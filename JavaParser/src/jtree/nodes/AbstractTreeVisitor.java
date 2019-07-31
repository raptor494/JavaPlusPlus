package jtree.nodes;

import java.util.function.Consumer;

import jtree.nodes.WildcardTypeArgument.Bound;

public abstract class AbstractTreeVisitor implements TreeVisitor {

	@Override
	public boolean visitAnnotation(Annotation node, Node parent, Consumer<Annotation> replacer) {
		return true;
	}

	@Override
	public boolean visitAnnotationArgument(AnnotationArgument node, Node parent,
										   Consumer<AnnotationArgument> replacer) {
		return true;
	}

	@Override
	public boolean visitAnnotationDecl(AnnotationDecl node, Node parent, Consumer<AnnotationDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitAnnotationProperty(AnnotationProperty node, Node parent,
										   Consumer<AnnotationProperty> replacer) {
		return true;
	}

	@Override
	public boolean visitArrayCreator(ArrayCreator node, Node parent, Consumer<ArrayCreator> replacer) {
		return true;
	}

	@Override
	public <T extends AnnotationValue> boolean visitArrayInitializer(ArrayInitializer<T> node, Node parent,
																	 Consumer<ArrayInitializer<T>> replacer) {
		return true;
	}

	@Override
	public boolean visitArrayType(ArrayType node, Node parent, Consumer<ArrayType> replacer) {
		return true;
	}

	@Override
	public boolean visitAssertStmt(AssertStmt node, Node parent, Consumer<AssertStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitAssignExpr(AssignExpr node, Node parent, Consumer<AssignExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitBinaryExpr(BinaryExpr node, Node parent, Consumer<BinaryExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitBlock(Block node, Node parent, Consumer<Block> replacer) {
		return true;
	}

	@Override
	public boolean visitBreakStmt(BreakStmt node, Node parent, Consumer<BreakStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitCastExpr(CastExpr node, Node parent, Consumer<CastExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitCatch(Catch node, Node parent, Consumer<Catch> replacer) {
		return true;
	}

	@Override
	public boolean visitClassCreator(ClassCreator node, Node parent, Consumer<ClassCreator> replacer) {
		return true;
	}

	@Override
	public boolean visitClassDecl(ClassDecl node, Node parent, Consumer<ClassDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitClassInitializer(ClassInitializer node, Node parent, Consumer<ClassInitializer> replacer) {
		return true;
	}

	@Override
	public boolean visitClassLiteral(ClassLiteral node, Node parent, Consumer<ClassLiteral> replacer) {
		return true;
	}

	@Override
	public boolean visitConditionalExpr(ConditionalExpr node, Node parent, Consumer<ConditionalExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitConstructorCall(ConstructorCall node, Node parent, Consumer<ConstructorCall> replacer) {
		return true;
	}

	@Override
	public boolean visitConstructorDecl(ConstructorDecl node, Node parent, Consumer<ConstructorDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitContinueStmt(ContinueStmt node, Node parent, Consumer<ContinueStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitDimension(Dimension node, Node parent, Consumer<Dimension> replacer) {
		return true;
	}

	@Override
	public boolean visitDoStmt(DoStmt node, Node parent, Consumer<DoStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitEmptyStmt(EmptyStmt node, Node parent, Consumer<EmptyStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitEnumDecl(EnumDecl node, Node parent, Consumer<EnumDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitEnumField(EnumField node, Node parent, Consumer<EnumField> replacer) {
		return true;
	}

	@Override
	public boolean visitExportsDirective(ExportsDirective node, Node parent, Consumer<ExportsDirective> replacer) {
		return true;
	}

	@Override
	public boolean visitExpressionStmt(ExpressionStmt node, Node parent, Consumer<ExpressionStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitForEachStmt(ForEachStmt node, Node parent, Consumer<ForEachStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitForStmt(ForStmt node, Node parent, Consumer<ForStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitFormalParameter(FormalParameter node, Node parent, Consumer<FormalParameter> replacer) {
		return true;
	}

	@Override
	public boolean visitFunctionCall(FunctionCall node, Node parent, Consumer<FunctionCall> replacer) {
		return true;
	}

	@Override
	public boolean visitFunctionDecl(FunctionDecl node, Node parent, Consumer<FunctionDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitGenericType(GenericType node, Node parent, Consumer<GenericType> replacer) {
		return true;
	}

	@Override
	public boolean visitIfStmt(IfStmt node, Node parent, Consumer<IfStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitImportDecl(ImportDecl node, Node parent, Consumer<ImportDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitIndexExpr(IndexExpr node, Node parent, Consumer<IndexExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitInformalParameter(InformalParameter node, Node parent, Consumer<InformalParameter> replacer) {
		return true;
	}

	@Override
	public boolean visitInterfaceDecl(InterfaceDecl node, Node parent, Consumer<InterfaceDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitLabeledStmt(LabeledStmt node, Node parent, Consumer<LabeledStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitLambda(Lambda node, Node parent, Consumer<Lambda> replacer) {
		return true;
	}

	@Override
	public boolean visitLiteral(Literal node, Node parent, Consumer<Literal> replacer) {
		return true;
	}

	@Override
	public boolean visitMemberAccess(MemberAccess node, Node parent, Consumer<MemberAccess> replacer) {
		return true;
	}

	@Override
	public boolean visitMethodReference(MethodReference node, Node parent, Consumer<MethodReference> replacer) {
		return true;
	}

	@Override
	public boolean visitModifier(Modifier node, Node parent, Consumer<Modifier> replacer) {
		return false;
	}

	@Override
	public boolean visitModuleCompilationUnit(ModuleCompilationUnit node, Node parent,
											  Consumer<ModuleCompilationUnit> replacer) {
		return true;
	}

	@Override
	public boolean visitName(Name node, Node parent, Consumer<Name> replacer) {
		return false;
	}

	@Override
	public boolean visitNormalCompilationUnit(NormalCompilationUnit node, Node parent,
											  Consumer<NormalCompilationUnit> replacer) {
		return true;
	}

	@Override
	public boolean visitOpensDirective(OpensDirective node, Node parent, Consumer<OpensDirective> replacer) {
		return true;
	}

	@Override
	public boolean visitPackageDecl(PackageDecl node, Node parent, Consumer<PackageDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitParensExpr(ParensExpr node, Node parent, Consumer<ParensExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitPostIncrementExpr(PostIncrementExpr node, Node parent, Consumer<PostIncrementExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitPostDecrementExpr(PostDecrementExpr node, Node parent, Consumer<PostDecrementExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitPreIncrementExpr(PreIncrementExpr node, Node parent, Consumer<PreIncrementExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitPreDecrementExpr(PreDecrementExpr node, Node parent, Consumer<PreDecrementExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitPrimitiveType(PrimitiveType node, Node parent, Consumer<PrimitiveType> replacer) {
		return true;
	}

	@Override
	public boolean visitProvidesDirective(ProvidesDirective node, Node parent, Consumer<ProvidesDirective> replacer) {
		return true;
	}

	@Override
	public boolean visitQualifiedName(QualifiedName node, Node parent, Consumer<QualifiedName> replacer) {
		return false;
	}

	@Override
	public boolean visitRequiresDirective(RequiresDirective node, Node parent, Consumer<RequiresDirective> replacer) {
		return true;
	}

	@Override
	public boolean visitReturnStmt(ReturnStmt node, Node parent, Consumer<ReturnStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitSize(Size node, Node parent, Consumer<Size> replacer) {
		return true;
	}

	@Override
	public boolean visitSuperFunctionCall(SuperFunctionCall node, Node parent, Consumer<SuperFunctionCall> replacer) {
		return true;
	}

	@Override
	public boolean visitSuperMethodReference(SuperMethodReference node, Node parent,
											 Consumer<SuperMethodReference> replacer) {
		return true;
	}

	@Override
	public boolean visitSwitch(Switch node, Node parent, Consumer<Switch> replacer) {
		return true;
	}

	@Override
	public boolean visitSwitchCase(SwitchCase node, Node parent, Consumer<SwitchCase> replacer) {
		return true;
	}

	@Override
	public boolean visitSynchronizedStmt(SynchronizedStmt node, Node parent, Consumer<SynchronizedStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitThis(This node, Node parent, Consumer<This> replacer) {
		return true;
	}

	@Override
	public boolean visitThisParameter(ThisParameter node, Node parent, Consumer<ThisParameter> replacer) {
		return true;
	}

	@Override
	public boolean visitThrowStmt(ThrowStmt node, Node parent, Consumer<ThrowStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitTryStmt(TryStmt node, Node parent, Consumer<TryStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitTypeUnion(TypeUnion node, Node parent, Consumer<TypeUnion> replacer) {
		return true;
	}

	@Override
	public boolean visitTypeIntersection(TypeIntersection node, Node parent, Consumer<TypeIntersection> replacer) {
		return true;
	}

	@Override
	public boolean visitTypeParameter(TypeParameter node, Node parent, Consumer<TypeParameter> replacer) {
		return true;
	}

	@Override
	public boolean visitTypeTest(TypeTest node, Node parent, Consumer<TypeTest> replacer) {
		return true;
	}

	@Override
	public boolean visitUnaryExpr(UnaryExpr node, Node parent, Consumer<UnaryExpr> replacer) {
		return true;
	}

	@Override
	public boolean visitUsesDirective(UsesDirective node, Node parent, Consumer<UsesDirective> replacer) {
		return true;
	}

	@Override
	public boolean visitVariable(Variable node, Node parent, Consumer<Variable> replacer) {
		return true;
	}

	@Override
	public boolean visitVariableDecl(VariableDecl node, Node parent, Consumer<VariableDecl> replacer) {
		return true;
	}

	@Override
	public boolean visitVariableDeclarator(VariableDeclarator node, Node parent,
										   Consumer<VariableDeclarator> replacer) {
		return true;
	}

	@Override
	public boolean visitVoidType(VoidType node, Node parent, Consumer<VoidType> replacer) {
		return true;
	}

	@Override
	public boolean visitWhileStmt(WhileStmt node, Node parent, Consumer<WhileStmt> replacer) {
		return true;
	}

	@Override
	public boolean visitWildcardTypeArgument(WildcardTypeArgument node, Node parent,
											 Consumer<WildcardTypeArgument> replacer) {
		return true;
	}

	@Override
	public boolean visitWildcardTypeArgumentBound(Bound node, Node parent, Consumer<Bound> replacer) {
		return true;
	}

	@Override
	public boolean visitYieldStmt(YieldStmt node, Node parent, Consumer<YieldStmt> replacer) {
		return true;
	}

}
