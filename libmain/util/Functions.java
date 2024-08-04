package libmain.util;

import java.io.File;
import java.lang.Character;
import java.util.Stack;

public class Functions {
    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock of = Character.UnicodeBlock.of(c);
        return (Character.isISOControl(c) || of == null || of == Character.UnicodeBlock.SPECIALS) ? false : true;
    }

    public static String getLocator(String str) {
        if (str.indexOf("http://") == 0) {
            str = str.substring(7);
        } else if (str.indexOf("https://") == 0) {
            str = str.substring(8);
        }
        return str.indexOf("www.") == 0 ? str.substring(4) : str;
    }

    public static String getPathQualifier(String str) {
        String replace = str.replace("\\", "_").replace("/", "_").replace(":", "_").replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");
        if (replace.lastIndexOf(46) < 0) {
            return replace.length() > Parameters.PATH_LENTGTH ? replace.substring(0, Parameters.PATH_LENTGTH + 1) : replace;
        }
        String substring = replace.substring(0, replace.lastIndexOf(46));
        String substring2 = replace.substring(replace.lastIndexOf(46) + 1);
        if (substring.length() > Parameters.PATH_LENTGTH) {
            substring.substring(0, Parameters.PATH_LENTGTH + 1);
        }
        if (substring2.length() <= Parameters.PATH_LENTGTH) {
            return replace;
        }
        substring2.substring(0, Parameters.PATH_LENTGTH + 1);
        return replace;
    }

    public static String getPath(String str) {
        String str2 = "";
        if (str.indexOf("http://") >= 0) {
            str = str.substring(str.indexOf("http://") + 7);
            if (str.indexOf(47) >= 0) {
                str = str.substring(str.indexOf(47) + 1);
            }
        } else if (str.indexOf("https://") >= 0) {
            str = str.substring(str.indexOf("http://") + 8);
            if (str.indexOf(47) >= 0) {
                str = str.substring(str.indexOf(47) + 1);
            }
        }
        Stack stack = new Stack();
        stack.push("");
        String[] split = str.split("/|\\\\");
        if (split != null) {
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }
            for (int i2 = 0; i2 < split.length; i2++) {
                if (split[i2].equals("..")) {
                    if (!stack.empty()) {
                        stack.pop();
                    }
                } else if (!split[i2].equals("/") && !split[i2].equals(".") && !split[i2].equals("")) {
                    stack.push(getPathQualifier(split[i2]));
                }
            }
        }
        while (!stack.empty()) {
            String str3 = (String) stack.pop();
            if (str3.length() > 0 && str2.length() > 0) {
                str2 = "/" + str2;
            }
            str2 = str3 + str2;
        }
        return str2;
    }

    public static boolean recreatePath(String str) {
        String[] split = str.split("/|\\\\");
        if (split == null || split.length <= 0) {
            return true;
        }
        String str2 = "";
        int i = 0;
        while (i < split.length) {
            try {
                if (split[i].length() != 0) {
                    if (i != 0 || split[i].indexOf(":") < 0) {
                        str2 = str2 + "/";
                    }
                    str2 = str2 + split[i];
                    File file = new File(str2);
                    if (!file.exists()) {
                        if (!file.mkdir()) {
                            return false;
                        }
                    } else if (!file.isDirectory()) {
                        return false;
                    }
                }
                i++;
            } catch (Exception e) {
                e.getSuppressed();
                return false;
            }
        }
        return true;
    }

    public static boolean recreateFilePath(String str) {
        int lastIndexOf = str.lastIndexOf(47);
        if (lastIndexOf < 0) {
            lastIndexOf = str.lastIndexOf(92);
        }
        if (lastIndexOf >= 0) {
            return recreatePath(str.substring(0, lastIndexOf));
        }
        return true;
    }

    public static String getNewFileName(String str, String str2) {
        String str3;
        int i = 0;
        while (true) {
            if (i > 0) {
                try {
                    if (str2.lastIndexOf(46) >= 0) {
                        str3 = str2.substring(0, str2.lastIndexOf(46)) + "[" + i + "]." + str2.substring(str2.lastIndexOf(46) + 1);
                    } else {
                        str3 = str2 + "[" + i + "]";
                    }
                } catch (Exception unused) {
                    return str2;
                }
            } else {
                str3 = str2;
            }
            if (!new File(str + "/" + str3).exists()) {
                return str3;
            }
            i++;
        }
    }

    public static String getCommonRelativePath(String str, String str2) {
        String str3 = "";
        String[] split = str.split("/|\\\\");
        String[] split2 = str2.split("/|\\\\");
        int i = 0;
        int i2 = 0;
        while (i < split.length && i2 < split2.length && split[i].equals(split2[i2])) {
            i++;
            i2++;
        }
        while (true) {
            i++;
            if (i >= split.length) {
                break;
            }
            if (str3.length() > 0) {
                str3 = str3 + "/";
            }
            str3 = str3 + "..";
        }
        while (i2 < split2.length) {
            if (str3.length() > 0) {
                str3 = str3 + "/";
            }
            str3 = str3 + split2[i2];
            i2++;
        }
        return str3;
    }

    public static String getRightURL(String str) {
        String str2 = "";
        String[] split = str.split("/");
        if (split.length == 0) {
            return str2;
        }
        if (split.length == 1) {
            return split[0];
        }
        if (split.length == 2) {
            return split[0] + "/" + split[1];
        }
        if (split.length == 3) {
            return split[0] + "/" + split[1] + "/" + split[2];
        }
        Stack stack = new Stack();
        for (int i = 3; i < split.length; i++) {
            if (split[i].equals("..")) {
                stack.pop();
            } else {
                stack.push(split[i]);
            }
        }
        while (!stack.empty()) {
            if (str2.length() <= 0 || str2.charAt(str2.length() - 1) == '/') {
                str2 = ((String) stack.pop()) + str2;
            } else {
                str2 = ((String) stack.pop()) + "/" + str2;
            }
        }
        return split[0] + "/" + split[1] + "/" + split[2] + "/" + str2;
    }
}
