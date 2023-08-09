public class OpenWnnDictionaryImpl implements WnnDictionary {
    static {
        System.loadLibrary( "wnndict" );
    }
    public static final int MAX_STROKE_LENGTH       = 50;
    public static final int MAX_CANDIDATE_LENGTH    = 50;
    protected static final String TABLE_NAME_DIC    = "dic";
    protected static final int TYPE_NAME_USER   = 0;
    protected static final int TYPE_NAME_LEARN  = 1;
    protected static final String COLUMN_NAME_ID                 = "rowid";
    protected static final String COLUMN_NAME_TYPE               = "type";
    protected static final String COLUMN_NAME_STROKE             = "stroke";
    protected static final String COLUMN_NAME_CANDIDATE          = "candidate";
    protected static final String COLUMN_NAME_POS_LEFT           = "posLeft";
    protected static final String COLUMN_NAME_POS_RIGHT          = "posRight";
    protected static final String COLUMN_NAME_PREVIOUS_STROKE    = "prevStroke";
    protected static final String COLUMN_NAME_PREVIOUS_CANDIDATE = "prevCandidate";
    protected static final String COLUMN_NAME_PREVIOUS_POS_LEFT  = "prevPosLeft";
    protected static final String COLUMN_NAME_PREVIOUS_POS_RIGHT = "prevPosRight";
    protected static final String NORMAL_QUERY =
        "select distinct " + COLUMN_NAME_STROKE + "," +
                             COLUMN_NAME_CANDIDATE + "," +
                             COLUMN_NAME_POS_LEFT + "," +
                             COLUMN_NAME_POS_RIGHT + "," +
                             COLUMN_NAME_TYPE +
                  " from " + TABLE_NAME_DIC + " where %s order by " +
                             COLUMN_NAME_TYPE + " DESC, %s";
    protected static final String LINK_QUERY =
        "select distinct " + COLUMN_NAME_STROKE + "," +
                             COLUMN_NAME_CANDIDATE + "," +
                             COLUMN_NAME_POS_LEFT + "," +
                             COLUMN_NAME_POS_RIGHT + "," +
                             COLUMN_NAME_TYPE +
                  " from " + TABLE_NAME_DIC + " where %s = ? and %s = ? and %s order by " +
                             COLUMN_NAME_TYPE + " DESC, %s";
    protected static final int MAX_WORDS_IN_USER_DICTIONARY     = 100;
    protected static final int MAX_WORDS_IN_LEARN_DICTIONARY    = 2000;
    protected static final int OFFSET_FREQUENCY_OF_USER_DICTIONARY  = 1000;
    protected static final int OFFSET_FREQUENCY_OF_LEARN_DICTIONARY = 2000;
    protected final static int MAX_PATTERN_OF_APPROX    = 6;
    protected final static int MAX_LENGTH_OF_QUERY      = 50;
    protected final static int FAST_QUERY_LENGTH        = 20;
    protected long mWnnWork = 0;
    protected String mDicFilePath = "";
    protected SQLiteDatabase mDbDic = null;
    protected SQLiteCursor mDbCursor = null;
    protected int mCountCursor = 0;
    protected int mTypeOfQuery = -1;
    protected String mExactQuerySqlOrderByFreq;
    protected String mExactQuerySqlOrderByKey;
    protected String mFullPrefixQuerySqlOrderByFreq;
    protected String mFastPrefixQuerySqlOrderByFreq;
    protected String mFullPrefixQuerySqlOrderByKey;
    protected String mFastPrefixQuerySqlOrderByKey;
    protected String mFullLinkQuerySqlOrderByFreq;
    protected String mFastLinkQuerySqlOrderByFreq;
    protected String mFullLinkQuerySqlOrderByKey;
    protected String mFastLinkQuerySqlOrderByKey;
    protected String mExactQueryArgs[] = new String[ 1 ];
    protected String mFullQueryArgs[] = new String[ MAX_LENGTH_OF_QUERY * (MAX_PATTERN_OF_APPROX+1) ];
    protected String mFastQueryArgs[] = new String[ FAST_QUERY_LENGTH * (MAX_PATTERN_OF_APPROX+1) ];
    protected int mFrequencyOffsetOfUserDictionary = -1;
    protected int mFrequencyOffsetOfLearnDictionary = -1;
    public OpenWnnDictionaryImpl( String dicLibPath ) {
        this( dicLibPath, null );
    }
    public OpenWnnDictionaryImpl( String dicLibPath, String dicFilePath ) {
        this.mWnnWork = OpenWnnDictionaryImplJni.createWnnWork( dicLibPath );
        if( this.mWnnWork != 0 && dicFilePath != null ) {
            String queryFullBaseString = 
                OpenWnnDictionaryImplJni.createQueryStringBase(
                    this.mWnnWork,
                    MAX_LENGTH_OF_QUERY,
                    MAX_PATTERN_OF_APPROX,
                    COLUMN_NAME_STROKE );
            String queryFastBaseString = 
                OpenWnnDictionaryImplJni.createQueryStringBase(
                    this.mWnnWork,
                    FAST_QUERY_LENGTH,
                    MAX_PATTERN_OF_APPROX,
                    COLUMN_NAME_STROKE );
            mExactQuerySqlOrderByFreq = String.format(
                NORMAL_QUERY,
                String.format( "%s=?", COLUMN_NAME_STROKE ), String.format( "%s DESC", COLUMN_NAME_ID ) );
            mExactQuerySqlOrderByKey = String.format(
                NORMAL_QUERY,
                String.format( "%s=?", COLUMN_NAME_STROKE ), COLUMN_NAME_STROKE );
            mFullPrefixQuerySqlOrderByFreq = String.format(
                NORMAL_QUERY,
                queryFullBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );
            mFastPrefixQuerySqlOrderByFreq = String.format(
                NORMAL_QUERY,
                queryFastBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );
            mFullPrefixQuerySqlOrderByKey = String.format(
                NORMAL_QUERY,
                queryFullBaseString, COLUMN_NAME_STROKE );
            mFastPrefixQuerySqlOrderByKey = String.format(
                NORMAL_QUERY,
                queryFastBaseString, COLUMN_NAME_STROKE );
            mFullLinkQuerySqlOrderByFreq = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFullBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );
            mFastLinkQuerySqlOrderByFreq = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFastBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );
            mFullLinkQuerySqlOrderByKey = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFullBaseString, COLUMN_NAME_STROKE );
            mFastLinkQuerySqlOrderByKey = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFastBaseString, COLUMN_NAME_STROKE );
            try {
                mDicFilePath = dicFilePath;
                setInUseState( true );
                createDictionaryTable( TABLE_NAME_DIC );
            } catch( SQLException e ) {
            }
        }
    }
    protected void finalize( ) {
        if( this.mWnnWork != 0 ) {
            OpenWnnDictionaryImplJni.freeWnnWork( this.mWnnWork );
            this.mWnnWork = 0;
            freeDatabase();
        }
    }
    protected void createDictionaryTable( String tableName ) {
        String sqlStr = "create table if not exists " + tableName +
            " (" + COLUMN_NAME_ID                 + " integer primary key autoincrement, " + 
                   COLUMN_NAME_TYPE               + " integer, " +
                   COLUMN_NAME_STROKE             + " text, " + 
                   COLUMN_NAME_CANDIDATE          + " text, " +
                   COLUMN_NAME_POS_LEFT           + " integer, " +
                   COLUMN_NAME_POS_RIGHT          + " integer, " +
                   COLUMN_NAME_PREVIOUS_STROKE    + " text, " + 
                   COLUMN_NAME_PREVIOUS_CANDIDATE + " text, " +
                   COLUMN_NAME_PREVIOUS_POS_LEFT  + " integer, " +
                   COLUMN_NAME_PREVIOUS_POS_RIGHT + " integer)";
        if( mDbDic != null ) {
            mDbDic.execSQL( sqlStr );
        }
    }
    protected void freeDatabase( ) {
        freeCursor();
        if( mDbDic != null ) {
            mDbDic.close();
            mDbDic = null;
        }
    }
    protected void freeCursor( ) {
        if( mDbCursor != null) {
            mDbCursor.close();
            mDbCursor = null;
            mTypeOfQuery = -1;
        }
    }
    public boolean isActive() {
        return (this.mWnnWork != 0);
    }
    public void setInUseState( boolean flag ) {
        if( flag ) {
            if( mDbDic == null ) {
                mDbDic = SQLiteDatabase.openOrCreateDatabase( mDicFilePath, null );
            }
        } else {
            freeDatabase();
        }
    }
    public int clearDictionary( ) {
        if( this.mWnnWork != 0 ) {
            mFrequencyOffsetOfUserDictionary  = -1;
            mFrequencyOffsetOfLearnDictionary = -1;
            return OpenWnnDictionaryImplJni.clearDictionaryParameters( this.mWnnWork );
        } else {
            return -1;
        }
    }
    public int setDictionary(int index, int base, int high ) {
        if( this.mWnnWork != 0 ) {
            switch( index ) {
            case WnnDictionary.INDEX_USER_DICTIONARY:
                if( base < 0 || high < 0 || base > high
                     ) {
                    mFrequencyOffsetOfUserDictionary = -1;
                } else {
                    mFrequencyOffsetOfUserDictionary = high;
                }
                return 0;
            case WnnDictionary.INDEX_LEARN_DICTIONARY:
                if( base < 0 || high < 0 || base > high
                     ) {
                    mFrequencyOffsetOfLearnDictionary = -1;
                } else {
                    mFrequencyOffsetOfLearnDictionary = high;
                }
                return 0;
            default:
                return OpenWnnDictionaryImplJni.setDictionaryParameter( this.mWnnWork, index, base, high );
            }
        } else {
            return -1;
        }
    }
    protected void createQuery( String keyString, WnnWord wnnWord, int operation, int order) {
        int newTypeOfQuery, maxBindsOfQuery;
        String querySqlOrderByFreq, querySqlOrderByKey;
        String queryArgs[];
        if( operation != WnnDictionary.SEARCH_LINK ) {
            wnnWord = null;
        }
        switch( operation ) {
        case WnnDictionary.SEARCH_EXACT:
            querySqlOrderByFreq = mExactQuerySqlOrderByFreq; 
            querySqlOrderByKey  = mExactQuerySqlOrderByKey;
            newTypeOfQuery      = 0;
            queryArgs           = mExactQueryArgs;
            queryArgs[ 0 ]      = keyString;
            break;
        case WnnDictionary.SEARCH_PREFIX:
        case WnnDictionary.SEARCH_LINK:
            if( keyString.length() <= FAST_QUERY_LENGTH ) {
                if( wnnWord != null ) {
                    querySqlOrderByFreq = mFastLinkQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFastLinkQuerySqlOrderByKey;
                    newTypeOfQuery      = 1;
                } else {
                    querySqlOrderByFreq = mFastPrefixQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFastPrefixQuerySqlOrderByKey;
                    newTypeOfQuery      = 2;
                }
                maxBindsOfQuery     = FAST_QUERY_LENGTH;
                queryArgs           = mFastQueryArgs;
            } else {
                if( wnnWord != null ) {
                    querySqlOrderByFreq = mFullLinkQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFullLinkQuerySqlOrderByKey;
                    newTypeOfQuery      = 3;
                } else {
                    querySqlOrderByFreq = mFullPrefixQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFullPrefixQuerySqlOrderByKey;
                    newTypeOfQuery      = 4;
                }
                maxBindsOfQuery     = MAX_LENGTH_OF_QUERY;
                queryArgs           = mFullQueryArgs;
            }
            if( wnnWord != null ) {
                String[] queryArgsTemp = OpenWnnDictionaryImplJni.createBindArray( this.mWnnWork, keyString, maxBindsOfQuery, MAX_PATTERN_OF_APPROX );
                queryArgs = new String[ queryArgsTemp.length + 2 ];
                for( int i = 0 ; i < queryArgsTemp.length ; i++ ) {
                    queryArgs[ i + 2 ] = queryArgsTemp[ i ];
                }
                queryArgs[ 0 ] = wnnWord.stroke;
                queryArgs[ 1 ] = wnnWord.candidate;
            } else {
                queryArgs = OpenWnnDictionaryImplJni.createBindArray( this.mWnnWork, keyString, maxBindsOfQuery, MAX_PATTERN_OF_APPROX );
            }
            break;
        default:
            mCountCursor = 0;
            freeCursor( );
            return;
        }
        mCountCursor = 0;
        if( mDbCursor == null || mTypeOfQuery != newTypeOfQuery ) {
            freeCursor( );
            try {
                switch( order ) {
                case WnnDictionary.ORDER_BY_FREQUENCY:
                    mDbCursor = ( SQLiteCursor )mDbDic.rawQuery( querySqlOrderByFreq, queryArgs );
                    break;
                case WnnDictionary.ORDER_BY_KEY:
                    mDbCursor = ( SQLiteCursor )mDbDic.rawQuery( querySqlOrderByKey, queryArgs );
                    break;
                default:
                    return;
                }
            } catch( SQLException e ) {
                return;
            }
            mTypeOfQuery = newTypeOfQuery;
        } else {
            try {
                mDbCursor.setSelectionArguments( queryArgs );
                mDbCursor.requery( );
            } catch( SQLException e ) {
                return;
            }
        }
        if( mDbCursor != null ) {
            mCountCursor = mDbCursor.getCount();
            if( mCountCursor == 0 ) {
                mDbCursor.deactivate( );
            }
        }
        return;
    }
    public int searchWord( int operation, int order, String keyString ) {
        OpenWnnDictionaryImplJni.clearResult( this.mWnnWork );
        if( mDbDic != null && ( mFrequencyOffsetOfUserDictionary  >= 0 ||
                                mFrequencyOffsetOfLearnDictionary >= 0 ) ) {
            try {
                if( keyString.length() > 0 ) {
                    createQuery( keyString, null, operation, order );
                    if( mDbCursor != null ) {
                        mDbCursor.moveToFirst();
                    }
                } else {
                    if( mDbCursor != null ) {
                        mDbCursor.deactivate();
                    }
                    mCountCursor = 0;
                }
            } catch( SQLException e ) {
                if( mDbCursor != null ) {
                    mDbCursor.deactivate();
                }
                mCountCursor = 0;
            }
        } else {
            mCountCursor = 0;
        }
        if( this.mWnnWork != 0 ) {
            int ret = OpenWnnDictionaryImplJni.searchWord( this.mWnnWork, operation, order, keyString );
            if (mCountCursor > 0) {
                ret = 1;
            }
            return ret;
        } else {
            return -1;
        }
    }
    public int searchWord( int operation, int order, String keyString, WnnWord wnnWord ) {
        if( wnnWord == null || wnnWord.partOfSpeech == null ) {
            return -1;
        }
        if( mDbDic != null && ( mFrequencyOffsetOfUserDictionary  >= 0 ||
                                mFrequencyOffsetOfLearnDictionary >= 0 ) ) {
            try {
                createQuery( keyString, wnnWord, operation, order );
                if( mDbCursor != null ) {
                    mDbCursor.moveToFirst();
                }
            } catch( SQLException e ) {
                if( mDbCursor != null ) {
                    mDbCursor.deactivate();
                }
                mCountCursor = 0;
            }
        } else {
            mCountCursor = 0;
        }
        OpenWnnDictionaryImplJni.clearResult( this.mWnnWork );
        OpenWnnDictionaryImplJni.setStroke( this.mWnnWork, wnnWord.stroke );
        OpenWnnDictionaryImplJni.setCandidate( this.mWnnWork, wnnWord.candidate );
        OpenWnnDictionaryImplJni.setLeftPartOfSpeech( this.mWnnWork, wnnWord.partOfSpeech.left );
        OpenWnnDictionaryImplJni.setRightPartOfSpeech( this.mWnnWork, wnnWord.partOfSpeech.right );
        OpenWnnDictionaryImplJni.selectWord( this.mWnnWork );
        if( this.mWnnWork != 0 ) {
            int ret = OpenWnnDictionaryImplJni.searchWord( this.mWnnWork, operation, order, keyString );
            if (mCountCursor > 0) {
                ret = 1;
            }
            return ret;
        } else {
            return -1;
        }
    }
    public WnnWord getNextWord( ) {
        return getNextWord( 0 );
    }
    public WnnWord getNextWord( int length ) {
        if( this.mWnnWork != 0 ) {
            if( mDbDic != null && mDbCursor != null && mCountCursor > 0 ) {
                WnnWord result = new WnnWord( );
                try {
                    while( mCountCursor > 0 &&
                           ( ( mFrequencyOffsetOfUserDictionary < 0  && mDbCursor.getInt( 4 ) == TYPE_NAME_USER      ) ||
                             ( mFrequencyOffsetOfLearnDictionary < 0 && mDbCursor.getInt( 4 ) == TYPE_NAME_LEARN     ) ||
                             ( length > 0                            && mDbCursor.getString( 0 ).length( ) != length ) ) ) {
                        mDbCursor.moveToNext();
                        mCountCursor--;
                    }
                    if( mCountCursor > 0 ) {
                        result.stroke               = mDbCursor.getString( 0 );
                        result.candidate            = mDbCursor.getString( 1 );
                        result.partOfSpeech.left    = mDbCursor.getInt( 2 );
                        result.partOfSpeech.right   = mDbCursor.getInt( 3 );
                        if( mDbCursor.getInt( 4 ) == TYPE_NAME_USER ) {
                            result.frequency        = mFrequencyOffsetOfUserDictionary;
                        } else {
                            result.frequency        = mFrequencyOffsetOfLearnDictionary;
                        }
                        mDbCursor.moveToNext();
                        if( --mCountCursor <= 0 ) {
                            mDbCursor.deactivate();
                        }
                        return result;
                    } else {
                        mDbCursor.deactivate();
                        result = null;
                    }
                } catch( SQLException e ) {
                    mDbCursor.deactivate();
                    mCountCursor = 0;
                    result = null;
                }
            }
            int res = OpenWnnDictionaryImplJni.getNextWord( this.mWnnWork, length );
            if( res > 0 ) {
                WnnWord result = new WnnWord( );
                if( result != null ) {
                    result.stroke               = OpenWnnDictionaryImplJni.getStroke( this.mWnnWork );
                    result.candidate            = OpenWnnDictionaryImplJni.getCandidate( this.mWnnWork );
                    result.frequency            = OpenWnnDictionaryImplJni.getFrequency( this.mWnnWork );
                    result.partOfSpeech.left    = OpenWnnDictionaryImplJni.getLeftPartOfSpeech( this.mWnnWork );
                    result.partOfSpeech.right   = OpenWnnDictionaryImplJni.getRightPartOfSpeech( this.mWnnWork );
                }
                return result;
            } else if ( res == 0 ) {
                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    public WnnWord[] getUserDictionaryWords( ) {
        if( this.mWnnWork != 0 && mDbDic != null ) {
            int numOfWords, i;
            SQLiteCursor cursor = null;
            try {
                cursor = ( SQLiteCursor )mDbDic.query(
                    TABLE_NAME_DIC,
                    new String[] { COLUMN_NAME_STROKE, COLUMN_NAME_CANDIDATE },
                    String.format( "%s=%d", COLUMN_NAME_TYPE, TYPE_NAME_USER ),
                    null, null, null, null);
                numOfWords = cursor.getCount();
                if( numOfWords > 0 ) {
                    WnnWord[] words = new WnnWord[ numOfWords ];
                    cursor.moveToFirst();
                    for( i = 0 ; i < numOfWords ; i++ ) {
                        words[ i ] = new WnnWord();
                        words[ i ].stroke       = cursor.getString( 0 );
                        words[ i ].candidate    = cursor.getString( 1 );
                        cursor.moveToNext();
                    }
                    return words;
                }
            } catch( SQLException e ) {
                return null;
            } finally {
                if( cursor != null ) {
                    cursor.close( );
                }
            }
        }
        return null;
    }
    public void clearApproxPattern( ) {
        if( this.mWnnWork != 0 ) {
            OpenWnnDictionaryImplJni.clearApproxPatterns( this.mWnnWork );
        }
    }
    public int setApproxPattern( String src, String dst ) {
        if( this.mWnnWork != 0 ) {
            return OpenWnnDictionaryImplJni.setApproxPattern( this.mWnnWork, src, dst );
        } else {
            return -1;
        }
    }
    public int setApproxPattern( int approxPattern ) {
        if( this.mWnnWork != 0 ) {
            return OpenWnnDictionaryImplJni.setApproxPattern( this.mWnnWork, approxPattern );
        } else {
            return -1;
        }
    }
    public byte[][] getConnectMatrix( ) {
        byte[][]    result;
        int         lcount, i;
        if (this.mWnnWork != 0) {
            lcount = OpenWnnDictionaryImplJni.getNumberOfLeftPOS( this.mWnnWork );
            result = new byte[ lcount + 1 ][ ];
            if( result != null ) {
                for( i = 0 ; i < lcount + 1 ; i++ ) {
                    result[ i ] = OpenWnnDictionaryImplJni.getConnectArray( this.mWnnWork, i );
                    if( result[ i ] == null ) {
                        return null;
                    }
                }
            }
        } else {
            result = new byte[1][1];
        }
        return result;
    }
    public WnnPOS getPOS( int type ) {
        WnnPOS result = new WnnPOS( );
        if( this.mWnnWork != 0 && result != null ) {
            result.left  = OpenWnnDictionaryImplJni.getLeftPartOfSpeechSpecifiedType( this.mWnnWork, type );
            result.right = OpenWnnDictionaryImplJni.getRightPartOfSpeechSpecifiedType( this.mWnnWork, type );
            if( result.left < 0 || result.right < 0 ) {
                return null;
            }
        }
        return result;
    }
    public int clearUserDictionary() {
        if( mDbDic != null ) {
            mDbDic.execSQL( String.format( "delete from %s where %s=%d", TABLE_NAME_DIC, COLUMN_NAME_TYPE, TYPE_NAME_USER ) );
        }
        return 0;
    }
    public int clearLearnDictionary() {
        if( mDbDic != null ) {
            mDbDic.execSQL( String.format( "delete from %s where %s=%d", TABLE_NAME_DIC, COLUMN_NAME_TYPE, TYPE_NAME_LEARN ) );
        }
        return 0;
    }
    public int addWordToUserDictionary( WnnWord[] word ) {
        int result = 0;
        if( mDbDic != null ) {
            SQLiteCursor cursor;
            cursor = ( SQLiteCursor )mDbDic.query(
                TABLE_NAME_DIC,
                new String[] { COLUMN_NAME_ID },
                String.format( "%s=%d", COLUMN_NAME_TYPE, TYPE_NAME_USER ),
                null, null, null, null);
            int count = cursor.getCount();
            cursor.close();
            if( count + word.length > MAX_WORDS_IN_USER_DICTIONARY ) {
                return -1;
            } else {
                mDbDic.beginTransaction();
                try {
                    StringBuilder strokeSQL    = new StringBuilder();
                    StringBuilder candidateSQL = new StringBuilder();
                    for( int index = 0 ; index < word.length ; index++ ) {
                        if( word[index].stroke.length()    > 0 && word[index].stroke.length()    <= MAX_STROKE_LENGTH &&
                            word[index].candidate.length() > 0 && word[index].candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                            strokeSQL.setLength( 0 );
                            candidateSQL.setLength( 0 );
                            DatabaseUtils.appendEscapedSQLString( strokeSQL, word[index].stroke );
                            DatabaseUtils.appendEscapedSQLString( candidateSQL, word[index].candidate );
                            cursor = ( SQLiteCursor )mDbDic.query(
                                TABLE_NAME_DIC,
                                new String[] { COLUMN_NAME_ID },
                                String.format( "%s=%d and %s=%s and %s=%s",
                                               COLUMN_NAME_TYPE, TYPE_NAME_USER,
                                               COLUMN_NAME_STROKE, strokeSQL.toString(),
                                               COLUMN_NAME_CANDIDATE, candidateSQL.toString() ),
                                null, null, null, null );
                            if( cursor.getCount() > 0 ) {
                                result = -2;
                            } else {
                                ContentValues content = new ContentValues();
                                content.clear();
                                content.put( COLUMN_NAME_TYPE,      TYPE_NAME_USER );
                                content.put( COLUMN_NAME_STROKE,    word[index].stroke );
                                content.put( COLUMN_NAME_CANDIDATE, word[index].candidate );
                                content.put( COLUMN_NAME_POS_LEFT,  word[index].partOfSpeech.left );
                                content.put( COLUMN_NAME_POS_RIGHT, word[index].partOfSpeech.right );
                                mDbDic.insert( TABLE_NAME_DIC, null, content );
                            }
                            cursor.close( );
                            cursor = null;
                        }
                    }
                    mDbDic.setTransactionSuccessful();
                } catch( SQLException e ) {
                    return -1;
                } finally {
                    mDbDic.endTransaction();
                    if( cursor != null ) {
                        cursor.close( );
                    }
                }
            }
        }
        return result;
    }
    public int addWordToUserDictionary( WnnWord word ) {
        WnnWord[] words = new WnnWord[1];
        words[0] = word;
        return addWordToUserDictionary( words );
    }
    public int removeWordFromUserDictionary( WnnWord[] word ) {
        if( mDbDic != null ) {
            mDbDic.beginTransaction();
            try {
                StringBuilder strokeSQL    = new StringBuilder();
                StringBuilder candidateSQL = new StringBuilder();
                for( int index = 0 ; index < word.length ; index++ ) {
                    if( word[index].stroke.length()    > 0 && word[index].stroke.length()    <= MAX_STROKE_LENGTH &&
                        word[index].candidate.length() > 0 && word[index].candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                        strokeSQL.setLength( 0 );
                        candidateSQL.setLength( 0 );
                        DatabaseUtils.appendEscapedSQLString( strokeSQL, word[index].stroke );
                        DatabaseUtils.appendEscapedSQLString( candidateSQL, word[index].candidate );
                        mDbDic.delete( TABLE_NAME_DIC,
                            String.format( "%s=%d and %s=%s and %s=%s",
                                           COLUMN_NAME_TYPE, TYPE_NAME_USER,
                                           COLUMN_NAME_STROKE, strokeSQL,
                                           COLUMN_NAME_CANDIDATE, candidateSQL ),
                            null );
                    }
                }
                mDbDic.setTransactionSuccessful();
            } catch( SQLException e ) {
                return -1;
            } finally {
                mDbDic.endTransaction();
            }
        }
        return 0;
    }
    public int removeWordFromUserDictionary( WnnWord word ) {
        WnnWord[] words = new WnnWord[1];
        words[0] = word;
        return removeWordFromUserDictionary( words );
    }
    public int learnWord( WnnWord word ) {
        return learnWord( word, null );
    }
    public int learnWord( WnnWord word, WnnWord previousWord ) {
        if( mDbDic != null ) {
            StringBuilder previousStrokeSQL    = new StringBuilder();
            StringBuilder previousCandidateSQL = new StringBuilder();
            if( previousWord != null &&
                previousWord.stroke.length()    > 0 && previousWord.stroke.length()    <= MAX_STROKE_LENGTH &&
                previousWord.candidate.length() > 0 && previousWord.candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                DatabaseUtils.appendEscapedSQLString( previousStrokeSQL, previousWord.stroke );
                DatabaseUtils.appendEscapedSQLString( previousCandidateSQL, previousWord.candidate );
            }
            if( word.stroke.length()    > 0 && word.stroke.length()    <= MAX_STROKE_LENGTH &&
                word.candidate.length() > 0 && word.candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                StringBuilder strokeSQL    = new StringBuilder();
                StringBuilder candidateSQL = new StringBuilder();
                DatabaseUtils.appendEscapedSQLString( strokeSQL, word.stroke );
                DatabaseUtils.appendEscapedSQLString( candidateSQL, word.candidate );
                SQLiteCursor cursor;
                cursor = ( SQLiteCursor )mDbDic.query(
                    TABLE_NAME_DIC,
                    new String[] { COLUMN_NAME_STROKE, COLUMN_NAME_CANDIDATE },
                    String.format( "%s=%d", COLUMN_NAME_TYPE, TYPE_NAME_LEARN ),
                    null, null, null,
                    String.format( "%s ASC", COLUMN_NAME_ID ) );
                if( cursor.getCount( ) >= MAX_WORDS_IN_LEARN_DICTIONARY ) {
                    mDbDic.beginTransaction();
                    try {
                        cursor.moveToFirst( );
                        StringBuilder oldestStrokeSQL    = new StringBuilder();
                        StringBuilder oldestCandidateSQL = new StringBuilder();
                        DatabaseUtils.appendEscapedSQLString( oldestStrokeSQL, cursor.getString( 0 ) );
                        DatabaseUtils.appendEscapedSQLString( oldestCandidateSQL, cursor.getString( 1 ) );
                        mDbDic.delete( TABLE_NAME_DIC,
                            String.format( "%s=%d and %s=%s and %s=%s",
                                           COLUMN_NAME_TYPE, TYPE_NAME_LEARN,
                                           COLUMN_NAME_STROKE, oldestStrokeSQL.toString( ),
                                           COLUMN_NAME_CANDIDATE, oldestCandidateSQL.toString( ) ),
                            null );
                        mDbDic.setTransactionSuccessful();
                    } catch( SQLException e ) {
                        return -1;
                    } finally {
                        mDbDic.endTransaction();
                        cursor.close();
                    }
                } else {
                    cursor.close();
                }
                ContentValues content = new ContentValues();
                content.clear();
                content.put( COLUMN_NAME_TYPE,                   TYPE_NAME_LEARN );
                content.put( COLUMN_NAME_STROKE,                 word.stroke );
                content.put( COLUMN_NAME_CANDIDATE,              word.candidate );
                content.put( COLUMN_NAME_POS_LEFT,               word.partOfSpeech.left );
                content.put( COLUMN_NAME_POS_RIGHT,              word.partOfSpeech.right );
                if( previousWord != null ) {
                    content.put( COLUMN_NAME_PREVIOUS_STROKE,    previousWord.stroke );
                    content.put( COLUMN_NAME_PREVIOUS_CANDIDATE, previousWord.candidate );
                    content.put( COLUMN_NAME_PREVIOUS_POS_LEFT,  previousWord.partOfSpeech.left );
                    content.put( COLUMN_NAME_PREVIOUS_POS_RIGHT, previousWord.partOfSpeech.right );
                }
                mDbDic.beginTransaction();
                try {
                    mDbDic.insert( TABLE_NAME_DIC, null, content );
                    mDbDic.setTransactionSuccessful();
                } catch( SQLException e ) {
                    mDbDic.endTransaction();
                    return -1;
                } finally {
                    mDbDic.endTransaction();
                }
            }
        }
        return 0;
    }
}
