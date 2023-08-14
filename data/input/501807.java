public class HierarchicalStateMachineTest extends TestCase {
    private static final int TEST_CMD_1 = 1;
    private static final int TEST_CMD_2 = 2;
    private static final int TEST_CMD_3 = 3;
    private static final int TEST_CMD_4 = 4;
    private static final int TEST_CMD_5 = 5;
    private static final int TEST_CMD_6 = 6;
    private static final boolean DBG = true;
    private static final boolean WAIT_FOR_DEBUGGER = true;
    private static final String TAG = "HierarchicalStateMachineTest";
    class StateMachineQuitTest extends HierarchicalStateMachine {
        private int mQuitCount = 0;
        StateMachineQuitTest(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            setInitialState(mS1);
        }
        class S1 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (isQuit(message)) {
                    mQuitCount += 1;
                    if (mQuitCount > 2) {
                        return false;
                    } else {
                        return true;
                    }
                } else  {
                    return true;
                }
            }
        }
        @Override
        protected void quitting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachineQuitTest mThisSm;
        private S1 mS1 = new S1();
    }
    @SmallTest
    public void testStateMachineQuitTest() throws Exception {
        StateMachineQuitTest smQuitTest = new StateMachineQuitTest("smQuitTest");
        smQuitTest.start();
        if (smQuitTest.isDbg()) Log.d(TAG, "testStateMachineQuitTest E");
        synchronized (smQuitTest) {
            for (int i = 1; i <= 6; i++) {
                smQuitTest.sendMessage(i);
            }
            smQuitTest.quit();
            smQuitTest.quit();
            smQuitTest.quit();
            try {
                smQuitTest.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachineQuitTest: exception while waiting " + e.getMessage());
            }
        }
        assertTrue(smQuitTest.getProcessedMessagesCount() == 9);
        ProcessedMessages.Info pmi;
        pmi = smQuitTest.getProcessedMessage(6);
        assertEquals(HierarchicalStateMachine.HSM_QUIT_CMD, pmi.getWhat());
        assertEquals(smQuitTest.mS1, pmi.getState());
        assertEquals(smQuitTest.mS1, pmi.getOriginalState());
        pmi = smQuitTest.getProcessedMessage(7);
        assertEquals(HierarchicalStateMachine.HSM_QUIT_CMD, pmi.getWhat());
        assertEquals(smQuitTest.mS1, pmi.getState());
        assertEquals(smQuitTest.mS1, pmi.getOriginalState());
        pmi = smQuitTest.getProcessedMessage(8);
        assertEquals(HierarchicalStateMachine.HSM_QUIT_CMD, pmi.getWhat());
        assertEquals(null, pmi.getState());
        assertEquals(null, pmi.getOriginalState());
        if (smQuitTest.isDbg()) Log.d(TAG, "testStateMachineQuitTest X");
    }
    class StateMachineEnterExitTransitionToTest extends HierarchicalStateMachine {
        StateMachineEnterExitTransitionToTest(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            addState(mS2);
            addState(mS3);
            addState(mS4);
            setInitialState(mS1);
        }
        class S1 extends HierarchicalState {
            @Override protected void enter() {
                mS1EnterCount += 1;
                transitionTo(mS2);
                Log.d(TAG, "S1.enter");
            }
            @Override protected void exit() {
                mS1ExitCount += 1;
                Log.d(TAG, "S1.exit");
            }
        }
        class S2 extends HierarchicalState {
            @Override protected void enter() {
                mS2EnterCount += 1;
                Log.d(TAG, "S2.enter");
            }
            @Override protected void exit() {
                mS2ExitCount += 1;
                transitionTo(mS4);
                Log.d(TAG, "S2.exit");
            }
            @Override protected boolean processMessage(Message message) {
                transitionTo(mS3);
                Log.d(TAG, "S2.processMessage");
                return true;
            }
        }
        class S3 extends HierarchicalState {
            @Override protected void enter() {
                transitionToHaltingState();
                mS3EnterCount += 1;
                Log.d(TAG, "S3.enter");
            }
            @Override protected void exit() {
                mS3ExitCount += 1;
                Log.d(TAG, "S3.exit");
            }
        }
        class S4 extends HierarchicalState {
            @Override protected void enter() {
                transitionToHaltingState();
                mS4EnterCount += 1;
                Log.d(TAG, "S4.enter");
            }
            @Override protected void exit() {
                mS4ExitCount += 1;
                Log.d(TAG, "S4.exit");
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachineEnterExitTransitionToTest mThisSm;
        private S1 mS1 = new S1();
        private S2 mS2 = new S2();
        private S3 mS3 = new S3();
        private S4 mS4 = new S4();
        private int mS1EnterCount = 0;
        private int mS1ExitCount = 0;
        private int mS2EnterCount = 0;
        private int mS2ExitCount = 0;
        private int mS3EnterCount = 0;
        private int mS3ExitCount = 0;
        private int mS4EnterCount = 0;
        private int mS4ExitCount = 0;
    }
    @SmallTest
    public void testStateMachineEnterExitTransitionToTest() throws Exception {
        StateMachineEnterExitTransitionToTest smEnterExitTranstionToTest =
            new StateMachineEnterExitTransitionToTest("smEnterExitTranstionToTest");
        smEnterExitTranstionToTest.start();
        if (smEnterExitTranstionToTest.isDbg()) {
            Log.d(TAG, "testStateMachineEnterExitTransitionToTest E");
        }
        synchronized (smEnterExitTranstionToTest) {
            smEnterExitTranstionToTest.sendMessage(1);
            try {
                smEnterExitTranstionToTest.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachineEnterExitTransitionToTest: exception while waiting "
                    + e.getMessage());
            }
        }
        assertTrue(smEnterExitTranstionToTest.getProcessedMessagesCount() == 1);
        ProcessedMessages.Info pmi;
        pmi = smEnterExitTranstionToTest.getProcessedMessage(0);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(smEnterExitTranstionToTest.mS2, pmi.getState());
        assertEquals(smEnterExitTranstionToTest.mS2, pmi.getOriginalState());
        assertEquals(smEnterExitTranstionToTest.mS1EnterCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS1ExitCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS2EnterCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS2ExitCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS3EnterCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS3ExitCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS3EnterCount, 1);
        assertEquals(smEnterExitTranstionToTest.mS3ExitCount, 1);
        if (smEnterExitTranstionToTest.isDbg()) {
            Log.d(TAG, "testStateMachineEnterExitTransitionToTest X");
        }
    }
    class StateMachine0 extends HierarchicalStateMachine {
        StateMachine0(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            setProcessedMessagesSize(3);
            addState(mS1);
            setInitialState(mS1);
        }
        class S1 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_6) {
                    transitionToHaltingState();
                }
                return true;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine0 mThisSm;
        private S1 mS1 = new S1();
    }
    @SmallTest
    public void testStateMachine0() throws Exception {
        StateMachine0 sm0 = new StateMachine0("sm0");
        sm0.start();
        if (sm0.isDbg()) Log.d(TAG, "testStateMachine0 E");
        synchronized (sm0) {
            for (int i = 1; i <= 6; i++) {
                sm0.sendMessage(sm0.obtainMessage(i));
            }
            try {
                sm0.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine0: exception while waiting " + e.getMessage());
            }
        }
        assertTrue(sm0.getProcessedMessagesCount() == 6);
        assertTrue(sm0.getProcessedMessagesSize() == 3);
        ProcessedMessages.Info pmi;
        pmi = sm0.getProcessedMessage(0);
        assertEquals(TEST_CMD_4, pmi.getWhat());
        assertEquals(sm0.mS1, pmi.getState());
        assertEquals(sm0.mS1, pmi.getOriginalState());
        pmi = sm0.getProcessedMessage(1);
        assertEquals(TEST_CMD_5, pmi.getWhat());
        assertEquals(sm0.mS1, pmi.getState());
        assertEquals(sm0.mS1, pmi.getOriginalState());
        pmi = sm0.getProcessedMessage(2);
        assertEquals(TEST_CMD_6, pmi.getWhat());
        assertEquals(sm0.mS1, pmi.getState());
        assertEquals(sm0.mS1, pmi.getOriginalState());
        if (sm0.isDbg()) Log.d(TAG, "testStateMachine0 X");
    }
    class StateMachine1 extends HierarchicalStateMachine {
        StateMachine1(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            setInitialState(mS1);
            if (DBG) Log.d(TAG, "StateMachine1: ctor X");
        }
        class S1 extends HierarchicalState {
            @Override protected void enter() {
                mEnterCount++;
            }
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_1) {
                    assertEquals(1, mEnterCount);
                    assertEquals(0, mExitCount);
                    transitionTo(mS1);
                } else if (message.what == TEST_CMD_2) {
                    assertEquals(2, mEnterCount);
                    assertEquals(1, mExitCount);
                    transitionToHaltingState();
                }
                return true;
            }
            @Override protected void exit() {
                mExitCount++;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine1 mThisSm;
        private S1 mS1 = new S1();
        private int mEnterCount;
        private int mExitCount;
    }
    @SmallTest
    public void testStateMachine1() throws Exception {
        StateMachine1 sm1 = new StateMachine1("sm1");
        sm1.start();
        if (sm1.isDbg()) Log.d(TAG, "testStateMachine1 E");
        synchronized (sm1) {
            sm1.sendMessage(TEST_CMD_1);
            sm1.sendMessage(TEST_CMD_2);
            try {
                sm1.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine1: exception while waiting " + e.getMessage());
            }
        }
        assertEquals(2, sm1.mEnterCount);
        assertEquals(2, sm1.mExitCount);
        assertTrue(sm1.getProcessedMessagesSize() == 2);
        ProcessedMessages.Info pmi;
        pmi = sm1.getProcessedMessage(0);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(sm1.mS1, pmi.getState());
        assertEquals(sm1.mS1, pmi.getOriginalState());
        pmi = sm1.getProcessedMessage(1);
        assertEquals(TEST_CMD_2, pmi.getWhat());
        assertEquals(sm1.mS1, pmi.getState());
        assertEquals(sm1.mS1, pmi.getOriginalState());
        assertEquals(2, sm1.mEnterCount);
        assertEquals(2, sm1.mExitCount);
        if (sm1.isDbg()) Log.d(TAG, "testStateMachine1 X");
    }
    class StateMachine2 extends HierarchicalStateMachine {
        StateMachine2(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            addState(mS2);
            setInitialState(mS1);
            if (DBG) Log.d(TAG, "StateMachine2: ctor X");
        }
        class S1 extends HierarchicalState {
            @Override protected void enter() {
                mDidEnter = true;
            }
            @Override protected boolean processMessage(Message message) {
                deferMessage(message);
                if (message.what == TEST_CMD_2) {
                    transitionTo(mS2);
                }
                return true;
            }
            @Override protected void exit() {
                mDidExit = true;
            }
        }
        class S2 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_2) {
                    transitionToHaltingState();
                }
                return true;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine2 mThisSm;
        private S1 mS1 = new S1();
        private S2 mS2 = new S2();
        private boolean mDidEnter = false;
        private boolean mDidExit = false;
    }
    @SmallTest
    public void testStateMachine2() throws Exception {
        StateMachine2 sm2 = new StateMachine2("sm2");
        sm2.start();
        if (sm2.isDbg()) Log.d(TAG, "testStateMachine2 E");
        synchronized (sm2) {
            sm2.sendMessage(TEST_CMD_1);
            sm2.sendMessage(TEST_CMD_2);
            try {
                sm2.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine2: exception while waiting " + e.getMessage());
            }
        }
        assertTrue(sm2.getProcessedMessagesSize() == 4);
        ProcessedMessages.Info pmi;
        pmi = sm2.getProcessedMessage(0);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(sm2.mS1, pmi.getState());
        pmi = sm2.getProcessedMessage(1);
        assertEquals(TEST_CMD_2, pmi.getWhat());
        assertEquals(sm2.mS1, pmi.getState());
        pmi = sm2.getProcessedMessage(2);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(sm2.mS2, pmi.getState());
        pmi = sm2.getProcessedMessage(3);
        assertEquals(TEST_CMD_2, pmi.getWhat());
        assertEquals(sm2.mS2, pmi.getState());
        assertTrue(sm2.mDidEnter);
        assertTrue(sm2.mDidExit);
        if (sm2.isDbg()) Log.d(TAG, "testStateMachine2 X");
    }
    class StateMachine3 extends HierarchicalStateMachine {
        StateMachine3(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mParentState);
                addState(mChildState, mParentState);
            setInitialState(mChildState);
            if (DBG) Log.d(TAG, "StateMachine3: ctor X");
        }
        class ParentState extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_2) {
                    transitionToHaltingState();
                }
                return true;
            }
        }
        class ChildState extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                return false;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine3 mThisSm;
        private ParentState mParentState = new ParentState();
        private ChildState mChildState = new ChildState();
    }
    @SmallTest
    public void testStateMachine3() throws Exception {
        StateMachine3 sm3 = new StateMachine3("sm3");
        sm3.start();
        if (sm3.isDbg()) Log.d(TAG, "testStateMachine3 E");
        synchronized (sm3) {
            sm3.sendMessage(TEST_CMD_1);
            sm3.sendMessage(TEST_CMD_2);
            try {
                sm3.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine3: exception while waiting " + e.getMessage());
            }
        }
        assertTrue(sm3.getProcessedMessagesSize() == 2);
        ProcessedMessages.Info pmi;
        pmi = sm3.getProcessedMessage(0);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(sm3.mParentState, pmi.getState());
        assertEquals(sm3.mChildState, pmi.getOriginalState());
        pmi = sm3.getProcessedMessage(1);
        assertEquals(TEST_CMD_2, pmi.getWhat());
        assertEquals(sm3.mParentState, pmi.getState());
        assertEquals(sm3.mChildState, pmi.getOriginalState());
        if (sm3.isDbg()) Log.d(TAG, "testStateMachine3 X");
    }
    class StateMachine4 extends HierarchicalStateMachine {
        StateMachine4(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mParentState);
                addState(mChildState1, mParentState);
                addState(mChildState2, mParentState);
            setInitialState(mChildState1);
            if (DBG) Log.d(TAG, "StateMachine4: ctor X");
        }
        class ParentState extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_2) {
                    transitionToHaltingState();
                }
                return true;
            }
        }
        class ChildState1 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                transitionTo(mChildState2);
                return true;
            }
        }
        class ChildState2 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                return false;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine4 mThisSm;
        private ParentState mParentState = new ParentState();
        private ChildState1 mChildState1 = new ChildState1();
        private ChildState2 mChildState2 = new ChildState2();
    }
    @SmallTest
    public void testStateMachine4() throws Exception {
        StateMachine4 sm4 = new StateMachine4("sm4");
        sm4.start();
        if (sm4.isDbg()) Log.d(TAG, "testStateMachine4 E");
        synchronized (sm4) {
            sm4.sendMessage(TEST_CMD_1);
            sm4.sendMessage(TEST_CMD_2);
            try {
                sm4.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine4: exception while waiting " + e.getMessage());
            }
        }
        assertTrue(sm4.getProcessedMessagesSize() == 2);
        ProcessedMessages.Info pmi;
        pmi = sm4.getProcessedMessage(0);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(sm4.mChildState1, pmi.getState());
        assertEquals(sm4.mChildState1, pmi.getOriginalState());
        pmi = sm4.getProcessedMessage(1);
        assertEquals(TEST_CMD_2, pmi.getWhat());
        assertEquals(sm4.mParentState, pmi.getState());
        assertEquals(sm4.mChildState2, pmi.getOriginalState());
        if (sm4.isDbg()) Log.d(TAG, "testStateMachine4 X");
    }
    class StateMachine5 extends HierarchicalStateMachine {
        StateMachine5(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mParentState1);
                addState(mChildState1, mParentState1);
                addState(mChildState2, mParentState1);
            addState(mParentState2);
                addState(mChildState3, mParentState2);
                addState(mChildState4, mParentState2);
                    addState(mChildState5, mChildState4);
            setInitialState(mChildState1);
            if (DBG) Log.d(TAG, "StateMachine5: ctor X");
        }
        class ParentState1 extends HierarchicalState {
            @Override protected void enter() {
                mParentState1EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                return true;
            }
            @Override protected void exit() {
                mParentState1ExitCount += 1;
            }
        }
        class ChildState1 extends HierarchicalState {
            @Override protected void enter() {
                mChildState1EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                assertEquals(1, mParentState1EnterCount);
                assertEquals(0, mParentState1ExitCount);
                assertEquals(1, mChildState1EnterCount);
                assertEquals(0, mChildState1ExitCount);
                assertEquals(0, mChildState2EnterCount);
                assertEquals(0, mChildState2ExitCount);
                assertEquals(0, mParentState2EnterCount);
                assertEquals(0, mParentState2ExitCount);
                assertEquals(0, mChildState3EnterCount);
                assertEquals(0, mChildState3ExitCount);
                assertEquals(0, mChildState4EnterCount);
                assertEquals(0, mChildState4ExitCount);
                assertEquals(0, mChildState5EnterCount);
                assertEquals(0, mChildState5ExitCount);
                transitionTo(mChildState2);
                return true;
            }
            @Override protected void exit() {
                mChildState1ExitCount += 1;
            }
        }
        class ChildState2 extends HierarchicalState {
            @Override protected void enter() {
                mChildState2EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                assertEquals(1, mParentState1EnterCount);
                assertEquals(0, mParentState1ExitCount);
                assertEquals(1, mChildState1EnterCount);
                assertEquals(1, mChildState1ExitCount);
                assertEquals(1, mChildState2EnterCount);
                assertEquals(0, mChildState2ExitCount);
                assertEquals(0, mParentState2EnterCount);
                assertEquals(0, mParentState2ExitCount);
                assertEquals(0, mChildState3EnterCount);
                assertEquals(0, mChildState3ExitCount);
                assertEquals(0, mChildState4EnterCount);
                assertEquals(0, mChildState4ExitCount);
                assertEquals(0, mChildState5EnterCount);
                assertEquals(0, mChildState5ExitCount);
                transitionTo(mChildState5);
                return true;
            }
            @Override protected void exit() {
                mChildState2ExitCount += 1;
            }
        }
        class ParentState2 extends HierarchicalState {
            @Override protected void enter() {
                mParentState2EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                assertEquals(1, mParentState1EnterCount);
                assertEquals(1, mParentState1ExitCount);
                assertEquals(1, mChildState1EnterCount);
                assertEquals(1, mChildState1ExitCount);
                assertEquals(1, mChildState2EnterCount);
                assertEquals(1, mChildState2ExitCount);
                assertEquals(2, mParentState2EnterCount);
                assertEquals(1, mParentState2ExitCount);
                assertEquals(1, mChildState3EnterCount);
                assertEquals(1, mChildState3ExitCount);
                assertEquals(2, mChildState4EnterCount);
                assertEquals(2, mChildState4ExitCount);
                assertEquals(1, mChildState5EnterCount);
                assertEquals(1, mChildState5ExitCount);
                transitionToHaltingState();
                return true;
            }
            @Override protected void exit() {
                mParentState2ExitCount += 1;
            }
        }
        class ChildState3 extends HierarchicalState {
            @Override protected void enter() {
                mChildState3EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                assertEquals(1, mParentState1EnterCount);
                assertEquals(1, mParentState1ExitCount);
                assertEquals(1, mChildState1EnterCount);
                assertEquals(1, mChildState1ExitCount);
                assertEquals(1, mChildState2EnterCount);
                assertEquals(1, mChildState2ExitCount);
                assertEquals(1, mParentState2EnterCount);
                assertEquals(0, mParentState2ExitCount);
                assertEquals(1, mChildState3EnterCount);
                assertEquals(0, mChildState3ExitCount);
                assertEquals(1, mChildState4EnterCount);
                assertEquals(1, mChildState4ExitCount);
                assertEquals(1, mChildState5EnterCount);
                assertEquals(1, mChildState5ExitCount);
                transitionTo(mChildState4);
                return true;
            }
            @Override protected void exit() {
                mChildState3ExitCount += 1;
            }
        }
        class ChildState4 extends HierarchicalState {
            @Override protected void enter() {
                mChildState4EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                assertEquals(1, mParentState1EnterCount);
                assertEquals(1, mParentState1ExitCount);
                assertEquals(1, mChildState1EnterCount);
                assertEquals(1, mChildState1ExitCount);
                assertEquals(1, mChildState2EnterCount);
                assertEquals(1, mChildState2ExitCount);
                assertEquals(1, mParentState2EnterCount);
                assertEquals(0, mParentState2ExitCount);
                assertEquals(1, mChildState3EnterCount);
                assertEquals(1, mChildState3ExitCount);
                assertEquals(2, mChildState4EnterCount);
                assertEquals(1, mChildState4ExitCount);
                assertEquals(1, mChildState5EnterCount);
                assertEquals(1, mChildState5ExitCount);
                transitionTo(mParentState2);
                return true;
            }
            @Override protected void exit() {
                mChildState4ExitCount += 1;
            }
        }
        class ChildState5 extends HierarchicalState {
            @Override protected void enter() {
                mChildState5EnterCount += 1;
            }
            @Override protected boolean processMessage(Message message) {
                assertEquals(1, mParentState1EnterCount);
                assertEquals(1, mParentState1ExitCount);
                assertEquals(1, mChildState1EnterCount);
                assertEquals(1, mChildState1ExitCount);
                assertEquals(1, mChildState2EnterCount);
                assertEquals(1, mChildState2ExitCount);
                assertEquals(1, mParentState2EnterCount);
                assertEquals(0, mParentState2ExitCount);
                assertEquals(0, mChildState3EnterCount);
                assertEquals(0, mChildState3ExitCount);
                assertEquals(1, mChildState4EnterCount);
                assertEquals(0, mChildState4ExitCount);
                assertEquals(1, mChildState5EnterCount);
                assertEquals(0, mChildState5ExitCount);
                transitionTo(mChildState3);
                return true;
            }
            @Override protected void exit() {
                mChildState5ExitCount += 1;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine5 mThisSm;
        private ParentState1 mParentState1 = new ParentState1();
        private ChildState1 mChildState1 = new ChildState1();
        private ChildState2 mChildState2 = new ChildState2();
        private ParentState2 mParentState2 = new ParentState2();
        private ChildState3 mChildState3 = new ChildState3();
        private ChildState4 mChildState4 = new ChildState4();
        private ChildState5 mChildState5 = new ChildState5();
        private int mParentState1EnterCount = 0;
        private int mParentState1ExitCount = 0;
        private int mChildState1EnterCount = 0;
        private int mChildState1ExitCount = 0;
        private int mChildState2EnterCount = 0;
        private int mChildState2ExitCount = 0;
        private int mParentState2EnterCount = 0;
        private int mParentState2ExitCount = 0;
        private int mChildState3EnterCount = 0;
        private int mChildState3ExitCount = 0;
        private int mChildState4EnterCount = 0;
        private int mChildState4ExitCount = 0;
        private int mChildState5EnterCount = 0;
        private int mChildState5ExitCount = 0;
    }
    @SmallTest
    public void testStateMachine5() throws Exception {
        StateMachine5 sm5 = new StateMachine5("sm5");
        sm5.start();
        if (sm5.isDbg()) Log.d(TAG, "testStateMachine5 E");
        synchronized (sm5) {
            sm5.sendMessage(TEST_CMD_1);
            sm5.sendMessage(TEST_CMD_2);
            sm5.sendMessage(TEST_CMD_3);
            sm5.sendMessage(TEST_CMD_4);
            sm5.sendMessage(TEST_CMD_5);
            sm5.sendMessage(TEST_CMD_6);
            try {
                sm5.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine5: exception while waiting " + e.getMessage());
            }
        }
        assertTrue(sm5.getProcessedMessagesSize() == 6);
        assertEquals(1, sm5.mParentState1EnterCount);
        assertEquals(1, sm5.mParentState1ExitCount);
        assertEquals(1, sm5.mChildState1EnterCount);
        assertEquals(1, sm5.mChildState1ExitCount);
        assertEquals(1, sm5.mChildState2EnterCount);
        assertEquals(1, sm5.mChildState2ExitCount);
        assertEquals(2, sm5.mParentState2EnterCount);
        assertEquals(2, sm5.mParentState2ExitCount);
        assertEquals(1, sm5.mChildState3EnterCount);
        assertEquals(1, sm5.mChildState3ExitCount);
        assertEquals(2, sm5.mChildState4EnterCount);
        assertEquals(2, sm5.mChildState4ExitCount);
        assertEquals(1, sm5.mChildState5EnterCount);
        assertEquals(1, sm5.mChildState5ExitCount);
        ProcessedMessages.Info pmi;
        pmi = sm5.getProcessedMessage(0);
        assertEquals(TEST_CMD_1, pmi.getWhat());
        assertEquals(sm5.mChildState1, pmi.getState());
        assertEquals(sm5.mChildState1, pmi.getOriginalState());
        pmi = sm5.getProcessedMessage(1);
        assertEquals(TEST_CMD_2, pmi.getWhat());
        assertEquals(sm5.mChildState2, pmi.getState());
        assertEquals(sm5.mChildState2, pmi.getOriginalState());
        pmi = sm5.getProcessedMessage(2);
        assertEquals(TEST_CMD_3, pmi.getWhat());
        assertEquals(sm5.mChildState5, pmi.getState());
        assertEquals(sm5.mChildState5, pmi.getOriginalState());
        pmi = sm5.getProcessedMessage(3);
        assertEquals(TEST_CMD_4, pmi.getWhat());
        assertEquals(sm5.mChildState3, pmi.getState());
        assertEquals(sm5.mChildState3, pmi.getOriginalState());
        pmi = sm5.getProcessedMessage(4);
        assertEquals(TEST_CMD_5, pmi.getWhat());
        assertEquals(sm5.mChildState4, pmi.getState());
        assertEquals(sm5.mChildState4, pmi.getOriginalState());
        pmi = sm5.getProcessedMessage(5);
        assertEquals(TEST_CMD_6, pmi.getWhat());
        assertEquals(sm5.mParentState2, pmi.getState());
        assertEquals(sm5.mParentState2, pmi.getOriginalState());
        if (sm5.isDbg()) Log.d(TAG, "testStateMachine5 X");
    }
    class StateMachine6 extends HierarchicalStateMachine {
        StateMachine6(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            setInitialState(mS1);
            if (DBG) Log.d(TAG, "StateMachine6: ctor X");
        }
        class S1 extends HierarchicalState {
            @Override protected void enter() {
                sendMessage(TEST_CMD_1);
            }
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_1) {
                    mArrivalTimeMsg1 = SystemClock.elapsedRealtime();
                } else if (message.what == TEST_CMD_2) {
                    mArrivalTimeMsg2 = SystemClock.elapsedRealtime();
                    transitionToHaltingState();
                }
                return true;
            }
            @Override protected void exit() {
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine6 mThisSm;
        private S1 mS1 = new S1();
        private long mArrivalTimeMsg1;
        private long mArrivalTimeMsg2;
    }
    @SmallTest
    public void testStateMachine6() throws Exception {
        long sentTimeMsg2;
        final int DELAY_TIME = 250;
        final int DELAY_FUDGE = 20;
        StateMachine6 sm6 = new StateMachine6("sm6");
        sm6.start();
        if (sm6.isDbg()) Log.d(TAG, "testStateMachine6 E");
        synchronized (sm6) {
            sentTimeMsg2 = SystemClock.elapsedRealtime();
            sm6.sendMessageDelayed(TEST_CMD_2, DELAY_TIME);
            try {
                sm6.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine6: exception while waiting " + e.getMessage());
            }
        }
        long arrivalTimeDiff = sm6.mArrivalTimeMsg2 - sm6.mArrivalTimeMsg1;
        long expectedDelay = DELAY_TIME - DELAY_FUDGE;
        if (sm6.isDbg()) Log.d(TAG, "testStateMachine6: expect " + arrivalTimeDiff
                                    + " >= " + expectedDelay);
        assertTrue(arrivalTimeDiff >= expectedDelay);
        if (sm6.isDbg()) Log.d(TAG, "testStateMachine6 X");
    }
    class StateMachine7 extends HierarchicalStateMachine {
        private final int SM7_DELAY_TIME = 250;
        StateMachine7(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            addState(mS2);
            setInitialState(mS1);
            if (DBG) Log.d(TAG, "StateMachine7: ctor X");
        }
        class S1 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                transitionTo(mS2);
                return true;
            }
            @Override protected void exit() {
                sendMessage(TEST_CMD_2);
            }
        }
        class S2 extends HierarchicalState {
            @Override protected void enter() {
                sendMessageDelayed(TEST_CMD_3, SM7_DELAY_TIME);
            }
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_2) {
                    mMsgCount += 1;
                    mArrivalTimeMsg2 = SystemClock.elapsedRealtime();
                } else if (message.what == TEST_CMD_3) {
                    mMsgCount += 1;
                    mArrivalTimeMsg3 = SystemClock.elapsedRealtime();
                }
                if (mMsgCount == 2) {
                    transitionToHaltingState();
                }
                return true;
            }
            @Override protected void exit() {
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachine7 mThisSm;
        private S1 mS1 = new S1();
        private S2 mS2 = new S2();
        private int mMsgCount = 0;
        private long mArrivalTimeMsg2;
        private long mArrivalTimeMsg3;
    }
    @SmallTest
    public void testStateMachine7() throws Exception {
        long sentTimeMsg2;
        final int SM7_DELAY_FUDGE = 20;
        StateMachine7 sm7 = new StateMachine7("sm7");
        sm7.start();
        if (sm7.isDbg()) Log.d(TAG, "testStateMachine7 E");
        synchronized (sm7) {
            sentTimeMsg2 = SystemClock.elapsedRealtime();
            sm7.sendMessage(TEST_CMD_1);
            try {
                sm7.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachine7: exception while waiting " + e.getMessage());
            }
        }
        long arrivalTimeDiff = sm7.mArrivalTimeMsg3 - sm7.mArrivalTimeMsg2;
        long expectedDelay = sm7.SM7_DELAY_TIME - SM7_DELAY_FUDGE;
        if (sm7.isDbg()) Log.d(TAG, "testStateMachine7: expect " + arrivalTimeDiff
                                    + " >= " + expectedDelay);
        assertTrue(arrivalTimeDiff >= expectedDelay);
        if (sm7.isDbg()) Log.d(TAG, "testStateMachine7 X");
    }
    class StateMachineUnhandledMessage extends HierarchicalStateMachine {
        StateMachineUnhandledMessage(String name) {
            super(name);
            mThisSm = this;
            setDbg(DBG);
            addState(mS1);
            setInitialState(mS1);
        }
        @Override protected void unhandledMessage(Message message) {
            mUnhandledMessageCount += 1;
        }
        class S1 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_2) {
                    transitionToHaltingState();
                }
                return false;
            }
        }
        @Override
        protected void halting() {
            synchronized (mThisSm) {
                mThisSm.notifyAll();
            }
        }
        private StateMachineUnhandledMessage mThisSm;
        private int mUnhandledMessageCount;
        private S1 mS1 = new S1();
    }
    @SmallTest
    public void testStateMachineUnhandledMessage() throws Exception {
        StateMachineUnhandledMessage sm = new StateMachineUnhandledMessage("sm");
        sm.start();
        if (sm.isDbg()) Log.d(TAG, "testStateMachineUnhandledMessage E");
        synchronized (sm) {
            for (int i = 1; i <= 2; i++) {
                sm.sendMessage(i);
            }
            try {
                sm.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachineUnhandledMessage: exception while waiting "
                        + e.getMessage());
            }
        }
        assertTrue(sm.getProcessedMessagesCount() == 2);
        assertEquals(2, sm.mUnhandledMessageCount);
        if (sm.isDbg()) Log.d(TAG, "testStateMachineUnhandledMessage X");
    }
    class StateMachineSharedThread extends HierarchicalStateMachine {
        StateMachineSharedThread(String name, Looper looper, int maxCount) {
            super(name, looper);
            mMaxCount = maxCount;
            setDbg(DBG);
            addState(mS1);
            setInitialState(mS1);
        }
        class S1 extends HierarchicalState {
            @Override protected boolean processMessage(Message message) {
                if (message.what == TEST_CMD_4) {
                    transitionToHaltingState();
                }
                return true;
            }
        }
        @Override
        protected void halting() {
            sharedCounter += 1;
            if (sharedCounter == mMaxCount) {
                synchronized (waitObject) {
                    waitObject.notifyAll();
                }
            }
        }
        private int mMaxCount;
        private S1 mS1 = new S1();
    }
    private static int sharedCounter = 0;
    private static Object waitObject = new Object();
    @SmallTest
    public void testStateMachineSharedThread() throws Exception {
        if (DBG) Log.d(TAG, "testStateMachineSharedThread E");
        HandlerThread smThread = new HandlerThread("testStateMachineSharedThread");
        smThread.start();
        StateMachineSharedThread sms[] = new StateMachineSharedThread[10];
        for (int i = 0; i < sms.length; i++) {
            sms[i] = new StateMachineSharedThread("sm", smThread.getLooper(), sms.length);
            sms[i].start();
        }
        synchronized (waitObject) {
            for (StateMachineSharedThread sm : sms) {
                for (int i = 1; i <= 4; i++) {
                    sm.sendMessage(i);
                }
            }
            try {
                waitObject.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testStateMachineSharedThread: exception while waiting "
                        + e.getMessage());
            }
        }
        for (StateMachineSharedThread sm : sms) {
            assertTrue(sm.getProcessedMessagesCount() == 4);
            for (int i = 0; i < sm.getProcessedMessagesCount(); i++) {
                ProcessedMessages.Info pmi = sm.getProcessedMessage(i);
                assertEquals(i+1, pmi.getWhat());
                assertEquals(sm.mS1, pmi.getState());
                assertEquals(sm.mS1, pmi.getOriginalState());
            }
        }
        if (DBG) Log.d(TAG, "testStateMachineSharedThread X");
    }
    @SmallTest
    public void testHsm1() throws Exception {
        if (DBG) Log.d(TAG, "testHsm1 E");
        Hsm1 sm = Hsm1.makeHsm1();
        sm.sendMessage(Hsm1.CMD_1);
        sm.sendMessage(Hsm1.CMD_2);
        synchronized (sm) {
            try {
                sm.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "testHsm1: exception while waiting " + e.getMessage());
            }
        }
        assertEquals(7, sm.getProcessedMessagesCount());
        ProcessedMessages.Info pmi = sm.getProcessedMessage(0);
        assertEquals(Hsm1.CMD_1, pmi.getWhat());
        assertEquals(sm.mS1, pmi.getState());
        assertEquals(sm.mS1, pmi.getOriginalState());
        pmi = sm.getProcessedMessage(1);
        assertEquals(Hsm1.CMD_2, pmi.getWhat());
        assertEquals(sm.mP1, pmi.getState());
        assertEquals(sm.mS1, pmi.getOriginalState());
        pmi = sm.getProcessedMessage(2);
        assertEquals(Hsm1.CMD_2, pmi.getWhat());
        assertEquals(sm.mS2, pmi.getState());
        assertEquals(sm.mS2, pmi.getOriginalState());
        pmi = sm.getProcessedMessage(3);
        assertEquals(Hsm1.CMD_3, pmi.getWhat());
        assertEquals(sm.mS2, pmi.getState());
        assertEquals(sm.mS2, pmi.getOriginalState());
        pmi = sm.getProcessedMessage(4);
        assertEquals(Hsm1.CMD_3, pmi.getWhat());
        assertEquals(sm.mP2, pmi.getState());
        assertEquals(sm.mP2, pmi.getOriginalState());
        pmi = sm.getProcessedMessage(5);
        assertEquals(Hsm1.CMD_4, pmi.getWhat());
        assertEquals(sm.mP2, pmi.getState());
        assertEquals(sm.mP2, pmi.getOriginalState());
        pmi = sm.getProcessedMessage(6);
        assertEquals(Hsm1.CMD_5, pmi.getWhat());
        assertEquals(sm.mP2, pmi.getState());
        assertEquals(sm.mP2, pmi.getOriginalState());
        if (DBG) Log.d(TAG, "testStateMachineSharedThread X");
    }
}
class Hsm1 extends HierarchicalStateMachine {
    private static final String TAG = "hsm1";
    public static final int CMD_1 = 1;
    public static final int CMD_2 = 2;
    public static final int CMD_3 = 3;
    public static final int CMD_4 = 4;
    public static final int CMD_5 = 5;
    public static Hsm1 makeHsm1() {
        Log.d(TAG, "makeHsm1 E");
        Hsm1 sm = new Hsm1("hsm1");
        sm.start();
        Log.d(TAG, "makeHsm1 X");
        return sm;
    }
    Hsm1(String name) {
        super(name);
        Log.d(TAG, "ctor E");
        addState(mP1);
            addState(mS1, mP1);
            addState(mS2, mP1);
        addState(mP2);
        setInitialState(mS1);
        Log.d(TAG, "ctor X");
    }
    class P1 extends HierarchicalState {
        @Override protected void enter() {
            Log.d(TAG, "P1.enter");
        }
        @Override protected boolean processMessage(Message message) {
            boolean retVal;
            Log.d(TAG, "P1.processMessage what=" + message.what);
            switch(message.what) {
            case CMD_2:
                sendMessage(CMD_3);
                deferMessage(message);
                transitionTo(mS2);
                retVal = true;
                break;
            default:
                retVal = false;
                break;
            }
            return retVal;
        }
        @Override protected void exit() {
            Log.d(TAG, "P1.exit");
        }
    }
    class S1 extends HierarchicalState {
        @Override protected void enter() {
            Log.d(TAG, "S1.enter");
        }
        @Override protected boolean processMessage(Message message) {
            Log.d(TAG, "S1.processMessage what=" + message.what);
            if (message.what == CMD_1) {
                transitionTo(mS1);
                return true;
            } else {
                return false;
            }
        }
        @Override protected void exit() {
            Log.d(TAG, "S1.exit");
        }
    }
    class S2 extends HierarchicalState {
        @Override protected void enter() {
            Log.d(TAG, "S2.enter");
        }
        @Override protected boolean processMessage(Message message) {
            boolean retVal;
            Log.d(TAG, "S2.processMessage what=" + message.what);
            switch(message.what) {
            case(CMD_2):
                sendMessage(CMD_4);
                retVal = true;
                break;
            case(CMD_3):
                deferMessage(message);
                transitionTo(mP2);
                retVal = true;
                break;
            default:
                retVal = false;
                break;
            }
            return retVal;
        }
        @Override protected void exit() {
            Log.d(TAG, "S2.exit");
        }
    }
    class P2 extends HierarchicalState {
        @Override protected void enter() {
            Log.d(TAG, "P2.enter");
            sendMessage(CMD_5);
        }
        @Override protected boolean processMessage(Message message) {
            Log.d(TAG, "P2.processMessage what=" + message.what);
            switch(message.what) {
            case(CMD_3):
                break;
            case(CMD_4):
                break;
            case(CMD_5):
                transitionToHaltingState();
                break;
            }
            return true;
        }
        @Override protected void exit() {
            Log.d(TAG, "P2.exit");
        }
    }
    @Override
    protected void halting() {
        Log.d(TAG, "halting");
        synchronized (this) {
            this.notifyAll();
        }
    }
    P1 mP1 = new P1();
    S1 mS1 = new S1();
    S2 mS2 = new S2();
    P2 mP2 = new P2();
}
