package mchorse.aperture.utils;

/**
 * Color class
 * 
 * This class represents RGB color. 
 */
public class Color
{
    public float red;
    public float green;
    public float blue;

    public static Color from8bit(byte red, byte green, byte blue)
    {
        return new Color(red / 255F, green / 255F, blue / 255F);
    }

    public static Color fromHex(int hex)
    {
        int red = (hex >> 16) & 255;
        int green = (hex >> 8) & 255;
        int blue = hex & 255;

        return new Color(red / 255F, green / 255F, blue / 255F);
    }

    public Color(float red, float green, float blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getHex()
    {
        int hex = 0;

        hex += (int) (blue * 256);
        hex += (int) (green * 256) << 8;
        hex += (int) (red * 256) << 16;

        return hex;
    }
}