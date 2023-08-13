public class RequestProperties {
    public static void main (String args[]) throws Exception {
        URL url0 = new URL ("http:
        URL url1 = new URL ("file:/etc/passwd");
        URL url2 = new URL ("ftp:
        URL url3 = new URL ("jar:http:
        URLConnection urlc0 = url0.openConnection ();
        URLConnection urlc1 = url1.openConnection ();
        URLConnection urlc2 = url2.openConnection ();
        URLConnection urlc3 = url3.openConnection ();
        int count = 0;
        String s = null;
        try {
            urlc0.setRequestProperty (null, null);
            System.out.println ("http: setRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc0.addRequestProperty (null, null);
            System.out.println ("http: addRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc1.setRequestProperty (null, null);
            System.out.println ("file: setRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc1.addRequestProperty (null, null);
            System.out.println ("file: addRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc2.setRequestProperty (null, null);
            System.out.println ("ftp: setRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc2.addRequestProperty (null, null);
            System.out.println ("ftp: addRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc3.setRequestProperty (null, null);
            System.out.println ("jar: setRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        try {
            urlc3.addRequestProperty (null, null);
            System.out.println ("jar: addRequestProperty (null,) did not throw NPE");
        } catch (NullPointerException e) {
            count ++;
        }
        if (urlc0.getRequestProperty (null) != null) {
            System.out.println ("http: getRequestProperty (null,) did not return null");
        } else {
            count ++;
        }
        if (urlc1.getRequestProperty (null) != null) {
            System.out.println ("file: getRequestProperty (null,) did not return null");
        } else {
            count ++;
        }
        if (urlc2.getRequestProperty (null) != null) {
            System.out.println ("ftp: getRequestProperty (null,) did not return null");
        } else {
            count ++;
        }
        if (urlc2.getRequestProperty (null) != null) {
            System.out.println ("jar: getRequestProperty (null,) did not return null");
        } else {
            count ++;
        }
        if (count != 12) {
            throw new RuntimeException ((12 -count) + " errors") ;
        }
    }
}
