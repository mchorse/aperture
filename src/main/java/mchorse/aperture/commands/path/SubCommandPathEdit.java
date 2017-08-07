package mchorse.aperture.commands.path;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Path's sub-command /camera path edit
 *
 * This sub-command is responsible for editing a point in a path fixture.
 */
public class SubCommandPathEdit extends CommandBase
{
    @Override
    public String getName()
    {
        return "edit";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.path.edit";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        CameraProfile profile = ClientProxy.control.currentProfile;
        int index = CommandBase.parseInt(args[0], 0);
        int point = CommandBase.parseInt(args[1]);

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

        if (!path.hasPoint(point))
        {
            L10n.error(sender, "profile.no_path_point", index, point);
            return;
        }

        path.editPoint(new DurablePosition((EntityPlayer) sender), point);
        profile.dirty();
    }
}