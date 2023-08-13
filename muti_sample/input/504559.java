public class SynchronousQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(SynchronousQueueTest.class);
    }
    public void testEmptyFull() {
        SynchronousQueue q = new SynchronousQueue();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        assertEquals(0, q.remainingCapacity());
        assertFalse(q.offer(zero));
    }
    public void testFairEmptyFull() {
        SynchronousQueue q = new SynchronousQueue(true);
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        assertEquals(0, q.remainingCapacity());
        assertFalse(q.offer(zero));
    }
    public void testOfferNull() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) { }   
    }
    public void testAddNull() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) { }   
    }
    public void testOffer() {
        SynchronousQueue q = new SynchronousQueue();
        assertFalse(q.offer(one));
    }
    public void testAdd() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            assertEquals(0, q.remainingCapacity());
            q.add(one);
            shouldThrow();
        } catch (IllegalStateException success){
        }   
    }
    public void testAddAll1() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.addAll(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAllSelf() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.addAll(q);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }
    public void testAddAll2() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            Integer[] ints = new Integer[1];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll4() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            Integer[] ints = new Integer[1];
            for (int i = 0; i < 1; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (IllegalStateException success) {}
    }
    public void testPutNull() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.put(null);
            shouldThrow();
        } 
        catch (NullPointerException success){
        }   
        catch (InterruptedException ie) {
            unexpectedException();
        }
     }
    public void testBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue();
                        q.put(zero);
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                    }   
                }});
        t.start();
        try { 
           Thread.sleep(SHORT_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testPutWithTake() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        threadShouldThrow();
                    } catch (InterruptedException e){
                        assertTrue(added >= 1);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testTimedOffer() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch (InterruptedException success){}
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testTakeFromEmpty() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.take();
                        threadShouldThrow();
                    } catch (InterruptedException success){ }                
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testFairBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue(true);
                        q.put(zero);
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                    }   
                }});
        t.start();
        try { 
           Thread.sleep(SHORT_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testFairPutWithTake() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        threadShouldThrow();
                    } catch (InterruptedException e){
                        assertTrue(added >= 1);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testFairTimedOffer() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch (InterruptedException success){}
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testFairTakeFromEmpty() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.take();
                        threadShouldThrow();
                    } catch (InterruptedException success){ }                
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testPoll() {
        SynchronousQueue q = new SynchronousQueue();
        assertNull(q.poll());
    }
    public void testTimedPoll0() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            assertNull(q.poll(0, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
            unexpectedException();
        }   
    }
    public void testTimedPoll() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
            unexpectedException();
        }   
    }
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue();
                        assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }   
                }});
        t.start();
        try { 
           Thread.sleep(SHORT_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testTimedPollWithOffer() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch (InterruptedException success) { }                
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  
    public void testFairInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue(true);
                        assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }   
                }});
        t.start();
        try { 
           Thread.sleep(SHORT_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testFairTimedPollWithOffer() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch (InterruptedException success) { }                
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  
    public void testPeek() {
        SynchronousQueue q = new SynchronousQueue();
        assertNull(q.peek());
    }
    public void testElement() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.element();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }
    public void testRemove() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success){
        }   
    }
    public void testRemoveElement() {
        SynchronousQueue q = new SynchronousQueue();
        assertFalse(q.remove(zero));
        assertTrue(q.isEmpty());
    }
    public void testContains() {
        SynchronousQueue q = new SynchronousQueue();
        assertFalse(q.contains(zero));
    }
    public void testClear() {
        SynchronousQueue q = new SynchronousQueue();
        q.clear();
        assertTrue(q.isEmpty());
    }
    public void testContainsAll() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] empty = new Integer[0];
        assertTrue(q.containsAll(Arrays.asList(empty)));
        Integer[] ints = new Integer[1]; ints[0] = zero;
        assertFalse(q.containsAll(Arrays.asList(ints)));
    }
    public void testRetainAll() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] empty = new Integer[0];
        assertFalse(q.retainAll(Arrays.asList(empty)));
        Integer[] ints = new Integer[1]; ints[0] = zero;
        assertFalse(q.retainAll(Arrays.asList(ints)));
    }
    public void testRemoveAll() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] empty = new Integer[0];
        assertFalse(q.removeAll(Arrays.asList(empty)));
        Integer[] ints = new Integer[1]; ints[0] = zero;
        assertFalse(q.containsAll(Arrays.asList(ints)));
    }
    public void testToArray() {
        SynchronousQueue q = new SynchronousQueue();
        Object[] o = q.toArray();
        assertEquals(o.length, 0);
    }
    public void testToArray2() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] ints = new Integer[1];
        assertNull(ints[0]);
    }
    public void testToArray_BadArg() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            Object o[] = q.toArray(null);
            shouldThrow();
        } catch(NullPointerException success){}
    }
    public void testIterator() {
        SynchronousQueue q = new SynchronousQueue();
        Iterator it = q.iterator();
        assertFalse(it.hasNext());
        try {
            Object x = it.next();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }
    public void testIteratorRemove() {
        SynchronousQueue q = new SynchronousQueue();
        Iterator it = q.iterator();
        try {
            it.remove();
            shouldThrow();
        }
        catch (IllegalStateException success) {}
    }
    public void testToString() {
        SynchronousQueue q = new SynchronousQueue();
        String s = q.toString();
        assertNotNull(s);
    }        
    public void testOfferInExecutor() {
        final SynchronousQueue q = new SynchronousQueue();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final Integer one = new Integer(1);
        executor.execute(new Runnable() {
            public void run() {
                threadAssertFalse(q.offer(one));
                try {
                    threadAssertTrue(q.offer(one, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS));
                    threadAssertEquals(0, q.remainingCapacity());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SMALL_DELAY_MS);
                    threadAssertEquals(one, q.take());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        joinPool(executor);
    }
    public void testPollInExecutor() {
        final SynchronousQueue q = new SynchronousQueue();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new Runnable() {
            public void run() {
                threadAssertNull(q.poll());
                try {
                    threadAssertTrue(null != q.poll(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS));
                    threadAssertTrue(q.isEmpty());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SMALL_DELAY_MS);
                    q.put(new Integer(1));
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        joinPool(executor);
    }
    public void testSerialization() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            SynchronousQueue r = (SynchronousQueue)in.readObject();
            assertEquals(q.size(), r.size());
            while (!q.isEmpty()) 
                assertEquals(q.remove(), r.remove());
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    public void testDrainToNull() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(null);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }
    public void testDrainToSelf() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(q);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }
    public void testDrainTo() {
        SynchronousQueue q = new SynchronousQueue();
        ArrayList l = new ArrayList();
        q.drainTo(l);
        assertEquals(q.size(), 0);
        assertEquals(l.size(), 0);
    }
    public void testDrainToWithActivePut() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Integer(1));
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            Thread.sleep(SHORT_DELAY_MS);
            q.drainTo(l);
            assertTrue(l.size() <= 1);
            if (l.size() > 0)
                assertEquals(l.get(0), new Integer(1));
            t.join();
            assertTrue(l.size() <= 1);
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testDrainToNullN() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(null, 0);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }
    public void testDrainToSelfN() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(q, 0);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }
    public void testDrainToN() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(one);
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(two);
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            ArrayList l = new ArrayList();
            Thread.sleep(SHORT_DELAY_MS);
            q.drainTo(l, 1);
            assertTrue(l.size() == 1);
            q.drainTo(l, 1);
            assertTrue(l.size() == 2);
            assertTrue(l.contains(one));
            assertTrue(l.contains(two));
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }
}
