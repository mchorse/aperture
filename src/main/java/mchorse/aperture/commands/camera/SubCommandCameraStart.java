package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera start
 *
 * This sub-command is responsible for starting current camera profile.
 */
public class SubCommandCameraStart extends CommandBase
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        int tick = args.length == 0 ? 0 : CommandBase.parseInt(args[0], 0);

        ClientProxy.runner.start(ClientProxy.control.currentProfile, tick);
        L10n.info(sender, "profile.start");
    }
}