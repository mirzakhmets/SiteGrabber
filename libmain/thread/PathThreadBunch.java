package libmain.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import libmain.sitegrabber.Path;
import libmain.sitegrabber.Site;

public class PathThreadBunch {
    public static int ACTION_TIMEOUT = 80000;
    public static int THREAD_COUNT = 20;
    private int currentThreadIndex;
    public Site site;
    public List<PathThread> threads = new ArrayList();

    public PathThreadBunch(Site site2) {
        this.currentThreadIndex = 0;
        this.site = site2;
        for (int i = 0; i < THREAD_COUNT; i++) {
            this.threads.add(new PathThread(this));
        }
    }

    public PathThreadBunch(Site site2, int i) {
        this.currentThreadIndex = 0;
        this.site = site2;
        for (int i2 = 0; i2 < i; i2++) {
            this.threads.add(new PathThread(this));
        }
    }

    public void addPath(Path path) {
        if (this.threads.size() > 0 && path != null) {
            this.threads.get(this.currentThreadIndex).addPath(path);
            this.currentThreadIndex = (this.currentThreadIndex + 1) % this.threads.size();
        }
    }

    public int getActiveCount() {
        int i = 0;
        for (PathThread isAlive : this.threads) {
            if (isAlive.isAlive()) {
                i++;
            }
        }
        return i;
    }

    public int run() {
        ArrayList<PathThread> arrayList = new ArrayList<>();
        int i = 0;
        for (PathThread next : this.threads) {
            int i2 = i;
            boolean z = true;
            for (Path next2 : next.paths) {
                if (next2.lastActionTime != null && new Date().getTime() - next2.lastActionTime.getTime() > ((long) ACTION_TIMEOUT)) {
                    next2.processed = true;
                    arrayList.add(next);
                }
                if (!next2.processed && !next2.downloaded) {
                    i2++;
                    z = false;
                }
            }
            if (!z && !next.isAlive()) {
                arrayList.add(next);
            }
            i = i2;
        }
        for (PathThread pathThread : arrayList) {
            pathThread.stop();
            PathThread pathThread2 = new PathThread(pathThread);
            this.threads.remove(pathThread);
            this.threads.add(pathThread2);
            pathThread2.start();
        }
        return i;
    }
}
