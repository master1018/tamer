public class JetBoyView extends SurfaceView implements SurfaceHolder.Callback {
    public static final int mSuccessThreshold = 50;
    public int mHitStreak = 0;
    public int mHitTotal = 0;
    public int mCurrentBed = 0;
    private Bitmap mTitleBG;
    private Bitmap mTitleBG2;
    class GameEvent {
        public GameEvent() {
            eventTime = System.currentTimeMillis();
        }
        long eventTime;
    }
    class KeyGameEvent extends GameEvent {
        public KeyGameEvent(int keyCode, boolean up, KeyEvent msg) {
            this.keyCode = keyCode;
            this.msg = msg;
            this.up = up;
        }
        public int keyCode;
        public KeyEvent msg;
        public boolean up;
    }
    class JetGameEvent extends GameEvent {
        public JetGameEvent(JetPlayer player, short segment, byte track, byte channel,
                byte controller, byte value) {
            this.player = player;
            this.segment = segment;
            this.track = track;
            this.channel = channel;
            this.controller = controller;
            this.value = value;
        }
        public JetPlayer player;
        public short segment;
        public byte track;
        public byte channel;
        public byte controller;
        public byte value;
    }
    class JetBoyThread extends Thread implements OnJetEventListener {
        public static final int STATE_START = -1;
        public static final int STATE_PLAY = 0;
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_RUNNING = 3;
        private static final int ANIMATION_FRAMES_PER_BEAT = 4;
        public boolean mInitialized = false;
        protected ConcurrentLinkedQueue<GameEvent> mEventQueue = new ConcurrentLinkedQueue<GameEvent>();
        protected Object mKeyContext = null;
        public int mTimerLimit;
        public final int TIMER_LIMIT = 72;
        private String mTimerValue = "1:12";
        public int mState;
        boolean mLaserOn = false;
        long mLaserFireTime = 0;
        private Bitmap mBackgroundImageFar;
        private Bitmap mBackgroundImageNear;
        private final byte NEW_ASTEROID_EVENT = 80;
        private final byte TIMER_EVENT = 82;
        private int mBeatCount = 1;
        private Bitmap[] mShipFlying = new Bitmap[4];
        private Bitmap[] mBeam = new Bitmap[4];
        private Bitmap[] mAsteroids = new Bitmap[12];
        private Bitmap[] mExplosions = new Bitmap[4];
        private Bitmap mTimerShell;
        private Bitmap mLaserShot;
        private long mLastBeatTime;
        private long mPassedTime;
        private int mPixelMoveX = 25;
        private Random mRandom = new Random();
        private JetPlayer mJet = null;
        private boolean mJetPlaying = false;
        private Handler mHandler;
        private SurfaceHolder mSurfaceHolder;
        private Context mContext;
        private boolean mRun = false;
        private Timer mTimer = null;
        private TimerTask mTimerTask = null;
        private int mTaskIntervalInMillis = 1000;
        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        private int mShipIndex = 0;
        private Vector<Asteroid> mDangerWillRobinson;
        private Vector<Explosion> mExplosion;
        private int mBGFarMoveX = 0;
        private int mBGNearMoveX = 0;
        private int mJetBoyYMin = 40;
        private int mJetBoyX = 0;
        private int mJetBoyY = 0;
        private int mAsteroidMoveLimitX = 110;
        private int mAsteroidMinY = 40;
        Resources mRes;
        private boolean muteMask[][] = new boolean[9][32];
        public JetBoyThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
            mRes = context.getResources();
            for (int ii = 0; ii < 8; ii++) {
                for (int xx = 0; xx < 32; xx++) {
                    muteMask[ii][xx] = true;
                }
            }
            muteMask[0][2] = false;
            muteMask[0][3] = false;
            muteMask[0][4] = false;
            muteMask[0][5] = false;
            muteMask[1][2] = false;
            muteMask[1][3] = false;
            muteMask[1][4] = false;
            muteMask[1][5] = false;
            muteMask[1][8] = false;
            muteMask[1][9] = false;
            muteMask[2][2] = false;
            muteMask[2][3] = false;
            muteMask[2][6] = false;
            muteMask[2][7] = false;
            muteMask[2][8] = false;
            muteMask[2][9] = false;
            muteMask[3][2] = false;
            muteMask[3][3] = false;
            muteMask[3][6] = false;
            muteMask[3][11] = false;
            muteMask[3][12] = false;
            muteMask[4][2] = false;
            muteMask[4][3] = false;
            muteMask[4][10] = false;
            muteMask[4][11] = false;
            muteMask[4][12] = false;
            muteMask[4][13] = false;
            muteMask[5][2] = false;
            muteMask[5][3] = false;
            muteMask[5][10] = false;
            muteMask[5][12] = false;
            muteMask[5][15] = false;
            muteMask[5][17] = false;
            muteMask[6][2] = false;
            muteMask[6][3] = false;
            muteMask[6][14] = false;
            muteMask[6][15] = false;
            muteMask[6][16] = false;
            muteMask[6][17] = false;
            muteMask[7][2] = false;
            muteMask[7][3] = false;
            muteMask[7][6] = false;
            muteMask[7][14] = false;
            muteMask[7][15] = false;
            muteMask[7][16] = false;
            muteMask[7][17] = false;
            muteMask[7][18] = false;
            for (int xx = 0; xx < 32; xx++) {
                muteMask[8][xx] = false;
            }
            mState = STATE_START;
            setInitialGameState();
            mTitleBG = BitmapFactory.decodeResource(mRes, R.drawable.title_hori);
            mBackgroundImageFar = BitmapFactory.decodeResource(mRes, R.drawable.background_a);
            mLaserShot = BitmapFactory.decodeResource(mRes, R.drawable.laser);
            mBackgroundImageNear = BitmapFactory.decodeResource(mRes, R.drawable.background_b);
            mShipFlying[0] = BitmapFactory.decodeResource(mRes, R.drawable.ship2_1);
            mShipFlying[1] = BitmapFactory.decodeResource(mRes, R.drawable.ship2_2);
            mShipFlying[2] = BitmapFactory.decodeResource(mRes, R.drawable.ship2_3);
            mShipFlying[3] = BitmapFactory.decodeResource(mRes, R.drawable.ship2_4);
            mBeam[0] = BitmapFactory.decodeResource(mRes, R.drawable.intbeam_1);
            mBeam[1] = BitmapFactory.decodeResource(mRes, R.drawable.intbeam_2);
            mBeam[2] = BitmapFactory.decodeResource(mRes, R.drawable.intbeam_3);
            mBeam[3] = BitmapFactory.decodeResource(mRes, R.drawable.intbeam_4);
            mTimerShell = BitmapFactory.decodeResource(mRes, R.drawable.int_timer);
            mAsteroids[11] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid01);
            mAsteroids[10] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid02);
            mAsteroids[9] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid03);
            mAsteroids[8] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid04);
            mAsteroids[7] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid05);
            mAsteroids[6] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid06);
            mAsteroids[5] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid07);
            mAsteroids[4] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid08);
            mAsteroids[3] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid09);
            mAsteroids[2] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid10);
            mAsteroids[1] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid11);
            mAsteroids[0] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid12);
            mExplosions[0] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode1);
            mExplosions[1] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode2);
            mExplosions[2] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode3);
            mExplosions[3] = BitmapFactory.decodeResource(mRes, R.drawable.asteroid_explode4);
        }
        private void initializeJetPlayer() {
            mJet = JetPlayer.getJetPlayer();
            mJetPlaying = false;
            mJet.clearQueue();
            mJet.setEventListener(this);
            Log.d(TAG, "opening jet file");
            mJet.loadJetFile(mContext.getResources().openRawResourceFd(R.raw.level1));
            Log.d(TAG, "opening jet file DONE");
            mCurrentBed = 0;
            byte sSegmentID = 0;
            Log.d(TAG, " start queuing jet file");
            mJet.queueJetSegment(0, 0, 0, 0, 0, sSegmentID);
            mJet.queueJetSegment(1, 0, 4, 0, 0, sSegmentID);
            mJet.queueJetSegment(1, 0, 4, 1, 0, sSegmentID);
            mJet.setMuteArray(muteMask[0], true);
            Log.d(TAG, " start queuing jet file DONE");
        }
        private void doDraw(Canvas canvas) {
            if (mState == STATE_RUNNING) {
                doDrawRunning(canvas);
            } else if (mState == STATE_START) {
                doDrawReady(canvas);
            } else if (mState == STATE_PLAY || mState == STATE_LOSE) {
                if (mTitleBG2 == null) {
                    mTitleBG2 = BitmapFactory.decodeResource(mRes, R.drawable.title_bg_hori);
                }
                doDrawPlay(canvas);
            }
        }
        private void doDrawRunning(Canvas canvas) {
            mBGFarMoveX = mBGFarMoveX - 1;
            mBGNearMoveX = mBGNearMoveX - 4;
            int newFarX = mBackgroundImageFar.getWidth() - (-mBGFarMoveX);
            if (newFarX <= 0) {
                mBGFarMoveX = 0;
                canvas.drawBitmap(mBackgroundImageFar, mBGFarMoveX, 0, null);
            } else {
                canvas.drawBitmap(mBackgroundImageFar, mBGFarMoveX, 0, null);
                canvas.drawBitmap(mBackgroundImageFar, newFarX, 0, null);
            }
            int newNearX = mBackgroundImageNear.getWidth() - (-mBGNearMoveX);
            if (newNearX <= 0) {
                mBGNearMoveX = 0;
                canvas.drawBitmap(mBackgroundImageNear, mBGNearMoveX, 0, null);
            } else {
                canvas.drawBitmap(mBackgroundImageNear, mBGNearMoveX, 0, null);
                canvas.drawBitmap(mBackgroundImageNear, newNearX, 0, null);
            }
            doAsteroidAnimation(canvas);
            canvas.drawBitmap(mBeam[mShipIndex], 51 + 20, 0, null);
            mShipIndex++;
            if (mShipIndex == 4)
                mShipIndex = 0;
            canvas.drawBitmap(mShipFlying[mShipIndex], mJetBoyX, mJetBoyY, null);
            if (mLaserOn) {
                canvas.drawBitmap(mLaserShot, mJetBoyX + mShipFlying[0].getWidth(), mJetBoyY
                        + (mShipFlying[0].getHeight() / 2), null);
            }
            canvas.drawBitmap(mTimerShell, mCanvasWidth - mTimerShell.getWidth(), 0, null);
        }
        private void setInitialGameState() {
            mTimerLimit = TIMER_LIMIT;
            mJetBoyY = mJetBoyYMin;
            initializeJetPlayer();
            mTimer = new Timer();
            mDangerWillRobinson = new Vector<Asteroid>();
            mExplosion = new Vector<Explosion>();
            mInitialized = true;
            mHitStreak = 0;
            mHitTotal = 0;
        }
        private void doAsteroidAnimation(Canvas canvas) {
            if ((mDangerWillRobinson == null | mDangerWillRobinson.size() == 0)
                    && (mExplosion != null && mExplosion.size() == 0))
                return;
            long frameDelta = System.currentTimeMillis() - mLastBeatTime;
            int animOffset = (int)(ANIMATION_FRAMES_PER_BEAT * frameDelta / 428);
            for (int i = (mDangerWillRobinson.size() - 1); i >= 0; i--) {
                Asteroid asteroid = mDangerWillRobinson.elementAt(i);
                if (!asteroid.mMissed)
                    mJetBoyY = asteroid.mDrawY;
                canvas.drawBitmap(
                        mAsteroids[(asteroid.mAniIndex + animOffset) % mAsteroids.length],
                        asteroid.mDrawX, asteroid.mDrawY, null);
            }
            for (int i = (mExplosion.size() - 1); i >= 0; i--) {
                Explosion ex = mExplosion.elementAt(i);
                canvas.drawBitmap(mExplosions[(ex.mAniIndex + animOffset) % mExplosions.length],
                        ex.mDrawX, ex.mDrawY, null);
            }
        }
        private void doDrawReady(Canvas canvas) {
            canvas.drawBitmap(mTitleBG, 0, 0, null);
        }
        private void doDrawPlay(Canvas canvas) {
            canvas.drawBitmap(mTitleBG2, 0, 0, null);
        }
        public void run() {
            while (mRun) {
                Canvas c = null;
                if (mState == STATE_RUNNING) {
                    updateGameState();
                    if (!mJetPlaying) {
                        mInitialized = false;
                        Log.d(TAG, "------> STARTING JET PLAY");
                        mJet.play();
                        mJetPlaying = true;
                    }
                    mPassedTime = System.currentTimeMillis();
                    if (mTimerTask == null) {
                        mTimerTask = new TimerTask() {
                            public void run() {
                                doCountDown();
                            }
                        };
                        mTimer.schedule(mTimerTask, mTaskIntervalInMillis);
                    }
                }
                else if (mState == STATE_PLAY && !mInitialized)
                {
                    setInitialGameState();
                } else if (mState == STATE_LOSE) {
                    mInitialized = false;
                }
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    doDraw(c);
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
        protected void updateGameState() {
            while (true) {
                GameEvent event = mEventQueue.poll();
                if (event == null)
                    break;
                if (event instanceof KeyGameEvent) {
                    mKeyContext = processKeyEvent((KeyGameEvent)event, mKeyContext);
                    updateLaser(mKeyContext);
                }
                else if (event instanceof JetGameEvent) {
                    JetGameEvent jetEvent = (JetGameEvent)event;
                    if (jetEvent.value == TIMER_EVENT) {
                        mLastBeatTime = System.currentTimeMillis();
                        updateLaser(mKeyContext);
                        updateExplosions(mKeyContext);
                        updateAsteroids(mKeyContext);
                    }
                    processJetEvent(jetEvent.player, jetEvent.segment, jetEvent.track,
                            jetEvent.channel, jetEvent.controller, jetEvent.value);
                }
            }
        }
        protected Object processKeyEvent(KeyGameEvent event, Object context) {
            if (event.up) {
                if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    return null;
                }
            }
            else {
                if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER && (context == null)) {
                    return event;
                }
            }
            return context;
        }
        protected void updateLaser(Object inputContext) {
            long keyTime = inputContext == null ? 0 : ((GameEvent)inputContext).eventTime;
            if (mLaserOn && System.currentTimeMillis() - mLaserFireTime > 400) {
                mLaserOn = false;
            }
            else if (System.currentTimeMillis() - mLaserFireTime > 300) {
                mJet.setMuteFlag(23, true, false);
            }
            if (!mLaserOn && System.currentTimeMillis() - keyTime <= 400) {
                mLaserOn = true;
                mLaserFireTime = keyTime;
                mJet.setMuteFlag(23, false, false);
            }
        }
        protected void updateAsteroids(Object inputContext) {
            if (mDangerWillRobinson == null | mDangerWillRobinson.size() == 0)
                return;
            for (int i = (mDangerWillRobinson.size() - 1); i >= 0; i--) {
                Asteroid asteroid = mDangerWillRobinson.elementAt(i);
                if (asteroid.mDrawX <= mAsteroidMoveLimitX + 20 && !asteroid.mMissed)
                {
                    if (mLaserOn) {
                        mHitStreak++;
                        mHitTotal++;
                        Explosion ex = new Explosion();
                        ex.mAniIndex = 0;
                        ex.mDrawX = asteroid.mDrawX;
                        ex.mDrawY = asteroid.mDrawY;
                        mExplosion.add(ex);
                        mJet.setMuteFlag(24, false, false);
                        mDangerWillRobinson.removeElementAt(i);
                        continue;
                    } else {
                        asteroid.mMissed = true;
                        mHitStreak = mHitStreak - 1;
                        if (mHitStreak < 0)
                            mHitStreak = 0;
                    }
                }
                asteroid.mDrawX -= mPixelMoveX;
                asteroid.mAniIndex = (asteroid.mAniIndex + ANIMATION_FRAMES_PER_BEAT)
                        % mAsteroids.length;
                if (asteroid.mDrawX < 0) {
                    mDangerWillRobinson.removeElementAt(i);
                }
            }
        }
        protected void updateExplosions(Object inputContext) {
            if (mExplosion == null | mExplosion.size() == 0)
                return;
            for (int i = mExplosion.size() - 1; i >= 0; i--) {
                Explosion ex = mExplosion.elementAt(i);
                ex.mAniIndex += ANIMATION_FRAMES_PER_BEAT;
                if (ex.mAniIndex > 3) {
                    mJet.setMuteFlag(24, true, false);
                    mJet.setMuteFlag(23, true, false);
                    mExplosion.removeElementAt(i);
                }
            }
        }
        protected void processJetEvent(JetPlayer player, short segment, byte track, byte channel,
                byte controller, byte value) {
            if (value == NEW_ASTEROID_EVENT) {
                doAsteroidCreation();
            }
            mBeatCount++;
            if (mBeatCount > 4) {
                mBeatCount = 1;
            }
            if (mBeatCount == 1) {
                if (mHitStreak > 28) {
                    if (mCurrentBed != 7) {
                        if (mCurrentBed < 7) {
                            mJet.triggerClip(7);
                        }
                        mCurrentBed = 7;
                        mJet.setMuteArray(muteMask[7], false);
                    }
                } else if (mHitStreak > 24) {
                    if (mCurrentBed != 6) {
                        if (mCurrentBed < 6) {
                            mJet.triggerClip(6);
                        }
                        mCurrentBed = 6;
                        mJet.setMuteArray(muteMask[6], false);
                    }
                } else if (mHitStreak > 20) {
                    if (mCurrentBed != 5) {
                        if (mCurrentBed < 5) {
                            mJet.triggerClip(5);
                        }
                        mCurrentBed = 5;
                        mJet.setMuteArray(muteMask[5], false);
                    }
                } else if (mHitStreak > 16) {
                    if (mCurrentBed != 4) {
                        if (mCurrentBed < 4) {
                            mJet.triggerClip(4);
                        }
                        mCurrentBed = 4;
                        mJet.setMuteArray(muteMask[4], false);
                    }
                } else if (mHitStreak > 12) {
                    if (mCurrentBed != 3) {
                        if (mCurrentBed < 3) {
                            mJet.triggerClip(3);
                        }
                        mCurrentBed = 3;
                        mJet.setMuteArray(muteMask[3], false);
                    }
                } else if (mHitStreak > 8) {
                    if (mCurrentBed != 2) {
                        if (mCurrentBed < 2) {
                            mJet.triggerClip(2);
                        }
                        mCurrentBed = 2;
                        mJet.setMuteArray(muteMask[2], false);
                    }
                } else if (mHitStreak > 4) {
                    if (mCurrentBed != 1) {
                        if (mCurrentBed < 1) {
                            mJet.triggerClip(1);
                        }
                        mJet.setMuteArray(muteMask[1], false);
                        mCurrentBed = 1;
                    }
                }
            }
        }
        private void doAsteroidCreation() {
            Asteroid _as = new Asteroid();
            int drawIndex = mRandom.nextInt(4);
            _as.mDrawY = mAsteroidMinY + (drawIndex * 63);
            _as.mDrawX = (mCanvasWidth - mAsteroids[0].getWidth());
            _as.mStartTime = System.currentTimeMillis();
            mDangerWillRobinson.add(_as);
        }
        public void setRunning(boolean b) {
            mRun = b;
            if (mRun == false) {
                if (mTimerTask != null)
                    mTimerTask.cancel();
            }
        }
        public int getGameState() {
            synchronized (mSurfaceHolder) {
                return mState;
            }
        }
        public void setGameState(int mode) {
            synchronized (mSurfaceHolder) {
                setGameState(mode, null);
            }
        }
        public void setGameState(int state, CharSequence message) {
            synchronized (mSurfaceHolder) {
                if (mState != state) {
                    mState = state;
                }
                if (mState == STATE_PLAY) {
                    Resources res = mContext.getResources();
                    mBackgroundImageFar = BitmapFactory
                            .decodeResource(res, R.drawable.background_a);
                    mBackgroundImageFar = Bitmap.createScaledBitmap(mBackgroundImageFar,
                            mCanvasWidth * 2, mCanvasHeight, true);
                    mBackgroundImageNear = BitmapFactory.decodeResource(res,
                            R.drawable.background_b);
                    mBackgroundImageNear = Bitmap.createScaledBitmap(mBackgroundImageNear,
                            mCanvasWidth * 2, mCanvasHeight, true);
                } else if (mState == STATE_RUNNING) {
                    mEventQueue.clear();
                    mKeyContext = null;
                }
            }
        }
        public boolean doKeyDown(int keyCode, KeyEvent msg) {
            mEventQueue.add(new KeyGameEvent(keyCode, false, msg));
            return true;
        }
        public boolean doKeyUp(int keyCode, KeyEvent msg) {
            mEventQueue.add(new KeyGameEvent(keyCode, true, msg));
            return true;
        }
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                mBackgroundImageFar = Bitmap.createScaledBitmap(mBackgroundImageFar, width * 2,
                        height, true);
                mBackgroundImageNear = Bitmap.createScaledBitmap(mBackgroundImageNear, width * 2,
                        height, true);
            }
        }
        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mState == STATE_RUNNING)
                    setGameState(STATE_PAUSE);
                if (mTimerTask != null) {
                    mTimerTask.cancel();
                }
                if (mJet != null) {
                    mJet.pause();
                }
            }
        }
        private void doCountDown() {
            mTimerLimit = mTimerLimit - 1;
            try {
                int moreThanMinute = mTimerLimit - 60;
                if (moreThanMinute >= 0) {
                    if (moreThanMinute > 9) {
                        mTimerValue = "1:" + moreThanMinute;
                    }
                    else {
                        mTimerValue = "1:0" + moreThanMinute;
                    }
                } else {
                    if (mTimerLimit > 9) {
                        mTimerValue = "0:" + mTimerLimit;
                    } else {
                        mTimerValue = "0:0" + mTimerLimit;
                    }
                }
            } catch (Exception e1) {
                Log.e(TAG, "doCountDown threw " + e1.toString());
            }
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("text", mTimerValue);
            if (mTimerLimit == 0) {
                b.putString("STATE_LOSE", "" + STATE_LOSE);
                mTimerTask = null;
                mState = STATE_LOSE;
            } else {
                mTimerTask = new TimerTask() {
                    public void run() {
                        doCountDown();
                    }
                };
                mTimer.schedule(mTimerTask, mTaskIntervalInMillis);
            }
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
        public void onJetNumQueuedSegmentUpdate(JetPlayer player, int nbSegments) {
        }
        public void onJetEvent(JetPlayer player, short segment, byte track, byte channel,
                byte controller, byte value) {
            mEventQueue.add(new JetGameEvent(player, segment, track, channel, controller, value));
        }
        public void onJetPauseUpdate(JetPlayer player, int paused) {
        }
        public void onJetUserIdUpdate(JetPlayer player, int userId, int repeatCount) {
        }
    }
    public static final String TAG = "JetBoy";
    private JetBoyThread thread;
    private TextView mTimerView;
    private Button mButtonRetry;
    private TextView mTextView;
    public JetBoyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        if (isInEditMode() == false) {
            thread = new JetBoyThread(holder, context, new Handler() {
                public void handleMessage(Message m) {
                    mTimerView.setText(m.getData().getString("text"));
                    if (m.getData().getString("STATE_LOSE") != null) {
                        mButtonRetry.setVisibility(View.VISIBLE);
                        mTimerView.setVisibility(View.INVISIBLE);
                        mTextView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "the total was " + mHitTotal);
                        if (mHitTotal >= mSuccessThreshold) {
                            mTextView.setText(R.string.winText);
                        } else {
                            mTextView.setText("Sorry, You Lose! You got " + mHitTotal
                                    + ". You need 50 to win.");
                        }
                        mTimerView.setText("1:12");
                        mTextView.setHeight(20);
                    }
                }
            });
        }
        setFocusable(true); 
        Log.d(TAG, "@@@ done creating view!");
    }
    public void setTimerView(TextView tv) {
        mTimerView = tv;
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) {
            if (thread != null)
                thread.pause();
        }
    }
    public JetBoyThread getThread() {
        return thread;
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }
    public void surfaceCreated(SurfaceHolder arg0) {
        thread.setRunning(true);
        thread.start();
    }
    public void surfaceDestroyed(SurfaceHolder arg0) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    public void SetButtonView(Button _buttonRetry) {
        mButtonRetry = _buttonRetry;
    }
    public void SetTextView(TextView textView) {
        mTextView = textView;
    }
}
