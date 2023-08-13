public class AndroidFontProperty extends FontProperty {
    String xlfd;
    String logicalName;
    String styleName;
    public AndroidFontProperty(String _logicalName, String _styleName, String _fileName, String _name, String _xlfd, int _style, int[] exclusionRange, String _encoding){
        this.logicalName = _logicalName;
        this.styleName = _styleName;
        this.name = _name;
        this.encoding = _encoding;
        this.exclRange = exclusionRange;
        this.fileName = _fileName;
        this.xlfd = _xlfd;
        this.style = _style;
    }
    public String getLogicalName(){
        return logicalName;
    }
    public String getStyleName(){
        return styleName;
    }
    public String getXLFD(){
        return xlfd;
    }
    public String toString(){
        return new String(this.getClass().getName() +
                "[name=" + name + 
                ",fileName="+ fileName + 
                ",Charset=" + encoding + 
                ",exclRange=" + exclRange + 
                ",xlfd=" + xlfd + "]"); 
    }
}
