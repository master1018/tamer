public final class CharsetProviderICU extends CharsetProvider{
    public CharsetProviderICU(){
    }
    public final Charset charsetForName(String charsetName) {
        String icuCanonicalName = NativeConverter.getICUCanonicalName(charsetName);      
        if(icuCanonicalName==null || icuCanonicalName.length()==0){
            return null;
        }
        try{
            long cn = NativeConverter.openConverter(icuCanonicalName);
            NativeConverter.closeConverter(cn);
        }catch (RuntimeException re) {
            return null;
        }
        return getCharset(icuCanonicalName);
    }
    private final Charset getCharset(String icuCanonicalName){
       String[] aliases = (String[])NativeConverter.getAliases(icuCanonicalName);    
       String canonicalName = NativeConverter.getJavaCanonicalName(icuCanonicalName);
       return (new CharsetICU(canonicalName,icuCanonicalName, aliases));  
    }
    public final void putCharsets(Map map) {
        String[] charsets = NativeConverter.getAvailable();        
        for(int i=0; i<charsets.length;i++){           
            if (!map.containsKey(charsets[i])){
                map.put(charsets[i], charsetForName(charsets[i]));
            }
        }
    }
    protected final class CharsetIterator implements Iterator{
      private String[] names;
      private int currentIndex;
      protected CharsetIterator(String[] strs){
        names = strs;
        currentIndex=0;
      }
      public boolean hasNext(){
        return (currentIndex< names.length);
      }
      public Object next(){
        if(currentIndex<names.length){
              return charsetForName(names[currentIndex++]);
        }else{
              throw new NoSuchElementException();
        }
      }
      public void remove() {
            throw new UnsupportedOperationException();
      }
    }
    public final Iterator charsets(){
          String[] charsets = NativeConverter.getAvailable();
          Iterator iter = new CharsetIterator(charsets);
          return iter;
    }
}
