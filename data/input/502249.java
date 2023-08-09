public class PhoneTypeChoiceRecognizerEngine extends RecognizerEngine {
    public PhoneTypeChoiceRecognizerEngine() {
    }
    protected void setupGrammar() throws IOException, InterruptedException {
        if (mSrecGrammar == null) {
            if (Config.LOGD) Log.d(TAG, "start new Grammar");
            mSrecGrammar = mSrec.new Grammar(SREC_DIR + "/grammars/phone_type_choice.g2g");
            mSrecGrammar.setupRecognizer();
        }
    }
    protected void onRecognitionSuccess(RecognizerClient recognizerClient) throws InterruptedException {
        if (Config.LOGD) Log.d(TAG, "onRecognitionSuccess " + mSrec.getResultCount());
        if (mLogger != null) mLogger.logNbestHeader();
        ArrayList<Intent> intents = new ArrayList<Intent>();
        for (int result = 0; result < mSrec.getResultCount() &&
                intents.size() < RESULT_LIMIT; result++) {
            String conf = mSrec.getResult(result, Recognizer.KEY_CONFIDENCE);
            String literal = mSrec.getResult(result, Recognizer.KEY_LITERAL);
            String semantic = mSrec.getResult(result, Recognizer.KEY_MEANING);
            String msg = "conf=" + conf + " lit=" + literal + " sem=" + semantic;
            if (Config.LOGD) Log.d(TAG, msg);
        }
        if (mSrec.getResultCount() > 0) {
            String conf = mSrec.getResult(0, Recognizer.KEY_CONFIDENCE);
            String literal = mSrec.getResult(0, Recognizer.KEY_LITERAL);
            String semantic = mSrec.getResult(0, Recognizer.KEY_MEANING);
            String msg = "conf=" + conf + " lit=" + literal + " sem=" + semantic;
            if (Config.LOGD) Log.d(TAG, msg);
            if (mLogger != null) mLogger.logLine(msg);
            if (("H".equalsIgnoreCase(semantic)) ||
                ("M".equalsIgnoreCase(semantic)) ||
                ("W".equalsIgnoreCase(semantic)) ||
                ("O".equalsIgnoreCase(semantic)) ||
                ("R".equalsIgnoreCase(semantic)) ||
                ("X".equalsIgnoreCase(semantic))) {
                if (Config.LOGD) Log.d(TAG, " got valid response");
                Intent intent = new Intent(RecognizerEngine.ACTION_RECOGNIZER_RESULT, null);
                intent.putExtra(RecognizerEngine.SENTENCE_EXTRA, literal);
                intent.putExtra(RecognizerEngine.SEMANTIC_EXTRA, semantic);
                addIntent(intents, intent);
            } else {
            }
        }
        if (mLogger != null) mLogger.logIntents(intents);
        if (Thread.interrupted()) throw new InterruptedException();
        if (intents.size() == 0) {
            if (Config.LOGD) Log.d(TAG, " no intents");
            recognizerClient.onRecognitionFailure("No Intents generated");
        }
        else {
            if (Config.LOGD) Log.d(TAG, " success");
            recognizerClient.onRecognitionSuccess(
                    intents.toArray(new Intent[intents.size()]));
        }
    }
}
