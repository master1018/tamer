import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.util.InstructionSequenceMatcher;
public class InstructionSequenceConstants
{
    public static final int X = InstructionSequenceMatcher.X;
    public static final int Y = InstructionSequenceMatcher.Y;
    public static final int Z = InstructionSequenceMatcher.Z;
    public static final int A = InstructionSequenceMatcher.A;
    public static final int B = InstructionSequenceMatcher.B;
    public static final int C = InstructionSequenceMatcher.C;
    public static final int D = InstructionSequenceMatcher.D;
    private static final int I_32768              =  0;
    private static final int I_65536              =  1;
    private static final int I_16777216           =  2;
    private static final int I_0x0000ff00         =  3;
    private static final int I_0x00ff0000         =  4;
    private static final int I_0xff000000         =  5;
    private static final int I_0x0000ffff         =  6;
    private static final int I_0xffff0000         =  7;
    private static final int L_M1                 =  8;
    private static final int L_2                  =  9;
    private static final int L_4                  = 10;
    private static final int L_8                  = 11;
    private static final int L_16                 = 12;
    private static final int L_32                 = 13;
    private static final int L_64                 = 14;
    private static final int L_128                = 15;
    private static final int L_256                = 16;
    private static final int L_512                = 17;
    private static final int L_1024               = 18;
    private static final int L_2048               = 19;
    private static final int L_4096               = 20;
    private static final int L_8192               = 21;
    private static final int L_16384              = 22;
    private static final int L_32768              = 23;
    private static final int L_65536              = 24;
    private static final int L_16777216           = 25;
    private static final int L_4294967296         = 26;
    private static final int L_0x00000000ffffffff = 27;
    private static final int L_0xffffffff00000000 = 28;
    private static final int F_M1                 = 29;
    private static final int D_M1                 = 30;
    private static final int FIELD_I              = 31;
    private static final int FIELD_L              = 32;
    private static final int FIELD_F              = 33;
    private static final int FIELD_D              = 34;
    private static final int NAME_AND_TYPE_I      = 35;
    private static final int NAME_AND_TYPE_L      = 36;
    private static final int NAME_AND_TYPE_F      = 37;
    private static final int NAME_AND_TYPE_D      = 38;
    private static final int TYPE_I               = 39;
    private static final int TYPE_L               = 40;
    private static final int TYPE_F               = 41;
    private static final int TYPE_D               = 42;
    public static final Constant[] CONSTANTS = new Constant[]
    {
        new IntegerConstant(32768),
        new IntegerConstant(65536),
        new IntegerConstant(16777216),
        new IntegerConstant(0x0000ff00),
        new IntegerConstant(0x00ff0000),
        new IntegerConstant(0xff000000),
        new IntegerConstant(0x0000ffff),
        new IntegerConstant(0xffff0000),
        new LongConstant(-1L),
        new LongConstant(2L),
        new LongConstant(4L),
        new LongConstant(8L),
        new LongConstant(16L),
        new LongConstant(32L),
        new LongConstant(64L),
        new LongConstant(128L),
        new LongConstant(256L),
        new LongConstant(512L),
        new LongConstant(1024L),
        new LongConstant(2048L),
        new LongConstant(4096L),
        new LongConstant(8192L),
        new LongConstant(16384L),
        new LongConstant(32768L),
        new LongConstant(65536L),
        new LongConstant(16777216L),
        new LongConstant(4294967296L),
        new LongConstant(0x00000000ffffffffL),
        new LongConstant(0xffffffff00000000L),
        new FloatConstant(-1f),
        new DoubleConstant(-1d),
        new FieldrefConstant(X, NAME_AND_TYPE_I, null, null),
        new FieldrefConstant(X, NAME_AND_TYPE_L, null, null),
        new FieldrefConstant(X, NAME_AND_TYPE_F, null, null),
        new FieldrefConstant(X, NAME_AND_TYPE_D, null, null),
        new NameAndTypeConstant(Y, TYPE_I),
        new NameAndTypeConstant(Y, TYPE_L),
        new NameAndTypeConstant(Y, TYPE_F),
        new NameAndTypeConstant(Y, TYPE_D),
        new Utf8Constant("I"),
        new Utf8Constant("J"),
        new Utf8Constant("F"),
        new Utf8Constant("D"),
    };
    public static final Instruction[][][] VARIABLE = new Instruction[][][]
    {
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_NOP),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_POP),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_POP),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ALOAD, X),
                new SimpleInstruction(InstructionConstants.OP_POP),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new VariableInstruction(InstructionConstants.OP_LSTORE, X),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new VariableInstruction(InstructionConstants.OP_FSTORE, X),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new VariableInstruction(InstructionConstants.OP_DSTORE, X),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ALOAD, X),
                new SimpleInstruction(InstructionConstants.OP_POP),
            },{
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_LSTORE, X),
                new VariableInstruction(InstructionConstants.OP_LSTORE, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new VariableInstruction(InstructionConstants.OP_LSTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_FSTORE, X),
                new VariableInstruction(InstructionConstants.OP_FSTORE, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new VariableInstruction(InstructionConstants.OP_FSTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_DSTORE, X),
                new VariableInstruction(InstructionConstants.OP_DSTORE, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new VariableInstruction(InstructionConstants.OP_DSTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ASTORE, X),
                new VariableInstruction(InstructionConstants.OP_ASTORE, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new VariableInstruction(InstructionConstants.OP_ASTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_LSTORE, X),
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP2),
                new VariableInstruction(InstructionConstants.OP_LSTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_FSTORE, X),
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP),
                new VariableInstruction(InstructionConstants.OP_FSTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_DSTORE, X),
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP2),
                new VariableInstruction(InstructionConstants.OP_DSTORE, X),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ASTORE, X),
                new VariableInstruction(InstructionConstants.OP_ALOAD, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP),
                new VariableInstruction(InstructionConstants.OP_ASTORE, X),
            },
        },
    };
    public static final Instruction[][][] ARITHMETIC = new Instruction[][][]
    {
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0, A),
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_LADD),
            },{
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_LCONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_LADD),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, A),
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_LADD),
            },{
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, A),
                new SimpleInstruction(InstructionConstants.OP_LADD),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0, A),
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_LLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_LCONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FCONST_0, A),
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_FADD),
            },{
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_FCONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_FADD),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_FADD),
            },{
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new SimpleInstruction(InstructionConstants.OP_FADD),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FCONST_0, A),
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_FMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_FCONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_FMUL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_FLOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC, A),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DCONST_0, A),
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_DADD),
            },{
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_DCONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_DADD),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, A),
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_DADD),
            },{
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, A),
                new SimpleInstruction(InstructionConstants.OP_DADD),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DCONST_0, A),
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_DMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_DCONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_DMUL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, A),
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new SimpleInstruction(InstructionConstants.OP_DMUL),
            },{
                new VariableInstruction(InstructionConstants.OP_DLOAD, X),
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, A),
                new SimpleInstruction(InstructionConstants.OP_DMUL),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, A),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, A),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new SimpleInstruction(InstructionConstants.OP_IADD),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, A),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, 1),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, -1),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_2),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, -2),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_3),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, -3),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_4),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, -4),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ILOAD, X),
                new SimpleInstruction(InstructionConstants.OP_ICONST_5),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
                new VariableInstruction(InstructionConstants.OP_ISTORE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_IINC, X, -5),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
                new SimpleInstruction(InstructionConstants.OP_LADD),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FCONST_0),
                new SimpleInstruction(InstructionConstants.OP_FADD),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DCONST_0),
                new SimpleInstruction(InstructionConstants.OP_DADD),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_ISUB),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
                new SimpleInstruction(InstructionConstants.OP_LSUB),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FCONST_0),
                new SimpleInstruction(InstructionConstants.OP_FSUB),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DCONST_0),
                new SimpleInstruction(InstructionConstants.OP_DSUB),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_INEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_2),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_4),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_ICONST_2),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_ICONST_3),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 4),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 5),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 64),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 6),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 128),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 7),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 256),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 512),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 9),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 1024),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 10),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 2048),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 11),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 4096),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 12),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 8192),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 13),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 16384),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 14),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_32768),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 15),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_65536),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_16777216),
                new SimpleInstruction(InstructionConstants.OP_IMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_M1),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_LNEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_1),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_2),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_4),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_ICONST_2),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_8),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_ICONST_3),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_16),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 4),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_32),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 5),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_64),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 6),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_128),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 7),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_256),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_512),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 9),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_1024),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 10),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_2048),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 11),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_4096),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 12),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_8192),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 13),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_16384),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 14),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_32768),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 15),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_65536),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_16777216),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_4294967296),
                new SimpleInstruction(InstructionConstants.OP_LMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, F_M1),
                new SimpleInstruction(InstructionConstants.OP_FMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_FNEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FCONST_1),
                new SimpleInstruction(InstructionConstants.OP_FMUL),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, D_M1),
                new SimpleInstruction(InstructionConstants.OP_DMUL),
            },{
                new SimpleInstruction(InstructionConstants.OP_DNEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DCONST_1),
                new SimpleInstruction(InstructionConstants.OP_DMUL),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new SimpleInstruction(InstructionConstants.OP_IDIV),
            },{
                new SimpleInstruction(InstructionConstants.OP_INEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new SimpleInstruction(InstructionConstants.OP_IDIV),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_M1),
                new SimpleInstruction(InstructionConstants.OP_LDIV),
            },{
                new SimpleInstruction(InstructionConstants.OP_LNEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_1),
                new SimpleInstruction(InstructionConstants.OP_LDIV),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, F_M1),
                new SimpleInstruction(InstructionConstants.OP_FDIV),
            },{
                new SimpleInstruction(InstructionConstants.OP_FNEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FCONST_1),
                new SimpleInstruction(InstructionConstants.OP_FDIV),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, D_M1),
                new SimpleInstruction(InstructionConstants.OP_DDIV),
            },{
                new SimpleInstruction(InstructionConstants.OP_DNEG),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DCONST_1),
                new SimpleInstruction(InstructionConstants.OP_DDIV),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new SimpleInstruction(InstructionConstants.OP_IREM),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_1),
                new SimpleInstruction(InstructionConstants.OP_LREM),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_INEG),
                new SimpleInstruction(InstructionConstants.OP_INEG),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LNEG),
                new SimpleInstruction(InstructionConstants.OP_LNEG),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FNEG),
                new SimpleInstruction(InstructionConstants.OP_FNEG),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DNEG),
                new SimpleInstruction(InstructionConstants.OP_DNEG),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_INEG),
                new SimpleInstruction(InstructionConstants.OP_IADD),
            },{
                new SimpleInstruction(InstructionConstants.OP_ISUB),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LNEG),
                new SimpleInstruction(InstructionConstants.OP_LADD),
            },{
                new SimpleInstruction(InstructionConstants.OP_LSUB),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_FNEG),
                new SimpleInstruction(InstructionConstants.OP_FADD),
            },{
                new SimpleInstruction(InstructionConstants.OP_FSUB),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_DNEG),
                new SimpleInstruction(InstructionConstants.OP_DADD),
            },{
                new SimpleInstruction(InstructionConstants.OP_DSUB),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_LSHR),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_LUSHR),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_M1),
                new SimpleInstruction(InstructionConstants.OP_LAND),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
                new SimpleInstruction(InstructionConstants.OP_LAND),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new SimpleInstruction(InstructionConstants.OP_IOR),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
            },
        },
        {   
            {
               new SimpleInstruction(InstructionConstants.OP_ICONST_0),
               new SimpleInstruction(InstructionConstants.OP_IOR),
           },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_M1),
                new SimpleInstruction(InstructionConstants.OP_LAND),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_M1),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
                new SimpleInstruction(InstructionConstants.OP_LOR),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new SimpleInstruction(InstructionConstants.OP_IXOR),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_LCONST_0),
                new SimpleInstruction(InstructionConstants.OP_LXOR),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0x0000ff00),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0x0000ff00),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 8),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0x00ff0000),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0x00ff0000),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0xff000000),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0xffff0000),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0xffff0000),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, 0xff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0x0000ffff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC, I_0x0000ffff),
                new SimpleInstruction(InstructionConstants.OP_IAND),
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 24),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_IUSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHL),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 16),
                new SimpleInstruction(InstructionConstants.OP_ISHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LSHL),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_L2I),
                new SimpleInstruction(InstructionConstants.OP_I2L),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_0x00000000ffffffff),
                new SimpleInstruction(InstructionConstants.OP_LAND),
                new SimpleInstruction(InstructionConstants.OP_L2I),
            },{
                new SimpleInstruction(InstructionConstants.OP_L2I),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_0xffffffff00000000),
                new SimpleInstruction(InstructionConstants.OP_LAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LSHR),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_LDC2_W, L_0xffffffff00000000),
                new SimpleInstruction(InstructionConstants.OP_LAND),
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LUSHR),
            },{
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, 32),
                new SimpleInstruction(InstructionConstants.OP_LUSHR),
            },
        },
        {   
            {
                new VariableInstruction(InstructionConstants.OP_IINC, X, 0),
            },{
            },
        },
    };
    public static final Instruction[][][] FIELD = new Instruction[][][]
    {
        {   
            {
                new VariableInstruction(InstructionConstants.OP_ALOAD, X),
                new VariableInstruction(InstructionConstants.OP_ALOAD, X),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Y),
                new ConstantInstruction(InstructionConstants.OP_PUTFIELD, Y),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, X),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, X),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_L),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_L),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_L),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_D),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_D),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_D),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, X),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, X),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_L),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, FIELD_L),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP2),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_L),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_D),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, FIELD_D),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP2),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, FIELD_D),
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, X),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, X),
            },{
                new SimpleInstruction(InstructionConstants.OP_DUP),
                new ConstantInstruction(InstructionConstants.OP_PUTSTATIC, X),
            },
        },
    };
    public static final Instruction[][][] CAST = new Instruction[][][]
    {
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2B),
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2C),
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2S),
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2C),
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2S),
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2C),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2B),
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2B),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2C),
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2S),
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },{
                new SimpleInstruction(InstructionConstants.OP_I2S),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_I2L),
                new SimpleInstruction(InstructionConstants.OP_L2I),
            },{
            },
        },
        {   
            {
                new ConstantInstruction(InstructionConstants.OP_CHECKCAST, X),
                new ConstantInstruction(InstructionConstants.OP_CHECKCAST, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_CHECKCAST, X),
            },
        },
    };
    public static final Instruction[][][] BRANCH = new Instruction[][][]
    {
        {   
            {
                new BranchInstruction(InstructionConstants.OP_GOTO, 3),
            },{
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFEQ, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFNE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFLT, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFGE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFGT, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFLE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP2),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFNULL, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, 3),
            },{
                new SimpleInstruction(InstructionConstants.OP_POP),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_1),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ILOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_M1),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, X),
            },{
                new ConstantInstruction(InstructionConstants.OP_GETSTATIC, Y),
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, X),
            },{
                new VariableInstruction(InstructionConstants.OP_ALOAD, Y),
                new ConstantInstruction(InstructionConstants.OP_GETFIELD, Z),
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0, A),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_BIPUSH, A),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_SIPUSH, A),
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },{
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ICONST_0),
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new BranchInstruction(InstructionConstants.OP_IFNULL, X),
            },{
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },
        },
        {   
            {
                new SimpleInstruction(InstructionConstants.OP_ACONST_NULL),
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, X),
            },{
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFEQ, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFNE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFNE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFEQ, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFLT, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFGE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFGE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFLT, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFGT, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFLE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFLE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFGT, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPNE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFICMPEQ, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPGE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFICMPLT, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFICMPLE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFICMPGT, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFACMPNE, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFACMPEQ, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFNULL, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, X),
            },
        },
        {   
            {
                new BranchInstruction(InstructionConstants.OP_IFNONNULL, 6),
                new BranchInstruction(InstructionConstants.OP_GOTO, X),
            },{
                new BranchInstruction(InstructionConstants.OP_IFNULL, X),
            },
        },
        {   
            {
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, A, new int[] { X, Y }, new int[] { A, B }),
            },{
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, A, new int[] { Y }, new int[] { B }),
            },
        },
        {   
            {
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, B, new int[] { X, Y }, new int[] { A, B }),
            },{
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, B, new int[] { X }, new int[] { A }),
            },
        },
        {   
            {
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, A, new int[] { X, Y, Z }, new int[] { A, B, C }),
            },{
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, A, new int[] { Y, Z }, new int[] { B, C }),
            },
        },
        {   
            {
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, B, new int[] { X, Y, Z }, new int[] { A, B, C }),
            },{
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, B, new int[] { X, Z }, new int[] { A, C }),
            },
        },
        {   
            {
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, C, new int[] { X, Y, Z }, new int[] { A, B, C }),
            },{
                new LookUpSwitchInstruction(InstructionConstants.OP_LOOKUPSWITCH, C, new int[] { X, Y }, new int[] { A, B }),
            },
        },
    };
}