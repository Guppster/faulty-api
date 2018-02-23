package step2;

import gr.ntua.softlab.filepaths.Paths;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import acdc.ACDC;

public class ThreadTest{

	public static class IoannaRunnable implements Runnable{

		private File inputFile;
		private File outputFile;

		public IoannaRunnable(File myInFile, File myOutFile){
			setMyFile(myInFile, myOutFile);
		}

		public void setMyFile(File myInFile, File myOutFile){
			this.inputFile = myInFile;
			this.outputFile = myOutFile;
		}

		public void run(){
			String outputFolder = outputFile.getPath();
			File outputFile = new File(outputFolder + "/" + inputFile.getName().split("\\.")[0] + ".rsf");
			System.out.println(outputFile.toString());
			if (!outputFile.exists()) {
				String[] args = new String[3];
				// String[] input = myFile.getName().split(".");

				args[0] = inputFile.toString(); // input .rsf
				args[1] = outputFolder + "/" + inputFile.getName(); // output .rsf
				args[2] = "-d1";
				java.lang.System.out.println(args[0] + " " + args[1] + " " + args[2]);
				ACDC.main(args);
			}
		}
	};

	public static void main(String[] args){
		// *** CLUSTERING OF COMPLETE SYSTEM ***//
		CompleteSystemClustering();

		// *** CLUSTERING OF FILE ONLY SYSTEM ***//
		// FileOnlySystemClustering();

		System.exit(0);
	}

	public static void executeClustering(){
		CompleteSystemClustering();
		FileOnlySystemClustering();
		System.exit(0);
	}

	private static void CompleteSystemClustering(){
		ExecutorService cachedPool = Executors.newFixedThreadPool(4);
		String inputFolder = Paths.ACDC_INPUT_PATH;
		File inputDir = new File(inputFolder);
		String outputFolder = Paths.ACDC_OUTPUT_PATH;
		File outputDir = new File(outputFolder);
		if (!outputDir.exists())
			outputDir.mkdirs();

		File[] inputFiles = inputDir.listFiles();
		System.out.println(inputFiles[0]);
		for (File inputFile : inputFiles) {
			if (inputFile.getName().endsWith("_final_acdc.rsf"))
				cachedPool.execute(new IoannaRunnable(inputFile, outputDir));
		}

		try {
			cachedPool.shutdown();
			cachedPool.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException ignored) {

		}
	}

	private static void FileOnlySystemClustering(){
		ExecutorService cachedPool = Executors.newFixedThreadPool(4);
		String inputFolder = Paths.ACDC_FILE_INPUT_PATH;
		File inputDir = new File(inputFolder);

		String outputFolder = Paths.ACDC_FILE_OUTPUT_PATH;
		File outputDir = new File(outputFolder);
		if (!outputDir.exists())
			outputDir.mkdirs();

		File[] inputFiles = inputDir.listFiles();

		for (File inputFile : inputFiles) {
			if (inputFile.getName().endsWith("_file_acdc.rsf")) {
				System.out.println(inputFile.toString());
				cachedPool.execute(new IoannaRunnable(inputFile, outputDir));
			}
		}

		try {
			cachedPool.shutdown();
			cachedPool.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException ignored) {

		}
	}
}
