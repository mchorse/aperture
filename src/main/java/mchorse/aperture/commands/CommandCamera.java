package mchorse.aperture.commands;

import mchorse.aperture.Aperture;
import mchorse.aperture.commands.camera.SubCommandCameraReload;
import mchorse.aperture.commands.camera.SubCommandCameraStart;
import mchorse.aperture.commands.camera.SubCommandCameraStop;
import mchorse.aperture.commands.camera.SubCommandCameraToTP;
import mchorse.aperture.commands.camera.control.SubCommandCameraDefault;
import mchorse.aperture.commands.camera.control.SubCommandCameraFOV;
import mchorse.aperture.commands.camera.control.SubCommandCameraRoll;
import mchorse.aperture.commands.camera.control.SubCommandCameraRotate;
import mchorse.aperture.commands.camera.control.SubCommandCameraStep;
import mchorse.mclib.commands.SubCommandBase;
import mchorse.mclib.commands.utils.L10n;
import net.minecraft.command.ICommandSender;

/**
 * Command /camera
 *
 * This command is an interface to work with camera, in general, but specifically
 * this commands provides sub-commands for manipulating camera fixtures and
 * camera profiles.
 *
 * This command works on the client side, so there's no way you could use it in
 * command block, yet.
 */
public class CommandCamera extends SubCommandBase
{
    /**
     * Camera's command constructor
     *
     * This constructor is responsible for registering its sub-commands.
     */
    public CommandCamera()
    {
        /* Start/stop */
        this.add(new SubCommandCameraStart());
        this.add(new SubCommandCameraStop());
        this.add(new SubCommandCameraReload());
        this.add(new SubCommandCameraToTP());

        /* Camera control */
        this.add(new SubCommandCameraStep());
        this.add(new SubCommandCameraRotate());
        this.add(new SubCommandCameraRoll());
        this.add(new SubCommandCameraFOV());
        this.add(new SubCommandCameraDefault());
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return Aperture.commandName.get();
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.help";
    }

    @Override
    public String getSyntax()
    {
        return "";
    }

    @Override
    public L10n getL10n()
    {
        return Aperture.l10n;
    }
}