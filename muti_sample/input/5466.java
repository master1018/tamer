public class InterOperableNamingImpl
{
    public String convertToString( org.omg.CosNaming.NameComponent[]
                                   theNameComponents )
    {
        String theConvertedString =
            convertNameComponentToString( theNameComponents[0] );
        String temp;
        for( int i = 1; i < theNameComponents.length; i++ ) {
            temp = convertNameComponentToString( theNameComponents[i] );
            if( temp != null ) {
                 theConvertedString =
                 theConvertedString + "/" +  convertNameComponentToString(
                     theNameComponents[i] );
            }
        }
        return theConvertedString;
    }
    private String convertNameComponentToString(
        org.omg.CosNaming.NameComponent theNameComponent )
    {
        if( ( ( theNameComponent.id == null )
            ||( theNameComponent.id.length() == 0 ) )
          &&( ( theNameComponent.kind == null )
            ||( theNameComponent.kind.length() == 0 ) ) )
        {
            return ".";
        }
        else if( ( theNameComponent.id == null )
               ||( theNameComponent.id.length() == 0 ) )
        {
            String kind = addEscape( theNameComponent.kind );
            return "." + kind;
        }
        else if( ( theNameComponent.kind == null )
               ||( theNameComponent.kind.length() == 0 ) )
        {
            String id = addEscape( theNameComponent.id );
            return id;
        }
        else {
            String id = addEscape( theNameComponent.id );
            String kind = addEscape( theNameComponent.kind );
            return (id + "." +  kind);
        }
    }
   private String addEscape( String value )
   {
        StringBuffer theNewValue;
        if( (value != null) && ( (value.indexOf('.') != -1 ) ||
                                 (value.indexOf('/') != -1)))
        {
            char c;
            theNewValue = new StringBuffer( );
            for( int i = 0; i < value.length( ); i++ ) {
                c = value.charAt( i );
                if( ( c != '.' ) && (c != '/' ) )
                {
                    theNewValue.append( c );
                }
                else {
                    theNewValue.append( '\\' );
                    theNewValue.append( c );
                }
            }
        }
        else {
            return value;
        }
        return new String( theNewValue );
   }
   public org.omg.CosNaming.NameComponent[] convertToNameComponent(
       String theStringifiedName )
       throws org.omg.CosNaming.NamingContextPackage.InvalidName
   {
        String[] theStringifiedNameComponents =
                 breakStringToNameComponents( theStringifiedName );
        if( ( theStringifiedNameComponents == null )
         || (theStringifiedNameComponents.length == 0 ) )
        {
            return null;
        }
        NameComponent[] theNameComponents =
            new NameComponent[theStringifiedNameComponents.length];
        for( int i = 0; i < theStringifiedNameComponents.length; i++ ) {
            theNameComponents[i] = createNameComponentFromString(
                theStringifiedNameComponents[i] );
        }
        return theNameComponents;
   }
   private String[] breakStringToNameComponents( String theStringifiedName ) {
       int[] theIndices = new int[100];
       int theIndicesIndex = 0;
       for(int index = 0; index <= theStringifiedName.length(); ) {
           theIndices[theIndicesIndex] = theStringifiedName.indexOf( '/',
                index );
           if( theIndices[theIndicesIndex] == -1 ) {
               index = theStringifiedName.length()+1;
           }
           else {
               if( (theIndices[theIndicesIndex] > 0 )
               && (theStringifiedName.charAt(
                   theIndices[theIndicesIndex]-1) == '\\') )
               {
                  index = theIndices[theIndicesIndex] + 1;
                  theIndices[theIndicesIndex] = -1;
               }
               else {
                  index = theIndices[theIndicesIndex] + 1;
                  theIndicesIndex++;
               }
           }
        }
        if( theIndicesIndex == 0 ) {
            String[] tempString = new String[1];
            tempString[0] = theStringifiedName;
            return tempString;
        }
        if( theIndicesIndex != 0 ) {
            theIndicesIndex++;
        }
        return StringComponentsFromIndices( theIndices, theIndicesIndex,
                                            theStringifiedName );
    }
   private String[] StringComponentsFromIndices( int[] theIndices,
          int indicesCount, String theStringifiedName )
   {
       String[] theStringComponents = new String[indicesCount];
       int firstIndex = 0;
       int lastIndex = theIndices[0];
       for( int i = 0; i < indicesCount; i++ ) {
           theStringComponents[i] = theStringifiedName.substring( firstIndex,
             lastIndex );
           if( ( theIndices[i] < theStringifiedName.length() - 1 )
             &&( theIndices[i] != -1 ) )
           {
               firstIndex = theIndices[i]+1;
           }
           else {
               firstIndex = 0;
               i = indicesCount;
           }
           if( (i+1 < theIndices.length)
            && (theIndices[i+1] < (theStringifiedName.length() - 1))
            && (theIndices[i+1] != -1) )
           {
               lastIndex = theIndices[i+1];
           }
           else {
               i = indicesCount;
           }
           if( firstIndex != 0 && i == indicesCount ) {
               theStringComponents[indicesCount-1] =
               theStringifiedName.substring( firstIndex );
           }
       }
       return theStringComponents;
   }
   private NameComponent createNameComponentFromString(
        String theStringifiedNameComponent )
        throws org.omg.CosNaming.NamingContextPackage.InvalidName
   {
        String id = null;
        String kind = null;
        if( ( theStringifiedNameComponent == null )
         || ( theStringifiedNameComponent.length( ) == 0)
         || ( theStringifiedNameComponent.endsWith(".") ) )
        {
            throw new org.omg.CosNaming.NamingContextPackage.InvalidName( );
        }
        int index = theStringifiedNameComponent.indexOf( '.', 0 );
        if( index == -1 ) {
            id = theStringifiedNameComponent;
        }
        else if( index == 0 ) {
            if( theStringifiedNameComponent.length( ) != 1 ) {
                kind = theStringifiedNameComponent.substring(1);
            }
        }
        else
        {
            if( theStringifiedNameComponent.charAt(index-1) != '\\' ) {
                id = theStringifiedNameComponent.substring( 0, index);
                kind = theStringifiedNameComponent.substring( index + 1 );
            }
            else {
                boolean kindfound = false;
                while( (index < theStringifiedNameComponent.length() )
                     &&( kindfound != true ) )
                {
                    index = theStringifiedNameComponent.indexOf( '.',index + 1);
                    if( index > 0 ) {
                        if( theStringifiedNameComponent.charAt(
                                index - 1 ) != '\\' )
                        {
                            kindfound = true;
                        }
                    }
                    else
                    {
                        index = theStringifiedNameComponent.length();
                    }
                }
                if( kindfound == true ) {
                    id = theStringifiedNameComponent.substring( 0, index);
                    kind = theStringifiedNameComponent.substring(index + 1 );
                }
                else {
                    id = theStringifiedNameComponent;
                }
            }
        }
        id = cleanEscapeCharacter( id );
        kind = cleanEscapeCharacter( kind );
        if( id == null ) {
                id = "";
        }
        if( kind == null ) {
                kind = "";
        }
        return new NameComponent( id, kind );
   }
   private String cleanEscapeCharacter( String theString )
   {
        if( ( theString == null ) || (theString.length() == 0 ) ) {
                return theString;
        }
        int index = theString.indexOf( '\\' );
        if( index == 0 ) {
            return theString;
        }
        else {
            StringBuffer src = new StringBuffer( theString );
            StringBuffer dest = new StringBuffer( );
            char c;
            for( int i = 0; i < theString.length( ); i++ ) {
                c = src.charAt( i );
                if( c != '\\' ) {
                    dest.append( c );
                } else {
                    if( i+1 < theString.length() ) {
                        char d = src.charAt( i + 1 );
                        if( Character.isLetterOrDigit(d) ) {
                            dest.append( c );
                        }
                    }
                }
            }
            return new String(dest);
        }
   }
    public String createURLBasedAddress( String address, String name )
        throws InvalidAddress
    {
        String theurl = null;
        if( ( address == null )
          ||( address.length() == 0 ) ) {
            throw new InvalidAddress();
        }
        else {
            theurl = "corbaname:" + address + "#" + encode( name );
        }
        return theurl;
    }
    private String encode( String stringToEncode ) {
        StringWriter theStringAfterEscape = new StringWriter();
        int byteCount = 0;
        for( int i = 0; i < stringToEncode.length(); i++ )
        {
            char c = stringToEncode.charAt( i ) ;
            if( Character.isLetterOrDigit( c ) ) {
                theStringAfterEscape.write( c );
            }
            else if((c == ';') || (c == '/') || (c == '?')
            || (c == ':') || (c == '@') || (c == '&') || (c == '=')
            || (c == '+') || (c == '$') || (c == ';') || (c == '-')
            || (c == '_') || (c == '.') || (c == '!') || (c == '~')
            || (c == '*') || (c == ' ') || (c == '(') || (c == ')') )
            {
                theStringAfterEscape.write( c );
            }
            else {
                theStringAfterEscape.write( '%' );
                String hexString = Integer.toHexString( (int) c );
                theStringAfterEscape.write( hexString );
            }
        }
        return theStringAfterEscape.toString();
    }
}
