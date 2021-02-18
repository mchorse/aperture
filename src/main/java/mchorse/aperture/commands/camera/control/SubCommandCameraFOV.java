package mchorse.aperture.commands.camera.control;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera fov
 *
 * This command is responsible for setting and getting fov of this client.
 */
public class SubCommandCameraFOV extends CommandBase
{
    @Override
    public String getName()
    {
        return "fov";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.fov";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (args.length == 0)
        {
            Aperture.l10n.info(sender, "camera.fov", mc.gameSettings.fovSetting);
        }
        else
        {
            ClientProxy.control.setFOV((float) CommandBase.parseDouble(args[0]));
        }
    }
}