package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Follow fixture panel
 *
 * This panel is responsible for editing follow fixture. Editing features are
 * derived from look fixture panel, but also, this panel adds an angle module
 * in order to be able to manipulate position's angle.
 */
public class GuiFollowFixturePanel extends GuiLookFixturePanel
{
    public GuiAngleModule angle;

    public GuiFollowFixturePanel(FontRenderer font)
    {
        super(font);

        this.angle = new GuiAngleModule(this, font);
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.angle.yaw)
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
    public void select(LookFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.angle.fill(fixture.position.angle);
    }

    @Override
    public void update(GuiScreen screen)
    {
        boolean height = screen.height - 60 > 200;

        this.height = height ? 200 : 100;

        super.update(screen);

        int x = this.area.x + this.area.w - 80;
        int y = this.area.y + 10;

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
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        Entity mob = ((FollowFixture) this.fixture).getTarget();

        if (mob != null)
        {
            float x = (float) (mob.posX - player.posX);
            float y = (float) (mob.posY - player.posY);
            float z = (float) (mob.posZ - player.posZ);

            this.fixture.position.point.set(x, y, z);
        }

        super.editFixture();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.angle.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        this.angle.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return super.hasActiveTextfields() || this.angle.hasActiveTextfields();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        super.keyTyped(typedChar, keyCode);

        this.angle.keyTyped(typedChar, keyCode);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.angle"), this.angle.yaw.area.x + this.angle.yaw.area.w / 2, this.angle.yaw.area.y - 14, 0xffffffff);

        this.angle.draw(mouseX, mouseY, partialTicks);
    }
}
