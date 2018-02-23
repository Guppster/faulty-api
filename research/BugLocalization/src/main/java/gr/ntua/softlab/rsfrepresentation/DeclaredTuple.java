/**
 * 
 */
package gr.ntua.softlab.rsfrepresentation;

/**
 * @author or10n
 *
 */
public class DeclaredTuple {
	String file;
	String content;
		DeclaredTuple(String file, String content){
			this.file = file;
			this.content = content;
		}
		
		String getFile(){
			return file;
		}
		
		String getContent(){
			return content;
		}
}
