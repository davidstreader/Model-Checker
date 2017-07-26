package mc;

import mc.gui.Download;
import mc.util.Utils;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.AnsiConsole;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by Sanjay on 18/07/2017.
 */
public class AutoUpdate {
    private Logger logger = LoggerFactory.getLogger(AutoUpdate.class);
    private static final String version = "v2.3";
    private static final String githubAPI = "https://api.github.com/repos/DavidSheridan/Model-Checker/releases/latest";
    private String[] getDownloadInfo() {
        try {
            JSONObject obj = new JSONObject(IOUtils.toString(new URL(githubAPI).openStream()));
            if (obj.getString("tag_name").equals(version)) {
                logger.info(""+ansi().render("@|green Model checker up to date!|@"));
                return null;
            }
            return new String[]{obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"),obj.getString("tag_name")};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean isUpdated() {
        return getDownloadInfo() == null;
    }
    public void checkForUpdates() {
        if (!Utils.isJar()) return;
        AnsiConsole.systemInstall();
        String[] downloadInfo = getDownloadInfo();
        if (downloadInfo == null) return;
        logger.info(""+ansi().render("@|red Update found! (Current version: "+version+", New version: "+downloadInfo[1]+")|@"));
        logger.info(""+ansi().render("@|yellow Starting update|@"));
        ProcessBuilder nativeLauncher = Main.getInstance().createWrappedProcess();
        String dlPath = Utils.getJarPath().replace(".jar","-tmp.jar");
        download(downloadInfo);
        ProcessBuilder builder = new ProcessBuilder("java","-Djansi.passthrough=true","-jar", dlPath,"update");
        Main.getInstance().spawnProcess(builder);
        new File(dlPath).delete();
        Main.getInstance().spawnProcess(nativeLauncher);
        Main.getInstance().stop();
    }
    public void download(String[] downloadInfo) {
        logger.info(""+ansi().render("@|yellow Downloading update|@"));
        String downloadURL = downloadInfo[0];
        try {
            Download download = new Download(new URL(downloadURL),Utils.getJarPath().replace(".jar","-tmp.jar"));
            if (Main.getInstance().getGui() != null)
                Main.getInstance().getGui().showProgressBar();
            while (download.getStatus() == Download.DOWNLOADING) {
                if (Main.getInstance().getGui() != null)
                    Main.getInstance().getGui().setProgressBarValue((int) download.getProgress());
                else
                    logger.info(""+ansi().render("@|yellow "+((int) download.getProgress())+"% Downloaded|@"));
                Thread.sleep(100);
            }
            if (Main.getInstance().getGui() != null)
                Main.getInstance().getGui().hideProgressBar();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void install() {
        logger.info(""+ansi().render("@|yellow Updating files|@"));
        try {
            FileUtils.copyFile(new File(Utils.getJarPath()),new File(Utils.getJarPath().replace("-tmp.jar",".jar")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
