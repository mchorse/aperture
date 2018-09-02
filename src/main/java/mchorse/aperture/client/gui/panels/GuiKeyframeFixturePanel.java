package mchorse.aperture.client.gui.panels;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.utils.Area;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;

public class GuiKeyframeFixturePanel extends GuiAbstractFixturePanel<KeyframeFixture>
{
    public KeyframeChannel active;
    public int selected = -1;

    public GuiTrackpad tick;
    public GuiTrackpad value;
    public Area frames = new Area();

    public GuiKeyframeFixturePanel(FontRenderer font)
    {
        super(font);

        this.tick = new GuiTrackpad(this, font);
        this.tick.title = "Tick";
        this.value = new GuiTrackpad(this, font);
        this.value.title = "Value";
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.tick)
        {
            this.active.keyframes.get(this.selected).tick = (long) value;
        }
        else if (trackpad == this.value)
        {
            this.active.keyframes.get(this.selected).value = value;
        }

        super.setTrackpadValue(trackpad, value);
    }

    @Override
    public void select(KeyframeFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.active = fixture.x;
        this.selected = -1;

        if (duration != -1)
        {
            int i = 0;

            for (Keyframe frame : this.active.keyframes)
            {
                if (frame.tick >= duration)
                {
                    this.selected = i;

                    break;
                }

                i++;
            }
        }

        if (this.selected != -1)
        {
            Keyframe frame = this.active.keyframes.get(this.selected);

            this.tick.setValue(frame.tick);
            this.value.setValue(frame.value);
        }
    }

    @Override
    public void update(GuiScreen screen)
    {
        super.update(screen);

        int x = this.area.x + this.area.w - 80;

        this.tick.update(x, this.area.y + 10, 80, 20);
        this.value.update(x, this.area.y + 35, 80, 20);

        int h = this.editor.height;

        this.frames.set(0, h - (h - this.duration.area.y - 25), this.editor.width, 0);
        this.frames.h = h - this.frames.y - 20;
    }

    @Override
    public void editFixture(EntityPlayer entity)
    {
        Position pos = new Position(entity);
        long tick = this.editor.scrub.value - this.currentOffset();

        this.fixture.x.insert(tick, pos.point.x);
        this.fixture.y.insert(tick, pos.point.y);
        this.fixture.z.insert(tick, pos.point.z);
        this.fixture.yaw.insert(tick, pos.angle.yaw);
        this.fixture.pitch.insert(tick, pos.angle.pitch);
        this.fixture.roll.insert(tick, pos.angle.roll);
        this.fixture.fov.insert(tick, pos.angle.fov);

        this.editor.updateProfile();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        super.keyTyped(typedChar, keyCode);

        if (this.selected != -1)
        {
            this.tick.keyTyped(typedChar, keyCode);
            this.value.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return this.selected != -1 && (this.tick.text.isFocused() || this.value.text.isFocused());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.selected != -1)
        {
            this.tick.mouseClicked(mouseX, mouseY, mouseButton);
            this.value.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.selected != -1)
        {
            this.tick.mouseReleased(mouseX, mouseY, state);
            this.value.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        int w = this.editor.width;
        int h = this.editor.height;

        Gui.drawRect(this.frames.x, this.frames.y, this.frames.x + this.frames.w, this.frames.y + this.frames.h, 0x88000000);
        GuiUtils.scissor(this.frames.x, this.frames.y, this.frames.w, this.frames.h, w, h);

        /* Draw graph of the keyframe channel */
        GL11.glLineWidth(2);
        GL11.glPointSize(6);
        GlStateManager.disableTexture2D();

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 0.75F);

        /* Draw lines */
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        Keyframe prev = null;
        float y = 0;
        float c = this.frames.y + this.frames.h / 2;

        vb.pos(0, c, 0).endVertex();

        for (Keyframe frame : this.active.keyframes)
        {
            if (prev != null)
            {
                vb.pos(prev.tick * 2, prev.value - y + c, 0).endVertex();
                vb.pos(frame.tick * 2, frame.value - y + c, 0).endVertex();
            }

            if (prev == null)
            {
                y = frame.value;
                vb.pos(frame.tick * 2, frame.value - y + c, 0).endVertex();
            }

            prev = frame;
        }

        vb.pos(prev.tick * 2, prev.value - y + c, 0).endVertex();
        vb.pos(w, prev.value - y + c, 0).endVertex();

        Tessellator.getInstance().draw();

        /* Draw points */
        int i = 0;

        for (Keyframe frame : this.active.keyframes)
        {
            GL11.glBegin(GL11.GL_POINTS);
            if (this.selected == i)
            {
                GL11.glColor3f(0, 0.5F, 1);
            }
            else
            {
                GL11.glColor3f(1, 1, 1);
            }

            GL11.glVertex2f(frame.tick * 2, frame.value - y + c);
            GL11.glEnd();

            i++;
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (this.selected != -1)
        {
            this.tick.draw(mouseX, mouseY, partialTicks);
            this.value.draw(mouseX, mouseY, partialTicks);
        }

        super.draw(mouseX, mouseY, partialTicks);
    }
}