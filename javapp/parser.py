import io
import java.tree as tree
from java.parser import JavaParser, JavaSyntaxError, parse_file as java_parse_file, parse_str as java_parse_str
from java.tokenize import *
from typeguard import check_type, check_argument_types
from typing import Union, List, Optional, Type, Tuple, Set

class JavaPlusPlusParser(JavaParser):
    supported_features = {'print_statement', 'new_expression', 'literals', 'trailing_argument_comma', 'named_arguments',
                                    'additional_auto_imports', 'trailing_other_comma', 'multiple_import_sections',
                                    'static_imports'}
                                    
    def __init__(self, tokens, filename='<unknown source>'):
        super().__init__(tokens, filename)
        self.print_statement = True
        self.literals_enabled = True
        self.new_expression = True
        self.trailing_argument_comma = True
        self.trailing_other_comma = False
        self.named_arguments = True
        self.additional_auto_imports = True
        self.static_imports = True
        self.multiple_import_sections = True

        self.auto_imports = {
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
        self.auto_static_imports = {
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

    def enable_feature(self, feature: str, enabled: bool=True):
        assert check_argument_types()
        if feature == '*':
            for feature in self.supported_features:
                setattr(self, feature, enabled)
        elif feature in self.supported_features:
            setattr(self, feature, enabled)
        else:
            raise ValueError(f"unsupported feature '{feature}'")

    #region Declarations
    def parse_import_section(self) -> List[tree.Import]:
        imports = []
        while True:
            if self.would_accept('import'):
                imports.extend(self.parse_import_declarations())
            elif self.would_accept('from'):
                imports.extend(self.parse_from_import_declarations())
            elif not self.accept(';'):
                break

        auto_import_index = 0

        if self.additional_auto_imports:
            for package, imported_types in self.auto_imports.items():
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

        if self.static_imports:
            for package, types in self.auto_static_imports.items():
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
            name, wildcard = self.parse_import_name()
            imports.append(tree.Import(name=name, static=static, wildcard=wildcard))
            if not self.accept(','):
                break
            if self.trailing_other_comma and self.would_accept(';'):
                break
        self.require(';')
        return imports

    def parse_from_import_declarations(self) -> List[tree.Import]:
        self.require('from')
        imports = []

        if self.would_accept('java', '++'): # 'from java++ import ...' / 'from java++ unimport ...' statement, allows modifying syntax
            self.next() # skips past the 'java' token
            self.next() # skips past the '++' token

            if self.accept('import'):
                enable_feature = True
            else:
                self.require('unimport')
                enable_feature = False

            if self.accept('*'):
                self.enable_feature('*', enable_feature)

            else:
                while True:
                    start_pos = self.position()
                    feature = self.parse_ident()
                    try:
                        self.enable_feature(feature, enable_feature)
                    except ValueError as e:
                        raise JavaSyntaxError(str(e), at=start_pos)
                    if not self.accept(','):
                        break
                    if self.trailing_other_comma and self.would_accept(';'):
                        break
        else:
            base = self.parse_qual_name()
            self.require('import')
            static = bool(self.accept('static'))

            name, wildcard = self.parse_from_import_name(base)
            imports.append(tree.Import(name=name, static=static, wildcard=wildcard))

            while self.accept(','):
                if self.trailing_other_comma and self.would_accept(';'):
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
        types = [self.parse_type_declaration(doc, modifiers, annotations)]
        while self.token.type != ENDMARKER:
            if not self.accept(';'):
                if self.multiple_import_sections and imports is not None and self.would_accept(('from', 'import')):
                    imports.extend(self.parse_import_section())
                else:
                    types.append(self.parse_type_declaration())
        return types

    #endregion Declarations

    #region Statements
    def parse_statement(self):
        if self.print_statement:
            if self.accept('println'):
                if self.accept(';'):
                    return self.make_print_statement('println')
                elements = [self.parse_arg()]
                if self.would_accept(','):
                    while self.accept(','):
                        if self.trailing_other_comma and self.would_accept(';'):
                            break
                        elements.append(self.parse_arg())
                elif not self.would_accept(';'):
                    while not self.would_accept((';', ENDMARKER)):
                        elements.append(self.parse_arg())
                self.require(';')
                if len(elements) == 1:
                    return self.make_print_statement('println', elements[0])
                stmts = []
                for i, arg in enumerate(elements):
                    if i:
                        stmts.append(self.make_print_statement('print', tree.Literal("' '")))
                    stmts.append(self.make_print_statement('print' if i+1 < len(elements) else 'println', arg))
                return tree.Block(stmts)
            elif self.accept('print'):
                if self.accept(';'):
                    return tree.EmptyStatement()
                elements = [self.parse_arg()]
                if self.would_accept(','):
                    while self.accept(','):
                        if self.trailing_other_comma and self.would_accept(';'):
                            break
                        elements.append(self.parse_arg())
                elif not self.would_accept(';'):
                    while not self.would_accept((';', ENDMARKER)):
                        elements.append(self.parse_arg())
                self.require(';')
                if len(elements) == 1:
                    return self.make_print_statement('print', elements[0])
                stmts = []
                for i, arg in enumerate(elements):
                    if i:
                        stmts.append(self.make_print_statement('print', tree.Literal("' '")))
                    stmts.append(self.make_print_statement('print', arg))
                return tree.Block(stmts)
            elif self.accept('printf'):
                args = [self.parse_arg()]
                if self.would_accept(','):
                    while self.accept(','):
                        if self.trailing_other_comma and self.would_accept(';'):
                            break
                        args.append(self.parse_arg())
                elif not self.would_accept(';'):
                    while not self.would_accept((';', ENDMARKER)):
                        args.append(self.parse_arg())
                self.require(';')
                return tree.ExpressionStatement(tree.FunctionCall(name=tree.Name('printf'), args=args, object=self.make_member_access_from_dotted_name('java.lang.System.out')))
            elif self.accept('printfln'):
                args = [tree.BinaryExpression(lhs=self.parse_arg(), op='+', rhs=tree.Literal('"%n"'))]
                if self.would_accept(','):
                    while self.accept(','):
                        if self.trailing_other_comma and self.would_accept(';'):
                            break
                        args.append(self.parse_arg())
                elif not self.would_accept(';'):
                    while not self.would_accept((';', ENDMARKER)):
                        args.append(self.parse_arg())
                self.require(';')
                return tree.ExpressionStatement(tree.FunctionCall(name=tree.Name('printf'), args=args, object=self.make_member_access_from_dotted_name('java.lang.System.out')))

        return super().parse_statement()

    def make_print_statement(self, name: str, arg: tree.Expression=None):
        return tree.ExpressionStatement(tree.FunctionCall(name=tree.Name(name), args=[] if arg is None else [arg], object=self.make_member_access_from_dotted_name('java.lang.System.out')))

    #endregion

    #region Expressions
    def parse_args(self):
        self.require('(')
        args = []
        if not self.would_accept(')'):
            args.append(self.parse_arg())
            while self.accept(','):
                if self.trailing_argument_comma and self.would_accept(')'):
                    break
                args.append(self.parse_arg())
        self.require(')')

        return args

    def parse_arg(self):
        if self.named_arguments:
            self.accept(NAME, ':')
        return super().parse_arg()

    def make_member_access_from_dotted_name(self, qualname: str) -> tree.MemberAccess:
        result = None
        for name in qualname.split('.'):
            result = tree.MemberAccess(name=tree.Name(name), object=result)
        return result

    def parse_list_literal(self):
        if not self.literals_enabled:
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
        if not self.literals_enabled:
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
        if not self.new_expression:
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
