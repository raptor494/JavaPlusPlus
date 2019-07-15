try:
    from .parser import JavaPlusPlusParser, parse_file, parse_str
except ImportError:
    from javapp.parser import JavaPlusPlusParser, parse_file, parse_str
from java import tokenize, JavaParser, JavaSyntaxError

import unittest

class UnitTests(unittest.TestCase):
    # def test_parser(self):
    #     import os.path
    #     import pprint
    #     from pathlib import Path
    #     from textwrap import indent
    #     self.maxDiff = None
    #     with open(os.path.join(os.path.dirname(__file__), 'test.java'), 'rb') as file:
    #         java_unit = parse_file(file, parser=JavaParser)
    #     with open(os.path.join(os.path.dirname(__file__), 'test.javapp'), 'rb') as file:
    #         javapp_unit = parse_file(file, parser=JavaPlusPlusParser)
    #     self.assertEqual(java_unit, javapp_unit, f"java_unit != javapp_unit.\njava_unit:\n{indent(pprint.pformat(java_unit), '    ')}\njavapp_unit:\n{indent(pprint.pformat(javapp_unit), '    ')}")
    #     java_unit_str = str(java_unit)
    #     javapp_unit_str = str(javapp_unit)
    #     self.assertEqual(java_unit_str, javapp_unit_str, f"str(java_unit) != str(javapy_unit).")

    def __init__(self, methodName='runTest'):
        super().__init__(methodName)

        self.maxDiff = None

    import functools
    import os
    from pathlib import Path

    test_files = Path(os.path.dirname(__file__), 'test_files')

    def test(self, test_folder):
        import pprint
        from textwrap import indent
        with test_folder.joinpath(test_folder.name + '.java').open('rb') as file:
            java_unit = parse_file(file, parser=JavaParser)
        with test_folder.joinpath(test_folder.name + '.javapp').open('rb') as file:
            parser = JavaPlusPlusParser(tokenize(file.readline), file.name)
            parser.enable_feature('*', False)
            parser.enable_feature('syntax.multiple_import_sections', True)
            parser.enable_feature('statements.print', True)
            javapp_unit = parser.parse_compilation_unit()
        self.assertEqual(java_unit, javapp_unit, f"java_unit != javapp_unit.\njava_unit:\n{indent(pprint.pformat(java_unit), '    ')}\njavapp_unit:\n{indent(pprint.pformat(javapp_unit), '    ')}")
        java_unit_str = str(java_unit)
        javapp_unit_str = str(javapp_unit)
        self.assertEqual(java_unit_str, javapp_unit_str, f"str(java_unit) != str(javapy_unit).")

    for test_folder in test_files.iterdir():
        if test_folder.is_dir():
            locals()['test_' + test_folder.name] = functools.partialmethod(test, test_folder)            

    del test, test_files, test_folder, functools, os, Path

def main(args=None):
    import argparse
    import sys
    from inspect import isfunction, signature, Parameter
    from pathlib import Path

    def get_parse_methods(cls):
        for func in vars(cls).values():
            if isfunction(func) and func.__name__.startswith("parse_"):
                sig = signature(func)
                params = sig.parameters
                
                if len(params) == 0:
                    valid = True
                else:
                    for param in params.values():
                        if param.default is Parameter.empty:
                            valid = False
                            break
                if valid:
                    yield func.__name__[6:]

    if '--list-parse-methods' in (sys.argv if args is None else args):
        
        for name in {*get_parse_methods(JavaParser), *get_parse_methods(JavaPlusPlusParser)}:
            print(name, end='  ')
        print()
        return

    argparser = argparse.ArgumentParser(description='Parse a javapy file')
    argparser.add_argument('file', metavar='FILE', default='STDIN',
                        help='The file to parse. Special name "STDIN" can be used to input from the console. Defaults to STDIN.')
    argparser.add_argument('--type', choices=['Java', 'Java++'], default='Java++',
                        help='What syntax to use')
    argparser.add_argument('--out', metavar='FILE', type=Path,
                        help='Where to save the output. Special name "STDOUT" can be used to output to the console. Special name "NUL" can be used to not output anything at all.')
    argparser.add_argument('--parse',
                        help='Instead of parsing a file, parse the argument as this type and display the resulting Java code.')
    argparser.add_argument('-e', dest='enable', choices=['*', *JavaPlusPlusParser.supported_features], action='append', default=[],
                        help='Enable the specified features by default')
    argparser.add_argument('-d', dest='disable', choices=['*', *JavaPlusPlusParser.supported_features], action='append', default=[],
                        help='Disable the specified features by default')
    argparser.add_argument('--list-parse-methods', dest='list_parse_methods', action='store_true',
                        help='Print a list of valid arguments to the --parse option and exit.')

    args = argparser.parse_args(args)

    assert not args.list_parse_methods

    if args.type == 'Java':
        parser = JavaParser
    else:
        assert args.type == 'Java++', f'args.type = {args.type!r}'
        parser = JavaPlusPlusParser

    if args.parse:
        import io

        if args.file == 'STDIN':
            p = parser(tokenize(sys.stdin.buffer.readline), '<stdin>')
        else:
            p = parser(tokenize(io.BytesIO(bytes(args.file, 'utf-8')).readline), '<string>')

        if parser is JavaPlusPlusParser:
            for feature in args.enable:
                p.enable_feature(feature)
            for feature in args.disable:
                p.enable_feature(feature, False)

        if not hasattr(p, 'parse_' + args.parse):
            print(argparser.usage)
            print(argparser.prog + ': error: invalid option for --parse:', args.parse)
            return
        unit = getattr(p, 'parse_' + args.parse)()

        if args.out and str(args.out) != 'STDOUT':
            if str(args.out) != 'NUL':
                with args.out.open('w') as file:
                    file.write(str(unit))
                    print('Wrote to', file.name)
        else:
            print(str(unit))

    else:
        if args.file == "STDIN":
            import io
            p = parser(tokenize(sys.stdin.buffer.readline), '<stdin>')
        else:
            with open(args.file, 'rb') as file:
                p = parser(tokenize(file.readline), file.name)

        unit = p.parse_compilation_unit()
        
        if parser is JavaPlusPlusParser:
            for feature in args.enable:
                p.enable_feature(feature)
            for feature in args.disable:
                p.enable_feature(feature, False)

        if args.out:
            if str(args.out) == 'STDOUT':
                filename = args.file.name
                print(unit)
            elif str(args.out) != 'NUL':
                with args.out.open('w') as file:
                    file.write(str(unit))
                    filename = file.name

        else:
            import os.path

            filename = os.path.join(os.path.dirname(args.file), os.path.splitext(os.path.basename(args.file))[0] + '.java')

            with open(filename, 'w') as file:
                file.write(str(unit))

        print("Converted", filename)

if __name__ == "__main__":
    main()