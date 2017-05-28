package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.utils.L10n;
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
    public String getCommandName()
    {
        return "stop";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.stop";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        ClientProxy.profileRunner.stop();
        L10n.info(sender, "profile.stop");
    }
}