package mchorse.aperture.commands.camera;

import java.util.List;

import mchorse.aperture.camera.destination.ClientDestination;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketLoadCameraProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 * Camera's sub-command /camera load
 *
 * This sub-command is responsible for loading camera profile. It doesn't
 * actually load the profile by itself, but sends the message to the server with
 * request to load specific camera profile to the client.
 */
public class SubCommandCameraLoad extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "load";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.load";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        if (args[0].equals("server"))
        {
            Dispatcher.sendToServer(new PacketLoadCameraProfile(args[1], true));
        }
        else
        {
            new ClientDestination(args[1]).load();
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "client", "server");
        }

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}