package mchorse.aperture.commands;

import mchorse.aperture.camera.CameraAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);

        if (args[0].equals("play"))
        {
            CameraAPI.playCameraProfile(player, new ResourceLocation(args[1]));
        }
    }
}