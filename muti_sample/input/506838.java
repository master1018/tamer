@TestTargetClass(Runtime.class) 
public class RuntimeTest extends junit.framework.TestCase {
    Runtime r = Runtime.getRuntime();
    InputStream is;
    String s;
    static boolean flag = false;
    static boolean ranFinalize = false;
    int statusCode = -1;
    class HasFinalizer {
        String internalString;
        HasFinalizer(String s) {
            internalString = s;
        }
        @Override
        protected void finalize() {
            internalString = "hit";
        }
    }
    @Override
    protected void finalize() {
        if (flag)
            ranFinalize = true;
    }
    protected RuntimeTest createInstance() {
        return new RuntimeTest("FT");
    }
    @Override protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "exec",
        args = {java.lang.String.class}
    )
    public void test_exec() {
        try {
            Runtime.getRuntime().exec("AnInexistentProgram");
            fail("failed to throw IOException when exec'ed inexistent program");
        } catch (IOException e) {  }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "freeMemory",
        args = {}
    )
    public void test_freeMemory() {
        assertTrue("must have some free memory",
                r.freeMemory() > 0); 
        assertTrue("must not exceed total memory",
                r.freeMemory() < r.totalMemory()); 
        long before = r.totalMemory() - r.freeMemory();
        Vector<byte[]> v = new Vector<byte[]>();
        for (int i = 1; i < 10; i++) {
            v.addElement(new byte[10000]);
        }
        long after =  r.totalMemory() - r.freeMemory();
        assertTrue("free memory must change with allocations", 
                after != before);            
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "gc",
        args = {}
    )
    public void test_gc() {
        try {
            r.gc(); 
            r.gc(); 
            long firstRead = r.totalMemory() - r.freeMemory();
            Vector<StringBuffer> v = new Vector<StringBuffer>();
            for (int i = 1; i < 10; i++)
                v.addElement(new StringBuffer(10000));
            long secondRead = r.totalMemory() - r.freeMemory();
            v = null;
            r.gc();
            r.gc();
            assertTrue("object memory did not grow", secondRead > firstRead);
            assertTrue("space was not reclaimed", (r.totalMemory() - r
                    .freeMemory()) < secondRead);
        } catch (Throwable t) {
            System.out.println("Out of memory during gc test");
            r.gc();
            r.gc();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "getRuntime method is verified in initial setup for other tests.",
        method = "getRuntime",
        args = {}
    )
    public void test_getRuntime() {
        assertNotNull(Runtime.getRuntime());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "runFinalization",
        args = {}
    )
    public void test_runFinalization() {
        flag = true;
        createInstance();
        int count = 10;
        while (!ranFinalize && count-- > 0) {
            r.gc();
            r.runFinalization();
        }
        assertTrue("Failed to run finalization", ranFinalize);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "totalMemory",
        args = {}
    )
    public void test_totalMemory() {
        assertTrue("totalMemory returned nonsense value", r.totalMemory() >= r
                .freeMemory());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addShutdownHook",
        args = {java.lang.Thread.class}
    )
    public void test_addShutdownHook() {
        Thread thrException = new Thread () {
            public void run() {
                try {
                    Runtime.getRuntime().addShutdownHook(this);
                    fail("IllegalStateException was not thrown.");
                } catch(IllegalStateException ise) {
                }
            }  
        };
        try {
            Runtime.getRuntime().addShutdownHook(thrException);
        } catch (Throwable t) {
            fail(t.getMessage());
        }
        try {
            Runtime.getRuntime().addShutdownHook(thrException);
            fail("IllegalArgumentException was not thrown.");            
        } catch(IllegalArgumentException  iae) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("shutdownHooks")) {
                    throw new SecurityException();
                }
            }
        };
        Runtime.getRuntime().removeShutdownHook(thrException);
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().addShutdownHook(thrException);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
        }
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException ie) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "availableProcessors",
        args = {}
    )
    public void test_availableProcessors() {
        assertTrue(Runtime.getRuntime().availableProcessors() > 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "exec",
        args = {java.lang.String.class, java.lang.String[].class}
    )
    public void test_execLjava_lang_StringLjava_lang_StringArray() {
        String [] envp =  getEnv();
        checkExec(0, envp, null);
        checkExec(0, null, null);
        try {
            Runtime.getRuntime().exec((String)null, null);
            fail("NullPointerException should be thrown.");
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(NullPointerException npe) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("checkExec")) {
                    throw new SecurityException();
                }
            }
            public void checkExec(String cmd) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().exec("ls", envp);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        } finally {
            System.setSecurityManager(oldSm);
        }  
        try {
            Runtime.getRuntime().exec("", envp);
            fail("IllegalArgumentException should be thrown.");            
        } catch(IllegalArgumentException iae) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "exec",
        args = {java.lang.String[].class, java.lang.String[].class}
    )
    public void test_execLjava_lang_StringArrayLjava_lang_StringArray() {
        String [] envp =  getEnv();
        checkExec(4, envp, null);
        checkExec(4, null, null);
        try {
            Runtime.getRuntime().exec((String[])null, null);
            fail("NullPointerException should be thrown.");
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(NullPointerException npe) {
        }
        try {
            Runtime.getRuntime().exec(new String[]{"ls", null}, null);
            fail("NullPointerException should be thrown.");
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(NullPointerException npe) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("checkExec")) {
                    throw new SecurityException();
                }
            }
            public void checkExec(String cmd) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().exec(new String[]{"ls"}, envp);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        } finally {
            System.setSecurityManager(oldSm);
        }  
        try {
            Runtime.getRuntime().exec(new String[]{}, envp);
            fail("IndexOutOfBoundsException should be thrown.");            
        } catch(IndexOutOfBoundsException ioob) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        } 
        try {
            Runtime.getRuntime().exec(new String[]{""}, envp);
            fail("IOException should be thrown.");
        } catch (IOException e) {  }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "exec",
        args = {java.lang.String.class, java.lang.String[].class, java.io.File.class}
    )
    public void test_execLjava_lang_StringLjava_lang_StringArrayLjava_io_File() {
        String [] envp =  getEnv();
        File workFolder = Support_Resources.createTempFolder();
        checkExec(2, envp, workFolder);
        checkExec(2, null, null);
        try {
            Runtime.getRuntime().exec((String)null, null, workFolder);
            fail("NullPointerException should be thrown.");
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(NullPointerException npe) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("checkExec")) {
                    throw new SecurityException();
                }
            }
            public void checkExec(String cmd) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().exec("ls",  envp, workFolder);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        } finally {
            System.setSecurityManager(oldSm);
        }  
        try {
            Runtime.getRuntime().exec("",  envp, workFolder);
            fail("SecurityException should be thrown.");            
        } catch(IllegalArgumentException iae) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "exec",
        args = {java.lang.String[].class, java.lang.String[].class, java.io.File.class}
    )
    public void test_execLjava_lang_StringArrayLjava_lang_StringArrayLjava_io_File() {
        String [] envp =  getEnv();
        File workFolder = Support_Resources.createTempFolder();
        checkExec(5, envp, workFolder);
        checkExec(5, null, null);
        try {
            Runtime.getRuntime().exec((String[])null, null, workFolder);
            fail("NullPointerException should be thrown.");
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(NullPointerException npe) {
        }
        try {
            Runtime.getRuntime().exec(new String[]{"ls", null}, null, workFolder);
            fail("NullPointerException should be thrown.");
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        } catch(NullPointerException npe) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("checkExec")) {
                    throw new SecurityException();
                }
            }
            public void checkExec(String cmd) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().exec(new String[] {"ls"},  envp, workFolder);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        } finally {
            System.setSecurityManager(oldSm);
        }  
        try {
            Runtime.getRuntime().exec(new String[]{""}, envp, workFolder);
            fail("IOException should be thrown.");
        } catch (IOException e) {
        }
    }
    String [] getEnv() {
        Object [] valueSet = System.getenv().values().toArray();
        Object [] keySet = System.getenv().keySet().toArray();  
        String [] envp = new String[valueSet.length];
        for(int i = 0; i < envp.length; i++) {
            envp[i] = keySet[i] + "=" + valueSet[i];
        }
        return envp;
    }
    void checkExec(int testCase, String [] envp, File file) {
        String dirName = "Test_Directory";
        String dirParentName = "Parent_Directory";
        File resources = Support_Resources.createTempFolder();
        String folder = resources.getAbsolutePath() + "/" + dirName;
        String folderWithParent = resources.getAbsolutePath() + "/"  + 
                                    dirParentName + "/" + dirName;
        String command = "mkdir " + folder;
        String [] commandArguments = {"mkdir", folder};        
        try {
            Process proc = null;
            switch(testCase) {
                case 0: 
                    proc = Runtime.getRuntime().exec(command, envp);
                    break;
                case 1: 
                    proc = Runtime.getRuntime().exec(command);
                    break;
                case 2: 
                    proc = Runtime.getRuntime().exec(command, envp, file);
                    break; 
                case 3:
                    proc = Runtime.getRuntime().exec(commandArguments);
                    break;
                case 4:
                    proc = Runtime.getRuntime().exec(commandArguments, envp);
                    break;  
                case 5:
                    proc = Runtime.getRuntime().exec(commandArguments, envp, file);
                    break; 
            }
            assertNotNull(proc);
            try {
                Thread.sleep(3000);
            } catch(InterruptedException ie) {
                fail("InterruptedException was thrown.");
            }
            File f = new File(folder);
            assertTrue(f.exists());
            if(f.exists()) {
                f.delete();
            }
        } catch(IOException io) {
            fail("IOException was thrown.");
        }  
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "exec",
        args = {java.lang.String.class}
    )
    public void test_execLjava_lang_String() {
        checkExec(1, null, null);        
        try {
            Runtime.getRuntime().exec((String) null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }
        try {
            Runtime.getRuntime().exec("");
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }  
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkExec(String cmd) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().exec("ls");
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } catch (IOException ioe) {
            fail("IOException was thrown.");
        } finally {
            System.setSecurityManager(oldSm);
        }   
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "exec",
        args = {java.lang.String[].class}
    )
    public void test_execLjava_lang_StringArray() {
        checkExec(3, null, null);        
        try {
            Runtime.getRuntime().exec((String[]) null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }
        try {
            Runtime.getRuntime().exec(new String[]{"ls", null});
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }        
        try {
            Runtime.getRuntime().exec(new String[]{});
            fail("IndexOutOfBoundsException was not thrown.");
        } catch(IndexOutOfBoundsException iobe) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }  
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkExec(String cmd) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().exec(new String[]{"ls"});
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } catch (IOException ioe) {
            fail("IOException was thrown.");
        } finally {
            System.setSecurityManager(oldSm);
        }          
        try {
            Runtime.getRuntime().exec(new String[]{""});
            fail("IOException should be thrown.");
        } catch (IOException e) {
        }
    }   
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "runFinalizersOnExit",
        args = {boolean.class}
    )
    public void test_runFinalizersOnExit() {
        Runtime.getRuntime().runFinalizersOnExit(true);
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkExit(int status) {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().runFinalizersOnExit(true);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeShutdownHook",
        args = {java.lang.Thread.class}
    )
    public void test_removeShutdownHookLjava_lang_Thread() {
        Thread thr1 = new Thread () {
            public void run() {
                try {
                    Runtime.getRuntime().addShutdownHook(this);
                } catch(IllegalStateException ise) {
                    fail("IllegalStateException shouldn't be thrown.");
                }
            }  
        };
        try {
            Runtime.getRuntime().addShutdownHook(thr1);
            Runtime.getRuntime().removeShutdownHook(thr1);
        } catch (Throwable t) {
            fail(t.getMessage());
        }
        Thread thr2 = new Thread () {
            public void run() {
                try {
                    Runtime.getRuntime().removeShutdownHook(this);
                    fail("IllegalStateException wasn't thrown.");                    
                } catch(IllegalStateException ise) {
                }
            }  
        };
        try {
            Runtime.getRuntime().addShutdownHook(thr2);
        } catch (Throwable t) {
            fail(t.getMessage());
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("shutdownHooks")) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().addShutdownHook(thr1);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
        }
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException ie) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "maxMemory",
        args = {}
    )
    public void test_maxMemory() {
        assertTrue(Runtime.getRuntime().maxMemory() < Long.MAX_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "traceInstructions",
        args = {boolean.class}
    )
    public void test_traceInstructions() {
        Runtime.getRuntime().traceInstructions(false);
        Runtime.getRuntime().traceInstructions(true);
        Runtime.getRuntime().traceInstructions(false);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "traceMethodCalls",
        args = {boolean.class}
    )
    @KnownFailure("Fails in CTS but passes under run-core-tests")
    public void test_traceMethodCalls() {
        try {
            Runtime.getRuntime().traceMethodCalls(false);
            Runtime.getRuntime().traceMethodCalls(true);
            Runtime.getRuntime().traceMethodCalls(false);
        } catch (RuntimeException ex) {
            if (!"file open failed".equals(ex.getMessage())) {
                throw ex;
            }
        }
    }
    @SuppressWarnings("deprecation")
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalizedInputStream",
        args = {java.io.InputStream.class}
    )
    public void test_getLocalizedInputStream() {
        String simpleString = "Heart \u2f3c";
        byte[] expected = {72, 0, 101, 0, 97, 0, 114, 0, 116, 0, 32, 0, 60, 47};
        byte[] returned = new byte[expected.length];
        System.setProperty("file.encoding", "UTF-16LE");
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    simpleString.getBytes("UTF-8")); 
            InputStream lcIn = 
                    Runtime.getRuntime().getLocalizedInputStream(bais);
            try {
                lcIn.read(returned);
            } catch(IOException ioe) {
                fail("IOException was thrown.");
            }  
            assertTrue("wrong result for String: " + simpleString,
                    Arrays.equals(expected, returned));
        } catch (UnsupportedEncodingException e) {
            fail("UnsupportedEncodingException was thrown.");
        }
    }
    @SuppressWarnings("deprecation")
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "getLocalizedOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void test_getLocalizedOutputStream() {
        String simpleString = "Heart \u2f3c";
        byte[] expected = {72, 0, 101, 0, 97, 0, 114, 0, 116, 0, 32, 0, 60, 47};
        byte[] returned;
        String oldEncoding = System.getProperty("file.encoding");
        System.setProperty("file.encoding", "UTF-16LE");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(); 
            OutputStream lcOut = 
                    Runtime.getRuntime().getLocalizedOutputStream(out);
            try {
                lcOut.write(simpleString.getBytes("UTF-8"));
                lcOut.flush();
                lcOut.close();
            } catch(IOException ioe) {
                fail("IOException was thrown.");
            }
            returned = out.toByteArray();
            assertTrue("wrong result for String: " + returned.toString() + 
                    " expected string: " + expected.toString(),
                    Arrays.equals(expected, returned));  
        } finally {
            System.setProperty("file.encoding", oldEncoding);
        }
    }   
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "load",
        args = {java.lang.String.class}
    )
    public void test_load() {
        try {
            Runtime.getRuntime().load("nonExistentLibrary");
            fail("UnsatisfiedLinkError was not thrown.");
        } catch(UnsatisfiedLinkError ule) {
        }
        try {
            Runtime.getRuntime().load(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkLink(String lib) {
                if (lib.endsWith("libjvm.so")) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().load("libjvm.so");
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "loadLibrary",
        args = {java.lang.String.class}
    )
    public void test_loadLibrary() {
        try {
            Runtime.getRuntime().loadLibrary("nonExistentLibrary");
            fail("UnsatisfiedLinkError was not thrown.");
        } catch(UnsatisfiedLinkError ule) {
        }
        try {
            Runtime.getRuntime().loadLibrary(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkLink(String lib) {
                if (lib.endsWith("libjvm.so")) {
                    throw new SecurityException();
                }
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            Runtime.getRuntime().loadLibrary("libjvm.so");
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
        }               
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "This method never returns normally, " +
                "and can't be tested. Only SecurityException can be checked.",
        method = "exit",
        args = {int.class}
    )    
    public void test_exit() {
        statusCode = -1;        
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkExit(int status) {
                statusCode = status;
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            r.exit(0);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            assertTrue("Incorrect status code was received: " + statusCode, 
                    statusCode == 0);            
            System.setSecurityManager(oldSm);
        }  
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Can't be tested. This method terminates the currently " +
                "running VM. Only SecurityException can be checked.",
        method = "halt",
        args = {int.class}
    )         
    public void test_halt() {
        statusCode = -1;
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkExit(int status) {
                statusCode = status;
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            r.halt(0);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            assertTrue("Incorrect status code was received: " + statusCode, 
                    statusCode == 0);
            System.setSecurityManager(oldSm);
        }  
    }
    public RuntimeTest() {
    }
    public RuntimeTest(String name) {
        super(name);
    }
}
