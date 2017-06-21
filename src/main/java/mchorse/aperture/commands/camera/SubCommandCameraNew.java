package mchorse.aperture.commands.camera;

import java.util.List;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 * Camera's sub-command /camera new
 *
 * This command is responsible for replacing current camera profile with a new
 * blank camera profile.
 */
public class SubCommandCameraNew extends CommandBase
{
    @Override
    public String getName()
    {
        return "new";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.new";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        String filename = args[1];
        boolean isServer = args[0].equals("server");

        ClientProxy.control.addProfile(new CameraProfile(isServer ? new ServerDestination(filename) : new ClientDestination(filename)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "client", "server");
        }

        return super.getTabCompletions(server, sender, args, pos);
    }
}