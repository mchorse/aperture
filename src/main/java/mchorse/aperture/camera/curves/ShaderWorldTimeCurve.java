package mchorse.aperture.camera.curves;

public class ShaderWorldTimeCurve extends ShaderUniform1iCurve
{

    public ShaderWorldTimeCurve()
    {
        super("worldTime");
    }
    
    @Override
    public void apply(double value) {
        while (value < 0) value += 24.0;
        super.apply((value % 24.0) * 1000.0);
    }
    
}
