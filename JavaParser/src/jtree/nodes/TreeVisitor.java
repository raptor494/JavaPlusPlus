package jtree.nodes;

import java.util.function.Consumer;

public interface TreeVisitor {
	default boolean visitNode(Node node, Node parent, Consumer<Node> replacer) {
		throw new IllegalArgumentException("Unsupported Node type: " + node.getClass().getSimpleName());
	}
	
	boolean visitAnnotation(Annotation node, Node parent, Consumer<Annotation> replacer);

	boolean visitAnnotationArgument(AnnotationArgument node, Node parent, Consumer<AnnotationArgument> replacer);

	boolean visitAnnotationDecl(AnnotationDecl node, Node parent, Consumer<AnnotationDecl> replacer);

	boolean visitAnnotationProperty(AnnotationProperty node, Node parent, Consumer<AnnotationProperty> replacer);

	boolean visitArrayCreator(ArrayCreator node, Node parent, Consumer<ArrayCreator> replacer);

	<T extends AnnotationValue> boolean visitArrayInitializer(ArrayInitializer<T> node, Node parent, Consumer<ArrayInitializer<T>> replacer);

	boolean visitArrayType(ArrayType node, Node parent, Consumer<ArrayType> replacer);

	boolean visitAssertStmt(AssertStmt node, Node parent, Consumer<AssertStmt> replacer);

	boolean visitAssignExpr(AssignExpr node, Node parent, Consumer<AssignExpr> replacer);

	boolean visitBinaryExpr(BinaryExpr node, Node parent, Consumer<BinaryExpr> replacer);

	boolean visitBlock(Block node, Node parent, Consumer<Block> replacer);

	boolean visitBreakStmt(BreakStmt node, Node parent, Consumer<BreakStmt> replacer);

	boolean visitCastExpr(CastExpr node, Node parent, Consumer<CastExpr> replacer);

	boolean visitCatch(Catch node, Node parent, Consumer<Catch> replacer);

	boolean visitClassCreator(ClassCreator node, Node parent, Consumer<ClassCreator> replacer);

	boolean visitClassDecl(ClassDecl node, Node parent, Consumer<ClassDecl> replacer);

	boolean visitClassInitializer(ClassInitializer node, Node parent, Consumer<ClassInitializer> replacer);

	boolean visitClassLiteral(ClassLiteral node, Node parent, Consumer<ClassLiteral> replacer);

	boolean visitConditionalExpr(ConditionalExpr node, Node parent, Consumer<ConditionalExpr> replacer);

	boolean visitConstructorCall(ConstructorCall node, Node parent, Consumer<ConstructorCall> replacer);

	boolean visitConstructorDecl(ConstructorDecl node, Node parent, Consumer<ConstructorDecl> replacer);

	boolean visitContinueStmt(ContinueStmt node, Node parent, Consumer<ContinueStmt> replacer);

	boolean visitDimension(Dimension node, Node parent, Consumer<Dimension> replacer);

	boolean visitDoStmt(DoStmt node, Node parent, Consumer<DoStmt> replacer);

	boolean visitEmptyStmt(EmptyStmt node, Node parent, Consumer<EmptyStmt> replacer);

	boolean visitEnumDecl(EnumDecl node, Node parent, Consumer<EnumDecl> replacer);

	boolean visitEnumField(EnumField node, Node parent, Consumer<EnumField> replacer);

	boolean visitExportsDirective(ExportsDirective node, Node parent, Consumer<ExportsDirective> replacer);

	boolean visitExpressionStmt(ExpressionStmt node, Node parent, Consumer<ExpressionStmt> replacer);

	boolean visitForEachStmt(ForEachStmt node, Node parent, Consumer<ForEachStmt> replacer);

	boolean visitForStmt(ForStmt node, Node parent, Consumer<ForStmt> replacer);

	boolean visitFormalParameter(FormalParameter node, Node parent, Consumer<FormalParameter> replacer);

	boolean visitFunctionCall(FunctionCall node, Node parent, Consumer<FunctionCall> replacer);

	boolean visitFunctionDecl(FunctionDecl node, Node parent, Consumer<FunctionDecl> replacer);

	boolean visitGenericType(GenericType node, Node parent, Consumer<GenericType> replacer);

	boolean visitIfStmt(IfStmt node, Node parent, Consumer<IfStmt> replacer);

	boolean visitImportDecl(ImportDecl node, Node parent, Consumer<ImportDecl> replacer);

	boolean visitIndexExpr(IndexExpr node, Node parent, Consumer<IndexExpr> replacer);

	boolean visitInformalParameter(InformalParameter node, Node parent, Consumer<InformalParameter> replacer);

	boolean visitInterfaceDecl(InterfaceDecl node, Node parent, Consumer<InterfaceDecl> replacer);

	boolean visitLabeledStmt(LabeledStmt node, Node parent, Consumer<LabeledStmt> replacer);

