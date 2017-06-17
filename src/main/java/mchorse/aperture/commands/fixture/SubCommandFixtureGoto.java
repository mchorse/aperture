package mchorse.aperture.commands.fixture;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.Position;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera goto
 *
 * Teleports player to specific camera fixture with specified progress.
 */
public class SubCommandFixtureGoto extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "goto";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.goto";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        EntityPlayer player = (EntityPlayer) sender;
        CameraProfile profile = ClientProxy.control.currentProfile;
        Position pos = new Position(player);

        int index = CommandBase.parseInt(args[0]);
        float progress = 0;

        if (args.length > 1)
        {
            progress = (float) CommandBase.parseDouble(args[1], 0, 1);
        }

        if (!profile.has(index))
        {
            L10n.error(sender, "profile.not_exists", index);
            return;
        }

        profile.get(index).applyFixture(progress, 0, pos);
        pos.apply(player);
        ClientProxy.control.setRollAndFOV(pos.angle.roll, pos.angle.fov);
    }
}
