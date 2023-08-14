class SubjectComber {
    private static final boolean DEBUG = Krb5Util.DEBUG;
    private SubjectComber() {  
    }
    static <T> T find(Subject subject, String serverPrincipal,
        String clientPrincipal, Class<T> credClass) {
        return (T)findAux(subject, serverPrincipal, clientPrincipal, credClass,
            true);
    }
    static <T> List<T> findMany(Subject subject, String serverPrincipal,
        String clientPrincipal, Class<T> credClass) {
        return (List<T>)findAux(subject, serverPrincipal, clientPrincipal, credClass,
            false);
    }
    private static <T> Object findAux(Subject subject, String serverPrincipal,
        String clientPrincipal, Class<T> credClass, boolean oneOnly) {
        if (subject == null) {
            return null;
        } else {
            List<T> answer = (oneOnly ? null : new ArrayList<T>());
            if (credClass == KeyTab.class) {    
                Iterator<T> iterator =
                    subject.getPrivateCredentials(credClass).iterator();
                while (iterator.hasNext()) {
                    T t = iterator.next();
                    if (DEBUG) {
                        System.out.println("Found " + credClass.getSimpleName());
                    }
                    if (oneOnly) {
                        return t;
                    } else {
                        answer.add(t);
                    }
                }
            } else if (credClass == KerberosKey.class) {
                Iterator<T> iterator =
                    subject.getPrivateCredentials(credClass).iterator();
                while (iterator.hasNext()) {
                    T t = iterator.next();
                    String name = ((KerberosKey)t).getPrincipal().getName();
                    if (serverPrincipal == null || serverPrincipal.equals(name)) {
                         if (DEBUG) {
                             System.out.println("Found " +
                                     credClass.getSimpleName() + " for " + name);
                         }
                         if (oneOnly) {
                             return t;
                         } else {
                             if (serverPrincipal == null) {
                                 serverPrincipal = name;
                             }
                             answer.add(t);
                         }
                    }
                }
            } else if (credClass == KerberosTicket.class) {
                Set<Object> pcs = subject.getPrivateCredentials();
                synchronized (pcs) {
                    Iterator<Object> iterator = pcs.iterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof KerberosTicket) {
                            KerberosTicket ticket = (KerberosTicket)obj;
                            if (DEBUG) {
                                System.out.println("Found ticket for "
                                                    + ticket.getClient()
                                                    + " to go to "
                                                    + ticket.getServer()
                                                    + " expiring on "
                                                    + ticket.getEndTime());
                            }
                            if (!ticket.isCurrent()) {
                                if (!subject.isReadOnly()) {
                                    iterator.remove();
                                    try {
                                        ticket.destroy();
                                        if (DEBUG) {
                                            System.out.println("Removed and destroyed "
                                                        + "the expired Ticket \n"
                                                        + ticket);
                                        }
                                    } catch (DestroyFailedException dfe) {
                                        if (DEBUG) {
                                            System.out.println("Expired ticket not" +
                                                    " detroyed successfully. " + dfe);
                                        }
                                    }
                                }
                            } else {
                                if (serverPrincipal == null ||
                                    ticket.getServer().getName().equals(serverPrincipal))  {
                                    if (clientPrincipal == null ||
                                        clientPrincipal.equals(
                                            ticket.getClient().getName())) {
                                        if (oneOnly) {
                                            return ticket;
                                        } else {
                                            if (clientPrincipal == null) {
                                                clientPrincipal =
                                                ticket.getClient().getName();
                                            }
                                            if (serverPrincipal == null) {
                                                serverPrincipal =
                                                ticket.getServer().getName();
                                            }
                                            answer.add((T)ticket);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return answer;
        }
    }
}
