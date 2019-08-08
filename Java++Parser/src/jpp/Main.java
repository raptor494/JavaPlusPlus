package jpp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import jpp.parser.JavaPlusPlusParser;
import jpp.parser.JavaPlusPlusParser.Feature;
import lombok.SneakyThrows;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.MessageLocalization;
import net.sourceforge.argparse4j.helper.TextHelper;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	
	public static void main(String[] args) {
		var parser = ArgumentParsers.newFor("java++")
				.fromFilePrefix("@")
				.singleMetavar(true)
				.build()
				.description("Parse Java++ code from files");
		var filesArg = parser.addArgument("files")
				.type(Arguments.fileType().acceptSystemIn().verifyCanRead().verifyExists().verifyIsFile().or().acceptSystemIn().verifyExists().verifyIsDirectory())
				.nargs("*")
				.metavar("FILE")
				.help("The files to parse");
		var listFeaturesArg = parser.addArgument("--list-features")
				.action(Arguments.storeTrue())
				.help("Print a list of supported features and exit");
		parser.addArgument("--enable", "-e")
				.type(new FeatureType())
				.action(new FeatureJoiningAction())
				.metavar("FEATURES")
				.setDefault(EnumSet.noneOf(Feature.class));
		parser.addArgument("--disable", "-d")
        		.type(new FeatureType())
        		.action(new FeatureJoiningAction())
        		.metavar("FEATURES")
        		.setDefault(EnumSet.noneOf(Feature.class));
		parser.addArgument("--out", "-o")
				.type(Arguments.fileType().verifyIsDirectory().verifyNotExists().verifyCanCreate().or().verifyIsDirectory().verifyExists());
		parser.addArgument("--recursive", "-r")
				.action(Arguments.storeTrue())
				.help("Look through subdirectories of folders as well");
		
		Namespace ns;
		try {
			ns = parser.parseArgs(args);
			validate_args:
    			if(ns.getBoolean("list_features")) {
    				String argName;
    				if(!ns.getList("files").isEmpty()) {
    					argName = "FILE";
    				} else if(!ns.<EnumSet<Feature>>get("enable").isEmpty()) {
    					argName = "enable";
    				} else if(!ns.<EnumSet<Feature>>get("disable").isEmpty()) {
    					argName = "disable";
    				} else if(ns.get("out") != null) {
    					argName = "out";
    				} else if(ns.getBoolean("recursive")) {
    					argName = "recursive";
    				} else {
    					break validate_args;
    				}
    				throw new ArgumentParserException(String.format(
    						TextHelper.LOCALE_ROOT,
    						MessageLocalization.localize(
    								parser.getConfig().getResourceBundle(),
    								"notAllowedWithArgumentError"),
    						argName), parser, listFeaturesArg);
    			} else if(ns.getList("files").isEmpty()) {
    				throw new ArgumentParserException(String.format(
    	                    TextHelper.LOCALE_ROOT,
    	                    MessageLocalization.localize(
    	                    		parser.getConfig().getResourceBundle(),
    	                    		"expectedNArgumentsError"),
    	                    1), parser, filesArg);
    			}
		} catch(ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
			return;
		}
		
		if(ns.getBoolean("list_features")) {
			System.out.println("Features:");
			for(var feature : Feature.VALUES.stream().sorted((feature1, feature2) -> feature1.id.compareTo(feature2.id)).collect(Collectors.toList())) {
				System.out.println(feature);
			}
			System.exit(1);
		}
		
		File out = ns.get("out");
		if(!out.exists()) {
			out.mkdirs();
		}
		
		Path outPath = out.toPath();
		
		EnumSet<Feature> enabledFeatures = ns.get("enable"),
						 disabledFeatures = ns.get("disable");
		BiFunction<CharSequence, String, JavaPlusPlusParser> parserSupplier;
		if(enabledFeatures.isEmpty() && disabledFeatures.isEmpty()) {
			parserSupplier = JavaPlusPlusParser::new;
		} else {
			var features = Feature.enabledByDefault();
			features.addAll(enabledFeatures);
			features.removeAll(disabledFeatures);
			parserSupplier = (code, filename) -> new JavaPlusPlusParser(code, filename, features);
		}
		
		parseFiles(ns.getList("files"), parserSupplier, ns.getBoolean("recursive"), outPath);
	}
	
	private static void parseFiles(List<File> files, BiFunction<CharSequence, String, JavaPlusPlusParser> parserCreator, boolean recursive, Path outDir) {
		for(var file : files) {
			if(file.isDirectory()) {
				var newOutDir = outDir.resolve(file.getName());
				for(var subfile : file.listFiles(f -> f.isDirectory() || f.getName().matches("(?i).*\\.j(pp|ava(pp)?)"))) {
					parseFile(subfile, parserCreator, recursive, newOutDir);
				}
			} else {
				parseFile(file, parserCreator, recursive, outDir);
			}
		}
	}
	
	@SneakyThrows
	private static void parseFile(File file, BiFunction<CharSequence, String, JavaPlusPlusParser> parserCreator, boolean recursive, Path outDir) {
		if(file.isDirectory()) {
			if(recursive) {
				var newOutDir = outDir.resolve(file.getName());
				for(var subfile : file.listFiles(f -> f.isDirectory() || f.getName().matches("(?i).*\\.j(pp|ava(pp)?)"))) {
					parseFile(subfile, parserCreator, recursive, newOutDir);
				}
			}
		} else {
			CharSequence text;
			try(var scan = new Scanner(file)) {
				scan.useDelimiter("\\A");
				text = scan.next();
			} catch(NoSuchElementException e) {
				System.out.print("Skipped ");
				System.out.println(file);
				return;
			}
			
			var parser = parserCreator.apply(text, file.getName());
			
			var unit = parser.parseCompilationUnit();
			
			String name;
			if(file.getName().matches("(?i).*\\.java") && outDir.toAbsolutePath().equals(file.getParentFile().toPath())) {
				name = file.getName();
				int i = name.lastIndexOf('.');
				name = name.substring(0, i) + "_converted.java";
			} else {
				name = file.getName();
				int i = name.lastIndexOf('.');
				name = name.substring(0, i) + ".java";
			}
			
			Path out = outDir.resolve(name);
			
			Files.writeString(out, unit.toCode(), StandardOpenOption.CREATE);
			System.out.print("Converted ");
			System.out.println(file);
		}
	}
	
}

