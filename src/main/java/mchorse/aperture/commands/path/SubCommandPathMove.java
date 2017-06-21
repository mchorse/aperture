package mchorse.aperture.commands.path;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

/**
 * Path's sub-command /camera path move
 *
 * This sub-command is responsible for moving a point to another index in
 * a path fixture.
 */
public class SubCommandPathMove extends CommandBase
{
    @Override
    public String getName()
    {
        return "move";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.path.move";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 3)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        CameraProfile profile = ClientProxy.control.currentProfile;
        int index = CommandBase.parseInt(args[0], 0);
        int from = CommandBase.parseInt(args[1]);
        int to = CommandBase.parseInt(args[2]);

        if (!profile.has(index))
        {
            L10n.error(sender, "profile.not_exists", args[0]);
            return;
        }

        AbstractFixture fixture = profile.get(index);

        if (!(fixture instanceof PathFixture))
        {
            L10n.error(sender, "profile.not_path", index);
            return;
        }

        PathFixture path = (PathFixture) fixture;

        if (!path.hasPoint(from) || !path.hasPoint(to))
        {
            L10n.error(sender, "profile.move_no_path_point", index, from, to);
            return;
        }

        path.movePoint(from, to);
        profile.dirty();
    }
}