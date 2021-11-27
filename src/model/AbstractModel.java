package model;

import io.InputInterface;
import io.ListEdgeBasedInput;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractModel {
    /**
     * time limit in minute
     */
    protected final int TIME_LIMIT = 20;
    /**
     * result partitions
     */
    protected List<List<Integer>> bestPartitions;

    /**
     * weight of the solution found
     */
    protected double weighted;

    /**
     * run time of this model
     */
    protected double timeElapse;

    /**
     * status of solution ( Optimal, Feasible, Infeasible)
     */
    protected String status;

    /**
     * used to set time limit for solving
     */
    protected boolean isTimeUp;

    /**
     * set timer
     */
    ScheduledExecutorService timer;

    protected int nRun;

    /**
     * interface to read input
     */
    protected InputInterface inputInterface;
    protected int k, alpha;

    protected String modelName;

    public String getModelName() {
        return modelName;
    }

    public void setNRun(int nRun) {
        this.nRun = nRun;
    }

    /**
     * read input data from file
     * @param fileName string contains name of file
     * @throws IOException {@code FileNotFoundException} or {@code NullPointerException}
     */
    protected void readInput(String fileName) throws IOException{
        inputInterface = new ListEdgeBasedInput(fileName);
    }

    /**
     * read input data from {@code file}
     * @param file object reference to data input
     * @throws IOException {@code FileNotFoundException} or {@code NullPointerException}
     */
    protected void readInput(File file) throws IOException{
        inputInterface = new ListEdgeBasedInput(file);
    }

    /**
     * write result of each run to specified files
     * @param filename (recommended including _x.txt , x is the ith run (if nRun >1) )
     * @param k number of partitions
     * @param alpha bound difference among partitions
     * @param runIdx the index of this run
     * @param delimiter the separate character between content
     */
    protected void writeResult(String filename, int k, int alpha, int runIdx, String delimiter){
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename,true)));
            printWriter.printf("%s{\n", delimiter);
            printWriter.printf("%2s\"K\": %d,\n", "", k);
            printWriter.printf("%2s\"Alpha\": %d,\n", "", alpha);
            printWriter.printf("%2s\"runIdx\": %d,\n", "", runIdx);
            printWriter.printf("%2s\"weight\": %f,\n", "", this.weighted);
            printWriter.printf("%2s\"timeElapsed\": %f,\n", "", this.timeElapse);
            printWriter.printf("%2s\"partitions\": {\n", "");

            int i=0; String deli="";
            for (List<Integer> parti : this.bestPartitions){
                printWriter.printf("%s%4s\"%d\": %s\n", deli, "", i++, parti.toString());
                deli=",";
            }

            printWriter.printf("%2s}\n",""); // close bracket for partition field
            printWriter.printf("}\n"); // cloe bracket for this object
            printWriter.close();
            bestPartitions.clear(); // clear to store result of new run(if nRun > 1)
        } catch (FileNotFoundException fileNotFoundException){
            System.out.println(fileNotFoundException.getMessage());
            fileNotFoundException.printStackTrace();
        }
    }

    /**
     * write log the results of the whole runs
     * @param filename (recommended csv file)
     * @param dataName name of input Data
     * @param k number of partitions
     * @param alpha bound of difference among partitions
     * @param runIdx the index of this run
     */
    protected void writeLog(String filename, String dataName, int k, int alpha, int runIdx){
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
            // columns: data, k, alpha, run idx, result, time, status
            printWriter.printf("%s,%d,%d,%d,%f,%f,%s",
                    dataName,k,alpha,runIdx,this.weighted,this.timeElapse,this.status);
            printWriter.close();
        } catch (FileNotFoundException fileNotFoundException){
            System.out.println(fileNotFoundException.getMessage());
            fileNotFoundException.printStackTrace();
        }
    }

    /**
     * implement a specific algorithm
     * @param k number of partitions
     * @param alpha bound of difference among partitions
     */
    protected abstract void solve(int k, int alpha);

    /**
     * prepare to solve, start timing
     * @param dataName name of input
     * @param k number of partitions
     * @param alpha bound of difference among partitions
     */
    protected void run(String dataName, int k, int alpha){
        System.out.printf("Start Solving %s with k=%d, alpha=%d",dataName,k,alpha);

        timer = Executors.newSingleThreadScheduledExecutor();
        timer.schedule(new Timer(this), TIME_LIMIT, TimeUnit.MINUTES);

        this.timeElapse = System.currentTimeMillis();
        this.solve(k,alpha);
        this.timeElapse = System.currentTimeMillis()- this.timeElapse;

        stop();
    }

    /**
     * stop timer
     */
    protected void stop(){
        timer.shutdownNow();
        isTimeUp = true;
    }


    private static final String inputFolder ="data/input/";
    private static final String outputFolder="data/output/";
    private static final String logFolder="data/log/";

    public static void createLogFile(String filename) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename,true)));
        // columns: data, k, alpha, run idx, result, time, status
        printWriter.printf("data,k,alpha,run idx, result,time, status");
        printWriter.close();

    }

    /**
     *
     * @param model specific algorithm
     * @param nRun number of run time
     * @param kArray : array of number of required partition
     * @param alphaArray : array of alpha
     * @param dataTypes specify the dataset :
     *                (small, medium, large, test),
     *                (dense, sparse),
     *                (distribution:Gauss, uniform)
     */
    public static void execute(AbstractModel model, int nRun,int[] kArray, int[]alphaArray, String... dataTypes){
        StringBuilder inputDir = new StringBuilder(inputFolder);
        StringBuilder outputFileName = new StringBuilder(outputFolder+model.getModelName()+"/");
        StringBuilder logFileName = new StringBuilder(logFolder+model.getModelName());
        for (String dataType: dataTypes){
            inputDir.append(dataType).append("/");
            outputFileName.append(dataType).append("/");
            logFileName.append("_").append(dataType);
        }


        File dir = new File(inputDir.toString());

        try {
            logFileName.append(".csv");
            createLogFile(logFileName.toString());
        }catch (FileNotFoundException fileNotFoundException){
            System.out.printf("Program stopped because log file %s cannot be created\n%n",logFileName);
            return;
        }

        for (File f : Objects.requireNonNull(dir.listFiles())) {
//			File[] files = {new File("data/uniform/W100-H020/NS0050-A0060.txt")};
//			for (File f: files){
            String dataName;
            if (!f.isDirectory() && (dataName = f.getName()).endsWith(".txt")) {
                dataName = dataName.substring(0, dataName.lastIndexOf(".txt")); // remove postfix .txt
                outputFileName.append(dataName).append(".json");
//                int lenResultName = outputFileName.length();

                try {
                    model.readInput(f);
                    String delimiter="";
                    for (int k : kArray){
                        for (int alpha: alphaArray) {
                            for (int i = 0; i < nRun; ++i) {
//                                model.setNRun(i+1);
                                model.run(dataName,k,alpha);

                                model.writeResult(outputFileName.toString(),k,alpha,i,delimiter); delimiter=",";
                                model.writeLog(logFileName.toString(),dataName,k,alpha,i);
                            }
                        }
                    }

                } catch (IOException ioException){
                    System.out.println("Error when reading data from "+dataName);
                    System.out.println(ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        }
    }
}