	boolean visitLambda(Lambda node, Node parent, Consumer<Lambda> replacer);

	boolean visitLiteral(Literal node, Node parent, Consumer<Literal> replacer);

	boolean visitMemberAccess(MemberAccess node, Node parent, Consumer<MemberAccess> replacer);

	boolean visitMethodReference(MethodReference node, Node parent, Consumer<MethodReference> replacer);

	boolean visitModifier(Modifier node, Node parent, Consumer<Modifier> replacer);

	boolean visitModuleCompilationUnit(ModuleCompilationUnit node, Node parent, Consumer<ModuleCompilationUnit> replacer);

	boolean visitName(Name node, Node parent, Consumer<Name> replacer);

	boolean visitNormalCompilationUnit(NormalCompilationUnit node, Node parent, Consumer<NormalCompilationUnit> replacer);

	boolean visitOpensDirective(OpensDirective node, Node parent, Consumer<OpensDirective> replacer);

	boolean visitPackageDecl(PackageDecl node, Node parent, Consumer<PackageDecl> replacer);

	boolean visitParensExpr(ParensExpr node, Node parent, Consumer<ParensExpr> replacer);

	boolean visitPostIncrementExpr(PostIncrementExpr node, Node parent, Consumer<PostIncrementExpr> replacer);

	boolean visitPostDecrementExpr(PostDecrementExpr node, Node parent, Consumer<PostDecrementExpr> replacer);

	boolean visitPreIncrementExpr(PreIncrementExpr node, Node parent, Consumer<PreIncrementExpr> replacer);

	boolean visitPreDecrementExpr(PreDecrementExpr node, Node parent, Consumer<PreDecrementExpr> replacer);

	boolean visitPrimitiveType(PrimitiveType node, Node parent, Consumer<PrimitiveType> replacer);

	boolean visitProvidesDirective(ProvidesDirective node, Node parent, Consumer<ProvidesDirective> replacer);

	boolean visitQualifiedName(QualifiedName node, Node parent, Consumer<QualifiedName> replacer);

	boolean visitRequiresDirective(RequiresDirective node, Node parent, Consumer<RequiresDirective> replacer);

	boolean visitReturnStmt(ReturnStmt node, Node parent, Consumer<ReturnStmt> replacer);

	boolean visitSize(Size node, Node parent, Consumer<Size> replacer);

	boolean visitSuperFunctionCall(SuperFunctionCall node, Node parent, Consumer<SuperFunctionCall> replacer);

	boolean visitSuperMethodReference(SuperMethodReference node, Node parent, Consumer<SuperMethodReference> replacer);

	boolean visitSwitch(Switch node, Node parent, Consumer<Switch> replacer);

	boolean visitSwitchCase(SwitchCase node, Node parent, Consumer<SwitchCase> replacer);

	boolean visitSynchronizedStmt(SynchronizedStmt node, Node parent, Consumer<SynchronizedStmt> replacer);

	boolean visitThis(This node, Node parent, Consumer<This> replacer);

	boolean visitThisParameter(ThisParameter node, Node parent, Consumer<ThisParameter> replacer);

	boolean visitThrowStmt(ThrowStmt node, Node parent, Consumer<ThrowStmt> replacer);

	boolean visitTryStmt(TryStmt node, Node parent, Consumer<TryStmt> replacer);

	boolean visitTypeUnion(TypeUnion node, Node parent, Consumer<TypeUnion> replacer);

	boolean visitTypeIntersection(TypeIntersection node, Node parent, Consumer<TypeIntersection> replacer);

	boolean visitTypeParameter(TypeParameter node, Node parent, Consumer<TypeParameter> replacer);

	boolean visitTypeTest(TypeTest node, Node parent, Consumer<TypeTest> replacer);

	boolean visitUnaryExpr(UnaryExpr node, Node parent, Consumer<UnaryExpr> replacer);

	boolean visitUsesDirective(UsesDirective node, Node parent, Consumer<UsesDirective> replacer);

	boolean visitVariable(Variable node, Node parent, Consumer<Variable> replacer);

	boolean visitVariableDecl(VariableDecl node, Node parent, Consumer<VariableDecl> replacer);

	boolean visitVariableDeclarator(VariableDeclarator node, Node parent, Consumer<VariableDeclarator> replacer);

	boolean visitVoidType(VoidType node, Node parent, Consumer<VoidType> replacer);

	boolean visitWhileStmt(WhileStmt node, Node parent, Consumer<WhileStmt> replacer);

	boolean visitWildcardTypeArgument(WildcardTypeArgument node, Node parent, Consumer<WildcardTypeArgument> replacer);
	
	boolean visitWildcardTypeArgumentBound(WildcardTypeArgument.Bound node, Node parent, Consumer<WildcardTypeArgument.Bound> replacer);

	boolean visitYieldStmt(YieldStmt node, Node parent, Consumer<YieldStmt> replacer);
}
