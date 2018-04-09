package buglocalization;

import buglocalization.acdc.ACDC;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadTest
{

    public static void main(String[] args)
    {
        // *** CLUSTERING OF COMPLETE SYSTEM ***//
        // SolutionClustering();

        // *** CLUSTERING OF FILE ONLY SYSTEM ***//
        // FileOnlySystemClustering();

        System.exit(0);
    }

    public static void executeClustering()
    {
        // SolutionClustering();
        // FileOnlySystemClustering();
        System.exit(0);
    }

    public static void SolutionClustering(String SolutionRsf)
    {
        String outputFolder = Paths.SOLUTION_CLUSTERS;
        File outputDir = new File(outputFolder);

        File inputFile = new File(Paths.SOLUTION_ACDC_INPUT + SolutionRsf);

        new IoannaRunnable(inputFile, outputDir).run();
    }

    private static void FileOnlySystemClustering()
    {
        ExecutorService cachedPool = Executors.newFixedThreadPool(4);
        String inputFolder = Paths.ACDC_FILE_INPUT_PATH;
        File inputDir = new File(inputFolder);

        String outputFolder = Paths.ACDC_FILE_OUTPUT_PATH;
        File outputDir = new File(outputFolder);
        if (!outputDir.exists())
        {
            outputDir.mkdirs();
        }

        File[] inputFiles = inputDir.listFiles();

        for (File inputFile : inputFiles)
        {
            if (inputFile.getName().endsWith("_file_acdc.rsf"))
            {
                System.out.println(inputFile.toString());
                cachedPool.execute(new IoannaRunnable(inputFile, outputDir));
            }
        }

        try
        {
            cachedPool.shutdown();
            cachedPool.awaitTermination(10, TimeUnit.DAYS);
        }
        catch (InterruptedException ignored)
        {

        }
    }

    static class IoannaRunnable implements Runnable
    {

        private File inputFile;
        private File outputFile;

        IoannaRunnable(File myInFile, File myOutFile)
        {
            setMyFile(myInFile, myOutFile);
        }

        void setMyFile(File myInFile, File myOutFile)
        {
            this.inputFile = myInFile;
            this.outputFile = myOutFile;
        }

        public void run()
        {
            String outputFolder = outputFile.getPath();
            File outputFile = new File(outputFolder + "/" + inputFile.getName().split("\\.")[0] + ".rsf");
            String[] args = new String[2];

            args[0] = inputFile.toString(); // input .rsf
            args[1] = outputFolder + "/" + inputFile.getName(); // output .rsf
            ACDC.main(args);
        }
    }
}
