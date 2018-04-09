package com.buglocalization.acdc;
/*
 * Created on Jul 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author V. Tzerpos
 * <p>
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
class IO
{
    private static int debug_level;

    static void put(String message, int level)
    {
        if (level <= debug_level) System.out.println(message);
    }

    static void set_debug_level(int level)
    {
        debug_level = level;
    }
}
