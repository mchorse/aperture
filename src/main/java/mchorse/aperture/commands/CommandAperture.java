package mchorse.aperture.commands;

import mchorse.aperture.camera.CameraAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

public class CommandAperture extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "aperture";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.aperture.help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        if (args[0].equals("play"))
        {
            if (args.length <= 2)
            {
                CameraAPI.playCameraProfile(getCommandSenderAsPlayer(sender), new ResourceLocation(args[1]));
            }
            else
            {
                CameraAPI.playCameraProfile(EntitySelector.matchOnePlayer(sender, args[1]), new ResourceLocation(args[2]));
            }
        }
    }
}