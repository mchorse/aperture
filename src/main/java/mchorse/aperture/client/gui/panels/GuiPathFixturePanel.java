package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.PathFixture;
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

    public Position position;

    public GuiPathFixturePanel(FontRenderer font)
    {
        super(font);

        this.point = new GuiPointModule(this, font);
        this.angle = new GuiAngleModule(this, font);
        this.points = new GuiPointsModule(this, font);
        this.interp = new GuiInterpModule(this);

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
    }

    @Override
    public void select(PathFixture fixture)
    {
        super.select(fixture);

        Position pos = fixture.getPoint(0);

        this.points.index = 0;
        this.position = pos;

        this.point.fill(pos.point);
        this.angle.fill(pos.angle);
        this.points.fill(fixture);
        this.interp.fill(fixture);
    }

    @Override
    public void update(GuiScreen screen)
    {
        super.update(screen);

        int x = this.area.x + this.area.w;
        int y = this.area.y;

        this.points.update(screen, x - 20, y, 100);

        x -= 30;

        this.point.update(x, y);

        x -= 80 * 2 + 10;

        this.angle.update(x, y);

        x = this.area.x;
        y = this.area.y + 55;

        this.interp.update(x, y, 80);
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
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.point.mouseClicked(mouseX, mouseY, mouseButton);
        this.angle.mouseClicked(mouseX, mouseY, mouseButton);
        this.points.mouseClicked(mouseX, mouseY, mouseButton);
        this.interp.mouseClicked(mouseX, mouseY, mouseButton);
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

        this.editor.drawCenteredString(this.font, "Position", this.area.x + this.area.w - 70, this.area.y + this.area.h - 14, 0xffffffff);
        this.editor.drawCenteredString(this.font, "Angle", this.area.x + this.area.w - 160, this.area.y + this.area.h - 14, 0xffffffff);

        this.point.draw(mouseX, mouseY, partialTicks);
        this.angle.draw(mouseX, mouseY, partialTicks);
        this.points.draw(mouseX, mouseY, partialTicks);
        this.interp.draw(mouseX, mouseY, partialTicks);
    }
}