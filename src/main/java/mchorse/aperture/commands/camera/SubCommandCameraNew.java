package mchorse.aperture.commands.camera;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraReset;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera new
 *
 * This command is responsible for replacing current camera profile with a new
 * blank camera profile.
 */
public class SubCommandCameraNew extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "new";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.new";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        CameraProfile profile = CommandCamera.getProfile();

        profile.reset();
        profile.setFilename(args[0]);
        Dispatcher.sendToServer(new PacketCameraReset());
        L10n.info(sender, "profile.new", args[0]);
    }
}