public class EventRecurrenceTest extends TestCase {
    @SmallTest
    public void test0() throws Exception {
        verifyRecurType("FREQ=SECONDLY",
                         EventRecurrence.SECONDLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test1() throws Exception {
        verifyRecurType("FREQ=MINUTELY",
                         EventRecurrence.MINUTELY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test2() throws Exception {
        verifyRecurType("FREQ=HOURLY",
                         EventRecurrence.HOURLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test3() throws Exception {
        verifyRecurType("FREQ=DAILY",
                         EventRecurrence.DAILY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test4() throws Exception {
        verifyRecurType("FREQ=WEEKLY",
                         EventRecurrence.WEEKLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test5() throws Exception {
        verifyRecurType("FREQ=MONTHLY",
                         EventRecurrence.MONTHLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test6() throws Exception {
        verifyRecurType("FREQ=YEARLY",
                         EventRecurrence.YEARLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test7() throws Exception {
        verifyRecurType("FREQ=DAILY;UNTIL=112233T223344Z",
                         EventRecurrence.DAILY,
                     "112233T223344Z",
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test8() throws Exception {
        verifyRecurType("FREQ=DAILY;COUNT=334",
                         EventRecurrence.DAILY,
                     null,
                        334,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test9() throws Exception {
        verifyRecurType("FREQ=DAILY;INTERVAL=5000",
                         EventRecurrence.DAILY,
                     null,
                        0,
                     5000,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @SmallTest
    public void test10() throws Exception {
        verifyRecurType("FREQ=DAILY"
                + ";BYSECOND=0"
                + ";BYMINUTE=1"
                + ";BYHOUR=2"
                + ";BYMONTHDAY=30"
                + ";BYYEARDAY=300"
                + ";BYWEEKNO=53"
                + ";BYMONTH=12"
                + ";BYSETPOS=-15"
                + ";WKST=SU",
                         EventRecurrence.DAILY,
                     null,
                        0,
                     0,
                   new int[]{0},
                   new int[]{1},
                     new int[]{2},
                      null,
                   null,
                 new int[]{30},
                  new int[]{300},
                   new int[]{53},
                    new int[]{12},
                   new int[]{-15},
                         EventRecurrence.SU
        );
    }
    @SmallTest
    public void test11() throws Exception {
        verifyRecurType("FREQ=DAILY"
                + ";BYSECOND=0,30,59"
                + ";BYMINUTE=0,41,59"
                + ";BYHOUR=0,4,23"
                + ";BYMONTHDAY=-31,-1,1,31"
                + ";BYYEARDAY=-366,-1,1,366"
                + ";BYWEEKNO=-53,-1,1,53"
                + ";BYMONTH=1,12"
                + ";BYSETPOS=1,2,3,4,500,10000"
                + ";WKST=SU",
                         EventRecurrence.DAILY,
                     null,
                        0,
                     0,
                   new int[]{0, 30, 59},
                   new int[]{0, 41, 59},
                     new int[]{0, 4, 23},
                      null,
                   null,
                 new int[]{-31, -1, 1, 31},
                  new int[]{-366, -1, 1, 366},
                   new int[]{-53, -1, 1, 53},
                    new int[]{1, 12},
                   new int[]{1, 2, 3, 4, 500, 10000},
                         EventRecurrence.SU
        );
    }
    private static class Check {
        Check(String k, int... v) {
            key = k;
            values = v;
        }
        String key;
        int[] values;
    }
    @SmallTest
    public void test12() throws Exception {
        Check[] checks = new Check[]{
                new Check("BYSECOND", -100, -1, 60, 100),
                new Check("BYMINUTE", -100, -1, 60, 100),
                new Check("BYHOUR", -100, -1, 24, 100),
                new Check("BYMONTHDAY", -100, -32, 0, 32, 100),
                new Check("BYYEARDAY", -400, -367, 0, 367, 400),
                new Check("BYWEEKNO", -100, -54, 0, 54, 100),
                new Check("BYMONTH", -100, -5, 0, 13, 100)
        };
        for (Check ck : checks) {
            for (int n : ck.values) {
                String recur = "FREQ=DAILY;" + ck.key + "=" + n;
                try {
                    EventRecurrence er = new EventRecurrence();
                    er.parse(recur);
                    fail("Negative verifyRecurType failed. "
                            + " parse failed to throw an exception for '"
                            + recur + "'");
                } catch (EventRecurrence.InvalidFormatException e) {
                }
            }
        }
    }
    @SmallTest
    public void test13() throws Exception {
        verifyRecurType("FREQ=DAILY;BYDAY=1SU,-2MO,+33TU,WE,TH,FR,SA",
                         EventRecurrence.DAILY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      new int[]{
                EventRecurrence.SU,
                EventRecurrence.MO,
                EventRecurrence.TU,
                EventRecurrence.WE,
                EventRecurrence.TH,
                EventRecurrence.FR,
                EventRecurrence.SA
        },
                   new int[]{1, -2, 33, 0, 0, 0, 0},
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    @Suppress
    public void test14() throws Exception {
        verifyRecurType("FREQ=WEEKLY;WKST=MO;UNTIL=20100129T130000Z;INTERVAL=1;BYDAY=MO,TU,WE,",
                         EventRecurrence.WEEKLY,
                     "20100129T130000Z",
                        0,
                     1,
                   null,
                   null,
                     null,
                      new int[] {
                        EventRecurrence.MO,
                        EventRecurrence.TU,
                        EventRecurrence.WE,
                },
                   new int[]{0, 0, 0},
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    public void test15() throws Exception {
        verifyRecurType("FREQ=WEEKLY;WKST=MO;UNTIL=20100129T130000Z;INTERVAL=1;"
                + "BYDAY=MO,TU,WE,TH,FR,SA,SU",
                         EventRecurrence.WEEKLY,
                     "20100129T130000Z",
                        0,
                     1,
                   null,
                   null,
                     null,
                      new int[] {
                        EventRecurrence.MO,
                        EventRecurrence.TU,
                        EventRecurrence.WE,
                        EventRecurrence.TH,
                        EventRecurrence.FR,
                        EventRecurrence.SA,
                        EventRecurrence.SU
                },
                   new int[]{0, 0, 0, 0, 0, 0, 0},
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    public void test16() throws Exception {
        verifyRecurType("FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-1",
                         EventRecurrence.MONTHLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      new int[] {
                        EventRecurrence.MO,
                        EventRecurrence.TU,
                        EventRecurrence.WE,
                        EventRecurrence.TH,
                        EventRecurrence.FR
                },
                   new int[] {0, 0, 0, 0, 0},
                 null,
                  null,
                   null,
                    null,
                   new int[] { -1 },
                         EventRecurrence.MO
        );
    }
    public void test17() throws Exception {
        verifyRecurType("FREQ=DAILY;COUNT=10;INTERVAL=2",
                         EventRecurrence.DAILY,
                     null,
                        10,
                     2,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    public void test18() throws Exception {
        verifyRecurType("FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10",
                         EventRecurrence.YEARLY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      new int[] {
                        EventRecurrence.SU
                },
                   new int[] { -1 },
                 null,
                  null,
                   null,
                    new int[] { 10 },
                   null,
                         EventRecurrence.MO
        );
    }
    public void fakeTestXX() throws Exception {
        verifyRecurType("FREQ=DAILY;",
                         EventRecurrence.DAILY,
                     null,
                        0,
                     0,
                   null,
                   null,
                     null,
                      null,
                   null,
                 null,
                  null,
                   null,
                    null,
                   null,
                         EventRecurrence.MO
        );
    }
    private static void cmp(int vlen, int[] v, int[] correct, String name) {
        if ((correct == null && v != null)
                || (correct != null && v == null)) {
            throw new RuntimeException("One is null, one isn't for " + name
                    + ": correct=" + Arrays.toString(correct)
                    + " actual=" + Arrays.toString(v));
        }
        if ((correct == null && vlen != 0)
                || (vlen != (correct == null ? 0 : correct.length))) {
            throw new RuntimeException("Reported length mismatch for " + name
                    + ": correct=" + ((correct == null) ? "null" : correct.length)
                    + " actual=" + vlen);
        }
        if (correct == null) {
            return;
        }
        if (v.length < correct.length) {
            throw new RuntimeException("Array length mismatch for " + name
                    + ": correct=" + Arrays.toString(correct)
                    + " actual=" + Arrays.toString(v));
        }
        for (int i = 0; i < correct.length; i++) {
            if (v[i] != correct[i]) {
                throw new RuntimeException("Array value mismatch for " + name
                        + ": correct=" + Arrays.toString(correct)
                        + " actual=" + Arrays.toString(v));
            }
        }
    }
    private static boolean eq(String a, String b) {
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        } else {
            return a == b || a.equals(b);
        }
    }
    private static void verifyRecurType(String recur,
            int freq, String until, int count, int interval,
            int[] bysecond, int[] byminute, int[] byhour,
            int[] byday, int[] bydayNum, int[] bymonthday,
            int[] byyearday, int[] byweekno, int[] bymonth,
            int[] bysetpos, int wkst) {
        EventRecurrence eventRecurrence = new EventRecurrence();
        eventRecurrence.parse(recur);
        if (eventRecurrence.freq != freq
                || !eq(eventRecurrence.until, until)
                || eventRecurrence.count != count
                || eventRecurrence.interval != interval
                || eventRecurrence.wkst != wkst) {
            System.out.println("Error... got:");
            print(eventRecurrence);
            System.out.println("expected:");
            System.out.println("{");
            System.out.println("    freq=" + freq);
            System.out.println("    until=" + until);
            System.out.println("    count=" + count);
            System.out.println("    interval=" + interval);
            System.out.println("    wkst=" + wkst);
            System.out.println("    bysecond=" + Arrays.toString(bysecond));
            System.out.println("    byminute=" + Arrays.toString(byminute));
            System.out.println("    byhour=" + Arrays.toString(byhour));
            System.out.println("    byday=" + Arrays.toString(byday));
            System.out.println("    bydayNum=" + Arrays.toString(bydayNum));
            System.out.println("    bymonthday=" + Arrays.toString(bymonthday));
            System.out.println("    byyearday=" + Arrays.toString(byyearday));
            System.out.println("    byweekno=" + Arrays.toString(byweekno));
            System.out.println("    bymonth=" + Arrays.toString(bymonth));
            System.out.println("    bysetpos=" + Arrays.toString(bysetpos));
            System.out.println("}");
            throw new RuntimeException("Mismatch in fields");
        }
        cmp(eventRecurrence.bysecondCount, eventRecurrence.bysecond, bysecond, "bysecond");
        cmp(eventRecurrence.byminuteCount, eventRecurrence.byminute, byminute, "byminute");
        cmp(eventRecurrence.byhourCount, eventRecurrence.byhour, byhour, "byhour");
        cmp(eventRecurrence.bydayCount, eventRecurrence.byday, byday, "byday");
        cmp(eventRecurrence.bydayCount, eventRecurrence.bydayNum, bydayNum, "bydayNum");
        cmp(eventRecurrence.bymonthdayCount, eventRecurrence.bymonthday, bymonthday, "bymonthday");
        cmp(eventRecurrence.byyeardayCount, eventRecurrence.byyearday, byyearday, "byyearday");
        cmp(eventRecurrence.byweeknoCount, eventRecurrence.byweekno, byweekno, "byweekno");
        cmp(eventRecurrence.bymonthCount, eventRecurrence.bymonth, bymonth, "bymonth");
        cmp(eventRecurrence.bysetposCount, eventRecurrence.bysetpos, bysetpos, "bysetpos");
    }
    private static void print(EventRecurrence er) {
        System.out.println("{");
        System.out.println("    freq=" + er.freq);
        System.out.println("    until=" + er.until);
        System.out.println("    count=" + er.count);
        System.out.println("    interval=" + er.interval);
        System.out.println("    wkst=" + er.wkst);
        System.out.println("    bysecond=" + Arrays.toString(er.bysecond));
        System.out.println("    bysecondCount=" + er.bysecondCount);
        System.out.println("    byminute=" + Arrays.toString(er.byminute));
        System.out.println("    byminuteCount=" + er.byminuteCount);
        System.out.println("    byhour=" + Arrays.toString(er.byhour));
        System.out.println("    byhourCount=" + er.byhourCount);
        System.out.println("    byday=" + Arrays.toString(er.byday));
        System.out.println("    bydayNum=" + Arrays.toString(er.bydayNum));
        System.out.println("    bydayCount=" + er.bydayCount);
        System.out.println("    bymonthday=" + Arrays.toString(er.bymonthday));
        System.out.println("    bymonthdayCount=" + er.bymonthdayCount);
        System.out.println("    byyearday=" + Arrays.toString(er.byyearday));
        System.out.println("    byyeardayCount=" + er.byyeardayCount);
        System.out.println("    byweekno=" + Arrays.toString(er.byweekno));
        System.out.println("    byweeknoCount=" + er.byweeknoCount);
        System.out.println("    bymonth=" + Arrays.toString(er.bymonth));
        System.out.println("    bymonthCount=" + er.bymonthCount);
        System.out.println("    bysetpos=" + Arrays.toString(er.bysetpos));
        System.out.println("    bysetposCount=" + er.bysetposCount);
        System.out.println("}");
    }
}
