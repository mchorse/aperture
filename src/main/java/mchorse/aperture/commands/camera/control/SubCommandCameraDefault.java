package mchorse.aperture.commands.camera.control;

import mchorse.aperture.ClientProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Sub-command /camera default
 *
 * This sub-command resets camera's Field-Of-View and roll to default values
 * (70.0 degrees and 0.0 degress).
 */
public class SubCommandCameraDefault extends CommandBase
{
    @Override
    public String getName()
    {
        return "default";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.default";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        ClientProxy.control.resetRoll();
        ClientProxy.control.resetFOV();
    }
}