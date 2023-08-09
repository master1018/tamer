public class FontExtraMetrics {
    private float lAverageCharWidth;
    private float lSubscriptSizeX;
    private float lSubscriptSizeY; 
    private float lSubscriptOffsetX; 
    private float lSubscriptOffsetY;
    private float lSuperscriptSizeX; 
    private float lSuperscriptSizeY;
    private float lSuperscriptOffsetX;
    private float lSuperscriptOffsetY;
    public FontExtraMetrics(){
    }
    public FontExtraMetrics(float[] metrics){
        lAverageCharWidth = metrics[0];
        lSubscriptSizeX = metrics[1];
        lSubscriptSizeY = metrics[2];
        lSubscriptOffsetX = metrics[3];
        lSubscriptOffsetY = metrics[4];
        lSuperscriptSizeX = metrics[5];
        lSuperscriptSizeY = metrics[6];
        lSuperscriptOffsetX = metrics[7];
        lSuperscriptOffsetY = metrics[8];
    }
    public float getAverageCharWidth(){
        return lAverageCharWidth;
    }
    public float getSubscriptSizeX(){
        return lSubscriptSizeX;
    }
    public float getSubscriptSizeY(){
        return lSubscriptSizeY;
    }
    public float getSubscriptOffsetX(){
        return lSubscriptOffsetX;
    }
    public float getSubscriptOffsetY(){
        return lSubscriptOffsetY;
    }
    public float getSuperscriptSizeX(){
        return lSuperscriptSizeX;
    }
    public float getSuperscriptSizeY(){
        return lSuperscriptSizeY;
    }
    public float getSuperscriptOffsetX(){
        return lSuperscriptOffsetX;
    }
    public float getSuperscriptOffsetY(){
        return lSuperscriptOffsetY;
    }
}
