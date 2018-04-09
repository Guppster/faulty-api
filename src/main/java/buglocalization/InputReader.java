package buglocalization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class InputReader
{
    private final String productName;
    private final RsfRepresentation rsfRepresentation;
    private final Set<String> inputTokens = new HashSet<>();

    @SuppressWarnings("unused")
    private String BugReport;

    private String[] filenames;
    private String[] BugReports;
    private ArrayList<Integer> BugNumbers;
    private int i = -1;
    private String filename;
    private ArrayList<String> lsaTokens;
    private Set<String> answers = new HashSet<>();
    private Map<String, String> fileName2Cluster;
    private Map<String, Set<String>> clusterName2fileName;

    public InputReader(String productName, RsfRepresentation rsfRepresentation)
    {
        this.productName = productName;
        this.rsfRepresentation = rsfRepresentation;
        FetchBugNumbers();
        makeNameArrays(BugNumbers);
        readClusters();
    }

    private Set<String> compileAnswers(int Bug_ID)
    {
        BufferedReader ansbr;
        File answers = new File(Paths.GOLDSTANDARD + productName + "/" + BugNumbers.get(Bug_ID) + "_sol.txt");
        this.answers = new HashSet<>();

        try
        {
            ansbr = new BufferedReader(new FileReader(answers));
            while (ansbr.ready())
            {
                String s = ansbr.readLine().toLowerCase();
                if (rsfRepresentation.isEntity(s))
                {
                    this.answers.add(s);
                }
            }
            ansbr.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }

        return this.answers;
    }

    private void makeNameArrays(ArrayList<Integer> BugNumbers)
    {
        filenames = new String[BugNumbers.size()];
        BugReports = new String[BugNumbers.size()];
        Collections.sort(BugNumbers);
        int i = 0;
        for (Integer I : BugNumbers)
        {
            filenames[i] = I + "_LSA.txt";
            BugReports[i] = I + ".txt";
            i++;
        }
    }

    public String getBugNumber()
    {
        return BugReport.split("\\.")[0];
    }

    private void FetchBugNumbers()
    {
        BugNumbers = new ArrayList<>();
        try
        {
            File BugIDs = new File(Paths.BRLists + productName + ".lst");
            BufferedReader bugIDr = new BufferedReader(new FileReader(BugIDs));
            while (bugIDr.ready())
            {
                int buffer = Integer.parseInt(bugIDr.readLine());
                BugNumbers.add(buffer);
            }
            bugIDr.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

    private void readLsaInput()
    {
        answers = compileAnswers(i);

        try (BufferedReader lsaInputReader = new BufferedReader(new FileReader(
                Paths.FINALINPUTBRS + productName + "/input/" + filename)))
        {
            lsaTokens = new ArrayList<>();
            String Entity;
            while (lsaInputReader.ready())
            {
                Entity = lsaInputReader.readLine();
                lsaTokens.add(Entity);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        for (String answer : answers)
        {
            lsaTokens.remove(answer);
        }
    }

    private void readInputBugReport()
    {
        try (BufferedReader inputBugReportReader = new BufferedReader(new FileReader(Paths.QUERYBRS + productName + "/" + BugReport)))
        {
            while (inputBugReportReader.ready())
            {
                inputTokens.add(inputBugReportReader.readLine());
            }

            inputTokens.removeAll(answers);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean nextReport()
    {
        if (i < BugReports.length - 1)
        {
            i++;

            BugReport = BugReports[i];
            filename = filenames[i];

            System.out.println(filename);

            answers.clear();
            answers.addAll(compileAnswers(i));

            readLsaInput();
            readInputBugReport();

            return true;
        }

        return false;
    }

    private void readClusters()
    {
        fileName2Cluster = new HashMap<>();
        clusterName2fileName = new HashMap<>();

        //Prepare reader for productName
        try (BufferedReader clustersReader = new BufferedReader(
                new FileReader(Paths.ACDC_OUTPUT_PATH + productName + "_file_final_acdc.rsf")))
        {
            //Wait till the file is ready to read (not needed)
            while (clustersReader.ready())
            {
                String[] tokens = clustersReader.readLine().split(" ");
                fileName2Cluster.put(rsfRepresentation.getName(tokens[2]), rsfRepresentation.getName(tokens[1].split("\\.")[0]));
            }

            clustersReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Flip the relation
        for (Entry<String, String> file2Cluster : fileName2Cluster.entrySet())
        {
            if (clusterName2fileName.containsKey(file2Cluster.getValue()))
            {
                clusterName2fileName.get(file2Cluster.getValue()).add(file2Cluster.getKey());
            }
            else
            {
                Set<String> toInsert = new HashSet<>();
                toInsert.add(file2Cluster.getKey());
                clusterName2fileName.put(file2Cluster.getValue(), toInsert);
            }
        }

    }

    public Set<String> getLsaTokens()
    {
        return new HashSet<>(lsaTokens);
    }

    public Set<String> getAnswers()
    {
        return answers;
    }

    public String getFilename()
    {
        return filename;
    }

    public ArrayList<Integer> getBugNumbers()
    {
        return BugNumbers;
    }

    public String[] getBugReports()
    {
        return BugReports;
    }

    public String[] getFileNames()
    {
        return filenames;
    }

    public Set<String> getInputTokens()
    {
        return inputTokens;
    }

    public Map<String, String> getFileNames2Clusters()
    {
        return fileName2Cluster;
    }

    public Map<String, Set<String>> getClusterName2FileName()
    {
        return clusterName2fileName;
    }
}
