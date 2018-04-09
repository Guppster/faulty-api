/**
 *
 */
package com.buglocalization;

/**
 * @author or10n
 */
class CallsTuple
{
    private final String caller;
    private final String callee;

    CallsTuple(String caller, String callee)
    {
        this.caller = caller;
        this.callee = callee;
    }

    String getCaller()
    {
        return caller;
    }

    String getCallee()
    {
        return callee;
    }
}