package libmain.sitegrabber;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import libmain.csv.CSV;
import libmain.csv.CSVObject;
import libmain.thread.PathThreadBunch;
import libmain.util.Functions;

public class Site {
    public HashSet<String> allPaths = new HashSet<>();
    public String folderPath;
    public boolean localDownload = true;
    public Path mainPath;
    public PathThreadBunch pathThreadBunch = new PathThreadBunch(this);
    public HashSet<Path> paths = new HashSet<>();
    public Map<String, Path> pathsMap = new HashMap();
    public String prefix;
    public HashSet<Path> progressPaths = new HashSet<>();
    public String startingURL;
    public HashSet<Integer> texts = new HashSet<>();
    public String url;

    public Site(String str, String str2, String str3) {
        this.url = str;
        this.folderPath = str2;
        this.prefix = str3;
    }

    public void addText(String str) {
        this.texts.add(Integer.valueOf(str.hashCode()));
    }

    public void addPath(Path path) {
        this.paths.add(path);
        this.pathsMap.put(path.path, path);
        this.pathThreadBunch.addPath(path);
    }

    public void pathDownloaded(Path path) {
        path.downloaded = true;
        StringBuilder sb = new StringBuilder();
        sb.append("Path downloaded: ");
        sb.append(path.url != null ? path.url.toString() : "null");
        sb.append("; ");
        sb.append(path.path);

        pathRecreate(path);
        try {
            if (path.data != null) {
                Functions.recreatePath(path.filePath);
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(path.filePath + "/" + path.fileName);
                    fileOutputStream.write(path.data);
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                path.data = null;
                System.gc();

            } else if (path.text != null) {
                Functions.recreatePath(path.filePath);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path.filePath + "/" + path.fileName));
                char[] cArr = new char[path.text.length()];
                for (int i = 0; i < cArr.length; i++) {
                    cArr[i] = path.text.charAt(i);
                }
                bufferedWriter.write(cArr, 0, cArr.length);
                bufferedWriter.close();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void pathChecked(Path path) {
        if (!path.checked) {
            path.checked = true;
            path.parentPathsCount = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("Path checked: ");
            sb.append(path.url != null ? path.url.toString() : "null");

            for (PathLink next : path.sourcePaths) {
                if (!next.updated) {
                    if (next.path.parentPathsCount != 0) {
                        path.parentPathsCount++;
                    }
                    next.path.parentPaths.add(new PathLink(path));
                    if (!next.path.downloaded) {
                        this.pathThreadBunch.addPath(next.path);
                    }
                }
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Path checked: ");
            sb2.append(path.url != null ? path.url.toString() : "null");
            sb2.append("; ");
            sb2.append(path.parentPathsCount);

            if (path.parentPathsCount == 0 && path.downloaded) {
                path.pathUpdated(path);
            }
        } else if (path.parentPathsCount == 0 && path.downloaded) {
            path.pathUpdated(path);
        }
    }

    public void pathRecreate(Path path) {
        if (path.filePath == null) {
            path.filePath = path.site.folderPath + "/" + Functions.getPath(path.path);
            int lastIndexOf = path.filePath.lastIndexOf(47);
            if (lastIndexOf < 0) {
                lastIndexOf = path.filePath.lastIndexOf(92);
            }
            if (lastIndexOf >= 0) {
                path.fileName = path.filePath.substring(lastIndexOf + 1);
                path.filePath = path.filePath.substring(0, lastIndexOf);
            }
            if (path.httpContentType != null && path.httpContentType.indexOf("text/html") >= 0) {
                if (path.fileName == null || path.fileName.length() == 0) {
                    path.fileName = "index.html";
                } else if (!(path.fileName.indexOf(".htm") == path.fileName.length() - 4 || path.fileName.indexOf(".html") == path.fileName.length() - 5)) {
                    path.fileName += ".html";
                }
            }
            path.fileName = Functions.getNewFileName(path.filePath, path.fileName);
        }
    }

    public void pathReady(Path path) {
        StringBuilder sb = new StringBuilder();
        sb.append("Path ready: ");
        sb.append(path.url != null ? path.url.toString() : "null");

        if (path.sourcePaths.size() > 0) {
            pathRecreate(path);
            String str = path.text;
            if (str == null && path.fileName != null) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(path.filePath + "/" + path.fileName));
                    char[] cArr = new char[256];
                    str = "";
                    while (true) {
                        int read = bufferedReader.read(cArr);
                        if (read <= 0) {
                            break;
                        } else if (path.textEncoding != null) {
                            str = str + new String(new String(cArr, 0, read).getBytes(), path.textEncoding);
                        } else {
                            str = str + new String(cArr, 0, read);
                        }
                    }
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (str != null) {
                for (PathLink next : path.sourcePaths) {
                    if (next.mark != null && next.mark.length() > 0) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(path.filePath);
                        sb2.append("/");
                        sb2.append(path.fileName);
                        String sb3 = sb2.toString();
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(next.path.filePath);
                        sb4.append("/");
                        sb4.append(next.path.fileName);
                        String commonRelativePath = Functions.getCommonRelativePath(sb3, sb4.toString());
                        if (commonRelativePath != null && commonRelativePath.length() > 0) {
                            str = str.replace(next.mark, commonRelativePath);
                        }
                    }
                }
                if (path.fileName != null) {
                    try {
                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path.filePath + "/" + path.fileName));
                        char[] cArr2 = new char[str.length()];
                        for (int i = 0; i < cArr2.length; i++) {
                            cArr2[i] = str.charAt(i);
                        }
                        bufferedWriter.write(cArr2);
                        bufferedWriter.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        path.text = null;
        System.gc();
    }

    public void download(String str, String str2) {
        this.prefix = str;
        String str3 = str + this.url;
        if ((this.url.indexOf("/") != this.url.length() - 1 || this.url.length() == 0) && str2.indexOf("/") != 0) {
            str3 = str3 + "/";
        }
        try {
            String str4 = str3 + str2;
            Path path = new Path(this);
            path.path = str2;
            path.url = new URL(str4);
            this.startingURL = str4;
            addPath(path);
            this.mainPath = path;
            this.pathThreadBunch.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(String str) {
        this.prefix = str;
        download(str, "");
    }

    public void save(String str) {
        CSV csv = new CSV(new String[]{"path", "filepath", "filename", "counter", "downloaded", "sourcepaths"});
        Object[] objArr = new Object[6];
        objArr[0] = "" + this.url + "|" + this.prefix + "|" + this.startingURL + "|" + this.localDownload;
        objArr[1] = this.folderPath;
        objArr[2] = this.mainPath.fileName;
        objArr[3] = Integer.valueOf(this.mainPath.parentPathsCount);
        objArr[4] = Boolean.valueOf(this.mainPath.parentPathsCount == 0);
        objArr[5] = this.mainPath.path;
        new CSVObject(csv, objArr);
        Iterator<Path> it = this.paths.iterator();
        while (it.hasNext()) {
            Path next = it.next();
            String str2 = "";
            for (PathLink next2 : next.sourcePaths) {
                if (str2.length() > 0) {
                    str2 = str2 + "|";
                }
                str2 = str2 + next2.mark + ":" + next2.path.path;
            }
            new CSVObject(csv, new Object[]{next.path, next.filePath, next.fileName, Integer.valueOf(next.parentPathsCount), Boolean.valueOf(next.downloaded), str2});
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(str);
            csv.write(fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open(String str) {
        try {
            FileInputStream fileInputStream = new FileInputStream(str);
            CSV csv = new CSV((InputStream) fileInputStream);
            fileInputStream.close();
            String[] split = csv.items.get(0).items.get(0).toString().split("\\|");
            this.url = split[0];
            this.prefix = split[1];
            this.startingURL = split[2];
            this.localDownload = split[3].equals("true");
            this.folderPath = csv.items.get(0).items.get(1).toString();
            String obj = csv.items.get(0).items.get(5).toString();
            for (int i = 1; i < csv.items.size(); i++) {
                CSVObject cSVObject = csv.items.get(i);
                Path path = new Path(this);
                path.path = cSVObject.items.get(0).toString();
                path.filePath = cSVObject.items.get(1).toString();
                path.fileName = cSVObject.items.get(2).toString();
                path.parentPathsCount = Integer.parseInt(cSVObject.items.get(3).toString());
                path.downloaded = cSVObject.items.get(4).toString().equals("true");
                if (path.fileName != null && path.fileName.length() > 0) {
                    path.currentTrie = -1;
                }
                String[] split2 = cSVObject.items.get(5).toString().split("\\|");
                for (int i2 = 0; i2 < split2.length; i2++) {
                    if (split2[i2].trim().length() != 0) {
                        String[] split3 = split2[i2].split("\\:");
                        Path path2 = new Path(this);
                        path2.path = split3[1];
                        PathLink pathLink = new PathLink(path2);
                        pathLink.mark = split3[0];
                        path.paths.add(path2);
                        path.sourcePaths.add(pathLink);
                    }
                }
                this.allPaths.add(path.path);
                this.pathsMap.put(path.path, path);
                this.paths.add(path);
            }
            Iterator<Path> it = this.paths.iterator();
            while (it.hasNext()) {
                Path next = it.next();
                for (PathLink next2 : next.sourcePaths) {
                    if (this.pathsMap.containsKey(next2.path.path)) {
                        Path path3 = this.pathsMap.get(next2.path.path);
                        next2.path.filePath = path3.filePath;
                        next2.path.fileName = path3.fileName;
                        path3.parentPaths.add(new PathLink(next));
                    }
                }
            }
            this.mainPath = this.pathsMap.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
