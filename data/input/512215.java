public class ContactMatcher {
    public static final int MAX_SCORE = 100;
    public static final int SCORE_THRESHOLD_SUGGEST = 50;
    public static final int SCORE_THRESHOLD_PRIMARY = 70;
    public static final int SCORE_THRESHOLD_SECONDARY = 50;
    private static final int NO_DATA_SCORE = -1;
    private static final int PHONE_MATCH_SCORE = 71;
    private static final int EMAIL_MATCH_SCORE = 71;
    private static final int NICKNAME_MATCH_SCORE = 71;
    private static final int MAX_MATCHED_NAME_LENGTH = 30;
    private static final int SCORE_SCALE = 1000;
    public static final int MATCHING_ALGORITHM_EXACT = 0;
    public static final int MATCHING_ALGORITHM_CONSERVATIVE = 1;
    public static final int MATCHING_ALGORITHM_APPROXIMATE = 2;
    public static final float APPROXIMATE_MATCH_THRESHOLD = 0.82f;
    public static final float APPROXIMATE_MATCH_THRESHOLD_FOR_EMAIL = 0.95f;
    private static int[] sMinScore =
            new int[NameLookupType.TYPE_COUNT * NameLookupType.TYPE_COUNT];
    private static int[] sMaxScore =
            new int[NameLookupType.TYPE_COUNT * NameLookupType.TYPE_COUNT];
    static {
        setScoreRange(NameLookupType.NAME_EXACT,
                NameLookupType.NAME_EXACT, 99, 99);
        setScoreRange(NameLookupType.NAME_VARIANT,
                NameLookupType.NAME_VARIANT, 90, 90);
        setScoreRange(NameLookupType.NAME_COLLATION_KEY,
                NameLookupType.NAME_COLLATION_KEY, 50, 80);
        setScoreRange(NameLookupType.NAME_COLLATION_KEY,
                NameLookupType.EMAIL_BASED_NICKNAME, 30, 60);
        setScoreRange(NameLookupType.NAME_COLLATION_KEY,
                NameLookupType.NICKNAME, 50, 60);
        setScoreRange(NameLookupType.EMAIL_BASED_NICKNAME,
                NameLookupType.EMAIL_BASED_NICKNAME, 50, 60);
        setScoreRange(NameLookupType.EMAIL_BASED_NICKNAME,
                NameLookupType.NAME_COLLATION_KEY, 50, 60);
        setScoreRange(NameLookupType.EMAIL_BASED_NICKNAME,
                NameLookupType.NICKNAME, 50, 60);
        setScoreRange(NameLookupType.NICKNAME,
                NameLookupType.NICKNAME, 50, 60);
        setScoreRange(NameLookupType.NICKNAME,
                NameLookupType.NAME_COLLATION_KEY, 50, 60);
        setScoreRange(NameLookupType.NICKNAME,
                NameLookupType.EMAIL_BASED_NICKNAME, 50, 60);
    }
    private static void setScoreRange(int candidateNameType, int nameType, int scoreFrom, int scoreTo) {
        int index = nameType * NameLookupType.TYPE_COUNT + candidateNameType;
        sMinScore[index] = scoreFrom;
        sMaxScore[index] = scoreTo;
    }
    private static int getMinScore(int candidateNameType, int nameType) {
        int index = nameType * NameLookupType.TYPE_COUNT + candidateNameType;
        return sMinScore[index];
    }
    private static int getMaxScore(int candidateNameType, int nameType) {
        int index = nameType * NameLookupType.TYPE_COUNT + candidateNameType;
        return sMaxScore[index];
    }
    public static class MatchScore implements Comparable<MatchScore> {
        private long mContactId;
        private boolean mKeepIn;
        private boolean mKeepOut;
        private int mPrimaryScore;
        private int mSecondaryScore;
        private int mMatchCount;
        public MatchScore(long contactId) {
            this.mContactId = contactId;
        }
        public void reset(long contactId) {
            this.mContactId = contactId;
            mKeepIn = false;
            mKeepOut = false;
            mPrimaryScore = 0;
            mSecondaryScore = 0;
            mMatchCount = 0;
        }
        public long getContactId() {
            return mContactId;
        }
        public void updatePrimaryScore(int score) {
            if (score > mPrimaryScore) {
                mPrimaryScore = score;
            }
            mMatchCount++;
        }
        public void updateSecondaryScore(int score) {
            if (score > mSecondaryScore) {
                mSecondaryScore = score;
            }
            mMatchCount++;
        }
        public void keepIn() {
            mKeepIn = true;
        }
        public void keepOut() {
            mKeepOut = true;
        }
        public int getScore() {
            if (mKeepOut) {
                return 0;
            }
            if (mKeepIn) {
                return MAX_SCORE;
            }
            int score = (mPrimaryScore > mSecondaryScore ? mPrimaryScore : mSecondaryScore);
            return score * SCORE_SCALE + mMatchCount;
        }
        public int compareTo(MatchScore another) {
            return another.getScore() - getScore();
        }
        @Override
        public String toString() {
            return mContactId + ": " + mPrimaryScore + "/" + mSecondaryScore + "(" + mMatchCount
                    + ")";
        }
    }
    private final HashMap<Long, MatchScore> mScores = new HashMap<Long, MatchScore>();
    private final ArrayList<MatchScore> mScoreList = new ArrayList<MatchScore>();
    private int mScoreCount = 0;
    private final NameDistance mNameDistanceConservative = new NameDistance();
    private final NameDistance mNameDistanceApproximate = new NameDistance(MAX_MATCHED_NAME_LENGTH);
    private MatchScore getMatchingScore(long contactId) {
        MatchScore matchingScore = mScores.get(contactId);
        if (matchingScore == null) {
            if (mScoreList.size() > mScoreCount) {
                matchingScore = mScoreList.get(mScoreCount);
                matchingScore.reset(contactId);
            } else {
                matchingScore = new MatchScore(contactId);
                mScoreList.add(matchingScore);
            }
            mScoreCount++;
            mScores.put(contactId, matchingScore);
        }
        return matchingScore;
    }
    public void matchName(long contactId, int candidateNameType, String candidateName,
            int nameType, String name, int algorithm) {
        int maxScore = getMaxScore(candidateNameType, nameType);
        if (maxScore == 0) {
            return;
        }
        if (candidateName.equals(name)) {
            updatePrimaryScore(contactId, maxScore);
            return;
        }
        if (algorithm == MATCHING_ALGORITHM_EXACT) {
            return;
        }
        int minScore = getMinScore(candidateNameType, nameType);
        if (minScore == maxScore) {
            return;
        }
        byte[] decodedCandidateName = Hex.decodeHex(candidateName);
        byte[] decodedName = Hex.decodeHex(name);
        NameDistance nameDistance = algorithm == MATCHING_ALGORITHM_CONSERVATIVE ?
                mNameDistanceConservative : mNameDistanceApproximate;
        int score;
        float distance = nameDistance.getDistance(decodedCandidateName, decodedName);
        boolean emailBased = candidateNameType == NameLookupType.EMAIL_BASED_NICKNAME
                || nameType == NameLookupType.EMAIL_BASED_NICKNAME;
        float threshold = emailBased
                ? APPROXIMATE_MATCH_THRESHOLD_FOR_EMAIL
                : APPROXIMATE_MATCH_THRESHOLD;
        if (distance > threshold) {
            score = (int)(minScore +  (maxScore - minScore) * (1.0f - distance));
        } else {
            score = 0;
        }
        updatePrimaryScore(contactId, score);
    }
    public void updateScoreWithPhoneNumberMatch(long contactId) {
        updateSecondaryScore(contactId, PHONE_MATCH_SCORE);
    }
    public void updateScoreWithEmailMatch(long contactId) {
        updateSecondaryScore(contactId, EMAIL_MATCH_SCORE);
    }
    public void updateScoreWithNicknameMatch(long contactId) {
        updateSecondaryScore(contactId, NICKNAME_MATCH_SCORE);
    }
    private void updatePrimaryScore(long contactId, int score) {
        getMatchingScore(contactId).updatePrimaryScore(score);
    }
    private void updateSecondaryScore(long contactId, int score) {
        getMatchingScore(contactId).updateSecondaryScore(score);
    }
    public void keepIn(long contactId) {
        getMatchingScore(contactId).keepIn();
    }
    public void keepOut(long contactId) {
        getMatchingScore(contactId).keepOut();
    }
    public void clear() {
        mScores.clear();
        mScoreCount = 0;
    }
    public List<Long> prepareSecondaryMatchCandidates(int threshold) {
        ArrayList<Long> contactIds = null;
        for (int i = 0; i < mScoreCount; i++) {
            MatchScore score = mScoreList.get(i);
            if (score.mKeepOut) {
                continue;
            }
            int s = score.mSecondaryScore;
            if (s >= threshold) {
                if (contactIds == null) {
                    contactIds = new ArrayList<Long>();
                }
                contactIds.add(score.mContactId);
            }
            score.mPrimaryScore = NO_DATA_SCORE;
        }
        return contactIds;
    }
    public long pickBestMatch(int threshold) {
        long contactId = -1;
        int maxScore = 0;
        for (int i = 0; i < mScoreCount; i++) {
            MatchScore score = mScoreList.get(i);
            if (score.mKeepOut) {
                continue;
            }
            if (score.mKeepIn) {
                return score.mContactId;
            }
            int s = score.mPrimaryScore;
            if (s == NO_DATA_SCORE) {
                s = score.mSecondaryScore;
            }
            if (s >= threshold && s > maxScore) {
                contactId = score.mContactId;
                maxScore = s;
            }
        }
        return contactId;
    }
    public List<MatchScore> pickBestMatches(int threshold) {
        int scaledThreshold = threshold * SCORE_SCALE;
        List<MatchScore> matches = mScoreList.subList(0, mScoreCount);
        Collections.sort(matches);
        int count = 0;
        for (int i = 0; i < mScoreCount; i++) {
            MatchScore matchScore = matches.get(i);
            if (matchScore.getScore() >= scaledThreshold) {
                count++;
            } else {
                break;
            }
        }
        return matches.subList(0, count);
    }
    @Override
    public String toString() {
        return mScoreList.toString();
    }
}
