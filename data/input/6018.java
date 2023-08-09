public class WrappedStaticApf {
    public static void main(String argv[]) {
        AnnotationProcessorFactory factory = new StaticApf();
        System.exit(com.sun.tools.apt.Main.process(factory, argv));
    }
}
