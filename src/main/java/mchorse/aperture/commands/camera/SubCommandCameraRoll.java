package mchorse.aperture.commands.camera;

import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.utils.L10n;
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
    public String getCommandName()
    {
        return "roll";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.roll";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CameraControl control = CommandCamera.getControl();

        if (args.length == 0)
        {
            L10n.info(sender, "camera.roll", control.roll);
        }
        else
        {
            CommandCamera.getControl().setRoll((float) CommandBase.parseDouble(args[0]));
        }
    }
}