   public class CdmaPhoneCallState {
        public enum PhoneCallState {
            IDLE,
            SINGLE_ACTIVE,
            THRWAY_ACTIVE,
            CONF_CALL
        }
        private PhoneCallState mPreviousCallState;
        private PhoneCallState mCurrentCallState;
        private boolean mThreeWayCallOrigStateDialing;
        private boolean mAddCallMenuStateAfterCW;
        public void CdmaPhoneCallStateInit() {
            mCurrentCallState = PhoneCallState.IDLE;
            mPreviousCallState = PhoneCallState.IDLE;
            mThreeWayCallOrigStateDialing = false;
            mAddCallMenuStateAfterCW = true;
        }
        public PhoneCallState getCurrentCallState() {
            return mCurrentCallState;
        }
        public void setCurrentCallState(PhoneCallState newState) {
            mPreviousCallState = mCurrentCallState;
            mCurrentCallState = newState;
            mThreeWayCallOrigStateDialing = false;
            if ((mCurrentCallState == PhoneCallState.SINGLE_ACTIVE)
                && (mPreviousCallState == PhoneCallState.IDLE)) {
                mAddCallMenuStateAfterCW = true;
            }
        }
        public boolean IsThreeWayCallOrigStateDialing() {
            return mThreeWayCallOrigStateDialing;
        }
        public void setThreeWayCallOrigState(boolean newState) {
            mThreeWayCallOrigStateDialing = newState;
        }
        public boolean getAddCallMenuStateAfterCallWaiting() {
            return mAddCallMenuStateAfterCW;
        }
        public void setAddCallMenuStateAfterCallWaiting(boolean newState) {
            mAddCallMenuStateAfterCW = newState;
        }
        public PhoneCallState getPreviousCallState() {
            return mPreviousCallState;
        }
        public void resetCdmaPhoneCallState() {
            mCurrentCallState = PhoneCallState.IDLE;
            mPreviousCallState = PhoneCallState.IDLE;
            mThreeWayCallOrigStateDialing = false;
            mAddCallMenuStateAfterCW = true;
        }
   }
