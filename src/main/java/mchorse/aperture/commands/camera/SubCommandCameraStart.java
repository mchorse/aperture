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
 * Camera's sub-command /camera start
 *
 * This sub-command is responsible for starting current camera profile.
 */
public class SubCommandCameraStart extends McCommandBase
{
    @Override
    public String getName()
    {
        return "start";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.start";
    }

    @Override
    public String getSyntax()
    {
        return "{l}{6}/{r}camera {8}start{r} {7}[tick]{r}";
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

        int tick = args.length == 0 ? 0 : CommandBase.parseInt(args[0], 0);

        ClientProxy.runner.start(ClientProxy.control.currentProfile, tick);
        Aperture.l10n.info(sender, "profile.start");
    }
}