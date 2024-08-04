package libmain;

import libmain.sitegrabber.Site;

public class Main {
    public static void main(String[] argv) {
        int i = 0;

        String prefix = "http://";

        if (argv.length > 0 && argv[0].equals("-ssl")) {
            ++i;
            prefix = "https://";
        }

        if (argv.length - i < 2) {
            System.out.println("sitegrabber [-ssl] <site domain> <folder>");

            System.exit(0);
        }

        Site site = new Site(argv[i], argv[i + 1], prefix);

        site.download(prefix);

        do {
        } while (site.pathThreadBunch.run() > 0);
    }
}
