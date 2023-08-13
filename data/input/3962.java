public abstract class
ATransformerManagementTestCase
    extends AInstrumentationTestCase
{
    private static final String redefinedClassName = "DummyClass";
    protected int   kModSamples = 2;
    public final    ClassFileTransformer[] kTransformerSamples = new ClassFileTransformer[]
        {
            new MyClassFileTransformer(         Integer.toString(0)),
            new MyClassFileTransformer( Integer.toString(1)),
            new MyClassFileTransformer(         Integer.toString(2)),
            new MyClassFileTransformer( Integer.toString(3)),
            new MyClassFileTransformer(         Integer.toString(4)),
            new MyClassFileTransformer( Integer.toString(5)),
            new MyClassFileTransformer(         Integer.toString(6)),
            new MyClassFileTransformer( Integer.toString(7)),
            new MyClassFileTransformer(         Integer.toString(8)),
            new MyClassFileTransformer( Integer.toString(9)),
            new MyClassFileTransformer(         Integer.toString(10)),
            new MyClassFileTransformer( Integer.toString(11)),
            new MyClassFileTransformer( Integer.toString(12)),
            new MyClassFileTransformer( Integer.toString(13)),
            new MyClassFileTransformer(         Integer.toString(14)),
            new MyClassFileTransformer( Integer.toString(15)),
            new MyClassFileTransformer(         Integer.toString(16)),
            new MyClassFileTransformer(         Integer.toString(17)),
            new MyClassFileTransformer( Integer.toString(18)),
        };
    private ArrayList           fTransformers;       
    private int                 fTransformerIndex;   
    private String              fDelayedFailure;     
    public ATransformerManagementTestCase(String name)
    {
        super(name);
    }
    protected ClassFileTransformer
    getRandomTransformer()
    {
        int randIndex = (int)Math.floor(Math.random() * kTransformerSamples.length);
        verbosePrint("Choosing random transformer #" + randIndex);
        return kTransformerSamples[randIndex];
    }
    protected void
    addTransformerToManager(
        Instrumentation         manager,
        ClassFileTransformer    transformer)
    {
        if (transformer != null)
        {
            fTransformers.add(transformer);
        }
        manager.addTransformer(transformer);
        verbosePrint("Added transformer " + transformer);
    }
    protected void
    removeTransformerFromManager(
        Instrumentation manager,
        ClassFileTransformer transformer)
    {
        assertTrue("Transformer not found in manager ("+transformer+")", manager.removeTransformer(transformer));
        if (transformer != null)
        {
            fTransformers.remove(transformer);
        }
        verbosePrint("Removed transformer " + transformer);
    }
    protected void
    removeTransformerFromManager(   Instrumentation manager,
                                    ClassFileTransformer transformer,
                                    boolean decrementIndex)
    {
        removeTransformerFromManager(manager, transformer);
        if (decrementIndex)
        {
            fTransformerIndex--;
            verbosePrint("removeTransformerFromManager fTransformerIndex decremented to: " +
                         fTransformerIndex);
        }
    }
    protected void
    verifyTransformers(Instrumentation manager)
    {
        File f = new File(System.getProperty("test.classes", "."), redefinedClassName + ".class");
        System.out.println("Reading test class from " + f);
        try
        {
            InputStream redefineStream = new FileInputStream(f);
            byte[] bytes = NamedBuffer.loadBufferFromStream(redefineStream);
            ClassDefinition cd = new ClassDefinition(DummyClass.class, bytes);
            fInst.redefineClasses(new ClassDefinition[]{ cd });
            verbosePrint("verifyTransformers redefined " + redefinedClassName);
        }
        catch (IOException e)
        {
            fail("Could not load the class: " + redefinedClassName);
        }
        catch (ClassNotFoundException e)
        {
            fail("Could not find the class: " + redefinedClassName);
        }
        catch (UnmodifiableClassException e)
        {
            fail("Could not modify the class: " + redefinedClassName);
        }
        assertTrue(fDelayedFailure, fDelayedFailure == null);
        assertEquals("The number of transformers that were run does not match the expected number added to manager",
                        fTransformers.size(), fTransformerIndex);
    }
    private void
    checkInTransformer(ClassFileTransformer transformer)
    {
        verbosePrint("checkInTransformer: " + transformer);
        if (fDelayedFailure == null)
        {
            if (fTransformers.size() <= fTransformerIndex)
            {
                String msg = "The number of transformers that have checked in (" +(fTransformerIndex+1) +
                    ") is greater number of tranformers created ("+fTransformers.size()+")";
                fDelayedFailure = msg;
                System.err.println("Delayed failure: " + msg);
                verbosePrint("Delayed failure: " + msg);
            }
            if (!fTransformers.get(fTransformerIndex).equals(transformer))
            {
                String msg = "Transformer " + fTransformers.get(fTransformerIndex) +
                    " should be the same as " + transformer;
                fDelayedFailure = msg;
                System.err.println("Delayed failure: " + msg);
                verbosePrint("Delayed failure: " + msg);
            }
            fTransformerIndex++;
            verbosePrint("fTransformerIndex incremented to: " + fTransformerIndex);
        }
    }
    protected void
    setUp()
        throws Exception
    {
        super.setUp();
        fTransformers = new ArrayList();
        fTransformerIndex = 0;
        fDelayedFailure = null;
        verbosePrint("setUp completed");
    }
    protected void
    tearDown()
        throws Exception
    {
        verbosePrint("tearDown beginning");
        fTransformers = null;
        super.tearDown();
    }
    public class MyClassFileTransformer extends SimpleIdentityTransformer {
        private final String fID;
        public MyClassFileTransformer(String id) {
            super();
            fID = id;
        }
        public String toString() {
            return MyClassFileTransformer.this.getClass().getName() + fID;
        }
        public byte[]
        transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain    protectionDomain,
            byte[] classfileBuffer) {
            if (classBeingRedefined != null) checkInTransformer(MyClassFileTransformer.this);
            return super.transform(    loader,
                                        className,
                                        classBeingRedefined,
                                        protectionDomain,
                                        classfileBuffer);
        }
    }
    public class MyClassLoader extends ClassLoader
    {
        public MyClassLoader()
        {
            super();
        }
    }
    public String debug_byteArrayToString(byte[] b) {
        if (b == null) return "null";
        StringBuffer buf = new StringBuffer();
        buf.append("byte[");
        buf.append(b.length);
        buf.append("] (");
        for (int i = 0; i < b.length; i++)
        {
            buf.append(b[i]);
            buf.append(",");
        }
        buf.deleteCharAt(buf.length()-1);
        buf.append(")");
        return buf.toString();
    }
}
