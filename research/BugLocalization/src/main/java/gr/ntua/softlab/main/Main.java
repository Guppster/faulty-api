package gr.ntua.softlab.main;

import gr.ntua.softlab.filepaths.Paths;
import gr.ntua.softlab.makefilersf.FileRsfMaker;
import gr.ntua.softlab.solutionspace.InputReader;
import gr.ntua.softlab.solutionspaceclustering.ThreadTest;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Main
{
    private final int maxClusterSize = 5;
    private String productName;
    private FileRsfMaker fileRsfMaker;
    private InputReader inputReader;
    private Set<String> answer = new HashSet<>();
    private Set<String> lsaTokens = new HashSet<>();
    private Set<String> inputTokens = new HashSet<>();
    private Set<String> fileLsaTokens = new HashSet<>();
    private final Map<String, String> fileNames2Cluster = new HashMap<>();
    private final Map<String, Set<String>> clusterName2FileName = new HashMap<>();
    private final Set<String> clique = new HashSet<>();
    private final Set<String> unusedRelations = new HashSet<>();
    private final Set<String> bad = new HashSet<>();
    private Map<String, Map<String, Set<String>>> allFileRelationsReversed;
    private final Map<String, Double> averagePerFileCon = new HashMap<>();
    private final Map<String, Double> averageToCliqueCon = new HashMap<>();
    private Map<String, Double> fileToCliqueClusterConnectivity = new HashMap<>();
    private Map<String, Double> fileToCliqueConnectivity = new HashMap<>();
    private String currentFileName = "";
    private Map<String, Set<String>> solutionClusterName2FileName;
    private Map<String, String> solutionFileName2ClusterName;
    private final Set<String> finalV = new HashSet<>();
    private Set<String> fileInputTokens;

    public static void main(String[] args)
    {
        Main main = new Main();
        main.run();
    }

    private void run()
    {
        //Prompt for the name of he folder that holds the rsf files
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in)))
        {
            System.out.println("productName : ");
            productName = consoleReader.readLine();
            consoleReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Grenerate rsf representation
        fileRsfMaker = new FileRsfMaker(productName);
        fileRsfMaker.doLift();

        //Allows reading of the rsf representation
        inputReader = new InputReader(productName, fileRsfMaker.getRsfRepresentation());

        //Find the cluster from the fliename
        fileNames2Cluster.putAll(inputReader.getFileNames2Clusters());

        //Find the file from the cluster
        clusterName2FileName.putAll(inputReader.getClusterName2FileName());

        //Relations we don't want to analyze
        unusedRelations.add("macrodefinition");
        unusedRelations.add("methodbelongstoclass");
        unusedRelations.add("declaredin");
        unusedRelations.add("hastype");
        unusedRelations.add("entitylocation");
        unusedRelations.add("filebelongstomodule");
        unusedRelations.add("usestype");
        unusedRelations.add("accessibleentitybelongstofile");
        unusedRelations.add("inheritsfrom");
        unusedRelations.add("classbelongstofile");
        unusedRelations.add("definedin");
        unusedRelations.add("attributebelongstoclass");

        while (prepareNextReport())
        {
            System.out.println("------------------------------------------------------------------------------------------------------------------------");
        }
    }

    private boolean prepareNextReport()
    {
        clearSets();

        //Check if another report exists, if not exit
        if (!inputReader.nextReport())
        {
            return false;
        }

        //answer is now the intersect of answer and fileRsfMaker entity names
        //Making sure the tokens are an rsf entity
        answer.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());

        answer = inputReader.getAnswer();

        lsaTokens = inputReader.getLsaTokens();

        //lsaTokens is now the intersect of lsaTokens and fileRsfMaker entity names
        //Making sure the tokens are an rsf entity
        lsaTokens.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());

        inputTokens = inputReader.getInputTokens();

        //inputTokens is now the intersect of inputTokens and fileRsfMaker entity names
        //Making sure the tokens are an rsf entity
        inputTokens.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());

        currentFileName = inputReader.getFilename();

        reverseAllFileRelations();
        constructSolutionSpace();
        performRanking();

        //Indicate that there may be more bug reports to process
        return true;
    }

    private void constructSolutionSpace()
    {
        // make fileLsaTokens
        fileLsaTokens = new HashSet<>();

        Map<String, Set<String>> entityNameToFileNames = fileRsfMaker.getEntityNameToFileNames();

        for (String token : lsaTokens)
        {
            if (fileRsfMaker.getEntityNameToFileNames().containsKey(token))
            {
                fileLsaTokens.addAll(fileRsfMaker.getEntityNameToFileNames().get(token));
            }

            fileLsaTokens.retainAll(fileRsfMaker.getAllRelations().get("filebelongstomodule").keySet());
        }

        // Make fileInputTokens and remove the GoldStandard
        fileInputTokens = new HashSet<>();

        for (String s : inputTokens)
        {
            fileInputTokens.addAll(entityNameToFileNames.get(s));
            fileInputTokens.retainAll(fileRsfMaker.getAllRelations().get("filebelongstomodule").keySet());
        }
    }

    @SuppressWarnings("unused")
    private void makeFilename2Parts()
    {
        for (String s : fileNames2Cluster.keySet())
        {
            for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
            {
                System.out.println(w);
            }
            System.out.println("----------------------------------------------------------");
        }
    }

    private void reverseAllFileRelations()
    {
        allFileRelationsReversed = new HashMap<>();

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfMaker.getAllFileRelations().entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String targetEntity : entityToEntities.getValue())
                {
                    if (allFileRelationsReversed.containsKey(completeRelation.getKey()))
                    {
                        if (allFileRelationsReversed.get(completeRelation.getKey()).containsKey(targetEntity))
                        {
                            allFileRelationsReversed.get(completeRelation.getKey()).get(targetEntity).add(entityToEntities.getKey());
                        }
                        else
                        {
                            Set<String> toInsert = new HashSet<>();
                            toInsert.add(entityToEntities.getKey());

                            allFileRelationsReversed.get(completeRelation.getKey()).put(targetEntity, toInsert);
                        }
                    }
                    else
                    {
                        Map<String, Set<String>> toInsertMap = new HashMap<>();
                        Set<String> toInsert = new HashSet<>();

                        toInsert.add(entityToEntities.getKey());
                        toInsertMap.put(targetEntity, toInsert);

                        allFileRelationsReversed.put(completeRelation.getKey(), toInsertMap);
                    }
                }
            }
        }
    }

    private void performRanking()
    {
        Set<String> Visited = new HashSet<>(expandeTokenSet(fileLsaTokens));

        finalV.addAll(Visited);

        Set<String> temp = new HashSet<>(fileRsfMaker.getAllRelations().get("filebelongstomodule").keySet());

        int c = 0;

        for (String s : temp)
        {
            if (!s.contains(".h"))
            {
                c++;
            }
        }

        System.out.println("Total System Files = " + temp.size());
        System.out.println("Total non .h Files = " + c);

        Set<String> nonLibraries = new HashSet<>();
        for (String s : finalV)
        {
            if (!s.contains(".h"))
            {
                nonLibraries.add(s);
            }
        }

        System.out.println("Total non .h Files from Expansion= " + nonLibraries.size());

        if (nonLibraries.size() < 0.15 * (double) c)
        {
            outputURanked();
        }
        else
        {
            performClusterRanking();
        }
    }

    private void outputURanked()
    {
        Set<String> Visited = new HashSet<>();

        clique.clear();

        for (String s : finalV)
        {
            if (!s.contains(".h"))
            {
                Visited.add(s);
            }
        }

        finalV.clear();
        finalV.addAll(Visited);

        Map<String, Map<String, Set<String>>> fileRsfIn = new HashMap<>(fileRsfMaker.getAllFileRelations());

        for (String s : unusedRelations)
        {
            fileRsfIn.remove(s);
        }

        clique.addAll(selectMostConnected(fileRsfMaker.getAllFileRelations(), finalV));

        System.out.println("clique size = " + clique.size());

        for (String fileName : finalV)
        {
            fileToCliqueConnectivity.put(fileName, 0.0);
        }

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String s : entityToEntities.getValue())
                {
                    if (finalV.contains(s))
                    {
                        fileToCliqueConnectivity.put(s, fileToCliqueConnectivity.get(s) + 1);
                    }
                    else if (finalV.contains(entityToEntities.getKey()))
                    {
                        fileToCliqueConnectivity.put(entityToEntities.getKey(), fileToCliqueConnectivity.get(entityToEntities.getKey()) + 1);
                    }
                }
            }
        }
        fileToCliqueConnectivity = sortByComparatorDouble(fileToCliqueConnectivity);

        Map<String, Double> treeMap = new TreeMap<>(Comparator.reverseOrder());

        treeMap.putAll(fileToCliqueConnectivity);

        try
        {
            new File(Paths.URANKEDREPORTS + productName).mkdirs();

            BufferedWriter URankedWriter = new BufferedWriter

                    (new FileWriter(Paths.URANKEDREPORTS + productName + "/" + currentFileName.split("_")[0] + ".csv"));

            System.out.println(currentFileName.split("_")[0]);

            for (Entry<String, Double> file : treeMap.entrySet())
            {
                if (!answer.contains(file.getKey()))
                {
                    URankedWriter.write(String.format("\t%-85s\t%15f\t\n", file.getKey(), file.getValue()));
                    System.out.format("\t%-85s\t%15f\t\n", file.getKey(), file.getValue());
                }
                else
                {
                    URankedWriter.write(String.format("\t%-85s\t%15f\t<-------------\n", file.getKey(), file.getValue()));
                    System.out.format("\t%-85s\t%15f\t<-------------\n", file.getKey(), file.getValue());
                }
            }

            URankedWriter.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

    private void performClusterRanking()
    {
        Set<String> clustersToBreakDown = new HashSet<>();
        Set<String> clique = new HashSet<>();
        Set<String> GSClusters = new HashSet<>();
        Set<String> bad = new HashSet<>();
        clique.clear();
        Set<String> Visited = new HashSet<>(finalV);
        Set<String> hits = new HashSet<>(finalV);

        // Recall before applying anything but the expansion step and without removing the .h files
        System.out.println("LsaFileTokens at start = " + fileLsaTokens.size());
        System.out.println("LsaFileTokens after expansion " + finalV.size());

        hits.retainAll(answer);

        System.out.println("Recall (Pre reduction)= " + hits.size() / (double) answer.size());

        fileInputTokens.removeAll(bad);

        clique.addAll(selectMostConnected(fileRsfMaker.getAllFileRelations(), finalV));

        System.out.println("***********************PERFORMING CLUSTERING***********************");
        printSolutionRsf(finalV, fileRsfMaker.getAllFileRelations(), inputReader.getBugNumber(), fileRsfMaker.getEntityId(), clique);

        // remove .h from answer
        hits.addAll(answer);
        for (String s : hits)
        {
            if (s.contains(".h"))
            {
                answer.remove(s);
            }
        }

        hits.clear();

        for (String fileName : answer)
        {
            GSClusters.add(solutionFileName2ClusterName.get(fileName));
        }

        // remove .h from visited
        hits.addAll(Visited);
        for (String s : hits)
        {
            if (s.contains(".h"))
            {
                Visited.remove(s);
            }
        }

        finalV.clear();
        finalV.addAll(Visited);

        // Cluster size calculation
        for (Entry<String, Set<String>> cluster : solutionClusterName2FileName.entrySet())
        {
            if (cluster.getValue().size() > maxClusterSize)
            {
                clustersToBreakDown.add(cluster.getKey());
            }
        }

        // Final results calculation
        toFromCliqueConnectivity(fileRsfMaker.getAllFileRelations(), clique, GSClusters);

        Map<String, Double> clusterScored = new HashMap<>();
        double MaxClusterSize = 0.0;

        for (Entry<String, Set<String>> e : solutionClusterName2FileName.entrySet())
        {
            if (MaxClusterSize < e.getValue().size())
            {
                MaxClusterSize = e.getValue().size();
            }
        }

        // MetricHasBeenChanged
        for (Entry<String, Double> cluster : averagePerFileCon.entrySet())
        {
            if (solutionClusterName2FileName.get(cluster.getKey()).size() != 0)
            {
                clusterScored.put(cluster.getKey(), (cluster.getValue() / averageToCliqueCon.get(cluster.getKey()))
                                                    * solutionClusterName2FileName.get(cluster.getKey()).size());
            }
        }

        clusterScored = sortByComparatorDouble(clusterScored);

        List<Map.Entry<String, Double>> list = new LinkedList<>(clusterScored.entrySet());
        Map<String, Set<String>> top50PercentileClusters = new HashMap<>();
        double max = 0;

        for (Entry<String, Double> cluster : clusterScored.entrySet())
        {
            if (max < cluster.getValue())
            {
                max = cluster.getValue();
            }
        }

        // Expectation Calculation
        Map<String, Double> clusterExpectation = new HashMap<>();
        double meanExpectation = 0.0;
        for (Entry<String, Double> cluster : clusterScored.entrySet())
        {
            clusterExpectation.put(cluster.getKey(), cluster.getValue()
                                                     * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()));
            if (!Double.isNaN(clusterExpectation.get(cluster.getKey())) && clusterExpectation.get(cluster.getKey()) < 10000000)
            {
                meanExpectation += clusterExpectation.get(cluster.getKey());
            }
        }

        meanExpectation = meanExpectation / clusterExpectation.size();
        System.out.println("meanExpectation = " + meanExpectation);

        try
        {
            new File(Paths.FIRSTLEVELREPORTS + productName).mkdirs();
            BufferedWriter firstLevelWriter = new BufferedWriter
                    (new FileWriter(Paths.FIRSTLEVELREPORTS + productName + "/" + currentFileName.split("_")[0] + ".csv"));

            firstLevelWriter.write(String.format("%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n", "ClusterName", "Score", "expectation",
                    "Connections", "ToClique", "ClusterSize", "ScorePercent"));

            System.out.format("%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n", "ClusterName", "Score", "expectation", "Connections", "ToClique",
                    "ClusterSize", "ScorePercent");

            for (Entry<String, Double> cluster : clusterScored.entrySet())
            {
                if (!GSClusters.contains(cluster.getKey()))
                {
                    if (list.indexOf(cluster) > 0.5 * list.size() && solutionClusterName2FileName.get(cluster.getKey()).size() < 0.5 * MaxClusterSize)
                    {
                        top50PercentileClusters.put(cluster.getKey(), solutionClusterName2FileName.get(cluster.getKey()));

                        firstLevelWriter.write(String.format(
                                "%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max));

                        System.out.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max);
                    }
                }
                else
                {
                    if (list.indexOf(cluster) > 0.5 * list.size() && solutionClusterName2FileName.get(cluster.getKey()).size() < 0.5 * MaxClusterSize)
                    {
                        top50PercentileClusters.put(cluster.getKey(), solutionClusterName2FileName.get(cluster.getKey()));

                        firstLevelWriter.write(String.format(
                                "%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\t<++++++++++++GS\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max));

                        System.out.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f <++++++++++++GS\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max);
                    }
                }
            }

            for (Entry<String, Set<String>> finalSelectedCluster : top50PercentileClusters.entrySet())
            {
                firstLevelWriter.write("------------------------------______________________________---------------------------\n");
                firstLevelWriter.write(finalSelectedCluster.getKey() + "\n");
                firstLevelWriter.write("....................................................................\n");
                System.out.println("------------------------------______________________________---------------------------");

                System.out.println(finalSelectedCluster.getKey());
                System.out.println("....................................................................");

                for (Entry<String, Double> filetoCliqueCluster : fileToCliqueClusterConnectivity.entrySet())
                {
                    if (finalSelectedCluster.getValue().contains(filetoCliqueCluster.getKey()) && filetoCliqueCluster.getValue() > 0)
                    {
                        if (!answer.contains(filetoCliqueCluster.getKey()))
                        {
                            firstLevelWriter.write(String.format("\t\t%-85s\t%15f\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue()));

                            System.out.format("\t\t%-85s\t%15f\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue());
                        }
                        else
                        {
                            firstLevelWriter.write(String.format("\t\t%-85s\t%15f\t<-----------\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue()));

                            System.out.format("\t\t%-85s\t%15f<-----------\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue());
                        }
                    }
                }
                firstLevelWriter.write("______________________________------------------------------___________________________\n");
                System.out.println("______________________________------------------------------___________________________");
            }
            firstLevelWriter.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }

        // ZeroReporting
        try
        {
            new File(Paths.ZEROREPORTS + productName).mkdirs();

            BufferedWriter ZeroWriter = new BufferedWriter(new FileWriter(Paths.ZEROREPORTS + productName + "/" + currentFileName.split("_")[0] + ".csv"));

            Map<String, Double> treeMap = new TreeMap<>(Comparator.reverseOrder());

            treeMap.putAll(fileToCliqueConnectivity);
            fileToCliqueConnectivity.clear();
            fileToCliqueConnectivity.putAll(treeMap);

            for (Entry<String, Set<String>> finalSelectedCluster : top50PercentileClusters.entrySet())
            {
                ZeroWriter.write("------------------------------______________________________---------------------------\n");

                System.out.println("------------------------------______________________________---------------------------");

                ZeroWriter.write(finalSelectedCluster.getKey() + "\n");

                System.out.println(finalSelectedCluster.getKey());

                ZeroWriter.write("....................................................................\n");

                System.out.println("....................................................................");

                for (Entry<String, Double> filetoCliqueCluster : fileToCliqueClusterConnectivity.entrySet())
                {
                    if (finalSelectedCluster.getValue().contains(filetoCliqueCluster.getKey()) && filetoCliqueCluster.getValue() == 0)
                    {
                        if (!answer.contains(filetoCliqueCluster.getKey()))
                        {
                            ZeroWriter.write(String.format("\t\t%-85s\t%15f\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue()));

                            System.out.format("\t\t%-85s\t%15f\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue());
                        }
                        else
                        {
                            ZeroWriter.write(String.format("\t\t%-85s\t%15f\t<-----------\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue()));

                            System.out.format("\t\t%-85s\t%15f\t<-----------\n", filetoCliqueCluster.getKey(), filetoCliqueCluster.getValue());
                        }
                    }
                }
                ZeroWriter.write("______________________________------------------------------___________________________\n");
                System.out.println("______________________________------------------------------___________________________");
            }
            ZeroWriter.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }

        // BigReporting
        try
        {
            top50PercentileClusters.clear();

            new File(Paths.BIGREPORTS + productName).mkdirs();

            BufferedWriter BigWriter = new BufferedWriter(new FileWriter(Paths.BIGREPORTS + productName + "/" + currentFileName.split("_")[0] + ".csv"));

            BigWriter.write(String.format("%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n", "ClusterName", "Score", "expectation", "Connections", "ToClique", "ClusterSize", "ScorePercent"));

            System.out.format("%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n", "ClusterName", "Score", "expectation", "Connections", "ToClique", "ClusterSize", "ScorePercent");

            for (Entry<String, Double> cluster : clusterScored.entrySet())
            {
                if (!GSClusters.contains(cluster.getKey()))
                {
                    if (list.indexOf(cluster) > 0.5 * list.size() && solutionClusterName2FileName.get(cluster.getKey()).size() >= 0.5 * MaxClusterSize)
                    {
                        top50PercentileClusters.put(cluster.getKey(), solutionClusterName2FileName.get(cluster.getKey()));

                        BigWriter.write(String.format(
                                "%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max));
                        System.out.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue()
                                * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max);
                    }
                }
                else
                {
                    if (list.indexOf(cluster) > 0.5 * list.size()
                        && solutionClusterName2FileName.get(cluster.getKey()).size() >= 0.5 * MaxClusterSize)
                    {
                        top50PercentileClusters.put(cluster.getKey(), solutionClusterName2FileName.get(cluster.getKey()));
                        BigWriter
                                .write(String.format(
                                        "%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\t<++++++++++++GS\n",
                                        cluster.getKey(),
                                        cluster.getValue(),
                                        cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                        averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                        averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                        solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max));

                        System.out.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f <++++++++++++GS\n",
                                cluster.getKey(),
                                cluster.getValue(),
                                cluster.getValue()
                                * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()),
                                averagePerFileCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                averageToCliqueCon.get(cluster.getKey()) * solutionClusterName2FileName.get(cluster.getKey()).size(),
                                solutionClusterName2FileName.get(cluster.getKey()).size(), cluster.getValue() / max);
                    }
                }
            }
            Map<Double, List<String>> inClusterRanking = new HashMap<>();

            for (Entry<String, Set<String>> finalSelectedCluster : top50PercentileClusters.entrySet())
            {
                BigWriter.write("------------------------------______________________________---------------------------\n");
                System.out.println("------------------------------______________________________---------------------------");
                BigWriter.write(finalSelectedCluster.getKey() + "\n");
                System.out.println(finalSelectedCluster.getKey());
                BigWriter.write("....................................................................\n");
                System.out.println("....................................................................");
                for (Entry<String, Double> filetoCliqueCluster : fileToCliqueClusterConnectivity.entrySet())
                {
                    if (finalSelectedCluster.getValue().contains(filetoCliqueCluster.getKey()))
                    {
                        if (inClusterRanking.containsKey(filetoCliqueCluster.getValue()))
                        {
                            inClusterRanking.get(filetoCliqueCluster.getValue()).add(filetoCliqueCluster.getKey());
                        }
                        else
                        {
                            inClusterRanking.put(filetoCliqueCluster.getValue(), new ArrayList<>());
                            inClusterRanking.get(filetoCliqueCluster.getValue()).add(filetoCliqueCluster.getKey());
                        }
                    }
                }

                Map<Double, List<String>> treeMap1 = new TreeMap<>(Comparator.reverseOrder());

                treeMap1.putAll(inClusterRanking);

                for (Entry<Double, List<String>> sameValue : treeMap1.entrySet())
                {
                    Collections.sort(sameValue.getValue());
                    for (String s : sameValue.getValue())
                    {
                        if (!answer.contains(s))
                        {
                            BigWriter.write(String.format("\t\t%-85s\t%15f\n", s, sameValue.getKey()));
                            System.out.format("\t\t%-85s\t%15f\n", s, sameValue.getKey());

                        }
                        else
                        {
                            BigWriter.write(String.format("\t\t%-85s\t%15f\t<-----------\n", s, sameValue.getKey()));
                            System.out.format("\t\t%-85s\t%15f\t<-----------\n", s, sameValue.getKey());
                        }
                    }
                }
                inClusterRanking.clear();
                treeMap1.clear();
                BigWriter.write("______________________________------------------------------___________________________\n");
                System.out.println("______________________________------------------------------___________________________");
            }
            BigWriter.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

    private Map<String, Set<String>> rearrangeClusters(Set<String> clustersToBreakDown, Set<String> clique)
    {
        Map<String, Map<String, Integer>> perClusterCliqueConnectivity = new HashMap<>();
        Map<String, Set<String>> allSplitClusters = new HashMap<>();
        Set<String> cliqueCluster = new HashSet<>();
        Map<String, Map<String, Set<String>>> fileRsfIn = new HashMap<>(fileRsfMaker.getAllFileRelations());

        for (String relName : unusedRelations)
        {
            fileRsfIn.remove(relName);
        }

        for (String s : clique)
        {
            cliqueCluster.add(solutionFileName2ClusterName.get(s));
        }
        // System.out.println(cliqueCluster.size());
        for (String s : clustersToBreakDown)
        {
            Map<String, Integer> clusterSpecificConnectivity = new HashMap<>();
            for (String fileName : solutionClusterName2FileName.get(s))
            {
                clusterSpecificConnectivity.put(fileName, 0);
            }
            perClusterCliqueConnectivity.put(s, clusterSpecificConnectivity);
        }

        for (String cluster : clustersToBreakDown)
        {
            for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
            {
                for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
                {
                    for (String s : entityToEntities.getValue())
                    {
                        if (solutionFileName2ClusterName.containsKey(s)
                            && solutionFileName2ClusterName.containsKey(entityToEntities.getKey()))
                        {
                            if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                                && cliqueCluster.contains(solutionFileName2ClusterName.get(s))
                                && solutionFileName2ClusterName.get(entityToEntities.getKey()).equals(cluster))
                            {
                                perClusterCliqueConnectivity.get(cluster).put(entityToEntities.getKey(),
                                        perClusterCliqueConnectivity.get(cluster).get(entityToEntities.getKey()) + 1);
                            }
                            else if (!solutionFileName2ClusterName.get(s).equals(
                                    solutionFileName2ClusterName.get(entityToEntities.getKey()))
                                     && cliqueCluster.contains(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                                     && solutionFileName2ClusterName.get(s).equals(cluster))
                            {
                                perClusterCliqueConnectivity.get(cluster).put(s, perClusterCliqueConnectivity.get(cluster).get(s) + 1);
                            }
                        }
                    }
                }
            }
            perClusterCliqueConnectivity.put(cluster, sortByComparator(perClusterCliqueConnectivity.get(cluster)));
        }

        // rearrange part?
        Map<String, Map<String, Set<String>>> clusterToSmallerClusters = new HashMap<>();
        int clusterPartitions = 0;
        for (Entry<String, Map<String, Integer>> cluster : perClusterCliqueConnectivity.entrySet())
        {
            Map<String, Set<String>> splitClusters = new HashMap<>();
            clusterPartitions = Math.floorDiv(solutionClusterName2FileName.get(cluster.getKey()).size(), maxClusterSize) + 1;

            int nonZeroToCliqueConnectivities = 0;
            for (Entry<String, Integer> clusterFileToClique : cluster.getValue().entrySet())
            {
                if (clusterFileToClique.getValue() > 0)
                {
                    nonZeroToCliqueConnectivities++;
                }
            }
            nonZeroToCliqueConnectivities = Math.floorDiv(nonZeroToCliqueConnectivities, clusterPartitions) + 1;
            int i = 0;
            Set<String> totalUsed = new HashSet<>();
            String currentClusterKey = "";
            for (Entry<String, Integer> clusterFileToCluster : cluster.getValue().entrySet())
            {
                if (i == 0)
                {
                    currentClusterKey = clusterFileToCluster.getKey();
                    splitClusters.put(currentClusterKey, new HashSet<>());
                    splitClusters.get(currentClusterKey).add(currentClusterKey);
                    i = (i + 1) % nonZeroToCliqueConnectivities;
                }
                else
                {
                    splitClusters.get(currentClusterKey).add(clusterFileToCluster.getKey());
                    i = (i + 1) % nonZeroToCliqueConnectivities;
                }
            }

            allSplitClusters.putAll(splitClusters);

            clusterToSmallerClusters.put(cluster.getKey(), splitClusters);
            solutionClusterName2FileName.get(cluster.getKey()).removeAll(totalUsed);
        }

        // Merging Part
        // Initialization of orphansToClustersConnections
        Map<String, Map<String, Integer>> orphansToClustersConnections = new HashMap<>();
        for (String clusterName : clustersToBreakDown)
        {
            for (String orphanName : solutionClusterName2FileName.get(clusterName))
            {
                orphansToClustersConnections.put(orphanName, new HashMap<>());
                for (String splitCluster : clusterToSmallerClusters.get(clusterName).keySet())
                {
                    orphansToClustersConnections.get(orphanName).put(splitCluster, 0);
                }
            }
        }

        // Calculation of Connections to parts of new Cluster
        for (String clusterName : clustersToBreakDown)
        {
            for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
            {
                for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
                {
                    for (String s : entityToEntities.getValue())
                    {
                        for (Entry<String, Set<String>> splitCluster : clusterToSmallerClusters.get(clusterName).entrySet())
                        {
                            if (solutionClusterName2FileName.get(clusterName).contains(entityToEntities.getKey())
                                && splitCluster.getValue().contains(s))
                            {
                                orphansToClustersConnections.get(entityToEntities.getKey()).put(splitCluster.getKey(),
                                        orphansToClustersConnections.get(entityToEntities.getKey()).get(splitCluster.getKey()) + 1);
                            }
                            else
                            {
                                if (solutionClusterName2FileName.get(clusterName).contains(s)
                                    && splitCluster.getValue().contains(entityToEntities.getKey()))
                                {
                                    orphansToClustersConnections.get(s).put(splitCluster.getKey(),
                                            orphansToClustersConnections.get(s).get(splitCluster.getKey()) + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        Map<String, Map<String, Integer>> temp = new HashMap<>(orphansToClustersConnections);

        for (Entry<String, Map<String, Integer>> e : temp.entrySet())
        {
            orphansToClustersConnections.put(e.getKey(), sortByComparatorReverse(e.getValue()));
        }

        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-Assignment of orphans to designated Clusters-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
        for (Entry<String, Map<String, Integer>> fileConnections : orphansToClustersConnections.entrySet())
        {
            for (String targetCluster : fileConnections.getValue().keySet())
            {
                if (allSplitClusters.get(targetCluster).size() < maxClusterSize)
                {
                    allSplitClusters.get(targetCluster).add(fileConnections.getKey());
                    break;
                }
            }
        }
        return allSplitClusters;
    }

    private void printSolutionRsf(Set<String> SolutionSet, Map<String, Map<String, Set<String>>> fileRsfIn, String Bug_ID,
                                  Map<String, String> entityToId, Set<String> fileInputSet)
    {
        System.out.println(fileInputSet.size());

        Map<String, Map<String, Set<String>>> fileRsf = new HashMap<>(fileRsfIn);
        Set<String> relations = new HashSet<>();
        solutionClusterName2FileName = new HashMap<>();
        solutionFileName2ClusterName = new HashMap<>();

        bad.clear();
        bad.add("src/widgets/osd.cpp");
        bad.add("tests/testdebug.cpp");
        bad.add("tests/synchronization/testmasterslavesynchronizationjob.cpp");
        bad.add("tests/synchronization/testunionjob.cpp");

        fileRsf.remove("macrodefinition");
        fileRsf.remove("methodbelongstoclass");
        fileRsf.remove("declaredin");
        fileRsf.remove("hastype");
        fileRsf.remove("entitylocation");
        fileRsf.remove("filebelongstomodule");
        fileRsf.remove("usestype");
        fileRsf.remove("inheritsfrom");
        fileRsf.remove("classbelongstofile");
        fileRsf.remove("definedin");

        relations.add("calls");
        relations.add("sets");
        relations.add("accesses");
        relations.add("attributebelongstoclass");
        relations.add("accessibleentitybelongstofile");

        try
        {
            BufferedWriter solutionWriter = new BufferedWriter(
                    new FileWriter(new File(Paths.SOLUTION_ACDC_INPUT + Bug_ID + "_sol_final_acdc.rsf")));

            for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsf.entrySet())
            {
                for (Entry<String, Set<String>> fromEntityToEntity : completeRelation.getValue().entrySet())
                {
                    for (String toEntity : fromEntityToEntity.getValue())
                    {
                        if (SolutionSet.contains(fromEntityToEntity.getKey()) && SolutionSet.contains(toEntity) && !bad.contains(fromEntityToEntity.getKey()) && !bad.contains(toEntity))
                        {
                            solutionWriter.write(completeRelation.getKey() + " " + entityToId.get(fromEntityToEntity.getKey()) + " " + entityToId.get(toEntity) + "\n");
                        }
                    }
                }
            }

            Set<String> inFileInputSet = new HashSet<>(fileInputSet);

            for (String s : fileInputSet)
            {
                inFileInputSet.remove(s);
                for (String ss : inFileInputSet)
                {
                    for (String sss : relations)
                    {
                        solutionWriter.write(sss + " " + entityToId.get(s) + " " + entityToId.get(ss) + "\n");
                        solutionWriter.write(sss + " " + entityToId.get(ss) + " " + entityToId.get(s) + "\n");
                    }
                }
                inFileInputSet.add(s);
            }

            solutionWriter.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        ThreadTest.SolutionClustering(Bug_ID + "_sol_final_acdc.rsf");

        try
        {
            BufferedReader solutionClusterReader = new BufferedReader(new FileReader(Paths.SOLUTION_CLUSTERS + Bug_ID
                                                                                     + "_sol_final_acdc.rsf"));
            while (solutionClusterReader.ready())
            {
                String[] line = solutionClusterReader.readLine().split(" ");

                String clusterName = line[1].split("\\.")[0];

                if (!line[1].contains("orphanContainer"))
                {
                    clusterName = fileRsfMaker.getIdEntity().get(line[1].split("\\.")[0]);
                }
                else
                {
                    clusterName = "orphanContainer";
                }

                String fileName = fileRsfMaker.getIdEntity().get(line[2]);

                if (solutionClusterName2FileName.containsKey(clusterName))
                {
                    solutionClusterName2FileName.get(clusterName).add(fileName);
                }
                else
                {
                    solutionClusterName2FileName.put(clusterName, new HashSet<>());
                    solutionClusterName2FileName.get(clusterName).add(fileName);
                }

                if (!solutionFileName2ClusterName.containsKey(fileName))
                {
                    solutionFileName2ClusterName.put(fileName, clusterName);
                }
            }
            Set<String> libraryFiles = new HashSet<>();
            for (Entry<String, Set<String>> cluster : solutionClusterName2FileName.entrySet())
            {
                for (String fileName : cluster.getValue())
                {
                    if (fileName.contains(".h"))
                    {
                        libraryFiles.add(fileName);
                    }
                }
                cluster.getValue().removeAll(libraryFiles);
                libraryFiles.clear();
            }
            for (Entry<String, String> toCluster : solutionFileName2ClusterName.entrySet())
            {
                if (toCluster.getKey().contains(".h"))
                {
                    libraryFiles.add(toCluster.getKey());
                }
            }
            for (String s : libraryFiles)
            {
                solutionFileName2ClusterName.remove(s);
            }
            solutionClusterReader.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

    private Map<String, Map<String, Integer>> countInterClusterRelations(Map<String, Map<String, Set<String>>> fileRsfIn,
                                                                         Set<String> goodClusters)
    {
        Map<String, Map<String, Integer>> weights = new HashMap<>();
        Set<String> validFiles = new HashSet<>(solutionFileName2ClusterName.keySet());
        Map<String, Map<String, Set<String>>> fileRsf = new HashMap<>(fileRsfIn);

        fileRsf.remove("include");
        fileRsf.remove("macrouse");
        fileRsf.remove("macrodefinition");
        fileRsf.remove("methodbelongstoclass");
        fileRsf.remove("declaredin");
        fileRsf.remove("hastype");
        fileRsf.remove("entitylocation");
        fileRsf.remove("filebelongstomodule");
        fileRsf.remove("usestype");
        fileRsf.remove("accessibleentitybelongstofile");
        fileRsf.remove("inheritsfrom");
        fileRsf.remove("classbelongstofile");
        fileRsf.remove("definedin");
        fileRsf.remove("attributebelongstoclass");

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsf.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String toEntity : entityToEntities.getValue())
                {
                    if (validFiles.contains(entityToEntities.getKey()) && validFiles.contains(toEntity))
                    {
                        if (goodClusters.contains(solutionFileName2ClusterName.get(entityToEntities.getKey())))
                        {
                            if (weights.containsKey(solutionFileName2ClusterName.get(entityToEntities.getKey())))
                            {
                                if (weights.get(solutionFileName2ClusterName.get(entityToEntities.getKey())).containsKey(solutionFileName2ClusterName.get(toEntity)))
                                {
                                    Integer cur = weights.get(solutionFileName2ClusterName.get(entityToEntities.getKey())).get(solutionFileName2ClusterName.get(toEntity));

                                    cur += 1;

                                    weights.get(solutionFileName2ClusterName.get(entityToEntities.getKey())).put(solutionFileName2ClusterName.get(toEntity), cur);
                                }
                                else
                                {
                                    weights.get(solutionFileName2ClusterName.get(entityToEntities.getKey())).put(solutionFileName2ClusterName.get(toEntity), 1);
                                }
                            }
                            else
                            {
                                weights.put(solutionFileName2ClusterName.get(entityToEntities.getKey()), new HashMap<>());
                                weights.get(solutionFileName2ClusterName.get(entityToEntities.getKey())).put(solutionFileName2ClusterName.get(toEntity), 1);
                            }
                        }
                        else if (weights.containsKey(solutionFileName2ClusterName.get(toEntity)))
                        {
                            if (weights.get(solutionFileName2ClusterName.get(toEntity)).containsKey(solutionFileName2ClusterName.get(entityToEntities.getKey())))
                            {
                                Integer cur = weights.get(solutionFileName2ClusterName.get(toEntity)).get(solutionFileName2ClusterName.get(entityToEntities.getKey()));

                                cur += 1;

                                weights.get(solutionFileName2ClusterName.get(toEntity)).put(solutionFileName2ClusterName.get(entityToEntities.getKey()), cur);
                            }
                            else
                            {
                                weights.get(solutionFileName2ClusterName.get(toEntity)).put(solutionFileName2ClusterName.get(entityToEntities.getKey()), 1);
                            }
                        }
                        else
                        {
                            weights.put(solutionFileName2ClusterName.get(toEntity), new HashMap<>());
                            weights.get(solutionFileName2ClusterName.get(toEntity)).put(solutionFileName2ClusterName.get(entityToEntities.getKey()), 1);
                        }
                    }
                }
            }
        }
        return weights;
    }

    private Set<String> selectClusters(Map<String, Map<String, Integer>> clusterConnectionWeights, Set<String> inputClusters)
    {
        Map<String, Integer> perClusterGain = new HashMap<>();
        Set<String> finalClusters = new HashSet<>();

        for (Entry<String, Map<String, Integer>> oneClusterWeight : clusterConnectionWeights.entrySet())
        {
            for (Entry<String, Integer> clusterWeights : oneClusterWeight.getValue().entrySet())
            {
                if (perClusterGain.containsKey(clusterWeights.getKey()))
                {
                    perClusterGain.put(clusterWeights.getKey(), (perClusterGain.get(clusterWeights.getKey()) + clusterWeights.getValue()));
                }
                else
                {
                    perClusterGain.put(clusterWeights.getKey(), clusterWeights.getValue());
                }
            }
        }

        int sum = 0;

        double average;

        for (Entry<String, Integer> clusterWeight : perClusterGain.entrySet())
        {
            sum += clusterWeight.getValue();
        }

        average = sum / (double) perClusterGain.size();

        for (Entry<String, Integer> clusterEntry : perClusterGain.entrySet())
        {
            if (clusterEntry.getValue() < average)
            {
                finalClusters.add(clusterEntry.getKey());
            }
        }

        finalClusters.addAll(inputClusters);

        return finalClusters;
    }

    private Set<String> selectMostConnected(Map<String, Map<String, Set<String>>> fileRsfIn, Set<String> candidateForConnection)
    {
        Set<String> mostConnectedFiles = new HashSet<>();
        Map<String, Integer> fileConnectivity = new HashMap<>();
        Set<String> finalVi = new HashSet<>();

        for (String s : candidateForConnection)
        {
            if (!s.endsWith(".h"))
            {
                finalVi.add(s);
            }
        }

        System.out.println("Starting with " + candidateForConnection.size() + " files, after removing .h I am left with " + finalVi.size() + " files");

        for (String fileName : finalVi)
        {
            fileConnectivity.put(fileName, 0);
        }

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> fromEntityToEntities : completeRelation.getValue().entrySet())
            {
                for (String toEntity : fromEntityToEntities.getValue())
                {
                    if (finalVi.contains(fromEntityToEntities.getKey()))
                    {
                        fileConnectivity.put(fromEntityToEntities.getKey(), fileConnectivity.get(fromEntityToEntities.getKey()) + 1);
                    }
                    if (finalVi.contains(toEntity))
                    {
                        fileConnectivity.put(toEntity, fileConnectivity.get(toEntity) + 1);
                    }
                }
            }
        }

        fileConnectivity = sortByComparator(fileConnectivity);

        int i = fileConnectivity.size();

        for (Entry<String, Integer> forfile : fileConnectivity.entrySet())
        {
            //HAHAHAHAHAH WHO WROTE THIS?!?!?
            i--;

            if (i <= fileConnectivity.size() * 0.1 && i <= 20)
            {
                mostConnectedFiles.add(forfile.getKey());
            }
        }
        return mostConnectedFiles;
    }

    private void toFromCliqueConnectivity(Map<String, Map<String, Set<String>>> fileRsfIn, Set<String> clique, Set<String> GSClusters)
    {
        for (String s : unusedRelations)
        {
            fileRsfIn.remove(s);
        }
        Set<String> cliqueCluster = new HashSet<>();
        for (String s : clique)
        {
            cliqueCluster.add(solutionFileName2ClusterName.get(s));
        }

        // ClusterToCliqueConnectivity
        Map<String, Integer> cliqueClusterConnectivity = new HashMap<>();
        for (String clusterName : solutionClusterName2FileName.keySet())
        {
            cliqueClusterConnectivity.put(clusterName, 0);
        }
        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String s : entityToEntities.getValue())
                {
                    if (solutionFileName2ClusterName.containsKey(s) && solutionFileName2ClusterName.containsKey(entityToEntities.getKey()))
                    {
                        if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                            && cliqueCluster.contains(solutionFileName2ClusterName.get(s)))
                        {
                            cliqueClusterConnectivity.put(solutionFileName2ClusterName.get(entityToEntities.getKey()),
                                    cliqueClusterConnectivity.get(solutionFileName2ClusterName.get(entityToEntities.getKey())) + 1);
                            cliqueClusterConnectivity.put(solutionFileName2ClusterName.get(s),
                                    cliqueClusterConnectivity.get(solutionFileName2ClusterName.get(s)) + 1);

                        }
                        else
                        {
                            if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                                && cliqueCluster.contains(solutionFileName2ClusterName.get(entityToEntities.getKey())))
                            {
                                cliqueClusterConnectivity.put(solutionFileName2ClusterName.get(s),
                                        cliqueClusterConnectivity.get(solutionFileName2ClusterName.get(s)) + 1);
                                cliqueClusterConnectivity.put(solutionFileName2ClusterName.get(entityToEntities.getKey()),
                                        cliqueClusterConnectivity.get(solutionFileName2ClusterName.get(entityToEntities.getKey())) + 1);
                            }
                        }
                    }
                }
            }
        }
        averageToCliqueCon.clear();

        for (Entry<String, Integer> cluster : cliqueClusterConnectivity.entrySet())
        {
            averageToCliqueCon.put(cluster.getKey(), cluster.getValue() / (double) solutionClusterName2FileName.get(cluster.getKey()).size());
        }

        // ClusterConnectivity
        Map<String, Integer> clusterConnectivity = new HashMap<>();

        for (String clusterName : solutionClusterName2FileName.keySet())
        {
            clusterConnectivity.put(clusterName, 0);
        }

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String s : entityToEntities.getValue())
                {
                    if (solutionFileName2ClusterName.containsKey(s) && solutionFileName2ClusterName.containsKey(entityToEntities.getKey()))
                    {
                        clusterConnectivity.put(solutionFileName2ClusterName.get(entityToEntities.getKey()),
                                clusterConnectivity.get(solutionFileName2ClusterName.get(entityToEntities.getKey())) + 1);
                        clusterConnectivity.put(solutionFileName2ClusterName.get(s),
                                clusterConnectivity.get(solutionFileName2ClusterName.get(s)) + 1);

                    }
                }
            }
        }

        averagePerFileCon.clear();

        for (Entry<String, Integer> cluster : clusterConnectivity.entrySet())
        {
            averagePerFileCon.put(cluster.getKey(), cluster.getValue() / (double) solutionClusterName2FileName.get(cluster.getKey()).size());
        }

        // FileToCliqueConnectivity
        fileToCliqueClusterConnectivity = new HashMap<>();
        for (String fileName : solutionFileName2ClusterName.keySet())
        {
            fileToCliqueClusterConnectivity.put(fileName, 0.0);
        }
        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String s : entityToEntities.getValue())
                {
                    if (solutionFileName2ClusterName.containsKey(s) && solutionFileName2ClusterName.containsKey(entityToEntities.getKey()))
                    {
                        if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                            && cliqueCluster.contains(solutionFileName2ClusterName.get(s)))
                        {
                            fileToCliqueClusterConnectivity.put(entityToEntities.getKey(),
                                    fileToCliqueClusterConnectivity.get(entityToEntities.getKey()) + 1);
                        }
                        else if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                                 && cliqueCluster.contains(solutionFileName2ClusterName.get(entityToEntities.getKey())))
                        {
                            fileToCliqueClusterConnectivity.put(s, fileToCliqueClusterConnectivity.get(s) + 1);
                        }
                    }

                }
            }
        }
        fileToCliqueClusterConnectivity = sortByComparatorDouble(fileToCliqueClusterConnectivity);

        for (String fileName : solutionFileName2ClusterName.keySet())
        {
            fileToCliqueConnectivity.put(fileName, 0.0);
        }

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                for (String s : entityToEntities.getValue())
                {
                    if (solutionFileName2ClusterName.containsKey(s) && solutionFileName2ClusterName.containsKey(entityToEntities.getKey()))
                    {
                        if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                            && clique.contains(s))
                        {
                            fileToCliqueConnectivity.put(entityToEntities.getKey(),
                                    fileToCliqueConnectivity.get(entityToEntities.getKey()) + 1);
                        }
                        else if (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey()))
                                 && clique.contains(entityToEntities.getKey()))
                        {
                            fileToCliqueConnectivity.put(s, fileToCliqueConnectivity.get(s) + 1);
                        }
                    }

                }
            }
        }
        fileToCliqueConnectivity = sortByComparatorDouble(fileToCliqueConnectivity);
    }

    private Set<String> SelectInGSValues(Map<String, Map<String, Set<String>>> fileRsfIn, Set<String> GSClusters)
    {
        // Removes UnusedRelations
        for (String s : unusedRelations)
        {
            fileRsfIn.remove(s);
        }

        Set<String> mostConnectedFiles = new HashSet<>();

        int sum = 0;

        // double average;
        Map<String, Integer> fileConnectivity = new HashMap<>();

        for (String fileName : finalV)
        {
            fileConnectivity.put(fileName, 0);
        }

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn.entrySet())
        {
            for (Entry<String, Set<String>> fromEntityToEntities : completeRelation.getValue().entrySet())
            {
                for (String toEntity : fromEntityToEntities.getValue())
                {
                    if (finalV.contains(fromEntityToEntities.getKey()))
                    {
                        if (completeRelation.getKey().equals("calls"))
                        {
                            fileConnectivity.put(fromEntityToEntities.getKey(), fileConnectivity.get(fromEntityToEntities.getKey()) + 1);
                        }
                        if (completeRelation.getKey().equals("sets"))
                        {
                            fileConnectivity.put(fromEntityToEntities.getKey(), fileConnectivity.get(fromEntityToEntities.getKey()) + 1);
                        }
                        if (completeRelation.getKey().equals("accesses"))
                        {
                            fileConnectivity.put(fromEntityToEntities.getKey(), fileConnectivity.get(fromEntityToEntities.getKey()) + 1);
                        }
                    }
                    if (finalV.contains(toEntity))
                    {
                        if (completeRelation.getKey().equals("calls"))
                        {
                            fileConnectivity.put(toEntity, fileConnectivity.get(toEntity) + 1);
                        }
                        if (completeRelation.getKey().equals("sets"))
                        {
                            fileConnectivity.put(toEntity, fileConnectivity.get(toEntity) + 1);
                        }
                        if (completeRelation.getKey().equals("accesses"))
                        {
                            fileConnectivity.put(toEntity, fileConnectivity.get(toEntity) + 1);
                        }
                    }
                }
            }
        }
        for (Entry<String, Integer> forfile : fileConnectivity.entrySet())
        {
            sum += forfile.getValue();
        }

        fileConnectivity = sortByComparator(fileConnectivity);

        List<Map.Entry<String, Integer>> list = new LinkedList<>(fileConnectivity.entrySet());

        sum = 0;

        for (Entry<String, Integer> forfile : fileConnectivity.entrySet())
        {
            if (GSClusters.contains(solutionFileName2ClusterName.get(forfile.getKey())))
            {
                if (solutionFileName2ClusterName.get(forfile.getKey()) != null && !forfile.getKey().contains(".h"))
                {
                    sum += forfile.getValue();
                }
            }
        }
        Set<String> selectors = new HashSet<>();

        for (Entry<String, Integer> entry : list)
        {
            if (list.indexOf(entry) < 0.3 * list.size())
            {
                selectors.add(entry.getKey());
            }
        }
        printSumOfFileConnectivityInGsClusters(fileConnectivity);
        for (String s : selectors)
        {
            if (solutionFileName2ClusterName.containsKey(s))
            {
                mostConnectedFiles.addAll(solutionClusterName2FileName.get(solutionFileName2ClusterName.get(s)));
            }
        }
        return mostConnectedFiles;
    }

    private void printSumOfFileConnectivityInGsClusters(Map<String, Integer> fileConnectivity)
    {
        Set<String> gsClusters = new HashSet<>();
        Map<String, Integer> clusterConnectivitySum = new HashMap<>();

        for (String gsToken : answer)
        {
            gsClusters.add(solutionFileName2ClusterName.get(gsToken));
        }
        for (String s : solutionClusterName2FileName.keySet())
        {
            clusterConnectivitySum.put(s, 0);
        }

        for (Entry<String, Integer> file : fileConnectivity.entrySet())
        {
            if (solutionFileName2ClusterName.containsKey(file.getKey()))
            {
                clusterConnectivitySum.put(solutionFileName2ClusterName.get(file.getKey()),
                        clusterConnectivitySum.get(solutionFileName2ClusterName.get(file.getKey())) + file.getValue());
            }
        }

        clusterConnectivitySum = sortByComparator(clusterConnectivitySum);

        averagePerFileCon.clear();

        for (Entry<String, Integer> cluster : clusterConnectivitySum.entrySet())
        {
            averagePerFileCon
                    .put(cluster.getKey(), cluster.getValue() / (double) solutionClusterName2FileName.get(cluster.getKey()).size());
        }
    }

    private Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap)
    {
        // Convert Map to List
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        list.sort(Comparator.comparing(o -> (o.getValue())));

        // Convert sorted map back to a Map
        Map<String, Integer> sortedMap = new LinkedHashMap<>();

        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private Map<String, Integer> sortByComparatorReverse(Map<String, Integer> unsortMap)
    {

        // Convert Map to List
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        list.sort((o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

        // Convert sorted map back to a Map
        Map<String, Integer> sortedMap = new LinkedHashMap<>();

        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private Map<String, Double> sortByComparatorDouble(Map<String, Double> unsortMap)
    {

        // Convert Map to List
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        list.sort(Comparator.comparing(o -> (o.getValue())));

        // Convert sorted map back to a Map
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Clears answer, input, and lsa token sets
     */
    private void clearSets()
    {
        answer.clear();
        lsaTokens.clear();
        inputTokens.clear();
    }

    private Set<String> expandeTokenSet(Set<String> tokenSetForExpansion)
    {
        Set<String> localAnswer = new HashSet<>();
        Set<String> toAddToVisited = new HashSet<>();
        Set<String> toRemoveFromAnswer = new HashSet<>();

        // prepare allRelsLocal Map containing all relations both forward and backwards
        Map<String, Map<String, Set<String>>> allRelsLocal = new HashMap<>(allFileRelationsReversed);

        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfMaker.getAllFileRelations().entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                if (allRelsLocal.get(completeRelation.getKey()).containsKey(entityToEntities.getKey()))
                {
                    allRelsLocal.get(completeRelation.getKey()).get(entityToEntities.getKey()).addAll(entityToEntities.getValue());
                }
                else
                {
                    allRelsLocal.get(completeRelation.getKey()).put(entityToEntities.getKey(), entityToEntities.getValue());
                }

            }
        }

        // Eliminate from the map all the Uncommented Relations Usually all of them except "sets", "accesses" and "calls"
        allRelsLocal.remove("include");
        allRelsLocal.remove("macrouse");
        allRelsLocal.remove("macrodefinition");
        allRelsLocal.remove("methodbelongstoclass");
        allRelsLocal.remove("declaredin");
        allRelsLocal.remove("hastype");
        allRelsLocal.remove("entitylocation");
        allRelsLocal.remove("filebelongstomodule");
        allRelsLocal.remove("usestype");
        allRelsLocal.remove("accessibleentitybelongstofile");
        allRelsLocal.remove("inheritsfrom");
        allRelsLocal.remove("classbelongstofile");
        allRelsLocal.remove("definedin");
        allRelsLocal.remove("attributebelongstoclass");

        // Copy the answer Set to a localAnswer set
        for (String s : answer)
        {
            if (!s.contains(".h"))
            {
                localAnswer.add(s);
            }
        }
        answer.retainAll(localAnswer);

        // Initialize the search frontier for the DFS expansion algorithm
        Set<String> nextFrontier = new HashSet<>(fileLsaTokens);
        Set<String> currentFrontier = new HashSet<>(nextFrontier);

        nextFrontier.clear();
        localAnswer.removeAll(fileLsaTokens);

        Set<String> Visited = new HashSet<>(currentFrontier);

        int step = 0;

        while (!currentFrontier.isEmpty() && step < 1)
        {
            for (Entry<String, Map<String, Set<String>>> completeRelation : allRelsLocal.entrySet())
            {
                for (String s : currentFrontier)
                {
                    if (completeRelation.getValue().containsKey(s))
                    {
                        for (String target : completeRelation.getValue().get(s))
                        {
                            if (!Visited.contains(target) && !currentFrontier.contains(target))
                            {
                                nextFrontier.add(target);
                            }
                            if (localAnswer.contains(target))
                            {
                                toRemoveFromAnswer.add(target);
                            }
                            toAddToVisited.add(target);
                        }
                    }
                }
            }

            localAnswer.removeAll(toRemoveFromAnswer);

            toRemoveFromAnswer.clear();

            step++;

            currentFrontier.clear();

            currentFrontier.addAll(nextFrontier);

            nextFrontier.clear();

            Visited.addAll(toAddToVisited);

            toAddToVisited.clear();
        }

        System.out.println("Total expansion Steps = " + step);
        return Visited;
    }
}
