package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera save
 *
 * This sub-command is responsible for saving the camera profile to the disk.
 * As with /camera load sub-command, this sub-command also sends message to
 * the server with request to save profile that was sent.
 */
public class SubCommandCameraSave extends CommandBase
{
    @Override
    public String getName()
    {
        return "save";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.save";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CameraProfile profile = ClientProxy.control.currentProfile;

        if (profile != null)
        {
            AbstractDestination destination = profile.getDestination();
            String filename = args.length == 0 ? destination.getFilename() : args[0];

            if (filename.isEmpty())
            {
                L10n.error(sender, "profile.empty_filename");
            }
            else
            {
                destination.setFilename(filename);

                profile.save();
            }
        }
    }
}