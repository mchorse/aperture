package mchorse.aperture.client.gui.modals;

import java.util.function.Consumer;

import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiDelegateElement;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiPromptModal extends GuiModal
{
    public String label;
    public Consumer<String> callback;

    public GuiTextElement text;
    public GuiButtonElement<GuiButton> confirm;
    public GuiButtonElement<GuiButton> cancel;

    public GuiPromptModal(Minecraft mc, GuiDelegateElement<IGuiElement> parent, String label, Consumer<String> callback)
    {
        super(mc, parent);

        this.label = label;
        this.callback = callback;

        this.text = new GuiTextElement(mc, null);
        this.text.resizer().parent(this.area).set(10, 0, 0, 20).y(1, -55).w(1, -20);
        this.text.field.setFocused(true);

        this.confirm = GuiButtonElement.button(mc, I18n.format("aperture.gui.ok"), (b) -> this.send());
        this.confirm.resizer().parent(this.area).set(10, 0, 0, 20).y(1, -30).w(0.5F, -15);

        this.cancel = GuiButtonElement.button(mc, I18n.format("aperture.gui.cancel"), (b) -> this.parent.setDelegate(null));
        this.cancel.resizer().parent(this.area).set(10, 0, 0, 20).x(0.5F, 5).y(1, -30).w(0.5F, -15);

        this.children.add(this.text, this.confirm, this.cancel);
    }

    public GuiPromptModal setValue(String value)
    {
        this.text.setText(value);

        return this;
    }

    private void send()
    {
        String text = this.text.field.getText();

        if (!text.isEmpty())
        {
            this.parent.setDelegate(null);
            this.callback.accept(text);
        }
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        super.draw(tooltip, mouseX, mouseY, partialTicks);

        this.font.drawSplitString(this.label, this.area.x + 10, this.area.y + 10, this.area.w - 20, 0xffffff);
    }
}