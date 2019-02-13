package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.NullFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiNullFixturePanel extends GuiAbstractFixturePanel<NullFixture>
{
    public GuiButtonElement<GuiCheckBox> previous;

    public GuiNullFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.previous = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.panels.previous"), false, (b) ->
        {
            this.fixture.previous = b.button.isChecked();
        });
        this.previous.resizer().relative(this.duration.resizer()).set(0, 25, this.previous.button.width, 11);

        this.children.add(this.previous);
    }

    @Override
    public void select(NullFixture fixture, long duration)
    {
        super.select(fixture, duration);

        this.previous.button.setIsChecked(this.fixture.previous);
    }
}