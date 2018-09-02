package mchorse.aperture.client.gui.panels;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Interpolation;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Keyframe;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeChannel;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.client.gui.widgets.GuiButtonList;
import mchorse.aperture.client.gui.widgets.buttons.GuiCirculate;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class GuiKeyframeFixturePanel extends GuiAbstractFixturePanel<KeyframeFixture> implements IButtonListener
{
    public KeyframeChannel active;
    public int selected = -1;

    public GuiTrackpad tick;
    public GuiTrackpad value;
    public Area frames = new Area();

    public GuiButtonList buttons;
    public GuiButton x;
    public GuiButton y;
    public GuiButton z;
    public GuiButton yaw;
    public GuiButton pitch;
    public GuiButton roll;
    public GuiButton fov;

    public GuiButton add;
    public GuiButton remove;
    public GuiCirculate interp;

    private boolean dragging = false;

    public GuiKeyframeFixturePanel(FontRenderer font)
    {
        super(font);

        this.tick = new GuiTrackpad(this, font);
        this.tick.title = I18n.format("aperture.gui.panels.tick");
        this.value = new GuiTrackpad(this, font);
        this.value.title = I18n.format("aperture.gui.panels.value");

        this.buttons = new GuiButtonList(Minecraft.getMinecraft(), this);

        this.x = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.x"));
        this.y = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.y"));
        this.z = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.z"));
        this.yaw = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.yaw"));
        this.pitch = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.pitch"));
        this.roll = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.roll"));
        this.fov = new GuiButton(0, 0, 0, I18n.format("aperture.gui.panels.fov"));

        this.add = new GuiButton(0, 0, 0, I18n.format("aperture.gui.add"));
        this.remove = new GuiButton(0, 0, 0, I18n.format("aperture.gui.remove"));
        this.interp = new GuiCirculate(0, 0, 0, 80, 20);
        this.interp.addLabel("Constant");
        this.interp.addLabel("Linear");
        this.interp.addLabel("Quadratic");
        this.interp.addLabel("Cubic");
        this.interp.addLabel("Exponential");
        this.interp.addLabel("Bezier");

        this.buttons.add(this.x);
        this.buttons.add(this.y);
        this.buttons.add(this.z);
        this.buttons.add(this.yaw);
        this.buttons.add(this.pitch);
        this.buttons.add(this.roll);
        this.buttons.add(this.fov);

        this.buttons.add(this.add);
        this.buttons.add(this.remove);
        this.buttons.add(this.interp);
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.tick)
        {
            this.active.keyframes.get(this.selected).tick = (long) value;
            this.dragging = true;
        }
        else if (trackpad == this.value)
        {
            this.active.keyframes.get(this.selected).value = value;
        }

        super.setTrackpadValue(trackpad, value);
    }

    @Override
    public void actionButtonPerformed(GuiButton button)
    {
        if (button == this.x) this.selectChannel(this.fixture.x);
        else if (button == this.y) this.selectChannel(this.fixture.y);
        else if (button == this.z) this.selectChannel(this.fixture.z);
        else if (button == this.yaw) this.selectChannel(this.fixture.yaw);
        else if (button == this.pitch) this.selectChannel(this.fixture.pitch);
        else if (button == this.roll) this.selectChannel(this.fixture.roll);
        else if (button == this.fov) this.selectChannel(this.fixture.fov);
        else if (button == this.add) this.addKeyframe();
        else if (button == this.remove) this.removeKeyframe();
        else if (button == this.interp) this.changeInterpolation();
    }

    private void addKeyframe()
    {
        Position pos = new Position(Minecraft.getMinecraft().thePlayer);
        float value = pos.point.x;

        if (this.active == this.fixture.y) value = pos.point.y;
        if (this.active == this.fixture.z) value = pos.point.z;
        if (this.active == this.fixture.yaw) value = pos.angle.yaw;
        if (this.active == this.fixture.pitch) value = pos.angle.pitch;
        if (this.active == this.fixture.roll) value = pos.angle.roll;
        if (this.active == this.fixture.fov) value = pos.angle.fov;

        this.selected = this.active.insert(this.editor.scrub.value - this.currentOffset(), value);
    }

    private void removeKeyframe()
    {
        this.active.keyframes.remove(this.selected);
        this.selected -= 1;
        this.editor.updateProfile();
    }

    private void changeInterpolation()
    {
        if (this.selected != -1)
        {
            this.active.keyframes.get(this.selected).interp = Interpolation.values()[this.interp.getValue()];
        }
    }

    @Override
    public void select(KeyframeFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        if (!same)
        {
            this.active = fixture.x;
            this.selected = -1;
        }

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
            this.fillData(this.active.keyframes.get(this.selected));
        }
    }

    public void fillData(Keyframe frame)
    {
        this.tick.setValue(frame.tick);
        this.value.setValue(frame.value);
        this.interp.setValue(frame.interp.ordinal());
    }

    public void selectChannel(KeyframeChannel channel)
    {
        this.active = channel;
        this.selected = -1;
    }

    @Override
    public void update(GuiScreen screen)
    {
        super.update(screen);

        int x = this.area.x + this.area.w - 80;

        this.tick.update(x, this.area.y + 10, 80, 20);
        this.value.update(x, this.area.y + 35, 80, 20);

        int h = this.editor.height;
        int i = 0;

        this.frames.set(0, h - (h - this.duration.area.y - 25), this.editor.width, 0);
        this.frames.h = h - this.frames.y - 50;

        x = 10;

        for (GuiButton button : this.buttons.buttons)
        {
            if (i > 6)
            {
                break;
            }

            button.xPosition = x;
            button.yPosition = h - 45;
            button.width = this.font.getStringWidth(button.displayString) + 15;
            button.height = 20;

            x += button.width + 5;
            i += 1;
        }

        this.add.yPosition = this.remove.yPosition = this.x.yPosition;
        this.add.height = this.remove.height = 20;
        this.add.width = 30;
        this.remove.width = 50;

        this.add.xPosition = this.editor.width - 95;
        this.remove.xPosition = this.add.xPosition + this.add.width + 5;

        this.interp.yPosition = this.tick.area.y;
        this.interp.xPosition = this.tick.area.x - 90;
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

        /* Select current point with a mouse click */
        if (this.frames.isInside(mouseX, mouseY))
        {
            Keyframe prev = null;
            int ry = 0;
            int index = 0;
            float c = this.frames.y + this.frames.h / 2;

            for (Keyframe frame : this.active.keyframes)
            {
                if (prev == null)
                {
                    ry = (int) frame.value;
                    prev = frame;
                }

                int x = (int) (frame.tick * 2);
                int y = (int) (-(frame.value - ry) + c);

                double d = Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2);

                if (Math.sqrt(d) < 4)
                {
                    this.selected = index;
                    this.fillData(frame);

                    break;
                }

                index++;
            }
        }

        this.buttons.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.selected != -1)
        {
            this.tick.mouseReleased(mouseX, mouseY, state);
            this.value.mouseReleased(mouseX, mouseY, state);

            if (this.dragging)
            {
                /* Resort after dragging the tick thing */
                Keyframe frame = this.active.keyframes.get(this.selected);

                this.active.sort();
                this.dragging = false;
                this.selected = this.active.keyframes.indexOf(frame);
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        int w = this.editor.width;
        int h = this.editor.height;

        Gui.drawRect(this.frames.x, this.frames.y, this.frames.x + this.frames.w, this.frames.y + this.frames.h, 0x88000000);
        GuiUtils.scissor(this.frames.x, this.frames.y, this.frames.w, this.frames.h, w, h);

        this.drawGraph(w, h);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (this.selected != -1)
        {
            this.tick.draw(mouseX, mouseY, partialTicks);
            this.value.draw(mouseX, mouseY, partialTicks);
        }

        this.buttons.draw(mouseX, mouseY);

        super.draw(mouseX, mouseY, partialTicks);
    }

    private void drawGraph(int w, int h)
    {
        if (this.active.isEmpty())
        {
            return;
        }

        /* Draw graph of the keyframe channel */
        GL11.glLineWidth(2);
        GL11.glPointSize(8);
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
                int px = (int) prev.tick * 2;
                int fx = (int) frame.tick * 2;

                if (prev.interp == Interpolation.LINEAR)
                {
                    vb.pos(px, -(prev.value - y) + c, 0).endVertex();
                    vb.pos(fx, -(frame.value - y) + c, 0).endVertex();
                }
                else
                {
                    for (int i = 0; i < 10; i++)
                    {
                        vb.pos(px + (fx - px) * (i / 10F), -(prev.interpolate(frame, i / 10F) - y) + c, 0).endVertex();
                        vb.pos(px + (fx - px) * ((i + 1) / 10F), -(prev.interpolate(frame, (i + 1) / 10F) - y) + c, 0).endVertex();
                    }
                }
            }

            if (prev == null)
            {
                y = frame.value;
                vb.pos(frame.tick * 2, -(frame.value - y) + c, 0).endVertex();
            }

            prev = frame;
        }

        vb.pos(prev.tick * 2, -(prev.value - y) + c, 0).endVertex();
        vb.pos(w, -(prev.value - y) + c, 0).endVertex();

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

            GL11.glVertex2f(frame.tick * 2, -(frame.value - y) + c);
            GL11.glEnd();

            i++;
        }

        /* Draw current point at the scrub */
        int cx = (int) (this.editor.scrub.value - this.currentOffset());
        int cy = (int) (-(this.active.interpolate(cx) - y) + c);

        cx *= 2;

        Gui.drawRect(cx - 1, cy, cx + 1, h, 0xff57f52a);

        GlStateManager.disableTexture2D();
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glColor3f(0x57 / 255F, 0xf5 / 255F, 0x2a / 255F);
        GL11.glVertex2f(cx, cy);
        GL11.glEnd();
        GlStateManager.enableTexture2D();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}