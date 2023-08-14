public class SetMaxPriority {
    public static void main(String args[]) throws Exception {
        ThreadGroup tg = new ThreadGroup("foo");
        ThreadGroup ptg = tg.getParent();
        int currentMaxPriority = tg.getMaxPriority();
        int halfMaxPriority = ptg.getMaxPriority() / 2;
        if (halfMaxPriority - Thread.MIN_PRIORITY < 2) {
            throw new RuntimeException("SetMaxPriority test no longer valid: starting parent max priority too close to Thread.MIN_PRIORITY");
        }
        tg.setMaxPriority(halfMaxPriority - 2);
        currentMaxPriority = tg.getMaxPriority();
        if (currentMaxPriority != halfMaxPriority - 2) {
            throw new RuntimeException("SetMaxPriority failed: max priority not changed");
        }
        tg.setMaxPriority(currentMaxPriority + 1);
        int newMaxPriority = tg.getMaxPriority();
        if (newMaxPriority != currentMaxPriority + 1) {
            throw new RuntimeException("SetMaxPriority failed: defect 6497629 present");
        }
        for (int badPriority : new int[] {Thread.MIN_PRIORITY - 1,
                                          Thread.MAX_PRIORITY + 1}) {
            int oldPriority = tg.getMaxPriority();
            tg.setMaxPriority(badPriority);
            if (oldPriority != tg.getMaxPriority())
                throw new RuntimeException(
                    "setMaxPriority bad arg not ignored as specified");
        }
        System.out.println("SetMaxPriority passed");
    }
}
