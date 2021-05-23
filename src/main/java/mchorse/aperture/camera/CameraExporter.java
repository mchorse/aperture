package mchorse.aperture.camera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.MinemaAPI;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.aperture.utils.EntitySelector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This is an exporter for the camera movement to a json file.
 * @author Christian F. (known as Chryfi)
 */
public class CameraExporter
{
    private boolean relativeOrigin = false;
    private JsonObject wrapper = new JsonObject();
    private JsonArray trackingData = new JsonArray();
    private JsonObject entityData = new JsonObject();
    private double[] trackingInitialPos = {0,0,0};
    private int heldframes = 0; //to determine double frames with minema's held frames
    private int heldframesEntity = 0; //to determine double frames with minema's held frames for entities (maybe redundant?)
    private List<Entity> entities = new ArrayList<>();

    public boolean building = false;
    public String selector; //for entities

    public void setRelativeOrigin(boolean state)
    {
        this.relativeOrigin = state;
    }

    public void setOriginX(double x)
    {
        this.trackingInitialPos[0] = x;
    }

    public void setOriginY(double y)
    {
        this.trackingInitialPos[1] = y;
    }

    public void setOriginZ(double z)
    {
        this.trackingInitialPos[2] = z;
    }

    /**
     * reset the class variables to default values - standby for next export process
     */
    public void reset()
    {
        this.trackingData = new JsonArray();
        this.entityData = new JsonObject();
        this.wrapper = new JsonObject();
        if(!this.relativeOrigin)
        {
            this.trackingInitialPos[0] = 0;
            this.trackingInitialPos[1] = 0;
            this.trackingInitialPos[2] = 0;
        }
        this.building = false;
        this.heldframes = 0;
        this.heldframesEntity = 0;
    }

    @Optional.Method(modid = Minema.MODID)
    public void build(Position position, float partialTick)
    {
        if (!building) //execute once at the beginning of making the json structure
        {
            if (MinemaIntegration.isAvailable())
            {
                this.wrapper.add("information", createCameraInformation());
            }

            this.wrapper.add("camera-tracking", this.trackingData);

            if(!this.entities.isEmpty())
            {
                this.wrapper.add("entities", this.entityData);
            }

            if (!this.relativeOrigin)
            {
                this.trackingInitialPos[0] = position.point.x;
                this.trackingInitialPos[1] = position.point.y;
                this.trackingInitialPos[2] = position.point.z;
            }
        }

        addCameraFrame(position);
        addEntitiesData(partialTick);

        this.building = true;
    }

    private JsonObject createCameraInformation()
    {
        JsonObject information = new JsonObject();
        JsonArray resolution = new JsonArray();

        resolution.add(Minema.instance.getConfig().frameWidth.get());
        resolution.add(Minema.instance.getConfig().frameHeight.get());

        information.add("fps", new JsonPrimitive(Minema.instance.getConfig().frameRate.get()));
        information.add("resolution", resolution);
        information.add("held_frames", new JsonPrimitive(Minema.instance.getConfig().heldFrames.get()));

        return information;
    }

