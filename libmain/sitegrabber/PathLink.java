package libmain.sitegrabber;

public class PathLink {
    public String mark;
    public Path path;
    public boolean updated = false;

    public PathLink(Path path2) {
        this.path = path2;
        this.updated = false;
    }
}
