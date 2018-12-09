package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Base class for abstract fixture panel
 *
 * This panel adds inputs for only two properties (which has every camera
 * fixture): name and duration.
 */
public abstract class GuiAbstractFixturePanel<T extends AbstractFixture> extends GuiElement implements IFixturePanel<T>
{
    /**
     * Currently editing camera fixture
     */
    public T fixture;

    /* Stuff */
    public GuiCameraEditor editor;

    /* GUI fields */
    public GuiTextElement name;
    public GuiTrackpadElement duration;

    public GuiAbstractFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.createChildren();
        this.name = new GuiTextElement(mc, 80, (str) ->
        {
            this.fixture.setName(str);
            this.editor.updateValues();
            this.editor.updateProfile();
        });

        this.duration = new GuiTrackpadElement(mc, I18n.format("aperture.gui.panels.duration"), (value) ->
        {
            this.updateDuration(value.longValue());
            this.editor.updatePlayerCurrently(0.0F);
            this.editor.updateProfile();
        });
        this.duration.trackpad.amplitude = 1.0F;
        this.duration.trackpad.min = 1;

        this.name.resizer().parent(this.area).set(0, 10, 100, 20);
        this.duration.resizer().parent(this.area).set(0, 35, 100, 20);

        this.children.add(this.name, this.duration);

        this.editor = editor;
    }

    protected void updateDuration(long value)
    {
        this.fixture.setDuration(value);
        this.editor.updateValues();
    }

    @Override
    public void select(T fixture, long duration)
    {
        this.fixture = fixture;

        this.name.setText(fixture.getName());
        this.duration.setValue(fixture.getDuration());
    }

    @Override
    public long currentOffset()
    {
        return this.editor.getProfile().calculateOffset(this.fixture);
    }

    public void editFixture(EntityPlayer entity)
    {
        this.select(this.fixture, -1);
        this.editor.updateProfile();
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        if (!this.name.field.isFocused())
        {
            GuiUtils.drawRightString(this.font, I18n.format("aperture.gui.panels.name"), this.name.area.x + this.name.area.w - 4, this.name.area.y + 6, 0xffaaaaaa);
        }
    }
}