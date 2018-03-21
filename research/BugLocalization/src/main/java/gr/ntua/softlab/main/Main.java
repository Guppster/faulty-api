package gr.ntua.softlab.main;

import gr.ntua.softlab.filepaths.Paths;
import gr.ntua.softlab.makefilersf.FileRsfMaker;
import gr.ntua.softlab.solutionspace.InputReader;
import gr.ntua.softlab.solutionspaceclustering.ThreadTest;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Main
{
    //Constants
    private static final int MAX_CLUSTER_SIZE_CONSTRAINT = 5;
    private static final double RANKING_SELECTION_MODIFIER = 0.15;

    private final Map<String, String> fileNames2Cluster = new HashMap<>();
    private final Map<String, Set<String>> clusterName2FileName = new HashMap<>();
    private final Set<String> unusedRelations = new HashSet<>();
    private final Set<String> bad = new HashSet<>();
    private final Map<String, Double> averagePerFileCon = new HashMap<>();
    private final Map<String, Double> averageToCliqueCon = new HashMap<>();
    private final Set<String> finalV = new HashSet<>();
    private String productName;
    private FileRsfMaker fileRsfMaker;
    private InputReader inputReader;
    private Set<String> answer = new HashSet<>();
    private Set<String> lsaTokens = new HashSet<>();
    private Set<String> inputTokens = new HashSet<>();
    private Set<String> lsaTokenFiles = new HashSet<>();
    private Map<String, Map<String, Set<String>>> allFileRelationsReversed;
    private Map<String, Double> fileToCliqueClusterConnectivity = new HashMap<>();
    private Map<String, Double> fileToCliqueConnectivity = new HashMap<>();
    private String currentFileName = "";
    private Map<String, Set<String>> solutionClusterName2FileName;
    private Map<String, String> solutionFileName2ClusterName;
    private Set<String> inputTokenFiles;

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
            System.out.println("--------------------------------------");
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

        answer = inputReader.getAnswers();

        //answer is now the intersect of answer and fileRsfMaker entity names
        //Making sure the tokens are an rsf entity
        answer.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());

        lsaTokens = inputReader.getLsaTokens();

        //lsaTokens is now the intersect of lsaTokens and fileRsfMaker entity names
        //Making sure the tokens are an rsf entity
        lsaTokens.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());

        inputTokens = inputReader.getInputTokens();

        //inputTokens is now the intersect of inputTokens and fileRsfMaker entity names
        //Making sure the tokens are an rsf entity
        inputTokens.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());

        //Move on to the current file
        currentFileName = inputReader.getFilename();

        //Generate a different view into the RSF structure
        reverseAllFileRelations();

        //Prepare resources for processing
        gatherSolutionFiles();

        performRanking();

        //Indicate that there may be more bug reports to process
        return true;
    }

    /**
     * This method takes a Map of <Relationship Name, <Entity Key, Entities related to key Entity>>
     * and transforms it into <Relationship Name, <Entity value, Entity keys pointing to this value>>
     * (in)Effectively reversing the inner-map
     */
    private void reverseAllFileRelations()
    {
        allFileRelationsReversed = new HashMap<>();

        //For each RSF relation (relation name, relationship map)
        for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfMaker.getAllFileRelations().entrySet())
        {
            //For each entity relationship map (key entity is related to all value entities)
            for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet())
            {
                //For each entity's value (each entity that is related to the current one)
                for (String targetEntity : entityToEntities.getValue())
                {
                    //Find if the relationship exists in the method's return hashmap
                    if (allFileRelationsReversed.containsKey(completeRelation.getKey()))
                    {
                        //If it is, that means we have encountered this relationship before
                        if (allFileRelationsReversed.get(completeRelation.getKey()).containsKey(targetEntity))
                        {
                            allFileRelationsReversed.get(completeRelation.getKey()).get(targetEntity).add(entityToEntities.getKey());
                        }
                        else
                        {
                            Set<String> entityMap = new HashSet<>();
                            entityMap.add(entityToEntities.getKey());

                            allFileRelationsReversed.get(completeRelation.getKey()).put(targetEntity, entityMap);
                        }
                    }
                    else
                    {
                        //If the relation is not found in the method's return hashmap, we need to create it
                        Map<String, Set<String>> relationship = new HashMap<>();
                        Set<String> entityMap = new HashSet<>();

                        entityMap.add(entityToEntities.getKey());
                        relationship.put(targetEntity, entityMap);

                        allFileRelationsReversed.put(completeRelation.getKey(), relationship);
                    }
                }
            }
        }
    }

    /**
     * Gathers a map of all the files required in the processing of the solution
     */
    private void gatherSolutionFiles()
    {
        lsaTokenFiles = new HashSet<>();
        inputTokenFiles = new HashSet<>();

        Map<String, Set<String>> entityToFilenames = fileRsfMaker.getEntityNameToFileNames();
        Map<String, Map<String, Set<String>>> allRelations = fileRsfMaker.getAllRelations();

        // make lsaTokenFiles
        for (String lsaToken : lsaTokens)
        {
            if (entityToFilenames.containsKey(lsaToken))
            {
                lsaTokenFiles.addAll(entityToFilenames.get(lsaToken));
            }

            lsaTokenFiles.retainAll(allRelations.get("filebelongstomodule").keySet());
        }

        // Make inputTokenFiles and remove the GoldStandard
        for (String inputToken : inputTokens)
        {
            inputTokenFiles.addAll(entityToFilenames.get(inputToken));
            inputTokenFiles.retainAll(allRelations.get("filebelongstomodule").keySet());
        }
    }

    /**
     * Print the filename in two parts
     */
    @SuppressWarnings("unused")
    private void makeFilename2Parts()
    {
        for (String filenameString : fileNames2Cluster.keySet())
        {
            Arrays.stream(filenameString.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
                    .forEach(System.out::println);

            System.out.println("----------------------------------------------------------");
        }
    }

    private void performRanking()
    {
        //TODO: Remove terrible use of global variable
        finalV.addAll(expandTokenSet(lsaTokenFiles));

        //Stores names of all files belonging to this module
        Set<String> files = new HashSet<>(fileRsfMaker.getAllRelations().get("filebelongstomodule").keySet());

        //Numebr of files that don't contain .h suffix
        double numberOfNonHeaderFiles = files.stream()
                .filter(s -> !s.contains(".h"))
                .count();

        System.out.println("Total System Files = " + files.size());
        System.out.println("Total non .h Files = " + numberOfNonHeaderFiles);

        //Same count as before but this time done on the final expanded set
        double nonLibraryFilesInExpansion = finalV
                .stream()
                .filter(s -> !s.contains(".h"))
                .count();

        System.out.println("Total non .h Files from Expansion= " + nonLibraryFilesInExpansion);

        //Some magic algorithm selection here
        if (nonLibraryFilesInExpansion < (RANKING_SELECTION_MODIFIER * numberOfNonHeaderFiles))
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
        //Backup the files?
        Set<String> Visited = finalV.stream().filter(s -> !s.contains(".h")).collect(Collectors.toSet());

        //Clear it?
        finalV.clear();

        //Add them back?
        finalV.addAll(Visited);

        //Get all relationships from rsf
        Map<String, Map<String, Set<String>>> relationships = fileRsfMaker.getAllFileRelations();

        //Remove unused relationships from set of all file relationships
        unusedRelations.forEach(relationships::remove);

        Set<String> clique = new HashSet<>(selectMostConnected(fileRsfMaker.getAllFileRelations(), finalV));

        System.out.println("clique size = " + clique.size());

        finalV.forEach(fileName -> fileToCliqueConnectivity.put(fileName, 0.0));

        //For each entity value, in each entitySet, in each relationship
        relationships.forEach(
                (relationshipName, entityMap) -> entityMap.forEach(
                        (entity, entitySet) -> entitySet.forEach(
                                entityValue ->
                                {
                                    //If the final set contains the entity value as a key add 1 to its value
                                    if (finalV.contains(entityValue))
                                    {
                                        fileToCliqueConnectivity.put(entityValue, fileToCliqueConnectivity.get(entityValue) + 1);
                                    }
                                    //If the final set contains the entity key as a key add 1 to its value
                                    else if (finalV.contains(entity))
                                    {
                                        fileToCliqueConnectivity.put(entity, fileToCliqueConnectivity.get(entity) + 1);
                                    }
                                })));

        //Sort by weight
        fileToCliqueConnectivity = sortByComparatorDouble(fileToCliqueConnectivity);

        Map<String, Double> treeMap = new TreeMap<>(Comparator.reverseOrder());

        treeMap.putAll(fileToCliqueConnectivity);

        //Outputting results to file and standard out
        try
        {
            //TODO: Get rid of hardcoded directories
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
        Set<String> GSClusters;
        Set<String> bad = new HashSet<>();
        Set<String> Visited = new HashSet<>(finalV);
        Set<String> hits = new HashSet<>(finalV);

        Map<String, Double> clusterScored = new HashMap<>();
        double maxClusterSize = 0.0;

        Map<String, Set<String>> top50PercentileClusters = new HashMap<>();
        double max = 0;

        Map<String, Double> clusterExpectation = new HashMap<>();
        double meanExpectation = 0.0;

        // Recall before applying anything but the expansion step and without removing the .h files
        System.out.println("LsaFileTokens at start = " + lsaTokenFiles.size());
        System.out.println("LsaFileTokens after expansion " + finalV.size());

        hits.retainAll(answer);

        System.out.println("Recall (Pre reduction)= " + hits.size() / (double) answer.size());

        inputTokenFiles.removeAll(bad);

        Set<String> clique = new HashSet<>(selectMostConnected(fileRsfMaker.getAllFileRelations(), finalV));

        System.out.println("***********************PERFORMING CLUSTERING***********************");

        printSolutionRsf(finalV, fileRsfMaker.getAllFileRelations(), inputReader.getBugNumber(), fileRsfMaker.getEntityId(), clique);

        // remove .h from answer
        hits.addAll(answer);

        hits.stream()
                .filter(filename -> filename.contains(".h"))
                .forEach(filename -> answer.remove(filename));

        hits.clear();

        GSClusters = answer.stream()
                .map(fileName -> solutionFileName2ClusterName.get(fileName))
                .collect(Collectors.toSet());

        // remove .h from visited
        hits.addAll(Visited);

        hits.stream()
                .filter(filename -> filename.contains(".h"))
                .forEach(Visited::remove);

        //Refresh main set
        finalV.clear();
        finalV.addAll(Visited);

        // Cluster size calculation
        for (Entry<String, Set<String>> cluster : solutionClusterName2FileName.entrySet())
        {
            if (cluster.getValue().size() > MAX_CLUSTER_SIZE_CONSTRAINT)
            {
                clustersToBreakDown.add(cluster.getKey());
            }
        }

        // Final results calculation
        toFromCliqueConnectivity(fileRsfMaker.getAllFileRelations(), clique, GSClusters);

        for (Entry<String, Set<String>> e : solutionClusterName2FileName.entrySet())
        {
            if (maxClusterSize < e.getValue().size())
            {
                maxClusterSize = e.getValue().size();
            }
        }

        // MetricHasBeenChanged
        for (Entry<String, Double> cluster : averagePerFileCon.entrySet())
        {
            if (solutionClusterName2FileName.get(cluster.getKey()).size() != 0)
            {
                clusterScored.put(
                        cluster.getKey(),
                        (cluster.getValue() / averageToCliqueCon.get(cluster.getKey()))
                        * solutionClusterName2FileName.get(cluster.getKey()).size());
            }
        }

        clusterScored = sortByComparatorDouble(clusterScored);

        List<Map.Entry<String, Double>> list = new LinkedList<>(clusterScored.entrySet());

        for (Entry<String, Double> cluster : clusterScored.entrySet())
        {
            if (max < cluster.getValue())
            {
                max = cluster.getValue();
            }
        }

        // Expectation Calculation
        for (Entry<String, Double> cluster : clusterScored.entrySet())
        {
            clusterExpectation.put(
                    cluster.getKey(),
                    cluster.getValue() * (solutionClusterName2FileName.get(cluster.getKey()).size() / (double) solutionFileName2ClusterName.size()));

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
                    if (list.indexOf(cluster) > 0.5 * list.size() && solutionClusterName2FileName.get(cluster.getKey()).size() < 0.5 * maxClusterSize)
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
                    if (list.indexOf(cluster) > 0.5 * list.size() && solutionClusterName2FileName.get(cluster.getKey()).size() < 0.5 * maxClusterSize)
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
                    if (list.indexOf(cluster) > 0.5 * list.size() && solutionClusterName2FileName.get(cluster.getKey()).size() >= 0.5 * maxClusterSize)
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
                        && solutionClusterName2FileName.get(cluster.getKey()).size() >= 0.5 * maxClusterSize)
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

    /**
     * @param tokenSetForExpansion
     * @return
     */
    private Set<String> expandTokenSet(Set<String> tokenSetForExpansion)
    {
        Set<String> localAnswer;
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
        localAnswer = answer.stream()
                .filter(filename -> !filename.contains(".h"))
                .collect(Collectors.toSet());

        answer.retainAll(localAnswer);

        // Initialize the search frontier for the DFS expansion algorithm
        Set<String> nextFrontier = new HashSet<>(tokenSetForExpansion);
        Set<String> currentFrontier = new HashSet<>(nextFrontier);

        nextFrontier.clear();
        localAnswer.removeAll(tokenSetForExpansion);

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
