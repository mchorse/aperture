package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.modifiers.DragModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiActiveWidget;
import mchorse.aperture.utils.undo.IUndo;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiDragModifierPanel extends GuiAbstractModifierPanel<DragModifier>
{
    public GuiTrackpadElement factor;
    public GuiActiveWidget active;

    public GuiDragModifierPanel(Minecraft mc, DragModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.factor = new GuiTrackpadElement(mc, (value) -> this.modifiers.editor.postUndo(this.undo(this.modifier.factor, value.floatValue())));
        this.factor.limit(0, 1).values(0.05F, 0.01F, 0.2F).increment(0.1F).tooltip(IKey.lang("aperture.gui.modifiers.panels.factor"));

        this.active = new GuiActiveWidget(mc, (value) ->
        {
            Position position = new Position(this.modifiers.editor.getCamera());

            this.modifiers.editor.postUndo(this.undo(this.modifier.active, value));
            this.modifier.reset(position);
        });

        this.fields.add(this.factor, this.active);
    }

    @Override
    public void handleUndo(IUndo<CameraProfile> undo, boolean redo)
    {
        super.handleUndo(undo, redo);

        Position position = new Position(this.modifiers.editor.getCamera());

        this.modifier.reset(position);
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.factor.setValue(this.modifier.factor.get());
        this.active.value = this.modifier.active.get();
    }
}