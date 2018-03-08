package mchorse.aperture.camera;

import java.util.List;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.camera.smooth.Filter;
import mchorse.aperture.camera.smooth.SmoothCamera;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Profile path renderer
 *
 * This class is responsible for rendering current loaded profile.
 */
@SideOnly(Side.CLIENT)
public class CameraRenderer
{
    /**
     * Background texture for a fixture rendering.
     */
    public static final ResourceLocation TEXTURE = new ResourceLocation(Aperture.MODID, "textures/gui/fixture.png");

    protected Minecraft mc = Minecraft.getMinecraft();

    public SmoothCamera smooth = new SmoothCamera();
    public Filter roll = new Filter();
    public Filter fov = new Filter();

    protected double playerX;
    protected double playerY;
    protected double playerZ;

    /**
     * Toggle path rendering
     */
    public void toggleRender()
    {
        Property prop = Aperture.proxy.forge.getCategory("camera").get("camera_profile_render");

        prop.set(!prop.getBoolean());

        Aperture.proxy.forge.save();
        Aperture.proxy.config.reload();
    }

    /**
     * Orient the camera
     *
     * This method is responsible for setting the roll of the camera and also
     * locking yaw and pitch during profile running.
     */
    @SubscribeEvent
    public void onCameraOrient(EntityViewRenderEvent.CameraSetup event)
    {
        CameraRunner runner = ClientProxy.runner;

        float ticks = (float) event.getRenderPartialTicks();
        EntityPlayer player = this.mc.player;

        /**
         * Apply camera angles only in case if it's the player. The 
         * reason behind this check is that mods like CFM which render 
         * the world to a texture won't get affected the camera changes.
         * 
         * The 0.001 part is added due to float precision error.
         */
        if (Math.abs((event.getYaw() - 180) - player.rotationYaw) > 0.001)
        {
            return;
        }

        if (runner.isRunning())
        {
            event.setYaw(-180 + runner.yaw);
            event.setPitch(runner.pitch);

            if (Aperture.proxy.config.camera_outside && !Aperture.proxy.config.camera_outside_hide_player)
            {
                this.mc.gameSettings.thirdPersonView = 1;
            }
        }
        else if (Minecraft.getMinecraft().currentScreen == ClientProxy.cameraEditor)
        {
            Position position = ClientProxy.cameraEditor.position;

            event.setYaw(-180 + position.angle.yaw);
            event.setPitch(position.angle.pitch);
        }
        else if (this.smooth.enabled && !this.mc.isGamePaused())
        {
            /* Yaw and pitch */
            float yaw = this.smooth.getInterpYaw(ticks);
            float pitch = this.smooth.getInterpPitch(ticks);

            event.setYaw(-180 + yaw);
            event.setPitch(-pitch);

            player.rotationYaw = yaw;
            player.rotationPitch = -pitch;

            player.prevRotationYaw = yaw;
            player.prevRotationPitch = -pitch;

            /* Roll and FOV */
            if (this.roll.acc != 0.0F)
            {
                ClientProxy.control.roll = this.roll.interpolate(ticks);
            }
            else
            {
                this.roll.set(ClientProxy.control.roll);
            }

            if (this.fov.acc != 0.0F)
            {
                this.fov.interpolate(ticks);
                this.fov.value = MathHelper.clamp(this.fov.value, 0.0001F, 179.9999F);

                this.mc.gameSettings.fovSetting = this.fov.value;
            }
            else
            {
                this.fov.set(this.mc.gameSettings.fovSetting);
            }
        }

        float roll = ClientProxy.control.roll;

        if (roll == 0)
        {
            return;
        }

        event.setRoll(roll);
    }

    /**
     * This is updating smooth camera
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        SmoothCamera camera = ClientProxy.renderer.smooth;
        EntityPlayer player = this.mc.player;

        if (event.side == Side.CLIENT && event.player == player && camera.enabled)
        {
            /* Copied from EntityRenderer */
            float sensetivity = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float finalSensetivity = sensetivity * sensetivity * sensetivity * 8.0F;
            float dx = this.mc.mouseHelper.deltaX * finalSensetivity * 0.15F;
            float dy = this.mc.mouseHelper.deltaY * finalSensetivity * 0.15F;

            /* Updating smooth camera */
            camera.update(this.mc.player, dx, dy);

            /* Roll and FOV acceleration */
            KeyboardHandler keys = ClientProxy.keys;

            float roll = keys.addRoll.isKeyDown() ? 1 : (keys.reduceRoll.isKeyDown() ? -1 : 0F);
            float fov = keys.addFov.isKeyDown() ? 1 : (keys.reduceFov.isKeyDown() ? -1 : 0F);

