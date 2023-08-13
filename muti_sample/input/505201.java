import proguard.classfile.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.visitor.AllConstantVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.visitor.*;
import proguard.classfile.util.MethodLinker;
import proguard.classfile.visitor.*;
import proguard.evaluation.*;
import proguard.evaluation.value.*;
import proguard.optimize.evaluation.*;
import proguard.optimize.info.*;
import proguard.optimize.peephole.*;
import proguard.util.*;
import java.io.IOException;
import java.util.*;
public class Optimizer
{
    private static final String CLASS_MARKING_FINAL            = "class/marking/final";
    private static final String CLASS_MERGING_VERTICAL         = "class/merging/vertical";
    private static final String CLASS_MERGING_HORIZONTAL       = "class/merging/horizontal";
    private static final String FIELD_REMOVAL_WRITEONLY        = "field/removal/writeonly";
    private static final String FIELD_MARKING_PRIVATE          = "field/marking/private";
    private static final String FIELD_PROPAGATION_VALUE        = "field/propagation/value";
    private static final String METHOD_MARKING_PRIVATE         = "method/marking/private";
    private static final String METHOD_MARKING_STATIC          = "method/marking/static";
    private static final String METHOD_MARKING_FINAL           = "method/marking/final";
    private static final String METHOD_REMOVAL_PARAMETER       = "method/removal/parameter";
    private static final String METHOD_PROPAGATION_PARAMETER   = "method/propagation/parameter";
    private static final String METHOD_PROPAGATION_RETURNVALUE = "method/propagation/returnvalue";
    private static final String METHOD_INLINING_SHORT          = "method/inlining/short";
    private static final String METHOD_INLINING_UNIQUE         = "method/inlining/unique";
    private static final String METHOD_INLINING_TAILRECURSION  = "method/inlining/tailrecursion";
    private static final String CODE_MERGING                   = "code/merging";
    private static final String CODE_SIMPLIFICATION_VARIABLE   = "code/simplification/variable";
    private static final String CODE_SIMPLIFICATION_ARITHMETIC = "code/simplification/arithmetic";
    private static final String CODE_SIMPLIFICATION_CAST       = "code/simplification/cast";
    private static final String CODE_SIMPLIFICATION_FIELD      = "code/simplification/field";
    private static final String CODE_SIMPLIFICATION_BRANCH     = "code/simplification/branch";
    private static final String CODE_SIMPLIFICATION_ADVANCED   = "code/simplification/advanced";
    private static final String CODE_REMOVAL_ADVANCED          = "code/removal/advanced";
    private static final String CODE_REMOVAL_SIMPLE            = "code/removal/simple";
    private static final String CODE_REMOVAL_VARIABLE          = "code/removal/variable";
    private static final String CODE_REMOVAL_EXCEPTION         = "code/removal/exception";
    private static final String CODE_ALLOCATION_VARIABLE       = "code/allocation/variable";
    public static final String[] OPTIMIZATION_NAMES = new String[]
    {
        CLASS_MARKING_FINAL,
        CLASS_MERGING_VERTICAL,
        CLASS_MERGING_HORIZONTAL,
        FIELD_REMOVAL_WRITEONLY,
        FIELD_PROPAGATION_VALUE,
        METHOD_MARKING_PRIVATE,
        METHOD_MARKING_STATIC,
        METHOD_MARKING_FINAL,
        METHOD_REMOVAL_PARAMETER,
        METHOD_PROPAGATION_PARAMETER,
        METHOD_PROPAGATION_RETURNVALUE,
        METHOD_INLINING_SHORT,
        METHOD_INLINING_UNIQUE,
        METHOD_INLINING_TAILRECURSION,
        CODE_MERGING,
        CODE_SIMPLIFICATION_VARIABLE,
        CODE_SIMPLIFICATION_ARITHMETIC,
        CODE_SIMPLIFICATION_CAST,
        CODE_SIMPLIFICATION_FIELD,
        CODE_SIMPLIFICATION_BRANCH,
        CODE_SIMPLIFICATION_ADVANCED,
        CODE_REMOVAL_ADVANCED,
        CODE_REMOVAL_SIMPLE,
        CODE_REMOVAL_VARIABLE,
        CODE_REMOVAL_EXCEPTION,
        CODE_ALLOCATION_VARIABLE,
    };
    private final Configuration configuration;
    public Optimizer(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public boolean execute(ClassPool programClassPool,
                           ClassPool libraryClassPool) throws IOException
    {
        if (configuration.keep         == null &&
            configuration.applyMapping == null &&
            configuration.printMapping == null)
        {
            throw new IOException("You have to specify '-keep' options for the optimization step.");
        }
        StringMatcher filter = configuration.optimizations != null ?
            new ListParser(new NameParser()).parse(configuration.optimizations) :
            new ConstantMatcher(true);
        boolean classMarkingFinal            = filter.matches(CLASS_MARKING_FINAL);
        boolean classMergingVertical         = filter.matches(CLASS_MERGING_VERTICAL);
        boolean classMergingHorizontal       = filter.matches(CLASS_MERGING_HORIZONTAL);
        boolean fieldRemovalWriteonly        = filter.matches(FIELD_REMOVAL_WRITEONLY);
        boolean fieldMarkingPrivate          = filter.matches(FIELD_MARKING_PRIVATE);
        boolean fieldPropagationValue        = filter.matches(FIELD_PROPAGATION_VALUE);
        boolean methodMarkingPrivate         = filter.matches(METHOD_MARKING_PRIVATE);
        boolean methodMarkingStatic          = filter.matches(METHOD_MARKING_STATIC);
        boolean methodMarkingFinal           = filter.matches(METHOD_MARKING_FINAL);
        boolean methodRemovalParameter       = filter.matches(METHOD_REMOVAL_PARAMETER);
        boolean methodPropagationParameter   = filter.matches(METHOD_PROPAGATION_PARAMETER);
        boolean methodPropagationReturnvalue = filter.matches(METHOD_PROPAGATION_RETURNVALUE);
        boolean methodInliningShort          = filter.matches(METHOD_INLINING_SHORT);
        boolean methodInliningUnique         = filter.matches(METHOD_INLINING_UNIQUE);
        boolean methodInliningTailrecursion  = filter.matches(METHOD_INLINING_TAILRECURSION);
        boolean codeMerging                  = filter.matches(CODE_MERGING);
        boolean codeSimplificationVariable   = filter.matches(CODE_SIMPLIFICATION_VARIABLE);
        boolean codeSimplificationArithmetic = filter.matches(CODE_SIMPLIFICATION_ARITHMETIC);
        boolean codeSimplificationCast       = filter.matches(CODE_SIMPLIFICATION_CAST);
        boolean codeSimplificationField      = filter.matches(CODE_SIMPLIFICATION_FIELD);
        boolean codeSimplificationBranch     = filter.matches(CODE_SIMPLIFICATION_BRANCH);
        boolean codeSimplificationAdvanced   = filter.matches(CODE_SIMPLIFICATION_ADVANCED);
        boolean codeRemovalAdvanced          = filter.matches(CODE_REMOVAL_ADVANCED);
        boolean codeRemovalSimple            = filter.matches(CODE_REMOVAL_SIMPLE);
        boolean codeRemovalVariable          = filter.matches(CODE_REMOVAL_VARIABLE);
        boolean codeRemovalException         = filter.matches(CODE_REMOVAL_EXCEPTION);
        boolean codeAllocationVariable       = filter.matches(CODE_ALLOCATION_VARIABLE);
        ClassCounter       classMarkingFinalCounter            = new ClassCounter();
        ClassCounter       classMergingVerticalCounter         = new ClassCounter();
        ClassCounter       classMergingHorizontalCounter       = new ClassCounter();
        MemberCounter      fieldRemovalWriteonlyCounter        = new MemberCounter();
        MemberCounter      fieldMarkingPrivateCounter          = new MemberCounter();
        MemberCounter      fieldPropagationValueCounter        = new MemberCounter();
        MemberCounter      methodMarkingPrivateCounter         = new MemberCounter();
        MemberCounter      methodMarkingStaticCounter          = new MemberCounter();
        MemberCounter      methodMarkingFinalCounter           = new MemberCounter();
        MemberCounter      methodRemovalParameterCounter       = new MemberCounter();
        MemberCounter      methodPropagationParameterCounter   = new MemberCounter();
        MemberCounter      methodPropagationReturnvalueCounter = new MemberCounter();
        InstructionCounter methodInliningShortCounter          = new InstructionCounter();
        InstructionCounter methodInliningUniqueCounter         = new InstructionCounter();
        InstructionCounter methodInliningTailrecursionCounter  = new InstructionCounter();
        InstructionCounter codeMergingCounter                  = new InstructionCounter();
        InstructionCounter codeSimplificationVariableCounter   = new InstructionCounter();
        InstructionCounter codeSimplificationArithmeticCounter = new InstructionCounter();
        InstructionCounter codeSimplificationCastCounter       = new InstructionCounter();
        InstructionCounter codeSimplificationFieldCounter      = new InstructionCounter();
        InstructionCounter codeSimplificationBranchCounter     = new InstructionCounter();
        InstructionCounter codeSimplificationAdvancedCounter   = new InstructionCounter();
        InstructionCounter deletedCounter                      = new InstructionCounter();
        InstructionCounter addedCounter                        = new InstructionCounter();
        MemberCounter      codeRemovalVariableCounter          = new MemberCounter();
        ExceptionCounter   codeRemovalExceptionCounter         = new ExceptionCounter();
        MemberCounter      codeAllocationVariableCounter       = new MemberCounter();
        MemberCounter      initializerFixCounter               = new MemberCounter();
        codeSimplificationAdvanced =
            codeSimplificationAdvanced ||
            fieldPropagationValue      ||
            methodPropagationParameter ||
            methodPropagationReturnvalue;
        codeRemovalAdvanced =
            codeRemovalAdvanced   ||
            fieldRemovalWriteonly ||
            methodMarkingStatic   ||
            methodRemovalParameter;
        codeRemovalSimple =
            codeRemovalSimple ||
            codeSimplificationBranch;
        codeRemovalException =
            codeRemovalException ||
            codeRemovalAdvanced ||
            codeRemovalSimple;
        programClassPool.classesAccept(new ClassCleaner());
        libraryClassPool.classesAccept(new ClassCleaner());
        programClassPool.classesAccept(new BottomClassFilter(
                                       new MethodLinker()));
        libraryClassPool.classesAccept(new BottomClassFilter(
                                       new MethodLinker()));
        KeepMarker keepMarker = new KeepMarker();
        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                    keepMarker,
                                                                    keepMarker,
                                                                    false,
                                                                    true,
                                                                    false);
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);
        libraryClassPool.classesAccept(keepMarker);
        libraryClassPool.classesAccept(new AllMemberVisitor(keepMarker));
        programClassPool.classesAccept(
            new AllMethodVisitor(
            new AllAttributeVisitor(
            new AllInstructionVisitor(
            new DotClassClassVisitor(keepMarker)))));
        programClassPool.classesAccept(
            new AllConstantVisitor(
            new ClassForNameClassVisitor(keepMarker)));
        programClassPool.classesAccept(new ClassOptimizationInfoSetter());
        programClassPool.classesAccept(new AllMemberVisitor(
                                       new MemberOptimizationInfoSetter()));
        if (configuration.assumeNoSideEffects != null)
        {
            NoSideEffectMethodMarker noSideEffectMethodMarker = new NoSideEffectMethodMarker();
            ClassPoolVisitor noClassPoolvisitor =
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.assumeNoSideEffects,
                                                                        null,
                                                                        noSideEffectMethodMarker);
            programClassPool.accept(noClassPoolvisitor);
            libraryClassPool.accept(noClassPoolvisitor);
        }
        if (classMarkingFinal)
        {
            programClassPool.classesAccept(
                new ClassFinalizer(classMarkingFinalCounter));
        }
        if (methodMarkingFinal)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new MethodFinalizer(methodMarkingFinalCounter)));
        }
        if (fieldRemovalWriteonly)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new AllInstructionVisitor(
                new ReadWriteFieldMarker()))));
            programClassPool.classesAccept(
                new AllFieldVisitor(
                new WriteOnlyFieldFilter(fieldRemovalWriteonlyCounter)));
        }
        else
        {
            programClassPool.classesAccept(
                new AllFieldVisitor(
                new ReadWriteFieldMarker()));
        }
        programClassPool.classesAccept(
            new AllMethodVisitor(
            new OptimizationInfoMemberFilter(
            new ParameterUsageMarker(!methodMarkingStatic,
                                     !methodRemovalParameter))));
        programClassPool.accept(new SideEffectMethodMarker());
        ValueFactory valueFactory = new IdentifiedValueFactory();
        if (fieldPropagationValue      ||
            methodPropagationParameter ||
            methodPropagationReturnvalue)
        {
            InvocationUnit storingInvocationUnit =
                new StoringInvocationUnit(valueFactory,
                                          fieldPropagationValue,
                                          methodPropagationParameter,
                                          methodPropagationReturnvalue);
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new PartialEvaluator(valueFactory, storingInvocationUnit, false))));
            programClassPool.classesAccept(
                new MultiClassVisitor(
                new ClassVisitor[]
                {
                    new AllFieldVisitor(
                    new ConstantMemberFilter(fieldPropagationValueCounter)),
                    new AllMethodVisitor(
                    new ConstantParameterFilter(methodPropagationParameterCounter)),
                    new AllMethodVisitor(
                    new ConstantMemberFilter(methodPropagationReturnvalueCounter)),
                }));
        }
        InvocationUnit loadingInvocationUnit =
            new LoadingInvocationUnit(valueFactory,
                                      fieldPropagationValue,
                                      methodPropagationParameter,
                                      methodPropagationReturnvalue);
        if (codeSimplificationAdvanced)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new EvaluationSimplifier(
                new PartialEvaluator(valueFactory, loadingInvocationUnit, false),
                codeSimplificationAdvancedCounter))));
        }
        if (codeRemovalAdvanced)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new EvaluationShrinker(
                new PartialEvaluator(valueFactory, loadingInvocationUnit, !codeSimplificationAdvanced),
                deletedCounter, addedCounter))));
        }
        if (methodRemovalParameter)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new OptimizationInfoMemberFilter(
                new MethodDescriptorShrinker())));
        }
        if (methodMarkingStatic)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new OptimizationInfoMemberFilter(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_STATIC,
                new MethodStaticizer(methodMarkingStaticCounter)))));
        }
        if (methodRemovalParameter)
        {
            programClassPool.classesAccept(
                new MemberReferenceFixer());
        }
        if (methodRemovalParameter ||
            methodMarkingPrivate   ||
            methodMarkingStatic)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new ParameterShrinker(methodRemovalParameterCounter))));
        }
        else if (codeRemovalAdvanced)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new StackSizeUpdater())));
        }
        programClassPool.classesAccept(
            new MultiClassVisitor(
            new ClassVisitor[]
            {
                new AllConstantVisitor(
                new PackageVisibleMemberInvokingClassMarker()),
                new AllMethodVisitor(
                new MultiMemberVisitor(
                new MemberVisitor[]
                {
                    new PackageVisibleMemberContainingClassMarker(),
                    new AllAttributeVisitor(
                    new MultiAttributeVisitor(
                    new AttributeVisitor[]
                    {
                        new CatchExceptionMarker(),
                        new AllInstructionVisitor(
                        new MultiInstructionVisitor(
                        new InstructionVisitor[]
                        {
                            new InstantiationClassMarker(),
                            new InstanceofClassMarker(),
                            new DotClassMarker(),
                            new MethodInvocationMarker(),
                            new SuperInvocationMarker(),
                            new BackwardBranchMarker(),
                            new AccessMethodMarker(),
                        })),
                        new AllExceptionInfoVisitor(
                        new ExceptionHandlerConstantVisitor(
                        new ReferencedClassVisitor(
                        new CaughtClassMarker()))),
                    })),
                })),
            }));
        if (classMergingVertical)
        {
            programClassPool.classesAccept(
                new VerticalClassMerger(configuration.allowAccessModification,
                                        configuration.mergeInterfacesAggressively,
                                        classMergingVerticalCounter));
        }
        if (classMergingHorizontal)
        {
            programClassPool.classesAccept(
                new HorizontalClassMerger(configuration.allowAccessModification,
                                          configuration.mergeInterfacesAggressively,
                                          classMergingHorizontalCounter));
        }
        if (classMergingVertical ||
            classMergingHorizontal)
        {
            programClassPool.classesAccept(new RetargetedInnerClassAttributeRemover());
            programClassPool.classesAccept(new TargetClassChanger());
            programClassPool.classesAccept(new ClassReferenceFixer(true));
            programClassPool.classesAccept(new MemberReferenceFixer());
            if (configuration.allowAccessModification)
            {
                programClassPool.classesAccept(
                    new AllConstantVisitor(
                    new AccessFixer()));
            }
        }
        if (methodRemovalParameter ||
            classMergingVertical   ||
            classMergingHorizontal)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new DuplicateInitializerFixer(initializerFixCounter)));
            if (initializerFixCounter.getCount() > 0)
            {
                programClassPool.classesAccept(
                    new AllMethodVisitor(
                    new AllAttributeVisitor(
                    new DuplicateInitializerInvocationFixer(addedCounter))));
                programClassPool.classesAccept(new MemberReferenceFixer());
            }
        }
        if (methodInliningUnique)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new MethodInliner(configuration.microEdition,
                                  configuration.allowAccessModification,
                                  true,
                                  methodInliningUniqueCounter))));
        }
        if (methodInliningShort)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new MethodInliner(configuration.microEdition,
                                  configuration.allowAccessModification,
                                  false,
                                  methodInliningShortCounter))));
        }
        if (methodInliningTailrecursion)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new TailRecursionSimplifier(methodInliningTailrecursionCounter))));
        }
        if (fieldMarkingPrivate ||
            methodMarkingPrivate)
        {
            programClassPool.classesAccept(
                new NonPrivateMemberMarker());
        }
        if (fieldMarkingPrivate ||
            methodMarkingPrivate)
        {
            programClassPool.classesAccept(
                new AllFieldVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberPrivatizer(fieldMarkingPrivateCounter))));
        }
        if (methodMarkingPrivate)
        {
            programClassPool.classesAccept(
                new ClassAccessFilter(0, ClassConstants.INTERNAL_ACC_INTERFACE,
                new AllMethodVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberPrivatizer(methodMarkingPrivateCounter)))));
        }
        if ((methodInliningUnique ||
             methodInliningShort  ||
             methodInliningTailrecursion) &&
            configuration.allowAccessModification)
        {
            programClassPool.classesAccept(
                new AllConstantVisitor(
                new AccessFixer()));
        }
        if (methodRemovalParameter ||
            classMergingVertical   ||
            classMergingHorizontal ||
            methodMarkingPrivate)
        {
            programClassPool.classesAccept(
                new AllMemberVisitor(
                new AllAttributeVisitor(
                new MethodInvocationFixer())));
        }
        if (codeMerging)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new GotoCommonCodeReplacer(codeMergingCounter))));
        }
        BranchTargetFinder  branchTargetFinder  = new BranchTargetFinder();
        CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();
        List peepholeOptimizations = new ArrayList();
        if (codeSimplificationVariable)
        {
            peepholeOptimizations.add(
                new InstructionSequencesReplacer(InstructionSequenceConstants.CONSTANTS,
                                                 InstructionSequenceConstants.VARIABLE,
                                                 branchTargetFinder, codeAttributeEditor, codeSimplificationVariableCounter));
        }
        if (codeSimplificationArithmetic)
        {
            peepholeOptimizations.add(
                new InstructionSequencesReplacer(InstructionSequenceConstants.CONSTANTS,
                                                 InstructionSequenceConstants.ARITHMETIC,
                                                 branchTargetFinder, codeAttributeEditor, codeSimplificationArithmeticCounter));
        }
        if (codeSimplificationCast)
        {
            peepholeOptimizations.add(
                new InstructionSequencesReplacer(InstructionSequenceConstants.CONSTANTS,
                                                 InstructionSequenceConstants.CAST,
                                                 branchTargetFinder, codeAttributeEditor, codeSimplificationCastCounter));
        }
        if (codeSimplificationField)
        {
            peepholeOptimizations.add(
                new InstructionSequencesReplacer(InstructionSequenceConstants.CONSTANTS,
                                                 InstructionSequenceConstants.FIELD,
                                                 branchTargetFinder, codeAttributeEditor, codeSimplificationFieldCounter));
        }
        if (codeSimplificationBranch)
        {
            peepholeOptimizations.add(
                new InstructionSequencesReplacer(InstructionSequenceConstants.CONSTANTS,
                                                 InstructionSequenceConstants.BRANCH,
                                                 branchTargetFinder, codeAttributeEditor, codeSimplificationBranchCounter));
            peepholeOptimizations.add(
                new GotoGotoReplacer(codeAttributeEditor, codeSimplificationBranchCounter));
            peepholeOptimizations.add(
                new GotoReturnReplacer(codeAttributeEditor, codeSimplificationBranchCounter));
        }
        if (!peepholeOptimizations.isEmpty())
        {
            InstructionVisitor[] peepholeOptimizationsArray =
                new InstructionVisitor[peepholeOptimizations.size()];
            peepholeOptimizations.toArray(peepholeOptimizationsArray);
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new PeepholeOptimizer(branchTargetFinder, codeAttributeEditor,
                new MultiInstructionVisitor(
                peepholeOptimizationsArray)))));
        }
        if (codeRemovalException)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new UnreachableExceptionRemover(codeRemovalExceptionCounter))));
        }
        if (codeRemovalSimple)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new UnreachableCodeRemover(deletedCounter))));
        }
        if (codeRemovalVariable)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new VariableShrinker(codeRemovalVariableCounter))));
        }
        else
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new VariableCleaner())));
        }
        if (codeAllocationVariable)
        {
            programClassPool.classesAccept(
                new AllMethodVisitor(
                new AllAttributeVisitor(
                new VariableOptimizer(false, codeAllocationVariableCounter))));
        }
        int classMarkingFinalCount            = classMarkingFinalCounter           .getCount();
        int classMergingVerticalCount         = classMergingVerticalCounter        .getCount();
        int classMergingHorizontalCount       = classMergingHorizontalCounter      .getCount();
        int fieldRemovalWriteonlyCount        = fieldRemovalWriteonlyCounter       .getCount();
        int fieldMarkingPrivateCount          = fieldMarkingPrivateCounter         .getCount();
        int fieldPropagationValueCount        = fieldPropagationValueCounter       .getCount();
        int methodMarkingPrivateCount         = methodMarkingPrivateCounter        .getCount();
        int methodMarkingStaticCount          = methodMarkingStaticCounter         .getCount();
        int methodMarkingFinalCount           = methodMarkingFinalCounter          .getCount();
        int methodRemovalParameterCount       = methodRemovalParameterCounter      .getCount() - methodMarkingStaticCounter.getCount() - initializerFixCounter.getCount();
        int methodPropagationParameterCount   = methodPropagationParameterCounter  .getCount();
        int methodPropagationReturnvalueCount = methodPropagationReturnvalueCounter.getCount();
        int methodInliningShortCount          = methodInliningShortCounter         .getCount();
        int methodInliningUniqueCount         = methodInliningUniqueCounter        .getCount();
        int methodInliningTailrecursionCount  = methodInliningTailrecursionCounter .getCount();
        int codeMergingCount                  = codeMergingCounter                 .getCount();
        int codeSimplificationVariableCount   = codeSimplificationVariableCounter  .getCount();
        int codeSimplificationArithmeticCount = codeSimplificationArithmeticCounter.getCount();
        int codeSimplificationCastCount       = codeSimplificationCastCounter      .getCount();
        int codeSimplificationFieldCount      = codeSimplificationFieldCounter     .getCount();
        int codeSimplificationBranchCount     = codeSimplificationBranchCounter    .getCount();
        int codeSimplificationAdvancedCount   = codeSimplificationAdvancedCounter  .getCount();
        int codeRemovalCount                  = deletedCounter                     .getCount() - addedCounter.getCount();
        int codeRemovalVariableCount          = codeRemovalVariableCounter         .getCount();
        int codeRemovalExceptionCount         = codeRemovalExceptionCounter        .getCount();
        int codeAllocationVariableCount       = codeAllocationVariableCounter      .getCount();
        if (configuration.verbose)
        {
            System.out.println("  Number of finalized classes:                 " + classMarkingFinalCount            + disabled(classMarkingFinal));
            System.out.println("  Number of vertically merged classes:         " + classMergingVerticalCount         + disabled(classMergingVertical));
            System.out.println("  Number of horizontally merged classes:       " + classMergingHorizontalCount       + disabled(classMergingHorizontal));
            System.out.println("  Number of removed write-only fields:         " + fieldRemovalWriteonlyCount        + disabled(fieldRemovalWriteonly));
            System.out.println("  Number of privatized fields:                 " + fieldMarkingPrivateCount          + disabled(fieldMarkingPrivate));
            System.out.println("  Number of inlined constant fields:           " + fieldPropagationValueCount        + disabled(fieldPropagationValue));
            System.out.println("  Number of privatized methods:                " + methodMarkingPrivateCount         + disabled(methodMarkingPrivate));
            System.out.println("  Number of staticized methods:                " + methodMarkingStaticCount          + disabled(methodMarkingStatic));
            System.out.println("  Number of finalized methods:                 " + methodMarkingFinalCount           + disabled(methodMarkingFinal));
            System.out.println("  Number of removed method parameters:         " + methodRemovalParameterCount       + disabled(methodRemovalParameter));
            System.out.println("  Number of inlined constant parameters:       " + methodPropagationParameterCount   + disabled(methodPropagationParameter));
            System.out.println("  Number of inlined constant return values:    " + methodPropagationReturnvalueCount + disabled(methodPropagationReturnvalue));
            System.out.println("  Number of inlined short method calls:        " + methodInliningShortCount          + disabled(methodInliningShort));
            System.out.println("  Number of inlined unique method calls:       " + methodInliningUniqueCount         + disabled(methodInliningUnique));
            System.out.println("  Number of inlined tail recursion calls:      " + methodInliningTailrecursionCount  + disabled(methodInliningTailrecursion));
            System.out.println("  Number of merged code blocks:                " + codeMergingCount                  + disabled(codeMerging));
            System.out.println("  Number of variable peephole optimizations:   " + codeSimplificationVariableCount   + disabled(codeSimplificationVariable));
            System.out.println("  Number of arithmetic peephole optimizations: " + codeSimplificationArithmeticCount + disabled(codeSimplificationArithmetic));
            System.out.println("  Number of cast peephole optimizations:       " + codeSimplificationCastCount       + disabled(codeSimplificationCast));
            System.out.println("  Number of field peephole optimizations:      " + codeSimplificationFieldCount      + disabled(codeSimplificationField));
            System.out.println("  Number of branch peephole optimizations:     " + codeSimplificationBranchCount     + disabled(codeSimplificationBranch));
            System.out.println("  Number of simplified instructions:           " + codeSimplificationAdvancedCount   + disabled(codeSimplificationAdvanced));
            System.out.println("  Number of removed instructions:              " + codeRemovalCount                  + disabled(codeRemovalAdvanced));
            System.out.println("  Number of removed local variables:           " + codeRemovalVariableCount          + disabled(codeRemovalVariable));
            System.out.println("  Number of removed exception blocks:          " + codeRemovalExceptionCount         + disabled(codeRemovalException));
            System.out.println("  Number of optimized local variable frames:   " + codeAllocationVariableCount       + disabled(codeAllocationVariable));
        }
        return classMarkingFinalCount            > 0 ||
               classMergingVerticalCount         > 0 ||
               classMergingHorizontalCount       > 0 ||
               fieldRemovalWriteonlyCount        > 0 ||
               fieldMarkingPrivateCount          > 0 ||
               methodMarkingPrivateCount         > 0 ||
               methodMarkingStaticCount          > 0 ||
               methodMarkingFinalCount           > 0 ||
               fieldPropagationValueCount        > 0 ||
               methodRemovalParameterCount       > 0 ||
               methodPropagationParameterCount   > 0 ||
               methodPropagationReturnvalueCount > 0 ||
               methodInliningShortCount          > 0 ||
               methodInliningUniqueCount         > 0 ||
               methodInliningTailrecursionCount  > 0 ||
               codeMergingCount                  > 0 ||
               codeSimplificationVariableCount   > 0 ||
               codeSimplificationArithmeticCount > 0 ||
               codeSimplificationCastCount       > 0 ||
               codeSimplificationFieldCount      > 0 ||
               codeSimplificationBranchCount     > 0 ||
               codeSimplificationAdvancedCount   > 0 ||
               codeRemovalCount                  > 0 ||
               codeRemovalVariableCount          > 0 ||
               codeRemovalExceptionCount         > 0 ||
               codeAllocationVariableCount       > 0;
    }
    private String disabled(boolean flag)
    {
        return flag ? "" : "   (disabled)";
    }
    private String disabled(boolean flag1, boolean flag2)
    {
        return flag1 && flag2 ? "" :
               flag1 || flag2 ? "   (partially disabled)" :
                                "   (disabled)";
    }
}
