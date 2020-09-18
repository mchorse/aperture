package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.TranslateModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiTranslateModifierPanel extends GuiAbstractModifierPanel<TranslateModifier>
{
    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public GuiTranslateModifierPanel(Minecraft mc, TranslateModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.x = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.translate.x = value;
            this.modifiers.editor.updateProfile();
        });
        this.x.tooltip(IKey.lang("aperture.gui.panels.x"));

        this.y = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.translate.y = value;
            this.modifiers.editor.updateProfile();
        });
        this.y.tooltip(IKey.lang("aperture.gui.panels.y"));

        this.z = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.translate.z = value;
            this.modifiers.editor.updateProfile();
        });
        this.z.tooltip(IKey.lang("aperture.gui.panels.z"));

        this.fields.add(Elements.row(mc, 5, 0, 20, this.x, this.y, this.z));
    }

    @Override
    public void resize()
    {
        super.resize();

        this.x.setValue((float) this.modifier.translate.x);
        this.y.setValue((float) this.modifier.translate.y);
        this.z.setValue((float) this.modifier.translate.z);
    }
}