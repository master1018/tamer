public class DelayQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
	return new TestSuite(DelayQueueTest.class);
    }
    private static final int NOCAP = Integer.MAX_VALUE;
    static class PDelay implements Delayed {
        int pseudodelay;
        PDelay(int i) { pseudodelay = Integer.MIN_VALUE + i; }
        public int compareTo(PDelay y) {
            int i = pseudodelay;
            int j = ((PDelay)y).pseudodelay;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }
        public int compareTo(Delayed y) {
            int i = pseudodelay;
            int j = ((PDelay)y).pseudodelay;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }
        public boolean equals(Object other) {
            return ((PDelay)other).pseudodelay == pseudodelay;
        }
        public boolean equals(PDelay other) {
            return ((PDelay)other).pseudodelay == pseudodelay;
        }
        public long getDelay(TimeUnit ignore) {
            return pseudodelay;
        }
        public int intValue() {
            return pseudodelay;
        }
        public String toString() {
            return String.valueOf(pseudodelay);
        }
    }
    static class NanoDelay implements Delayed {
        long trigger;
        NanoDelay(long i) {
            trigger = System.nanoTime() + i;
        }
        public int compareTo(NanoDelay y) {
            long i = trigger;
            long j = ((NanoDelay)y).trigger;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }
        public int compareTo(Delayed y) {
            long i = trigger;
            long j = ((NanoDelay)y).trigger;
            if (i < j) return -1;
            if (i > j) return 1;
            return 0;
        }
        public boolean equals(Object other) {
            return ((NanoDelay)other).trigger == trigger;
        }
        public boolean equals(NanoDelay other) {
            return ((NanoDelay)other).trigger == trigger;
        }
        public long getDelay(TimeUnit unit) {
            long n = trigger - System.nanoTime();
            return unit.convert(n, TimeUnit.NANOSECONDS);
        }
        public long getTriggerTime() {
            return trigger;
        }
        public String toString() {
            return String.valueOf(trigger);
        }
    }
    private DelayQueue populatedQueue(int n) {
        DelayQueue q = new DelayQueue();
        assertTrue(q.isEmpty());
	for(int i = n-1; i >= 0; i-=2)
	    assertTrue(q.offer(new PDelay(i)));
	for(int i = (n & 1); i < n; i+=2)
	    assertTrue(q.offer(new PDelay(i)));
        assertFalse(q.isEmpty());
        assertEquals(NOCAP, q.remainingCapacity());
	assertEquals(n, q.size());
        return q;
    }
    public void testConstructor1() {
        assertEquals(NOCAP, new DelayQueue().remainingCapacity());
    }
    public void testConstructor3() {
        try {
            DelayQueue q = new DelayQueue(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testConstructor4() {
        try {
            PDelay[] ints = new PDelay[SIZE];
            DelayQueue q = new DelayQueue(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testConstructor5() {
        try {
            PDelay[] ints = new PDelay[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new PDelay(i);
            DelayQueue q = new DelayQueue(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testConstructor6() {
        try {
            PDelay[] ints = new PDelay[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new PDelay(i);
            DelayQueue q = new DelayQueue(Arrays.asList(ints));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }
    public void testEmpty() {
        DelayQueue q = new DelayQueue();
        assertTrue(q.isEmpty());
        assertEquals(NOCAP, q.remainingCapacity());
        q.add(new PDelay(1));
        assertFalse(q.isEmpty());
        q.add(new PDelay(2));
        q.remove();
        q.remove();
        assertTrue(q.isEmpty());
    }
    public void testRemainingCapacity() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(NOCAP, q.remainingCapacity());
            assertEquals(SIZE-i, q.size());
            q.remove();
        }
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(NOCAP, q.remainingCapacity());
            assertEquals(i, q.size());
            q.add(new PDelay(i));
        }
    }
    public void testOfferNull() {
	try {
            DelayQueue q = new DelayQueue();
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) { }
    }
    public void testAddNull() {
	try {
            DelayQueue q = new DelayQueue();
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) { }
    }
    public void testOffer() {
        DelayQueue q = new DelayQueue();
        assertTrue(q.offer(new PDelay(0)));
        assertTrue(q.offer(new PDelay(1)));
    }
    public void testAdd() {
        DelayQueue q = new DelayQueue();
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.size());
            assertTrue(q.add(new PDelay(i)));
        }
    }
    public void testAddAll1() {
        try {
            DelayQueue q = new DelayQueue();
            q.addAll(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAllSelf() {
        try {
            DelayQueue q = populatedQueue(SIZE);
            q.addAll(q);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }
    public void testAddAll2() {
        try {
            DelayQueue q = new DelayQueue();
            PDelay[] ints = new PDelay[SIZE];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll3() {
        try {
            DelayQueue q = new DelayQueue();
            PDelay[] ints = new PDelay[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new PDelay(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll5() {
        try {
            PDelay[] empty = new PDelay[0];
            PDelay[] ints = new PDelay[SIZE];
            for (int i = SIZE-1; i >= 0; --i)
                ints[i] = new PDelay(i);
            DelayQueue q = new DelayQueue();
            assertFalse(q.addAll(Arrays.asList(empty)));
            assertTrue(q.addAll(Arrays.asList(ints)));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }
     public void testPutNull() {
	try {
            DelayQueue q = new DelayQueue();
            q.put(null);
            shouldThrow();
        }
        catch (NullPointerException success){
	}
     }
     public void testPut() {
         try {
             DelayQueue q = new DelayQueue();
             for (int i = 0; i < SIZE; ++i) {
                 PDelay I = new PDelay(i);
                 q.put(I);
                 assertTrue(q.contains(I));
             }
             assertEquals(SIZE, q.size());
         }
         finally {
        }
    }
    public void testPutWithTake() {
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new PDelay(0));
                        ++added;
                        q.put(new PDelay(0));
                        ++added;
                        q.put(new PDelay(0));
                        ++added;
                        q.put(new PDelay(0));
                        ++added;
                        threadAssertTrue(added == 4);
                    } finally {
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testTimedOffer() {
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new PDelay(0));
                        q.put(new PDelay(0));
                        threadAssertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(q.offer(new PDelay(0), LONG_DELAY_MS, TimeUnit.MILLISECONDS));
                    } finally { }
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
    public void testTake() {
	try {
            DelayQueue q = populatedQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(new PDelay(i), ((PDelay)q.take()));
            }
        } catch (InterruptedException e){
	    unexpectedException();
	}
    }
    public void testTakeFromEmpty() {
        final DelayQueue q = new DelayQueue();
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
    public void testBlockingTake() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        DelayQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(new PDelay(i), ((PDelay)q.take()));
                        }
                        q.take();
                        threadShouldThrow();
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
    public void testPoll() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(new PDelay(i), ((PDelay)q.poll()));
        }
	assertNull(q.poll());
    }
    public void testTimedPoll0() {
        try {
            DelayQueue q = populatedQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(new PDelay(i), ((PDelay)q.poll(0, TimeUnit.MILLISECONDS)));
            }
            assertNull(q.poll(0, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
	    unexpectedException();
	}
    }
    public void testTimedPoll() {
        try {
            DelayQueue q = populatedQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(new PDelay(i), ((PDelay)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)));
            }
            assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
	    unexpectedException();
	}
    }
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        DelayQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(new PDelay(i), ((PDelay)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)));
                        }
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
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
        final DelayQueue q = new DelayQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadFail("Should block");
                    } catch (InterruptedException success) { }
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(new PDelay(0), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testPeek() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(new PDelay(i), ((PDelay)q.peek()));
            q.poll();
            if (q.isEmpty())
                assertNull(q.peek());
            else
                assertTrue(i != ((PDelay)q.peek()).intValue());
        }
	assertNull(q.peek());
    }
    public void testElement() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(new PDelay(i), ((PDelay)q.element()));
            q.poll();
        }
        try {
            q.element();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }
    public void testRemove() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(new PDelay(i), ((PDelay)q.remove()));
        }
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success){
	}
    }
    public void testRemoveElement() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 1; i < SIZE; i+=2) {
            assertTrue(q.remove(new PDelay(i)));
        }
        for (int i = 0; i < SIZE; i+=2) {
            assertTrue(q.remove(new PDelay(i)));
            assertFalse(q.remove(new PDelay(i+1)));
        }
        assertTrue(q.isEmpty());
    }
    public void testContains() {
        DelayQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.contains(new PDelay(i)));
            q.poll();
            assertFalse(q.contains(new PDelay(i)));
        }
    }
    public void testClear() {
        DelayQueue q = populatedQueue(SIZE);
        q.clear();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        assertEquals(NOCAP, q.remainingCapacity());
        PDelay x = new PDelay(1);
        q.add(x);
        assertFalse(q.isEmpty());
        assertTrue(q.contains(x));
        q.clear();
        assertTrue(q.isEmpty());
    }
    public void testContainsAll() {
        DelayQueue q = populatedQueue(SIZE);
        DelayQueue p = new DelayQueue();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.containsAll(p));
            assertFalse(p.containsAll(q));
            p.add(new PDelay(i));
        }
        assertTrue(p.containsAll(q));
    }
    public void testRetainAll() {
        DelayQueue q = populatedQueue(SIZE);
        DelayQueue p = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            boolean changed = q.retainAll(p);
            if (i == 0)
                assertFalse(changed);
            else
                assertTrue(changed);
            assertTrue(q.containsAll(p));
            assertEquals(SIZE-i, q.size());
            p.remove();
        }
    }
    public void testRemoveAll() {
        for (int i = 1; i < SIZE; ++i) {
            DelayQueue q = populatedQueue(SIZE);
            DelayQueue p = populatedQueue(i);
            assertTrue(q.removeAll(p));
            assertEquals(SIZE-i, q.size());
            for (int j = 0; j < i; ++j) {
                PDelay I = (PDelay)(p.remove());
                assertFalse(q.contains(I));
            }
        }
    }
    public void testToArray() {
        DelayQueue q = populatedQueue(SIZE);
	Object[] o = q.toArray();
        Arrays.sort(o);
	try {
	for(int i = 0; i < o.length; i++)
	    assertEquals(o[i], q.take());
	} catch (InterruptedException e){
	    unexpectedException();
	}
    }
    public void testToArray2() {
        DelayQueue q = populatedQueue(SIZE);
	PDelay[] ints = new PDelay[SIZE];
	ints = (PDelay[])q.toArray(ints);
        Arrays.sort(ints);
	try {
	    for(int i = 0; i < ints.length; i++)
		assertEquals(ints[i], q.take());
	} catch (InterruptedException e){
	    unexpectedException();
	}
    }
    public void testToArray_BadArg() {
	try {
            DelayQueue q = populatedQueue(SIZE);
	    Object o[] = q.toArray(null);
	    shouldThrow();
	} catch(NullPointerException success){}
    }
    public void testToArray1_BadArg() {
	try {
            DelayQueue q = populatedQueue(SIZE);
	    Object o[] = q.toArray(new String[10] );
	    shouldThrow();
	} catch(ArrayStoreException  success){}
    }
    public void testIterator() {
        DelayQueue q = populatedQueue(SIZE);
        int i = 0;
	Iterator it = q.iterator();
        while(it.hasNext()) {
            assertTrue(q.contains(it.next()));
            ++i;
        }
        assertEquals(i, SIZE);
    }
    public void testIteratorRemove () {
        final DelayQueue q = new DelayQueue();
        q.add(new PDelay(2));
        q.add(new PDelay(1));
        q.add(new PDelay(3));
        Iterator it = q.iterator();
        it.next();
        it.remove();
        it = q.iterator();
        assertEquals(it.next(), new PDelay(2));
        assertEquals(it.next(), new PDelay(3));
        assertFalse(it.hasNext());
    }
    public void testToString() {
        DelayQueue q = populatedQueue(SIZE);
        String s = q.toString();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(s.indexOf(String.valueOf(Integer.MIN_VALUE+i)) >= 0);
        }
    }
    public void testPollInExecutor() {
        final DelayQueue q = new DelayQueue();
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
                    Thread.sleep(SHORT_DELAY_MS);
                    q.put(new PDelay(1));
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        joinPool(executor);
    }
    public void testDelay() {
        DelayQueue q = new DelayQueue();
        NanoDelay[] elements = new NanoDelay[SIZE];
        for (int i = 0; i < SIZE; ++i) {
            elements[i] = new NanoDelay(1000000000L + 1000000L * (SIZE - i));
        }
        for (int i = 0; i < SIZE; ++i) {
            q.add(elements[i]);
        }
        try {
            long last = 0;
            for (int i = 0; i < SIZE; ++i) {
                NanoDelay e = (NanoDelay)(q.take());
                long tt = e.getTriggerTime();
                assertTrue(tt <= System.nanoTime());
                if (i != 0)
                    assertTrue(tt >= last);
                last = tt;
            }
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testPeekDelayed() {
        DelayQueue q = new DelayQueue();
        q.add(new NanoDelay(Long.MAX_VALUE));
        assert(q.peek() != null);
    }
    public void testPollDelayed() {
        DelayQueue q = new DelayQueue();
        q.add(new NanoDelay(Long.MAX_VALUE));
        assertNull(q.poll());
    }
    public void testTimedPollDelayed() {
        DelayQueue q = new DelayQueue();
        q.add(new NanoDelay(LONG_DELAY_MS * 1000000L));
        try {
            assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testDrainToNull() {
        DelayQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(null);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }
    public void testDrainToSelf() {
        DelayQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(q);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }
    public void testDrainTo() {
        DelayQueue q = new DelayQueue();
        PDelay[] elems = new PDelay[SIZE];
        for (int i = 0; i < SIZE; ++i) {
            elems[i] = new PDelay(i);
            q.add(elems[i]);
        }
        ArrayList l = new ArrayList();
        q.drainTo(l);
        assertEquals(q.size(), 0);
        for (int i = 0; i < SIZE; ++i)
            assertEquals(l.get(i), elems[i]);
        q.add(elems[0]);
        q.add(elems[1]);
        assertFalse(q.isEmpty());
        assertTrue(q.contains(elems[0]));
        assertTrue(q.contains(elems[1]));
        l.clear();
        q.drainTo(l);
        assertEquals(q.size(), 0);
        assertEquals(l.size(), 2);
        for (int i = 0; i < 2; ++i)
            assertEquals(l.get(i), elems[i]);
    }
    public void testDrainToWithActivePut() {
        final DelayQueue q = populatedQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    q.put(new PDelay(SIZE+1));
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            q.drainTo(l);
            assertTrue(l.size() >= SIZE);
            t.join();
            assertTrue(q.size() + l.size() >= SIZE);
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testDrainToNullN() {
        DelayQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(null, 0);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }
    public void testDrainToSelfN() {
        DelayQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(q, 0);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }
    public void testDrainToN() {
        for (int i = 0; i < SIZE + 2; ++i) {
            DelayQueue q = populatedQueue(SIZE);
            ArrayList l = new ArrayList();
            q.drainTo(l, i);
            int k = (i < SIZE)? i : SIZE;
            assertEquals(q.size(), SIZE-k);
            assertEquals(l.size(), k);
        }
    }
}
