package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;

public class GuiShakeModifierPanel extends GuiAbstractModifierPanel<ShakeModifier>
{
    public GuiTrackpadElement shake;
    public GuiTrackpadElement shakeAmount;
    public GuiActiveWidget active;

    public GuiShakeModifierPanel(Minecraft mc, ShakeModifier modifier, GuiModifiersManager panel)
    {
        super(mc, modifier, panel);

        this.shake = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.shake = value.floatValue();
            this.modifiers.editor.updateProfile();
        });
        this.shake.tooltip(IKey.lang("aperture.gui.modifiers.panels.shake"), Direction.BOTTOM);

        this.shakeAmount = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.shakeAmount = value.floatValue();
            this.modifiers.editor.updateProfile();
        });
        this.shakeAmount.tooltip(IKey.lang("aperture.gui.modifiers.panels.shake_amount"), Direction.BOTTOM);

        this.active = new GuiActiveWidget(mc, (value) ->
        {
            this.modifier.active = value;
            this.modifiers.editor.updateProfile();
        });

        this.fields.add(Elements.row(mc, 5, 0, 20, this.shake, this.shakeAmount), this.active);
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.shake.setValue(this.modifier.shake);
        this.shakeAmount.setValue(this.modifier.shakeAmount);
        this.active.value = this.modifier.active;
    }
}