package mchorse.aperture.camera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.MinemaAPI;
import info.ata4.minecraft.minema.client.event.MinemaEventbus;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.aperture.utils.EntitySelector;
import mchorse.aperture.utils.OptifineHelper;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MatrixUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.vecmath.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.*;

/**
 * This is an exporter for camera, entity and morph tracking data
 * @author Christian F. (known as Chryfi)
 */
public class CameraExporter
{
    private final DoubleBuffer buffer = BufferUtils.createDoubleBuffer(16);
    private final double[] doubles = new double[16];
    private final Matrix4d modelview = new Matrix4d();
    private final Matrix4d projection = new Matrix4d();
    private final Matrix4d camera = new Matrix4d();

    private JsonObject wrapper = new JsonObject();
    private JsonArray trackingData = new JsonArray();
    private JsonObject entityData = new JsonObject();
    private JsonObject morphData = new JsonObject();
    private HashMap<String, TrackingPacket> morphNames = new HashMap<>();
    private double[] trackingInitialPos = {0,0,0};
    private int heldframes = 0; //to determine double frames with minema's held frames
    //private int heldframesEntity = 0; //to determine double frames with minema's held frames for entities (maybe redundant?)
    private List<Entity> entities = new ArrayList<>();
    private CameraRunner runner;
    private int frame = 0;

    public boolean relativeOrigin = false;
    public boolean building = false;
    public String selector; //for entities

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

    private void readMVP()
    {
        this.buffer.clear();

        GL11.glGetDouble(2982, this.buffer);
        this.buffer.get(this.doubles);
        this.modelview.set(this.doubles);
        this.modelview.transpose();

        this.buffer.clear();

        GL11.glGetDouble(2983, this.buffer);
        this.buffer.get(this.doubles);
        this.projection.set(this.doubles);
        this.projection.transpose();
    }

    /**
     * reset the class variables to default values - standby for next export process
     */
    public void reset()
    {
        this.buffer.clear();
        this.modelview.setIdentity();
        this.projection.setIdentity();
        this.camera.setIdentity();

        this.wrapper = new JsonObject();
        this.trackingData = new JsonArray();
        this.entityData = new JsonObject();
        this.morphData = new JsonObject();

        this.morphNames.forEach((key, value) ->
        {
            value.reset(); //let the morph know, to delete this tracker
        });

        this.morphNames.clear(); //avoid memory leak

        if (!this.relativeOrigin)
        {
            this.trackingInitialPos[0] = 0;
            this.trackingInitialPos[1] = 0;
            this.trackingInitialPos[2] = 0;
        }

        this.building = false;
        this.frame = 0;
        this.heldframes = 0;
        this.runner = null;
    }

    /**
     * @param tracker
     * @return
     */
    public boolean addTracker(TrackingPacket tracker)
    {
        if (!tracker.combiningMorphs)
        {
            tracker.name = this.checkDuplicateName(tracker.name);

            this.morphData.add(tracker.name, tracker.trackingData);
            this.morphNames.put(tracker.name, tracker);
        }
        else
        {
            if (!this.morphNames.containsKey(tracker.name))
            {
                this.morphData.add(tracker.name, tracker.trackingData);
                this.morphNames.put(tracker.name, tracker);
            }
            else
            {
                tracker.trackingData = this.morphData.getAsJsonArray(tracker.name);
            }
        }

        return true;
    }

    /**
     * This method checks the morphNames map for duplicate name.
     *
     * @param name name of the morph
     * @return It returns either an indexed name like "morph.2" if it found a duplicate. If it didn't find a duplicate it returns the original name
     */
    private String checkDuplicateName(String name)
    {
        int counter = 0;

        while(this.morphNames.containsKey(name + ((counter == 0) ? "" : "."+counter)))
        {
            counter++;
        }

        return name + ((counter == 0) ? "" : "."+counter);
    }

    /**
     * Execute this method at the end of a frame - in gui rendering
     * @param partialTick
     */
    public void frameEnd(float partialTick)
    {
        addEntitiesData(partialTick);

        this.frame = (this.heldframes <= 1) ? this.frame + 1 : this.frame;
    }

