package mchorse.aperture.commands;

import java.util.List;

import mchorse.aperture.camera.CameraAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CommandAperture extends CommandBase
{
    @Override
    public String getName()
    {
        return "aperture";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.aperture.help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 2 && args.length >= 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        if (args[0].equals("play"))
        {
            if (args.length <= 2)
            {
                CameraAPI.playCameraProfile(getCommandSenderAsPlayer(sender), new ResourceLocation(args[1]));
            }
            else
            {
                CameraAPI.playCameraProfile(getPlayer(server, sender, args[2]), new ResourceLocation(args[1]));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "play");
        }
        else if (args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return super.getTabCompletions(server, sender, args, pos);
    }
}