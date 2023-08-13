public class FontProperty {
    String fileName = null;
    String encoding = null;
    int[] exclRange = null;
    String name = null;
    int style = -1;
    public int getStyle(){
        return this.style;
    }
    public String getName(){
        return this.name;
    }
    public String getEncoding(){
        return this.encoding;
    }
    public int[] getExclusionRange(){
        return this.exclRange;
    }
    public String getFileName(){
        return this.fileName;
    }
    public boolean isCharExcluded(char ch){
        if (exclRange == null ){
            return false;
        }
        for (int i = 0; i < exclRange.length;){
            int lb = exclRange[i++];
            int hb = exclRange[i++];
            if (ch >= lb && ch <= hb){
                return true;
            }
        }
        return false;
    }
}
