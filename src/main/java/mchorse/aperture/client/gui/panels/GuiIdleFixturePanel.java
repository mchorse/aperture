package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

/**
 * Idle fixture panel
 *
 * This panel is responsible for editing an idle fixture. This panel uses basic
 * point and angle modules for manipulating idle fixture's position.
 */
public class GuiIdleFixturePanel extends GuiAbstractFixturePanel<IdleFixture> implements ITrackpadListener
{
    public GuiPointModule point;
    public GuiAngleModule angle;

    public GuiIdleFixturePanel(FontRenderer font)
    {
        super(font);

        this.point = new GuiPointModule(this, font);
        this.angle = new GuiAngleModule(this, font);

        this.height = 100;
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.point.x)
        {
            this.fixture.position.point.x = trackpad.value;
        }
        else if (trackpad == this.point.y)
        {
            this.fixture.position.point.y = trackpad.value;
        }
        else if (trackpad == this.point.z)
        {
            this.fixture.position.point.z = trackpad.value;
        }
        else if (trackpad == this.angle.yaw)
        {
            this.fixture.position.angle.yaw = trackpad.value;
        }
        else if (trackpad == this.angle.pitch)
        {
            this.fixture.position.angle.pitch = trackpad.value;
        }
        else if (trackpad == this.angle.roll)
        {
            this.fixture.position.angle.roll = trackpad.value;
        }
        else if (trackpad == this.angle.fov)
        {
            this.fixture.position.angle.fov = trackpad.value;
        }

        super.setTrackpadValue(trackpad, value);
    }

    @Override
    public void select(IdleFixture fixture)
    {
        super.select(fixture);

        this.point.fill(fixture.position.point);
        this.angle.fill(fixture.position.angle);
    }

    @Override
    public void update(GuiScreen screen)
    {
        boolean height = screen.height - 60 > 200;

        this.height = height ? 200 : 100;

        super.update(screen);

        int x = this.area.x + this.area.w - 80;
        int y = this.area.y;

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
    }

    @Override
    public void editFixture()
    {
        this.fixture.position.set(Minecraft.getMinecraft().thePlayer);

        super.editFixture();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.point.mouseClicked(mouseX, mouseY, mouseButton);
        this.angle.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        this.point.mouseReleased(mouseX, mouseY, state);
        this.angle.mouseReleased(mouseX, mouseY, state);
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

        this.editor.drawCenteredString(this.font, "Position", this.point.z.area.x + this.point.z.area.w / 2, this.point.z.area.y + this.point.z.area.h + 6, 0xffffffff);
        this.editor.drawCenteredString(this.font, "Angle", this.angle.fov.area.x + this.angle.fov.area.w / 2, this.angle.fov.area.y + this.angle.fov.area.h + 6, 0xffffffff);

        this.point.draw(mouseX, mouseY, partialTicks);
        this.angle.draw(mouseX, mouseY, partialTicks);
    }
}