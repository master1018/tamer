public class Lazy {
    volatile int ii;
    volatile long ll;
    volatile Boolean bb;
    static final Lazy z = new Lazy();
    public static void main(String[] args) throws Exception {
        final AtomicBoolean b = new AtomicBoolean();
        final AtomicInteger i = new AtomicInteger();
        final AtomicLong    l = new AtomicLong();
        final AtomicReference<Long> r = new AtomicReference<Long>();
        final AtomicIntegerArray ia = new AtomicIntegerArray(1);
        final AtomicLongArray    la = new AtomicLongArray(1);
        final AtomicReferenceArray<Long> ra = new AtomicReferenceArray<Long>(1);
        final AtomicIntegerFieldUpdater<Lazy> iu =
            AtomicIntegerFieldUpdater.newUpdater(Lazy.class, "ii");
        final AtomicLongFieldUpdater<Lazy> lu =
            AtomicLongFieldUpdater.newUpdater(Lazy.class, "ll");
        final AtomicReferenceFieldUpdater<Lazy,Boolean> ru =
            AtomicReferenceFieldUpdater.newUpdater(Lazy.class,
                                                   Boolean.class, "bb");
        Thread[] threads = {
            new Thread() { public void run() { b.lazySet(true);    }},
            new Thread() { public void run() { i.lazySet(2);       }},
            new Thread() { public void run() { l.lazySet(3L);      }},
            new Thread() { public void run() { r.lazySet(9L);      }},
            new Thread() { public void run() { ia.lazySet(0,4);    }},
            new Thread() { public void run() { la.lazySet(0,5L);   }},
            new Thread() { public void run() { ra.lazySet(0,6L);   }},
            new Thread() { public void run() { iu.lazySet(z,7);    }},
            new Thread() { public void run() { lu.lazySet(z,8L);   }},
            new Thread() { public void run() { ru.lazySet(z,true); }}};
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        if (! (b.get()   == true &&
               i.get()   == 2    &&
               l.get()   == 3L   &&
               r.get()   == 9L   &&
               ia.get(0) == 4    &&
               la.get(0) == 5L   &&
               ra.get(0) == 6L   &&
               z.ii      == 7    &&
               z.ll      == 8L   &&
               z.bb      == true))
            throw new Exception("lazySet failed");
    }
}
