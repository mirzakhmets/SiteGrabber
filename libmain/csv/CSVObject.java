package libmain.csv;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSVObject {
    public CSV csv = null;
    public List<Object> items = new ArrayList();

    public CSVObject(CSV csv2, Object[] objArr) {
        this.csv = csv2;
        if (objArr != null) {
            for (Object add : objArr) {
                this.items.add(add);
            }
        }
        this.csv.items.add(this);
    }

    public CSVObject(CSV csv2, List<Object> list) {
        this.csv = csv2;
        this.items = list;
        this.csv.items.add(this);
    }

    public static String string(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("\"", "\"\"").replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
    }

    public static String fromString(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("\"\"", "\"").replace("\\r", "\r").replace("\\n", "\n").replace("\\t", "\t");
    }

    public void write(OutputStream outputStream) {
        try {
            for (Object next : this.items) {
                if (next instanceof Integer) {
                    outputStream.write((((Integer) next).intValue() + "").getBytes());
                } else if (next instanceof Long) {
                    outputStream.write((((Long) next).longValue() + "").getBytes());
                } else if (next instanceof Float) {
                    outputStream.write((((Float) next).floatValue() + "").getBytes());
                } else if (next instanceof Double) {
                    outputStream.write((((Double) next).doubleValue() + "").getBytes());
                } else if (next instanceof Date) {
                    outputStream.write(("\"" + ((Date) next).toString() + "\"").getBytes());
                } else if (next != null) {
                    outputStream.write(("\"" + string(next.toString()) + "\"").getBytes());
                }
                outputStream.write(CSV.SEPARATOR.getBytes());
            }
            outputStream.write("\r\n".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
