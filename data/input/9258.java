public class Stress {
    public static void main(String[] args) throws Throwable {
        final DelayQueue<Delayed> q = new DelayQueue<Delayed>();
        final long t0 = System.nanoTime();
        for (long i = 0; i < 1000; i++) {
            final long expiry = t0 + i*10L*1000L*1000L;
            q.add(new Delayed() {
                    public long getDelay(TimeUnit unit) {
                        return unit.convert(expiry - System.nanoTime(),
                                            NANOSECONDS);
                    }
                    public int compareTo(Delayed x) {
                        long d = getDelay(NANOSECONDS)
                            - x.getDelay(NANOSECONDS);
                        return d < 0 ? -1 : d > 0 ? 1 : 0; }});
        }
        for (int i = 0; i < 300; i++)
            new Thread() { public void run() {
                try {
                    while (!q.isEmpty())
                        q.poll(10L, TimeUnit.SECONDS);
                } catch (Throwable t) { t.printStackTrace(); }
            }}.start();
    }
}
