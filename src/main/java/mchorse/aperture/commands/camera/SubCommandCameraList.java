package mchorse.aperture.commands.camera;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.camera.destination.ServerDestination;
import mchorse.aperture.utils.L10n;
import net.minecraft.client.resources.I18n;
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
    public String getName()
    {
        return "list";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.list";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CameraControl control = ClientProxy.control;

        if (control.profiles.isEmpty())
        {
            L10n.info(sender, "no_loaded_profiles");

            return;
        }

        String text = "";

        for (CameraProfile profile : control.profiles)
        {
            boolean current = profile == control.currentProfile;
            AbstractDestination dest = profile.getDestination();

            String side = I18n.format("aperture.misc." + (dest instanceof ServerDestination ? "server" : "client"));
            String desc = I18n.format("aperture.misc.profile_info" + (current ? "_current" : ""), side, dest.getFilename(), profile.getDuration(), profile.getCount());

            text += "- " + desc + "§r\n";
        }

        sender.sendMessage(new TextComponentString(text.trim()));
    }
}