package com.alluvial.mlink.iress.util;

import com.alluvial.mds.common.MDSHelper;

public class Sizeof {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public static int sizeof(boolean b)
    {
            return 1;
    }

    public static int sizeof(byte b)
    {
            return 1;
    }

    public static int sizeof(char c)
    {
            return 2;
    }

    public static int sizeof(short s)
    {
            return 2;
    }

    public static int sizeof(int i)
    {
            return 4;
    }

    public static int sizeof(long l)
    {
            return 8;
    }

    public static int sizeof(float f)
    {
            return 4;
    }

    public static int sizeof(double d)
    {
            return 8;
    }
}
