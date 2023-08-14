public class Setup {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Setup <work-dir> <premain-class>");
            return;
        }
        String workDir = args[0];
        String premainClass = args[1];
        String manifestFile = workDir + fileSeparator + "MANIFEST.MF";
        String bootClassPath = "boot" + suffix();
        String bootDir = workDir + fileSeparator + bootClassPath;
        File f = new File(bootDir);
        f.mkdir();
        try (FileOutputStream out = new FileOutputStream(manifestFile)) {
            out.write("Manifest-Version: 1.0\n".getBytes("UTF-8"));
            byte[] premainBytes =
                ("Premain-Class: " + premainClass + "\n").getBytes("UTF-8");
            out.write(premainBytes);
            out.write( "Boot-Class-Path: ".getBytes("UTF-8") );
            byte[] value = bootClassPath.getBytes("UTF-8");
            for (int i=0; i<value.length; i++) {
                int v = (int)value[i];
                if (v < 0) v += 256;
                byte[] escaped =
                    ("%" + Integer.toHexString(v)).getBytes("UTF-8");
                out.write(escaped);
            }
            out.write( "\n\n".getBytes("UTF-8") );
        }
        f = new File(workDir + fileSeparator + "boot.dir");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(bootDir.getBytes(defaultEncoding));
        }
    }
    private static final String fileSeparator = System.getProperty("file.separator");
    private static final String osName = System.getProperty("os.name");
    private static final String defaultEncoding = Charset.defaultCharset().name();
    private static final String arabic = "\u0627\u0644\u0639\u0631\u0628\u064a\u0629";
    private static final String s_chinese = "\u4e2d\u6587";
    private static final String t_chinese = "\u4e2d\u6587";
    private static final String russian = "\u0440\u0443\u0441\u0441\u043A\u0438\u0439";
    private static final String hindi = "\u0939\u093f\u0902\u0926\u0940";
    private static final String greek = "\u03b5\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac";
    private static final String hebrew = "\u05e2\u05d1\u05e8\u05d9\u05ea";
    private static final String japanese = "\u65e5\u672c\u8a9e";
    private static final String korean = "\ud55c\uad6d\uc5b4";
    private static final String lithuanian = "Lietuvi\u0173";
    private static final String czech = "\u010de\u0161tina";
    private static final String turkish = "T\u00fcrk\u00e7e";
    private static final String spanish = "espa\u00f1ol";
    private static final String thai = "\u0e44\u0e17\u0e22";
    private static final String unicode = arabic + s_chinese + t_chinese
            + russian + hindi + greek + hebrew + japanese + korean
            + lithuanian + czech + turkish + spanish + thai;
    private static String suffix() {
        String[][] names = {
            { "UTF-8",          unicode,        ""              },
            { "windows-1256",   null,           ""              },
            { "iso-8859-6",     arabic,         null            },
            { "GBK",            s_chinese,      s_chinese       },
            { "GB18030",        s_chinese,      s_chinese       },
            { "GB2312",         s_chinese,      null            },
            { "x-windows-950",  null,           t_chinese       },
            { "x-MS950-HKSCS",  null,           t_chinese       },
            { "x-euc-tw",       t_chinese,      null            },
            { "Big5",           t_chinese,      null            },
            { "Big5-HKSCS",     t_chinese,      null            },
            { "windows-1251",   null,           ""              },
            { "iso-8859-5",     russian,        null            },
            { "koi8-r",         russian,        null            },
            { "windows-1253",   null,           ""              },
            { "iso-8859-7",     greek,          null            },
            { "windows-1255",   null,           ""              },
            { "iso8859-8",      hebrew,         null            },
            { "windows-31j",    null,           japanese        },
            { "x-eucJP-Open",   japanese,       null            },
            { "x-EUC-JP-LINUX", japanese,       null            },
            { "x-pck",          japanese,       null            },
            { "x-windows-949",  null,           korean          },
            { "euc-kr",         korean,         null            },
            { "windows-1257",   null,           ""              },
            { "iso-8859-13",    lithuanian,     null            },
            { "windows-1250",   null,           ""              },
            { "iso-8859-2",     czech,          null            },
            { "windows-1254",   null,           ""              },
            { "iso-8859-9",     turkish,        null            },
            { "windows-1252",   null,           ""              },
            { "iso-8859-1",     spanish,        null            },
            { "iso-8859-15",    spanish,        null            },
            { "x-windows-874",  null,           thai            },
            { "tis-620",        thai,           null            },
        };
        int column;
        if (osName.startsWith("Windows")) {
            column = 2;
        } else {
            column = 1;
        }
        for (int i = 0; i < names.length; i++) {
             if (names[i][0].equalsIgnoreCase(defaultEncoding)) {
                 return names[i][column];
             }
         }
         return "";
    }
}
