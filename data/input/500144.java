import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.instruction.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.evaluation.*;
import proguard.evaluation.value.*;
import proguard.optimize.peephole.BranchTargetFinder;
public class PartialEvaluator
extends      SimplifiedVisitor
implements   AttributeVisitor,
             ExceptionInfoVisitor
{
    private static final boolean DEBUG         = false;
    private static final boolean DEBUG_RESULTS = false;
    public PartialEvaluator()
    {
        this(new ValueFactory(),
             new BasicInvocationUnit(new ValueFactory()),
             true);
    }
    public PartialEvaluator(ValueFactory   valueFactory,
                            InvocationUnit invocationUnit,
                            boolean        evaluateAllCode)
    {
        this(valueFactory,
             invocationUnit,
             evaluateAllCode,
             evaluateAllCode ?
                 new BasicBranchUnit() :
                 new TracedBranchUnit(),
             new BranchTargetFinder(),
             null);
    }
    private PartialEvaluator(PartialEvaluator partialEvaluator)
    {
        this(partialEvaluator.valueFactory,
             partialEvaluator.invocationUnit,
             partialEvaluator.evaluateAllCode,
             partialEvaluator.branchUnit,
             partialEvaluator.branchTargetFinder,
             partialEvaluator.instructionBlockStack);
    }
    private PartialEvaluator(ValueFactory       valueFactory,
                             InvocationUnit     invocationUnit,
                             boolean            evaluateAllCode,
                             BasicBranchUnit    branchUnit,
                             BranchTargetFinder branchTargetFinder,
                             java.util.Stack    callingInstructionBlockStack)
    {
        this.valueFactory       = valueFactory;
        this.invocationUnit     = invocationUnit;
        this.evaluateAllCode    = evaluateAllCode;
        this.branchUnit         = branchUnit;
        this.branchTargetFinder = branchTargetFinder;
        this.callingInstructionBlockStack = callingInstructionBlockStack == null ?
            this.instructionBlockStack :
            callingInstructionBlockStack;
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
            System.err.println("Unexpected error while performing partial evaluation:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            if (DEBUG)
            {
                method.accept(clazz, new ClassPrinter());
            }
            throw ex;
        }
    }
    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println();
            System.out.println("Partial evaluation: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
            System.out.println("  Max locals = "+codeAttribute.u2maxLocals);
            System.out.println("  Max stack  = "+codeAttribute.u2maxStack);
        }
        TracedVariables variables = new TracedVariables(codeAttribute.u2maxLocals);
        TracedStack     stack     = new TracedStack(codeAttribute.u2maxStack);
        initializeVariables(clazz, method, codeAttribute, variables, stack);
        codeAttribute.accept(clazz, method, branchTargetFinder);
        evaluateInstructionBlockAndExceptionHandlers(clazz,
                                                     method,
                                                     codeAttribute,
                                                     variables,
                                                     stack,
                                                     0,
                                                     codeAttribute.u4codeLength);
        if (DEBUG_RESULTS)
        {
            System.out.println("Evaluation results:");
            int offset = 0;
            do
            {
                if (isBranchOrExceptionTarget(offset))
                {
                    System.out.println("Branch target from ["+branchOriginValues[offset]+"]:");
                    if (isTraced(offset))
                    {
                        System.out.println("  Vars:  "+variablesBefore[offset]);
                        System.out.println("  Stack: "+stacksBefore[offset]);
                    }
                }
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                System.out.println(instruction.toString(offset));
                if (isTraced(offset))
                {
                    int variableIndex = initializedVariable(offset);
                    if (variableIndex >= 0)
                    {
                        System.out.println("     is initializing variable v"+variableIndex);
                    }
                    int initializationOffset = branchTargetFinder.initializationOffset(offset);
                    if (initializationOffset != NONE)
                    {
                        System.out.println("     is to be initialized at ["+initializationOffset+"]");
                    }
                    InstructionOffsetValue branchTargets = branchTargets(offset);
                    if (branchTargets != null)
                    {
                        System.out.println("     has overall been branching to "+branchTargets);
                    }
                    System.out.println("  Vars:  "+variablesAfter[offset]);
                    System.out.println("  Stack: "+stacksAfter[offset]);
                }
                offset += instruction.length(offset);
            }
            while (offset < codeAttribute.u4codeLength);
        }
    }
    public boolean isTraced(int startOffset, int endOffset)
    {
        for (int index = startOffset; index < endOffset; index++)
        {
            if (isTraced(index))
            {
                return true;
            }
        }
        return false;
    }
    public boolean isTraced(int instructionOffset)
    {
        return evaluationCounts[instructionOffset] > 0;
    }
    public boolean isInstruction(int instructionOffset)
    {
        return branchTargetFinder.isInstruction(instructionOffset);
    }
    public boolean isBranchOrExceptionTarget(int instructionOffset)
    {
        return branchTargetFinder.isBranchTarget(instructionOffset) ||
               branchTargetFinder.isExceptionHandler(instructionOffset);
    }
    public boolean isSubroutineStart(int instructionOffset)
    {
        return branchTargetFinder.isSubroutineStart(instructionOffset);
    }
    public boolean isSubroutineInvocation(int instructionOffset)
    {
        return branchTargetFinder.isSubroutineInvocation(instructionOffset);
    }
    public boolean isSubroutine(int instructionOffset)
    {
        return branchTargetFinder.isSubroutine(instructionOffset);
    }
    public boolean isSubroutineReturning(int instructionOffset)
    {
        return branchTargetFinder.isSubroutineReturning(instructionOffset);
    }
    public int subroutineEnd(int instructionOffset)
    {
        return branchTargetFinder.subroutineEnd(instructionOffset);
    }
    public int initializationOffset(int instructionOffset)
    {
        return branchTargetFinder.initializationOffset(instructionOffset);
    }
    public boolean isInitializer()
    {
        return branchTargetFinder.isInitializer();
    }
    public int superInitializationOffset()
    {
        return branchTargetFinder.superInitializationOffset();
    }
    public int creationOffset(int offset)
    {
        return branchTargetFinder.creationOffset(offset);
    }
    public TracedVariables getVariablesBefore(int instructionOffset)
    {
        return variablesBefore[instructionOffset];
    }
    public TracedVariables getVariablesAfter(int instructionOffset)
    {
        return variablesAfter[instructionOffset];
    }
    public TracedStack getStackBefore(int instructionOffset)
    {
        return stacksBefore[instructionOffset];
    }
    public TracedStack getStackAfter(int instructionOffset)
    {
        return stacksAfter[instructionOffset];
    }
    public InstructionOffsetValue branchOrigins(int instructionOffset)
    {
        return branchOriginValues[instructionOffset];
    }
    public InstructionOffsetValue branchTargets(int instructionOffset)
    {
        return branchTargetValues[instructionOffset];
    }
    public int initializedVariable(int instructionOffset)
    {
        return initializedVariables[instructionOffset];
    }
    private void pushCallingInstructionBlock(TracedVariables variables,
                                             TracedStack     stack,
                                             int             startOffset)
    {
        callingInstructionBlockStack.push(new MyInstructionBlock(variables,
                                                                 stack,
                                                                 startOffset));
    }
    private void pushInstructionBlock(TracedVariables variables,
                                      TracedStack     stack,
                                      int             startOffset)
    {
        instructionBlockStack.push(new MyInstructionBlock(variables,
                                                          stack,
                                                          startOffset));
    }
    private void evaluateInstructionBlockAndExceptionHandlers(Clazz           clazz,
                                                              Method          method,
                                                              CodeAttribute   codeAttribute,
                                                              TracedVariables variables,
                                                              TracedStack     stack,
                                                              int             startOffset,
                                                              int             endOffset)
    {
        evaluateInstructionBlock(clazz,
                                 method,
                                 codeAttribute,
                                 variables,
                                 stack,
                                 startOffset);
        evaluateExceptionHandlers(clazz,
                                  method,
                                  codeAttribute,
                                  startOffset,
                                  endOffset);
    }
    private void evaluateInstructionBlock(Clazz           clazz,
                                          Method          method,
                                          CodeAttribute   codeAttribute,
                                          TracedVariables variables,
                                          TracedStack     stack,
                                          int             startOffset)
    {
        evaluateSingleInstructionBlock(clazz,
                                       method,
                                       codeAttribute,
                                       variables,
                                       stack,
                                       startOffset);
        while (!instructionBlockStack.empty())
        {
            if (DEBUG) System.out.println("Popping alternative branch out of "+instructionBlockStack.size()+" blocks");
            MyInstructionBlock instructionBlock =
                (MyInstructionBlock)instructionBlockStack.pop();
            evaluateSingleInstructionBlock(clazz,
                                           method,
                                           codeAttribute,
                                           instructionBlock.variables,
                                           instructionBlock.stack,
                                           instructionBlock.startOffset);
        }
    }
    private void evaluateSingleInstructionBlock(Clazz            clazz,
                                                Method           method,
                                                CodeAttribute    codeAttribute,
                                                TracedVariables  variables,
                                                TracedStack      stack,
                                                int              startOffset)
    {
        byte[] code = codeAttribute.code;
        if (DEBUG)
        {
             System.out.println("Instruction block starting at ["+startOffset+"] in "+
                                ClassUtil.externalFullMethodDescription(clazz.getName(),
                                                                        0,
                                                                        method.getName(clazz),
                                                                        method.getDescriptor(clazz)));
             System.out.println("Init vars:  "+variables);
             System.out.println("Init stack: "+stack);
        }
        Processor processor = new Processor(variables,
                                            stack,
                                            valueFactory,
                                            branchUnit,
                                            invocationUnit);
        int instructionOffset = startOffset;
        int maxOffset = startOffset;
        while (true)
        {
            if (maxOffset < instructionOffset)
            {
                maxOffset = instructionOffset;
            }
            int evaluationCount = evaluationCounts[instructionOffset];
            if (evaluationCount == 0)
            {
                if (variablesBefore[instructionOffset] == null)
                {
                    variablesBefore[instructionOffset] = new TracedVariables(variables);
                    stacksBefore[instructionOffset]    = new TracedStack(stack);
                }
                else
                {
                    variablesBefore[instructionOffset].initialize(variables);
                    stacksBefore[instructionOffset].copy(stack);
                }
                generalizedContexts[instructionOffset] = true;
            }
            else
            {
                boolean variablesChanged = variablesBefore[instructionOffset].generalize(variables, true);
                boolean stackChanged     = stacksBefore[instructionOffset].generalize(stack);
                if (!variablesChanged &&
                    !stackChanged     &&
                    generalizedContexts[instructionOffset])
                {
                    if (DEBUG) System.out.println("Repeated variables, stack, and branch targets");
                    break;
                }
                if (evaluationCount >= MAXIMUM_EVALUATION_COUNT)
                {
                    if (DEBUG) System.out.println("Generalizing current context after "+evaluationCount+" evaluations");
                    variables.generalize(variablesBefore[instructionOffset], false);
                    stack.generalize(stacksBefore[instructionOffset]);
                    generalizedContexts[instructionOffset] = true;
                }
                else
                {
                    generalizedContexts[instructionOffset] = false;
                }
            }
            evaluationCounts[instructionOffset]++;
            Value storeValue = new InstructionOffsetValue(instructionOffset);
            variables.setProducerValue(storeValue);
            stack.setProducerValue(storeValue);
            InstructionOffsetValue traceValue = InstructionOffsetValue.EMPTY_VALUE;
            variables.resetInitialization();
            Instruction instruction = InstructionFactory.create(code, instructionOffset);
            int nextInstructionOffset = instructionOffset +
                                        instruction.length(instructionOffset);
            InstructionOffsetValue nextInstructionOffsetValue = new InstructionOffsetValue(nextInstructionOffset);
            branchUnit.resetCalled();
            branchUnit.setTraceBranchTargets(nextInstructionOffsetValue);
            if (DEBUG)
            {
                System.out.println(instruction.toString(instructionOffset));
            }
            try
            {
                instruction.accept(clazz,
                                   method,
                                   codeAttribute,
                                   instructionOffset,
                                   processor);
            }
            catch (RuntimeException ex)
            {
                System.err.println("Unexpected error while evaluating instruction:");
                System.err.println("  Class       = ["+clazz.getName()+"]");
                System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
                System.err.println("  Instruction = "+instruction.toString(instructionOffset));
                System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
                throw ex;
            }
            initializedVariables[instructionOffset] = variables.getInitializationIndex();
            InstructionOffsetValue branchTargets = branchUnit.getTraceBranchTargets();
            int branchTargetCount = branchTargets.instructionOffsetCount();
            branchUnit.setTraceBranchTargets(traceValue);
            if (DEBUG)
            {
                if (branchUnit.wasCalled())
                {
                    System.out.println("     is branching to "+branchTargets);
                }
                if (branchTargetValues[instructionOffset] != null)
                {
                    System.out.println("     has up till now been branching to "+branchTargetValues[instructionOffset]);
                }
                System.out.println(" Vars:  "+variables);
                System.out.println(" Stack: "+stack);
            }
            if (evaluationCount == 0)
            {
                if (variablesAfter[instructionOffset] == null)
                {
                    variablesAfter[instructionOffset] = new TracedVariables(variables);
                    stacksAfter[instructionOffset]    = new TracedStack(stack);
                }
                else
                {
                    variablesAfter[instructionOffset].initialize(variables);
                    stacksAfter[instructionOffset].copy(stack);
                }
            }
            else
            {
                variablesAfter[instructionOffset].generalize(variables, true);
                stacksAfter[instructionOffset].generalize(stack);
            }
            if (branchUnit.wasCalled())
            {
                branchTargetValues[instructionOffset] = branchTargetValues[instructionOffset] == null ?
                    branchTargets :
                    branchTargetValues[instructionOffset].generalize(branchTargets).instructionOffsetValue();
                if (branchTargetCount == 0)
                {
                    break;
                }
                InstructionOffsetValue instructionOffsetValue = new InstructionOffsetValue(instructionOffset);
                for (int index = 0; index < branchTargetCount; index++)
                {
                    int branchTarget = branchTargets.instructionOffset(index);
                    branchOriginValues[branchTarget] = branchOriginValues[branchTarget] == null ?
                        instructionOffsetValue:
                        branchOriginValues[branchTarget].generalize(instructionOffsetValue).instructionOffsetValue();
                }
                if (branchTargetCount > 1)
                {
                    for (int index = 0; index < branchTargetCount; index++)
                    {
                        if (DEBUG) System.out.println("Pushing alternative branch #"+index+" out of "+branchTargetCount+", from ["+instructionOffset+"] to ["+branchTargets.instructionOffset(index)+"]");
                        pushInstructionBlock(new TracedVariables(variables),
                                             new TracedStack(stack),
                                             branchTargets.instructionOffset(index));
                    }
                    break;
                }
                if (DEBUG) System.out.println("Definite branch from ["+instructionOffset+"] to ["+branchTargets.instructionOffset(0)+"]");
            }
            instructionOffset = branchTargets.instructionOffset(0);
            if (instruction.opcode == InstructionConstants.OP_JSR ||
                instruction.opcode == InstructionConstants.OP_JSR_W)
            {
                evaluateSubroutine(clazz,
                                   method,
                                   codeAttribute,
                                   variables,
                                   stack,
                                   instructionOffset,
                                   instructionBlockStack);
                break;
            }
            else if (instruction.opcode == InstructionConstants.OP_RET)
            {
                pushCallingInstructionBlock(new TracedVariables(variables),
                                            new TracedStack(stack),
                                            instructionOffset);
                break;
            }
        }
        if (DEBUG) System.out.println("Ending processing of instruction block starting at ["+startOffset+"]");
    }
    private void evaluateSubroutine(Clazz           clazz,
                                    Method          method,
                                    CodeAttribute   codeAttribute,
                                    TracedVariables variables,
                                    TracedStack     stack,
                                    int             subroutineStart,
                                    java.util.Stack instructionBlockStack)
    {
        int subroutineEnd = branchTargetFinder.subroutineEnd(subroutineStart);
        if (DEBUG) System.out.println("Evaluating subroutine from "+subroutineStart+" to "+subroutineEnd);
        PartialEvaluator subroutinePartialEvaluator = this;
        if (evaluationCounts[subroutineStart] > 0)
        {
            if (DEBUG) System.out.println("Creating new partial evaluator for subroutine");
            subroutinePartialEvaluator = new PartialEvaluator(this);
            subroutinePartialEvaluator.initializeVariables(clazz,
                                                           method,
                                                           codeAttribute,
                                                           variables,
                                                           stack);
        }
        subroutinePartialEvaluator.evaluateInstructionBlockAndExceptionHandlers(clazz,
                                                                                method,
                                                                                codeAttribute,
                                                                                variables,
                                                                                stack,
                                                                                subroutineStart,
                                                                                subroutineEnd);
        if (subroutinePartialEvaluator != this)
        {
            generalize(subroutinePartialEvaluator, 0, codeAttribute.u4codeLength);
        }
        if (DEBUG) System.out.println("Ending subroutine from "+subroutineStart+" to "+subroutineEnd);
    }
    private void generalize(PartialEvaluator other,
                            int              codeStart,
                            int              codeEnd)
    {
        if (DEBUG) System.out.println("Generalizing with temporary partial evaluation");
        for (int offset = codeStart; offset < codeEnd; offset++)
        {
            if (other.branchOriginValues[offset] != null)
            {
                branchOriginValues[offset] = branchOriginValues[offset] == null ?
                    other.branchOriginValues[offset] :
                    branchOriginValues[offset].generalize(other.branchOriginValues[offset]).instructionOffsetValue();
            }
            if (other.isTraced(offset))
            {
                if (other.branchTargetValues[offset] != null)
                {
                    branchTargetValues[offset] = branchTargetValues[offset] == null ?
                        other.branchTargetValues[offset] :
                        branchTargetValues[offset].generalize(other.branchTargetValues[offset]).instructionOffsetValue();
                }
                if (evaluationCounts[offset] == 0)
                {
                    variablesBefore[offset]      = other.variablesBefore[offset];
                    stacksBefore[offset]         = other.stacksBefore[offset];
                    variablesAfter[offset]       = other.variablesAfter[offset];
                    stacksAfter[offset]          = other.stacksAfter[offset];
                    generalizedContexts[offset]  = other.generalizedContexts[offset];
                    evaluationCounts[offset]     = other.evaluationCounts[offset];
                    initializedVariables[offset] = other.initializedVariables[offset];
                }
                else
                {
                    variablesBefore[offset].generalize(other.variablesBefore[offset], false);
                    stacksBefore[offset]   .generalize(other.stacksBefore[offset]);
                    variablesAfter[offset] .generalize(other.variablesAfter[offset], false);
                    stacksAfter[offset]    .generalize(other.stacksAfter[offset]);
                    evaluationCounts[offset] += other.evaluationCounts[offset];
                }
            }
        }
    }
    private void evaluateExceptionHandlers(Clazz         clazz,
                                           Method        method,
                                           CodeAttribute codeAttribute,
                                           int           startOffset,
                                           int           endOffset)
    {
        if (DEBUG) System.out.println("Evaluating exceptions covering ["+startOffset+" -> "+endOffset+"]:");
        ExceptionHandlerFilter exceptionEvaluator =
            new ExceptionHandlerFilter(startOffset,
                                       endOffset,
                                       this);
        do
        {
            evaluateExceptions = false;
            codeAttribute.exceptionsAccept(clazz,
                                           method,
                                           startOffset,
                                           endOffset,
                                           exceptionEvaluator);
        }
        while (evaluateExceptions);
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        int startPC = exceptionInfo.u2startPC;
        int endPC   = exceptionInfo.u2endPC;
        if (isTraced(startPC, endPC))
        {
            int handlerPC = exceptionInfo.u2handlerPC;
            int catchType = exceptionInfo.u2catchType;
            if (DEBUG) System.out.println("Evaluating exception ["+startPC +" -> "+endPC +": "+handlerPC+"]:");
            TracedVariables variables = new TracedVariables(codeAttribute.u2maxLocals);
            TracedStack     stack     = new TracedStack(codeAttribute.u2maxStack);
            Value storeValue = new InstructionOffsetValue(AT_CATCH_ENTRY);
            variables.setProducerValue(storeValue);
            stack.setProducerValue(storeValue);
            generalizeVariables(startPC,
                                endPC,
                                evaluateAllCode,
                                variables);
            String catchClassName = catchType != 0 ?
                 clazz.getClassName(catchType) :
                 ClassConstants.INTERNAL_NAME_JAVA_LANG_THROWABLE;
            Clazz catchClass = catchType != 0 ?
                ((ClassConstant)((ProgramClass)clazz).getConstant(catchType)).referencedClass :
                null;
            stack.push(valueFactory.createReferenceValue(catchClassName,
                                                         catchClass,
                                                         false));
            int evaluationCount = evaluationCounts[handlerPC];
            evaluateInstructionBlock(clazz,
                                     method,
                                     codeAttribute,
                                     variables,
                                     stack,
                                     handlerPC);
            if (!evaluateExceptions)
            {
                evaluateExceptions = evaluationCount < evaluationCounts[handlerPC];
            }
        }
        else
        {
            if (DEBUG) System.out.println("No information for partial evaluation of exception ["+startPC +" -> "+endPC +": "+exceptionInfo.u2handlerPC+"]");
        }
    }
    private void initializeVariables(Clazz           clazz,
                                     Method          method,
                                     CodeAttribute   codeAttribute,
                                     TracedVariables variables,
                                     TracedStack     stack)
    {
        int codeLength = codeAttribute.u4codeLength;
        if (variablesAfter.length < codeLength)
        {
            branchOriginValues   = new InstructionOffsetValue[codeLength];
            branchTargetValues   = new InstructionOffsetValue[codeLength];
            variablesBefore      = new TracedVariables[codeLength];
            stacksBefore         = new TracedStack[codeLength];
            variablesAfter       = new TracedVariables[codeLength];
            stacksAfter          = new TracedStack[codeLength];
            generalizedContexts  = new boolean[codeLength];
            evaluationCounts     = new int[codeLength];
            initializedVariables = new int[codeLength];
            for (int index = 0; index < codeLength; index++)
            {
                initializedVariables[index] = NONE;
            }
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                branchOriginValues[index]   = null;
                branchTargetValues[index]   = null;
                generalizedContexts[index]  = false;
                evaluationCounts[index]     = 0;
                initializedVariables[index] = NONE;
                if (variablesBefore[index] != null)
                {
                    variablesBefore[index].reset(codeAttribute.u2maxLocals);
                }
                if (stacksBefore[index] != null)
                {
                    stacksBefore[index].reset(codeAttribute.u2maxStack);
                }
                if (variablesAfter[index] != null)
                {
                    variablesAfter[index].reset(codeAttribute.u2maxLocals);
                }
                if (stacksAfter[index] != null)
                {
                    stacksAfter[index].reset(codeAttribute.u2maxStack);
                }
            }
        }
        TracedVariables parameters = new TracedVariables(codeAttribute.u2maxLocals);
        Value storeValue = new InstructionOffsetValue(AT_METHOD_ENTRY);
        parameters.setProducerValue(storeValue);
        invocationUnit.enterMethod(clazz, method, parameters);
        if (DEBUG)
        {
            System.out.println("  Params: "+parameters);
        }
        variables.initialize(parameters);
        InstructionOffsetValue atMethodEntry = new InstructionOffsetValue(AT_METHOD_ENTRY);
        for (int index = 0; index < parameters.size(); index++)
        {
            variables.setProducerValue(index, atMethodEntry);
        }
    }
    private void generalizeVariables(int             startOffset,
                                     int             endOffset,
                                     boolean         includeAfterLastInstruction,
                                     TracedVariables generalizedVariables)
    {
        boolean first     = true;
        int     lastIndex = -1;
        for (int index = startOffset; index < endOffset; index++)
        {
            if (isTraced(index))
            {
                TracedVariables tracedVariables = variablesBefore[index];
                if (first)
                {
                    generalizedVariables.initialize(tracedVariables);
                    first = false;
                }
                else
                {
                    generalizedVariables.generalize(tracedVariables, false);
                }
                lastIndex = index;
            }
        }
        if (includeAfterLastInstruction &&
            lastIndex >= 0)
        {
            TracedVariables tracedVariables = variablesAfter[lastIndex];
            if (first)
            {
                generalizedVariables.initialize(tracedVariables);
            }
            else
            {
                generalizedVariables.generalize(tracedVariables, false);
            }
        }
        if (first)
        {
            generalizedVariables.reset(generalizedVariables.size());
        }
    }
    private static class MyInstructionBlock
    {
        private TracedVariables variables;
        private TracedStack     stack;
        private int             startOffset;
        private MyInstructionBlock(TracedVariables variables,
                                   TracedStack     stack,
                                   int             startOffset)
        {
            this.variables   = variables;
            this.stack       = stack;
            this.startOffset = startOffset;
        }
    }
}
