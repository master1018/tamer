public class test {
    public static void main(String[] args) {
        try {
            out = new FileWriter(System.getProperty("generateToDir") + "/OPT_Assembler.java");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            lowLevelAsm = Class.forName("com.ibm.JikesRVM.VM_Assembler");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        emit("package com.ibm.JikesRVM.opt;\n\n");
        emit("import com.ibm.JikesRVM.*;\n\n");
        emit("import com.ibm.JikesRVM.opt.ir.*;\n\n");
        emit("\n\n");
        emit("\n");
        emit("class OPT_Assembler extends OPT_AssemblerBase {\n\n");
        emitTab(1);
        emit("\n");
        emitTab(1);
        emit("OPT_Assembler(int bcSize, boolean print) {\n");
        emitTab(2);
        emit("super(bcSize, print);\n");
        emitTab(1);
        emit("}");
        emit("\n\n");
        Method[] emitters = lowLevelAsm.getDeclaredMethods();
        Set opcodes = getOpcodes(emitters);
        Iterator i = opcodes.iterator();
        while (i.hasNext()) {
            String opcode = (String) i.next();
            setCurrentOpcode(opcode);
            emitTab(1);
            emit("\n");
            emitTab(1);
            emit("private void do" + opcode + "(OPT_Instruction inst) {\n");
            EmitterSet emitter = buildSetForOpcode(emitters, opcode);
            boolean[][] tp = new boolean[4][encoding.length];
            emitter.emitSet(opcode, tp, 2);
            emitTab(1);
            emit("}\n\n");
        }
        emitTab(1);
        emit("\n");
        emitTab(1);
        emit("private int instructionCount = 0;\n\n");
        emitTab(1);
        emit("\n");
        emitTab(1);
        emit("void doInst(OPT_Instruction inst) {\n");
        emitTab(2);
        emit("resolveForwardReferences(++instructionCount);\n");
        emitTab(2);
        emit("switch (inst.getOpcode()) {\n");
        Set emittedOpcodes = new HashSet();
        i = opcodes.iterator();
        while (i.hasNext()) {
            String opcode = (String) i.next();
            Iterator operators = getMatchingOperators(opcode).iterator();
            while (operators.hasNext()) {
                Object operator = operators.next();
                emitTab(3);
                emittedOpcodes.add(operator);
                emit("case IA32_" + operator + "_opcode:\n");
            }
            emitTab(4);
            emit("do" + opcode + "(inst);\n");
            emitTab(4);
            emit("break;\n");
        }
        emittedOpcodes.add("LOCK");
        emitTab(3);
        emit("case IA32_LOCK_opcode:\n");
        emitTab(4);
        emit("emitLockNextInstruction();\n");
        emitTab(4);
        emit("break;\n");
        emittedOpcodes.add("LOCK");
        emitTab(3);
        emit("case IG_PATCH_POINT_opcode:\n");
        emitTab(4);
        emit("emitPatchPoint();\n");
        emitTab(4);
        emit("break;\n");
        Set errorOpcodes = getErrorOpcodes(emittedOpcodes);
        if (!errorOpcodes.isEmpty()) {
            i = errorOpcodes.iterator();
            while (i.hasNext()) {
                emitTab(3);
                emit("case IA32_" + i.next() + "_opcode:\n");
            }
            emitTab(4);
            emit("throw new OPT_OptimizingCompilerException(inst + \" has unimplemented IA32 opcode (check excludedOpcodes)\");\n");
        }
        emitTab(2);
        emit("}\n");
        emitTab(2);
        emit("inst.setmcOffset( mi );\n");
        emitTab(1);
        emit("}\n\n");
        emit("\n}\n");
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
