package jpp.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import jpp.nodes.EnableDisableStmt;
import jpp.nodes.EnableDisableStmt.FeatureId;
import jpp.parser.JavaPlusPlusParser;
import jpp.parser.JavaPlusPlusParser.Feature;
import jpp.parser.JavaPlusPlusTokenizer;
import jtree.nodes.REPLEntry;
import jtree.parser.JavaParser;
import jtree.parser.JavaTokenType;
import jtree.parser.JavaTokenizer;

public class Tester extends jtree.parser.Tester {
	public static void main(String[] args) {
		new Tester().run();
	}
	
	protected EnumSet<Feature> enabledFeatures = Feature.enabledByDefault();
	
	@Override
	protected void dispatchCommand(String command, String[] args) {
		switch(command) {
			case "enable", "Enable", "ENABLE" -> enable(args);
			case "disable", "Disable", "DISABLE" -> disable(args);
			case "features", "Features", "FEATURES" -> features(args);
			default -> super.dispatchCommand(command, args);
		}
	}
	
	protected void features(String[] args) {
		if(args.length == 0) {
			printFeatures();
		} else {
			System.out.println("Too many arguments to command 'features'");
		}
	}
	
	protected void printFeatures() {
		System.out.println("Features:");
		for(var feature : Feature.VALUES.stream().sorted((feature1, feature2) -> feature1.id.compareTo(feature2.id)).collect(Collectors.toList())) {
			System.out.println(feature.id);
		}
	}
	
	protected void enable(String[] args) {
		if(args.length == 0) {
			if(enabledFeatures.isEmpty()) {
				System.out.println("All features are currently disabled");
			} else {
				System.out.println("Enabled features:");
				for(var feature : enabledFeatures.stream().sorted((feature1, feature2) -> feature1.id.compareTo(feature2.id)).collect(Collectors.toList())) {
					System.out.println(feature.id);
				}
			}
		} else {
			setEnabled(args, true);
		}
	}
	
	protected void disable(String[] args) {
		if(args.length == 0) {
			if(enabledFeatures.size() == Feature.VALUES.size()) {
				System.out.print("All features are currently enabled");
			} else {
				System.out.println("Disabled features:");
				for(var feature : EnumSet.complementOf(enabledFeatures).stream().sorted((feature1, feature2) -> feature1.id.compareTo(feature2.id)).collect(Collectors.toList())) {
					System.out.println(feature.id);
				}
			}
		} else {
			setEnabled(args, false);
		}
	}
	
	protected final void setEnabled(String[] features, boolean enabled) {
		setEnabled(Arrays.asList(features), enabled);
	}
	
	protected void setEnabled(Collection<String> features, boolean enabled) {
		outer:
		for(String featureId : features) {
			if(featureId.equals("*")) {
				if(enabled) {
					enabledFeatures.addAll(Feature.VALUES);
					System.out.println("Enabled all features");
				} else {
					enabledFeatures.clear();
					System.out.println("Disabled all features");
				}
			} else if(featureId.endsWith(".*") && featureId.length() > 2) {
				String prefix = featureId.substring(0, featureId.length()-1); // removes the *
				boolean found = false;
				for(var feature : Feature.VALUES) {
					if(feature.id.startsWith(prefix)) {
						found = true;
						if(enabled) {
							if(enabledFeatures.add(feature)) {
								System.out.println("Enabled " + feature.id);
							}
						} else {
							if(enabledFeatures.remove(feature)) {
								System.out.println("Disabled " + feature.id);
							}
						}
					}
				}
				if(!found) {
					System.out.println("No feature found matching '" + featureId + "'");
				}
			} else {
				for(var feature : Feature.VALUES) {
					if(feature.id.equals(featureId)) {
						if(enabled) {
							if(enabledFeatures.add(feature)) {
								System.out.println("Enabled " + feature.id);
							}
						} else {
							if(enabledFeatures.remove(feature)) {
								System.out.println("Disabled " + feature.id);
							}
						}
						continue outer;
					}
				}
				System.out.println("No feature found matching '" + featureId + "'");
			}
		}
	}
	
	@Override
	protected void printJshellEntries(List<REPLEntry> jshellEntries) {
		boolean first = true;
		for(var elem : jshellEntries) {
			if(first) {
				first = false;
			} else {
				System.out.println();
			}
			if(elem instanceof EnableDisableStmt) {
				var stmt = (EnableDisableStmt)elem;
				setEnabled(stmt.getFeatures().stream().map(FeatureId::toCode).collect(Collectors.toList()), stmt.isEnable());
			} else {
				printNodeString(elem);
			}
		}
	}
	
	@Override
	protected void printHelp() {
		super.printHelp();
		System.out.println(
			"/enable [<features>]\n"
			+ "/disable [<features>]\n"
			+ "/features"
		);
	}
	
	@Override
	protected JavaParser createParser(CharSequence text, String filename) {
		return new JavaPlusPlusParser(text, filename, enabledFeatures);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends JavaParser>[] getParserClasses() {
		return new Class[] { JavaParser.class, JavaPlusPlusParser.class };
	}
	
	@Override
	protected JavaTokenizer<JavaTokenType> createTokenizer(CharSequence text, String filename) {
		return new JavaPlusPlusTokenizer(text, filename, enabledFeatures);
	}
	
}
