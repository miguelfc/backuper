package backuper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class Application {
  final static Logger logger = LoggerFactory.getLogger(Application.class);
  private static final Object DESTINATION = "destination";
  private static final Object FILES = "files";

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {


    if (args.length <= 1) {
      logger.error("Parameters missing.");
      logger.error("Syntax: java -jar backuper.jar <includes filename>");
      System.exit(1);
    }

    String includesFilename = args[0];

    logger.info("Starting backup...");

    Set<Object> includesList = new LinkedHashSet<>();

    Yaml yaml = new Yaml();

    System.out.println(yaml.dump(yaml.load(new FileInputStream(new File(includesFilename)))));
    Map<String, Map<String, Object>> groups = (Map<String, Map<String, Object>>) yaml
        .load(new FileInputStream(new File(includesFilename)));

    Boolean isCheckOk = Boolean.TRUE;
    // Check directories
    for (String basePath : groups.keySet()) {
      logger.info("Checking destination directory for base path <" + basePath + ">.");
      String destinationDir = (String) groups.get(basePath).get(DESTINATION);
      logger.info(" - Destination dir: {}", destinationDir);
      File dir = new File(destinationDir);
      if (dir.isDirectory()) {
        if (dir.list().length > 0) {
          isCheckOk = Boolean.FALSE;
          logger.error(
              "   Destination dir is not empty. It needs to be empty in order to continue with the backup.");
        }
      }

      if (isCheckOk) {
        List<String> fileExpressionsList = (List<String>) groups.get(basePath).get(FILES);
        String[] files = getFiles(fileExpressionsList, basePath);

        logger.info(" - Actions to be performed:");
        for (String file : files) {
          String originFile = basePath + "/" + file;
          String destinationFile = destinationDir + "/" + file;
          logger.info("   copy <{}> to <{}>.", originFile, destinationFile);
        }
      }
    }

    if (isCheckOk) {
      logger.info("Checks passed.");
      promptEnterKey();
      logger.info("");
      logger.info("Starting copy operation.");
      for (String basePath : groups.keySet()) {
        logger.info("Processing base path <" + basePath + ">.");

        String destinationDir = (String) groups.get(basePath).get(DESTINATION);

        logger.info(" - Destination dir: {}", destinationDir);

        List<String> fileExpressionsList = (List<String>) groups.get(basePath).get(FILES);
        String[] files = getFiles(fileExpressionsList, basePath);

        for (String file : files) {
          File originFile = new File(basePath + "/" + file);
          File destinationFile = new File(destinationDir + "/" + file);
          String destinationPath = FilenameUtils.getPrefix(destinationFile.getPath()) + FilenameUtils.getPath(destinationFile.getPath());
          
          Files.createDirectories(Paths.get(destinationPath));
          logger.info(" - Copying: <{}> to <{}>.", originFile, destinationFile);
          Files.copy(originFile.toPath(), destinationFile.toPath());
        }
      }
    }

    logger.info("Backup finished.");
  }

  private static String[] getFiles(List<String> fileExpressionsList, String basePath) {
    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setIncludes(fileExpressionsList.toArray(new String[fileExpressionsList.size()]));
    scanner.setBasedir(basePath);
    scanner.setCaseSensitive(false);
    scanner.scan();
    return scanner.getIncludedFiles();
  }

  public static void promptEnterKey(){
    System.out.println("Press \"ENTER\" to continue...");
    Scanner scanner = new Scanner(System.in);
    scanner.nextLine();
 }
}
