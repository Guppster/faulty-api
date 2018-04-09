/**
 *
 */
package com.buglocalization;

/**
 * @author or10n
 */
class DeclaredTuple
{
    private final String file;
    private final String content;

    DeclaredTuple(String file, String content)
    {
        this.file = file;
        this.content = content;
    }

    String getFile()
    {
        return file;
    }

    String getContent()
    {
        return content;
    }
}
