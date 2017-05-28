package mchorse.aperture.commands;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.commands.camera.SubCommandCameraClear;
import mchorse.aperture.commands.camera.SubCommandCameraDefault;
import mchorse.aperture.commands.camera.SubCommandCameraFOV;
import mchorse.aperture.commands.camera.SubCommandCameraLoad;
import mchorse.aperture.commands.camera.SubCommandCameraNew;
import mchorse.aperture.commands.camera.SubCommandCameraRoll;
import mchorse.aperture.commands.camera.SubCommandCameraRotate;
import mchorse.aperture.commands.camera.SubCommandCameraSave;
import mchorse.aperture.commands.camera.SubCommandCameraStart;
import mchorse.aperture.commands.camera.SubCommandCameraStep;
import mchorse.aperture.commands.camera.SubCommandCameraStop;
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
    private static CameraProfile profile;
    private static CameraControl control = new CameraControl();

    public static CameraProfile getProfile()
    {
        return profile;
    }

    public static CameraControl getControl()
    {
        return control;
    }

    public static void setProfile(CameraProfile profile)
    {
        CommandCamera.profile = profile;
        CommandCamera.control.profile = profile;
        ClientProxy.profileRunner.setProfile(profile);
        ClientProxy.profileRenderer.setProfile(profile);
    }

    public static void reset()
    {
        setProfile(new CameraProfile(""));
    }

    static
    {
        reset();
    }

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

        /* Camera control */
        this.add(new SubCommandCameraStep());
        this.add(new SubCommandCameraRotate());
        this.add(new SubCommandCameraRoll());
        this.add(new SubCommandCameraFOV());
        this.add(new SubCommandCameraDefault());

        /* Fixture editing */
        this.add(new SubCommandFixtureAdd());
        this.add(new SubCommandFixtureEdit());
        this.add(new SubCommandFixtureMove());
        this.add(new SubCommandFixtureRemove());
        this.add(new SubCommandFixtureDuration());
        this.add(new SubCommandFixturePath());
        this.add(new SubCommandFixtureGoto());
    }

    @Override
    public String getCommandName()
    {
        return "camera";
    }

    @Override
    protected String getHelp()
    {
        return "blockbuster.commands.camera.help";
    }
}