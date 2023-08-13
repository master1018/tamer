public class WnnSentence extends WnnWord {
    public ArrayList<WnnClause> elements;
    public WnnSentence(String input, ArrayList<WnnClause> clauses) {
        if (clauses == null || clauses.isEmpty()) {
            this.id = 0;
            this.candidate = "";
            this.stroke = "";
            this.frequency = 0;
            this.partOfSpeech = new WnnPOS();
            this.attribute = 0;
        } else {
            this.elements = clauses;
            WnnClause headClause = (WnnClause)clauses.get(0);
            if (clauses.size() == 1) {
                this.id = headClause.id;
                this.candidate = headClause.candidate;
                this.stroke = input;
                this.frequency = headClause.frequency;
                this.partOfSpeech = headClause.partOfSpeech;
                this.attribute = headClause.attribute;
            } else {
                StringBuffer candidate = new StringBuffer();
                Iterator<WnnClause> ci = clauses.iterator();
                while (ci.hasNext()) {
                    WnnClause clause = ci.next();
                    candidate.append(clause.candidate);
                }
                WnnClause lastClause = (WnnClause)clauses.get(clauses.size() - 1);
                this.id = headClause.id;
                this.candidate = candidate.toString();
                this.stroke = input;
                this.frequency = headClause.frequency;
                this.partOfSpeech = new WnnPOS(headClause.partOfSpeech.left, lastClause.partOfSpeech.right);
                this.attribute = 2;
            }
        }
    }
    public WnnSentence(String input, WnnClause clause) {
        this.id = clause.id;
        this.candidate = clause.candidate;
        this.stroke = input;
        this.frequency = clause.frequency;
        this.partOfSpeech = clause.partOfSpeech;
        this.attribute = clause.attribute;
        this.elements = new ArrayList<WnnClause>();
        this.elements.add(clause);
    }
    public WnnSentence(WnnSentence prev, WnnClause clause) {
        this.id = prev.id;
        this.candidate = prev.candidate + clause.candidate;
        this.stroke = prev.stroke + clause.stroke;
        this.frequency = prev.frequency + clause.frequency;
        this.partOfSpeech = new WnnPOS(prev.partOfSpeech.left, clause.partOfSpeech.right);
        this.attribute = prev.attribute;
        this.elements = new ArrayList<WnnClause>();
        this.elements.addAll(prev.elements);
        this.elements.add(clause);
    }
    public WnnSentence(WnnClause head, WnnSentence tail) {
        if (tail == null) {
            this.id = head.id;
            this.candidate = head.candidate;
            this.stroke = head.stroke;
            this.frequency = head.frequency;
            this.partOfSpeech = head.partOfSpeech;
            this.attribute = head.attribute;
            this.elements = new ArrayList<WnnClause>();
            this.elements.add(head);
        } else {
            this.id = head.id;
            this.candidate = head.candidate + tail.candidate;
            this.stroke = head.stroke + tail.stroke;
            this.frequency = head.frequency + tail.frequency;
            this.partOfSpeech = new WnnPOS(head.partOfSpeech.left, tail.partOfSpeech.right);
            this.attribute = 2;
            this.elements = new ArrayList<WnnClause>();
            this.elements.add(head);
            this.elements.addAll(tail.elements);
        }
    }
}
