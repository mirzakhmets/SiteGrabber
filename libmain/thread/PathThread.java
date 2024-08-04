package libmain.thread;

import java.util.ArrayList;
import java.util.List;
import libmain.sitegrabber.Path;
import libmain.util.Functions;

public class PathThread extends Thread {
    public PathThreadBunch bunch;
    public List<Path> paths = new ArrayList();

    public PathThread(PathThreadBunch pathThreadBunch) {
        this.bunch = pathThreadBunch;
    }

    public PathThread(PathThread pathThread) {
        synchronized (this.paths) {
            this.paths = pathThread.paths;
        }
        this.bunch = pathThread.bunch;
    }

    public void addPath(Path path) {
        if (path != null) {
            synchronized (this.paths) {
                this.paths.add(path);
            }
            run();
        }
    }

    public void run() {
        int i;
        Path[] pathArr = new Path[this.paths.size()];
        synchronized (this.paths) {
            for (int i2 = 0; i2 < pathArr.length; i2++) {
                pathArr[i2] = this.paths.get(i2);
            }
        }
        for (Path path : pathArr) {
            if (!this.bunch.site.progressPaths.contains(path)) {
                this.bunch.site.progressPaths.add(path);
                if (!path.processed) {
                    path.processed = true;
                    if (((!this.bunch.site.localDownload || (!(Functions.getLocator(path.path.toLowerCase()).indexOf(Functions.getLocator(this.bunch.site.url.toLowerCase())) == 0 && (path.path.indexOf("http://") == 0 || path.path.indexOf("https://") == 0)) && (path.path.indexOf("http://") == 0 || path.path.indexOf("https://") == 0))) && this.bunch.site.localDownload) || !path.download()) {
                        this.bunch.site.pathDownloaded(path);
                        this.bunch.site.pathChecked(path);
                    } else {
                        this.bunch.site.pathDownloaded(path);
                        try {
                            if (path.httpContentType != null && path.httpContentType.indexOf("text/") >= 0 && path.httpContentType != null && path.httpContentType.indexOf("text/html") >= 0 && path.text != null && !path.site.texts.contains(Integer.valueOf(path.text.hashCode()))) {
                                path.site.addText(path.text);
                                path.check();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.bunch.site.pathChecked(path);
                    }
                }
            } else {
                path.processed = true;
            }
        }
    }
}
