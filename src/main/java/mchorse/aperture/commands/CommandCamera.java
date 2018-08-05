package mchorse.aperture.commands;

import mchorse.aperture.Aperture;
import mchorse.aperture.commands.camera.SubCommandCameraClear;
import mchorse.aperture.commands.camera.SubCommandCameraClose;
import mchorse.aperture.commands.camera.SubCommandCameraList;
import mchorse.aperture.commands.camera.SubCommandCameraLoad;
import mchorse.aperture.commands.camera.SubCommandCameraNew;
import mchorse.aperture.commands.camera.SubCommandCameraSave;
import mchorse.aperture.commands.camera.SubCommandCameraStart;
import mchorse.aperture.commands.camera.SubCommandCameraStop;
import mchorse.aperture.commands.camera.control.SubCommandCameraDefault;
import mchorse.aperture.commands.camera.control.SubCommandCameraFOV;
import mchorse.aperture.commands.camera.control.SubCommandCameraRoll;
import mchorse.aperture.commands.camera.control.SubCommandCameraRotate;
import mchorse.aperture.commands.camera.control.SubCommandCameraStep;
import mchorse.aperture.commands.fixture.SubCommandFixtureAdd;
import mchorse.aperture.commands.fixture.SubCommandFixtureDuration;
import mchorse.aperture.commands.fixture.SubCommandFixtureEdit;
import mchorse.aperture.commands.fixture.SubCommandFixtureGoto;
import mchorse.aperture.commands.fixture.SubCommandFixtureMove;
import mchorse.aperture.commands.fixture.SubCommandFixturePath;
import mchorse.aperture.commands.fixture.SubCommandFixtureRemove;

/**
 * Command /camera
 *
 * This command is an interface to work with camera, in general, but specifically
 * this commands provides sub-commands for manipulating camera fixtures and
 * camera profiles.
 *
 * This command works on the client side, so there's no way you could use it in
 * command block, yet.
 *
 * @todo Create /cb-camera, which will be able to start/stop camera using
 *       command block, only if being requested.
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

        /* Load/save */
        this.add(new SubCommandCameraNew());
        this.add(new SubCommandCameraLoad());
        this.add(new SubCommandCameraSave());

        /* Profile */
        this.add(new SubCommandCameraClear());
        this.add(new SubCommandCameraList());
        this.add(new SubCommandCameraClose());

        /* Camera control */
        this.add(new SubCommandCameraStep());
        this.add(new SubCommandCameraRotate());
        this.add(new SubCommandCameraRoll());
        this.add(new SubCommandCameraFOV());
        this.add(new SubCommandCameraDefault());

        /* Fixture editing */
        this.add(new SubCommandFixtureAdd());
        this.add(new SubCommandFixtureEdit());
        this.add(new SubCommandFixtureRemove());
        this.add(new SubCommandFixtureMove());
        this.add(new SubCommandFixtureGoto());
        this.add(new SubCommandFixtureDuration());
        this.add(new SubCommandFixturePath());
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return Aperture.proxy.config.camera_command_name;
    }

    @Override
    protected String getHelp()
    {
        return "aperture.commands.camera.help";
    }
}