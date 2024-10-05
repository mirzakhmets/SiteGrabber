package libmain;

import libmain.sitegrabber.Site;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {
    public static boolean isRegistered() {
        try {
            File file = new File(System.getProperty("user.home") + "/register.lic");

            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void checkRuns() {
        try {
            File file = new File(System.getProperty("user.home") + "/run.lic");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileInputStream fis = new FileInputStream(new File(System.getProperty("user.home") + "/run.lic"));

            byte[] b = fis.readAllBytes();

            if (b.length > 30) {
                System.out.println("Number of runs expired. Please purchase the program (visit site https://ovg-developers.mystrikingly.com/).");
                System.exit(0);
            }

            fis.close();

            FileOutputStream fos = new FileOutputStream(new File(System.getProperty("user.home") + "/run.lic"));

            fos.write(b);

            fos.write(' ');

            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        if (!isRegistered()) {
            checkRuns();
        }

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
