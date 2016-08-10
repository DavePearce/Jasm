// Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright
//      notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//      notice, this list of conditions and the following disclaimer in the
//      documentation and/or other materials provided with the distribution.
//    * Neither the name of the <organization> nor the
//      names of its contributors may be used to endorse or promote products
//      derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package jasm.io;

import jasm.lang.*;

import java.io.*;
import java.util.*;


public class JasmFileWriter {	
	protected final PrintWriter output;
	
	public JasmFileWriter(OutputStream o) {
		output = new PrintWriter(o);		
	}	

	public void write(ClassFile cfile) throws IOException {
		ArrayList<Constant.Info> constantPool = cfile.constantPool();
		HashMap<Constant.Info,Integer> poolMap = new HashMap<Constant.Info,Integer>();
		
		int index = 0;
		for(Constant.Info ci : constantPool) {
			poolMap.put(ci, index++);
		}

		index = 0;
		for (Constant.Info c : constantPool) {
			if (c != null) { // item at index 0 is always null
				output.print("#" + ++index + "\t");
				output.println(c);
			}
		}
		output.println();

		writeModifiers(cfile.modifiers());
		output.print("class " + cfile.type() + " ");
		if(cfile.superClass() != null) {
			output.print(" extends " + cfile.superClass());
		}
		if(cfile.interfaces().size() > 0) {
			output.print(" implements ");
			boolean firstTime=true;
			for(JvmType.Clazz i : cfile.interfaces()) {
				if(!firstTime) {
					output.print(", ");
				}
				firstTime=false;
				output.print(i);
			}			
		}				
		
		output.println();
		
		for(BytecodeAttribute a : cfile.attributes()) {
			a.print(output,poolMap);
		}
		
		output.println(" {");
		
		for(ClassFile.Field f : cfile.fields()) {
			writeField(f,poolMap);
		}
		
		if(!cfile.fields().isEmpty()) {
			output.println();
		}
		
		for(ClassFile.Method m : cfile.methods()) {
			writeMethod(cfile,m,poolMap);
			output.println();
		}
	
		output.println("}");
		
		output.flush();
	}
	
	protected void writeField(ClassFile.Field f,
			HashMap<Constant.Info, Integer> poolMap) throws IOException {
		output.print("  ");
		writeModifiers(f.modifiers());
		writeTypeWithoutBounds(f.type());		
		output.println(" " + f.name() + ";");
		for(BytecodeAttribute a : f.attributes()) {
			a.print(output,poolMap);
		}
	}

	protected void writeMethod(ClassFile clazz, ClassFile.Method method,
			HashMap<Constant.Info, Integer> poolMap) throws IOException {
		output.print("  ");
		writeModifiers(method.modifiers());
		JvmType.Function type = method.type(); 
		
		List<JvmType.Variable> typeArgs = type.typeArguments();
		boolean firstTime=true;
		if(typeArgs.size() > 0) {
			output.print("<");
			for(JvmType.Variable tv : typeArgs) {
				if(!firstTime) {
					output.print(", ");
				}
				firstTime=false;
				output.print(tv);
			}
			output.print("> ");
		}
		
		writeTypeWithoutBounds(type.returnType());
		output.print(" " + method.name());
		output.print("(");		
		firstTime=true;
		
		List<JvmType> paramTypes = type.parameterTypes();				
		
		for(int i = 0; i != paramTypes.size();++i) {
			if(!firstTime) {
				output.print(", ");
			}
			firstTime=false;					
			writeTypeWithoutBounds(paramTypes.get(i));
		}
		
		output.println(");");
		
		for(BytecodeAttribute a : method.attributes()) {			
			a.print(output,poolMap);			
		}					
	}	
	protected void writeModifiers(List<Modifier> modifiers) {	
		writeModifiers(modifiers,output);
	}
	
	protected void writeTypeWithoutBounds(JvmType t) {
		if(t instanceof JvmType.Variable) {
			JvmType.Variable v = (JvmType.Variable) t;
			output.write(v.variable());
		} else {
			output.write(t.toString());
		}
	}
	
	public static void writeModifiers(List<Modifier> modifiers, PrintWriter output) {
		for (Modifier x : modifiers) {			
			if (x instanceof Modifier.Private) {
				output.write("private ");
			} else if (x instanceof Modifier.Protected) {
				output.write("protected ");
			} else if (x instanceof Modifier.Public) {
				output.write("public ");
			} else if (x instanceof Modifier.Static) {
				output.write("static ");
			} else if (x instanceof Modifier.Abstract) {
				output.write("abstract ");
			} else if (x instanceof Modifier.Final) {
				output.write("final ");
			} else if (x instanceof Modifier.Super) {
				output.write("super ");
			} else if (x instanceof Modifier.Bridge) {
				output.write("bridge ");
			} else if (x instanceof Modifier.Enum) {
				output.write("enum ");
			} else if (x instanceof Modifier.Synthetic) {
				output.write("synthetic ");
			} else if (x instanceof Modifier.Native) {
				output.write("native ");
			} else if (x instanceof Modifier.StrictFP) {
				output.write("strictfp ");
			} else if (x instanceof Modifier.Synchronized) {
				output.write("synchronized ");
			} else if (x instanceof Modifier.Transient) {
				output.write("transient ");
			} else if (x instanceof Modifier.Volatile) {
				output.write("volatile ");
			} else {
				output.write("unknown ");
			}
		}
	}	
}
