package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import net.minecraft.client.Minecraft;
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

    public GuiFollowFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.angle = new GuiAngleModule(mc, editor);
    }

    @Override
    public void select(LookFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.angle.fill(fixture.position.angle);
    }

    @Override
    public void resize(int width, int height)
    {
        boolean h = this.editor.height - 60 > 200;

        this.angle.resizer().parent(this.area).set(0, 10, 80, 80).x(1, -170);

        if (h)
        {
            this.angle.resizer().x(1, -80).y(120);
        }

        super.resize(width, height);
    }

    @Override
    public void editFixture(EntityPlayer entity)
    {
        Entity mob = ((FollowFixture) this.fixture).getTarget();

        if (mob != null)
        {
            float x = (float) (mob.posX - entity.posX);
            float y = (float) (mob.posY - entity.posY);
            float z = (float) (mob.posZ - entity.posZ);

            this.fixture.position.point.set(x, y, z);
        }

        super.editFixture(entity);
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.editor.drawCenteredString(this.font, I18n.format("aperture.gui.panels.angle"), this.angle.area.x + this.angle.area.w / 2, this.angle.area.y - 14, 0xffffffff);
    }
}