class Stopwatch {
    long last = System.currentTimeMillis();
    void reset(String label) {
        long now = System.currentTimeMillis();
        Log.info(label + ": " + (now - last) + "ms");
        last = now;
    }
}