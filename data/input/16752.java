public class InputMethod extends Properties.Entity {
    private String name;
    private int bci;
    private String shortName;
    private List<InputMethod> inlined;
    private InputMethod parentMethod;
    private Group group;
    private List<InputBytecode> bytecodes;
    public InputMethod(Group parent, String name, String shortName, int bci) {
        this.group = parent;
        this.name = name;
        this.bci = bci;
        this.shortName = shortName;
        inlined = new ArrayList<InputMethod>();
        bytecodes = new ArrayList<InputBytecode>();
    }
    public List<InputBytecode> getBytecodes() {
        return Collections.unmodifiableList(bytecodes);
    }
    public List<InputMethod> getInlined() {
        return Collections.unmodifiableList(inlined);
    }
    public void addInlined(InputMethod m) {
        for (InputMethod m2 : inlined) {
            assert m2.getBci() != m.getBci();
        }
        inlined.add(m);
        assert m.parentMethod == null;
        m.parentMethod = this;
        for (InputBytecode bc : bytecodes) {
            if (bc.getBci() == m.getBci()) {
                bc.setInlined(m);
            }
        }
    }
    public Group getGroup() {
        return group;
    }
    public String getShortName() {
        return shortName;
    }
    public void setBytecodes(String text) {
        String[] strings = text.split("\n");
        int oldNumber = -1;
        for (String s : strings) {
            if (s.length() > 0 && Character.isDigit(s.charAt(0))) {
                s = s.trim();
                int spaceIndex = s.indexOf(' ');
                String numberString = s.substring(0, spaceIndex);
                String tmpName = s.substring(spaceIndex + 1, s.length());
                int number = -1;
                number = Integer.parseInt(numberString);
                assert number > oldNumber;
                InputBytecode bc = new InputBytecode(number, tmpName);
                bytecodes.add(bc);
                for (InputMethod m : inlined) {
                    if (m.getBci() == number) {
                        bc.setInlined(m);
                        break;
                    }
                }
            }
        }
    }
    public String getName() {
        return name;
    }
    public int getBci() {
        return bci;
    }
}
