package mchorse.aperture.camera.curves;

/**
 * Abstract rendering curve
 * 
 * Rendering curves can modify some rendering parameters.
 * For some reason, it's not a StructureBase.
 */
public abstract class AbstractCurve
{
    public abstract String getTranslatedName();

    public abstract void apply(double value);
    
    public abstract void reset();
    
    public String convertTranslateKey(String str)
    {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);

            if (c >= 'A' && c <= 'Z')
            {
                c += 'a' - 'A';
                
                if (i > 0)
                {
                    builder.append('_');
                }
            }
            
            builder.append(c);
        }
        
        return builder.toString();
    }
}
