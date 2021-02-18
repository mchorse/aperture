package mchorse.aperture.commands.camera;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera stop
 *
 * This sub-command is responsible for stopping current running camera profile.
 */
public class SubCommandCameraStop extends CommandBase
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!ClientProxy.canUseCameraEditor())
        {
            return;
        }

        ClientProxy.runner.stop();
        Aperture.l10n.info(sender, "profile.stop");
    }
}