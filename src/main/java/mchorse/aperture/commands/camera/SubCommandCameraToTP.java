package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SubCommandCameraToTP extends CommandBase
{
	@Override
	public String getName()
	{
		return "to_tp";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		CameraProfile profile = ClientProxy.control.currentProfile;

		if (profile == null)
		{
			sender.sendMessage(new TextComponentString("No camera profile selected..."));
			return;
		}

		Position position = new Position();
		Position first = new Position();
		String tps = "";

		double rx = 0;
		double ry = 0;
		double rz = 0;

		if (args.length >= 3)
		{
			rx = CommandBase.parseDouble(args[0]);
			ry = CommandBase.parseDouble(args[1]);
			rz = CommandBase.parseDouble(args[2]);
		}

		profile.applyProfile(0, 0, first);

		for (int i = 0, c = (int) profile.getDuration(); i < c; i++)
		{
			profile.applyProfile(i, 0, position);

			if (args.length >= 3)
			{
				position.point.set(rx + (position.point.x - first.point.x), ry + (position.point.y - first.point.y), rz + (position.point.z - first.point.z));
			}

			tps += "tp @s[scores={var=" + (i + 1) + "}] " + position.point.x + " " + position.point.y + " " + position.point.z + " " + position.angle.yaw + " " + position.angle.pitch + "\n";
		}

		GuiScreen.setClipboardString(tps);
		sender.sendMessage(new TextComponentString("Copied the thing to your clipboard!"));
	}
}
