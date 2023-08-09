public class VerifyAccess {
    private VerifyAccess() { }  
    private static final int PACKAGE_ONLY = 0;
    private static final int ALL_ACCESS_MODES = (PUBLIC|PRIVATE|PROTECTED|PACKAGE_ONLY);
    private static final boolean ALLOW_NESTMATE_ACCESS = false;
    public static boolean isMemberAccessible(Class<?> refc,  
                                             Class<?> defc,  
                                             int      mods,  
                                             Class<?> lookupClass) {
        if (refc != defc) {
            if (!isClassAccessible(refc, lookupClass)) {
                return false;
            }
            if ((mods & (ALL_ACCESS_MODES|STATIC)) == (PROTECTED|STATIC)) {
                if (!isRelatedClass(refc, lookupClass))
                    return isSamePackage(defc, lookupClass);
            }
        }
        if (defc == lookupClass)
            return true;        
        switch (mods & ALL_ACCESS_MODES) {
        case PUBLIC:
            if (refc != defc)  return true;  
            return isClassAccessible(refc, lookupClass);
        case PROTECTED:
            return isSamePackage(defc, lookupClass) || isPublicSuperClass(defc, lookupClass);
        case PACKAGE_ONLY:
            return isSamePackage(defc, lookupClass);
        case PRIVATE:
            return (ALLOW_NESTMATE_ACCESS &&
                    isSamePackageMember(defc, lookupClass));
        default:
            throw new IllegalArgumentException("bad modifiers: "+Modifier.toString(mods));
        }
    }
    static boolean isRelatedClass(Class<?> refc, Class<?> lookupClass) {
        return (refc == lookupClass ||
                refc.isAssignableFrom(lookupClass) ||
                lookupClass.isAssignableFrom(refc));
    }
    static boolean isPublicSuperClass(Class<?> defc, Class<?> lookupClass) {
        return isPublic(defc.getModifiers()) && defc.isAssignableFrom(lookupClass);
    }
    public static boolean isClassAccessible(Class<?> refc, Class<?> lookupClass) {
        int mods = refc.getModifiers();
        if (isPublic(mods))
            return true;
        if (isSamePackage(lookupClass, refc))
            return true;
        return false;
    }
    public static boolean isSamePackage(Class<?> class1, Class<?> class2) {
        assert(!class1.isArray() && !class2.isArray());
        if (class1 == class2)
            return true;
        if (!loadersAreRelated(class1.getClassLoader(), class2.getClassLoader(), false))
            return false;
        String name1 = class1.getName(), name2 = class2.getName();
        int dot = name1.lastIndexOf('.');
        if (dot != name2.lastIndexOf('.'))
            return false;
        for (int i = 0; i < dot; i++) {
            if (name1.charAt(i) != name2.charAt(i))
                return false;
        }
        return true;
    }
    public static String getPackageName(Class<?> cls) {
        assert(!cls.isArray());
        String name = cls.getName();
        int dot = name.lastIndexOf('.');
        if (dot < 0)  return "";
        return name.substring(0, dot);
    }
    public static boolean isSamePackageMember(Class<?> class1, Class<?> class2) {
        if (class1 == class2)
            return true;
        if (!isSamePackage(class1, class2))
            return false;
        if (getOutermostEnclosingClass(class1) != getOutermostEnclosingClass(class2))
            return false;
        return true;
    }
    private static Class<?> getOutermostEnclosingClass(Class<?> c) {
        Class<?> pkgmem = c;
        for (Class<?> enc = c; (enc = enc.getEnclosingClass()) != null; )
            pkgmem = enc;
        return pkgmem;
    }
    private static boolean loadersAreRelated(ClassLoader loader1, ClassLoader loader2,
                                             boolean loader1MustBeParent) {
        if (loader1 == loader2 || loader1 == null
                || (loader2 == null && !loader1MustBeParent)) {
            return true;
        }
        for (ClassLoader scan2 = loader2;
                scan2 != null; scan2 = scan2.getParent()) {
            if (scan2 == loader1)  return true;
        }
        if (loader1MustBeParent)  return false;
        for (ClassLoader scan1 = loader1;
                scan1 != null; scan1 = scan1.getParent()) {
            if (scan1 == loader2)  return true;
        }
        return false;
    }
    public static boolean classLoaderIsAncestor(Class<?> parentClass, Class<?> childClass) {
        return loadersAreRelated(parentClass.getClassLoader(), childClass.getClassLoader(), true);
    }
}
