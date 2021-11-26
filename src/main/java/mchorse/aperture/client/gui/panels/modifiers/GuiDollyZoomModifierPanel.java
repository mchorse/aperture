package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.DollyZoomModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;

public class GuiDollyZoomModifierPanel extends GuiAbstractModifierPanel<DollyZoomModifier>
{
    public GuiTrackpadElement focus;

    public GuiDollyZoomModifierPanel(Minecraft mc, DollyZoomModifier modifier, GuiModifiersManager panel)
    {
        super(mc, modifier, panel);

        this.focus = new GuiTrackpadElement(mc, (value) -> this.modifiers.editor.postUndo(this.undo(this.modifier.focus, value.floatValue()))).limit(0.0);
        this.focus.tooltip(IKey.lang("aperture.gui.modifiers.panels.focus"), Direction.BOTTOM);

        this.fields.add(this.focus);
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.focus.setValue(this.modifier.focus.get());
    }
}
