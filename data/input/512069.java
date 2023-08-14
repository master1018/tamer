class MediaPlayerStateErrors {
    public static final int MEDIA_PLAYER_ERROR = 100;
    public static enum MediaPlayerState {
        IDLE,
        IDLE_AFTER_RESET,
        INITIALIZED,
        PREPARED,
        PREPARED_AFTER_STOP,
        STARTED,
        STARTED_AFTER_PAUSE,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        ERROR,
    }
    public boolean errorInIdleState = false;
    public boolean errorInIdleStateAfterReset = false;
    public boolean errorInInitializedState = false;
    public boolean errorInPreparedState = false;
    public boolean errorInStartedState = false;
    public boolean errorInPausedState = false;
    public boolean errorInStartedStateAfterPause = false;
    public boolean errorInStoppedState = false;
    public boolean errorInPreparedStateAfterStop = false;
    public boolean errorInPlaybackCompletedState = false;
    public boolean errorInErrorState = false;
}
