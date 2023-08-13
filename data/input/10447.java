public class MetaPragma extends com.sun.tools.corba.se.idl.PragmaHandler {
    public static int metaKey = com.sun.tools.corba.se.idl.SymtabEntry.getVariableKey();
    public boolean process(String pragma, String currentToken) {
        if ( !pragma.equals("meta"))
            return false;
        com.sun.tools.corba.se.idl.SymtabEntry entry ;
        String msg;
        try {
            entry = scopedName();
            if ( entry == null){
                parseException(Messages.msg("idlemit.MetaPragma.scopedNameNotFound"));
                skipToEOL();
            }
            else {
                msg = (currentToken()+ getStringToEOL());
                Vector v;
                v = (Vector) entry.dynamicVariable(metaKey);
                if ( v== null){
                    v = new Vector();
                    entry.dynamicVariable(metaKey, v);
                }
                parseMsg(v, msg);
           }
        } catch(Exception e){
        }
        return true;
    }
    static public void processForward(com.sun.tools.corba.se.idl.ForwardEntry forwardEntry){
        Vector forwardMeta;
        try {
            forwardMeta = (Vector)forwardEntry.dynamicVariable(metaKey);
        } catch (Exception e){
            forwardMeta = null;
        }
        com.sun.tools.corba.se.idl.SymtabEntry forwardInterface = forwardEntry.type();
        if (forwardMeta != null && forwardInterface!= null) {
            Vector interfaceMeta;
            try {
                 interfaceMeta= (Vector)forwardInterface.dynamicVariable(metaKey);
            } catch ( Exception e){
                 interfaceMeta = null;
            }
            if ( interfaceMeta == null) {
                try {
                    forwardInterface.dynamicVariable(MetaPragma.metaKey, forwardMeta);
                } catch(Exception e){};
            }
            else if (interfaceMeta != forwardMeta) {
                for (int i=0; i < forwardMeta.size(); i++){
                    try {
                        Object obj = forwardMeta.elementAt(i);
                        interfaceMeta.addElement(obj);
                    } catch (Exception e){};
                }
            }
         }
    }
    private static int initialState = 0;
    private static int commentState = 1;
    private static int textState = 2;
    private static int finalState =3;
    private void parseMsg(Vector v, String msg){
        int state = initialState;
        String text = "";
        int index = 0;
        while ( state != finalState ){
             boolean isNoMore = index >= msg.length();
             char ch = ' ';
             boolean isSlashStar = false;
             boolean isSlashSlash = false;
             boolean isWhiteSpace = false;
             boolean isStarSlash = false;
             boolean isText = false;
             if (!isNoMore ){
                 ch = msg.charAt(index);
                 if (ch == '/' && index+1 < msg.length()){
                     if (msg.charAt(index+1) == '/'){
                         isSlashSlash = true;
                          index++;
                     }
                     else if (msg.charAt(index+1) == '*'){
                         isSlashStar= true;
                         index++;
                     } else isText = true;
                 }
                 else if (ch == '*' && index+1 < msg.length() ){
                     if (msg.charAt(index+1) == '/'){
                         isStarSlash = true;
                         index++;
                     } else isText = true;
                 }
                 else if ( Character.isSpace(ch) || (ch == ',') 
                              || (ch == ';') ) 
                     isWhiteSpace = true;
                 else isText = true;
            }
            if (state == initialState){
                   if (isSlashStar){
                      state = commentState;
                   }
                   else if (isSlashSlash || isNoMore){
                      state = finalState;
                   }
                   else if (isText){
                       state = textState;
                       text = text+ ch;
                   }
             }
             else if (state == commentState){
                   if (isNoMore){
                        state = finalState;
                   }
                   else if ( isStarSlash){
                        state = initialState;
                   }
             }
             else if (state == textState){
                   if (isNoMore || isStarSlash || isSlashSlash ||
                       isSlashStar || isWhiteSpace ){
                       if (!text.equals("")) {
                           v.addElement(text);
                           text = "";
                       }
                       if (isNoMore)
                            state = finalState;
                       else if (isSlashStar)
                            state = commentState;
                       else state = initialState;
                   }
                   else if (isText){
                       text = text+ch;
                   }
             }
             index++;
        }
    }
}
