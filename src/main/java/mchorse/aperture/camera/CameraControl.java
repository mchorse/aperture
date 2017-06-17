package mchorse.aperture.camera;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
     * Currently stored camera profiles 
     */
    public List<CameraProfile> profiles = new ArrayList<CameraProfile>();

    /**
     * Currently rendered/editing camera profile
     */
    public CameraProfile currentProfile;

    /**
     * Roll of the camera
     */
    public float roll = 0;

    /**
     * Reset camera profiles 
     */
    public void reset()
    {
        this.profiles.clear();
        this.currentProfile = null;
    }

    public void addProfile(CameraProfile profile)
    {
        this.profiles.add(profile);
        this.currentProfile = profile;
    }

    /**
     * Add roll (it can be negative too)
     */
    public void setRoll(float value)
    {
        ClientProxy.renderer.roll.reset(value);
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
}