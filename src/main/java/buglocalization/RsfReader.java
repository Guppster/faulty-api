/**
 *
 */
package buglocalization;

import java.io.*;

/**
 * @author or10n RsfReader has a private BufferedReader which reads from the file passed as argument when instanciating the Class
 */
public class RsfReader
{
    private BufferedReader bufferedReader = null;

    public RsfReader(File file)
    {
        try
        {
            bufferedReader = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException ioe)
        {
            // System.out.println(ioe.getMessage());
            System.out.println("Error File NotFound");
        }
    }

    /**
     * returns one parsed Line per call
     *
     * @return[0] = relation
     * @return[1] = Entity1 (from) name
     * @return[2] = Entity1 (from) id
     * @return[3] = Entity2 (to) name
     * @return[4] = Entity2 (to) id
     */
    public String[] readLine()
    {
        String relation = "";
        String[] initial_tokens;
        String[] nameId1;
        String[] nameId2;
        try
        {
            do
            {
                String line = bufferedReader.readLine().toLowerCase();
                initial_tokens = line.split("\t");
                relation = initial_tokens[0];
            } while ((relation.equals("visibility") || relation.equals("signature")) && readerReady());
            nameId1 = initial_tokens[1].split("#");
            nameId2 = initial_tokens[2].split("#");
            nameId1[0] = nameId1[0].substring(1, nameId1[0].length() - 1);
            nameId2[0] = nameId2[0].substring(1, nameId2[0].length() - 1);
            return new String[]{relation, nameId1[0], nameId1[1], nameId2[0], nameId2[1]};
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            return null;
        }
    }

    /**
     * Check if contained reader is ready for reading
     *
     * @return true if ready false if not ready or if exception was thrown trying to assess the status
     */
    public boolean readerReady()
    {
        try
        {
            return bufferedReader.ready();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            return false;
        }
    }

    /**
     * closes the reader and prints a message to standard output if an exception is thrown
     */
    public void close()
    {
        try
        {
            bufferedReader.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
    }

}
