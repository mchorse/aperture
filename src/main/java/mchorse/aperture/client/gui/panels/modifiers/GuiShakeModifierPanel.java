package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.mclib.client.gui.framework.elements.GuiTrackpadElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiShakeModifierPanel extends GuiAbstractModifierPanel<ShakeModifier>
{
    public GuiTrackpadElement shake;
    public GuiTrackpadElement shakeAmount;
    public GuiActiveWidget active;

    public GuiShakeModifierPanel(Minecraft mc, ShakeModifier modifier, GuiModifiersManager panel)
    {
        super(mc, modifier, panel);

        this.shake = new GuiTrackpadElement(mc, I18n.format("aperture.gui.modifiers.panels.shake"), (value) ->
        {
            this.modifier.shake = value;
            this.modifiers.editor.updateProfile();
        });

        this.shakeAmount = new GuiTrackpadElement(mc, I18n.format("aperture.gui.modifiers.panels.shake_amount"), (value) ->
        {
            this.modifier.shakeAmount = value;
            this.modifiers.editor.updateProfile();
        });

        this.active = new GuiActiveWidget(mc, (value) ->
        {
            this.modifier.active = value;
            this.modifiers.editor.updateProfile();
        });

        this.shake.resizer().parent(this.area).set(5, 25, 0, 20).w(0.5F, -10);
        this.shakeAmount.resizer().parent(this.area).set(0, 25, 0, 20).x(0.5F, 5).w(0.5F, -10);
        this.active.resizer().parent(this.area).set(5, 50, 0, 20).w(1, -10);

        this.children.add(this.shake, this.shakeAmount, this.active);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.shake.setValue(this.modifier.shake);
        this.shakeAmount.setValue(this.modifier.shakeAmount);
        this.active.value = this.modifier.active;
    }

    @Override
    public int getHeight()
    {
        return 75;
    }
}