package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class SubCommandCameraReload extends CommandBase
{
	@Override
	public String getName()
	{
		return "reload";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "aperture.commands.camera.start";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		ClientProxy.cameraEditor = null;
	}
}