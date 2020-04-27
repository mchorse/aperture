package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.NullFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiNullFixturePanel extends GuiAbstractFixturePanel<NullFixture>
{
    public GuiToggleElement previous;

    public GuiNullFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.previous = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.previous"), false, (b) -> this.fixture.previous = b.isToggled());
        this.previous.flex().relative(this.duration).set(0, 0, 0, 20).y(1, 5).w(1, 0);

        this.add(this.previous);
    }

    @Override
    public void select(NullFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.previous.toggled(this.fixture.previous);
    }
}