import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.*;
public class InstructionSequenceReplacer
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor
{
    private static final boolean DEBUG = false;
    private final InstructionSequenceMatcher instructionSequenceMatcher;
    private final Instruction[]              replacementInstructions;
    private final BranchTargetFinder         branchTargetFinder;
    private final CodeAttributeEditor        codeAttributeEditor;
    private final InstructionVisitor         extraInstructionVisitor;
    private final MyReplacementInstructionFactory replacementInstructionFactory = new MyReplacementInstructionFactory();
    public InstructionSequenceReplacer(Constant[]          patternConstants,
                                       Instruction[]       patternInstructions,
                                       Instruction[]       replacementInstructions,
                                       BranchTargetFinder  branchTargetFinder,
                                       CodeAttributeEditor codeAttributeEditor)
    {
        this(patternConstants,
             patternInstructions,
             replacementInstructions,
             branchTargetFinder,
             codeAttributeEditor,
             null);
    }
    public InstructionSequenceReplacer(Constant[]          patternConstants,
                                       Instruction[]       patternInstructions,
                                       Instruction[]       replacementInstructions,
                                       BranchTargetFinder  branchTargetFinder,
                                       CodeAttributeEditor codeAttributeEditor,
                                       InstructionVisitor  extraInstructionVisitor)
    {
        this.instructionSequenceMatcher = new InstructionSequenceMatcher(patternConstants, patternInstructions);
        this.replacementInstructions    = replacementInstructions;
        this.branchTargetFinder         = branchTargetFinder;
        this.codeAttributeEditor        = codeAttributeEditor;
        this.extraInstructionVisitor    = extraInstructionVisitor;
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        if (branchTargetFinder.isTarget(offset) ||
            codeAttributeEditor.isModified(offset))
        {
            instructionSequenceMatcher.reset();
        }
        instruction.accept(clazz, method, codeAttribute, offset, instructionSequenceMatcher);
        if (instructionSequenceMatcher.isMatching() &&
            matchedInstructionsUnmodified())
        {
            if (DEBUG)
            {
                System.out.println("InstructionSequenceReplacer: ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]");
                System.out.println("  Matched:");
                for (int index = 0; index < instructionSequenceMatcher.instructionCount(); index++)
                {
                    int matchedOffset = instructionSequenceMatcher.matchedInstructionOffset(index);
                    System.out.println("    "+InstructionFactory.create(codeAttribute.code, matchedOffset).toString(matchedOffset));
                }
                System.out.println("  Replacement:");
                for (int index = 0; index < replacementInstructions.length; index++)
                {
                    int matchedOffset = instructionSequenceMatcher.matchedInstructionOffset(index);
                    System.out.println("    "+replacementInstructionFactory.create(index).shrink().toString(matchedOffset));
                }
            }
            for (int index = 0; index < replacementInstructions.length; index++)
            {
                codeAttributeEditor.replaceInstruction(instructionSequenceMatcher.matchedInstructionOffset(index),
                                                       replacementInstructionFactory.create(index).shrink());
            }
            for (int index = replacementInstructions.length; index < instructionSequenceMatcher.instructionCount(); index++)
            {
                codeAttributeEditor.deleteInstruction(instructionSequenceMatcher.matchedInstructionOffset(index));
            }
            if (extraInstructionVisitor != null)
            {
                instruction.accept(clazz,
                                   method,
                                   codeAttribute,
                                   offset,
                                   extraInstructionVisitor);
            }
        }
    }
    private boolean matchedInstructionsUnmodified()
    {
        for (int index = 0; index < instructionSequenceMatcher.instructionCount(); index++)
        {
            if (codeAttributeEditor.isModified(instructionSequenceMatcher.matchedInstructionOffset(index)))
            {
                return false;
            }
        }
        return true;
    }
    private class MyReplacementInstructionFactory
    implements    InstructionVisitor
    {
        private Instruction replacementInstruction;
        public Instruction create(int index)
        {
            replacementInstructions[index].accept(null,
                                                  null,
                                                  null,
                                                  instructionSequenceMatcher.matchedInstructionOffset(index),
                                                  this);
            return replacementInstruction.shrink();
        }
        public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
        {
            replacementInstruction =
                new SimpleInstruction(simpleInstruction.opcode,
                                      instructionSequenceMatcher.matchedArgument(simpleInstruction.constant));
        }
        public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
        {
            replacementInstruction =
                new VariableInstruction(variableInstruction.opcode,
                                        instructionSequenceMatcher.matchedArgument(variableInstruction.variableIndex),
                                        instructionSequenceMatcher.matchedArgument(variableInstruction.constant));
        }
        public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
        {
            replacementInstruction =
                new ConstantInstruction(constantInstruction.opcode,
                                        instructionSequenceMatcher.matchedConstantIndex(constantInstruction.constantIndex),
                                        instructionSequenceMatcher.matchedArgument(constantInstruction.constant));
        }
        public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
        {
            replacementInstruction =
                new BranchInstruction(branchInstruction.opcode,
                                      instructionSequenceMatcher.matchedBranchOffset(offset, branchInstruction.branchOffset));
        }
        public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
        {
            replacementInstruction =
                new TableSwitchInstruction(tableSwitchInstruction.opcode,
                                           instructionSequenceMatcher.matchedBranchOffset(offset, tableSwitchInstruction.defaultOffset),
                                           instructionSequenceMatcher.matchedArgument(tableSwitchInstruction.lowCase),
                                           instructionSequenceMatcher.matchedArgument(tableSwitchInstruction.highCase),
                                           instructionSequenceMatcher.matchedJumpOffsets(offset, tableSwitchInstruction.jumpOffsets));
        }
        public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
        {
            replacementInstruction =
                new LookUpSwitchInstruction(lookUpSwitchInstruction.opcode,
                                            instructionSequenceMatcher.matchedBranchOffset(offset, lookUpSwitchInstruction.defaultOffset),
                                            instructionSequenceMatcher.matchedArguments(lookUpSwitchInstruction.cases),
                                            instructionSequenceMatcher.matchedJumpOffsets(offset, lookUpSwitchInstruction.jumpOffsets));
        }
    }
}
