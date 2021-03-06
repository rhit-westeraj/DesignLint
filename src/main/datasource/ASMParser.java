package datasource;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ASMParser {

	private Map<String, ClassNode> classMap;
	List<String> reusedDataList = new ArrayList<>();
	List<MethodNode> reusedNodeList = new ArrayList<>();
	ClassNode currentClassNode;

	public ASMParser(String[] classList) throws IOException {
		this.classMap = new HashMap<String, ClassNode>();
		try {
			for (String className : classList) {
				className = className.replace('.', '/');
				ClassReader reader = new ClassReader(className);

				this.currentClassNode = new ClassNode();
				reader.accept(this.currentClassNode, ClassReader.EXPAND_FRAMES);
				classMap.put(className, this.currentClassNode);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading class definitions!");
			System.exit(1);
		}

	}

	public ASMParser(InputStream[] classStreams) throws IOException {
		this.classMap = new HashMap<>();
		try {
			for (InputStream stream : classStreams) {
				ClassReader reader = new ClassReader(stream);

				this.currentClassNode = new ClassNode();
				reader.accept(this.currentClassNode, ClassReader.EXPAND_FRAMES);

				String className = this.currentClassNode.name;
				classMap.put(className, this.currentClassNode);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading class definitions!");
			System.exit(1);
		}
	}

	/**
	 * Creates an array and places the current data in the reused data list,
	 * then clears it
	 * 
	 * @return The contents of reusedDataList before clearing
	 */
	private String[] dataListAsArray() {
		String[] returnList = new String[reusedDataList.size()];
		this.reusedDataList.toArray(returnList);
		this.reusedDataList.clear();
		return returnList;
	}

	private List<String> dataListAsList() {
		List<String> returnList = new ArrayList<>(this.reusedDataList);
		this.reusedDataList.clear();
		return returnList;
	}

	/**
	 * This method is for the presentation layer's functionality.
	 * 
	 * @return <code>String[]</code> classNames
	 */
	public String[] getParsedClassNames() {
		String[] classNames = new String[this.classMap.size()];
		this.classMap.keySet().toArray(classNames);
		return classNames;
	}

	public String getSuperName(String className) {
		this.currentClassNode = this.classMap.get(className);
		return this.currentClassNode.superName;
	}

	public String[] getInterfaces(String className) {
		this.currentClassNode = this.classMap.get(className);
		this.reusedDataList = new ArrayList<>(this.currentClassNode.interfaces);
		return dataListAsArray();
	}

	/**
	 * Returns a list of methods contained in the specified decompiled class.
	 * 
	 * @throws IllegalArgumentException If the class specified is not being parsed
	 * @param className The name of the class to retrieve methods from
	 * @return An array of strings containing all names of the methods in the class.
	 */

	public String[] getMethods(String className) {
		if (!this.classMap.containsKey(className)) {
			throw new IllegalArgumentException("Error! The specified class was not found in the parsed class map.");
		}

		this.currentClassNode = this.classMap.get(className);

		for (MethodNode node : this.currentClassNode.methods) {
			reusedDataList.add(node.name);
		}

		return dataListAsArray();
	}

	/**
	 * Returns a list of exceptions that exist in the signature of a method
	 * 
	 * @throws IllegalArgumentException If the method is not found in the
	 *                                  specified class
	 * @param className  The name of the class where the method exists in
	 * @param methodName The name of the method to retrieve the exceptions from
	 * @return An array of strings containing the names of methods in the class.
	 */

	public String[] getMethodExceptionSignature(String className, String methodName) {
		this.currentClassNode = this.classMap.get(className);

		MethodNode decompMethod = null;
		for (MethodNode node : this.currentClassNode.methods) {
			if (node.name.equals(methodName)) {
				decompMethod = node;
			}
		}

		if (decompMethod == null) {
			throw new IllegalArgumentException("Error! Specified Method was not found in the class!");
		}

		reusedDataList = new ArrayList<>(decompMethod.exceptions);
		return dataListAsArray();
	}

	/**
	 * Returns a list of exceptions types that a method catches
	 * 
	 * @throws IllegalArgumentException If the method is not found in the specified
	 *                                  class
	 * @param className  The name of the class where the method should reside in
	 * @param methodName THe name of the method to retrieve caught exception types
	 *                   from
	 * @return A string array of internal types for each exception type that is
	 *         caught. Duplicates can occur
	 * 
	 */
	public String[] getMethodExceptionCaught(String className, String methodName) {
		this.currentClassNode = this.classMap.get(className);

		MethodNode decompMethod = null;
		for (MethodNode node : this.currentClassNode.methods) {
			if (node.name.equals(methodName)) {
				decompMethod = node;
			}
		}

		if (decompMethod == null) {
			throw new IllegalArgumentException("Error! Specified Method was not found in the class!");
		}

		List<TryCatchBlockNode> caughtExceptions = decompMethod.tryCatchBlocks;
		for (TryCatchBlockNode block : caughtExceptions) {
			reusedDataList.add(block.type);
		}

		return dataListAsArray();
	}

	/**
	 * Searches through the methods of a class, and finds the onces that are public
	 * facing, static access.
	 * 
	 * @param className the class to be searched for static methods
	 * @return list of methods in the class with the static access modifier
	 */
	public String[] getStaticMethods(String className) {
		if (!this.classMap.containsKey(className)) {
			throw new IllegalArgumentException("Error! The specified class was not found in the parsed class map.");
		}

		this.currentClassNode = this.classMap.get(className);

		for (MethodNode node : this.currentClassNode.methods) {
			if (node.access == Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC) {
				reusedDataList.add(node.name);
			}
		}
		return dataListAsArray();
	}

	/**
	 * Determines if this class has a public facing constructor
	 * 
	 * @param className
	 * @return if the classes constructor is private - true. Otherwise
	 *         false
	 */
	public boolean isClassConstructorPrivate(String className) {
		this.currentClassNode = this.classMap.get(className);

		for (MethodNode method : this.currentClassNode.methods) {
			if (method.name.equals("<init>")) {
				if (method.access == Opcodes.ACC_PRIVATE) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * searches the given class for fields that have private static access modifiers
	 * 
	 * @param className
	 * @return list of fieldNames that are private static
	 */
	public String[] getClassStaticPrivateFieldNames(String className) {
		this.currentClassNode = this.classMap.get(className);
		this.reusedDataList = new ArrayList<>();

		for (FieldNode field : this.currentClassNode.fields) {

			if (field.access == Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC) {
				this.reusedDataList.add(field.name);
			}
		}

		return dataListAsArray();
	}

	public List<String> getClassFieldNames(String className) {
		this.currentClassNode = this.classMap.get(className);

		for (FieldNode field : this.currentClassNode.fields) {
			if ((field.access & Opcodes.ACC_STATIC) == 0) {
				this.reusedDataList.add(field.name);
			}
		}

		return dataListAsList();
	}

	public List<String> getGlobalNames(String className) {
		this.currentClassNode = this.classMap.get(className);

		for (FieldNode field : this.currentClassNode.fields) {
			if ((field.access & Opcodes.ACC_STATIC) != 0) {
				this.reusedDataList.add(field.name);
			}
		}

		return dataListAsList();
	}

	public Map<String, List<String>> findCorrectMethodInfo(String className, Boolean names_and_vars) {
		Map<String, List<String>> methodNames = new HashMap<>();
		this.currentClassNode = this.classMap.get(className);

		for (MethodNode method : this.currentClassNode.methods) {
			if (method.localVariables == null) {
				methodNames.put(method.name, new ArrayList<String>());
			} else {
				ArrayList<String> methodVar = new ArrayList<>();
				for (LocalVariableNode local : method.localVariables) {
					if (local.name.compareTo("this") != 0 && names_and_vars) {
						methodVar.add(local.name);
					} else if (local.name.compareTo("this") != 0 && !names_and_vars) {
						methodVar.add(local.desc);
					}
				}
				methodNames.put(method.name, methodVar);
			}
		}

		return methodNames;
	}

	public List<String> getClassFieldTypes(String className) {
		this.currentClassNode = this.classMap.get(className);
		if (this.currentClassNode == null) {
			System.out.println("Node not found");
			return null;
		}

		for (FieldNode field : this.currentClassNode.fields) {
			this.reusedDataList.add(field.desc);
		}

		return this.reusedDataList;
	}

	public List<String> getInterfacesList(String className) {
		if (this.classMap.get(className) == null) {
			try {
				ClassReader reader = new ClassReader(className);
				this.currentClassNode = new ClassNode();
				reader.accept(this.currentClassNode, ClassReader.EXPAND_FRAMES);

				this.classMap.put(className, this.currentClassNode);
				return this.currentClassNode.interfaces;
			} catch (IOException e) {
				System.out.println("Class Not Found: " + className);
				return new ArrayList<>();
			}
		} else {
			return this.classMap.get(className).interfaces;
		}
	}

	public boolean compareMethodFromInterface(String className, String methodName, String interfaceName) {
		try {
			if (!this.classMap.keySet().contains(className)) {
				ClassReader reader = new ClassReader(className);
				this.currentClassNode = new ClassNode();
				reader.accept(this.currentClassNode, ClassReader.EXPAND_FRAMES);
				this.classMap.put(className, this.currentClassNode);
			}

			if (!this.classMap.keySet().contains(interfaceName)) {
				ClassReader reader1 = new ClassReader(interfaceName);
				this.currentClassNode = new ClassNode();
				reader1.accept(this.currentClassNode, ClassReader.EXPAND_FRAMES);
				this.classMap.put(interfaceName, this.currentClassNode);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		MethodNode original;
		MethodNode compare;
		try {
			original = getMethodNode(className, methodName);
		} catch (IllegalArgumentException e) {
			original = null;
		}

		try {
			compare = getMethodNode(interfaceName, methodName);
		} catch (IllegalArgumentException e) {
			compare = null;
		}

		if (original != null && compare != null) {
			if (original.desc.compareTo(compare.desc) == 0) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public List<List<String>> getAbstractMethods(String className) {
		this.currentClassNode = this.classMap.get(className);
		this.reusedNodeList = this.currentClassNode.methods;
		List<List<String>> abstractMethods = new ArrayList<>();
		for (MethodNode method : this.reusedNodeList) {
			if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
				List<String> list = new ArrayList<>();
				list.add(method.name);
				list.add(method.desc);
				abstractMethods.add(list);
			}
		}
		return abstractMethods;
	}

	public List<List<String>> getConcreteMethods(String className) {
		this.currentClassNode = this.classMap.get(className);
		this.reusedNodeList = this.currentClassNode.methods;
		List<List<String>> abstractMethods = new ArrayList<>();
		for (MethodNode method : this.reusedNodeList) {
			if ((method.access & Opcodes.ACC_ABSTRACT) == 0) {
				List<String> list = new ArrayList<>();
				list.add(method.name);
				list.add(method.desc);
				abstractMethods.add(list);
			}
		}

		return abstractMethods;
	}

	public List<String> getAbstractMethodsInConcrete(String className, List<String> methodName,
			List<List<String>> methodList) {
		List<MethodCall> methodCalls = getMethodCalls(className, methodName.get(0));
		for (MethodCall method : methodCalls) {
			if (method.getInvokedClass().compareTo(className) == 0) {
				for (int i = 0; i < methodList.size(); i++) {
					if (methodList.get(i).get(0).compareTo(method.getCalledMethodName()) == 0) {
						MethodNode node = getMethodNode(className, method.getCalledMethodName());
						if ((node.access & Opcodes.ACC_ABSTRACT) != 0) {
							reusedDataList.add(node.name);
						}
					}
				}
			}
		}
		return dataListAsList();
	}

	public String getSignature(String className) {
		return (this.classMap.get(className).signature);
	}

	public String getSignatureNonEnum(String className) {
		return (this.classMap.get(className).access - 0x4000) < 0 ? this.getSignature(className) : null;
	}

	private MethodNode getMethodNode(String className, String methodName) {
		this.currentClassNode = this.classMap.get(className);
		this.reusedNodeList = this.currentClassNode.methods;
		MethodNode method = null;
		for (MethodNode mNode : this.reusedNodeList) {
			if (mNode.name.equals(methodName)) {
				method = mNode;
				break;
			}
		}
		if (method == null) {
			throw new IllegalArgumentException("Error! Specified Method was not found in the class!");
		}
		return method;
	}

	/**
	 * Provides a list of MethodCall Objects corresponding to method calls within
	 * the specified method
	 * 
	 * @throws IllegalArgumentException If the method is not found in the specified
	 *                                  class
	 * @param className  The name of the class where the method should reside in
	 * @param methodName The name of the method to retrieve method call information
	 *                   from
	 * @return List of MethodCall Objects
	 * 
	 */
	public List<MethodCall> getMethodCalls(String className, String methodName) {
		List<MethodCall> methodCalls = new ArrayList<MethodCall>();
		MethodNode method = this.getMethodNode(className, methodName);
		Analyzer<SourceValue> analyzer = new Analyzer<SourceValue>(new SourceInterpreter());
		Set<String> newVars = new HashSet<String>();
		Set<String> fieldStructVars = new HashSet<String>();

		try {
			Frame<SourceValue>[] frames = analyzer.analyze(className, method);
			instructions: for (int i = 0; i < frames.length; i++) {
				AbstractInsnNode insn = method.instructions.get(i);
				if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
					MethodInsnNode call = (MethodInsnNode) insn;

					if (call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals("<init>")) {
						if (!call.owner.equals("java/lang/Object") &&
								call.getNext().getType() == AbstractInsnNode.VAR_INSN) {
							VarInsnNode newVar = (VarInsnNode) call.getNext();
							if (newVar.var < method.localVariables.size()) {
								newVars.add(method.localVariables.get(newVar.var).name);
							}
							continue instructions;
						}
					}
					Map<Integer, LocalVariableNode> varsCurrentContext = this.getLocalVarContext(method, i);
					for (int j = 0; j < frames[i].getStackSize(); j++) {
						SourceValue value = (SourceValue) frames[i].getStack(j);
						for (AbstractInsnNode insn2 : value.insns) {
							switch (insn2.getType()) {
								case AbstractInsnNode.FIELD_INSN:
									methodCalls.add(new MethodCall(((MethodInsnNode) insn).name,
											Invoker.FIELD,
											((FieldInsnNode) insn2).name,
											call.owner));
									if (call.owner.length() > 9 && call.owner.substring(0, 9).equals("java/util")
											&& call.getNext().getType() == AbstractInsnNode.TYPE_INSN) {
										if (call.getNext().getNext().getType() == AbstractInsnNode.VAR_INSN) {
											VarInsnNode fieldVar = (VarInsnNode) call.getNext().getNext();
											if (varsCurrentContext.containsKey(fieldVar.var)) {
												fieldStructVars.add(varsCurrentContext.get(fieldVar.var).name);
											}
										}
									}
									continue instructions;
								case AbstractInsnNode.VAR_INSN:
									VarInsnNode varInsn = (VarInsnNode) insn2;

									Type[] arguments = Type.getArgumentTypes(method.desc);
									if (varInsn.var > 0 && varInsn.var < arguments.length + 1) {
										methodCalls.add(new MethodCall(((MethodInsnNode) insn).name,
												Invoker.PARAMETER,
												method.localVariables.get(varInsn.var).name,
												call.owner));
									} else if (!varsCurrentContext.containsKey(varInsn.var)) {
										continue instructions;
									} else if (newVars.contains(varsCurrentContext.get(varInsn.var).name)) {
										methodCalls.add(new MethodCall(((MethodInsnNode) insn).name,
												Invoker.CONSTRUCTED,
												varsCurrentContext.get(varInsn.var).name,
												call.owner));
									} else {
										Invoker type = Invoker.RETURNED;
										if (fieldStructVars.contains(varsCurrentContext.get(varInsn.var).name)) {
											type = Invoker.FIELD;
										}
										methodCalls.add(new MethodCall(((MethodInsnNode) insn).name,
												type,
												varsCurrentContext.get(varInsn.var).name,
												call.owner));
									}
									continue instructions;
								case AbstractInsnNode.METHOD_INSN:
									Invoker type = Invoker.RETURNED;
									methodCalls.add(new MethodCall(((MethodInsnNode) insn).name,
											type,
											"",
											call.owner));
									continue instructions;
								default:
									break;
							}
						}
					}
				}
			}
		} catch (AnalyzerException e) {
			e.printStackTrace();
		}

		return methodCalls;
	}

	private Map<Integer, LocalVariableNode> getLocalVarContext(MethodNode method, int index) {
		Map<Integer, LocalVariableNode> vars = new HashMap<Integer, LocalVariableNode>();
		int localVarIndex = 0;
		for (LocalVariableNode var : method.localVariables) {
			int start = 0;
			int end = 0;
			for (int i = 0; i < method.instructions.size(); i++) {
				if (method.instructions.get(i) == var.start) {
					start = i;
				}
				if (method.instructions.get(i) == var.end) {
					end = i;
				}
			}
			if (start <= index && index < end) {
				vars.put(localVarIndex, var);
				localVarIndex++;
			}
		}
		return vars;
	}

	/**
	 * Determines all of the types used by fields of a specified parsed class.
	 * This does not actually associate any information about what field has what
	 * type, rather it just gives an unordered list of all field types.
	 * 
	 * @param className The name of the class to process
	 * @return An array of unique strings containing the internal type names of
	 *         fields
	 */
	public String[] getFieldTypeNames(String className) {
		Set<String> types = new HashSet<>();
		this.currentClassNode = this.classMap.get(className);

		for (FieldNode field : this.currentClassNode.fields) {
			String internalTypeName = field.desc;
			String betterTypeName = Type.getType(internalTypeName).getInternalName();

			betterTypeName = betterTypeName.replace("[", "");

			if (betterTypeName.charAt(0) == 'L' && betterTypeName.charAt(betterTypeName.length() - 1) == ';') {
				betterTypeName = betterTypeName.substring(1, betterTypeName.length() - 1);
			}

			if (betterTypeName.length() > 1) {
				types.add(betterTypeName);
			}
		}

		String[] result = new String[types.size()];
		types.toArray(result);
		return result;
	}

	/**
	 * Determines all of the types used by method returns of a specified parsed
	 * class. This does not actually associate any information about what field has
	 * what type, rather it just gives an unordered list of all return types.
	 * 
	 * @param className The name of the class to process
	 * @return An array of unique strings containing the internal type names of
	 *         method returns
	 */

	public String[] getAllMethodReturnTypes(String className) {
		Set<String> types = new HashSet<>();
		this.currentClassNode = this.classMap.get(className);

		for (MethodNode method : this.currentClassNode.methods) {
			String betterTypeName = Type.getReturnType(method.desc).getInternalName();

			betterTypeName = betterTypeName.replaceAll("\\(.*\\)", "");

			betterTypeName = betterTypeName.replace("[", "");

			if (betterTypeName.length() > 1) {
				types.add(betterTypeName);
			}
		}

		String[] result = new String[types.size()];
		types.toArray(result);
		return result;
	}

	/**
	 * Determines all of the types used by method parameters of a specified parsed
	 * class. This does not actually associate any information about what parameter
	 * has what type, rather it just gives an unordered list of all parameter types.
	 * 
	 * @param className The name of the class to process
	 * @return An array of unique strings containing the internal type names of
	 *         method parameters
	 */
	public String[] getAllMethodParameterTypes(String className) {
		Set<String> types = new HashSet<>();
		this.currentClassNode = this.classMap.get(className);

		for (MethodNode method : this.currentClassNode.methods) {
			for (Type paramType : Type.getArgumentTypes(method.desc)) {
				String betterTypeName = "";
				if (paramType.getSort() == Type.ARRAY) {
					betterTypeName = paramType.getClassName();
					betterTypeName = betterTypeName.replace("[]", "");
					betterTypeName = betterTypeName.replace(".", "/");
				} else {
					betterTypeName = paramType.getInternalName();
					betterTypeName = betterTypeName.replace("[", "");
				}

				if (betterTypeName.length() > 1) {
					types.add(betterTypeName);
				}
			}

		}

		String[] result = new String[types.size()];
		types.toArray(result);
		return result;
	}

	public String[] getAllMethodBodyTypes(String className) {
		Set<String> types = new HashSet<>();
		this.currentClassNode = this.classMap.get(className);

		for (MethodNode method : this.currentClassNode.methods) {
			for (AbstractInsnNode instruction : method.instructions) {
				String betterTypeName = "";

				switch (instruction.getType()) {
					case AbstractInsnNode.METHOD_INSN:
						betterTypeName = ((MethodInsnNode) instruction).owner;
						break;
					case AbstractInsnNode.FIELD_INSN:
						betterTypeName = Type.getType(((FieldInsnNode) instruction).desc).getInternalName();
						String ownerTypeName = ((FieldInsnNode) instruction).owner;
						ownerTypeName = ownerTypeName.replace("[", "");
						if (ownerTypeName.length() > 1) {
							types.add(ownerTypeName);
						}
						break;

					default:
						break;
				}

				betterTypeName = betterTypeName.replace("[", "");

				if (betterTypeName.length() > 1) {
					types.add(betterTypeName);
				}
			}

		}

		String[] result = new String[types.size()];
		types.toArray(result);
		return result;
	}

	public String[] getAllMethodLocalTypes(String className) {
		Set<String> types = new HashSet<>();
		this.currentClassNode = this.classMap.get(className);

		for (MethodNode method : this.currentClassNode.methods) {
			if (method.localVariables != null) {
				for (LocalVariableNode local : method.localVariables) {

					String betterTypeName = "";
					if (Type.getType(local.desc).getSort() == Type.ARRAY) {
						betterTypeName = Type.getType(local.desc).getClassName();
						betterTypeName = betterTypeName.replace("[]", "");
						betterTypeName = betterTypeName.replace(".", "/");
					} else {
						betterTypeName = Type.getType(local.desc).getInternalName();
						betterTypeName = betterTypeName.replace("[", "");
					}

					Type.getType(local.desc).getInternalName();

					betterTypeName = betterTypeName.replace("[", "");

					if (betterTypeName.length() > 1) {
						types.add(betterTypeName);
					}
				}
			}

		}

		String[] result = new String[types.size()];
		types.toArray(result);
		return result;
	}

	public String[] getExtendsImplementsTypes(String className) {
		Set<String> types = new HashSet<>();
		this.currentClassNode = this.classMap.get(className);

		if (this.currentClassNode.interfaces != null) {
			for (String interfaceType : this.currentClassNode.interfaces) {
				types.add(interfaceType);
			}
		}

		String[] result = new String[types.size()];
		types.toArray(result);
		return result;
	}

	public boolean isInterface(String className) {
		return ((this.classMap.get(className).access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
	}

	public boolean isEnum(String className) {
		return ((this.classMap.get(className).access & Opcodes.ACC_ENUM) == Opcodes.ACC_ENUM);
	}

	public boolean isFinal(String className) {
		return (this.classMap.get(className).access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL;
	}

	public boolean allMethodsStatic(String className) {
		for (MethodNode method : this.classMap.get(className).methods) {
			if ((!method.name.equals("<init>") &&
					(method.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC)) {
				return false;
			}
		}
		return true;
	}
}
