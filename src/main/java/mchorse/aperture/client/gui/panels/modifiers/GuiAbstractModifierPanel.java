package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiEnvelope;
import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;
import mchorse.aperture.utils.APIcons;
import mchorse.aperture.utils.undo.IUndo;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> extends GuiElement
{
    public T modifier;
    public GuiModifiersManager modifiers;

    public GuiLabel title;
    public int color;

    public GuiIconElement enable;
    public GuiIconElement remove;
    public GuiIconElement moveUp;
    public GuiIconElement moveDown;
    public GuiIconElement copy;
    public GuiIconElement envelope;

    public GuiElement header;
    public GuiElement buttons;
    public GuiElement fields;
    public GuiEnvelope envelopes;

    public GuiAbstractModifierPanel(Minecraft mc, T modifier, GuiModifiersManager modifiers)
    {
        super(mc);

        this.modifier = modifier;
        this.modifiers = modifiers;

        this.fields = new GuiElement(mc);
        this.fields.flex().column(5).vertical().stretch().height(20).padding(10);

        this.envelopes = new GuiEnvelope(mc, this);
        this.enable = new GuiIconElement(mc, Icons.NONE, (b) ->
        {
            this.modifiers.editor.postUndo(this.undo(this.modifier.enabled, !this.modifier.enabled.get()));
            this.updateEnable();
        });
        this.enable.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.lock"));
        this.remove = new GuiIconElement(mc, Icons.REMOVE, (b) -> this.modifiers.removeModifier(this));
        this.remove.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.remove"));
        this.moveUp = new GuiIconElement(mc, Icons.MOVE_UP, (b) -> this.modifiers.moveModifier(this, -1));
        this.moveUp.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.move_up"));
        this.moveDown = new GuiIconElement(mc, Icons.MOVE_DOWN, (b) -> this.modifiers.moveModifier(this, 1));
        this.moveDown.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.move_down"));
        this.copy = new GuiIconElement(mc, Icons.COPY, (b) -> this.modifiers.setClipboard(this.modifier));
        this.copy.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.copy"));
        this.envelope = new GuiIconElement(mc, APIcons.ENVELOPE, (b) -> this.toggleEnvelopes());
        this.envelope.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.envelope"));

        this.header = new GuiElement(mc);
        this.header.flex().h(15);

        this.buttons = new GuiElement(mc);
        this.buttons.flex().relative(this.header).wh(1F, 1F);

        this.remove.flex().relative(this.header).set(-20, 5, 20, 20).x(1, -30);
        this.enable.flex().relative(this.remove).set(-20, 0, 20, 20);
        this.moveUp.flex().relative(this.enable).set(-20, 2, 20, 8);
        this.moveDown.flex().relative(this.enable).set(-20, 10, 20, 8);
        this.copy.flex().relative(this.moveUp).set(-20, -2, 20, 20);
        this.envelope.flex().relative(this.copy).set(-20, 0, 20, 20);

        this.buttons.add(this.remove, this.enable, this.moveUp, this.moveDown, this.copy, this.envelope);

        this.flex().column(0).vertical().stretch();
        this.add(this.header, this.fields);

        String key = ModifierRegistry.CLIENT.get(modifier.getClass()).title;

        this.title = Elements.label(IKey.lang(key));
        this.title.anchor(0, 0.5F).flex().relative(this.header).x(10).y(5).w(0.5F, -10).h(20);
        this.title.tooltip(IKey.lang(key + "_description"));
        this.color = ModifierRegistry.CLIENT.get(this.modifier.getClass()).color.getRGBColor();

        this.header.add(this.title, this.buttons);
    }

    protected IUndo<CameraProfile> undo(IConfigValue property, Object value)
    {
        return this.modifiers.undo(this.modifier, property, value);
    }

    public void initiate()
    {
        this.envelopes.initiate();
    }

    public void fillData()
    {
        this.envelopes.fillData();
        this.updateEnable();
    }

    public void updateDuration()
    {
        this.envelopes.updateDuration();
    }

    private void toggleEnvelopes()
    {
        if (this.envelopes.hasParent())
        {
            this.envelopes.removeFromParent();
            this.add(this.fields);
        }
        else
        {
            this.fields.removeFromParent();
            this.add(this.envelopes);
        }

        this.parent.resize();
        this.envelopes.wasToggled();
    }

    private void updateEnable()
    {
        this.enable.both(this.modifier.enabled.get() ? Icons.UNLOCKED : Icons.LOCKED);
    }

    @Override
    public void draw(GuiContext context)
    {
        if (this.modifier.enabled.get())
        {
            this.area.draw(0x88000000 + this.color);
        }
        else
        {
            ColorUtils.bindColor(0x88000000 + this.color);
            Icons.DISABLED.renderArea(this.area.x, this.area.y, this.area.w, this.area.h);
        }

        this.buttons.setVisible(this.area.isInside(context.mouseX, context.mouseY));

        super.draw(context);
    }
}