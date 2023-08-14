import proguard.classfile.constant.Constant;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.visitor.*;
public class InstructionSequencesReplacer
extends      MultiInstructionVisitor
implements   InstructionVisitor
{
    private static final int PATTERN_INDEX     = 0;
    private static final int REPLACEMENT_INDEX = 1;
    public InstructionSequencesReplacer(Constant[]          patternConstants,
                                        Instruction[][][]   instructionSequences,
                                        BranchTargetFinder  branchTargetFinder,
                                        CodeAttributeEditor codeAttributeEditor)
    {
        this(patternConstants,
             instructionSequences,
             branchTargetFinder,
             codeAttributeEditor,
             null);
    }
    public InstructionSequencesReplacer(Constant[]          patternConstants,
                                        Instruction[][][]   instructionSequences,
                                        BranchTargetFinder  branchTargetFinder,
                                        CodeAttributeEditor codeAttributeEditor,
                                        InstructionVisitor  extraInstructionVisitor)
    {
        super(createInstructionSequenceReplacers(patternConstants,
                                                 instructionSequences,
                                                 branchTargetFinder,
                                                 codeAttributeEditor,
                                                 extraInstructionVisitor));
    }
    private static InstructionVisitor[] createInstructionSequenceReplacers(Constant[]          patternConstants,
                                                                           Instruction[][][]   instructionSequences,
                                                                           BranchTargetFinder  branchTargetFinder,
                                                                           CodeAttributeEditor codeAttributeEditor,
                                                                           InstructionVisitor  extraInstructionVisitor)
    {
        InstructionVisitor[] instructionSequenceReplacers =
            new InstructionSequenceReplacer[instructionSequences.length];
        for (int index = 0; index < instructionSequenceReplacers.length; index++)
        {
            Instruction[][] instructionSequencePair = instructionSequences[index];
            instructionSequenceReplacers[index] =
                new InstructionSequenceReplacer(patternConstants,
                                                instructionSequencePair[PATTERN_INDEX],
                                                instructionSequencePair[REPLACEMENT_INDEX],
                                                branchTargetFinder,
                                                codeAttributeEditor,
                                                extraInstructionVisitor);
        }
        return instructionSequenceReplacers;
    }
}
