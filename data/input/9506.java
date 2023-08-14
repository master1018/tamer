public class InterceptorList {
    static final int INTERCEPTOR_TYPE_CLIENT            = 0;
    static final int INTERCEPTOR_TYPE_SERVER            = 1;
    static final int INTERCEPTOR_TYPE_IOR               = 2;
    static final int NUM_INTERCEPTOR_TYPES              = 3;
    static final Class[] classTypes = {
        org.omg.PortableInterceptor.ClientRequestInterceptor.class,
        org.omg.PortableInterceptor.ServerRequestInterceptor.class,
        org.omg.PortableInterceptor.IORInterceptor.class
    };
    private boolean locked = false;
    private InterceptorsSystemException wrapper ;
    private Interceptor[][] interceptors =
        new Interceptor[NUM_INTERCEPTOR_TYPES][];
    InterceptorList( InterceptorsSystemException wrapper ) {
        this.wrapper = wrapper ;
        initInterceptorArrays();
    }
    void register_interceptor( Interceptor interceptor, int type )
        throws DuplicateName
    {
        if( locked ) {
            throw wrapper.interceptorListLocked() ;
        }
        String interceptorName = interceptor.name();
        boolean anonymous = interceptorName.equals( "" );
        boolean foundDuplicate = false;
        Interceptor[] interceptorList = interceptors[type];
        if( !anonymous ) {
            int size = interceptorList.length;
            for( int i = 0; i < size; i++ ) {
                Interceptor in = (Interceptor)interceptorList[i];
                if( in.name().equals( interceptorName ) ) {
                    foundDuplicate = true;
                    break;
                }
            }
        }
        if( !foundDuplicate ) {
            growInterceptorArray( type );
            interceptors[type][interceptors[type].length-1] = interceptor;
        }
        else {
            throw new DuplicateName( interceptorName );
        }
    }
    void lock() {
        locked = true;
    }
    Interceptor[] getInterceptors( int type ) {
        return interceptors[type];
    }
    boolean hasInterceptorsOfType( int type ) {
        return interceptors[type].length > 0;
    }
    private void initInterceptorArrays() {
        for( int type = 0; type < NUM_INTERCEPTOR_TYPES; type++ ) {
            Class classType = classTypes[type];
            interceptors[type] =
                (Interceptor[])Array.newInstance( classType, 0 );
        }
    }
    private void growInterceptorArray( int type ) {
        Class classType = classTypes[type];
        int currentLength = interceptors[type].length;
        Interceptor[] replacementArray;
        replacementArray = (Interceptor[])
            Array.newInstance( classType, currentLength + 1 );
        System.arraycopy( interceptors[type], 0,
                          replacementArray, 0, currentLength );
        interceptors[type] = replacementArray;
    }
    void destroyAll() {
        int numTypes = interceptors.length;
        for( int i = 0; i < numTypes; i++ ) {
            int numInterceptors = interceptors[i].length;
            for( int j = 0; j < numInterceptors; j++ ) {
                interceptors[i][j].destroy();
            }
        }
    }
    void sortInterceptors() {
        List sorted = null;
        List unsorted = null;
        int numTypes = interceptors.length;
        for( int i = 0; i < numTypes; i++ ) {
            int numInterceptors = interceptors[i].length;
            if (numInterceptors > 0) {
                sorted = new ArrayList(); 
                unsorted = new ArrayList();
            }
            for( int j = 0; j < numInterceptors; j++ ) {
                Interceptor interceptor = interceptors[i][j];
                if (interceptor instanceof Comparable) {
                    sorted.add(interceptor);
                } else {
                    unsorted.add(interceptor);
                }
            }
            if (numInterceptors > 0 && sorted.size() > 0) {
                Collections.sort(sorted);
                Iterator sortedIterator = sorted.iterator();
                Iterator unsortedIterator = unsorted.iterator();
                for( int j = 0; j < numInterceptors; j++ ) {
                    if (sortedIterator.hasNext()) {
                        interceptors[i][j] =
                            (Interceptor) sortedIterator.next();
                    } else if (unsortedIterator.hasNext()) {
                        interceptors[i][j] =
                            (Interceptor) unsortedIterator.next();
                    } else {
                        throw wrapper.sortSizeMismatch() ;
                    }
                }
            }
        }
    }
}
