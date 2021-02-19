package mchorse.aperture.commands.camera;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.mclib.commands.McCommandBase;
import mchorse.mclib.commands.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera stop
 *
 * This sub-command is responsible for stopping current running camera profile.
 */
public class SubCommandCameraStop extends McCommandBase
{
    @Override
    public String getName()
    {
        return "stop";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.stop";
    }

    @Override
    public String getSyntax()
    {
        return "{l}{6}/{r}camera {8}stop{r}";
    }

    @Override
    public L10n getL10n()
    {
        return Aperture.l10n;
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!ClientProxy.canUseCameraEditor())
        {
            return;
        }

        ClientProxy.runner.stop();
        Aperture.l10n.info(sender, "profile.stop");
    }
}