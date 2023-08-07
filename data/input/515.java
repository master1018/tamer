public class UnitState extends SentenceHMMState {
    private Unit unit;
    private transient StatePath tail;
    private HMMPosition position = HMMPosition.INTERNAL;
    public UnitState(PronunciationState parent, int which, Unit unit) {
        super("U", parent, which);
        this.unit = unit;
        Unit[] units = parent.getPronunciation().getUnits();
        int length = units.length;
        if (units[length - 1] == Unit.SILENCE && length > 1) {
            length--;
        }
        if (length == 1) {
            position = HMMPosition.SINGLE;
        } else if (which == 0) {
            position = HMMPosition.BEGIN;
        } else if (which == length - 1) {
            position = HMMPosition.END;
        }
    }
    public Unit getUnit() {
        return unit;
    }
    public boolean isLast() {
        return position == HMMPosition.SINGLE || position == HMMPosition.END;
    }
    public String getName() {
        return super.getName() + "<" + unit + ">";
    }
    public String getValueSignature() {
        return unit.toString();
    }
    public String getPrettyName() {
        return unit.toString();
    }
    public String getTypeLabel() {
        return "Unit";
    }
    public HMMPosition getPosition() {
        return position;
    }
    public StatePath getTail() {
        return tail;
    }
    public void setTail(StatePath tail) {
        this.tail = tail;
    }
    public boolean isUnit() {
        return true;
    }
}