            this.roll.accelerate(roll * this.roll.factor);
            this.fov.accelerate(fov * this.fov.factor);
        }
    }

    /**
     * Render all camera fixtures
     */
    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event)
    {
        CameraProfile profile = ClientProxy.control.currentProfile;
        CameraRunner runner = ClientProxy.runner;

        boolean badProfile = profile == null || profile.getCount() < 1;

        if (!Aperture.proxy.config.camera_profile_render) return;
        if (runner.isRunning()) return;
        if (badProfile) return;

        Position prev = new Position(0, 0, 0, 0, 0);
        Position next = new Position(0, 0, 0, 0, 0);

        EntityPlayer player = runner.outside.active ? runner.outside.camera : this.mc.player;
        float ticks = event.getPartialTicks();

        this.playerX = player.prevPosX + (player.posX - player.prevPosX) * ticks;
        this.playerY = player.prevPosY + (player.posY - player.prevPosY) * ticks;
        this.playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * ticks;

        GlStateManager.pushAttrib();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        int i = 0;

        for (AbstractFixture fixture : profile.getAll())
        {
            fixture.applyFixture(0, 0.0F, prev);
            fixture.applyFixture(fixture.getDuration(), 0.0F, next);

            long duration = fixture.getDuration();

            float distX = Math.abs(next.point.x - prev.point.x);
            float distY = Math.abs(next.point.y - prev.point.y);
            float distZ = Math.abs(next.point.z - prev.point.z);

            Color color = FixtureRegistry.CLIENT.get(fixture.getClass()).color;

            if (distX + distY + distZ >= 0.5) this.drawCard(color, i, duration, next);

            this.drawCard(color, i++, duration, prev);
            this.drawFixture(0.0F, color, fixture, prev, next);
        }

        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
        GL11.glLineWidth(2);
    }

    /**
     * Draw a fixture's fixture
     */
    private void drawFixture(float partialTicks, Color color, AbstractFixture fixture, Position prev, Position next)
    {
        if (fixture instanceof PathFixture)
        {
            this.drawPathFixture(color, fixture, prev, next);
        }
        else if (fixture instanceof CircularFixture)
        {
            this.drawCircularFixture(partialTicks, color, fixture, prev, next);
        }
    }

    /**
     * Draw the passed path fixture
     */
    private void drawPathFixture(Color color, AbstractFixture fixture, Position prev, Position next)
    {
        PathFixture path = (PathFixture) fixture;
        List<DurablePosition> points = path.getPoints();
        long duration = fixture.getDuration();

        final int p = 15;

        for (int i = 0, size = points.size() - 1; i < size; i++)
        {
            for (int j = 0; j < p; j++)
            {
                path.applyFixture((long) ((float) (j + i * p) / (float) (size * p) * duration), 0, prev);
                path.applyFixture((long) ((float) (j + i * p + 1) / (float) (size * p) * duration), 0, next);

                this.drawLine(color, this.playerX, this.playerY, this.playerZ, prev, next);
            }

            if (i != 0)
            {
                path.applyFixture(path.getTickForPoint(i), 0, prev);

                this.drawPathPoint(color, prev, i);
            }
        }
    }

    /**
     * Draw the path point
     *
     * This method is responsible for drawing a square with a label which shows
     * the index of that point. This is very useful for path point management.
     */
    private void drawPathPoint(Color color, Position position, int index)
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.color(color.red, color.green, color.blue, 1.0F);

        this.mc.renderEngine.bindTexture(TEXTURE);

        double x = position.point.x - this.playerX;
        double y = position.point.y - this.playerY;
        double z = position.point.z - this.playerZ;

        GL11.glNormal3f(0, 1, 0);
        GlStateManager.translate(x, y + this.mc.player.eyeHeight, z);

        if (Minecraft.getMinecraft().currentScreen == ClientProxy.cameraEditor)
        {
            Position pos = ClientProxy.cameraEditor.position;

            GlStateManager.rotate(-pos.angle.yaw, 0, 1, 0);
            GlStateManager.rotate(pos.angle.pitch, 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(-this.mc.getRenderManager().playerViewY, 0, 1, 0);
            GlStateManager.rotate(this.mc.getRenderManager().playerViewX, 1, 0, 0);
        }

        float factor = 0.1F;
        float minX = -factor;
        float minY = -factor;
        float maxX = factor;
        float maxY = factor;

        int tw = 34;

        int tx = 32;
        int tx2 = 34;

        float texX = (float) tx / (float) tw;
        float texY = 0;
        float texRX = (float) tx2 / (float) tw;
        float texRY = 2.0F / 16.0F;

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(minX, minY, 0).tex(texRX, texRY).endVertex();
        vb.pos(minX, maxY, 0).tex(texRX, texY).endVertex();
        vb.pos(maxX, maxY, 0).tex(texX, texY).endVertex();
        vb.pos(maxX, minY, 0).tex(texX, texRY).endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();

        String indexString = String.valueOf(index);
        int indexWidth = this.mc.fontRendererObj.getStringWidth(indexString) / 2;

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.scale(0.03f, 0.03f, 0.03f);
        GlStateManager.translate(0, -3.5, -0.1);
        GlStateManager.translate(0, -12, 0);

        this.mc.fontRendererObj.drawString(indexString, -indexWidth, 0, -1);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    /**
     * Draw the passed circular fixture
     */
    private void drawCircularFixture(float partialTicks, Color color, AbstractFixture fixture, Position prev, Position next)
    {
        float circles = Math.min(((CircularFixture) fixture).circles, 360);
        long duration = fixture.getDuration();

        for (int i = 0; i < circles; i += 5)
        {
            float a = i / circles * duration;
            float b = (i + 3) / circles * duration;

            fixture.applyFixture((long) a, a - (int) a, prev);
            fixture.applyFixture((long) b, b - (int) b, next);

            this.drawLine(color, this.playerX, this.playerY, this.playerZ, prev, next);
        }
    }

    /**
     * Draw the card of the fixture with the information about this fixture,
     * like duration and stuff.
     */
    private void drawCard(Color color, int index, long duration, Position pos)
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.color(color.red, color.green, color.blue, 0.8F);

        this.mc.renderEngine.bindTexture(TEXTURE);

        double x = pos.point.x - this.playerX;
        double y = pos.point.y - this.playerY;
        double z = pos.point.z - this.playerZ;

        GL11.glNormal3f(0, 1, 0);
        GlStateManager.translate(x, y + this.mc.player.eyeHeight, z);

        if (Minecraft.getMinecraft().currentScreen == ClientProxy.cameraEditor)
        {
            Position position = ClientProxy.cameraEditor.position;

            GlStateManager.rotate(-position.angle.yaw, 0, 1, 0);
            GlStateManager.rotate(position.angle.pitch, 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(-this.mc.getRenderManager().playerViewY, 0, 1, 0);
            GlStateManager.rotate(this.mc.getRenderManager().playerViewX, 1, 0, 0);
        }

        float factor = 0.5F;

        float minX = -factor;
        float minY = -factor;
        float maxX = factor;
        float maxY = factor;

        int tw = 34;
        int i = 0;

        int tx = i * 16;
        int tx2 = tx + 16;

        float texX = (float) tx / (float) tw;
        float texY = 0;
        float texRX = (float) tx2 / (float) tw;
        float texRY = 1;

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(minX, minY, 0).tex(texRX, texRY).endVertex();
        vb.pos(minX, maxY, 0).tex(texRX, texY).endVertex();
        vb.pos(maxX, maxY, 0).tex(texX, texY).endVertex();
        vb.pos(maxX, minY, 0).tex(texX, texRY).endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();

        String indexString = String.valueOf(index);
        String durationString = duration + "t";
        int indexWidth = this.mc.fontRendererObj.getStringWidth(indexString) / 2;
        int durationWidth = this.mc.fontRendererObj.getStringWidth(durationString) / 2;

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.scale(0.05f, 0.05f, 0.05f);
        GlStateManager.translate(0, -3.5, -0.1);

        this.mc.fontRendererObj.drawString(indexString, -indexWidth, 0, -1);

        GlStateManager.translate(0, -13, 0);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);

        this.mc.fontRendererObj.drawString(durationString, -durationWidth, 0, -1);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    /**
     * Draw a line between two positions
     */
    private void drawLine(Color color, double playerX, double playerY, double playerZ, Position prev, Position next)
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        double x = prev.point.x - playerX;
        double y = prev.point.y - playerY;
        double z = prev.point.z - playerZ;

        GL11.glLineWidth(4);
        GlStateManager.disableTexture2D();
        GlStateManager.color(color.red, color.green, color.blue, 0.5F);

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        vb.setTranslation(x, y + this.mc.player.eyeHeight, z);
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        vb.pos(next.point.x - prev.point.x, next.point.y - prev.point.y, next.point.z - prev.point.z).endVertex();
        vb.pos(0, 0, 0).endVertex();

        Tessellator.getInstance().draw();

        vb.setTranslation(0, 0, 0);

        GlStateManager.enableTexture2D();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }
}