    public void start(CameraRunner runner)
    {
        this.building = true;

        if (MinemaIntegration.isAvailable())
        {
            this.wrapper.add("information", getHeaderInformation());
        }

        this.wrapper.add("camera_tracking", this.trackingData);

        if(!this.entities.isEmpty())
        {
            this.wrapper.add("entity_tracking", this.entityData);
        }

        this.wrapper.add("morph_tracking", this.morphData);

        this.runner = runner;

        MinemaEventbus.cameraBUS.registerListener((e) -> addCameraFrame(this.runner.getPosition()));
    }

    private JsonObject getHeaderInformation()
    {
        JsonObject information = new JsonObject();
        JsonArray resolution = new JsonArray();

        resolution.add(Minema.instance.getConfig().frameWidth.get());
        resolution.add(Minema.instance.getConfig().frameHeight.get());

        information.add("fps", new JsonPrimitive(Minema.instance.getConfig().frameRate.get()));

        try //try whether Minema 3.6+ is used
        {
            information.add("motionblur_fps", new JsonPrimitive(Minema.instance.getConfig().getFrameRate()));
        }
        catch(Exception e) {}

        information.add("dynamic_fov", new JsonPrimitive(OptifineHelper.dynamicFov()));
        information.add("resolution", resolution);
        information.add("held_frames", new JsonPrimitive(Minema.instance.getConfig().heldFrames.get()));
        information.add("required_import_version", new JsonPrimitive(140));

        return information;
    }

    public void track(TrackingPacket tracker, EntityLivingBase entity, float partialTicks)
    {
        if (this.heldframes > 1)
        {
            return;
        }

        this.readMVP();

        Matrix4d parent = new Matrix4d(this.modelview);
        Matrix4d rotation = new Matrix4d();
        //MatrixUtils.Transformation modelTransform = MatrixUtils.extractTransformations(MatrixUtils.matrix, MatrixUtils.readModelView(matrix));

        parent.invert();
        parent.mul(this.camera);

        Matrix4d translation = new Matrix4d();
        Minecraft mc = Minecraft.getMinecraft();
        Entity renderEntity = mc.getRenderViewEntity();

        double x = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * mc.getRenderPartialTicks();
        double y = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * mc.getRenderPartialTicks();
        double z = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * mc.getRenderPartialTicks();

        translation.setIdentity();
        translation.setTranslation(new Vector3d(-x, -y, -z));
        parent.mul(translation);
        parent.invert();

        Vector3d pos = new Vector3d(parent.m03, parent.m13, parent.m23);

        Vector4d rx = new Vector4d(parent.m00, parent.m10, parent.m20, 0);
        Vector4d ry = new Vector4d(parent.m01, parent.m11, parent.m21, 0);
        Vector4d rz = new Vector4d(parent.m02, parent.m12, parent.m22, 0);

        rx.normalize();
        ry.normalize();
        rz.normalize();
        rotation.setRow(0, rx);
        rotation.setRow(1, ry);
        rotation.setRow(2, rz);

        pos.add(new Vector3d(Interpolations.lerp(entity.lastTickPosX, entity.posX, partialTicks),
                             Interpolations.lerp(entity.lastTickPosY, entity.posY, partialTicks),
                             Interpolations.lerp(entity.lastTickPosZ, entity.posZ, partialTicks)));

        Vector3d scale = new Vector3d();
        scale.x = Math.sqrt(parent.m00 * parent.m00 + parent.m10 * parent.m10 + parent.m20 * parent.m20);
        scale.y = Math.sqrt(parent.m01 * parent.m01 + parent.m11 * parent.m11 + parent.m21 * parent.m21);
        scale.z = Math.sqrt(parent.m02 * parent.m02 + parent.m12 * parent.m12 + parent.m22 * parent.m22);

        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray rotationData = new JsonArray();
        JsonArray scaleData = new JsonArray();

        positionData.add(pos.x - this.trackingInitialPos[0]);
        positionData.add(pos.y - this.trackingInitialPos[1]);
        positionData.add(pos.z - this.trackingInitialPos[2]);

        JsonArray ax = new JsonArray();
        ax.add(rotation.m00);
        ax.add(rotation.m01);
        ax.add(rotation.m02);

        JsonArray ay = new JsonArray();
        ay.add(rotation.m10);
        ay.add(rotation.m11);
        ay.add(rotation.m12);

        JsonArray az = new JsonArray();
        az.add(rotation.m20);
        az.add(rotation.m21);
        az.add(rotation.m22);

        rotationData.add(ax);
        rotationData.add(ay);
        rotationData.add(az);

        scaleData.add(scale.x);
        scaleData.add(scale.y);
        scaleData.add(scale.z);

        if (tracker.trackingData.size() == 0)
        {
            frame.addProperty("frame",  this.frame);
        }

        frame.add("position", positionData);
        frame.add("rotation", rotationData);
        frame.add("scale", scaleData);

        tracker.trackingData.add(frame);
    }

