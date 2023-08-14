public class TransformerManager
{
    private class TransformerInfo {
        final ClassFileTransformer  mTransformer;
        String                      mPrefix;
        TransformerInfo(ClassFileTransformer transformer) {
            mTransformer = transformer;
            mPrefix = null;
        }
        ClassFileTransformer transformer() {
            return  mTransformer;
        }
        String getPrefix() {
            return mPrefix;
        }
        void setPrefix(String prefix) {
            mPrefix = prefix;
        }
    }
    private TransformerInfo[]  mTransformerList;
    private boolean            mIsRetransformable;
    TransformerManager(boolean isRetransformable) {
        mTransformerList    = new TransformerInfo[0];
        mIsRetransformable  = isRetransformable;
    }
    boolean isRetransformable() {
        return mIsRetransformable;
    }
    public synchronized void
    addTransformer( ClassFileTransformer    transformer) {
        TransformerInfo[] oldList = mTransformerList;
        TransformerInfo[] newList = new TransformerInfo[oldList.length + 1];
        System.arraycopy(   oldList,
                            0,
                            newList,
                            0,
                            oldList.length);
        newList[oldList.length] = new TransformerInfo(transformer);
        mTransformerList = newList;
    }
    public synchronized boolean
    removeTransformer(ClassFileTransformer  transformer) {
        boolean                 found           = false;
        TransformerInfo[]       oldList         = mTransformerList;
        int                     oldLength       = oldList.length;
        int                     newLength       = oldLength - 1;
        int matchingIndex   = 0;
        for ( int x = oldLength - 1; x >= 0; x-- ) {
            if ( oldList[x].transformer() == transformer ) {
                found           = true;
                matchingIndex   = x;
                break;
            }
        }
        if ( found ) {
            TransformerInfo[]  newList = new TransformerInfo[newLength];
            if ( matchingIndex > 0 ) {
                System.arraycopy(   oldList,
                                    0,
                                    newList,
                                    0,
                                    matchingIndex);
            }
            if ( matchingIndex < (newLength) ) {
                System.arraycopy(   oldList,
                                    matchingIndex + 1,
                                    newList,
                                    matchingIndex,
                                    (newLength) - matchingIndex);
            }
            mTransformerList = newList;
        }
        return found;
    }
    synchronized boolean
    includesTransformer(ClassFileTransformer transformer) {
        for (TransformerInfo info : mTransformerList) {
            if ( info.transformer() == transformer ) {
                return true;
            }
        }
        return false;
    }
    private TransformerInfo[]
    getSnapshotTransformerList() {
        return mTransformerList;
    }
    public byte[]
    transform(  ClassLoader         loader,
                String              classname,
                Class               classBeingRedefined,
                ProtectionDomain    protectionDomain,
                byte[]              classfileBuffer) {
        boolean someoneTouchedTheBytecode = false;
        TransformerInfo[]  transformerList = getSnapshotTransformerList();
        byte[]  bufferToUse = classfileBuffer;
        for ( int x = 0; x < transformerList.length; x++ ) {
            TransformerInfo         transformerInfo = transformerList[x];
            ClassFileTransformer    transformer = transformerInfo.transformer();
            byte[]                  transformedBytes = null;
            try {
                transformedBytes = transformer.transform(   loader,
                                                            classname,
                                                            classBeingRedefined,
                                                            protectionDomain,
                                                            bufferToUse);
            }
            catch (Throwable t) {
            }
            if ( transformedBytes != null ) {
                someoneTouchedTheBytecode = true;
                bufferToUse = transformedBytes;
            }
        }
        byte [] result;
        if ( someoneTouchedTheBytecode ) {
            result = bufferToUse;
        }
        else {
            result = null;
        }
        return result;
    }
    int
    getTransformerCount() {
        TransformerInfo[]  transformerList = getSnapshotTransformerList();
        return transformerList.length;
    }
    boolean
    setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        TransformerInfo[]  transformerList = getSnapshotTransformerList();
        for ( int x = 0; x < transformerList.length; x++ ) {
            TransformerInfo         transformerInfo = transformerList[x];
            ClassFileTransformer    aTransformer = transformerInfo.transformer();
            if ( aTransformer == transformer ) {
                transformerInfo.setPrefix(prefix);
                return true;
            }
        }
        return false;
    }
    String[]
    getNativeMethodPrefixes() {
        TransformerInfo[]  transformerList = getSnapshotTransformerList();
        String[] prefixes                  = new String[transformerList.length];
        for ( int x = 0; x < transformerList.length; x++ ) {
            TransformerInfo         transformerInfo = transformerList[x];
            prefixes[x] = transformerInfo.getPrefix();
        }
        return prefixes;
    }
}
