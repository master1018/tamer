public final class RuimCard extends IccCard {
    RuimCard(CDMAPhone phone) {
        super(phone, "CDMA", true);
        mPhone.mCM.registerForRUIMLockedOrAbsent(mHandler, EVENT_ICC_LOCKED_OR_ABSENT, null);
        mPhone.mCM.registerForOffOrNotAvailable(mHandler, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        mPhone.mCM.registerForRUIMReady(mHandler, EVENT_ICC_READY, null);
        updateStateProperty();
    }
    @Override
    public void dispose() {
        mPhone.mCM.unregisterForRUIMLockedOrAbsent(mHandler);
        mPhone.mCM.unregisterForOffOrNotAvailable(mHandler);
        mPhone.mCM.unregisterForRUIMReady(mHandler);
    }
    @Override
    public String getServiceProviderName () {
        return ((CDMAPhone)mPhone).mRuimRecords.getServiceProviderName();
    }
 }
