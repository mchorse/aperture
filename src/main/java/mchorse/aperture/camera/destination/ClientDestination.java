package mchorse.aperture.camera.destination;

import java.io.File;

import org.apache.commons.io.FileUtils;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraUtils;
import mchorse.aperture.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Client destination
 * 
 * Saves and reloads camera prfoile from Aperture's config folder.
 */
public class ClientDestination extends AbstractDestination
{
    public ClientDestination(String filename)
    {
        super(filename);
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj) && obj instanceof ClientDestination;
    }

    @Override
    public void save(CameraProfile profile)
    {
        try
        {
            FileUtils.write(new File(ClientProxy.getClientCameras(), this.filename + ".json"), CameraUtils.toJSON(profile));

            L10n.success(Minecraft.getMinecraft().thePlayer, "profile.client_save", this.filename);
        }
        catch (Exception e)
        {
            L10n.error(Minecraft.getMinecraft().thePlayer, "profile.client_cant_save", this.filename);
        }
    }

    @Override
    public void load()
    {
        try
        {
            String json = FileUtils.readFileToString(new File(ClientProxy.getClientCameras(), this.filename + ".json"));
            CameraProfile newProfile = CameraUtils.cameraJSONBuilder(false).fromJson(json, CameraProfile.class);

            newProfile.setDestination(this);
            newProfile.dirty = false;

            ClientProxy.control.addProfile(newProfile);

            L10n.success(Minecraft.getMinecraft().thePlayer, "profile.client_load", this.filename);
        }
        catch (Exception e)
        {
            L10n.error(Minecraft.getMinecraft().thePlayer, "profile.client_cant_load", this.filename);
        }
    }

    @Override
    public ResourceLocation toResourceLocation()
    {
        return new ResourceLocation("client", this.filename);
    }
}