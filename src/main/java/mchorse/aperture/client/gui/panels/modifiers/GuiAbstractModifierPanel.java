package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.modifiers.widgets.GuiEnvelope;
import mchorse.aperture.utils.APIcons;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Icons;
import net.minecraft.client.Minecraft;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> extends GuiElement
{
    public T modifier;
    public GuiModifiersManager modifiers;

    public String title;
    public int color;

    public GuiIconElement enable;
    public GuiIconElement remove;
    public GuiIconElement moveUp;
    public GuiIconElement moveDown;
    public GuiIconElement copy;
    public GuiIconElement envelope;

    public GuiElement header;
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
            this.modifier.enabled = !this.modifier.enabled;
            this.updateEnable();
            this.modifiers.editor.updateProfile();
        });
        this.remove = new GuiIconElement(mc, Icons.CLOSE, (b) -> this.modifiers.removeModifier(this));
        this.moveUp = new GuiIconElement(mc, Icons.MOVE_UP, (b) -> this.modifiers.moveModifier(this, -1));
        this.moveDown = new GuiIconElement(mc, Icons.MOVE_DOWN, (b) -> this.modifiers.moveModifier(this, 1));
        this.copy = new GuiIconElement(mc, Icons.COPY, (b) -> this.modifiers.setClipboard(this.modifier));
        this.envelope = new GuiIconElement(mc, APIcons.ENVELOPE, (b) -> this.toggleEnvelopes());

        this.header = new GuiElement(mc);
        this.header.flex().h(20);

        this.remove.flex().relative(this.header).set(0, 2, 16, 16).x(1, -18);
        this.enable.flex().relative(this.remove).set(-20, 0, 16, 16);
        this.moveUp.flex().relative(this.enable).set(-20, 0, 16, 8);
        this.moveDown.flex().relative(this.enable).set(-20, 8, 16, 8);
        this.copy.flex().relative(this.moveUp).set(-20, 0, 16, 16);
        this.envelope.flex().relative(this.copy).set(-20, 0, 16, 16);

        this.header.add(this.remove, this.enable, this.moveUp, this.moveDown, this.copy, this.envelope);

        this.flex().column(0).vertical().stretch();
        this.add(this.header, this.fields);

        this.title = ModifierRegistry.CLIENT.get(modifier.getClass()).getTitle();
        this.color = ModifierRegistry.CLIENT.get(this.modifier.getClass()).color.getRGBColor();
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
    }

    @Override
    public void resize()
    {
        super.resize();

        this.updateEnable();
    }

    protected void updateEnable()
    {
        this.enable.both(this.modifier.enabled ? Icons.UNLOCKED : Icons.LOCKED);
    }

    @Override
    public void draw(GuiContext context)
    {
        this.area.draw(0x88000000 + this.color);
        this.font.drawStringWithShadow(this.title, this.header.area.x + 5, this.header.area.y + 10 - this.font.FONT_HEIGHT / 2, 0xffffff);

        this.header.setVisible(this.area.isInside(context.mouseX, context.mouseY));

        super.draw(context);
    }
}