    private void addEntitiesData(float partialTick)
    {
        if (this.heldframes > 1)
        {
            return;
        }

        if (this.checkForDead())
        {
            this.tryFindingEntity(this.selector);
        }

        for (Entity entity : this.entities)
        {
            JsonArray entityFrameArray = new JsonArray();

            if (this.frame == 0) //execute once at the beginning of exporting
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

            if(entityFrameArray != null)
            {
                entityFrameArray.add(frame);
            }
        }
    }

    public void addCameraFrame(Position position)
    {
        if (!this.building) //dont track when it's not through aperture, at least at the moment
        {
            return;
        }

        this.readMVP();
        this.camera.set(this.modelview);

        Matrix4d translation = new Matrix4d();
        Minecraft mc = Minecraft.getMinecraft();
        Entity renderEntity = mc.getRenderViewEntity();

        double x = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * mc.getRenderPartialTicks();
        double y = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * mc.getRenderPartialTicks();
        double z = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * mc.getRenderPartialTicks();

        translation.setIdentity();
        translation.setTranslation(new Vector3d(-x, -y, -z));
        this.modelview.mul(translation);
        this.modelview.invert();

        Vector3d pos = new Vector3d(this.modelview.m03, this.modelview.m13, this.modelview.m23);

        if (this.frame == 0)
        {
            if (!this.relativeOrigin)
            {
                this.trackingInitialPos[0] = pos.x;
                this.trackingInitialPos[1] = pos.y + 1.62;
                this.trackingInitialPos[2] = pos.z;
            }
        }

        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray angleData = new JsonArray();

        positionData.add(pos.x - this.trackingInitialPos[0]);
        positionData.add(pos.y - this.trackingInitialPos[1] + 1.62);
        positionData.add(pos.z - this.trackingInitialPos[2]);

        angleData.add(position.angle.fov);
        angleData.add(position.angle.roll);
        angleData.add(position.angle.yaw);
        angleData.add(position.angle.pitch);

        frame.add("position", positionData);
        frame.add("angle", angleData);

        if (MinemaIntegration.isAvailable())
        {
            int minemaHF = Minema.instance.getConfig().heldFrames.get();

            if (minemaHF > 1)
            {
                this.heldframes = (this.heldframes < minemaHF) ? this.heldframes + 1 : 1;

                if (this.heldframes > 1)
                {
                    JsonObject prevFrame = this.trackingData.get(this.trackingData.size() - 1).getAsJsonObject();
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

    public static class TrackingPacket
    {
        private String name;
        private JsonArray trackingData = new JsonArray();
        private boolean reset = false;
        private int heldframes = 0; //to determine double frames with minema's held frames
        private boolean combiningMorphs;

        public TrackingPacket(String name, boolean combiningMorphs)
        {
            this.name = name;
            this.combiningMorphs = combiningMorphs;
        }

        public void reset()
        {
            this.trackingData = new JsonArray();
            this.reset = true; //morph should check for this value to delete the packet
        }

        public boolean isReset()
        {
            return this.reset;
        }


        public String getName()
        {
            return this.name;
        }

        public void addTrackingData(JsonElement element)
        {
            this.trackingData.add(element);
        }
    }
}