    private void addEntitiesData(float partialTick)
    {
        if (this.checkForDead())
        {
            this.tryFindingEntity(this.selector);
        }

        if (!this.entities.isEmpty() && MinemaIntegration.isAvailable() && Minema.instance.getConfig().heldFrames.get() > 1)
        {
            this.heldframesEntity = (this.heldframesEntity < Minema.instance.getConfig().heldFrames.get()) ? this.heldframesEntity + 1 : 1;
        }

        for (Entity entity : this.entities)
        {
            JsonArray entityFrameArray = new JsonArray();

            if (!building) //execute once at the beginning of exporting
            {
                this.entityData.add(entity.getName(), entityFrameArray);
            }

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;

            JsonObject frame = new JsonObject();
            JsonArray positionData = new JsonArray();
            JsonArray angleData = new JsonArray();

            positionData.add(x - this.trackingInitialPos[0]);
            positionData.add(y - this.trackingInitialPos[1]);
            positionData.add(z - this.trackingInitialPos[2]);
            frame.add("position", positionData);

            if (entity instanceof EntityLivingBase)
            {
                double bodyYaw = ((EntityLivingBase) entity).prevRenderYawOffset + (((EntityLivingBase) entity).renderYawOffset - ((EntityLivingBase) entity).prevRenderYawOffset) * partialTick;

                angleData.add(0);
                angleData.add(bodyYaw);
                angleData.add(0);
                frame.add("body_rotation", angleData);
            }

            entityFrameArray = this.entityData.getAsJsonArray(entity.getName());

            if (MinemaIntegration.isAvailable())
            {
                if (Minema.instance.getConfig().heldFrames.get() > 1 && this.heldframesEntity > 1)
                {
                    JsonObject prevFrame = entityFrameArray.get(entityFrameArray.size()-1).getAsJsonObject();
                    JsonArray prevPositionData = prevFrame.get("position").getAsJsonArray();
                    JsonArray prevBodyRotationData = prevFrame.get("body_rotation").getAsJsonArray();

                    boolean angleComparison = true;

                    if (prevBodyRotationData != null && !prevBodyRotationData.equals(angleData))
                    {
                        angleComparison = false;
                    }

                    if (prevPositionData.equals(positionData) && angleComparison)
                    {
                        continue;
                    }
                    else
                    {
                        /*
                         * expects that heldframes has the same effect on every entity each frame
                         * (otherwise you would need individual heldframe checks for every entity - like an instance attached to it with this algorithm)
                         */
                        this.heldframesEntity = 1;
                    }
                }
            }

            if(entityFrameArray != null)
            {
                entityFrameArray.add(frame);
            }
        }
    }

    private void addCameraFrame(Position position)
    {
        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray angleData = new JsonArray();

        positionData.add(position.point.x - this.trackingInitialPos[0]);
        positionData.add(position.point.y - this.trackingInitialPos[1] + (this.relativeOrigin ? 1.62 : 0 ));
        positionData.add(position.point.z - this.trackingInitialPos[2]);

        angleData.add(position.angle.fov);
        angleData.add(position.angle.roll);
        angleData.add(position.angle.yaw);
        angleData.add(position.angle.pitch);

        frame.add("position", positionData);
        frame.add("angle", angleData);

        if (MinemaIntegration.isAvailable())
        {
            if (Minema.instance.getConfig().heldFrames.get()>1)
            {
                this.heldframes = (this.heldframes<Minema.instance.getConfig().heldFrames.get()) ? this.heldframes + 1 : 1;

                if (this.heldframes>1)
                {
                    JsonObject prevFrame = this.trackingData.get(this.trackingData.size()-1).getAsJsonObject();
                    JsonArray prevPositionData = prevFrame.get("position").getAsJsonArray();
                    JsonArray prevAngleData = prevFrame.get("angle").getAsJsonArray();

                    if (prevAngleData.equals(angleData) && prevPositionData.equals(positionData))
                    {
                        return;
                    }
                    else
                    {
                        this.heldframes = 1;
                    }
                }
            }
        }

        this.trackingData.add(frame);
    }

    @Optional.Method(modid = Minema.MODID)
    public void exportTrackingData(String filename)
    {
        if (MinemaIntegration.isAvailable())
        {
            try
            {
                FileWriter file = new FileWriter(MinemaAPI.getCapturePath().toURI().getPath() + filename);

                file.write(this.wrapper.toString());
                file.close();
                System.out.println("Successfully created the tracking data file.");
            }
            catch (IOException e)
            {
                System.out.println("An error occurred during writing the tracking data file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity(String selector)
    {
        this.entities.clear();

        if (selector != null && !selector.isEmpty() && FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            String[] selectorArray = selector.split(" - ");

            this.tryFindingEntityClient(selectorArray);
        }
    }

    @SideOnly(Side.CLIENT)
    private void tryFindingEntityClient(String[] selectorArray)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;

        for (String selector : selectorArray)
        {
            if (!selector.contains("@"))
            {
                selector = "@e[name=" + selector + "]";
            }

            try
            {
                this.entities.addAll(EntitySelector.matchEntities(player, selector, Entity.class));
            }
            catch (Exception e) { }
        }
    }

    /**
     * Check for dead entities
     */
    protected boolean checkForDead()
    {
        if (this.entities.isEmpty())
        {
            return true;
        }

        Iterator<Entity> it = this.entities.iterator();

        while (it.hasNext())
        {
            Entity entity = it.next();

            if (entity.isDead || entity == Minecraft.getMinecraft().player)
            {
                it.remove();
            }
        }

        return this.entities.isEmpty();
    }
}
