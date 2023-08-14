public class NameTests {
    public static void main(String args[]) throws Exception {
        String[] rdnStr = new String[] {"one=voilet"};
        ArrayList rdnList = new ArrayList();
        for (int i = 0; i < rdnStr.length; i++) {
            rdnList.add(i, new Rdn(rdnStr[i]));
        }
        LdapName dn = new LdapName(rdnList);
        Collection rdns = dn.getRdns();
        System.out.println("size is :" + dn.size());
        System.out.println("isEmpty :" + dn.isEmpty());
        System.out.println("************Printing as Rdns*********");
        Iterator iter = rdns.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
        System.out.println();
        System.out.println("************Printing the Enumeration*********");
        Enumeration dnEnum = dn.getAll();
        while (dnEnum.hasMoreElements()) {
            System.out.println(dnEnum.nextElement());
        }
        System.out.println();
        LdapName nameSuffix = new LdapName("two=Indigo");
        System.out.println("addAll():" + dn.addAll(nameSuffix));
        ArrayList list = new ArrayList();
        list.add(new Rdn("five=Yellow"));
        System.out.println("Rdn- addAll():" + dn.addAll(list));
        nameSuffix = new LdapName("three=Blue");
        System.out.println();
        System.out.println("addAll at pos = 2");
        System.out.println("addAll():" + dn.addAll(2, nameSuffix));
        list = new ArrayList();
        list.add(new Rdn("four=Green"));
        System.out.println();
        System.out.println("addAll at pos = 3");
        System.out.println("Rdn- addAll():" + dn.addAll(3, list));
        Rdn rdn;
        System.out.println();
        System.out.println("add():" + dn.add("eight=white"));
        rdn = new Rdn("nine=Black");
        System.out.println();
        System.out.println("Rdn- add():" + dn.add(rdn));
        System.out.println();
        System.out.println("add() at pos 5");
        System.out.println("add():" + dn.add(5, "six=Orange"));
        rdn = new Rdn("six=Orange");
        System.out.println();
        System.out.println("add() at pos 6");
        System.out.println("Rdn- add():" + dn.add(6, "seven=Red"));
        System.out.println();
        System.out.println("Removing entries at positions: 7, 8");
        System.out.println("Removed:" + dn.remove(8));
        System.out.println("Removed:" + dn.remove(7));
        System.out.println();
        System.out.println("toString():" + dn);
        int size  = dn.size();
        System.out.println("get(0):" + dn.get(0));
        System.out.println("get(size() - 1):" + dn.get(size - 1));
        System.out.println("getRdn(0):" + dn.getRdn(0));
        System.out.println("getRdn(size() - 1):" + dn.getRdn(size - 1));
        System.out.println();
        System.out.println("********Prefixes**********");
        System.out.println("getPrefix(0):" + dn.getPrefix(0));
        System.out.println("getPrefix(size / 2):" + dn.getPrefix(size / 2));
        System.out.println("getPrefix(size):" + dn.getPrefix(size));
        System.out.println();
        System.out.println("********Suffixes**********");
        System.out.println("getSuffix(0):" + dn.getSuffix(0));
        System.out.println("getSuffix(size/2):" + dn.getSuffix(size / 2));
        System.out.println("getSuffix(size):" + dn.getSuffix(size));
        System.out.println();
        System.out.println("startsWith(" + rdnStr[0] + "):" +
                                dn.startsWith(new LdapName(rdnStr[0])));
        String lastEntry = "seven=red";
        System.out.println("startsWith(" + lastEntry + "):" +
                                dn.startsWith(new LdapName(lastEntry)));
        System.out.println("compositeName- startsWith(" +
                        rdnStr[0] + "): " + dn.startsWith(
                                        new CompositeName(rdnStr[0])));
        java.util.List prefixList = (dn.getRdns()).subList(0, size /2);
        System.out.println("Rdn - startsWith(" + prefixList + "):" +
                                dn.startsWith(prefixList));
        System.out.println("Rdn - startsWith() - empty RDN list:" +
                                dn.startsWith(new ArrayList()));
        System.out.println();
        System.out.println("endsWith(" + rdnStr[0] + "):" +
                                dn.endsWith(new LdapName(rdnStr[0])));
        System.out.println("endsWith(" + lastEntry + "):" +
                                dn.endsWith(new LdapName(lastEntry)));
        System.out.println("compositeName- endsWith(" + rdnStr[0] + "):    " +
                dn.endsWith(new CompositeName(rdnStr[0])));
        System.out.println("Rdn - endsWith(" + prefixList + "):" +
                                dn.endsWith(prefixList));
        System.out.println("Rdn - endsWith() empty RDN list:" +
                                dn.endsWith(new ArrayList()));
        System.out.println();
        System.out.println("cloned name:" + dn.clone());
        ObjectOutputStream out = new ObjectOutputStream(
                                    new FileOutputStream("dn.ser"));
        out.writeObject(dn);
        out.close();
        ObjectInputStream in = new ObjectInputStream(
                                    new FileInputStream("dn.ser"));
        System.out.println();
        System.out.println("Deserialized name:" + in.readObject());
        in.close();
    }
}
