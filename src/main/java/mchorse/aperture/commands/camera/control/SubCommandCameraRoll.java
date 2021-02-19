package mchorse.aperture.commands.camera.control;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.mclib.commands.McCommandBase;
import mchorse.mclib.commands.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera roll
 *
 * This command is responsible for setting and getting roll of this client.
 */
public class SubCommandCameraRoll extends McCommandBase
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
    public String getSyntax()
    {
        return "{l}{6}/{r}camera {8}roll{r} {7}[roll]{r}";
    }

    @Override
    public L10n getL10n()
    {
        return Aperture.l10n;
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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