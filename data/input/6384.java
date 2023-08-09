public class AclEdit {
    static Set<AclEntryPermission> parsePermissions(String permsString) {
        Set<AclEntryPermission> perms = new HashSet<AclEntryPermission>();
        String[] result = permsString.split("/");
        for (String s : result) {
            if (s.equals(""))
                continue;
            try {
                perms.add(AclEntryPermission.valueOf(s.toUpperCase()));
            } catch (IllegalArgumentException x) {
                System.err.format("Invalid permission '%s'\n", s);
                System.exit(-1);
            }
        }
        return perms;
    }
    static Set<AclEntryFlag> parseFlags(String flagsString) {
        Set<AclEntryFlag> flags = new HashSet<AclEntryFlag>();
        String[] result = flagsString.split("/");
        for (String s : result) {
            if (s.equals(""))
                continue;
            try {
                flags.add(AclEntryFlag.valueOf(s.toUpperCase()));
            } catch (IllegalArgumentException x) {
                System.err.format("Invalid flag '%s'\n", s);
                System.exit(-1);
            }
        }
        return flags;
    }
    static AclEntryType parseType(String typeString) {
        if (typeString.equalsIgnoreCase("allow"))
            return AclEntryType.ALLOW;
        if (typeString.equalsIgnoreCase("deny"))
            return AclEntryType.DENY;
        System.err.format("Invalid type '%s'\n", typeString);
        System.exit(-1);
        return null;    
    }
    static AclEntry parseAceString(String s,
                                   UserPrincipalLookupService lookupService)
    {
        String[] result = s.split(":");
        if (result.length < 3)
            usage();
        int index = 0;
        int remaining = result.length;
        boolean isGroup = false;
        if (result[index].equalsIgnoreCase("user") ||
            result[index].equalsIgnoreCase("group"))
        {
            if (--remaining < 3)
                usage();
            isGroup = result[index++].equalsIgnoreCase("group");
        }
        String userString = result[index++]; remaining--;
        String permsString = result[index++]; remaining--;
        String flagsString = "";
        String typeString = null;
        if (remaining == 1) {
            typeString = result[index++];
        } else {
            if (remaining == 2) {
                flagsString = result[index++];
                typeString = result[index++];
            } else {
                usage();
            }
        }
        UserPrincipal user = null;
        try {
            user = (isGroup) ?
                lookupService.lookupPrincipalByGroupName(userString) :
                lookupService.lookupPrincipalByName(userString);
        } catch (UserPrincipalNotFoundException x) {
            System.err.format("Invalid %s '%s'\n",
                ((isGroup) ? "group" : "user"),
                userString);
            System.exit(-1);
        } catch (IOException x) {
            System.err.format("Lookup of '%s' failed: %s\n", userString, x);
            System.exit(-1);
        }
        Set<AclEntryPermission> perms = parsePermissions(permsString);
        Set<AclEntryFlag> flags = parseFlags(flagsString);
        AclEntryType type = parseType(typeString);
        return AclEntry.newBuilder()
            .setType(type)
            .setPrincipal(user)
            .setPermissions(perms).setFlags(flags).build();
    }
    static void usage() {
        System.err.println("usage: java AclEdit [ACL-operation] file");
        System.err.println("");
        System.err.println("Example 1: Prepends access control entry to the begining of the myfile's ACL");
        System.err.println("       java AclEdit A+alice:read_data/read_attributes:allow myfile");
        System.err.println("");
        System.err.println("Example 2: Remove the entry at index 6 of myfile's ACL");
        System.err.println("       java AclEdit A6- myfile");
        System.err.println("");
        System.err.println("Example 3: Replace the entry at index 2 of myfile's ACL");
        System.err.println("       java AclEdit A2=bob:write_data/append_data:deny myfile");
        System.exit(-1);
    }
    static enum Action {
        PRINT,
        ADD,
        REMOVE,
        REPLACE;
    }
    public static void main(String[] args) throws IOException {
        Action action = null;
        int index = -1;
        String entryString = null;
        if (args.length < 1 || args[0].equals("-help") || args[0].equals("-?"))
            usage();
        if (args.length == 1) {
            action = Action.PRINT;
        } else {
            String s = args[0];
            if (Pattern.matches("^A[0-9]*\\+.*", s)) {
                String[] result = s.split("\\+", 2);
                if (result.length == 2) {
                    if (result[0].length() < 2) {
                        index = 0;
                    } else {
                        index = Integer.parseInt(result[0].substring(1));
                    }
                    entryString = result[1];
                    action = Action.ADD;
                }
            }
            if (Pattern.matches("^A[0-9]+\\-", s)) {
                String[] result = s.split("\\-", 2);
                if (result.length == 2) {
                    index = Integer.parseInt(result[0].substring(1));
                    entryString = result[1];
                    action = Action.REMOVE;
                }
            }
            if (Pattern.matches("^A[0-9]+=.*", s)) {
                String[] result = s.split("=", 2);
                if (result.length == 2) {
                    index = Integer.parseInt(result[0].substring(1));
                    entryString = result[1];
                    action = Action.REPLACE;
                }
            }
        }
        if (action == null)
            usage();
        int fileArg = (action == Action.PRINT) ? 0 : 1;
        Path file = Paths.get(args[fileArg]);
        AclFileAttributeView view =
            Files.getFileAttributeView(file, AclFileAttributeView.class);
        if (view == null) {
            System.err.println("ACLs not supported on this platform");
            System.exit(-1);
        }
        List<AclEntry> acl = view.getAcl();
        switch (action) {
            case PRINT : {
                for (int i=0; i<acl.size(); i++) {
                    System.out.format("%5d: %s\n", i, acl.get(i));
                }
                break;
            }
            case ADD: {
                AclEntry entry = parseAceString(entryString, file
                    .getFileSystem().getUserPrincipalLookupService());
                if (index >= acl.size()) {
                    acl.add(entry);
                } else {
                    acl.add(index, entry);
                }
                view.setAcl(acl);
                break;
            }
            case REMOVE: {
                if (index >= acl.size()) {
                    System.err.format("Index '%d' is invalid", index);
                    System.exit(-1);
                }
                acl.remove(index);
                view.setAcl(acl);
                break;
            }
            case REPLACE: {
                if (index >= acl.size()) {
                    System.err.format("Index '%d' is invalid", index);
                    System.exit(-1);
                }
                AclEntry entry = parseAceString(entryString, file
                    .getFileSystem().getUserPrincipalLookupService());
                acl.set(index, entry);
                view.setAcl(acl);
                break;
            }
        }
    }
}
