
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import presentation.PresentationLayer;

public class LinterMain {
	public static void main(String[] args) {
		String[] classList = {};
		int flags = 0;

		if (args.length > 0) {
			for (String string : args) {
				// There should be no beginning hyphen for the list of files, therefore we can
				// assume this is likely an actual path
				if (string.charAt(0) != '-') {
					classList = getClassList(string);
				} else {
					flags = setFlags(flags, string);
				}
			}
		} else {
			System.out.println("ERROR: DesignLint requires arguments to run!");
			System.out.println();
			displayHelp();
			System.exit(1);
		}

		// If there are no analyzer flags set, enable all of them
		if ((flags & PresentationLayer.ALL_ANALYZERS) == 0x0) {
			flags = flags | PresentationLayer.ALL_ANALYZERS;
		}

		PresentationLayer frontend = new PresentationLayer(flags);
		frontend.setupAnalyzers(classList);
		frontend.runAnalyzers();
		frontend.vomitOutput(System.out);

	}

	private static String[] getClassList(String string) {
		List<String> classFilesToAnalyze = new ArrayList<>();
		File pathListed = new File(string);

		if (pathListed.isDirectory()) {
			String[] recursed = recurseDirectory(string);
			if (recursed != null & recursed.length > 0) {
				classFilesToAnalyze.addAll(Arrays.asList(recursed));
			}
		} else if (pathListed.getName().contains(".class")) {
			classFilesToAnalyze.add(pathListed.getAbsolutePath());
		}

		String[] returnList = new String[classFilesToAnalyze.size()];
		classFilesToAnalyze.toArray(returnList);
		return returnList;
	}

	// Presentation layer will not determine what files to parse, only to parse
	// files
	// Hence directory recursion is defined here and not in the presentation layer
	private static String[] recurseDirectory(String path) {
		List<String> files = new ArrayList<>();
		File currentDir = new File(path);

		for (File child : currentDir.listFiles()) {
			if (Files.isSymbolicLink(child.toPath())) {
				throw new IllegalArgumentException(
						"Error, I found a symlink and I don't want to touch that yucky stuff >:P");
			} else if (child.isFile() && child.getName().contains(".class")) {
				files.add(child.getAbsolutePath());
			} else {
				if (child.isDirectory()) {
					String[] recursed = recurseDirectory(child.getAbsolutePath());
					if (recursed != null && recursed.length > 0) {
						files.addAll(Arrays.asList(recursed));
					}
				}
			}
		}

		String[] result = new String[files.size()];
		files.toArray(result);
		return result;
	}

	private static int setFlags(int inFlags, String switchStr) {
		// All options will be single char/multi char (analyzers only)
		switch (switchStr.charAt(1)) {
			case 'v':
				// case 'V':
				int flags = inFlags | PresentationLayer.VERBOSE_FLAG;
				if (switchStr.length() >= 3 && switchStr.charAt(2) == 'v') {
					flags = flags | PresentationLayer.SUPER_VERBOSE_FLAG;
					if (switchStr.length() >= 4 && switchStr.charAt(3) == 'v') {
						flags = flags | PresentationLayer.ULTRA_VERBOSE_FLAG;
					}
				}
				return flags;

			case 'h':
				displayHelp();
				System.exit(0);
				break;
			case 'a':
				// Limited to if-else her because string compares
				// should also only be only two chars
				String analyzerFlag = switchStr.substring(2, switchStr.length());
				if (analyzerFlag.length() >= 3) {
					break;
				}

				// Now actual analyzer flag stuff
				if (analyzerFlag.equals("GN")) {
					return inFlags | PresentationLayer.GENERIC_NAME_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("VN")) {
					return inFlags | PresentationLayer.VAR_NAME_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("ET")) {
					return inFlags | PresentationLayer.EXCEPTION_THROWN_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("EH")) {
					return inFlags | PresentationLayer.EQUALS_HASHCODE_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("HC")) {
					return inFlags | PresentationLayer.HIGH_COUPLING_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("LK")) {
					return inFlags | PresentationLayer.PRINCIPLE_OF_LEAST_KNOWLEDGE_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("DR")) {
					return inFlags | PresentationLayer.DRY_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("CI")) {
					return inFlags | PresentationLayer.CODE_TO_INTERFACE_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("SI")) {
					return inFlags | PresentationLayer.SINGLETON_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("OA")) {
					return inFlags | PresentationLayer.OBJECT_ADAPTER_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("ST")) {
					return inFlags | PresentationLayer.STRATEGY_ANALYZER_FLAG;
				}
				if (analyzerFlag.equals("TM")) {
					return inFlags | PresentationLayer.TEMPLATE_METHOD_ANALYZER_FLAG;
				}

				// Passes to default if for some reason we didn't return
			default:
				System.out.println("ERROR: Unexpected or Invalid Switch Encountered!");
				System.out.println();
				break;
		}

		displayHelp();
		System.exit(1);
		return 0;
	}

	private static void displayHelp() {
		System.out.println("DesignLint v1.0-rc1");
		System.out.println("USAGE:");
		System.out.println(
				"java -jar <Path to DesignLint JAR> [-v[v[v]]] [-h] [[-a{XX|YY|...}] [-a...] ...]  <.class file | directory>");
		System.out.println();
		System.out.println("SWITCHES:");
		System.out.println("\t -v \t\t Includes summary output of analyzer-specific findings.");
		System.out.println("\t -vv \t\t Display all errors found by the analyzers.");
		System.out.println("\t\t\t Includes output of -v");
		System.out.println(
				"\t -vvv \t\t Display all output generated by analyzers (Errors, Warnings, Info, and Pattern Detection)");
		System.out.println("\t\t\t Includes output of -v and -vv");
		System.out.println();
		System.out.println("\t -h \t\t Show this help output");
		System.out.println();
		System.out.println(
				"\t -aXX \t\t Only run the analyzer specified by the code XX. This switch is used once for each analyzer desired,");
		System.out.println("\t\t\t but excluding this switch will run all available switches.");
		System.out.println();
		System.out.println("ANALYZER CODES:");
		System.out.println("\t GN - Generic Name Analyzer");
		System.out.println("\t VN - Variable Name Analyzer");
		System.out.println("\t ET - Exception Thrown Analyzer");
		System.out.println("\t EH - equals() and hashcode() Analyzer");
		System.out.println("\t HC - High Coupling Analyzer");
		System.out.println("\t LK - Principle of Least Knowledge Analyzer");
		System.out.println("\t DR - Don't Repeat Yourself (DRY) Analyzer");
		System.out.println("\t CI - Code to Interface Analyzer");
		System.out.println("\t SI - Singleton Pattern Detector");
		System.out.println("\t OA - Object Adapter Pattern Detector");
		System.out.println("\t ST - Strategy Pattern Detector");
		System.out.println("\t TM - Template Method Pattern Detector");
		System.out.println();
		System.out.println();
		System.out.println("This program uses OW2 ASM for Java bytecode analysis.");
		System.out.println("For a copy of the license used by ASM, please go to https://asm.ow2.io/license.html");
	}
}
