package gr.ntua.softlab.makefilersf;

import gr.ntua.softlab.cdifutilities.CdifRepresentation;
import gr.ntua.softlab.filepaths.Paths;
import gr.ntua.softlab.rsfReader.RsfReader;
import gr.ntua.softlab.rsfrepresentation.RsfRepresentation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FileRsfMaker
{
    private final String productName;
    private RsfRepresentation rsfRepresentation;
    private Map<String, String> entityNameToFileName;
    private Map<String, Map<String, Set<String>>> allRelations;
    private Map<String, Map<String, Set<String>>> allFileRelations;
    private Map<String, Set<String>> entityToFiles;

    public FileRsfMaker(String productName)
    {
        this.productName = productName;
    }

    @SuppressWarnings("all")
    private void makefileRsf()
    {
        makeEntityToId();
        cleanfileToId();
        writeFileRsf();
    }

    public void doLift()
    {
        makefileRsf();
    }

    private void cleanfileToId()
    {
        Set<String> files = new HashSet<>(rsfRepresentation.getFileIds());
    }

    public Map<String, String> getEntityId()
    {
        return rsfRepresentation.getEntityToId();
    }

    public Map<String, String> getIdEntity()
    {
        return rsfRepresentation.getIdToEntityName();
    }

    private void writeFileRsf()
    {
        // I have to do something useful ...
        // for each entity in the rsf substitute with the file or files which are related to the specific entity... should I first populate
        // a map containing a mapping from each entity to the set of files which contain it???
        // the answer is probably strongly yes..
        allRelations = new HashMap<>();
        allRelations.putAll(rsfRepresentation.getRelationEntityEntitiesMap());
        entityToFiles = new HashMap<>();
        Map<String, Set<String>> DeclaredIn = allRelations.get("DeclaredIn".toLowerCase());
        Map<String, Set<String>> DefinedIn = allRelations.get("DefinedIn".toLowerCase());
        for (Entry<String, String> entityToFile : entityNameToFileName.entrySet())
        {
            entityToFiles.put(entityToFile.getKey(), initializeSet(entityToFile.getValue()));
        }
        for (Entry<String, Set<String>> methodToFiles : DeclaredIn.entrySet())
        {
            if (entityToFiles.containsKey(methodToFiles.getKey()))
            {
                entityToFiles.get(methodToFiles.getKey()).addAll(methodToFiles.getValue());
            }
            else
            {
                entityToFiles.put(methodToFiles.getKey(), methodToFiles.getValue());
            }
        }
        for (Entry<String, Set<String>> methodToFiles : DefinedIn.entrySet())
        {
            if (entityToFiles.containsKey(methodToFiles.getKey()))
            {
                entityToFiles.get(methodToFiles.getKey()).addAll(methodToFiles.getValue());
            }
            else
            {
                entityToFiles.put(methodToFiles.getKey(), methodToFiles.getValue());
            }
        }
        try
        {
            BufferedWriter rsfWriter = new BufferedWriter(new FileWriter(new File(Paths.ACDC_INPUT_PATH + productName
                                                                                  + "_file_final_acdc.rsf")));
            for (Entry<String, Map<String, Set<String>>> oneCompleteRelation : allRelations.entrySet())
            {
                for (Entry<String, Set<String>> entityToEntities : oneCompleteRelation.getValue().entrySet())
                {
                    if (entityToFiles.containsKey(entityToEntities.getKey()))
                    {
                        for (String entity : entityToEntities.getValue())
                        {
                            if (entityToFiles.containsKey(entity))
                            {
                                for (String fromFile : entityToFiles.get(entityToEntities.getKey()))
                                {
                                    for (String toFile : entityToFiles.get(entity))
                                    {
                                        if (!fromFile.contains("tests/testdebug.cpp") && !fromFile.contains("src/core/support/debug.h")
                                            && !toFile.contains("tests/testdebug.cpp") && !toFile.contains("src/core/support/debug.h"))
                                        {
                                            rsfWriter.write(oneCompleteRelation.getKey() + " " + rsfRepresentation.getId(fromFile) + " "
                                                            + rsfRepresentation.getId(toFile) + "\n");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            rsfWriter.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

    private void makeAllFileRelations()
    {
        allRelations = new HashMap<>();
        allRelations.putAll(rsfRepresentation.getRelationEntityEntitiesMap());
        entityToFiles = new HashMap<>();
        Set<String> allMethodKeys = new HashSet<>();
        allMethodKeys.addAll(allRelations.get("DeclaredIn".toLowerCase()).keySet());
        allMethodKeys.addAll(allRelations.get("DefinedIn".toLowerCase()).keySet());
        Map<String, Set<String>> DeclaredIn = allRelations.get("DeclaredIn".toLowerCase());
        Map<String, Set<String>> DefinedIn = allRelations.get("DefinedIn".toLowerCase());
        for (Entry<String, String> entityToFile : entityNameToFileName.entrySet())
        {
            if (!allMethodKeys.contains(entityToFile.getKey()))
            {
                entityToFiles.put(entityToFile.getKey(), initializeSet(entityToFile.getValue()));
            }
        }
        for (Entry<String, Set<String>> methodToFiles : DefinedIn.entrySet())
        {
            allMethodKeys.remove(methodToFiles.getKey());
            if (entityToFiles.containsKey(methodToFiles.getKey()))
            {
                entityToFiles.get(methodToFiles.getKey()).addAll(methodToFiles.getValue());
            }
            else
            {
                entityToFiles.put(methodToFiles.getKey(), methodToFiles.getValue());
            }
        }
        for (Entry<String, Set<String>> methodToFiles : DeclaredIn.entrySet())
        {
            if (allMethodKeys.contains(methodToFiles.getKey()))
            {
                if (entityToFiles.containsKey(methodToFiles.getKey()))
                {
                    entityToFiles.get(methodToFiles.getKey()).addAll(methodToFiles.getValue());
                }
                else
                {
                    entityToFiles.put(methodToFiles.getKey(), methodToFiles.getValue());
                }
            }
        }
        allFileRelations = new HashMap<>();
        allFileRelations.put("filebelongstomodule", allRelations.get("filebelongstomodule"));
        allFileRelations.put("modulebelongstomodule", allRelations.get("modulebelongstomodule"));
        for (Entry<String, Map<String, Set<String>>> completeRelations : allRelations.entrySet())
        {
            for (Entry<String, Set<String>> entityToEntities : completeRelations.getValue().entrySet())
            {
                for (String targetEntity : entityToEntities.getValue())
                {
                    // if (entityToEntities.getKey().contains("sqlmeta.cpp")) {
                    // System.out.println("from " + entityToEntities.getKey() + "\tto\t" + targetEntity);
                    // }
                    if (entityToFiles.containsKey(entityToEntities.getKey()) && entityToFiles.containsKey(targetEntity))
                    {
                        for (String fromFile : entityToFiles.get(entityToEntities.getKey()))
                        {
                            for (String toFile : entityToFiles.get(targetEntity))
                            {
                                if (!fromFile.equals(toFile))
                                {
                                    if (allFileRelations.containsKey(completeRelations.getKey()))
                                    {
                                        if (allFileRelations.get(completeRelations.getKey()).containsKey(fromFile))
                                        {
                                            allFileRelations.get(completeRelations.getKey()).get(fromFile).add(toFile);
                                        }
                                        else
                                        {
                                            allFileRelations.get(completeRelations.getKey()).put(fromFile, initializeSet(toFile));
                                        }
                                    }
                                    else
                                    {
                                        allFileRelations.put(completeRelations.getKey(), initializeMap(fromFile, toFile));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DEBUG
    // int same = 0;
    // int different = 0;
    // for (Entry<String, Map<String, Set<String>>> completeRelation : allFileRelations.entrySet()) {
    // for (Entry<String, Set<String>> entityToEntities : completeRelation.getValue().entrySet()) {
    // for (String targetEntity : entityToEntities.getValue()) {
    // if (targetEntity.contains(".cpp") || entityToEntities.getKey().contains(".cpp"))
    // same++;
    // else
    // different++;
    // }
    // }
    // }
    // System.out.println(allFileRelations.keySet());
    // System.out.println("interCpp = " + same + " | " + "interh = " + different);

    public Map<String, Map<String, Set<String>>> getAllFileRelations()
    {
        if (allFileRelations == null)
        {
            makeAllFileRelations();
        }
        return allFileRelations;
    }

    private void makeEntityToId()
    {
        rsfRepresentation = makeRsfRepresentation(productName);

        CdifRepresentation cdifRepresentation = new CdifRepresentation(productName, makeIdsToEntityNames(rsfRepresentation),
                makeFileNames(rsfRepresentation));

        entityNameToFileName = cdifRepresentation.readFile();
    }

    private RsfRepresentation makeRsfRepresentation(String productName)
    {
        File rsfReaderFile = new File(Paths.RSF_FOLDER + productName + "_final.rsf");
        RsfReader rsfReader = new RsfReader(rsfReaderFile);
        rsfRepresentation = new RsfRepresentation();
        while (rsfReader.readerReady())
        {
            rsfRepresentation.insert(rsfReader.readLine());
        }
        rsfReader.close();
        return rsfRepresentation;
    }

    private Map<String, String> makeIdsToEntityNames(RsfRepresentation rsfRepresentation)
    {
        return new HashMap<>(rsfRepresentation.getIdToEntityName());
    }

    private Set<String> makeFileNames(RsfRepresentation rsfRepresentation)
    {
        Set<String> fileNames = new HashSet<>();
        Set<String> fileIds = rsfRepresentation.getFileIds();
        for (String s : fileIds)
        {
            fileNames.add(rsfRepresentation.getName(s));
        }
        return fileNames;
    }

    // @SuppressWarnings("unused")
    private Set<String> initializeSet(String firstElement)
    {
        Set<String> toReturn = new HashSet<>();
        toReturn.add(firstElement);
        return toReturn;
    }

    public Map<String, Map<String, Set<String>>> getAllRelations()
    {
        return allRelations;
    }

    public Map<String, Set<String>> getEntityNameToFileNames()
    {
        return entityToFiles;
    }

    private Map<String, Set<String>> initializeMap(String key, String Value)
    {
        Map<String, Set<String>> toReturn = new HashMap<>();
        toReturn.put(key, initializeSet(Value));
        return toReturn;
    }

    public RsfRepresentation getRsfRepresentation()
    {
        return rsfRepresentation;
    }
}
