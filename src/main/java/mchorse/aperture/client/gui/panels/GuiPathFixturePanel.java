package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiInterpModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule.IPointPicker;
import mchorse.aperture.client.gui.widgets.buttons.GuiCirculate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

/**
 * Path fixture panel
 *
 * This panel has the most modules used. It's responsible for editing path
 * fixture. It uses point and angle modules to edit a position which is picked
 * from the points module. Interpolation module is used to modify path fixture's
 * interpolation methods.
 */
public class GuiPathFixturePanel extends GuiAbstractFixturePanel<PathFixture> implements ITrackpadListener, IPointPicker, IButtonListener
{
    public GuiPointModule point;
    public GuiAngleModule angle;
    public GuiPointsModule points;
    public GuiInterpModule interp;
    public GuiCheckBox perPointDuration;

    public DurablePosition position;

    public GuiPathFixturePanel(FontRenderer font)
    {
        super(font);

        this.point = new GuiPointModule(this, font);
        this.angle = new GuiAngleModule(this, font);
        this.points = new GuiPointsModule(this, font);
        this.interp = new GuiInterpModule(this);
        this.perPointDuration = new GuiCheckBox(0, 0, 0, I18n.format("aperture.gui.panels.per_point"), false);

        this.height = 100;
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.point.x)
        {
            this.position.point.x = trackpad.value;
        }
        else if (trackpad == this.point.y)
        {
            this.position.point.y = trackpad.value;
        }
        else if (trackpad == this.point.z)
        {
            this.position.point.z = trackpad.value;
        }
        else if (trackpad == this.angle.yaw)
        {
            this.position.angle.yaw = trackpad.value;
        }
        else if (trackpad == this.angle.pitch)
        {
            this.position.angle.pitch = trackpad.value;
        }
        else if (trackpad == this.angle.roll)
        {
            this.position.angle.roll = trackpad.value;
        }
        else if (trackpad == this.angle.fov)
        {
            this.position.angle.fov = trackpad.value;
        }

        super.setTrackpadValue(trackpad, value);
    }

    @Override
    public void pickPoint(GuiPointsModule module, int index)
    {
        this.position = this.fixture.getPoint(index);

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);

        if (this.fixture.perPointDuration)
        {
            this.duration.setValue(this.position.getDuration());
        }
        else
        {
            this.duration.setValue(this.fixture.getDuration());
        }

        if (this.editor.syncing)
        {
            this.editor.scrub.setValue((int) this.currentOffset());
        }
    }

    @Override
    public void actionButtonPerformed(GuiButton button)
    {
        if (button == this.interp.pos)
        {
            this.fixture.interpolationPos = this.interp.typeFromIndex(((GuiCirculate) button).getValue());
        }
        else if (button == this.interp.angle)
        {
            this.fixture.interpolationAngle = this.interp.typeFromIndex(((GuiCirculate) button).getValue());
        }

        this.editor.updateProfile();
    }

    @Override
    public void select(PathFixture fixture, long duration)
    {
        super.select(fixture, duration);

        int index = this.points.index;

        if (duration != -1)
        {
            index = (int) ((duration / (float) fixture.getDuration()) * fixture.getCount());

            DurablePosition pos = fixture.getPoint(index);

            this.position = pos;
            this.points.index = index;
        }

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);
        this.points.fill(fixture);
        this.interp.fill(fixture);
        this.perPointDuration.setIsChecked(fixture.perPointDuration);

        this.points.index = index;
    }

    @Override
    public long currentOffset()
    {
        long point = this.fixture.getTickForPoint(this.points.index);

        if (point == this.fixture.getDuration())
        {
            point--;
        }

        return super.currentOffset() + point;
    }

    @Override
    public void update(GuiScreen screen)
    {
        boolean height = screen.height - 60 > 200;

        this.height = height ? 200 : 100;

        super.update(screen);

        int x = this.area.x + this.area.w - 80;
        int y = this.area.y + 10;

        this.points.update(screen, screen.width / 2 - 45, screen.height - 50, 90, 20);

        this.point.update(x, y);

        if (height)
        {
            y += 110;
        }
        else
        {
            x -= 80 + 10;
        }

        this.angle.update(x, y);

        x = this.area.x;
        y = this.area.y + 60;

        this.interp.update(x, y, 80);

        this.perPointDuration.xPosition = x;
        this.perPointDuration.yPosition = y + 50;
    }

    @Override
    public void editFixture()
    {
        if (this.position != null)
        {
            this.position.set(Minecraft.getMinecraft().thePlayer);

            super.editFixture();
        }
    }

    @Override
    protected void updateDuration(long value)
    {
        if (this.fixture.perPointDuration)
        {
            this.position.setDuration(value);
        }
        else
        {
            this.fixture.setDuration(value);
        }

        this.editor.updateValues();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.point.mouseClicked(mouseX, mouseY, mouseButton);
        this.angle.mouseClicked(mouseX, mouseY, mouseButton);
        this.points.mouseClicked(mouseX, mouseY, mouseButton);
        this.interp.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.perPointDuration.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
        {
            this.fixture.perPointDuration = this.perPointDuration.isChecked();
            this.editor.updateProfile();

            if (this.fixture.perPointDuration)
            {
                this.duration.setValue(this.position.getDuration());
            }
            else
            {
                this.duration.setValue(this.fixture.getDuration());
            }

            this.editor.updateValues();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        this.point.mouseReleased(mouseX, mouseY, state);
        this.angle.mouseReleased(mouseX, mouseY, state);
        this.points.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return super.hasActiveTextfields() || this.point.hasActiveTextfields() || this.angle.hasActiveTextfields();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        super.keyTyped(typedChar, keyCode);

        this.point.keyTyped(typedChar, keyCode);
        this.angle.keyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.position"), this.point.x.area.x + this.point.x.area.w / 2, this.point.x.area.y - 14, 0xffffffff);
        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.angle"), this.angle.yaw.area.x + this.angle.yaw.area.w / 2, this.angle.yaw.area.y - 14, 0xffffffff);

        this.point.draw(mouseX, mouseY, partialTicks);
        this.angle.draw(mouseX, mouseY, partialTicks);
        this.points.draw(mouseX, mouseY, partialTicks);
        this.interp.draw(mouseX, mouseY, partialTicks);
        this.perPointDuration.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }
}