public class TimeUnitTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());        
    }
    public static Test suite() {
        return new TestSuite(TimeUnitTest.class);
    }
    public void testConvert() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t,
                         TimeUnit.SECONDS.convert(t, 
                                                  TimeUnit.SECONDS));
            assertEquals(t, 
                         TimeUnit.SECONDS.convert(1000L*t, 
                                                  TimeUnit.MILLISECONDS));
            assertEquals(t, 
                         TimeUnit.SECONDS.convert(1000000L*t, 
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t, 
                         TimeUnit.SECONDS.convert(1000000000L*t, 
                                                  TimeUnit.NANOSECONDS));
            assertEquals(1000L*t,
                         TimeUnit.MILLISECONDS.convert(t, 
                                                  TimeUnit.SECONDS));
            assertEquals(t, 
                         TimeUnit.MILLISECONDS.convert(t, 
                                                  TimeUnit.MILLISECONDS));
            assertEquals(t, 
                         TimeUnit.MILLISECONDS.convert(1000L*t, 
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t, 
                         TimeUnit.MILLISECONDS.convert(1000000L*t, 
                                                  TimeUnit.NANOSECONDS));
            assertEquals(1000000L*t,
                         TimeUnit.MICROSECONDS.convert(t, 
                                                  TimeUnit.SECONDS));
            assertEquals(1000L*t, 
                         TimeUnit.MICROSECONDS.convert(t, 
                                                  TimeUnit.MILLISECONDS));
            assertEquals(t, 
                         TimeUnit.MICROSECONDS.convert(t, 
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t, 
                         TimeUnit.MICROSECONDS.convert(1000L*t, 
                                                  TimeUnit.NANOSECONDS));
            assertEquals(1000000000L*t,
                         TimeUnit.NANOSECONDS.convert(t, 
                                                  TimeUnit.SECONDS));
            assertEquals(1000000L*t, 
                         TimeUnit.NANOSECONDS.convert(t, 
                                                  TimeUnit.MILLISECONDS));
            assertEquals(1000L*t, 
                         TimeUnit.NANOSECONDS.convert(t, 
                                                  TimeUnit.MICROSECONDS));
            assertEquals(t, 
                         TimeUnit.NANOSECONDS.convert(t, 
                                                  TimeUnit.NANOSECONDS));
        }
    }
    public void testToNanos() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(1000000000L*t,
                         TimeUnit.SECONDS.toNanos(t));
            assertEquals(1000000L*t, 
                         TimeUnit.MILLISECONDS.toNanos(t));
            assertEquals(1000L*t, 
                         TimeUnit.MICROSECONDS.toNanos(t));
            assertEquals(t, 
                         TimeUnit.NANOSECONDS.toNanos(t));
        }
    }
    public void testToMicros() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(1000000L*t,
                         TimeUnit.SECONDS.toMicros(t));
            assertEquals(1000L*t, 
                         TimeUnit.MILLISECONDS.toMicros(t));
            assertEquals(t, 
                         TimeUnit.MICROSECONDS.toMicros(t));
            assertEquals(t, 
                         TimeUnit.NANOSECONDS.toMicros(t*1000L));
        }
    }
    public void testToMillis() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(1000L*t,
                         TimeUnit.SECONDS.toMillis(t));
            assertEquals(t, 
                         TimeUnit.MILLISECONDS.toMillis(t));
            assertEquals(t, 
                         TimeUnit.MICROSECONDS.toMillis(t*1000L));
            assertEquals(t, 
                         TimeUnit.NANOSECONDS.toMillis(t*1000000L));
        }
    }
    public void testToSeconds() {
        for (long t = 0; t < 88888; ++t) {
            assertEquals(t,
                         TimeUnit.SECONDS.toSeconds(t));
            assertEquals(t, 
                         TimeUnit.MILLISECONDS.toSeconds(t*1000L));
            assertEquals(t, 
                         TimeUnit.MICROSECONDS.toSeconds(t*1000000L));
            assertEquals(t, 
                         TimeUnit.NANOSECONDS.toSeconds(t*1000000000L));
        }
    }
    public void testConvertSaturate() {
        assertEquals(Long.MAX_VALUE,
                     TimeUnit.NANOSECONDS.convert(Long.MAX_VALUE / 2,
                                                  TimeUnit.SECONDS));
        assertEquals(Long.MIN_VALUE,
                     TimeUnit.NANOSECONDS.convert(-Long.MAX_VALUE / 4,
                                                  TimeUnit.SECONDS));
    }
    public void testToNanosSaturate() {
            assertEquals(Long.MAX_VALUE,
                         TimeUnit.MILLISECONDS.toNanos(Long.MAX_VALUE / 2));
            assertEquals(Long.MIN_VALUE,
                         TimeUnit.MILLISECONDS.toNanos(-Long.MAX_VALUE / 3));
    }
    public void testToString() {
        String s = TimeUnit.SECONDS.toString();
        assertTrue(s.indexOf("ECOND") >= 0);
    }
    public void testTimedWait_IllegalMonitorException() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    Object o = new Object();
                    TimeUnit tu = TimeUnit.MILLISECONDS;
                    try {
                        tu.timedWait(o,LONG_DELAY_MS);
                        threadShouldThrow();
                    }
                    catch (InterruptedException ie) {
                        threadUnexpectedException();
                    } 
                    catch(IllegalMonitorStateException success) {
                    }
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    public void testTimedWait() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    Object o = new Object();
                    TimeUnit tu = TimeUnit.MILLISECONDS;
                    try {
                        synchronized(o) {
                            tu.timedWait(o,MEDIUM_DELAY_MS);
                        }
                        threadShouldThrow();
                    }
                    catch(InterruptedException success) {} 
                    catch(IllegalMonitorStateException failure) {
                        threadUnexpectedException();
                    }
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    public void testTimedJoin() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    TimeUnit tu = TimeUnit.MILLISECONDS;        
                    try {
                        Thread s = new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        Thread.sleep(MEDIUM_DELAY_MS);
                                    } catch(InterruptedException success){}
                                }
                            });
                        s.start();
                        tu.timedJoin(s,MEDIUM_DELAY_MS);
                        threadShouldThrow();
                    }
                    catch(Exception e) {}
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    public void testTimedSleep() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    TimeUnit tu = TimeUnit.MILLISECONDS;
                    try {
                        tu.sleep(MEDIUM_DELAY_MS);
                        threadShouldThrow();
                    }
                    catch(InterruptedException success) {} 
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e) {
            unexpectedException();
        }
    }
    public void testSerialization() {
        TimeUnit q = TimeUnit.MILLISECONDS;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            TimeUnit r = (TimeUnit)in.readObject();
            assertEquals(q.toString(), r.toString());
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
}
