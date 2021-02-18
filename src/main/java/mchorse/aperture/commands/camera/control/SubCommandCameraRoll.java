package mchorse.aperture.commands.camera.control;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera roll
 *
 * This command is responsible for setting and getting roll of this client.
 */
public class SubCommandCameraRoll extends CommandBase
{
    @Override
    public String getName()
    {
        return "roll";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.roll";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CameraControl control = ClientProxy.control;

        if (args.length == 0)
        {
            Aperture.l10n.info(sender, "camera.roll", control.roll);
        }
        else
        {
            control.setRoll((float) CommandBase.parseDouble(args[0]));
        }
    }
}