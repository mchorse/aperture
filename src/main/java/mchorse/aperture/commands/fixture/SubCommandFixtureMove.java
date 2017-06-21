package mchorse.aperture.commands.fixture;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera move
 *
 * This sub-command is responsible for moving camera fixture at passed index
 * to another place in camera profile.
 */
public class SubCommandFixtureMove extends CommandBase
{
    @Override
    public String getName()
    {
        return "move";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.fixture.move";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        CameraProfile profile = ClientProxy.control.currentProfile;
        int from = CommandBase.parseInt(args[0]);
        int to = CommandBase.parseInt(args[1]);

        if (profile.has(from) && profile.has(to))
        {
            profile.move(from, to);
        }
        else
        {
            L10n.error(sender, "profile.cant_move", args[0], args[1]);
        }
    }
}