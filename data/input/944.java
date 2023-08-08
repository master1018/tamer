public class GateKeeper {
    private Log log = LogFactory.getLog(this.getClass());
    @Before("execution(public * *.service.*.*(..))")
    public void enter(JoinPoint jp) {
        log.debug("Entering " + jp.toString());
    }
    @After("execution(public * *.service.*.*(..))")
    public void exit(JoinPoint jp) {
        log.debug("Exiting  " + jp.toString());
    }
}
