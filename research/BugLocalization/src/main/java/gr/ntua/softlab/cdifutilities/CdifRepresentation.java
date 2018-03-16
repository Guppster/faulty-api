package gr.ntua.softlab.cdifutilities;

import gr.ntua.softlab.filepaths.Paths;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CdifRepresentation{

	private BufferedReader cdifReader;
	private final Map<String, String> idToEntity = new HashMap<>();
	private final Set<String> fileNames = new HashSet<>();
	private final Map<String, String> entityIdtoFileId = new HashMap<>();

	public CdifRepresentation(String productName, Map<String, String> idToEntity, Set<String> fileNames){
		this.idToEntity.putAll(idToEntity);
		this.fileNames.addAll(fileNames);
		try {
			cdifReader = new BufferedReader(new FileReader(Paths.CDIF_FOLDER + productName + ".cdif"));
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());
		}
	}

	private String[] getEntityFileName(){
		String id = "";
		String containerName = "";
		String[] result = null;
		try {
			while (cdifReader.ready() && id.isEmpty()) {
				String line = cdifReader.readLine();
				Matcher m = Pattern.compile("\\bFM\\d*\\b").matcher(line);
				if (m.find()) {
					id = line.substring(m.start() + 2, m.end());
				}
			}
			while (cdifReader.ready() && containerName.isEmpty() && containerName != ")") {
				String line = cdifReader.readLine();
				if (line.contains("(sourceAnchor ")) {
					containerName = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
				} else {
					if (line.equals(")"))
						containerName = ")";
				}
			}
			if (containerName.contains(")"))
				result = null;
			else
				result = new String[] { id, containerName.toLowerCase() };
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		return result;
	}

	public Map<String, String> readFile(){
		String[] toInsert;
		try {
			while (cdifReader.ready()) {
				toInsert = getEntityFileName();
				if (toInsert != null) {
					// System.out.println(toInsert[0] + "->" + entityToId.get(toInsert[1]));
					entityIdtoFileId.put(idToEntity.get(toInsert[0]), toInsert[1]);
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		for (String fileName : fileNames) {
			entityIdtoFileId.put(fileName, fileName);
		}
		// System.out.println("Total ID->Redirects Caught : " + entityIdtoFileId.size());
		return entityIdtoFileId;
	}
}
