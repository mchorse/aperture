package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.RemapperModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.aperture.client.gui.utils.GuiTextHelpElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

public class GuiRemapperModifierPanel extends GuiAbstractModifierPanel<RemapperModifier>
{
    public GuiToggleElement keyframes;
    public GuiCameraEditorKeyframesGraphEditor channel;
    public GuiTextHelpElement expression;
    public GuiIconElement help;

    public GuiRemapperModifierPanel(Minecraft mc, RemapperModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.keyframes = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.keyframes"), (b) ->
        {
            this.modifiers.editor.postUndo(this.undo(this.modifier.keyframes, b.isToggled()));
            this.toggleKeyframes(b.isToggled());
        });

        this.channel = new GuiCameraEditorKeyframesGraphEditor(mc, modifiers.editor);
        this.channel.flex().h(200);
        this.expression = new GuiTextHelpElement(mc, 500, (str) ->
        {
            this.modifiers.editor.postUndo(this.undo(this.modifier.expression, str));
            this.expression.field.setTextColor(!this.modifier.expression.isErrored() ? 0xffffff : 0xff2244);
        });
        this.expression.link("https://github.com/mchorse/aperture/wiki/Math-Expressions").tooltip(IKey.lang("aperture.gui.modifiers.panels.math"));

        this.fields.add(this.expression, this.keyframes);
    }

    public void initiate()
    {
        super.initiate();

        this.updateDuration();
        this.channel.graph.resetView();
        this.channel.updateConverter();
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.keyframes.toggled(this.modifier.keyframes.get());
        this.channel.setChannel(this.modifier.channel, 0x0088ff);
        this.expression.setText(this.modifier.expression.toString());
        this.expression.field.setTextColor(0xffffff);

        this.keyframes.toggled(this.modifier.keyframes.get());
        this.toggleKeyframes(this.modifier.keyframes.get());
    }

    @Override
    public void updateDuration()
    {
        super.updateDuration();

        AbstractFixture fixture = this.modifiers.editor.getFixture();

        this.channel.graph.duration = fixture == null ? 30 : (int) fixture.getDuration();
    }

    private void toggleKeyframes(boolean toggled)
    {
        this.fields.removeAll();
        this.fields.add(toggled ? this.channel : this.expression, this.keyframes);

        if (this.getParent() != null)
        {
            this.getParent().resize();
        }
    }

    @Override
    public void resize()
    {
        super.resize();

        this.updateDuration();
        this.channel.graph.resetView();
    }
}