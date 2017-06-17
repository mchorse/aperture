package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/**
 * Camera's sub-command /camera list
 *
 * This sub-command is responsible for listing all camera profile loaded. 
 */
public class SubCommandCameraList extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "list";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.list";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CameraControl control = ClientProxy.control;

        /* TODO: extract strings */
        if (control.profiles.isEmpty())
        {
            sender.addChatMessage(new TextComponentString("No camera profiles"));
        }

        String text = "";

        for (CameraProfile profile : control.profiles)
        {
            boolean current = profile == control.currentProfile;

            text += "- " + (current ? "§7" : "") + profile.toString() + "§r\n";
        }

        sender.addChatMessage(new TextComponentString(text.trim()));
    }
}