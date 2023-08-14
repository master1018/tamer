public class Application {
    public int doSomething() {
        org.tools.Tracer.trace("MethodEnter");
        System.out.println("Bring us up to warp speed!");
        org.tools.Tracer.trace("MethodExit");
        return 3;
    }
}
