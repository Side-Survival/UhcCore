package com.gmail.val59000mc.utils.color;

import java.awt.*;
import java.util.Comparator;

public final class ColorComparator implements Comparator<Color> {

    @Override
    public int compare(Color c1, Color c2) {
        float[] hsb1 = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);
        if (hsb1[0] < hsb2[0])
            return -1;
        if (hsb1[0] > hsb2[0])
            return 1;
        if (hsb1[1] < hsb2[1])
            return -1;
        if (hsb1[1] > hsb2[1])
            return 1;
        if (hsb1[2] < hsb2[2])
            return -1;
        if (hsb1[2] > hsb2[2])
            return 1;
        return 0;
    }
}