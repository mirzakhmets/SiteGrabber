package libmain.sitegrabber;

import libmain.util.Functions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Path {
    public static int CONNECT_TIMEOUT = 34000;
    public static int DOWNLOAD_TRIES = 4;
    public static int READ_BUFFER = 1024;
    public static int READ_TIMEOUT = 34000;
    public boolean checked = false;
    public int currentTrie = 0;
    public byte[] data = null;
    public boolean downloaded = false;
    public String fileName;
    public String filePath;
    public int httpCode;
    public String httpContentType;
    public Map<String, List<String>> httpFields;
    public String httpMessage;
    public Date lastActionTime;
    public List<PathLink> parentPaths = new ArrayList();
    public int parentPathsCount = 0;
    public String path;
    public HashSet<Path> paths = new HashSet<>();
    public boolean processed = false;
    public Site site;
    public List<PathLink> sourcePaths = new ArrayList();
    public String text;
    public String textEncoding;
    public URL url;

    public Path(Site site2) {
        this.site = site2;
    }

    public void addPath(Path path2, String str) {
        if (!this.paths.contains(path2)) {
            this.paths.add(path2);
            PathLink pathLink = new PathLink(path2);
            pathLink.mark = str;
            this.sourcePaths.add(pathLink);
        }
        if (!this.site.allPaths.contains(path2.path)) {
            this.site.allPaths.add(path2.path);
            this.site.addPath(path2);
        }
    }

    public boolean downloadData(InputStream inputStream) {
        this.downloaded = true;
        this.lastActionTime = new Date();
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bArr = new byte[READ_BUFFER];
            byte[] bArr2 = new byte[12];
            int i = 0;
            while (true) {
                int read = bufferedInputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                while (i + read >= bArr2.length) {
                    byte[] bArr3 = new byte[bArr2.length];
                    for (int i2 = 0; i2 < bArr3.length; i2++) {
                        bArr3[i2] = bArr2[i2];
                    }
                    bArr2 = new byte[((bArr2.length + 1) * 2)];
                    for (int i3 = 0; i3 < bArr3.length; i3++) {
                        bArr2[i3] = bArr3[i3];
                    }
                }
                int i4 = i;
                int i5 = 0;
                while (i5 < read) {
                    bArr2[i4] = bArr[i5];
                    i5++;
                    i4++;
                }
                i = i4;
            }
            bufferedInputStream.close();
            if (i > 0) {
                this.data = new byte[i];
                for (int i6 = 0; i6 < i; i6++) {
                    this.data[i6] = bArr2[i6];
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean download(InputStream inputStream, String str) {
        BufferedReader bufferedReader;
        this.downloaded = true;
        this.lastActionTime = new Date();
        this.textEncoding = str;
        if (str != null) {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, str));
            } catch (Exception e) {
                e.printStackTrace();
                bufferedReader = null;
            }
        } else {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        this.text = "";
        try {
            char[] cArr = new char[READ_BUFFER];
            while (true) {
                int read = bufferedReader.read(cArr);
                if (read <= 0) {
                    break;
                }
                this.lastActionTime = new Date();
                this.text += new String(cArr, 0, read);
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return true;
        } catch (Exception e3) {
            e3.printStackTrace();
            return false;
        }
    }

    public boolean download() {
        this.downloaded = true;
        this.lastActionTime = new Date();

        try {
            URL url = this.url;
            URLConnection urlConnection = url.openConnection();

            if (urlConnection != null) {
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.setReadTimeout(READ_TIMEOUT);

                urlConnection.setRequestProperty("Accept", "*");

                HttpURLConnection httpURLConnection = null;

                if (urlConnection instanceof HttpURLConnection) {
                    httpURLConnection = (HttpURLConnection) urlConnection;
                    httpURLConnection.setRequestMethod("GET");

                    this.httpCode = httpURLConnection.getResponseCode();

                    this.httpMessage = httpURLConnection.getResponseMessage();
                }


                Map m = urlConnection.getHeaderFields();
                this.httpFields = m;

                String contentType = urlConnection.getHeaderField("Content-Type");

                this.httpContentType = contentType;
                String contentEncoding = urlConnection.getHeaderField("Content-Encoding");

                if (contentEncoding == null || contentEncoding.indexOf("utf-8") >= 0) {
                    contentEncoding = "UTF-8";
                }

                if (urlConnection instanceof HttpURLConnection) {
                    httpURLConnection = (HttpURLConnection) urlConnection;
                }

                String location = urlConnection.getHeaderField("Location");
                String cookie = urlConnection.getHeaderField("Set-Cookie");

                if (location != null || this.httpCode == 301 || this.httpCode == 302 || this.httpCode == 303) {
                    url = new URL(location);

                    urlConnection = url.openConnection();

                    if (urlConnection instanceof HttpURLConnection) {
                        httpURLConnection = (HttpURLConnection) urlConnection;
                    }

                    httpURLConnection.setRequestProperty("Cookie", cookie);
                }

                if (httpURLConnection != null) {
                    this.httpCode = httpURLConnection.getResponseCode();

                    this.httpMessage = httpURLConnection.getResponseMessage();
                }

                InputStream is = url.openStream();

                this.lastActionTime = new Date();

                String[] extensions = new String[] {".png", ".gif", ".jpg", ".jpeg", ".avi", ".mov", ".mkv", ".mpg", ".mpeg", ".mp4", ".divx", ".bmp", ".tif", ".tiff", ".au", ".mp3"};

                boolean ok = false;

                for (String s: extensions) {
                    if (this.path.indexOf(s) >= 0) {
                        ok = true;
                        break;
                    }
                }

                contentType = this.httpContentType;

                if (contentType.indexOf("text/") >= 0) {
                    this.download(is, contentEncoding);
                } else {
                    this.downloadData(is);
                }

                ++this.currentTrie;

                this.downloaded = true;
                this.processed = true;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return this.path.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && getClass() == obj.getClass() && this.path.equals(((Path) obj).path);
    }

    public void pathUpdated(Path path2) {
        int i = this.parentPathsCount;

        if (i != 0) {
            this.parentPathsCount = i - 1;
        }

        try {
            if (this.parentPathsCount < 0) {
                throw new Exception("<0");
            } else if (this.parentPathsCount == 0) {
                this.site.pathReady(this);
                for (PathLink next : this.parentPaths) {
                    if (!next.updated) {
                        next.updated = true;
                        next.path.pathUpdated(this);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean ready() {
        return this.parentPathsCount == 0;
    }

    public void check() {
        try {
            if (this.text != null) {
                check(new ByteArrayInputStream(this.text.getBytes()));
                if (this.fileName != null) {
                    this.text = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPath(String s) {
        if (s.indexOf(' ') >= 0) return false;

        if (s.indexOf('\t') >= 0) return false;

        if (s.indexOf('\n') >= 0) return false;

        if (s.indexOf('\r') >= 0) return false;

        if (s.indexOf('"') >= 0) return false;

        if (s.indexOf('\'') >= 0) return false;

        return true;
    }

    public void check(java.io.InputStream is) {
        String current = "";
        char delimiter = '\0';
        boolean doCurrent = false;

        int i = -1;

        do {
            try {
                i = is.read();
            } catch (Exception e) {
            }

            if (i >= 0) {
                if (doCurrent) {
                    if ((char) i == delimiter) {
                        doCurrent = false;
                    }
                } else {
                    if ("\"'".indexOf((char) i) >= 0) {
                        delimiter = (char) i;
                        doCurrent = true;
                    }
                }

                if (doCurrent && i != delimiter) {
                    current += (char) i;
                }

                if (!doCurrent && current.length() > 0) {
                    if (isPath(current)) {

                        Path path = new Path(this.site);

                        if (current.indexOf("http") == 0) {
                            path.path = current;
                        } else {
                            path.path = this.site.prefix + this.site.url + "/" + current;
                        }

                        try {
                            path.url = new URL(path.path);

                            if (!this.site.pathsMap.containsKey(path.path)) {
                                this.addPath(path, this.site.prefix);
                            }
                        } catch (Exception e) {
                        }
                    }

                    current = "";
                }
            }
        } while (i != -1);
    }
}
