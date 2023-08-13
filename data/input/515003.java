import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.evaluation.*;
import proguard.evaluation.value.*;
import proguard.optimize.info.*;
public class EvaluationShrinker
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private static final boolean DEBUG_RESULTS  = false;
    private static final boolean DEBUG          = false;
    public EvaluationShrinker()
    {
        this(new PartialEvaluator(), null, null);
    }
    public EvaluationShrinker(PartialEvaluator   partialEvaluator,
                              InstructionVisitor extraDeletedInstructionVisitor,
                              InstructionVisitor extraAddedInstructionVisitor)
    {
        this.partialEvaluator               = partialEvaluator;
        this.extraDeletedInstructionVisitor = extraDeletedInstructionVisitor;
        this.extraAddedInstructionVisitor   = extraAddedInstructionVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        try
        {
            visitCodeAttribute0(clazz, method, codeAttribute);
        }
        catch (RuntimeException ex)
        {
            System.err.println("Unexpected error while shrinking instructions after partial evaluation:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            System.err.println("Not optimizing this method");
            if (DEBUG)
            {
                method.accept(clazz, new ClassPrinter());
                throw ex;
            }
        }
    }
    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG_RESULTS)
        {
            System.out.println();
            System.out.println("Class "+ClassUtil.externalClassName(clazz.getName()));
            System.out.println("Method "+ClassUtil.externalFullMethodDescription(clazz.getName(),
                                                                                 0,
                                                                                 method.getName(clazz),
                                                                                 method.getDescriptor(clazz)));
        }
        initializeNecessary(codeAttribute);
        partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);
        int codeLength = codeAttribute.u4codeLength;
        codeAttributeEditor.reset(codeLength);
        if (DEBUG) System.out.println("Invocation simplification:");
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                instruction.accept(clazz, method, codeAttribute, offset, unusedParameterSimplifier);
            }
        }
        if (DEBUG) System.out.println("Usage initialization: ");
        maxMarkedOffset = -1;
        int superInitializationOffset = partialEvaluator.superInitializationOffset();
        if (superInitializationOffset != PartialEvaluator.NONE)
        {
            if (DEBUG) System.out.print("(super.<init>)");
            markInstruction(superInitializationOffset);
        }
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                if (instruction.opcode == InstructionConstants.OP_GOTO &&
                    ((BranchInstruction)instruction).branchOffset == 0)
                {
                    if (DEBUG) System.out.print("(infinite loop)");
                    markInstruction(offset);
                }
                else if (sideEffectInstructionChecker.hasSideEffects(clazz,
                                                                     method,
                                                                     codeAttribute,
                                                                     offset,
                                                                     instruction))
                {
                    markInstruction(offset);
                }
            }
        }
        if (DEBUG) System.out.println();
        if (DEBUG) System.out.println("Usage marking:");
        while (maxMarkedOffset >= 0)
        {
            int offset = maxMarkedOffset;
            maxMarkedOffset = offset - 1;
            if (partialEvaluator.isTraced(offset))
            {
                if (isInstructionNecessary(offset))
                {
                    Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                        offset);
                    instruction.accept(clazz, method, codeAttribute, offset, producerMarker);
                }
                markStraddlingBranches(offset,
                                       partialEvaluator.branchTargets(offset),
                                       true);
                markStraddlingBranches(offset,
                                       partialEvaluator.branchOrigins(offset),
                                       false);
            }
            if (DEBUG)
            {
                if (maxMarkedOffset > offset)
                {
                    System.out.println(" -> "+maxMarkedOffset);
                }
            }
        }
        if (DEBUG) System.out.println();
        if (DEBUG) System.out.println("Initialization marking: ");
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset) &&
                !isInstructionNecessary(offset))
            {
                int variableIndex = partialEvaluator.initializedVariable(offset);
                if (variableIndex >= 0 &&
                    isVariableInitializationNecessary(clazz,
                                                      method,
                                                      codeAttribute,
                                                      offset,
                                                      variableIndex))
                {
                    markInstruction(offset);
                }
            }
        }
        if (DEBUG) System.out.println();
        if (DEBUG) System.out.println("Stack consistency fixing:");
        maxMarkedOffset = codeLength - 1;
        while (maxMarkedOffset >= 0)
        {
            int offset = maxMarkedOffset;
            maxMarkedOffset = offset - 1;
            if (partialEvaluator.isTraced(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                instruction.accept(clazz, method, codeAttribute, offset, stackConsistencyFixer);
                markStraddlingBranches(offset,
                                       partialEvaluator.branchTargets(offset),
                                       true);
                markStraddlingBranches(offset,
                                       partialEvaluator.branchOrigins(offset),
                                       false);
            }
        }
        if (DEBUG) System.out.println();
        if (DEBUG) System.out.println("Infinite loop fixing:");
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset) &&
                !isInstructionNecessary(offset)   &&
                isAllSmallerThanOrEqual(partialEvaluator.branchTargets(offset),
                                        offset)   &&
                !isAnyUnnecessaryInstructionBranchingOver(lastNecessaryInstructionOffset(offset),
                                                          offset))
            {
                replaceByInfiniteLoop(clazz, offset);
            }
        }
        if (DEBUG) System.out.println();
        if (DEBUG) System.out.println("Non-returning subroutine fixing:");
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (isInstructionNecessary(offset) &&
                partialEvaluator.isSubroutineInvocation(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                int nextOffset = offset + instruction.length(offset);
                if (!isInstructionNecessary(nextOffset))
                {
                    replaceByInfiniteLoop(clazz, nextOffset);
                }
            }
        }
        if (DEBUG) System.out.println();
        int offset = 0;
        do
        {
            Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                offset);
            if (!isInstructionNecessary(offset))
            {
                codeAttributeEditor.deleteInstruction(offset);
                codeAttributeEditor.insertBeforeInstruction(offset, (Instruction)null);
                codeAttributeEditor.replaceInstruction(offset,      (Instruction)null);
                codeAttributeEditor.insertAfterInstruction(offset,  (Instruction)null);
                if (extraDeletedInstructionVisitor != null)
                {
                    instruction.accept(clazz, method, codeAttribute, offset, extraDeletedInstructionVisitor);
                }
            }
            offset += instruction.length(offset);
        }
        while (offset < codeLength);
        if (DEBUG_RESULTS)
        {
            System.out.println("Simplification results:");
            offset = 0;
            do
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                System.out.println((isInstructionNecessary(offset) ? " + " : " - ")+instruction.toString(offset));
                if (partialEvaluator.isTraced(offset))
                {
                    int initializationOffset = partialEvaluator.initializationOffset(offset);
                    if (initializationOffset != PartialEvaluator.NONE)
                    {
                        System.out.println("     is to be initialized at ["+initializationOffset+"]");
                    }
                    InstructionOffsetValue branchTargets = partialEvaluator.branchTargets(offset);
                    if (branchTargets != null)
                    {
                        System.out.println("     has overall been branching to "+branchTargets);
                    }
                    boolean deleted = codeAttributeEditor.deleted[offset];
                    if (isInstructionNecessary(offset) && deleted)
                    {
                        System.out.println("     is deleted");
                    }
                    Instruction preInsertion = codeAttributeEditor.preInsertions[offset];
                    if (preInsertion != null)
                    {
                        System.out.println("     is preceded by: "+preInsertion);
                    }
                    Instruction replacement = codeAttributeEditor.replacements[offset];
                    if (replacement != null)
                    {
                        System.out.println("     is replaced by: "+replacement);
                    }
                    Instruction postInsertion = codeAttributeEditor.postInsertions[offset];
                    if (postInsertion != null)
                    {
                        System.out.println("     is followed by: "+postInsertion);
                    }
                }
                offset += instruction.length(offset);
            }
            while (offset < codeLength);
        }
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
    }
    private class MyUnusedParameterSimplifier
    extends       SimplifiedVisitor
    implements    InstructionVisitor, ConstantVisitor, MemberVisitor
    {
        private int                 invocationOffset;
        private ConstantInstruction invocationInstruction;
        public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
        public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
        {
            switch (constantInstruction.opcode)
            {
                case InstructionConstants.OP_INVOKEVIRTUAL:
                case InstructionConstants.OP_INVOKESPECIAL:
                case InstructionConstants.OP_INVOKESTATIC:
                case InstructionConstants.OP_INVOKEINTERFACE:
                    this.invocationOffset      = offset;
                    this.invocationInstruction = constantInstruction;
                    clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                    break;
            }
        }
        public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
        {
            refConstant.referencedMemberAccept(this);
        }
        public void visitAnyMember(Clazz clazz, Member member) {}
        public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
        {
            int parameterSize = ParameterUsageMarker.getParameterSize(programMethod);
            if ((programMethod.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) == 0 &&
                !ParameterUsageMarker.isParameterUsed(programMethod, 0))
            {
                replaceByStaticInvocation(programClass,
                                          invocationOffset,
                                          invocationInstruction);
            }
            for (int index = 0; index < parameterSize; index++)
            {
                if (!ParameterUsageMarker.isParameterUsed(programMethod, index))
                {
                    TracedStack stack =
                        partialEvaluator.getStackBefore(invocationOffset);
                    int stackIndex = stack.size() - parameterSize + index;
                    if (DEBUG)
                    {
                        System.out.println("  ["+invocationOffset+"] Ignoring parameter #"+index+" of "+programClass.getName()+"."+programMethod.getName(programClass)+programMethod.getDescriptor(programClass)+"] (stack entry #"+stackIndex+" ["+stack.getBottom(stackIndex)+"])");
                        System.out.println("    Full stack: "+stack);
                    }
                    markStackSimplificationBefore(invocationOffset, stackIndex);
                }
            }
        }
    }
    private class MyProducerMarker
    extends       SimplifiedVisitor
    implements    InstructionVisitor
    {
        public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
        {
            markStackProducers(clazz, offset, instruction);
        }
        public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
        {
            switch (simpleInstruction.opcode)
            {
                case InstructionConstants.OP_DUP:
                    conditionallyMarkStackEntryProducers(offset, 0, 0);
                    conditionallyMarkStackEntryProducers(offset, 1, 0);
                    break;
                case InstructionConstants.OP_DUP_X1:
                    conditionallyMarkStackEntryProducers(offset, 0, 0);
                    conditionallyMarkStackEntryProducers(offset, 1, 1);
                    conditionallyMarkStackEntryProducers(offset, 2, 0);
                    break;
                case InstructionConstants.OP_DUP_X2:
                    conditionallyMarkStackEntryProducers(offset, 0, 0);
                    conditionallyMarkStackEntryProducers(offset, 1, 1);
                    conditionallyMarkStackEntryProducers(offset, 2, 2);
                    conditionallyMarkStackEntryProducers(offset, 3, 0);
                    break;
                case InstructionConstants.OP_DUP2:
                    conditionallyMarkStackEntryProducers(offset, 0, 0);
                    conditionallyMarkStackEntryProducers(offset, 1, 1);
                    conditionallyMarkStackEntryProducers(offset, 2, 0);
                    conditionallyMarkStackEntryProducers(offset, 3, 1);
                    break;
                case InstructionConstants.OP_DUP2_X1:
                    conditionallyMarkStackEntryProducers(offset, 0, 0);
                    conditionallyMarkStackEntryProducers(offset, 1, 1);
                    conditionallyMarkStackEntryProducers(offset, 2, 2);
                    conditionallyMarkStackEntryProducers(offset, 3, 0);
                    conditionallyMarkStackEntryProducers(offset, 4, 1);
                    break;
                case InstructionConstants.OP_DUP2_X2:
                    conditionallyMarkStackEntryProducers(offset, 0, 0);
                    conditionallyMarkStackEntryProducers(offset, 1, 1);
                    conditionallyMarkStackEntryProducers(offset, 2, 2);
                    conditionallyMarkStackEntryProducers(offset, 3, 3);
                    conditionallyMarkStackEntryProducers(offset, 4, 0);
                    conditionallyMarkStackEntryProducers(offset, 5, 1);
                    break;
                case InstructionConstants.OP_SWAP:
                    conditionallyMarkStackEntryProducers(offset, 0, 1);
                    conditionallyMarkStackEntryProducers(offset, 1, 0);
                    break;
                default:
                    markStackProducers(clazz, offset, simpleInstruction);
                    break;
            }
        }
        public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
        {
            if (variableInstruction.opcode < InstructionConstants.OP_ISTORE)
            {
                markVariableProducers(offset, variableInstruction.variableIndex);
            }
            else
            {
                markStackProducers(clazz, offset, variableInstruction);
            }
        }
        public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
        {
            if (constantInstruction.opcode == InstructionConstants.OP_NEW)
            {
                markInitialization(offset);
            }
            markStackProducers(clazz, offset, constantInstruction);
        }
        public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
        {
            if (branchInstruction.opcode == InstructionConstants.OP_JSR ||
                branchInstruction.opcode == InstructionConstants.OP_JSR_W)
            {
                markStackEntryAfter(offset, 0);
            }
            else
            {
                markStackProducers(clazz, offset, branchInstruction);
            }
        }
    }
    private class MyStackConsistencyFixer
    extends       SimplifiedVisitor
    implements    InstructionVisitor
    {
        public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
        {
            if (isInstructionNecessary(offset))
            {
                int popCount = instruction.stackPopCount(clazz);
                if (popCount > 0)
                {
                    TracedStack tracedStack =
                        partialEvaluator.getStackBefore(offset);
                    int top = tracedStack.size() - 1;
                    int requiredPushCount = 0;
                    for (int stackIndex = 0; stackIndex < popCount; stackIndex++)
                    {
                        if (!isStackSimplifiedBefore(offset, top - stackIndex) &&
                            !isAnyStackEntryNecessaryAfter(tracedStack.getTopProducerValue(stackIndex).instructionOffsetValue(), top - stackIndex))
                        {
                            requiredPushCount++;
                        }
                    }
                    if (requiredPushCount > 0)
                    {
                        if (DEBUG) System.out.println("  Inserting before marked consumer "+instruction.toString(offset));
                        if (requiredPushCount > (instruction.isCategory2() ? 2 : 1))
                        {
                            throw new IllegalArgumentException("Unsupported stack size increment ["+requiredPushCount+"]");
                        }
                        insertPushInstructions(offset, false, tracedStack.getTop(0).computationalType());
                    }
                }
                int pushCount = instruction.stackPushCount(clazz);
                if (pushCount > 0)
                {
                    TracedStack tracedStack =
                        partialEvaluator.getStackAfter(offset);
                    int top = tracedStack.size() - 1;
                    int requiredPopCount = 0;
                    for (int stackIndex = 0; stackIndex < pushCount; stackIndex++)
                    {
                        if (!isStackEntryNecessaryAfter(offset, top - stackIndex))
                        {
                            requiredPopCount++;
                        }
                    }
                    if (requiredPopCount > 0)
                    {
                        if (DEBUG) System.out.println("  Inserting after marked producer "+instruction.toString(offset));
                        insertPopInstructions(offset, false, requiredPopCount);
                    }
                }
            }
            else
            {
                int popCount = instruction.stackPopCount(clazz);
                if (popCount > 0)
                {
                    TracedStack tracedStack =
                        partialEvaluator.getStackBefore(offset);
                    int top = tracedStack.size() - 1;
                    int expectedPopCount = 0;
                    for (int stackIndex = 0; stackIndex < popCount; stackIndex++)
                    {
                        if (isAnyStackEntryNecessaryAfter(tracedStack.getTopProducerValue(stackIndex).instructionOffsetValue(), top - stackIndex))
                        {
                            expectedPopCount++;
                        }
                    }
                    if (expectedPopCount > 0)
                    {
                        if (DEBUG) System.out.println("  Replacing unmarked consumer "+instruction.toString(offset));
                        insertPopInstructions(offset, true, expectedPopCount);
                    }
                }
                int pushCount = instruction.stackPushCount(clazz);
                if (pushCount > 0)
                {
                    TracedStack tracedStack =
                        partialEvaluator.getStackAfter(offset);
                    int top = tracedStack.size() - 1;
                    int expectedPushCount = 0;
                    for (int stackIndex = 0; stackIndex < pushCount; stackIndex++)
                    {
                        if (isStackEntryNecessaryAfter(offset, top - stackIndex))
                        {
                            expectedPushCount++;
                        }
                    }
                    if (expectedPushCount > 0)
                    {
                        if (DEBUG) System.out.println("  Replacing unmarked producer "+instruction.toString(offset));
                        insertPushInstructions(offset, true, tracedStack.getTop(0).computationalType());
                    }
                }
            }
        }
        public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
        {
            if (isInstructionNecessary(offset) &&
                isDupOrSwap(simpleInstruction))
            {
                fixDupInstruction(clazz, codeAttribute, offset, simpleInstruction);
            }
            else
            {
                visitAnyInstruction(clazz, method, codeAttribute, offset, simpleInstruction);
            }
        }
    }
    private void markVariableProducers(int consumerOffset,
                                       int variableIndex)
    {
        TracedVariables tracedVariables =
            partialEvaluator.getVariablesBefore(consumerOffset);
        markVariableProducers(tracedVariables.getProducerValue(variableIndex).instructionOffsetValue(),
                              variableIndex);
    }
    private void markVariableProducers(InstructionOffsetValue producerOffsets,
                                       int                    variableIndex)
    {
        if (producerOffsets != null)
        {
            int offsetCount = producerOffsets.instructionOffsetCount();
            for (int offsetIndex = 0; offsetIndex < offsetCount; offsetIndex++)
            {
                int offset = producerOffsets.instructionOffset(offsetIndex);
                markVariableAfter(offset, variableIndex);
                markInstruction(offset);
            }
        }
    }
    private void markStackProducers(Clazz       clazz,
                                    int         consumerOffset,
                                    Instruction consumer)
    {
        int popCount = consumer.stackPopCount(clazz);
        for (int stackIndex = 0; stackIndex < popCount; stackIndex++)
        {
            markStackEntryProducers(consumerOffset, stackIndex);
        }
    }
    private void conditionallyMarkStackEntryProducers(int consumerOffset,
                                                      int consumerStackIndex,
                                                      int producerStackIndex)
    {
        int top = partialEvaluator.getStackAfter(consumerOffset).size() - 1;
        if (isStackEntryNecessaryAfter(consumerOffset, top - consumerStackIndex))
        {
            markStackEntryProducers(consumerOffset, producerStackIndex);
        }
    }
    private void markStackEntryProducers(int consumerOffset,
                                         int stackIndex)
    {
        TracedStack tracedStack =
            partialEvaluator.getStackBefore(consumerOffset);
        int stackBottomIndex = tracedStack.size() - 1 - stackIndex;
        if (!isStackSimplifiedBefore(consumerOffset, stackBottomIndex))
        {
            markStackEntryProducers(tracedStack.getTopProducerValue(stackIndex).instructionOffsetValue(),
                                    stackBottomIndex);
        }
    }
    private void markStackEntryProducers(InstructionOffsetValue producerOffsets,
                                         int                    stackIndex)
    {
        if (producerOffsets != null)
        {
            int offsetCount = producerOffsets.instructionOffsetCount();
            for (int offsetIndex = 0; offsetIndex < offsetCount; offsetIndex++)
            {
                int offset = producerOffsets.instructionOffset(offsetIndex);
                markStackEntryAfter(offset, stackIndex);
                markInstruction(offset);
            }
        }
    }
    private void markInitialization(int newInstructionOffset)
    {
        int initializationOffset =
            partialEvaluator.initializationOffset(newInstructionOffset);
        TracedStack tracedStack =
            partialEvaluator.getStackAfter(newInstructionOffset);
        markStackEntryAfter(initializationOffset, tracedStack.size() - 1);
        markInstruction(initializationOffset);
    }
    private void markStraddlingBranches(int                    instructionOffset,
                                        InstructionOffsetValue branchOffsets,
                                        boolean                isPointingToTargets)
    {
        if (branchOffsets != null)
        {
            int branchCount = branchOffsets.instructionOffsetCount();
            for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
            {
                int branchOffset = branchOffsets.instructionOffset(branchIndex);
                if (isPointingToTargets)
                {
                    markStraddlingBranch(instructionOffset,
                                         branchOffset,
                                         instructionOffset,
                                         branchOffset);
                }
                else
                {
                    markStraddlingBranch(instructionOffset,
                                         branchOffset,
                                         branchOffset,
                                         instructionOffset);
                }
            }
        }
    }
    private void markStraddlingBranch(int instructionOffsetStart,
                                      int instructionOffsetEnd,
                                      int branchOrigin,
                                      int branchTarget)
    {
        if (!isInstructionNecessary(branchOrigin) &&
            isAnyInstructionNecessary(instructionOffsetStart, instructionOffsetEnd))
        {
            if (DEBUG) System.out.print("["+branchOrigin+"->"+branchTarget+"]");
            markInstruction(branchOrigin);
        }
    }
    private void fixDupInstruction(Clazz         clazz,
                                   CodeAttribute codeAttribute,
                                   int           dupOffset,
                                   Instruction   instruction)
    {
        int top = partialEvaluator.getStackAfter(dupOffset).size() - 1;
        byte oldOpcode = instruction.opcode;
        byte newOpcode = 0;
        switch (oldOpcode)
        {
            case InstructionConstants.OP_DUP:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, top - 0);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, top - 1);
                if (stackEntryPresent0 ||
                    stackEntryPresent1)
                {
                    if (stackEntryPresent0 &&
                        stackEntryPresent1)
                    {
                        newOpcode = InstructionConstants.OP_DUP;
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP_X1:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, top - 0);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, top - 1);
                boolean stackEntryPresent2 = isStackEntryNecessaryAfter(dupOffset, top - 2);
                if (stackEntryPresent0 ||
                    stackEntryPresent2)
                {
                    if (stackEntryPresent2)
                    {
                        int skipCount = stackEntryPresent1 ? 1 : 0;
                        if (stackEntryPresent0)
                        {
                            newOpcode = (byte)(InstructionConstants.OP_DUP + skipCount);
                        }
                        else if (skipCount == 1)
                        {
                            newOpcode = InstructionConstants.OP_SWAP;
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP_X2:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, top - 0);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, top - 1);
                boolean stackEntryPresent2 = isStackEntryNecessaryAfter(dupOffset, top - 2);
                boolean stackEntryPresent3 = isStackEntryNecessaryAfter(dupOffset, top - 3);
                if (stackEntryPresent0 ||
                    stackEntryPresent3)
                {
                    if (stackEntryPresent3)
                    {
                        int skipCount = (stackEntryPresent1 ? 1 : 0) +
                                        (stackEntryPresent2 ? 1 : 0);
                        if (stackEntryPresent0)
                        {
                            newOpcode = (byte)(InstructionConstants.OP_DUP + skipCount);
                        }
                        else if (skipCount == 1)
                        {
                            newOpcode = InstructionConstants.OP_SWAP;
                        }
                        else if (skipCount == 2)
                        {
                            throw new UnsupportedOperationException("Can't handle dup_x2 instruction moving original element across two elements at ["+dupOffset +"]");
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP2:
            {
                boolean stackEntriesPresent01 = isStackEntriesNecessaryAfter(dupOffset, top - 0, top - 1);
                boolean stackEntriesPresent23 = isStackEntriesNecessaryAfter(dupOffset, top - 2, top - 3);
                if (stackEntriesPresent01 ||
                    stackEntriesPresent23)
                {
                    if (stackEntriesPresent01 &&
                        stackEntriesPresent23)
                    {
                        newOpcode = InstructionConstants.OP_DUP2;
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP2_X1:
            {
                boolean stackEntriesPresent01 = isStackEntriesNecessaryAfter(dupOffset, top - 0, top - 1);
                boolean stackEntryPresent2    = isStackEntryNecessaryAfter(dupOffset, top - 2);
                boolean stackEntriesPresent34 = isStackEntriesNecessaryAfter(dupOffset, top - 3, top - 4);
                if (stackEntriesPresent01 ||
                    stackEntriesPresent34)
                {
                    if (stackEntriesPresent34)
                    {
                        int skipCount = stackEntryPresent2 ? 1 : 0;
                        if (stackEntriesPresent01)
                        {
                            newOpcode = (byte)(InstructionConstants.OP_DUP2 + skipCount);
                        }
                        else if (skipCount > 0)
                        {
                            throw new UnsupportedOperationException("Can't handle dup2_x1 instruction moving original element across "+skipCount+" elements at ["+dupOffset +"]");
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP2_X2:
            {
                boolean stackEntriesPresent01 = isStackEntriesNecessaryAfter(dupOffset, top - 0, top - 1);
                boolean stackEntryPresent2    = isStackEntryNecessaryAfter(dupOffset, top - 2);
                boolean stackEntryPresent3    = isStackEntryNecessaryAfter(dupOffset, top - 3);
                boolean stackEntriesPresent45 = isStackEntriesNecessaryAfter(dupOffset, top - 4, top - 5);
                if (stackEntriesPresent01 ||
                    stackEntriesPresent45)
                {
                    if (stackEntriesPresent45)
                    {
                        int skipCount = (stackEntryPresent2 ? 1 : 0) +
                                        (stackEntryPresent3 ? 1 : 0);
                        if (stackEntriesPresent01)
                        {
                            newOpcode = (byte)(InstructionConstants.OP_DUP2 + skipCount);
                        }
                        else if (skipCount > 0)
                        {
                            throw new UnsupportedOperationException("Can't handle dup2_x2 instruction moving original element across "+skipCount+" elements at ["+dupOffset +"]");
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_SWAP:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, top - 0);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, top - 1);
                if (stackEntryPresent0 ||
                    stackEntryPresent1)
                {
                    if (stackEntryPresent0 &&
                        stackEntryPresent1)
                    {
                        newOpcode = InstructionConstants.OP_SWAP;
                    }
                }
                break;
            }
        }
        if      (newOpcode == 0)
        {
            codeAttributeEditor.deleteInstruction(dupOffset);
            if (extraDeletedInstructionVisitor != null)
            {
                extraDeletedInstructionVisitor.visitSimpleInstruction(null, null, null, dupOffset, null);
            }
            if (DEBUG) System.out.println("  Marking but deleting instruction "+instruction.toString(dupOffset));
        }
        else if (newOpcode == oldOpcode)
        {
            codeAttributeEditor.undeleteInstruction(dupOffset);
            if (DEBUG) System.out.println("  Marking unchanged instruction "+instruction.toString(dupOffset));
        }
        else
        {
            Instruction replacementInstruction = new SimpleInstruction(newOpcode);
            codeAttributeEditor.replaceInstruction(dupOffset,
                                                   replacementInstruction);
            if (DEBUG) System.out.println("  Replacing instruction "+instruction.toString(dupOffset)+" by "+replacementInstruction.toString());
        }
    }
    private void insertPushInstructions(int     offset,
                                        boolean replace,
                                        int     computationalType)
    {
        markInstruction(offset);
        Instruction replacementInstruction =
            new SimpleInstruction(pushOpcode(computationalType));
        if (DEBUG) System.out.println(": "+replacementInstruction.toString(offset));
        if (replace)
        {
            codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
        }
        else
        {
            codeAttributeEditor.insertBeforeInstruction(offset, replacementInstruction);
            if (extraAddedInstructionVisitor != null)
            {
                replacementInstruction.accept(null, null, null, offset, extraAddedInstructionVisitor);
            }
        }
    }
    private byte pushOpcode(int computationalType)
    {
        switch (computationalType)
        {
            case Value.TYPE_INTEGER:            return InstructionConstants.OP_ICONST_0;
            case Value.TYPE_LONG:               return InstructionConstants.OP_LCONST_0;
            case Value.TYPE_FLOAT:              return InstructionConstants.OP_FCONST_0;
            case Value.TYPE_DOUBLE:             return InstructionConstants.OP_DCONST_0;
            case Value.TYPE_REFERENCE:
            case Value.TYPE_INSTRUCTION_OFFSET: return InstructionConstants.OP_ACONST_NULL;
        }
        throw new IllegalArgumentException("No push opcode for computational type ["+computationalType+"]");
    }
    private void insertPopInstructions(int offset, boolean replace, int popCount)
    {
        markInstruction(offset);
        switch (popCount)
        {
            case 1:
            {
                Instruction popInstruction =
                    new SimpleInstruction(InstructionConstants.OP_POP);
                if (replace)
                {
                    codeAttributeEditor.replaceInstruction(offset, popInstruction);
                }
                else
                {
                    codeAttributeEditor.insertAfterInstruction(offset, popInstruction);
                    if (extraAddedInstructionVisitor != null)
                    {
                        popInstruction.accept(null, null, null, offset, extraAddedInstructionVisitor);
                    }
                }
                break;
            }
            case 2:
            {
                Instruction popInstruction =
                    new SimpleInstruction(InstructionConstants.OP_POP2);
                if (replace)
                {
                    codeAttributeEditor.replaceInstruction(offset, popInstruction);
                }
                else
                {
                    codeAttributeEditor.insertAfterInstruction(offset, popInstruction);
                    if (extraAddedInstructionVisitor != null)
                    {
                        popInstruction.accept(null, null, null, offset, extraAddedInstructionVisitor);
                    }
                }
                break;
            }
            default:
            {
                Instruction[] popInstructions =
                    new Instruction[popCount / 2 + popCount % 2];
                Instruction popInstruction =
                    new SimpleInstruction(InstructionConstants.OP_POP2);
                for (int index = 0; index < popCount / 2; index++)
                {
                      popInstructions[index] = popInstruction;
                }
                if (popCount % 2 == 1)
                {
                    popInstruction =
                        new SimpleInstruction(InstructionConstants.OP_POP);
                    popInstructions[popCount / 2] = popInstruction;
                }
                if (replace)
                {
                    codeAttributeEditor.replaceInstruction(offset, popInstructions);
                    for (int index = 1; index < popInstructions.length; index++)
                    {
                        if (extraAddedInstructionVisitor != null)
                        {
                            popInstructions[index].accept(null, null, null, offset, extraAddedInstructionVisitor);
                        }
                    }
                }
                else
                {
                    codeAttributeEditor.insertAfterInstruction(offset, popInstructions);
                    for (int index = 0; index < popInstructions.length; index++)
                    {
                        if (extraAddedInstructionVisitor != null)
                        {
                            popInstructions[index].accept(null, null, null, offset, extraAddedInstructionVisitor);
                        }
                    }
                }
                break;
            }
        }
    }
    private void replaceByStaticInvocation(Clazz               clazz,
                                           int                 offset,
                                           ConstantInstruction constantInstruction)
    {
        Instruction replacementInstruction =
             new ConstantInstruction(InstructionConstants.OP_INVOKESTATIC,
                                     constantInstruction.constantIndex).shrink();
        if (DEBUG) System.out.println("  Replacing by static invocation "+constantInstruction.toString(offset)+" -> "+replacementInstruction.toString());
        codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
    }
    private void replaceByInfiniteLoop(Clazz clazz,
                                       int   offset)
    {
        if (DEBUG) System.out.println("  Inserting infinite loop at ["+offset+"]");
        markInstruction(offset);
        Instruction replacementInstruction =
            new BranchInstruction(InstructionConstants.OP_GOTO, 0);
        codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
    }
    private boolean isDupOrSwap(Instruction instruction)
    {
        return instruction.opcode >= InstructionConstants.OP_DUP &&
               instruction.opcode <= InstructionConstants.OP_SWAP;
    }
    private boolean isPop(Instruction instruction)
    {
        return instruction.opcode == InstructionConstants.OP_POP ||
               instruction.opcode == InstructionConstants.OP_POP2;
    }
    private boolean isAnyUnnecessaryInstructionBranchingOver(int instructionOffset1,
                                                             int instructionOffset2)
    {
        for (int offset = instructionOffset1; offset < instructionOffset2; offset++)
        {
            if (partialEvaluator.isTraced(offset) &&
                !isInstructionNecessary(offset)   &&
                isAnyLargerThan(partialEvaluator.branchTargets(offset),
                                instructionOffset2))
            {
                return true;
            }
        }
        return false;
    }
    private boolean isAllSmallerThanOrEqual(InstructionOffsetValue instructionOffsets,
                                            int                    instructionOffset)
    {
        if (instructionOffsets != null)
        {
            int branchCount = instructionOffsets.instructionOffsetCount();
            if (branchCount > 0)
            {
                for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
                {
                    if (instructionOffsets.instructionOffset(branchIndex) > instructionOffset)
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    private boolean isAnyLargerThan(InstructionOffsetValue instructionOffsets,
                                    int                    instructionOffset)
    {
        if (instructionOffsets != null)
        {
            int branchCount = instructionOffsets.instructionOffsetCount();
            if (branchCount > 0)
            {
                for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
                {
                    if (instructionOffsets.instructionOffset(branchIndex) > instructionOffset)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private void initializeNecessary(CodeAttribute codeAttribute)
    {
        int codeLength = codeAttribute.u4codeLength;
        int maxLocals  = codeAttribute.u2maxLocals;
        int maxStack   = codeAttribute.u2maxStack;
        if (variablesNecessaryAfter.length    < codeLength ||
            variablesNecessaryAfter[0].length < maxLocals)
        {
            variablesNecessaryAfter = new boolean[codeLength][maxLocals];
        }
        else
        {
            for (int offset = 0; offset < codeLength; offset++)
            {
                for (int index = 0; index < maxLocals; index++)
                {
                    variablesNecessaryAfter[offset][index] = false;
                }
            }
        }
        if (stacksNecessaryAfter.length    < codeLength ||
            stacksNecessaryAfter[0].length < maxStack)
        {
            stacksNecessaryAfter = new boolean[codeLength][maxStack];
        }
        else
        {
            for (int offset = 0; offset < codeLength; offset++)
            {
                for (int index = 0; index < maxStack; index++)
                {
                    stacksNecessaryAfter[offset][index] = false;
                }
            }
        }
        if (stacksSimplifiedBefore.length    < codeLength ||
            stacksSimplifiedBefore[0].length < maxStack)
        {
            stacksSimplifiedBefore = new boolean[codeLength][maxStack];
        }
        else
        {
            for (int offset = 0; offset < codeLength; offset++)
            {
                for (int index = 0; index < maxStack; index++)
                {
                    stacksSimplifiedBefore[offset][index] = false;
                }
            }
        }
        if (instructionsNecessary.length < codeLength)
        {
            instructionsNecessary = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                instructionsNecessary[index] = false;
            }
        }
    }
    private boolean isStackEntriesNecessaryAfter(int instructionOffset,
                                                 int stackIndex1,
                                                 int stackIndex2)
    {
        boolean present1 = isStackEntryNecessaryAfter(instructionOffset, stackIndex1);
        boolean present2 = isStackEntryNecessaryAfter(instructionOffset, stackIndex2);
        return present1 || present2;
    }
    private boolean isVariableInitializationNecessary(Clazz         clazz,
                                                      Method        method,
                                                      CodeAttribute codeAttribute,
                                                      int           initializationOffset,
                                                      int           variableIndex)
    {
        int codeLength = codeAttribute.u4codeLength;
        if (isVariableNecessaryAfterAny(0, codeLength, variableIndex))
        {
            if (DEBUG) System.out.println("Simple partial evaluation for initialization of variable v"+variableIndex+" at ["+initializationOffset+"]");
            simplePartialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);
            if (DEBUG) System.out.println("End of simple partial evaluation for initialization of variable v"+variableIndex+" at ["+initializationOffset+"]");
            for (int offset = 0; offset < codeLength; offset++)
            {
                if (isInstructionNecessary(offset))
                {
                    Value producer = partialEvaluator.getVariablesBefore(offset).getProducerValue(variableIndex);
                    if (producer != null)
                    {
                        Value simpleProducer = simplePartialEvaluator.getVariablesBefore(offset).getProducerValue(variableIndex);
                        if (simpleProducer != null)
                        {
                            InstructionOffsetValue producerOffsets =
                                producer.instructionOffsetValue();
                            InstructionOffsetValue simpleProducerOffsets =
                                simpleProducer.instructionOffsetValue();
                            if (producerOffsets.instructionOffsetCount() <
                                simpleProducerOffsets.instructionOffsetCount() &&
                                isVariableNecessaryAfterAny(producerOffsets, variableIndex) &&
                                simpleProducerOffsets.contains(initializationOffset))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    private void markVariableAfter(int instructionOffset,
                                   int variableIndex)
    {
        if (!isVariableNecessaryAfter(instructionOffset, variableIndex))
        {
            if (DEBUG) System.out.print("["+instructionOffset+".v"+variableIndex+"],");
            variablesNecessaryAfter[instructionOffset][variableIndex] = true;
            if (maxMarkedOffset < instructionOffset)
            {
                maxMarkedOffset = instructionOffset;
            }
        }
    }
    private boolean isVariableNecessaryAfterAny(int startOffset,
                                                int endOffset,
                                                int variableIndex)
    {
        for (int offset = startOffset; offset < endOffset; offset++)
        {
            if (isVariableNecessaryAfter(offset, variableIndex))
            {
                return true;
            }
        }
        return false;
    }
    private boolean isVariableNecessaryAfterAny(InstructionOffsetValue instructionOffsetValue,
                                                int                    variableIndex)
    {
        int count = instructionOffsetValue.instructionOffsetCount();
        for (int index = 0; index < count; index++)
        {
            if (isVariableNecessaryAfter(instructionOffsetValue.instructionOffset(index),
                                         variableIndex))
            {
                return true;
            }
        }
        return false;
    }
    private boolean isVariableNecessaryAfter(int instructionOffset,
                                             int variableIndex)
    {
        return instructionOffset == PartialEvaluator.AT_METHOD_ENTRY ||
               variablesNecessaryAfter[instructionOffset][variableIndex];
    }
    private void markStackEntryAfter(int instructionOffset,
                                     int stackIndex)
    {
        if (!isStackEntryNecessaryAfter(instructionOffset, stackIndex))
        {
            if (DEBUG) System.out.print("["+instructionOffset+".s"+stackIndex+"],");
            stacksNecessaryAfter[instructionOffset][stackIndex] = true;
            if (maxMarkedOffset < instructionOffset)
            {
                maxMarkedOffset = instructionOffset;
            }
        }
    }
    private boolean isAnyStackEntryNecessaryAfter(InstructionOffsetValue instructionOffsets,
                                                  int                    stackIndex)
    {
        int offsetCount = instructionOffsets.instructionOffsetCount();
        for (int offsetIndex = 0; offsetIndex < offsetCount; offsetIndex++)
        {
            if (isStackEntryNecessaryAfter(instructionOffsets.instructionOffset(offsetIndex), stackIndex))
            {
                return true;
            }
        }
        return false;
    }
    private boolean isStackEntryNecessaryAfter(int instructionOffset,
                                               int stackIndex)
    {
        return instructionOffset == PartialEvaluator.AT_CATCH_ENTRY ||
               stacksNecessaryAfter[instructionOffset][stackIndex];
    }
    private void markStackSimplificationBefore(int instructionOffset,
                                               int stackIndex)
    {
        stacksSimplifiedBefore[instructionOffset][stackIndex] = true;
    }
    private boolean isStackSimplifiedBefore(int instructionOffset,
                                            int stackIndex)
    {
        return stacksSimplifiedBefore[instructionOffset][stackIndex];
    }
    private void markInstruction(int instructionOffset)
    {
        if (!isInstructionNecessary(instructionOffset))
        {
            if (DEBUG) System.out.print(instructionOffset+",");
            instructionsNecessary[instructionOffset] = true;
            if (maxMarkedOffset < instructionOffset)
            {
                maxMarkedOffset = instructionOffset;
            }
        }
    }
    private boolean isAnyInstructionNecessary(int instructionOffset1,
                                              int instructionOffset2)
    {
        for (int instructionOffset = instructionOffset1;
             instructionOffset < instructionOffset2;
             instructionOffset++)
        {
            if (isInstructionNecessary(instructionOffset))
            {
                return true;
            }
        }
        return false;
    }
    private int lastNecessaryInstructionOffset(int instructionOffset)
    {
        for (int offset = instructionOffset-1; offset >= 0; offset--)
        {
            if (isInstructionNecessary(instructionOffset))
            {
                return offset;
            }
        }
        return 0;
    }
    private boolean isInstructionNecessary(int instructionOffset)
    {
        return instructionOffset == PartialEvaluator.AT_METHOD_ENTRY ||
               instructionsNecessary[instructionOffset];
    }
}