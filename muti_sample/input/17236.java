public class ExampleRedefine
    {
    private Counter fCounter;
    public
    ExampleRedefine()
        {
        super();
        System.out.println("Simple ExampleRedefine constructor");
        fCounter = new Counter();
        }
    public int
    get()
        {
        return fCounter.get();
        }
    public void
    doSomething()
        {
        }
    }
