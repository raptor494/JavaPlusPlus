import io
import java.tree as tree
from java.parser import JavaParser, JavaSyntaxError, parse_file as java_parse_file, parse_str as java_parse_str
from java.tokenize import *
from typeguard import check_type, check_argument_types
from typing import Union, List, Optional, Type, Tuple, Set

class JavaPlusPlusParser(JavaParser):
    #region init
    supported_features = {
        'statements.print', 'expressions.class_creator', 'literals.collections', 'trailing_commas.argument', 'trailing_commas.other',
        'syntax.argument_annotations', 'auto_imports.types', 'auto_imports.statics', 'syntax.multiple_import_sections',
        'literals.optional', 'syntax.default_arguments', 'expressions.vardecl', 'expressions.elvisoperator', 'expressions.equalityoperator',
        'syntax.default_modifiers', 'syntax.empty_class_body'
    }
                                    
    auto_imports = {
        'java.util': {
            'List', 'Set', 'Map', 'ArrayList', 'HashSet', 'HashMap',
            'EnumSet', 'Collection', 'Iterator', 'Collections', 'Arrays',
            'Calendar', 'Date', 'EnumMap', 'GregorianCalendar', 'Locale',
            'Objects', 'Optional', 'OptionalDouble', 'OptionalInt', 'OptionalLong',
            'Properties', 'Random', 'Scanner', 'Spliterators', 'Spliterator', 'Timer',
            'SimpleTimeZone', 'TimeZone', 'UUID', 'ConcurrentModificationException',
            'NoSuchElementException'
        },
        'java.util.stream': {
            'Collector', 'DoubleStream', 'IntStream', 'LongStream', 'Stream',
            'Collectors', 'StreamSupport'
        },
        'java.io': {
            'Closeable', 'Serializable', 'BufferedInputStream', 'BufferedOutputStream', 'BufferedReader',
            'BufferedWriter', 'ByteArrayInputStream', 'ByteArrayOutputStream', 'CharArrayReader', 'CharArrayWriter',
            'Console', 'File', 'FileInputStream', 'FileOutputStream', 'FileReader', 'FileWriter', 'InputStream',
            'InputStreamReader', 'OutputStream', 'OutputStreamWriter', 'PrintStream', 'PrintWriter', 'Reader',
            'Writer', 'StringReader', 'StringWriter', 'FileNotFoundException', 'IOException', 'IOError'
        },
        'java.nio.file': {
            'Path', 'Files', 'Paths', 'StandardCopyOption', 'StandardOpenOption',
        },
        'java.math': {
            'BigDecimal', 'BigInteger', 'MathContext', 'RoundingMode'
        }, 
        'java.nio.charset': {
            'StandardCharsets'
        },
        'java.util.concurrent': {
            'Callable', 'Executors', 'TimeUnit'
        },
        'java.util.function': '*',
        'java.util.regex': {
            'Pattern'
        }
    }

    static_imports = {
        'java.lang': {
            'Boolean': {'parseBoolean'},
            'Byte': {'parseByte'},
            'Double': {'parseDouble'},
            'Float': {'parseFloat'},
            'Integer': {'parseInt', 'parseUnsignedInt'},
            'Long': {'parseLong', 'parseUnsignedLong'},
            'Short': {'parseShort'},
            'String': {'format', 'join'},
        }
    }

    def __init__(self, tokens, filename='<unknown source>'):
        super().__init__(tokens, filename)
        self.print_statements = True
        self.collections_literals = True
        self.class_creator_expressions = True
        self.argument_trailing_commas = True
        self.other_trailing_commas = False
        self.argument_annotations_syntax = True
        self.types_auto_imports = False
        self.statics_auto_imports = False
        self.multiple_import_sections_syntax = True
        self.optional_literals = True
        self.default_arguments_syntax = True
        self.vardecl_expressions = True
        self.elvisoperator_expressions = True
        self.equalityoperator_expressions = False
        self.default_modifiers_syntax = True
        self.empty_class_body_syntax = True

    #endregion init

    def enable_feature(self, feature: str, enabled: bool=True):
        enabled = bool(enabled)
        if feature == '*':
            for feature in type(self).supported_features:
                setattr(self, type(self).feature_name_to_attr(feature), enabled)
        elif feature in type(self).supported_features:
            setattr(self, type(self).feature_name_to_attr(feature), enabled)
        elif feature.endswith('.*'):
            prefix = feature[0:-2]
            found = False
            for feature in type(self).supported_features:
                if feature.startswith(prefix):
                    setattr(self, type(self).feature_name_to_attr(feature), enabled)
                    found = True
            if not found:
                raise ValueError(f"unsupported category '{prefix}'")
        else:
            raise ValueError(f"unsupported feature '{feature}'")
        return self

    @classmethod
    def feature_name_to_attr(cls, name: str) -> str:
        strs = name.split('.')
        return '_'.join(reversed(strs))

    #region Declarations
    def parse_import_section(self) -> List[tree.Import]:
        imports = []
        while True:
            if self.would_accept('import'):
                imports.extend(self.parse_import_declarations())
            elif self.would_accept('from'):
                imports.extend(self.parse_from_import_declarations())
            elif self.would_accept('unimport'):
                self.parse_unimport(imports)
            elif not self.accept(';'):
                break

        auto_import_index = 0

        if self.types_auto_imports:
            for package, imported_types in type(self).auto_imports.items():
                if imported_types == '*':
                    found = False
                    for _import in imports:
                        if not _import.static and _import.imported_package == package and _import.wildcard:
                            found = True
                            break
                    if not found:
                        imports.insert(0, tree.Import(name=tree.Name(package), wildcard=True))
                        auto_import_index += 1
                else:
                    for imported_type in imported_types:
                        found = 0
                        for _import in imports:
                            if not _import.static:
                                if _import.imported_package == package and _import.wildcard:
                                    found = 2
                                    break
                                elif _import.imported_type == imported_type:
                                    found = 1
                                    break
                        if found:
                            if found == 2:
                                break # wildcard imported package, skip this one
                        else:
                            imports.insert(0, tree.Import(name=tree.Name(package + '.' + imported_type)))
                            auto_import_index += 1

        if self.statics_auto_imports:
            for package, types in type(self).static_imports.items():
                for typename, members in types.items():
                    if members == '*':
                        found = False
                        for _import in imports:
                            if _import.static and _import.wildcard and _import.imported_package == package and _import.imported_type == typename:
                                found = True
                                break
                        if not found:
                            imports.insert(0, tree.Import(name=tree.Name(package + '.' + typename), static=True, wildcard=True))
                            auto_import_index += 1
                    else:
                        for member in members:
                            found = 0
                            for _import in imports:
                                if _import.static:
                                    if _import.imported_package == package and _import.imported_type == typename and _import.wildcard:
                                        found = 2
                                        break
                                    elif _import.imported_name == member:
                                        found = 1
                                        break
                            if found:
                                if found == 2:
                                    break # wildcard imported member, skip this one
                            else:
                                imports.insert(0, tree.Import(name=tree.Name(package + '.' + typename + '.' + member), static=True))                
                                auto_import_index += 1

        if auto_import_index:
            from functools import cmp_to_key
            def import_cmp(i1, i2):
                if i1.static and not i2.static:
                    return -1
                if not i1.static and i2.static:
                    return 1
                if i1.wildcard and not i2.wildcard:
                    return -1
                if not i1.wildcard and i2.wildcard:
                    return 1
                i1 = str(i1.name)
                i2 = str(i2.name)
                if i1 == i2:
                    return 0
                elif i1 < i2:
                    return -1
                else:
                    return 1
                
            imports[0:auto_import_index] = sorted(imports[0:auto_import_index], key=cmp_to_key(import_cmp))

        return imports

    def parse_import_declarations(self) -> List[tree.Import]:
        imports = []
        self.require('import')
        static = bool(self.accept('static'))
        while True:
            if not static and self.accept('java', '++'):
                if self.accept('.', '*'):
                    feature = '*'
                else:
                    self.require('.')
                    feature = self.parse_ident()
                    while self.accept('.'):
                        if self.accept('*'):
                            feature += '.*'
                            break
                        else:
                            feature += '.' + self.parse_ident()
                self.enable_feature(feature)
            else:
                name, wildcard = self.parse_import_name()
                imports.append(tree.Import(name=name, static=static, wildcard=wildcard))
            if not self.accept(','):
                break
            if self.other_trailing_commas and self.would_accept(';'):
                break
        self.require(';')
        return imports

    def parse_unimport(self, imports: List[tree.Import]):
        self.require('unimport')
        self.require('java', '++')
        while True:
            if self.accept('.', '*'):
                feature = '*'
            else:
                self.require('.')
                feature = self.parse_ident()
                while self.accept('.'):
                    if self.accept('*'):
                        feature += '.*'
                        break
                    else:
                        feature += '.' + self.parse_ident()
            self.enable_feature(feature, False)
            if not self.accept(','):
                break
            if self.other_trailing_commas and self.would_accept(';'):
                break
        return []

    def parse_from_import_declarations(self) -> List[tree.Import]:
        self.require('from')
        imports = []

        if self.would_accept('java', '++'): # 'from java++ import ...' / 'from java++ unimport ...' statement, allows modifying syntax
            self.next() # skips past the 'java' token
            self.next() # skips past the '++' token

            base_feature = ''
            if self.accept('.'):
                base_feature = self.parse_ident()
                while self.accept('.'):
                    base_feature += '.' + self.parse_ident()
                base_feature += '.'

            if self.accept('import'):
                enable_feature = True
            else:
                self.require('unimport')
                enable_feature = False

            if self.accept('*'):
                self.enable_feature(base_feature + '*', enable_feature)

            else:
                while True:
                    start_pos = self.position()
                    feature = base_feature + self.parse_ident()
                    while self.accept('.'):
                        if self.accept('*'):
                            feature += '*'
                            break
                        else:
                            feature += '.' + self.parse_ident()
                    try:
                        self.enable_feature(feature, enable_feature)
                    except ValueError as e:
                        raise JavaSyntaxError(str(e), at=start_pos)
                    if not self.accept(','):
                        break
                    if self.other_trailing_commas and self.would_accept(';'):
                        break
        else:
            base = self.parse_qual_name()
            self.require('import')
            static = bool(self.accept('static'))

            name, wildcard = self.parse_from_import_name(base)
            imports.append(tree.Import(name=name, static=static, wildcard=wildcard))

            while self.accept(','):
                if self.other_trailing_commas and self.would_accept(';'):
                    break
                name, wildcard = self.parse_from_import_name(base)
                imports.append(tree.Import(name=name, static=static, wildcard=wildcard))

        self.require(';')

        return imports

    def parse_from_import_name(self, base_name):
        if self.accept('*'):
            return base_name, True
        else:
            base_name += self.parse_qual_name()
            wildcard = bool(self.accept('.', '*'))
            return base_name, wildcard
    
    def parse_type_declarations(self, doc=None, modifiers=None, annotations=None, imports: List[tree.Import]=None) -> List[tree.TypeDeclaration]:
        if self.default_modifiers_syntax and (modifiers or annotations) and self.accept(':'):
            base_annotations = annotations
            base_modifiers = modifiers
            annotations = None
            modifiers = None
            doc = None
        else:
            base_annotations = []
            base_modifiers = []
        decl = self.parse_type_declaration(doc, modifiers, annotations)
        tree.Modifier.merge(decl.modifiers, base_modifiers)
        tree.Annotation.merge(decl.annotations, base_annotations)
        types = [decl]
        while self.token.type != ENDMARKER:
            if not self.accept(';'):
                if self.multiple_import_sections_syntax and imports is not None and self.would_accept(('from', 'import', 'unimport')):
                    imports.extend(self.parse_import_section())
                else:
                    doc = self.doc
                    modifiers, annotations = self.parse_mods_and_annotations()
                    if self.default_modifiers_syntax and (modifiers or annotations) and self.accept(':'):
                        base_annotations = annotations
                        base_modifiers = modifiers
                        annotations = None
                        modifiers = None
                        doc = None
                    decl = self.parse_type_declaration(doc, modifiers, annotations)
                    tree.Modifier.merge(decl.modifiers, base_modifiers)
                    tree.Annotation.merge(decl.annotations, base_annotations)
                    types.append(decl)
        return types

    def parse_mods_and_annotations(self, newlines=True):
        modifiers = []
        annotations = []
        while True:
            if self.would_accept('@') and not self.would_accept('@', 'interface'):
                annotations.append(self.parse_annotation())
            elif self.would_accept(tree.Modifier.VALUES):
                if self.would_accept('package', NAME, ('.', ';')):
                    break
                modifiers.append(tree.Modifier(self.token.string))
                self.next()
            elif self.would_accept('non', '-', tree.Modifier.VALUES) and self.tokens.look(2).string not in tree.Modifier.VISIBILITY:
                next1 = self.tokens.look(1)
                next2 = self.tokens.look(2)
                if next1.start == self.token.end and next2.start == next1.end:
                    self.next(); self.next()
                    modifiers.append(tree.Modifier('non-' + self.token.string))
                    self.next()
                    continue
                else:
                    break
            else:
                break
        return modifiers, annotations

    def parse_method_rest(self, *, return_type, name, typeparams=None, doc=None, modifiers=[], annotations=[]):
        params = self.parse_parameters()
        if self.would_accept('[') or self.would_accept('@'):
            dimensions = self.parse_dimensions()
            if isinstance(return_type, tree.ArrayType):
                return_type.dimensions += dimensions
            else:
                return_type = tree.ArrayType(return_type, dimensions)
        throws = self.parse_generic_type_list() if self.accept('throws') else []
        if self.would_accept('{'):
            body = self.parse_function_body()
        else:
            self.require(';')
            body = None

        funcs = [tree.FunctionDeclaration(name=name, return_type=return_type, params=params, throws=throws, body=body, doc=doc, modifiers=modifiers, annotations=annotations)] 

        if not self.default_arguments_syntax:
            return funcs

        for i, param in enumerate(params):
            if param.default is not None:
                break
        else:
            return funcs

        def make_args(start_idx):
            return [tree.MemberAccess(name=param.name.copy()) if param.default is None or i+1 < start_idx else param.default.copy() for i, param in enumerate(params[0:start_idx])]

        def make_params(start_idx):
            results = []
            for i in range(start_idx):
                p = params[i].copy()
                p.default = None
                results.append(p)
            return results

        if params[-1].variadic:
            if params[-1].default is not None:
                for idx in range(i, len(params)):
                    call = tree.FunctionCall(name=name.copy(), args=make_args(idx+1))
                    body = tree.Block(stmts=[tree.ExpressionStatement(call) if isinstance(return_type, tree.VoidType) else tree.ReturnStatement(call)])
                    funcs.append(tree.FunctionDeclaration(name=name.copy(), return_type=return_type.copy(), params=make_params(idx), throws=[exc.copy() for exc in throws], body=body, doc=doc, modifiers=[mod.copy() for mod in modifiers], annotations=[anno.copy() for anno in annotations]))
            for idx in range(i, len(params)-1):
                args = make_args(idx+1)
                args.append(tree.MemberAccess(name=params[-1].name.copy()))
                call = tree.FunctionCall(name=name.copy(), args=args)
                body = tree.Block(stmts=[tree.ExpressionStatement(call) if isinstance(return_type, tree.VoidType) else tree.ReturnStatement(call)])
                params2 = make_params(idx)
                params2.append(params[-1].copy())
                funcs.append(tree.FunctionDeclaration(name=name.copy(), return_type=return_type.copy(), params=params2, throws=[exc.copy() for exc in throws], body=body, doc=doc, modifiers=[mod.copy() for mod in modifiers], annotations=[anno.copy() for anno in annotations]))    
        else:
            for idx in range(i, len(params)):
                call = tree.FunctionCall(name=name.copy(), args=make_args(idx+1))
                body = tree.Block(stmts=[tree.ExpressionStatement(call) if isinstance(return_type, tree.VoidType) else tree.ReturnStatement(call)])
                funcs.append(tree.FunctionDeclaration(name=name.copy(), return_type=return_type.copy(), params=make_params(idx), throws=[exc.copy() for exc in throws], body=body, doc=doc, modifiers=[mod.copy() for mod in modifiers], annotations=[anno.copy() for anno in annotations]))

        return funcs

    def parse_constructor_rest(self, *, name, typeparams=None, doc=None, modifiers=[], annotations=[]):
        params = self.parse_parameters()
        throws = self.parse_generic_type_list() if self.accept('throws') else []
        body = self.parse_function_body()
        funcs =  [tree.ConstructorDeclaration(name=name, params=params, throws=throws, body=body, doc=doc, modifiers=modifiers, annotations=annotations)]

        if not self.default_arguments_syntax:
            return funcs

        for i, param in enumerate(params):
            if param.default is not None:
                break
        else:
            return funcs

        def make_args(start_idx):
            return [tree.MemberAccess(name=param.name.copy()) if param.default is None or i+1 < start_idx else param.default.copy() for i, param in enumerate(params[0:start_idx])]

        def make_params(start_idx):
            results = []
            for i in range(start_idx):
                p = params[i].copy()
                p.default = None
                results.append(p)
            return results

        if params[-1].variadic:
            if params[-1].default is not None:
                for idx in range(i, len(params)):
                    call = tree.ThisCall(args=make_args(idx+1))
                    body = tree.Block(stmts=[tree.ExpressionStatement(call)])
                    funcs.append(tree.ConstructorDeclaration(name=name.copy(), params=make_params(idx), throws=[exc.copy() for exc in throws], body=body, doc=doc, modifiers=[mod.copy() for mod in modifiers], annotations=[anno.copy() for anno in annotations]))
            for idx in range(i, len(params)-1):
                args = make_args(idx+1)
                args.append(tree.MemberAccess(name=params[-1].name.copy()))
                call = tree.ThisCall(args=args)
                body = tree.Block(stmts=[tree.ExpressionStatement(call)])
                params2 = make_params(idx)
                params2.append(params[-1].copy())
                funcs.append(tree.ConstructorDeclaration(name=name.copy(), params=params2, throws=[exc.copy() for exc in throws], body=body, doc=doc, modifiers=[mod.copy() for mod in modifiers], annotations=[anno.copy() for anno in annotations]))    
        else:
            for idx in range(i, len(params)):
                call = tree.FunctionCall(name=name.copy(), args=make_args(idx+1))
                body = tree.Block(stmts=[tree.ExpressionStatement(call)])
                funcs.append(tree.ConstructorDeclaration(name=name.copy(), params=make_params(idx), throws=[exc.copy() for exc in throws], body=body, doc=doc, modifiers=[mod.copy() for mod in modifiers], annotations=[anno.copy() for anno in annotations]))

        return funcs

    def parse_field_rest(self, *, var_type, name, doc=None, modifiers=[], annotations=[], require_init=False):
        declarators = [self.parse_declarator_rest(name, require_init, array=isinstance(var_type, tree.ArrayType))]
        while self.accept(','):
            if self.other_trailing_commas and self.would_accept(';'):
                break
            declarators.append(self.parse_declarator(require_init, array=isinstance(var_type, tree.ArrayType)))
        self.require(';')
        return tree.FieldDeclaration(type=var_type, declarators=declarators, doc=doc, modifiers=modifiers, annotations=annotations)

    def parse_parameters(self, allow_this=True):
        self.require('(')
        if self.would_accept(')'):
            params = []
        else:
            if allow_this:
                param = self.parse_parameter_opt_this(req_default=False, allow_default=allow_this)
            else:
                param = self.parse_parameter(req_default=False, allow_default=allow_this)
            params = [param]
            if not param.variadic:
                while self.accept(','):
                    if self.argument_trailing_commas and self.would_accept(')'):
                        break
                    param = self.parse_parameter(req_default=param.default is not None)
                    params.append(param)
                    if param.variadic:
                        break
        self.require(')')
        return params

    def parse_parameter_opt_this(self, req_default=False, allow_default=True):
        modifiers, annotations = self.parse_mods_and_annotations(newlines=False)
        typ = self.parse_type(annotations=[])
        if not modifiers and self.accept('this'):
            return tree.ThisParameter(type=typ, annotations=annotations)
        else:
            variadic = bool(self.accept('...'))
            name = self.parse_name()
            if not variadic and not modifiers and self.accept('.', 'this'):
                return tree.ThisParameter(type=typ, annotations=annotations, qualifier=name)
            dimensions = self.parse_dimensions_opt()
            if allow_default and self.default_arguments_syntax and (req_default and not variadic and self.require('=') or self.accept('=')):
                default = self.parse_initializer(isinstance(typ, tree.ArrayType) or dimensions)
                if isinstance(default, tree.ArrayInitializer):
                    if variadic:
                        default = tree.ArrayCreator(type=typ, dimensions=[tree.DimensionExpression()], initializer=default)
                    else:
                        default = tree.ArrayCreator(type=typ.base, dimensions=[tree.DimensionExpression() for dim in typ.dimensions], initializer=default)
                
            else:
                default = None
            return tree.FormalParameter(type=typ, name=name, variadic=variadic, default=default, modifiers=modifiers, annotations=annotations, dimensions=dimensions)

    def parse_parameter(self, req_default=False, allow_default=True):
        modifiers, annotations = self.parse_mods_and_annotations(newlines=False)
        typ = self.parse_type(annotations=[])
        variadic = bool(self.accept('...'))
        name = self.parse_name()
        dimensions = self.parse_dimensions_opt()
        if allow_default and self.default_arguments_syntax and (req_default and not variadic and self.require('=') or self.accept('=')):
            default = self.parse_initializer(isinstance(typ, tree.ArrayType) or dimensions or variadic)
            if isinstance(default, tree.ArrayInitializer):
                if variadic:
                    default = tree.ArrayCreator(type=typ, dimensions=[tree.DimensionExpression()], initializer=default)
                else:
                    default = tree.ArrayCreator(type=typ.base, dimensions=[tree.DimensionExpression() for dim in typ.dimensions], initializer=default)
        else:
            default = None
        return tree.FormalParameter(type=typ, name=name, variadic=variadic, default=default, modifiers=modifiers, annotations=annotations, dimensions=dimensions)
    
    def parse_class_body(self, parse_member):
        if self.empty_class_body_syntax and self.accept(';'):
            return []

        self.require('{')
        members = []
        base_annotations = []
        base_modifiers = []
        while not self.would_accept(('}', ENDMARKER)):
            if not self.accept(';'):
                try:
                    with self.tokens:
                        modifiers, annotations = self.parse_mods_and_annotations()
                        if self.default_modifiers_syntax and (modifiers or annotations) and self.accept(':'):
                            base_annotations = annotations
                            base_modifiers = modifiers
                        else:
                            raise JavaSyntaxError('')
                except JavaSyntaxError:
                    pass
                members2 = self.parse_class_member()
                for member in members2:
                    if base_annotations and not isinstance(member, tree.InitializerBlock):
                        if member.annotations is None:
                            member.annotations = []
                        tree.Annotation.merge(member.annotations, base_annotations)
                    if base_modifiers and not isinstance(member, tree.InitializerBlock):
                        if member.modifiers is None:
                            member.modifiers = []
                        tree.Modifier.merge(member.modifiers, base_modifiers)
                members.extend(members2)
        self.require('}')

        return members

    def parse_enum_body(self):
        if self.empty_class_body_syntax and self.accept(';'):
            return [], []

        self.require('{')
        fields = []
        members = []

        while not self.would_accept((';', '}', ENDMARKER)):
            fields.append(self.parse_enum_field())
            if not self.accept(','):
                break
        
        if self.accept(';'):
            base_annotations = []
            base_modifiers = []
            while not self.would_accept(('}', ENDMARKER)):
                if not self.accept(';'):
                    try:
                        with self.tokens:
                            modifiers, annotations = self.parse_mods_and_annotations()
                            if self.default_modifiers_syntax and (modifiers or annotations) and self.accept(':'):
                                base_annotations = annotations
                                base_modifiers = modifiers
                            else:
                                raise JavaSyntaxError('')
                    except JavaSyntaxError:
                        pass
                    members2 = self.parse_class_member()
                    for member in members2:
                        if base_annotations and not isinstance(member, tree.InitializerBlock):
                            if member.annotations is None:
                                member.annotations = []
                            tree.Annotation.merge(member.annotations, base_annotations)
                        if base_modifiers and not isinstance(member, tree.InitializerBlock):
                            if member.modifiers is None:
                                member.modifiers = []
                            tree.Modifier.merge(member.modifiers, base_modifiers)
                    members.extend(members2)
                
        self.require('}')

        return fields, members

    #endregion Declarations

    #region Statements
    def parse_statement(self):
        if self.print_statements:
            if self.accept('println'):
                if self.accept(';'):
                    return self.make_print_statement('println')
                with self.pre_stmts:
                    elements = [self.parse_arg()]
                    if self.would_accept(','):
                        while self.accept(','):
                            if self.other_trailing_commas and self.would_accept(';'):
                                break
                            elements.append(self.parse_arg())
                    elif not self.would_accept(';'):
                        while not self.would_accept((';', ENDMARKER)):
                            elements.append(self.parse_arg())
                    self.require(';')
                    if len(elements) == 1:
                        return self.pre_stmts.apply(self.make_print_statement('println', elements[0]))
                    stmts = []
                    for i, arg in enumerate(elements):
                        if i:
                            stmts.append(self.make_print_statement('print', tree.Literal("' '")))
                        stmts.append(self.make_print_statement('print' if i+1 < len(elements) else 'println', arg))
                    return self.pre_stmts.apply(tree.Block(stmts))
            elif self.accept('print'):
                if self.accept(';'):
                    return tree.EmptyStatement()
                with self.pre_stmts:
                    elements = [self.parse_arg()]
                    if self.would_accept(','):
                        while self.accept(','):
                            if self.other_trailing_commas and self.would_accept(';'):
                                break
                            elements.append(self.parse_arg())
                    elif not self.would_accept(';'):
                        while not self.would_accept((';', ENDMARKER)):
                            elements.append(self.parse_arg())
                    self.require(';')
                    if len(elements) == 1:
                        return self.pre_stmts.apply(self.make_print_statement('print', elements[0]))
                    stmts = []
                    for i, arg in enumerate(elements):
                        if i:
                            stmts.append(self.make_print_statement('print', tree.Literal("' '")))
                        stmts.append(self.make_print_statement('print', arg))
                    return self.pre_stmts.apply(tree.Block(stmts))
            elif self.accept('printf'):
                with self.pre_stmts:
                    args = [self.parse_arg()]
                    if self.would_accept(','):
                        while self.accept(','):
                            if self.other_trailing_commas and self.would_accept(';'):
                                break
                            args.append(self.parse_arg())
                    elif not self.would_accept(';'):
                        while not self.would_accept((';', ENDMARKER)):
                            args.append(self.parse_arg())
                    self.require(';')
                    return self.pre_stmts.apply(tree.ExpressionStatement(tree.FunctionCall(name=tree.Name('printf'), args=args, object=self.make_member_access_from_dotted_name('java.lang.System.out'))))
            elif self.accept('printfln'):
                with self.pre_stmts:
                    args = [tree.BinaryExpression(lhs=self.parse_arg(), op='+', rhs=tree.Literal('"%n"'))]
                    if self.would_accept(','):
                        while self.accept(','):
                            if self.other_trailing_commas and self.would_accept(';'):
                                break
                            args.append(self.parse_arg())
                    elif not self.would_accept(';'):
                        while not self.would_accept((';', ENDMARKER)):
                            args.append(self.parse_arg())
                    self.require(';')
                    return self.pre_stmts.apply(tree.ExpressionStatement(tree.FunctionCall(name=tree.Name('printf'), args=args, object=self.make_member_access_from_dotted_name('java.lang.System.out'))))

        return super().parse_statement()

    def make_print_statement(self, name: str, arg: tree.Expression=None):
        return tree.ExpressionStatement(tree.FunctionCall(name=tree.Name(name), args=[] if arg is None else [arg], object=self.make_member_access_from_dotted_name('java.lang.System.out')))

    def parse_variable_decl(self, doc=None, modifiers=None, annotations=None, end=';'):
        if doc is None:
            doc = self.doc
        if modifiers is None and annotations is None:
            modifiers, annotations = self.parse_mods_and_annotations(newlines=(end == NEWLINE))
        if self.accept('var'):
            typ = tree.GenericType(name=tree.Name('var'))
        else:
            typ = self.parse_type()
        declarators = [self.parse_declarator(array=isinstance(typ, tree.ArrayType))]
        while self.accept(','):
            if self.other_trailing_commas and self.would_accept(end):
                break
            declarators.append(self.parse_declarator(array=isinstance(typ, tree.ArrayType)))
        self.require(end)
        return tree.VariableDeclaration(type=typ, declarators=declarators, doc=doc, modifiers=modifiers, annotations=annotations)

    def parse_condition(self):
        self.require('(')
        if self.vardecl_expressions and self.would_accept(('@', 'final', NAME, tree.PrimitiveType.VALUES)):
            try:
                with self.tokens:
                    modifiers, annotations = self.parse_mods_and_annotations()
                    if self.would_accept('var'):
                        var = True
                        typ = tree.GenericType(name=self.parse_name())
                    else:
                        var = False
                        typ = self.parse_type()
                    name = self.parse_name()
                    decl = self.parse_declarator_rest(name, require_init=True)
                    if var:
                        self.pre_stmts.append(tree.VariableDeclaration(type=typ, declarators=[decl], modifiers=modifiers, annotations=annotations))
                        expr = tree.MemberAccess(name=name)
                    else:
                        if isinstance(decl.init, tree.ArrayInitializer):
                            dims = []
                            for _ in decl.dimensions:
                                dims.append(tree.DimensionExpression())
                            if isinstance(typ, tree.ArrayType):
                                for _ in typ.dimensions:
                                    dims.append(tree.DimensionExpression())
                                typ = typ.base
                            init = tree.ArrayCreator(type=typ, dimensions=dims, initializer=decl.init)
                        else:
                            init = decl.init
                        decl.init = None
                        self.pre_stmts.append(tree.VariableDeclaration(type=typ, declarators=[decl], modifiers=modifiers, annotations=annotations))
                        expr = tree.Assignment(lhs=tree.MemberAccess(name=name), op='=', rhs=init)
            except JavaSyntaxError:
                expr = self.parse_expr()
        else:
            expr = self.parse_expr()
        self.require(')')
        return expr

    def parse_expr_list(self, end):
        update = [self.parse_expr()]
        while self.accept(','):
            if self.other_trailing_commas and self.would_accept(end):
                break
            update.append(self.parse_expr())
        return update

    def parse_case_labels(self):
        labels = [self.parse_case_label()]
        while self.accept(','):
            if self.other_trailing_commas and self.would_accept((':', '->')):
                break
            labels.append(self.parse_case_label())
        return labels

    #endregion Statements

    #region Type Stuff
    def parse_type_parameters(self):
        self.require('<')
        params = [self.parse_type_parameter()]
        while self.accept(','):
            if self.other_trailing_commas and self.would_accept('>'):
                break
            params.append(self.parse_type_parameter())
        self.require('>')
        return params

    def parse_annotation(self):
        self.require('@')
        typ = tree.GenericType(name=self.parse_qual_name())

        if self.accept('('):
            if self.would_accept(NAME, '='):
                args = [self.parse_annotation_arg()]
                while self.accept(','):
                    if self.argument_trailing_commas and self.would_accept(')'):
                        break
                    args.append(self.parse_annotation_arg())
            elif not self.would_accept(')'):
                args = self.parse_annotation_value()
            self.require(')')
        else:
            args = None

        return tree.Annotation(type=typ, args=args)

    def parse_type_args(self):
        self.require('<')
        args = []
        if not self.would_accept('>'):
            args.append(self.parse_type_arg())
            while self.accept(','):
                if self.argument_trailing_commas and self.would_accept('>'):
                    break
                args.append(self.parse_type_arg())
        self.require('>')
        return args

    def parse_generic_type_list(self):
        types = [self.parse_generic_type()]
        while self.accept(','):
            if self.other_trailing_commas and not self.would_accept(NAME):
                break
            types.append(self.parse_generic_type())
        return types

    def primitive_to_wrapper(self, typ: tree.Type) -> tree.Type:
        if isinstance(typ, tree.PrimitiveType):
            if typ.name == 'boolean':
                return tree.GenericType(tree.Name('java.lang.Boolean'))
            elif typ.name == 'byte':
                return tree.GenericType(tree.Name('java.lang.Byte'))
            elif typ.name == 'short':
                return tree.GenericType(tree.Name('java.lang.Short'))
            elif typ.name == 'char':
                return tree.GenericType(tree.Name('java.lang.Character'))
            elif typ.name == 'int':
                return tree.GenericType(tree.Name('java.lang.Integer'))
            elif typ.name == 'long':
                return tree.GenericType(tree.Name('java.lang.Long'))
            elif typ.name == 'float':
                return tree.GenericType(tree.Name('java.lang.Float'))
            else:
                assert typ.name == 'double'
                return tree.GenericType(tree.Name('java.lang.Double'))
        elif isinstance(typ, tree.VoidType):
            return tree.GenericType(tree.Name('java.lang.Void'))
        else:
            return typ

    #endregion Type Stuff

    #region Expressions
    def make_synthetic_var_name(self, hint, hint2=None) -> str:
        import re
        name = '__$$'
        if isinstance(hint, tree.Type):
            name += re.sub(r'[^\w$]', '_', str(hint.name)) + f"$${id(hint2 or hint):08x}"
        else:
            if not isinstance(hint, str):
                raise TypeError
            name += f"{hint}$${id(hint2 or hint):08x}"

        return name

    def parse_conditional(self):
        if self.would_accept(NAME, '->') or self.would_accept('('):
            try:
                with self.tokens:
                    result = self.parse_lambda()
            except JavaSyntaxError:
                result = self.parse_logic_or_expr()
        else:
            result = self.parse_logic_or_expr()            
        if self.accept('?'):
            if self.elvisoperator_expressions and self.token.string == ':' and self.token.start == self.tokens.look(-1).end:
                self.next()
                arg = self.parse_conditional()
                def is_simple(arg):
                    if isinstance(arg, tree.MemberAccess):
                        return arg.object is None
                    elif isinstance(arg, tree.Parenthesis):
                        return is_simple(arg.expr)
                    else:
                        return isinstance(arg, (tree.Literal, tree.NullLiteral, tree.TypeLiteral))
                
                if is_simple(arg):
                    name = 'requireNonNullElse'
                else:
                    name = 'requireNonNullElseGet'                    
                    arg = tree.Lambda(params=[], body=arg)

                result = tree.FunctionCall(name=tree.Name(name), object=self.make_member_access_from_dotted_name('java.util.Objects'), args=[result, arg])

            elif self.optional_literals and self.would_accept(('<', ')', ']', '}', ',', ';', ENDMARKER)):
                result = self.parse_optional_literal_rest(result)

            else:
                truepart = self.parse_assignment()
                self.require(':')
                falsepart = self.parse_conditional()
                result = tree.ConditionalExpression(condition=result, truepart=truepart, falsepart=falsepart)
        return result

    def parse_equality(self):
        result = self.parse_comp()
        while True:
            if self.accept('=='):
                rhs = self.parse_comp()
                result = self.make_equality(result, rhs, invert=False)
            elif self.accept('!='):
                rhs = self.parse_comp()
                result = self.make_equality(result, rhs, invert=True)
            elif self.equalityoperator_expressions and self.accept('is'):
                if self.token.string == '!' and self.token.start == self.tokens.look(-1).end:
                    self.next()
                    op = '!='
                else:
                    op = '=='
                result = tree.BinaryExpression(lhs=result, op=op, rhs=self.parse_comp())
            else:
                return result

    def is_non_string_literal(self, expr):
        if isinstance(expr, tree.Parenthesis):
            return self.is_non_string_literal(expr.expr)
        elif isinstance(expr, tree.Literal):
            return not expr.isstring
        else:
            return isinstance(expr, (tree.NullLiteral, tree.TypeLiteral))

    def make_equality(self, lhs, rhs, invert: bool):
        if self.equalityoperator_expressions and not self.is_non_string_literal(lhs) and not self.is_non_string_literal(rhs):
            result = tree.FunctionCall(name=tree.Name('deepEquals'), object=self.make_member_access_from_dotted_name('java.util.Objects'), args=[lhs, rhs])
            if invert:
                result = tree.UnaryExpression(expr=result, op='!')
            return result
        else:
            return tree.BinaryExpression(lhs=lhs, rhs=rhs, op='!=' if invert else '==')

    def parse_optional_literal_rest(self, value):
        typename = 'Optional'
        name = 'ofNullable'
        if isinstance(value, tree.CastExpression) and isinstance(value.type, tree.PrimitiveType):
            if value.type.name == 'int':
                typename = 'OptionalInt'
                name = 'of'
            elif value.type.name == 'double':
                typename = 'OptionalDouble'
                name = 'of'
            elif value.type.name == 'long':
                typename = 'OptionalLong'
                name = 'of'
        if self.accept('<'):
            annotations = self.parse_annotations()
            if self.accept('int', '>'):
                typename = 'OptionalInt'
                name = 'of'
            elif self.accept('double', '>'):
                typename = 'OptionalDouble'
                name = 'of'
            elif self.accept('long', '>'):
                typename = 'OptionalLong'
                name = 'of'
            else:
                if self.would_accept('?'):
                    typ = self.parse_type_arg(annotations)
                else:
                    typ = self.parse_type(annotations)
                self.require('>')
                for anno in annotations:
                    if anno.type.name == 'NonNull' or anno.type.name.endswith('.NonNull'):
                        name = 'of'
                        break
                return tree.FunctionCall(args=[value], name=tree.Name(name), object=self.make_member_access_from_dotted_name('java.util.Optional'), typeargs=[self.primitive_to_wrapper(typ)])
        return tree.FunctionCall(args=[value], name=tree.Name(name), object=self.make_member_access_from_dotted_name('java.util.' + typename))
    
    def parse_cast(self):
        if self.would_accept('('):
            try:
                with self.tokens:
                    self.next() # skip past the '(' token
                    typ = self.parse_cast_type()
                    self.require(')')
                    if self.would_accept('(') or self.would_accept(NAME, '->'):
                        try:
                            with self.tokens:
                                expr = self.parse_lambda()
                        except JavaSyntaxError:
                            expr = self.parse_postfix()
                            if self.would_accept(('++', '--')):
                                op = self.token.string
                                self.next()
                                expr = tree.IncrementExpression(op=op, prefix=False, expr=expr)  
                    else:
                        # if self.optional_literals and self.would_accept('?', (')', ']', '}', ',', ';', ENDMARKER)):
                        #     self.next() # skips past the '?' token
                        #     if isinstance(typ, tree.PrimitiveType):
                        #         if typ.name == 'int':
                        #             return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.OptionalInt'))
                        #         elif typ.name == 'double':
                        #             return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.OptionalDouble'))
                        #         elif typ.name == 'long':
                        #             return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.OptionalLong'))
                                
                        #     return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.Optional'), typeargs=[self.primitive_to_wrapper(typ)])
                        expr = self.parse_cast()
                    return tree.CastExpression(type=typ, expr=expr)
            except JavaSyntaxError:
                pass
        result = self.parse_postfix()
        if self.would_accept(('++', '--')):
            op = self.token.string
            self.next()
            result = tree.IncrementExpression(op=op, prefix=False, expr=result)
        return result

    def parse_postfix(self):
        result = self.parse_primary()
        while True:
            if self.would_accept('.'):
                result = self.parse_dot_expr(result)

            elif self.accept('['):
                index = self.parse_expr()
                self.require(']')
                result = tree.IndexExpression(indexed=result, index=index)

            elif self.would_accept('::'):
                result = self.parse_ref_expr(result)

            elif self.optional_literals and self.accept('!'):
                result = tree.FunctionCall(name=tree.Name('orElseThrow'), object=result)

            else:
                return result

    def parse_args(self):
        self.require('(')
        args = []
        if not self.would_accept(')'):
            args.append(self.parse_arg())
            while self.accept(','):
                if self.argument_trailing_commas and self.would_accept(')'):
                    break
                args.append(self.parse_arg())
        self.require(')')

        return args

    def parse_arg(self):
        if self.argument_annotations_syntax:
            self.accept(NAME, ':')
        return super().parse_arg()

    def make_member_access_from_dotted_name(self, qualname: str) -> tree.MemberAccess:
        result = None
        for name in qualname.split('.'):
            result = tree.MemberAccess(name=tree.Name(name), object=result)
        return result

    def parse_primary(self):
        if self.would_accept(REGEX):
            import re
            string = self.token.string[1:-1]
            self.next()
            regex = '"'
            escape = False
            for c in string:
                if escape:
                    if c == '"':
                        regex += '\\'
                    elif c != '/':
                        regex += R'\\'
                    regex += c
                    escape = False
                elif c == '\\':
                    escape = True
                elif c == '"':
                    regex += R'\"'
                else:
                    regex += c
            regex += '"'
            regex = re.sub(r"((?:\\\\)*)\\x([a-fA-F0-9]{2})", R'\1\\u00\2', regex)
            literal = tree.Literal(regex)
            return tree.FunctionCall(name=tree.Name('compile'), object=self.make_member_access_from_dotted_name('java.util.regex.Pattern'), args=[literal])

        elif self.would_accept(STRING) and (self.token.string[0] in "bB" or self.token.string[0] != '"' and self.token.string[1] in "bB"):
            import ast
            b = ast.literal_eval(self.token.string)
            assert isinstance(b, bytes), self.token.string
            elems = [tree.Literal(str(i)) for i in b]
            self.next()
            return tree.ArrayCreator(type=tree.PrimitiveType('byte'), dimensions=[tree.DimensionExpression()], initializer=tree.ArrayInitializer(elems))

        elif self.optional_literals and self.accept('?'):
            if self.accept('<'):
                annotations = self.parse_annotations()
                if self.accept('int', '>'):
                    return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.OptionalInt'))
                elif self.accept('double', '>'):
                    return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.OptionalDouble'))
                elif self.accept('long', '>'):
                    return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.OptionalLong'))
                else:
                    if self.would_accept('?'):
                        typ = self.parse_type_arg(annotations)
                    else:
                        typ = self.parse_type(annotations)
                    self.require('>')
                    return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.Optional'), typeargs=[self.primitive_to_wrapper(typ)])
            else:
                return tree.FunctionCall(name=tree.Name('empty'), object=self.make_member_access_from_dotted_name('java.util.Optional'), args=[])

        else:
            return super().parse_primary()

    def parse_parens(self):
        return tree.Parenthesis(self.parse_condition())

    def parse_list_literal(self):
        if not self.collections_literals:
            return super().parse_list_literal()
        self.require('[')
        elements = []
        if not self.would_accept(']'):
            if not self.accept(','):
                elements.append(self.parse_expr())
                while self.accept(','):
                    if self.would_accept(']'):
                        break
                    elements.append(self.parse_expr())
        self.require(']')

        return self.make_list_literal(elements)

    def make_list_literal(self, elements: List[tree.Expression]):
        return tree.FunctionCall(args=elements,
                                 name=tree.Name('of'), 
                                 object=self.make_member_access_from_dotted_name('java.util.List'))

    def parse_map_literal(self):
        if not self.collections_literals:
            return super().parse_map_literal()
        self.require('{')
        entries = []
        if not self.would_accept('}'):
            if not self.accept(','):
                key = self.parse_expr()
                if not self.accept(':'):
                    return self.parse_set_literal_rest(key)
                entries.append((key, self.parse_expr()))
                while self.accept(','):
                    if self.would_accept(']'):
                        break
                    entries.append(self.parse_map_entry())
        self.require('}')

        return self.make_map_literal(entries)

    def make_map_literal(self, entries: List[Tuple[tree.Expression, tree.Expression]]):
        if len(entries) <= 10:
            args = []
            for key, value in entries:
                args.append(key)
                args.append(value)
            return tree.FunctionCall(args=args,
                                     name=tree.Name('of'), 
                                     object=self.make_member_access_from_dotted_name('java.util.Map'))
        else:
            for i, (key, value) in enumerate(entries):
                entries[i] = tree.FunctionCall(args=[key, value],
                                               name=tree.Name('entry'), 
                                               object=self.make_member_access_from_dotted_name('java.util.Map'))
            return tree.FunctionCall(args=entries,
                                     name=tree.Name('ofEntries'), 
                                     object=self.make_member_access_from_dotted_name('java.util.Map'))

    def parse_map_entry(self):
        key = self.parse_expr()
        self.require(':')
        value = self.parse_expr()
        return key, value

    def parse_set_literal_rest(self, elem):
        elements = [elem]
        while self.accept(','):
            if self.would_accept('}'):
                break
            elements.append(self.parse_expr())
        self.require('}')

        return self.make_set_literal(elements)

    def make_set_literal(self, elements: List[tree.Expression]):
        return tree.FunctionCall(args=elements,
                                 name=tree.Name('of'),
                                 object=self.make_member_access_from_dotted_name('java.util.Set'))

    def parse_class_creator_rest(self, type, typeargs):
        if not self.class_creator_expressions:
            return super().parse_class_creator_rest(type, typeargs)
            
        members = None

        if self.accept('{'):
            expr = self.parse_expr()
            if self.accept(':'):
                entries = [(expr, self.parse_expr())]
                while self.accept(','):
                    if self.would_accept('}'):
                        break
                    entries.append(self.parse_map_entry())
                self.require('}')
                args = [self.make_map_literal(entries)]
            else:
                elements = [expr]
                while self.accept(','):
                    if self.would_accept('}'):
                        break
                    elements.append(self.parse_expr())
                self.require('}')
                args = [self.make_list_literal(elements)]

        elif self.would_accept('('):
            args = self.parse_args()
        
            if self.would_accept('{'):
                members = self.parse_class_body(self.parse_class_member)

        else:
            args = []
        
        if typeargs is None:
            typeargs = []
        return tree.ClassCreator(type=type, args=args, typeargs=typeargs, members=members)

    #endregion Expressions

def parse_file(file, parser: Type[JavaParser]=JavaPlusPlusParser) -> tree.CompilationUnit:
    return java_parse_file(file, parser)

def parse_str(s: Union[str, bytes, bytearray], encoding='utf-8', parser: Type[JavaParser]=JavaPlusPlusParser) -> tree.CompilationUnit:
    return java_parse_str(s, encoding=encoding, parser=parser)
