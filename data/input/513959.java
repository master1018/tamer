import proguard.classfile.*;
import proguard.classfile.visitor.*;
public class ShortestUsageMarker extends UsageMarker
{
    private static final ShortestUsageMark INITIAL_MARK =
        new ShortestUsageMark("is kept by a directive in the configuration.\n\n");
    private ShortestUsageMark currentUsageMark = INITIAL_MARK;
    private final MyRecursiveCauseChecker recursiveCauseChecker = new MyRecursiveCauseChecker();
    protected void markProgramClassBody(ProgramClass programClass)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;
        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(programClass),
                                                 "is extended by   ",
                                                 10000,
                                                 programClass);
        super.markProgramClassBody(programClass);
        currentUsageMark = previousUsageMark;
    }
    protected void markProgramFieldBody(ProgramClass programClass, ProgramField programField)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;
        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(programField),
                                                 "is referenced by ",
                                                 1,
                                                 programClass,
                                                 programField);
        super.markProgramFieldBody(programClass, programField);
        currentUsageMark = previousUsageMark;
    }
    protected void markProgramMethodBody(ProgramClass programClass, ProgramMethod programMethod)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;
        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(programMethod),
                                                 "is invoked by    ",
                                                 1,
                                                 programClass,
                                                 programMethod);
        super.markProgramMethodBody(programClass, programMethod);
        currentUsageMark = previousUsageMark;
    }
    protected void markMethodHierarchy(Clazz clazz, Method method)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;
        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(method),
                                                 "implements       ",
                                                 100,
                                                 clazz,
                                                 method);
        super.markMethodHierarchy(clazz, method);
        currentUsageMark = previousUsageMark;
    }
    protected void markAsUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();
        ShortestUsageMark shortestUsageMark =
            visitorInfo != null                           &&
            visitorInfo instanceof ShortestUsageMark      &&
            !((ShortestUsageMark)visitorInfo).isCertain() &&
            !currentUsageMark.isShorter((ShortestUsageMark)visitorInfo) ?
                new ShortestUsageMark((ShortestUsageMark)visitorInfo, true):
                currentUsageMark;
        visitorAccepter.setVisitorInfo(shortestUsageMark);
    }
    protected boolean shouldBeMarkedAsUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();
        return 
               (visitorInfo == null                           ||
               !(visitorInfo instanceof ShortestUsageMark)   ||
               !((ShortestUsageMark)visitorInfo).isCertain() ||
               currentUsageMark.isShorter((ShortestUsageMark)visitorInfo));
    }
    protected boolean isUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();
        return visitorInfo != null                      &&
               visitorInfo instanceof ShortestUsageMark &&
               ((ShortestUsageMark)visitorInfo).isCertain();
    }
    protected void markAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(new ShortestUsageMark(currentUsageMark, false));
    }
    protected boolean shouldBeMarkedAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();
        return visitorInfo == null                         ||
               !(visitorInfo instanceof ShortestUsageMark) ||
               (!((ShortestUsageMark)visitorInfo).isCertain() &&
                currentUsageMark.isShorter((ShortestUsageMark)visitorInfo));
    }
    protected boolean isPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();
        return visitorInfo != null                      &&
               visitorInfo instanceof ShortestUsageMark &&
               !((ShortestUsageMark)visitorInfo).isCertain();
    }
    protected ShortestUsageMark getShortestUsageMark(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();
        return (ShortestUsageMark)visitorInfo;
    }
    private boolean isCausedBy(ShortestUsageMark shortestUsageMark,
                               Clazz             clazz)
    {
        return recursiveCauseChecker.check(shortestUsageMark, clazz);
    }
    private class MyRecursiveCauseChecker implements ClassVisitor, MemberVisitor
    {
        private Clazz   checkClass;
        private boolean isRecursing;
        public boolean check(ShortestUsageMark shortestUsageMark,
                             Clazz             clazz)
        {
            checkClass  = clazz;
            isRecursing = false;
            shortestUsageMark.acceptClassVisitor(this);
            shortestUsageMark.acceptMemberVisitor(this);
            return isRecursing;
        }
        public void visitProgramClass(ProgramClass programClass)
        {
            checkCause(programClass);
        }
        public void visitLibraryClass(LibraryClass libraryClass)
        {
            checkCause(libraryClass);
        }
        public void visitProgramField(ProgramClass programClass, ProgramField programField)
        {
            checkCause(programField);
        }
        public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
        {
            checkCause(programMethod);
        }
        public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
        {
             checkCause(libraryField);
       }
        public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
        {
            checkCause(libraryMethod);
        }
        private void checkCause(VisitorAccepter visitorAccepter)
        {
            if (ShortestUsageMarker.this.isUsed(visitorAccepter))
            {
                ShortestUsageMark shortestUsageMark = ShortestUsageMarker.this.getShortestUsageMark(visitorAccepter);
                isRecursing = shortestUsageMark.isCausedBy(checkClass);
                if (!isRecursing)
                {
                    shortestUsageMark.acceptClassVisitor(this);
                    shortestUsageMark.acceptMemberVisitor(this);
                }
            }
        }
    }
}
