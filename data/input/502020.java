package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class InstructionSequenceMatcher
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor
{
    private static final boolean DEBUG      = false;
    private static final boolean DEBUG_MORE = false;
    public static final int X = 0x40000000;
    public static final int Y = 0x40000001;
    public static final int Z = 0x40000002;
    public static final int A = 0x40000003;
    public static final int B = 0x40000004;
    public static final int C = 0x40000005;
    public static final int D = 0x40000006;
    private final Constant[]    patternConstants;
    private final Instruction[] patternInstructions;
    private boolean     matching;
    private boolean     matchingAnyWildCards;
    private int         patternInstructionIndex;
    private final int[] matchedInstructionOffsets;
    private int         matchedArgumentFlags;
    private final int[] matchedArguments = new int[7];
    private long        matchedConstantFlags;
    private final int[] matchedConstantIndices;
    private Constant patternConstant;
    private boolean  matchingConstant;
    public InstructionSequenceMatcher(Constant[]    patternConstants,
                                      Instruction[] patternInstructions)
    {
        this.patternConstants    = patternConstants;
        this.patternInstructions = patternInstructions;
        matchedInstructionOffsets = new int[patternInstructions.length];
        matchedConstantIndices    = new int[patternConstants.length];
    }
    public void reset()
    {
        patternInstructionIndex = 0;
        matchedArgumentFlags    = 0;
        matchedConstantFlags    = 0L;
    }
    public boolean isMatching()
    {
        return matching;
    }
    public boolean isMatchingAnyWildcards()
    {
        return matchingAnyWildCards;
    }
    public int instructionCount()
    {
        return patternInstructions.length;
    }
    public int matchedInstructionOffset(int index)
    {
        return matchedInstructionOffsets[index];
    }
    public int matchedArgument(int argument)
    {
        int argumentIndex = argument - X;
        return argumentIndex < 0 ?
            argument :
            matchedArguments[argumentIndex];
    }
    public int[] matchedArguments(int[] arguments)
    {
        int[] matchedArguments = new int[arguments.length];
        for (int index = 0; index < arguments.length; index++)
        {
            matchedArguments[index] = matchedArgument(arguments[index]);
        }
        return matchedArguments;
    }
    public int matchedConstantIndex(int constantIndex)
    {
        int argumentIndex = constantIndex - X;
        return argumentIndex < 0 ?
            matchedConstantIndices[constantIndex] :
            matchedArguments[argumentIndex];
    }
    public int matchedBranchOffset(int offset, int branchOffset)
    {
        int argumentIndex = branchOffset - X;
        return argumentIndex < 0 ?
            branchOffset :
            matchedArguments[argumentIndex] - offset;
    }
    public int[] matchedJumpOffsets(int offset, int[] jumpOffsets)
    {
        int[] matchedJumpOffsets = new int[jumpOffsets.length];
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            matchedJumpOffsets[index] = matchedBranchOffset(offset,
                                                            jumpOffsets[index]);
        }
        return matchedJumpOffsets;
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];
        boolean condition =
            matchingOpcodes(simpleInstruction, patternInstruction) &&
            matchingArguments(simpleInstruction.constant,
                              ((SimpleInstruction)patternInstruction).constant);
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   simpleInstruction);
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];
        boolean condition =
            matchingOpcodes(variableInstruction, patternInstruction) &&
            matchingArguments(variableInstruction.variableIndex,
                              ((VariableInstruction)patternInstruction).variableIndex) &&
            matchingArguments(variableInstruction.constant,
                              ((VariableInstruction)patternInstruction).constant);
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   variableInstruction);
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];
        boolean condition =
            matchingOpcodes(constantInstruction, patternInstruction) &&
            matchingConstantIndices(clazz,
                                    constantInstruction.constantIndex,
                                    ((ConstantInstruction)patternInstruction).constantIndex) &&
            matchingArguments(constantInstruction.constant,
                              ((ConstantInstruction)patternInstruction).constant);
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   constantInstruction);
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];
        boolean condition =
            matchingOpcodes(branchInstruction, patternInstruction) &&
            matchingBranchOffsets(offset,
                                  branchInstruction.branchOffset,
                                  ((BranchInstruction)patternInstruction).branchOffset);
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   branchInstruction);
    }
    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];
        boolean condition =
            matchingOpcodes(tableSwitchInstruction, patternInstruction) &&
            matchingBranchOffsets(offset,
                                  tableSwitchInstruction.defaultOffset,
                                  ((TableSwitchInstruction)patternInstruction).defaultOffset) &&
            matchingArguments(tableSwitchInstruction.lowCase,
                              ((TableSwitchInstruction)patternInstruction).lowCase)  &&
            matchingArguments(tableSwitchInstruction.highCase,
                              ((TableSwitchInstruction)patternInstruction).highCase) &&
            matchingJumpOffsets(offset,
                                tableSwitchInstruction.jumpOffsets,
                                ((TableSwitchInstruction)patternInstruction).jumpOffsets);
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   tableSwitchInstruction);
    }
    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        Instruction patternInstruction = patternInstructions[patternInstructionIndex];
        boolean condition =
            matchingOpcodes(lookUpSwitchInstruction, patternInstruction) &&
            matchingBranchOffsets(offset,
                                  lookUpSwitchInstruction.defaultOffset,
                                  ((LookUpSwitchInstruction)patternInstruction).defaultOffset) &&
            matchingArguments(lookUpSwitchInstruction.cases,
                              ((LookUpSwitchInstruction)patternInstruction).cases) &&
            matchingJumpOffsets(offset,
                                lookUpSwitchInstruction.jumpOffsets,
                                ((LookUpSwitchInstruction)patternInstruction).jumpOffsets);
        checkMatch(condition,
                   clazz,
                   method,
                   codeAttribute,
                   offset,
                   lookUpSwitchInstruction);
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        IntegerConstant integerPatternConstant = (IntegerConstant)patternConstant;
        matchingConstant = integerConstant.getValue() ==
                           integerPatternConstant.getValue();
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        LongConstant longPatternConstant = (LongConstant)patternConstant;
        matchingConstant = longConstant.getValue() ==
                           longPatternConstant.getValue();
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        FloatConstant floatPatternConstant = (FloatConstant)patternConstant;
        matchingConstant = floatConstant.getValue() ==
                           floatPatternConstant.getValue();
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        DoubleConstant doublePatternConstant = (DoubleConstant)patternConstant;
        matchingConstant = doubleConstant.getValue() ==
                           doublePatternConstant.getValue();
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        StringConstant stringPatternConstant = (StringConstant)patternConstant;
        matchingConstant =
            matchingConstantIndices(clazz,
                                    stringConstant.u2stringIndex,
                                    stringPatternConstant.u2stringIndex);
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        Utf8Constant utf8PatternConstant = (Utf8Constant)patternConstant;
        matchingConstant = utf8Constant.getString().equals(
                           utf8PatternConstant.getString());
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        RefConstant refPatternConstant = (RefConstant)patternConstant;
        matchingConstant =
            matchingConstantIndices(clazz,
                                    refConstant.getClassIndex(),
                                    refPatternConstant.getClassIndex()) &&
            matchingConstantIndices(clazz,
                                    refConstant.getNameAndTypeIndex(),
                                    refPatternConstant.getNameAndTypeIndex());
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        ClassConstant classPatternConstant = (ClassConstant)patternConstant;
        matchingConstant =
            matchingConstantIndices(clazz,
                                    classConstant.u2nameIndex,
                                    classPatternConstant.u2nameIndex);
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        NameAndTypeConstant typePatternConstant = (NameAndTypeConstant)patternConstant;
        matchingConstant =
            matchingConstantIndices(clazz,
                                    nameAndTypeConstant.u2nameIndex,
                                    typePatternConstant.u2nameIndex) &&
            matchingConstantIndices(clazz,
                                    nameAndTypeConstant.u2descriptorIndex,
                                    typePatternConstant.u2descriptorIndex);
    }
    private boolean matchingOpcodes(Instruction instruction1,
                                    Instruction instruction2)
    {
        return instruction1.opcode            == instruction2.opcode ||
               instruction1.canonicalOpcode() == instruction2.opcode;
    }
    private boolean matchingArguments(int argument1,
                                      int argument2)
    {
        int argumentIndex = argument2 - X;
        if (argumentIndex < 0)
        {
            return argument1 == argument2;
        }
        else if ((matchedArgumentFlags & (1 << argumentIndex)) == 0)
        {
            matchedArguments[argumentIndex] = argument1;
            matchedArgumentFlags |= 1 << argumentIndex;
            return true;
        }
        else
        {
            return matchedArguments[argumentIndex] == argument1;
        }
    }
    private boolean matchingArguments(int[] arguments1,
                                      int[] arguments2)
    {
        if (arguments1.length != arguments2.length)
        {
            return false;
        }
        for (int index = 0; index < arguments1.length; index++)
        {
            if (!matchingArguments(arguments1[index], arguments2[index]))
            {
                return false;
            }
        }
        return true;
    }
    private boolean matchingConstantIndices(Clazz clazz,
                                            int   constantIndex1,
                                            int   constantIndex2)
    {
        if (constantIndex2 >= X)
        {
            return matchingArguments(constantIndex1, constantIndex2);
        }
        else if ((matchedConstantFlags & (1L << constantIndex2)) == 0)
        {
            matchingConstant = false;
            patternConstant  = patternConstants[constantIndex2];
            if (clazz.getTag(constantIndex1) == patternConstant.getTag())
            {
                clazz.constantPoolEntryAccept(constantIndex1, this);
                if (matchingConstant)
                {
                    matchedConstantIndices[constantIndex2] = constantIndex1;
                    matchedConstantFlags |= 1L << constantIndex2;
                }
            }
            return matchingConstant;
        }
        else
        {
            return matchedConstantIndices[constantIndex2] == constantIndex1;
        }
    }
    private boolean matchingBranchOffsets(int offset,
                                          int branchOffset1,
                                          int branchOffset2)
    {
        int argumentIndex = branchOffset2 - X;
        if (argumentIndex < 0)
        {
            return branchOffset1 == branchOffset2;
        }
        else if ((matchedArgumentFlags & (1 << argumentIndex)) == 0)
        {
            matchedArguments[argumentIndex] = offset + branchOffset1;
            matchedArgumentFlags |= 1 << argumentIndex;
            return true;
        }
        else
        {
            return matchedArguments[argumentIndex] == offset + branchOffset1;
        }
    }
    private boolean matchingJumpOffsets(int   offset,
                                        int[] jumpOffsets1,
                                        int[] jumpOffsets2)
    {
        if (jumpOffsets1.length != jumpOffsets2.length)
        {
            return false;
        }
        for (int index = 0; index < jumpOffsets1.length; index++)
        {
            if (!matchingBranchOffsets(offset,
                                       jumpOffsets1[index],
                                       jumpOffsets2[index]))
            {
                return false;
            }
        }
        return true;
    }
    private void checkMatch(boolean       condition,
                            Clazz         clazz,
                            Method        method,
                            CodeAttribute codeAttribute,
                            int           offset,
                            Instruction   instruction)
    {
        if (DEBUG_MORE)
        {
            System.out.println("InstructionSequenceMatcher: ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]: "+patternInstructions[patternInstructionIndex].toString(patternInstructionIndex)+(condition?"\t== ":"\t   ")+instruction.toString(offset));
        }
        if (condition)
        {
            matchedInstructionOffsets[patternInstructionIndex] = offset;
            patternInstructionIndex++;
            matching = patternInstructionIndex == patternInstructions.length;
            matchingAnyWildCards = matchedArgumentFlags != 0;
            if (matching)
            {
                if (DEBUG)
                {
                    System.out.println("InstructionSequenceMatcher: ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]");
                    for (int index = 0; index < patternInstructionIndex; index++)
                    {
                        System.out.println("    "+InstructionFactory.create(codeAttribute.code, matchedInstructionOffsets[index]).toString(matchedInstructionOffsets[index]));
                    }
                }
                reset();
            }
        }
        else
        {
            matching = false;
            boolean retry = patternInstructionIndex == 1;
            reset();
            if (retry)
            {
                instruction.accept(clazz, method, codeAttribute, offset, this);
            }
        }
    }
}
