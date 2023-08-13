package proguard.classfile.util;
import proguard.classfile.ClassConstants;
public class AccessUtil
{
    public static final int PRIVATE         = 0;
    public static final int PACKAGE_VISIBLE = 1;
    public static final int PROTECTED       = 2;
    public static final int PUBLIC          = 3;
    private static final int ACCESS_MASK =
        ClassConstants.INTERNAL_ACC_PUBLIC  |
        ClassConstants.INTERNAL_ACC_PRIVATE |
        ClassConstants.INTERNAL_ACC_PROTECTED;
    public static int accessLevel(int accessFlags)
    {
        switch (accessFlags & ACCESS_MASK)
        {
            case ClassConstants.INTERNAL_ACC_PRIVATE:   return PRIVATE;
            default:                                    return PACKAGE_VISIBLE;
            case ClassConstants.INTERNAL_ACC_PROTECTED: return PROTECTED;
            case ClassConstants.INTERNAL_ACC_PUBLIC:    return PUBLIC;
        }
    }
    public static int accessFlags(int accessLevel)
    {
        switch (accessLevel)
        {
            case PRIVATE:   return ClassConstants.INTERNAL_ACC_PRIVATE;
            default:        return 0;
            case PROTECTED: return ClassConstants.INTERNAL_ACC_PROTECTED;
            case PUBLIC:    return ClassConstants.INTERNAL_ACC_PUBLIC;
        }
    }
    public static int replaceAccessFlags(int accessFlags, int newAccessFlags)
    {
        if (newAccessFlags == ClassConstants.INTERNAL_ACC_PRIVATE)
        {
            accessFlags &= ~ClassConstants.INTERNAL_ACC_FINAL;
        }
        return (accessFlags    & ~ACCESS_MASK) |
               (newAccessFlags &  ACCESS_MASK);
    }
}
