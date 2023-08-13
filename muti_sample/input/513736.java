    public boolean isWakeRelMovementTq(int device, int classes,
            RawInputEvent event);
    public boolean isWakeAbsMovementTq(int device, int classes,
            RawInputEvent event);
    public void enableKeyguard(boolean enabled);
    interface OnKeyguardExitResult {
        void onKeyguardExitResult(boolean success);
    }
    void exitKeyguardSecurely(OnKeyguardExitResult callback);
    public boolean inKeyguardRestrictedKeyInputMode();
    public int rotationForOrientationLw(int orientation, int lastRotation,
            boolean displayEnabled);
    public boolean detectSafeMode();
    public void systemReady();
    public void userActivity();
    public void enableScreenAfterBoot();
    public boolean isCheekPressedAgainstScreen(MotionEvent ev);
    public void dispatchedPointerEventLw(MotionEvent ev, int targetX, int targetY);
    public void setCurrentOrientationLw(int newOrientation);
    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always);
    public void keyFeedbackFromInput(KeyEvent event);
    public void screenOnStoppedLw();
    public boolean allowKeyRepeat();
}
