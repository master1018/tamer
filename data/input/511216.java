public class WnnWord {
    public int      id;
    public String   candidate;
    public String   stroke;
    public int      frequency;
    public WnnPOS   partOfSpeech;
    public int      attribute;
    public WnnWord() {
        this(0, "", "", new WnnPOS(), 0, 0);
    }
    public WnnWord(String candidate, String stroke) {
        this(0, candidate, stroke, new WnnPOS(), 0, 0);
    }
    public WnnWord(String candidate, String stroke, int frequency) {
        this(0, candidate, stroke, new WnnPOS(), frequency, 0);
    }
    public WnnWord(String candidate, String stroke, WnnPOS posTag) {
        this(0, candidate, stroke, posTag, 0, 0);
    }
    public WnnWord(String candidate, String stroke, WnnPOS posTag, int frequency) {
        this(0, candidate, stroke, posTag, frequency, 0);
    }
    public WnnWord(int id, String candidate, String stroke, WnnPOS posTag, int frequency) {
        this(id, candidate, stroke, posTag, frequency, 0);
    }
    public WnnWord(int id, String candidate, String stroke, WnnPOS posTag, int frequency, int attribute) {
        this.id = id;
        this.candidate = candidate;
        this.stroke = stroke;
        this.frequency = frequency;
        this.partOfSpeech = posTag;
        this.attribute = attribute;
    }
}
