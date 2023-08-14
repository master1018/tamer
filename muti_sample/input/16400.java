public class LangVers extends Doclet {
    public static void main(String[] args) {
        String thisFile = "" +
            new java.io.File(System.getProperty("test.src", "."),
                             "LangVers.java");
        if (com.sun.tools.javadoc.Main.execute(
                "javadoc",
                "LangVers",
                LangVers.class.getClassLoader(),
                new String[] {"-source", "1.5", thisFile}) != 0)
            throw new Error("Javadoc encountered warnings or errors.");
    }
    public static boolean start(RootDoc root) {
        ClassDoc fishdoc = root.classNamed("LangVers.Fish");
        System.out.println(fishdoc);
        if (fishdoc.isEnum()) {
            throw new Error("Enums are not hidden.");
        }
        for (MethodDoc meth : fishdoc.methods()) {
            System.out.println(meth);
            if (meth.flatSignature().indexOf('<') >= 0) {
                throw new Error("Type parameters are not hidden.");
            }
        }
        return true;
    }
    public enum Fish {
        One, Two, Red, Blue;
        public void enroll(List<? super Fish> school) {
            school.add(this);
        }
    }
}
