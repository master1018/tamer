public class Smiley extends PerfoMeter {
    protected String code;
    protected String imageHautePerf;
    protected String imageFaiblePerf;
    public Smiley(String aCode, String aImageHaute, String aImageBasse) {
        super();
        code = aCode;
        imageHautePerf = aImageHaute;
        imageFaiblePerf = aImageBasse;
    }
    public String getCode() {
        return code;
    }
    public String getImageFaiblePerf() {
        return imageFaiblePerf;
    }
    public String getImageHautePerf() {
        return imageHautePerf;
    }
    public void setCode(String string) {
        code = string;
    }
    public void setImageFaiblePerf(String url) {
        imageFaiblePerf = url;
    }
    public void setImageHautePerf(String url) {
        imageHautePerf = url;
    }
    public String getImage() {
        if (isHautePerf()) {
            return getImageHautePerf();
        } else {
            return getImageFaiblePerf();
        }
    }
}
