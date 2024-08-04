package libmain.csv;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CSV {
    public static final String SEPARATOR = ";";
    public List<String> header = new ArrayList();
    public List<CSVObject> items = new ArrayList();

    public CSV(InputStream inputStream) {
        read(inputStream);
    }

    public CSV(String[] strArr) {
        if (strArr != null) {
            for (String add : strArr) {
                this.header.add(add);
            }
        }
    }

    public CSV(List<String> list) {
        this.header = list;
    }

    public void write(OutputStream outputStream) {
        try {
            if (this.header != null) {
                for (String string : this.header) {
                    outputStream.write(("\"" + CSVObject.string(string) + "\"" + SEPARATOR + "").getBytes());
                }
                outputStream.write("\r\n".getBytes());
            }
            if (this.items != null) {
                for (CSVObject write : this.items) {
                    write.write(outputStream);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readLine(InputStream inputStream) {
        String str = "";
        while (true) {
            try {
                int read = inputStream.read();
                if (read == 10) {
                    break;
                } else if (read == -1) {
                    if (str.length() == 0) {
                        return null;
                    }
                } else if (read != 13) {
                    str = str + ((char) read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str.trim();
    }

    public void read(InputStream inputStream) {
        String[] split = readLine(inputStream).split(SEPARATOR);
        for (String fromString : split) {
            this.header.add(CSVObject.fromString(fromString));
        }
        while (true) {
            String readLine = readLine(inputStream);
            if (readLine != null) {
                String[] split2 = readLine.split(SEPARATOR);
                for (int i = 0; i < split2.length; i++) {
                    if (split2[i].charAt(0) == '\"') {
                        split2[i] = split2[i].trim();
                        split2[i] = CSVObject.fromString(split2[i]);
                        if (split2[i].length() > 2) {
                            split2[i] = split2[i].substring(1, split2[i].length() - 1);
                        } else {
                            split2[i] = "";
                        }
                    }
                }
                if (split2.length > 0) {
                    new CSVObject(this, (Object[]) split2);
                }
            } else {
                return;
            }
        }
    }

    public void add(CSVObject cSVObject) {
        this.items.add(cSVObject);
    }
}
