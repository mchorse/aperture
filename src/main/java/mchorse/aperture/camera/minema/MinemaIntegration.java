package mchorse.aperture.camera.minema;

import info.ata4.minecraft.minema.Minema;
import net.minecraftforge.fml.common.Loader;

public class MinemaIntegration
{
	public static boolean isLoaded()
	{
		return Loader.isModLoaded(Minema.MODID);
	}
}