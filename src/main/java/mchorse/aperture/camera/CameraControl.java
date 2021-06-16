package mchorse.aperture.camera;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.destination.AbstractDestination;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiProfilesManager;
import mchorse.mclib.utils.EntityUtils;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

/**
 * Camera control class
 *
 * This class is responsible for controlling the camera profile. The actions
 * are delegated from keyboard handler.
 */
@SideOnly(Side.CLIENT)
public class CameraControl
{
    /**
     * Currently rendered/editing camera profile
     */
    public CameraProfile currentProfile;

    private boolean prevRollMode;
    private float prevRoll;

    /**
     * Roll of the camera
     */
    public float roll = 0;

    public int lastCounter;
    public float lastRoll;
    public float lastFov;
    public float lastGamma;
    public int lastThirdPersonView;
    public GameType lastGameMode = GameType.NOT_SET;

    public void cache()
    {
        if (this.lastCounter == 0)
        {
            Minecraft mc = Minecraft.getMinecraft();

            this.lastGameMode = EntityUtils.getGameMode();
            this.lastRoll = roll;
            this.lastFov = mc.gameSettings.fovSetting;
            this.lastGamma = mc.gameSettings.gammaSetting;
            this.lastThirdPersonView = mc.gameSettings.thirdPersonView;
        }

        this.lastCounter ++;
    }

    public void restore()
    {
        this.lastCounter --;

        if (this.lastCounter == 0)
        {
            Minecraft mc = Minecraft.getMinecraft();

            this.roll = this.lastRoll;
            mc.gameSettings.fovSetting = this.lastFov;
            mc.gameSettings.gammaSetting = this.lastGamma;
            mc.gameSettings.thirdPersonView = this.lastThirdPersonView;

            if (this.lastGameMode != EntityUtils.getGameMode())
            {
                mc.player.sendChatMessage("/gamemode " + this.lastGameMode.getID());
            }

            this.lastRoll = this.lastFov = this.lastGamma = 0F;
        }
    }

    /**
     * Reset camera profiles 
     */
    public void reset()
    {
        /* Saving dirty camera profiles */
        if (Aperture.profileAutoSave.get())
        {
            GuiCameraEditor cameraEditor = ClientProxy.cameraEditor;

            if (cameraEditor != null)
            {
                this.saveCameraProfiles(cameraEditor);
            }
        }

        this.currentProfile = null;
        this.lastCounter = 0;
        this.lastRoll = this.lastFov = 0;
        this.lastGameMode = null;
    }

    private void saveCameraProfiles(GuiCameraEditor editor)
    {
        GuiProfilesManager manager = editor.profiles;

        for (CameraProfile profile : manager.profiles.list.getList())
        {
            if (profile.dirty)
            {
                profile.save();
            }
        }
    }

    public CameraProfile getProfile(AbstractDestination destination)
    {
        GuiCameraEditor editor = ClientProxy.cameraEditor;
        GuiProfilesManager manager = editor.profiles;

        for (CameraProfile profile : manager.profiles.list.getList())
        {
            if (Objects.equals(profile.destination, destination))
            {
                return profile;
            }
        }

        return null;
    }

    /**
     * Add roll (it can be negative too)
     */
    public void setRoll(float value)
    {
        this.prevRollMode = false;

        ClientProxy.renderer.roll.reset(value);
        this.roll = value;
    }

    /**
     * Add roll (it can be negative too)
     */
    public void setRoll(float prevValue, float value)
    {
        this.prevRollMode = true;

        ClientProxy.renderer.roll.reset(value);
        this.prevRoll = prevValue;
        this.roll = value;
    }

    /**
     * Reset roll (set it back to 0)
     */
    public void resetRoll()
    {
        this.setRoll(0.0F);
    }

    /**
     * Set FOV
     */
    public void setFOV(float value)
    {
        ClientProxy.renderer.fov.reset(value);
        Minecraft.getMinecraft().gameSettings.fovSetting = value;
    }

    /**
     * Reset FOV to default value
     */
    public void resetFOV()
    {
        this.setFOV(70.0F);
    }

    /**
     * Set both roll and FOV at the same time
     */
    public void setRollAndFOV(float roll, float fov)
    {
        this.setRoll(roll);
        this.setFOV(fov);
    }

    public float getRoll(float partialTicks)
    {
        return this.prevRollMode ? Interpolations.lerp(this.prevRoll, this.roll, partialTicks) : this.roll;
    }
}