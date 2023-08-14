public class IDLGenerator extends sun.rmi.rmic.iiop.Generator {
    private boolean valueMethods = true;
    private boolean factory = true;                              
    private Hashtable ifHash = new Hashtable();              
    private Hashtable imHash = new Hashtable();            
    private boolean isThrown = true;                      
    private boolean isException = true;       
    private boolean isForward = true;                      
    private boolean forValuetype = true;                 
    public IDLGenerator() {
    }
    protected boolean requireNewInstance() {
        return false;
    }
    protected boolean parseNonConforming(ContextStack stack) {
        return valueMethods;
    }
    protected sun.rmi.rmic.iiop.CompoundType getTopType(ClassDefinition cdef,
                                                        ContextStack stack) {
        return CompoundType.forCompound(cdef,stack);
    }
    protected Identifier getOutputId (
                                      OutputType ot ) {
        Identifier id = super.getOutputId( ot );
        Type t = ot.getType();
        String fName = ot.getName();
        if ( id == idJavaLangClass )                 
            if ( t.isArray() )
                return Identifier.lookup(
                                         "org.omg.boxedRMI.javax.rmi.CORBA." + fName  );
            else return idClassDesc;
        if ( id == idJavaLangString &&                  
             t.isArray() )
            return Identifier.lookup( "org.omg.boxedRMI.CORBA." + fName );
        if ( "org.omg.CORBA.Object".equals( t.getQualifiedName() ) &&
             t.isArray() )                          
            return Identifier.lookup( "org.omg.boxedRMI." + fName );
        if ( t.isArray()) {                                                 
            ArrayType at = (ArrayType)t;
            Type et = at.getElementType();
            if ( et.isCompound() ) {
                CompoundType ct = (CompoundType)et;
                String qName = ct.getQualifiedName();
                if ( ct.isIDLEntity() )
                    return Identifier.lookup( getQualifiedName( at ) );
            }
            return Identifier.lookup( idBoxedRMI,id );
        }
        if ( t.isCompound() ) {                                   
            CompoundType ct = (CompoundType)t;
            String qName = ct.getQualifiedName();
            if ( ct.isBoxed() )
                return Identifier.lookup( getQualifiedName( ct ) );
        }
        return id;
    }
    protected String getFileNameExtensionFor(OutputType outputType) {
        return IDL_FILE_EXTENSION;
    }
    public boolean parseArgs(String argv[], Main main) {
        boolean result = super.parseArgs(argv,main);
        String idlFrom;
        String idlTo;
        if (result) {
        nextArg:
            for (int i = 0; i < argv.length; i++) {
                if (argv[i] != null) {
                    if (argv[i].equalsIgnoreCase("-idl")) {
                        idl = true;
                        argv[i] = null;
                    }
                    else if ( argv[i].equalsIgnoreCase( "-valueMethods" ) ) {
                        valueMethods = true;
                        argv[i] = null;
                    }
                    else if ( argv[i].equalsIgnoreCase( "-noValueMethods" ) ) {
                        valueMethods = false;
                        argv[i] = null;
                    }
                    else if ( argv[i].equalsIgnoreCase( "-init" ) ) {
                        factory = false;
                        argv[i] = null;
                }
                    else if ( argv[i].equalsIgnoreCase( "-factory" ) ) {
                        factory = true;
                        argv[i] = null;
            }
                    else if ( argv[i].equalsIgnoreCase( "-idlfile" ) ) {
                        argv[i] = null;
                        if ( ++i < argv.length && argv[i] != null && !argv[i].startsWith("-") ) {
                            idlFrom = argv[i];
                            argv[i] = null;
                            if ( ++i < argv.length && argv[i] != null && !argv[i].startsWith("-") ) {
                                idlTo = argv[i];
                                argv[i] = null;
                                ifHash.put( idlFrom,idlTo );
                                continue nextArg;
        }
                        }
                        main.error("rmic.option.requires.argument", "-idlfile");
                        result = false;
                    }
                    else if ( argv[i].equalsIgnoreCase( "-idlmodule" ) ) {
                        argv[i] = null;
                        if ( ++i < argv.length && argv[i] != null && !argv[i].startsWith("-") ) {
                            idlFrom = argv[i];
                            argv[i] = null;
                            if ( ++i < argv.length && argv[i] != null && !argv[i].startsWith("-") ) {
                                idlTo = argv[i];
                                argv[i] = null;
                                imHash.put( idlFrom,idlTo );
                                continue nextArg;
                            }
                        }
                        main.error("rmic.option.requires.argument", "-idlmodule");
                        result = false;
                    }
                }
            }
        }
        return result;
    }
    protected OutputType[] getOutputTypesFor(
                                             CompoundType topType,
                                             HashSet alreadyChecked ) {
        Vector refVec = getAllReferencesFor( topType );
        Vector outVec = new Vector();
        for ( int i1 = 0; i1 < refVec.size(); i1++ ) {          
            Type t = (Type)refVec.elementAt( i1 );
            if ( t.isArray() ) {
                ArrayType at = (ArrayType)t;
                int dim = at.getArrayDimension();
                Type et = at.getElementType();
                String fName = unEsc( et.getIDLName() ).replace( ' ','_' );
                for ( int i2 = 0; i2 < dim; i2++ ) {                
                    String fileName = "seq" + ( i2 + 1 ) + "_" + fName;
                    outVec.addElement( new OutputType( fileName,at ) );
                }
            }
            else if ( t.isCompound() ) {
                String fName = unEsc( t.getIDLName() );
                outVec.addElement( new OutputType( fName.replace( ' ','_' ),t ) );
            if ( t.isClass() ) {
                ClassType ct = (ClassType)t;
                    if ( ct.isException() ) {                            
                        fName = unEsc( ct.getIDLExceptionName() );
                        outVec.addElement( new OutputType( fName.replace( ' ','_' ),t ) );
            }
        }
    }
    }
        OutputType[] outArr = new OutputType[outVec.size()];
        outVec.copyInto( outArr );
        return outArr;
    }
    protected Vector getAllReferencesFor(
                                         CompoundType ct ) {
        Hashtable refHash = new Hashtable();
        Hashtable spcHash = new Hashtable();
        Hashtable arrHash = new Hashtable();
        int refSize;
        refHash.put( ct.getQualifiedName(),ct );               
        accumulateReferences( refHash,spcHash,arrHash );
        do {
            refSize = refHash.size();
            accumulateReferences( refHash,spcHash,arrHash );
        }
        while ( refSize < refHash.size() );        
        Vector outVec = new Vector();
        Enumeration e = refHash.elements();                   
        while ( e.hasMoreElements() ) {
            CompoundType t = (CompoundType)e.nextElement();
            outVec.addElement( t );
        }
        e = spcHash.elements();                                
        while ( e.hasMoreElements() ) {
            CompoundType t = (CompoundType)e.nextElement();
            outVec.addElement( t );
    }
        e = arrHash.elements();                                  
                                         nextSequence:
        while ( e.hasMoreElements() ) {
            ArrayType at = (ArrayType)e.nextElement();
            int dim = at.getArrayDimension();
            Type et = at.getElementType();
            Enumeration e2 = arrHash.elements();
            while ( e2.hasMoreElements() ) {                   
                ArrayType at2 = (ArrayType)e2.nextElement();
                if ( et == at2.getElementType() &&                
                     dim < at2.getArrayDimension() )               
                    continue nextSequence;                              
            }
            outVec.addElement( at );
        }
        return outVec;
    }
    protected void accumulateReferences(
                                        Hashtable refHash,
                                        Hashtable spcHash,
                                        Hashtable arrHash ) {
        Enumeration e = refHash.elements();
        while ( e.hasMoreElements() ) {
            CompoundType t = (CompoundType)e.nextElement();
            Vector datVec = getData( t );                     
            Vector mthVec = getMethods( t );             
            getInterfaces( t,refHash );                          
            getInheritance( t,refHash );                            
            getMethodReferences( mthVec,refHash,spcHash,arrHash,refHash );
            getMemberReferences( datVec,refHash,spcHash,arrHash );
        }
        e = arrHash.elements();                      
        while ( e.hasMoreElements() ) {
            ArrayType at = (ArrayType)e.nextElement();
            Type et = at.getElementType();
            addReference( et,refHash,spcHash,arrHash );
        }
        e = refHash.elements();
        while ( e.hasMoreElements() ) {
            CompoundType t = (CompoundType)e.nextElement();
            if ( !isIDLGeneratedFor( t ) )              
                refHash.remove( t.getQualifiedName() );
    }
    }
    protected boolean isIDLGeneratedFor(
                                 CompoundType t ) {
        if ( t.isCORBAObject() ) return false;
        if ( t.isIDLEntity() )
            if ( t.isBoxed() ) return true;
            else if ( "org.omg.CORBA.portable.IDLEntity"
                      .equals( t.getQualifiedName() ) ) return true;
            else if ( t.isCORBAUserException() ) return true;
            else return false;
        Hashtable inhHash = new Hashtable();
        getInterfaces( t,inhHash );
        if ( t.getTypeCode() == TYPE_IMPLEMENTATION )
            if ( inhHash.size() < 2 ) return false;         
            else return true;
        return true;                                   
    }
    protected void writeOutputFor(
                                  OutputType ot,
                                  HashSet alreadyChecked,
                                  IndentingWriter p )
        throws IOException {
        Type t = ot.getType();
        if ( t.isArray() ) {                                
            writeSequence( ot,p );
            return;
        }
        if ( isSpecialReference( t ) ) {                
            writeSpecial( t,p );
            return;
        }
        if ( t.isCompound() ) {                            
            CompoundType ct = (CompoundType)t;
            if ( ct.isIDLEntity() && ct.isBoxed() ) {
                writeBoxedIDL( ct,p );
                return;
            }
        }
        if ( t.isClass() ) {                               
            ClassType ct = (ClassType)t;
            if ( ct.isException() ) {
                String eName = unEsc( ct.getIDLExceptionName() );
                String fName = ot.getName();
                if ( fName.equals( eName.replace( ' ','_' ) ) ) {
                    writeException( ct,p );
                    return;
                }
            }
        }
        switch ( t.getTypeCode() ) {                                 
        case TYPE_IMPLEMENTATION:
            writeImplementation( (ImplementationType)t,p );
            break;
        case TYPE_NC_CLASS:
        case TYPE_NC_INTERFACE:
            writeNCType( (CompoundType)t,p );
            break;
        case TYPE_ABSTRACT:                        
        case TYPE_REMOTE:
            writeRemote( (RemoteType)t,p );
            break;
        case TYPE_VALUE:
            writeValue( (ValueType)t,p );
            break;
        default:
            throw new CompilerError(
                                    "IDLGenerator got unexpected type code: "
                                    + t.getTypeCode());
        }
    }
    protected void writeImplementation(
                                       ImplementationType t,
                                       IndentingWriter p )
        throws IOException {
        Hashtable inhHash = new Hashtable();
        Hashtable refHash = new Hashtable();
        getInterfaces( t,inhHash );                            
        writeBanner( t,0,!isException,p );
        writeInheritedIncludes( inhHash,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeIncOrb( p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.p( "interface " + t.getIDLName() );
        writeInherits( inhHash,!forValuetype,p );
        p.pln( " {" );
        p.pln( "};" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeEpilog( t,refHash,p );
    }
    protected void writeNCType(
                               CompoundType t,
                               IndentingWriter p )
        throws IOException {
        Vector conVec = getConstants( t );                      
        Vector mthVec = getMethods( t );                          
        Hashtable inhHash = new Hashtable();
        Hashtable refHash = new Hashtable();
        Hashtable spcHash = new Hashtable();
        Hashtable arrHash = new Hashtable();
        Hashtable excHash = new Hashtable();
        getInterfaces( t,inhHash );                            
        getInheritance( t,inhHash );                              
        getMethodReferences( mthVec,refHash,spcHash,arrHash,excHash );
        writeProlog( t,refHash,spcHash,arrHash,excHash,inhHash,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.p( "abstract valuetype " + t.getIDLName() );
        writeInherits( inhHash,!forValuetype,p );
        p.pln( " {" );
        if ( conVec.size() + mthVec.size() > 0 ) {                   
            p.pln();p.pI();
            for ( int i1 = 0; i1 < conVec.size(); i1++ )            
                writeConstant( (CompoundType.Member)conVec.elementAt( i1 ),p );
            for ( int i1 = 0; i1 < mthVec.size(); i1++ )              
                writeMethod( (CompoundType.Method)mthVec.elementAt( i1 ),p );
            p.pO();p.pln();
        }
        p.pln( "};" );
                p.pO();p.pln();
        writeModule2( t,p );
        writeEpilog( t,refHash,p );
    }
    protected void writeRemote(
                               RemoteType t,
                               IndentingWriter p )
        throws IOException {
        Vector conVec = getConstants( t );                      
        Vector mthVec = getMethods( t );                          
        Hashtable inhHash = new Hashtable();
        Hashtable refHash = new Hashtable();
        Hashtable spcHash = new Hashtable();
        Hashtable arrHash = new Hashtable();
        Hashtable excHash = new Hashtable();
        getInterfaces( t,inhHash );                            
        getMethodReferences( mthVec,refHash,spcHash,arrHash,excHash );
        writeProlog( t,refHash,spcHash,arrHash,excHash,inhHash,p );
        writeModule1( t,p );
        p.pln();p.pI();
        if ( t.getTypeCode() == TYPE_ABSTRACT ) p.p( "abstract " );
        p.p( "interface " + t.getIDLName() );
        writeInherits( inhHash,!forValuetype,p );
        p.pln( " {" );
        if ( conVec.size() + mthVec.size() > 0 ) {      
            p.pln();p.pI();
            for ( int i1 = 0; i1 < conVec.size(); i1++ )                  
                writeConstant( (CompoundType.Member)conVec.elementAt( i1 ),p );
            for ( int i1 = 0; i1 < mthVec.size(); i1++ )        
                writeMethod( (CompoundType.Method)mthVec.elementAt( i1 ),p );
            p.pO();p.pln();
        }
        p.pln( "};" );
        p.pO();p.pln();
        writeRepositoryID ( t,p );
        p.pln();
        writeModule2( t,p );
        writeEpilog( t,refHash,p );
    }
    protected void writeValue(
                              ValueType t,
                              IndentingWriter p )
        throws IOException {
        Vector datVec = getData( t );                       
        Vector conVec = getConstants( t );                      
        Vector mthVec = getMethods( t );               
        Hashtable inhHash = new Hashtable();
        Hashtable refHash = new Hashtable();
        Hashtable spcHash = new Hashtable();
        Hashtable arrHash = new Hashtable();
        Hashtable excHash = new Hashtable();
        getInterfaces( t,inhHash );                            
        getInheritance( t,inhHash );                              
        getMethodReferences( mthVec,refHash,spcHash,arrHash,excHash );
        getMemberReferences( datVec,refHash,spcHash,arrHash );
        writeProlog( t,refHash,spcHash,arrHash,excHash,inhHash,p );
        writeModule1( t,p );
        p.pln();p.pI();
        if ( t.isCustom() ) p.p( "custom " );
        p.p( "valuetype " + t.getIDLName() );
        writeInherits( inhHash,forValuetype,p );
        p.pln( " {" );
        if ( conVec.size() + datVec.size() + mthVec.size() > 0 ) {   
            p.pln();p.pI();
            for ( int i1 = 0; i1 < conVec.size(); i1++ )            
                writeConstant( (CompoundType.Member)conVec.elementAt( i1 ),p );
            for ( int i1 = 0; i1 < datVec.size(); i1++ ) {
                CompoundType.Member mem = (CompoundType.Member)datVec.elementAt( i1 );
                if ( mem.getType().isPrimitive() )
                    writeData( mem,p );                            
            }
            for ( int i1 = 0; i1 < datVec.size(); i1++ ) {
                CompoundType.Member mem = (CompoundType.Member)datVec.elementAt( i1 );
                if ( !mem.getType().isPrimitive() )
                    writeData( mem,p );                        
            }
            for ( int i1 = 0; i1 < mthVec.size(); i1++ )              
                writeMethod( (CompoundType.Method)mthVec.elementAt( i1 ),p );
            p.pO();p.pln();
        }
        p.pln( "};" );
        p.pO();p.pln();
        writeRepositoryID ( t,p );
            p.pln();
        writeModule2( t,p );
        writeEpilog( t,refHash,p );
        }
    protected void writeProlog(
                               CompoundType t,
                               Hashtable refHash,
                               Hashtable spcHash,
                               Hashtable arrHash,
                               Hashtable excHash,
                               Hashtable inhHash,
                               IndentingWriter p )
        throws IOException {
        writeBanner( t,0,!isException,p );
        writeForwardReferences( refHash,p );
        writeIncludes( excHash,isThrown,p );      
        writeInheritedIncludes( inhHash,p );
        writeIncludes( spcHash,!isThrown,p );         
        writeBoxedRMIIncludes( arrHash,p );
        writeIDLEntityIncludes( refHash,p );
        writeIncOrb( p );
        writeIfndef( t,0,!isException,!isForward,p );
    }
    protected void writeEpilog(
                               CompoundType t,
                               Hashtable refHash,
                               IndentingWriter p )
        throws IOException {
        writeIncludes( refHash,!isThrown,p );     
        writeEndif( p );
    }
    protected void writeSpecial(
                                Type t,
                                IndentingWriter p )
        throws IOException {
        String spcName = t.getQualifiedName();
        if ( "java.io.Serializable".equals( spcName ) )
            writeJavaIoSerializable( t,p );
        else if ( "java.io.Externalizable".equals( spcName ) )
            writeJavaIoExternalizable( t,p );
        else if ( "java.lang.Object".equals( spcName) )
            writeJavaLangObject( t,p );
        else if ( "java.rmi.Remote".equals( spcName) )
            writeJavaRmiRemote( t,p );
        else if ( "org.omg.CORBA.portable.IDLEntity".equals( spcName) )
            writeIDLEntity( t,p );
    }
    protected void writeJavaIoSerializable(
                                           Type t,
                                           IndentingWriter p )
        throws IOException {
        writeBanner( t,0,!isException,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.pln( "typedef any Serializable;" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeEndif( p );
    }
    protected void writeJavaIoExternalizable(
                                             Type t,
                                             IndentingWriter p )
        throws IOException {
        writeBanner( t,0,!isException,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.pln( "typedef any Externalizable;" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeEndif( p );
    }
    protected void writeJavaLangObject(
                                       Type t,
                                       IndentingWriter p )
        throws IOException {
        writeBanner( t,0,!isException,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.pln( "typedef any _Object;" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeEndif( p );
    }
    protected void writeJavaRmiRemote(
                                      Type t,
                                      IndentingWriter p )
        throws IOException {
        writeBanner( t,0,!isException,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.pln( "typedef Object Remote;" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeEndif( p );
    }
    protected void writeIDLEntity(
                                  Type t,
                                  IndentingWriter p )
        throws IOException {
        writeBanner( t,0,!isException,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.pln( "typedef any IDLEntity;" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeEndif( p );
    }
    protected void getInterfaces(
                                 CompoundType ct,
                                 Hashtable inhHash ) {
        InterfaceType[] infs = ct.getInterfaces();
                                 nextInterface:
        for ( int i1 = 0; i1 < infs.length; i1++ ) {  
            String inhName = infs[i1].getQualifiedName();
            switch ( ct.getTypeCode() ) {
            case TYPE_NC_CLASS:
            case TYPE_VALUE:                                   
                if ( "java.io.Externalizable".equals( inhName ) ||
                     "java.io.Serializable".equals( inhName ) ||
                     "org.omg.CORBA.portable.IDLEntity".equals( inhName ) )
                    continue nextInterface;
                break;
            default:                                        
                if ( "java.rmi.Remote".equals( inhName ) )
                    continue nextInterface;
                break;
            }
            inhHash.put( inhName,infs[i1] );                           
        }
    }
    protected void getInheritance(
                                  CompoundType ct,
                                  Hashtable inhHash ) {
        ClassType par = ct.getSuperclass();                            
        if ( par == null ) return;
        String parName = par.getQualifiedName();
        switch ( ct.getTypeCode() ) {
        case TYPE_NC_CLASS:
        case TYPE_VALUE:
            if ( "java.lang.Object".equals( parName ) )          
                return;
            break;
        default: return;                                     
        }
        inhHash.put( parName,par );                          
    }
    protected void getMethodReferences(
                                       Vector mthVec,
                                       Hashtable refHash,
                                       Hashtable spcHash,
                                       Hashtable arrHash,
                                       Hashtable excHash ) {
        for ( int i1 = 0; i1 < mthVec.size(); i1++ ) {             
            CompoundType.Method mth = (CompoundType.Method)mthVec.elementAt( i1 );
            Type[] args = mth.getArguments();
            Type ret = mth.getReturnType();
            getExceptions( mth,excHash );                 
            for ( int i2 = 0; i2 < args.length; i2++ )             
                addReference( args[i2],refHash,spcHash,arrHash );
            addReference( ret,refHash,spcHash,arrHash );
        }
    }
    protected void getMemberReferences(
                                       Vector datVec,
                                       Hashtable refHash,
                                       Hashtable spcHash,
                                       Hashtable arrHash ) {
        for ( int i1 = 0; i1 < datVec.size(); i1++ ) {         
            CompoundType.Member mem = (CompoundType.Member)datVec.elementAt( i1 );
            Type dat = mem.getType();
            addReference( dat,refHash,spcHash,arrHash );
        }
    }
    protected void addReference(
                                Type ref,
                                Hashtable refHash,
                                Hashtable spcHash,
                                Hashtable arrHash ) {
        String rName = ref.getQualifiedName();
        switch ( ref.getTypeCode() ) {
        case TYPE_ABSTRACT:
        case TYPE_REMOTE:
        case TYPE_NC_CLASS:
        case TYPE_NC_INTERFACE:
        case TYPE_VALUE:
            refHash.put( rName,ref );
            return;
        case TYPE_CORBA_OBJECT:
            if ( "org.omg.CORBA.Object".equals( rName ) ) return;      
            refHash.put( rName,ref );
            return;
        case TYPE_ARRAY:                                                 
            arrHash.put( rName + ref.getArrayDimension(),ref );
            return;
        default:
            if ( isSpecialReference( ref ) )                 
                spcHash.put( rName,ref );
        }
    }
    protected boolean isSpecialReference(
                                         Type ref ) {
        String rName = ref.getQualifiedName();
        if ( "java.io.Serializable".equals( rName ) ) return true;
        if ( "java.io.Externalizable".equals( rName ) ) return true;
        if ( "java.lang.Object".equals( rName) ) return true;
        if ( "java.rmi.Remote".equals( rName) ) return true;
        if ( "org.omg.CORBA.portable.IDLEntity".equals( rName) ) return true;
        return false;
    }
    protected void getExceptions(
                                      CompoundType.Method mth,
                                      Hashtable excHash ) {
        ClassType[] excs = mth.getExceptions();
        for ( int i1 = 0; i1 < excs.length; i1++ ) {            
            ClassType exc = excs[i1];
            if ( exc.isCheckedException() &&
                 !exc.isRemoteExceptionOrSubclass() ) {
                excHash.put( exc.getQualifiedName(),exc );
        }
    }
    }
    protected Vector getMethods(
                                CompoundType ct ) {
        Vector vec = new Vector();
        int ctType = ct.getTypeCode();
        switch ( ctType ) {
        case TYPE_ABSTRACT:
        case TYPE_REMOTE:       break;
        case TYPE_NC_CLASS:
        case TYPE_NC_INTERFACE:
        case TYPE_VALUE:        if ( valueMethods ) break;
        default: return vec;
        }
        Identifier ctId = ct.getIdentifier();
        CompoundType.Method[] mths = ct.getMethods();
                                nextMethod:
        for ( int i1 = 0; i1 < mths.length; i1++ ) {               
            if ( mths[i1].isPrivate() ||                            
                 mths[i1].isInherited() )                         
                continue nextMethod;                                   
            if ( ctType == TYPE_VALUE ) {
                String mthName = mths[i1].getName();
                if ( "readObject"  .equals( mthName ) ||
                     "writeObject" .equals( mthName ) ||
                     "readExternal".equals( mthName ) ||
                     "writeExternal".equals( mthName ) )
                    continue nextMethod;                                
            }
            if ( ( ctType == TYPE_NC_CLASS ||
                   ctType == TYPE_NC_INTERFACE ) &&
                 mths[i1].isConstructor() )   
                continue nextMethod;                                  
            vec.addElement( mths[i1] );                                
        }
        return vec;
    }
    protected Vector getConstants(
                                  CompoundType ct ) {
        Vector vec = new Vector();
        CompoundType.Member[] mems = ct.getMembers();
        for ( int i1 = 0; i1 < mems.length; i1++ ) {               
            Type   memType  = mems[i1].getType();
            String memValue = mems[i1].getValue();
            if ( mems[i1].isPublic() &&
                 mems[i1].isFinal()  &&
                 mems[i1].isStatic() &&
                 ( memType.isPrimitive() || "String".equals( memType.getName() ) ) &&
                 memValue != null )
                vec.addElement( mems[i1] );                              
        }
        return vec;
    }
    protected Vector getData(
                             CompoundType t ) {
        Vector vec = new Vector();
        if ( t.getTypeCode() != TYPE_VALUE ) return vec;
        ValueType vt = (ValueType)t;
        CompoundType.Member[] mems = vt.getMembers();
        boolean notCust = !vt.isCustom();
        for ( int i1 = 0; i1 < mems.length; i1++ ) {               
            if ( !mems[i1].isStatic()    &&
                 !mems[i1].isTransient() &&
                 (  mems[i1].isPublic() || notCust ) ) {
                int i2;
                String memName = mems[i1].getName();
                for ( i2 = 0; i2 < vec.size(); i2++ ) {      
                    CompoundType.Member aMem = (CompoundType.Member)vec.elementAt( i2 );
                    if ( memName.compareTo( aMem.getName() ) < 0 ) break;
                }
                vec.insertElementAt( mems[i1],i2 );                   
            }
        }
        return vec;
    }
    protected void writeForwardReferences(
                                          Hashtable refHash,
                                          IndentingWriter p )
        throws IOException {
        Enumeration refEnum = refHash.elements();
        nextReference:
        while ( refEnum.hasMoreElements() ) {
            Type t = (Type)refEnum.nextElement();
            if ( t.isCompound() ) {
                CompoundType ct = (CompoundType)t;
                if ( ct.isIDLEntity() )
                    continue nextReference;                  
            }
            writeForwardReference( t,p );
        }
    }
    protected void writeForwardReference(
                                         Type t,
                                         IndentingWriter p )
        throws IOException {
        String qName = t.getQualifiedName();
        if ( "java.lang.String".equals( qName ) ) ;
        else if ( "org.omg.CORBA.Object".equals( qName ) ) return ;    
        writeIfndef( t,0,!isException,isForward,p );
            writeModule1( t,p );
            p.pln();p.pI();
            switch ( t.getTypeCode() ) {
        case TYPE_NC_CLASS:
            case TYPE_NC_INTERFACE: p.p( "abstract valuetype " ); break;
            case TYPE_ABSTRACT:     p.p( "abstract interface " ); break;
            case TYPE_VALUE:        p.p( "valuetype " ); break;
        case TYPE_REMOTE:
        case TYPE_CORBA_OBJECT: p.p( "interface " ); break;
            default: ;                              
            }
            p.pln( t.getIDLName() + ";" );
            p.pO();p.pln();
            writeModule2( t,p );
        writeEndif( p );
        }
    protected void writeForwardReference(
                                         ArrayType at,
                                         int dim,
                                         IndentingWriter p)
        throws IOException {
        Type et = at.getElementType();
        if ( dim < 1 ) {
            if ( et.isCompound() ) {
                CompoundType ct = (CompoundType)et;
                writeForwardReference( et,p);
    }
            return;
        }
        String fName = unEsc( et.getIDLName() ).replace( ' ','_' );
        writeIfndef( at,dim,!isException,isForward,p );
        writeModule1( at,p );
        p.pln();p.pI();
        switch ( et.getTypeCode() ) {
        case TYPE_NC_CLASS:
        case TYPE_NC_INTERFACE: p.p( "abstract valuetype " ); break;
        case TYPE_ABSTRACT:     p.p( "abstract interface " ); break;
        case TYPE_VALUE:        p.p( "valuetype " ); break;
        case TYPE_REMOTE:
        case TYPE_CORBA_OBJECT: p.p( "interface " ); break;
        default: ;                              
        }
        p.pln( "seq" + dim + "_" + fName + ";" );
        p.pO();p.pln();
        writeModule2( at,p );
        writeEndif( p );
    }
    protected void writeIDLEntityIncludes(
                                          Hashtable refHash,
                                          IndentingWriter p )
        throws IOException {
        Enumeration refEnum = refHash.elements();
        while ( refEnum.hasMoreElements() ) {
            Type t = (Type)refEnum.nextElement();
            if ( t.isCompound() ) {
                CompoundType ct = (CompoundType)t;
                if ( ct.isIDLEntity() ) {                          
                    writeInclude( ct,0,!isThrown,p );
                    refHash.remove( ct.getQualifiedName() );     
                }
            }
        }
    }
    protected void writeIncludes(
                                 Hashtable incHash,
                                 boolean isThrown,
                                 IndentingWriter p )
        throws IOException {
        Enumeration incEnum = incHash.elements();
        while ( incEnum.hasMoreElements() ) {
            CompoundType t = (CompoundType)incEnum.nextElement();
            writeInclude( t,0,isThrown,p );
            }
    }
    protected void writeBoxedRMIIncludes(
                                         Hashtable arrHash,
                                         IndentingWriter p)
        throws IOException {
        Enumeration e1 = arrHash.elements();
        nextSequence:
        while ( e1.hasMoreElements() ) {
            ArrayType at = (ArrayType)e1.nextElement();
            int dim = at.getArrayDimension();
            Type et = at.getElementType();
            Enumeration e2 = arrHash.elements();
            while ( e2.hasMoreElements() ) {                   
                ArrayType at2 = (ArrayType)e2.nextElement();
                if ( et == at2.getElementType() &&                
                     dim < at2.getArrayDimension() )               
                    continue nextSequence;                              
        }
            writeInclude( at,dim,!isThrown,p );
    }
    }
    protected void writeInheritedIncludes(
                                          Hashtable inhHash,
                                 IndentingWriter p )
        throws IOException {
        Enumeration inhEnum = inhHash.elements();
        while ( inhEnum.hasMoreElements() ) {
            CompoundType t = (CompoundType)inhEnum.nextElement();
            writeInclude( t,0,!isThrown,p );
        }
    }
    protected void writeInclude(
                                Type t,
                                int dim,
                                boolean isThrown,
                                  IndentingWriter p)
        throws IOException {
        CompoundType ct;
        String tName;
        String[] modNames;
        if ( t.isCompound() ) {
            ct = (CompoundType)t;
            String qName = ct.getQualifiedName();
            if ( "java.lang.String".equals( qName ) ) {
                writeIncOrb( p );                         
                return;
            }
            if ( "org.omg.CORBA.Object".equals( qName ) )
                return;                                 
            modNames = getIDLModuleNames( ct );                   
            tName = unEsc( ct.getIDLName() );                     
            if ( ct.isException() )
                if ( ct.isIDLEntityException() )
                    if ( ct.isCORBAUserException() )
                        if ( isThrown ) tName = unEsc( ct.getIDLExceptionName() );
                        else ;
                    else tName = ct.getName();                    
                else if ( isThrown )
                    tName = unEsc( ct.getIDLExceptionName() );
            }
        else if ( t.isArray() ) {
            Type et = t.getElementType();                    
            if ( dim > 0 ) {
                modNames = getIDLModuleNames( t );                  
                tName = "seq" + dim + "_" + unEsc( et.getIDLName().replace( ' ','_' ) );
            }
            else{                                                  
                if ( !et.isCompound() ) return;       
                ct = (CompoundType) et;
                modNames = getIDLModuleNames( ct );           
                tName = unEsc( ct.getIDLName() );
                writeInclude( ct,modNames,tName,p );
                return;
            }
        }
        else return;                              
        writeInclude( t,modNames,tName,p );
    }
    protected void writeInclude(
                                Type t,
                                String[] modNames,
                                String tName,
                                IndentingWriter p)
        throws IOException {
        if ( t.isCompound() ) {
            CompoundType it = (CompoundType)t;
            if ( ifHash.size() > 0 &&             
                 it.isIDLEntity() ) {                         
                String qName = t.getQualifiedName();   
                Enumeration k = ifHash.keys();
                while ( k.hasMoreElements() ) {      
                    String from = (String)k.nextElement();
                    if ( qName.startsWith( from ) ) {                    
                        String to = (String)ifHash.get( from );
                        p.pln( "#include \"" + to + "\"" );   
                        return;                                   
                    }
                }
            }
        }
        else if ( t.isArray() ) ;        
        else return;                             
        p.p( "#include \"" );                    
        for ( int i1 = 0; i1 < modNames.length; i1++ ) p.p( modNames[i1] + "/" );
        p.p( tName + ".idl\"" );
        p.pln();
    }
    protected String getQualifiedName(
                                      Type t ) {
        String[] modNames = getIDLModuleNames( t );
        int len = modNames.length;
        StringBuffer buf = new StringBuffer();
        for ( int i1 = 0; i1 < len; i1++ )
            buf.append( modNames[i1] + "." );
        buf.append( t.getIDLName() );
        return buf.toString();
    }
    protected String getQualifiedIDLName(Type t) {
        if ( t.isPrimitive() )
            return t.getIDLName();
        if ( !t.isArray() &&
             "org.omg.CORBA.Object".equals( t.getQualifiedName() ) )
            return t.getIDLName();
        String[] modNames = getIDLModuleNames( t );
        int len = modNames.length;
        if (len > 0) {
            StringBuffer buf = new StringBuffer();
            for ( int i1 = 0; i1 < len; i1++ )
                buf.append( IDL_NAME_SEPARATOR + modNames[i1] );
            buf.append( IDL_NAME_SEPARATOR + t.getIDLName() );
            return buf.toString();
        } else {
            return t.getIDLName();
        }
    }
    protected String[] getIDLModuleNames(Type t) {
        String[] modNames = t.getIDLModuleNames();      
        CompoundType ct;
        if ( t.isCompound() ) {
            ct = (CompoundType)t;
            if ( !ct.isIDLEntity ) return modNames;     
            if ( "org.omg.CORBA.portable.IDLEntity"
                 .equals( t.getQualifiedName() ) )
                return modNames;
        }
        else if ( t.isArray() ) {
            Type et = t.getElementType();
            if ( et.isCompound() ) {
                ct = (CompoundType)et;
                if ( !ct.isIDLEntity ) return modNames;   
                if ( "org.omg.CORBA.portable.IDLEntity"
                     .equals( t.getQualifiedName() ) )
                    return modNames;
            }
            else return modNames;
        }
        else return modNames;              
        Vector mVec = new Vector();
        if ( !translateJavaPackage( ct,mVec ) )      
            stripJavaPackage( ct,mVec );             
        if ( ct.isBoxed() ) {                            
            mVec.insertElementAt( "org",0 );
            mVec.insertElementAt( "omg",1 );
            mVec.insertElementAt( "boxedIDL",2 );
        }
        if ( t.isArray() ) {                             
            mVec.insertElementAt( "org",0 );
            mVec.insertElementAt( "omg",1 );
            mVec.insertElementAt( "boxedRMI",2 );
        }
        String[] outArr = new String[mVec.size()];
        mVec.copyInto( outArr );
        return outArr;
    }
    protected boolean translateJavaPackage(
                                           CompoundType ct,
                                           Vector vec ) {
        vec.removeAllElements();
        boolean ret = false;
        String fc = null;
        if ( ! ct.isIDLEntity() ) return ret;
        String pName = ct.getPackageName();         
        if ( pName == null ) return ret;
        StringTokenizer pt = new StringTokenizer( pName,"." );
        while ( pt.hasMoreTokens() ) vec.addElement( pt.nextToken() );
        if ( imHash.size() > 0 ) {           
            Enumeration k = imHash.keys();
        nextModule:
            while ( k.hasMoreElements() ) {      
                String from = (String)k.nextElement();                  
                StringTokenizer ft = new StringTokenizer( from,"." );
                int vecLen = vec.size();
                int ifr;
                for ( ifr = 0; ifr < vecLen && ft.hasMoreTokens(); ifr++ )
                    if ( ! vec.elementAt(ifr).equals( ft.nextToken() ) )
                        continue nextModule;                                  
                if ( ft.hasMoreTokens() ) {                          
                    fc = ft.nextToken();                         
                    if ( ! ct.getName().equals( fc ) ||             
                         ft.hasMoreTokens() )
                        continue nextModule;                                   
                }
                ret = true;                                             
                for ( int i4 = 0; i4 < ifr; i4++ )
                    vec.removeElementAt( 0 );                     
                String to = (String)imHash.get( from );                   
                StringTokenizer tt = new StringTokenizer( to,IDL_NAME_SEPARATOR );
                int itoco = tt.countTokens();
                int ito = 0;
                if ( fc != null ) itoco--;               
                for ( ito = 0; ito < itoco; ito++ )
                    vec.insertElementAt( tt.nextToken(),ito );      
                if ( fc != null ) {
                    String tc = tt.nextToken();
                    if ( ! ct.getName().equals( tc ) )           
                        vec.insertElementAt( tc,ito );           
                }
            }
        }
        return ret;
    }
    protected void stripJavaPackage(
                                    CompoundType ct,
                                    Vector vec ) {
        vec.removeAllElements();
        if ( ! ct.isIDLEntity() ) return;
        String repID = ct.getRepositoryID().substring( 4 );
        StringTokenizer rept = new StringTokenizer( repID,"/" );
        if ( rept.countTokens() < 2 ) return;
        while ( rept.hasMoreTokens() )
            vec.addElement( rept.nextToken() );
        vec.removeElementAt( vec.size() - 1 );
        String pName = ct.getPackageName();         
        if ( pName == null ) return;
        Vector pVec = new Vector();
        StringTokenizer pt = new StringTokenizer( pName,"." );
        while ( pt.hasMoreTokens() ) pVec.addElement( pt.nextToken() );
        int i1 = vec.size() - 1;
        int i2 = pVec.size() - 1;
        while ( i1 >= 0 && i2 >= 0 ) {                      
            String rep = (String)( vec.elementAt( i1 ) );
            String pkg = (String)( pVec.elementAt( i2 ) );
            if ( ! pkg.equals( rep ) ) break;
            i1--; i2--;
        }
        for ( int i3 = 0; i3 <= i1; i3++ )
            vec.removeElementAt( 0 );                                  
    }
    protected void writeSequence(
                                 OutputType ot,
                                 IndentingWriter p)
        throws IOException {
        ArrayType at = (ArrayType)ot.getType();
        Type et = at.getElementType();
        String fName = ot.getName();
        int dim = Integer.parseInt( fName.substring( 3,fName.indexOf( "_" ) ) );
        String idlName = unEsc( et.getIDLName() ).replace( ' ','_' );
        String qIdlName = getQualifiedIDLName( et );
        String qName = et.getQualifiedName();
        String repID = at.getRepositoryID();
        int rix1 = repID.indexOf( '[' );                       
        int rix2 = repID.lastIndexOf( '[' ) + 1;
            StringBuffer rid = new StringBuffer(
                                            repID.substring( 0,rix1 ) +
                                            repID.substring( rix2 ) );
        for ( int i1 = 0; i1 < dim; i1++ ) rid.insert( rix1,'[' );
        String vtName = "seq" + dim + "_" + idlName;
        boolean isFromIDL = false;
        if ( et.isCompound() ) {
            CompoundType ct = (CompoundType)et;
            isFromIDL = ct.isIDLEntity() || ct.isCORBAObject();
        }
        boolean isForwardInclude =
            et.isCompound() &&
            !isSpecialReference( et ) &&
            dim == 1 &&
            !isFromIDL &&
            !"org.omg.CORBA.Object".equals(qName) &&
            !"java.lang.String".equals(qName);
        writeBanner( at,dim,!isException,p );
        if ( dim == 1 && "java.lang.String".equals(qName) )          
            writeIncOrb( p );
        if ( dim == 1 && "org.omg.CORBA.Object".equals(qName) ) ;
        else if ( isSpecialReference( et ) || dim > 1 || isFromIDL )
            writeInclude( at,dim-1,!isThrown,p );               
        writeIfndef( at,dim,!isException,!isForward,p );
        if ( isForwardInclude )
            writeForwardReference( at,dim-1,p );                    
        writeModule1( at,p );
                p.pln();p.pI();
                p.p( "valuetype " + vtName );
                p.p( " sequence<" );
        if ( dim == 1 ) p.p( qIdlName );
                else {
            p.p( "seq" + ( dim - 1 ) + "_"  );
                    p.p( idlName );
                }
                p.pln( ">;" );
                p.pO();p.pln();
                p.pln( "#pragma ID " + vtName + " \"" + rid + "\"" );
                p.pln();
        writeModule2( at,p );
        if ( isForwardInclude )
            writeInclude( at,dim-1,!isThrown,p );      
                writeEndif( p );
            }
    protected void writeBoxedIDL(
                                 CompoundType t,
                                 IndentingWriter p)
        throws IOException {
        String[] boxNames = getIDLModuleNames( t );
        int len = boxNames.length;
        String[] modNames = new String[len - 3];               
        for ( int i1 = 0; i1 < len - 3; i1++ ) modNames[i1] = boxNames[i1 + 3];
        String tName = unEsc( t.getIDLName() );
        writeBanner( t,0,!isException,p );
        writeInclude( t,modNames,tName,p );
        writeIfndef( t,0,!isException,!isForward,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.p( "valuetype " + tName + " " );
        for ( int i1 = 0; i1 < modNames.length; i1++ )
            p.p( IDL_NAME_SEPARATOR + modNames[i1] );
        p.pln( IDL_NAME_SEPARATOR + tName + ";" );
        p.pO();p.pln();
        writeRepositoryID( t,p );
        p.pln();
        writeModule2( t,p );
        writeEndif( p );
        }
    protected void writeException(
                                  ClassType t,
                                  IndentingWriter p)
        throws IOException {
        writeBanner( t,0,isException,p );
        writeIfndef( t,0,isException,!isForward,p );
        writeForwardReference( t,p );
        writeModule1( t,p );
        p.pln();p.pI();
        p.pln( "exception " + t.getIDLExceptionName() + " {" );
        p.pln();p.pI();
        p.pln( t.getIDLName() + " value;" );
        p.pO();p.pln();
        p.pln( "};" );
        p.pO();p.pln();
        writeModule2( t,p );
        writeInclude( t,0,!isThrown,p );               
        writeEndif( p );
    }
    protected void writeRepositoryID(
                                     Type t,
                                     IndentingWriter p )
        throws IOException {
        String repid = t.getRepositoryID();
        if ( t.isCompound() ) {
            CompoundType ct = (CompoundType)t;
            if ( ct.isBoxed() )
                repid = ct.getBoxedRepositoryID();
        }
        p.pln( "#pragma ID " + t.getIDLName() + " \"" +
               repid + "\"" );
    }
    protected void writeInherits(
                                 Hashtable inhHash,
                                 boolean forValuetype,
                                 IndentingWriter p )
        throws IOException {
        int itot = inhHash.size();
        int iinh = 0;
        int isup = 0;
        if ( itot < 1 ) return;                         
        Enumeration inhEnum = inhHash.elements();
        CompoundType ct;
        if ( forValuetype )
            while ( inhEnum.hasMoreElements() ) {
                ct = (CompoundType)inhEnum.nextElement();
                if ( ct.getTypeCode() == TYPE_ABSTRACT ) isup++;
            }
        iinh = itot - isup;
        if ( iinh > 0 ) {
        p.p( ": " );
            inhEnum = inhHash.elements();
        while ( inhEnum.hasMoreElements() ) {         
                ct = (CompoundType)inhEnum.nextElement();
                if ( ct.isClass() ) {
                    p.p( getQualifiedIDLName( ct ) );
                    if ( iinh > 1 ) p.p( ", " );               
                    else if ( itot > 1 ) p.p( " " );
                break;                                                
            }
        }
            int i = 0;
        inhEnum = inhHash.elements();
        while ( inhEnum.hasMoreElements() ) {     
                ct = (CompoundType)inhEnum.nextElement();
                if ( !ct.isClass() &&
                     !( ct.getTypeCode() == TYPE_ABSTRACT ) ) {
                    if ( i++ > 0 ) p.p( ", " );                     
                    p.p( getQualifiedIDLName( ct ) );
            }
        }
    }
        if ( isup > 0 ) {                    
            p.p( " supports " );
            int i = 0;
            inhEnum = inhHash.elements();
            while ( inhEnum.hasMoreElements() ) {
                ct = (CompoundType)inhEnum.nextElement();
                if ( ct.getTypeCode() == TYPE_ABSTRACT ) {
                    if ( i++ > 0 ) p.p( ", " );                     
                    p.p( getQualifiedIDLName( ct ) );
                }
            }
        }
    }
    protected void writeConstant(
                                 CompoundType.Member constant,
                                 IndentingWriter p )
        throws IOException {
        Type t = constant.getType();
        p.p( "const " );
        p.p( getQualifiedIDLName( t ) );
        p.p( " " + constant.getIDLName() + " = " + constant.getValue() );
        p.pln( ";" );
    }
    protected void writeData(
                             CompoundType.Member data,
                             IndentingWriter p )
        throws IOException {
        if ( data.isInnerClassDeclaration() ) return;                      
        Type t = data.getType();
        if ( data.isPublic() )
            p.p( "public " );
        else p.p( "private " );
        p.pln( getQualifiedIDLName( t ) +  " " +
               data.getIDLName() + ";" );
    }
    protected void writeAttribute(
                                  CompoundType.Method attr,
                                  IndentingWriter p )
        throws IOException {
        if ( attr.getAttributeKind() == ATTRIBUTE_SET ) return;  
        Type t = attr.getReturnType();
        if ( !attr.isReadWriteAttribute() ) p.p( "readonly " );
        p.p( "attribute " + getQualifiedIDLName( t ) + " " );
        p.pln( attr.getAttributeName() + ";" );
    }
    protected void writeMethod(
                               CompoundType.Method method,
                               IndentingWriter p )
        throws IOException {
        if ( method.isAttribute() ) {
            writeAttribute( method,p );
            return;
        }
        Type[]    pts = method.getArguments();
        String[]  paramNames = method.getArgumentNames();
        Type      rt = method.getReturnType();
        Hashtable excHash = new Hashtable();
        getExceptions( method,excHash );
        if ( method.isConstructor() )
            if ( factory ) p.p( "factory " + method.getIDLName() + "(" );
            else p.p( "init(" );                                    
        else {
            p.p( getQualifiedIDLName( rt ) );
            p.p( " " + method.getIDLName() + "(" );
        }
        p.pI();
        for ( int i=0; i < pts.length; i++ ) {
            if ( i > 0 ) p.pln( "," );               
            else p.pln();
            p.p( "in " );
            p.p( getQualifiedIDLName( pts[i] ) );
            p.p( " " + paramNames[i] );
        }
        p.pO();
        p.p( " )" );
        if ( excHash.size() > 0 ) {                      
            p.pln( " raises (" );
            p.pI();
            int i = 0;
            Enumeration excEnum = excHash.elements();
            while ( excEnum.hasMoreElements() ) {
                ValueType exc = (ValueType)excEnum.nextElement();
                if ( i > 0 ) p.pln( "," );                   
                if ( exc.isIDLEntityException() )
                    if ( exc.isCORBAUserException() )
                        p.p( "::org::omg::CORBA::UserEx" );
                    else {
                        String[] modNames = getIDLModuleNames( exc );
                        for ( int i2 = 0; i2 < modNames.length; i2++ )
                            p.p( IDL_NAME_SEPARATOR + modNames[i2] );
                        p.p( IDL_NAME_SEPARATOR + exc.getName() );
                    }
                else p.p( exc.getQualifiedIDLExceptionName( true ) );
                i++;
            }
            p.pO();
            p.p( " )" );
        }
        p.pln( ";" );
    }
    protected String unEsc(
                           String name ) {
        if ( name.startsWith( "_" ) ) return name.substring( 1 );
        else return name;
    }
    protected void writeBanner(
                               Type t,
                               int dim,
                               boolean isException,
                               IndentingWriter p )
        throws IOException {
        String[] modNames = getIDLModuleNames( t );             
        String fName = unEsc( t.getIDLName() );                 
        if ( isException && t.isClass() ) {
            ClassType ct = (ClassType)t;                    
            fName = unEsc( ct.getIDLExceptionName() );
        }
        if ( dim > 0 && t.isArray() ) {
            Type et = t.getElementType();                    
            fName = "seq" + dim + "_" + unEsc( et.getIDLName().replace( ' ','_' ) );
        }
        p.pln( "" );
        p.pln();
    }
    protected void writeIncOrb(
                               IndentingWriter p )
        throws IOException {
        p.pln( "#include \"orb.idl\"" );
    }
    protected void writeIfndef(
                               Type t,
                               int dim,
                               boolean isException,
                               boolean isForward,
                               IndentingWriter p )
        throws IOException {
        String[] modNames = getIDLModuleNames( t );             
        String fName = unEsc( t.getIDLName() );                 
        if ( isException && t.isClass() ) {
            ClassType ct = (ClassType)t;                    
            fName = unEsc( ct.getIDLExceptionName() );
        }
        if ( dim > 0 && t.isArray() ) {
            Type et = t.getElementType();                    
            fName = "seq" + dim + "_" + unEsc( et.getIDLName().replace( ' ','_' ) );
        }
        p.pln();
        p.p( "#ifndef __" );
        for ( int i = 0; i < modNames.length; i++ ) p.p( modNames[i] + "_" );
        p.pln( fName + "__" );
        if ( !isForward ) {
        p.p( "#define __" );
        for ( int i = 0; i < modNames.length; i++ ) p.p( modNames[i] + "_" );
            p.pln( fName + "__" );
            p.pln();
    }
    }
    protected void writeEndif(
                              IndentingWriter p )
        throws IOException {
        p.pln("#endif");
        p.pln();
    }
    protected void writeModule1(
                                Type t,
                                IndentingWriter p )
        throws IOException {
        String[] modNames = getIDLModuleNames( t );
        p.pln();
        for ( int i = 0; i < modNames.length; i++ )
            p.pln( "module " + modNames[i] + " {" );
    }
    protected void writeModule2(
                                Type t,
                                IndentingWriter p )
        throws IOException {
        String[] modNames = getIDLModuleNames( t );
        for ( int i=0; i < modNames.length; i++ ) p.pln( "};" );
        p.pln();
    }
}
