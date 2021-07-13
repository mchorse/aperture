package mchorse.aperture.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mchorse.aperture.camera.curves.*;
import mchorse.aperture.camera.values.ValueCurves;
import mchorse.aperture.client.AsmRenderingHandler.Curve;
import mchorse.aperture.client.AsmShaderHandler;
import mchorse.aperture.utils.OptifineHelper;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;

public class CurveManager
{
    public Map<String, AbstractCurve> curves = new LinkedHashMap<String, AbstractCurve>();

    public void refreshCurves()
    {
        this.curves.clear();

        this.curves.put("brightness", new BrightnessCurve());

        for (Curve curve : Curve.values())
        {
            this.curves.put(curve.name().toLowerCase(), new VanillaAsmCurve(curve));
        }

        if (OptifineHelper.isShaderLoaded())
        {
            this.curves.put("shader_sun_path_rotation", new ShaderSunPathRotationCurve());
            this.curves.put("shader_center_depth", new ShaderCenterDepthCurve());
            this.curves.put("shader_rain_strength", new ShaderUniform1fCurve("rainStrength"));
            this.curves.put("shader_wetness", new ShaderUniform1fCurve("wetness"));
            this.curves.put("shader_frame_time", new ShaderUniform1fCurve("frameTimeCounter"));
            this.curves.put("shader_world_time", new ShaderWorldTimeCurve());
            this.curves.put("shader_in_water", new ShaderUniform1iCurve("isEyeInWater"));

            for (String id : this.getSortedOptions(AsmShaderHandler.option1f.keySet(), true))
            {
                Shaders.getShaderOption(id);
                this.curves.put("shader_" + id, new ShaderFloatOptionCurve(id));
            }

            for (String id : this.getSortedOptions(AsmShaderHandler.option1i.keySet(), true))
            {
                Shaders.getShaderOption(id);
                this.curves.put("shader_" + id, new ShaderIntegerOptionCurve(id));
            }

            for (String id : this.getSortedOptions(AsmShaderHandler.option1f.keySet(), false))
            {
                Shaders.getShaderOption(id);
                this.curves.put("shader_" + id, new ShaderFloatOptionCurve(id));
            }

            for (String id : this.getSortedOptions(AsmShaderHandler.option1i.keySet(), false))
            {
                Shaders.getShaderOption(id);
                this.curves.put("shader_" + id, new ShaderIntegerOptionCurve(id));
            }
        }
    }

    public void applyCurves(ValueCurves value, long progress, float partialTick)
    {
        float tick = progress + partialTick;

        for (String id : this.curves.keySet())
        {
            AbstractCurve curve = this.curves.get(id);
            KeyframeChannel channel = value.get(id);

            if (channel != null && !channel.isEmpty())
            {
                curve.apply(channel.interpolate(tick));
            }
            else
            {
                curve.reset();
            }
        }
    }

    public void resetAll()
    {
        for (AbstractCurve curve : this.curves.values())
        {
            curve.reset();
        }
    }

    public List<String> getSortedOptions(Collection<String> keys, boolean visible)
    {
        List<String> list = new ArrayList<String>();

        for (String key : keys)
        {
            ShaderOption option = Shaders.getShaderOption(key);

            if (option != null && option.isVisible() == visible)
            {
                list.add(key);
            }
        }

        list.sort((a, b) ->
        {
            ShaderOption optionA = Shaders.getShaderOption(a);
            ShaderOption optionB = Shaders.getShaderOption(b);

            String strA = optionA != null ? optionA.getNameText() : a;
            String strB = optionB != null ? optionB.getNameText() : b;

            return strA.compareTo(strB);
        });

        return list;
    }
}