class FeatureType implements ArgumentType<EnumSet<Feature>> {

	@Override
	public EnumSet<Feature> convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
		if(value.matches("\\s*\\*\\s*")) {
			return EnumSet.allOf(Feature.class);
		}
		var result = EnumSet.noneOf(Feature.class);
		boolean found, loop = true;
		do {
			found = false;
			int i = value.indexOf(',');
			if(i == -1) {
				i = value.length();
				loop = false;
			}
			String sub = value.substring(0, i).strip();
			if(loop) {
				value = value.substring(i+1);
			}
			if(sub.endsWith(".*")) {
				String prefix = sub.substring(0, sub.length()-1); // removes the '*'
				for(var feature : Feature.VALUES) {
					if(feature.id.startsWith(prefix)) {
						found = true;
						result.add(feature);
					}
				}
			} else {
    			for(var feature : Feature.VALUES) {
    	            if(feature.id.equals(sub)) {
    	                result.add(feature);
    	                found = true;
    	            }
    	        }
			}
		} while(found && loop);

		if(found) {
			return result;
		}
		
		String choices = TextHelper.concat(Feature.VALUES, 0,
                ",", "{", "}");
        throw new ArgumentParserException(String.format(TextHelper.LOCALE_ROOT,
                MessageLocalization.localize(
                        parser.getConfig().getResourceBundle(),
                        "couldNotConvertChooseFromError"),
                value, choices), parser, arg);
	}
	
}

class FeatureJoiningAction implements ArgumentAction {
	@SuppressWarnings("unchecked")
	@Override
    public void run(ArgumentParser parser, Argument arg,
            Map<String, Object> attrs, String flag, Object value)
            throws ArgumentParserException {
        if(attrs.containsKey(arg.getDest())) {
            Object obj = attrs.get(arg.getDest());
            if(obj instanceof EnumSet) {
            	var set = (EnumSet<Feature>)obj;
            	if(value instanceof Feature) {
            		set.add((Feature)value);
            	} else {
            		for(var elem : (Collection<?>)value) {
            			if(elem instanceof Feature) {
	            			set.add((Feature)elem);
	            		} else {
	            			set.addAll((Collection<Feature>)elem);
	            		}
            		}
            	}
                return;
            } else if(obj instanceof List) {
            	EnumSet<Feature> set;
            	if(value instanceof Feature) {
            		set = EnumSet.of((Feature)value);
            	} else {
            		set = EnumSet.copyOf((Collection<Feature>)value);
            	}
            	for(var elem : (List<?>)obj) {
            		if(elem instanceof Feature) {
            			set.add((Feature)elem);
            		} else {
            			set.addAll((Collection<Feature>)elem);
            		}
            	}
            	attrs.put(arg.getDest(), set);
            	return;
            }
        }
        EnumSet<Feature> set;
        if(value instanceof EnumSet) {
        	set = (EnumSet<Feature>)value;
        } else {
        	set = EnumSet.of((Feature)value);
        }
        attrs.put(arg.getDest(), set);
    }

    @Override
    public boolean consumeArgument() {
        return true;
    }

    @Override
    public void onAttach(Argument arg) {
    }
}
