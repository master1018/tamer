public class ZeitgeberTest {
    private ZeitgeberSimulation state = new ZeitgeberSimulation(1);
    @Test
    public void testZeitraffer() throws InterruptedException {
        double[] zeitRaffer = { 0.1, 0.5, 1, 3.333, 5, 20, 100, 200, 1000, 10000 };
        long[] waitTime = { 0, 1, 2, 10, 11, 20, 50, 60, 100, 1000 };
        for (double zeitR : zeitRaffer) {
            state.setGeschwindigkeit(zeitR);
            for (long waitT : waitTime) {
                testZeitvergeht(waitT, zeitR);
            }
        }
    }
    @Test
    public void testZeitvergeht() {
        long[] waitTime = { 0, 1, 2, 5, 6, 11, 20, 50, 60, 100, 1000 };
        for (int i = 0; i < waitTime.length; ++i) {
            testZeitvergeht(waitTime[i], 1.0);
        }
    }
    public void testZeitvergeht(long waitTime, double zeitR) {
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            Protokoll.getInstanc().exception(e);
        }
        long echtZeitStart = System.nanoTime();
        Date start = state.getCurrentDate();
        state.sleep(waitTime);
        Date ende = state.getCurrentDate();
        long echtZeitEnde = System.nanoTime();
        long differenzMs = Math.abs(waitTime - (ende.getTime() - start.getTime()));
        double grenzwert = 200;
        double soll = waitTime;
        double ist = ende.getTime() - start.getTime();
        String msg = String.format("Abweichtung zu groß, (soll%.0f;ist%.0f;diff%d) grenzwert(%f), echtzeit(diff%f), zeitR(%f)", soll, ist, differenzMs, grenzwert, (echtZeitEnde - echtZeitStart) / 10e6, zeitR);
        assertTrue(msg, differenzMs <= Math.abs(grenzwert));
    }
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testZeitvergehtNegativ() {
        long[] waitTime = { -1, -100, -1000 };
        for (int i = 0; i < waitTime.length; ++i) {
            testZeitvergeht(waitTime[i], 1.0);
        }
    }
}
