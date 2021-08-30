package mchorse.aperture.client;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProvider;

public class AsmRenderingHandler
{
    public static final String owner = "mchorse/aperture/client/AsmRenderingHandler";

    public static final HashMap<Curve, Double> values = new HashMap<Curve, Double>();

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.WorldTransformer}<br>
     * Called by {@link net.minecraft.world.World#getSkyColor(Entity, float)}
     */
    public static Vec3d getSkyColor(WorldProvider provider, Entity entity, float partialTicks)
    {
        Vec3d sky = provider.getSkyColor(entity, partialTicks);
        double skyR = getOption(Curve.SkyR, sky.x);
        double skyG = getOption(Curve.SkyG, sky.y);
        double skyB = getOption(Curve.SkyB, sky.z);
        return new Vec3d(skyR, skyG, skyB);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.WorldTransformer}<br>
     * Called by {@link net.minecraft.world.World#getCloudColour(float)}
     */
    public static Vec3d getCloudColor(WorldProvider provider, float partialTicks)
    {
        Vec3d cloud = provider.getCloudColor(partialTicks);
        double cloudR = getOption(Curve.CloudR, cloud.x);
        double cloudG = getOption(Curve.CloudG, cloud.y);
        double cloudB = getOption(Curve.CloudB, cloud.z);
        return new Vec3d(cloudR, cloudG, cloudB);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.WorldTransformer}<br>
     * Called by {@link net.minecraft.world.World#getFogColor(float)}
     */
    public static Vec3d getFogColor(WorldProvider provider, float celestialAngle, float partialTicks)
    {
        Vec3d fog = provider.getFogColor(celestialAngle, partialTicks);
        double fogR = getOption(Curve.FogR, fog.x);
        double fogG = getOption(Curve.FogG, fog.y);
        double fogB = getOption(Curve.FogB, fog.z);
        return new Vec3d(fogR, fogG, fogB);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.WorldTransformer}<br>
     * Called by {@link net.minecraft.world.World#getCelestialAngle(float)}
     */
    public static float getCelestialAngle(WorldProvider provider, long worldTime, float partialTicks)
    {
        double angle = getOption(Curve.CelestialAngle, provider.calculateCelestialAngle(worldTime, partialTicks) * 360.0);

        angle %= 360.0;

        if (angle < 0)
        {
            angle += 360.0;
        }

        return (float) angle / 360.0f;
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.GlStateManagerTransformer}<br>
     * Called by {@link net.minecraft.client.renderer.GlStateManager#setFogDensity(float)}
     */
    public static float getFogDensity(float density)
    {
        return (float) getOption(Curve.FogDensity, density);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.GlStateManagerTransformer}<br>
     * Called by {@link net.minecraft.client.renderer.GlStateManager#setFogStart(float)}
     */
    public static float getFogStart(float start)
    {
        return (float) getOption(Curve.FogStart, start);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.GlStateManagerTransformer}<br>
     * Called by {@link net.minecraft.client.renderer.GlStateManager#setFogEnd(float)}
     */
    public static float getFogEnd(float end)
    {
        return (float) getOption(Curve.FogEnd, end);
    }

    public static double getOption(Curve option, double defaultValue)
    {
        Double value = values.get(option);

        if (value != null)
        {
            return value;
        }
        else
        {
            return defaultValue;
        }
    }

    public static enum Curve
    {
        SkyR, SkyG, SkyB,
        CloudR, CloudG, CloudB,
        FogR, FogG, FogB,
        FogStart, FogEnd, FogDensity,
        CelestialAngle;
    }
}
