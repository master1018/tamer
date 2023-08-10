public class SecureDiceBag implements DiceBag, DataListener {
    protected static final char REROLL = ((char) 0x12);
    private Connection connection;
    private String nick;
    private Random random = new Random();
    private RerollState reroll = new RerollState();
    private Hashtable localIndexToDie = new Hashtable();
    private Hashtable remoteIndexToDie = new Hashtable();
    private Hashtable localIndexToRoll = new Hashtable();
    private Hashtable remoteIndexToRoll = new Hashtable();
    private int nextLocalIndex = 1;
    public SecureDiceBag(Connection connection, String nick) {
        this.connection = connection;
        this.nick = nick;
        connection.registerClass(DiceMessage.class, "dice");
        connection.addDataListener(this, String.class, true);
    }
    public Random getRandom() {
        return random;
    }
    public void rollDone(SecureDie die, int result) {
        DiceRoll roll;
        Integer index = new Integer(die.getIndex());
        if (die.isAlice()) {
            localIndexToDie.remove(index);
            roll = (DiceRoll) localIndexToRoll.get(index);
        } else {
            remoteIndexToDie.remove(index);
            roll = (DiceRoll) remoteIndexToRoll.get(index);
        }
        int diceIndex = die.getDiceIndex();
        roll.dice[diceIndex] = result;
        diceIndex++;
        if (diceIndex == roll.getDiceCount()) {
            String s = roll.roll();
            if (die.isAlice()) {
                localIndexToRoll.remove(index);
                if (s != null) {
                    connection.doLocalAction(false, connection.getNick(), "rolled " + s);
                    connection.sendData(null, new DiceMessage(false, s));
                    if (s.indexOf("Pro roll successful") > -1) {
                        DiceRoll proRoll = reroll.doProReroll();
                        send("pro " + nextLocalIndex + " " + reroll.getLastRoll());
                        Integer proIndex = new Integer(nextLocalIndex);
                        localIndexToRoll.put(proIndex, proRoll);
                        SecureDie proDie = new SecureDie(this, nextLocalIndex, 0, proRoll.getDiceType(0));
                        localIndexToDie.put(proIndex, proDie);
                        proDie.startRoll();
                        nextLocalIndex++;
                    }
                }
            } else {
                remoteIndexToRoll.remove(index);
                if (s != null) connection.doLocalAction(false, nick, "rolled " + s);
            }
        } else {
            if (die.isAlice()) {
                SecureDie newDie = new SecureDie(this, index.intValue(), diceIndex, roll.getDiceType(diceIndex));
                localIndexToDie.put(index, newDie);
                newDie.startRoll();
            }
        }
    }
    public void failedRoll(SecureDie die, String msg) {
        failedRoll(die);
        if (die.isAlice()) connection.sendAction(null, "-- protocol failure: " + msg);
    }
    public void failedRoll(SecureDie die) {
        DiceRoll roll;
        Integer index = new Integer(die.getIndex());
        if (die.isAlice()) {
            localIndexToDie.remove(index);
            localIndexToRoll.remove(index);
        } else {
            remoteIndexToDie.remove(index);
            remoteIndexToRoll.remove(index);
        }
        if (die.isAlice()) connection.sendAction(null, "-- dice roll failed");
    }
    public void send(String msg) {
        connection.sendData(nick, msg);
    }
    private static boolean startsWith(String s1, String s2) {
        return (s1.length() >= s2.length()) ? s1.substring(0, s2.length()).equalsIgnoreCase(s2) : false;
    }
    public void roll(String dice) {
        connection.doLocalAction(false, connection.getNick(), "rolls " + dice);
        connection.sendData(null, new DiceMessage(true, dice));
        DiceRoll diceRoll;
        if (startsWith(dice, "rr") || startsWith(dice, "reroll")) {
            dice += REROLL + reroll.toString();
            diceRoll = reroll.doReroll();
        } else if (startsWith(dice, "pro")) {
            dice += REROLL + reroll.toString();
            diceRoll = reroll.doPro();
        } else {
            diceRoll = new DiceRoll(dice);
            reroll.setLastRoll(dice);
        }
        send("roll " + nextLocalIndex + " " + dice);
        Integer index = new Integer(nextLocalIndex);
        localIndexToRoll.put(index, diceRoll);
        SecureDie die = new SecureDie(this, nextLocalIndex, 0, diceRoll.getDiceType(0));
        localIndexToDie.put(index, die);
        die.startRoll();
        nextLocalIndex++;
    }
    public void onData(boolean priv, String nick, Object data) {
        if (nick.equalsIgnoreCase(this.nick)) {
            try {
                String msg = (String) data;
                StringTokenizer st = new StringTokenizer(msg);
                String s = st.nextToken();
                if (s.equalsIgnoreCase("roll")) {
                    Integer n = new Integer(st.nextToken());
                    String dice = msg.substring(msg.indexOf(' ', msg.indexOf(' ') + 1) + 1);
                    RerollState reroll = null;
                    int i = dice.indexOf(REROLL);
                    if (i > -1) {
                        reroll = new RerollState(dice.substring(i + 1));
                        dice = dice.substring(0, i);
                    }
                    connection.doLocalAction(false, nick, "rolls " + dice);
                    DiceRoll diceRoll;
                    if (startsWith(dice, "rr") || startsWith(dice, "reroll")) diceRoll = reroll.doReroll(); else if (startsWith(dice, "pro")) diceRoll = reroll.doPro(); else diceRoll = new DiceRoll(dice);
                    remoteIndexToRoll.put(n, diceRoll);
                } else if (s.equalsIgnoreCase("pro")) {
                    Integer n = new Integer(st.nextToken());
                    String dice = msg.substring(msg.indexOf(' ', msg.indexOf(' ') + 1) + 1);
                    remoteIndexToRoll.put(n, new DiceRoll(dice));
                } else if (s.equalsIgnoreCase("die")) {
                    int n = Integer.parseInt(st.nextToken());
                    SecureDie die;
                    if (n < 0) die = (SecureDie) localIndexToDie.get(new Integer(-n)); else die = (SecureDie) remoteIndexToDie.get(new Integer(n));
                    if (die != null) die.receive(msg.substring(msg.indexOf(' ', msg.indexOf(' ') + 1) + 1)); else if (n > 0) {
                        die = new SecureDie(this, n, msg.substring(msg.indexOf(' ', msg.indexOf(' ') + 1) + 1));
                        remoteIndexToDie.put(new Integer(n), die);
                        die.startRoll();
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
