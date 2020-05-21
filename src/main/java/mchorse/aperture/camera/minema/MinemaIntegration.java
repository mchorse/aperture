package mchorse.aperture.camera.minema;

import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.MinemaAPI;
import info.ata4.minecraft.minema.client.modules.video.VideoHandler;
import mchorse.aperture.Aperture;
import mchorse.mclib.client.gui.utils.GuiUtils;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class MinemaIntegration
{
	private static int available = -1;

	public static boolean isLoaded()
	{
		return Loader.isModLoaded(Minema.MODID);
	}

	public static boolean isAvailable()
	{
		if (!isLoaded())
		{
			return false;
		}

		if (available == -1)
		{
			try
			{
				Class.forName("info.ata4.minecraft.minema.MinemaAPI").getMethod("getVersion");
				available = 1;
			}
			catch (Exception e)
			{
				available = 0;
			}
		}

		return available == 1;
	}

	@Optional.Method(modid = Minema.MODID)
	public static void toggleRecording(boolean start) throws Exception
	{
		MinemaAPI.toggleRecording(start);
	}

	@Optional.Method(modid = Minema.MODID)
	public static boolean isRecording()
	{
		return MinemaAPI.isRecording();
	}

	@Optional.Method(modid = Minema.MODID)
	public static void openMovies()
	{
		GuiUtils.openWebLink(MinemaAPI.getCapturePath().toURI());
	}

	@Optional.Method(modid = Minema.MODID)
	public static String getMessage(Exception e)
	{
		return MinemaAPI.getMessage(e);
	}

	@Optional.Method(modid = Minema.MODID)
	public static void setName(String filename)
	{
		VideoHandler.customName = filename;
	}

	@Optional.Method(modid = Minema.MODID)
	public static void setEngineSpeed(float speed)
	{
		MinemaAPI.setEngineSpeed(speed);
	}
}