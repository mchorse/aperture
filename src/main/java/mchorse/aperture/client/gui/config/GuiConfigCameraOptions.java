package mchorse.aperture.client.gui.config;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.Loader;

public class GuiConfigCameraOptions extends GuiAbstractConfigOptions
{
    private String title = I18n.format("aperture.gui.config.title");

    public GuiButtonElement<GuiCheckBox> outside;
    public GuiButtonElement<GuiCheckBox> minema;
    public GuiButtonElement<GuiCheckBox> spectator;
    public GuiButtonElement<GuiCheckBox> renderPath;
    public GuiButtonElement<GuiCheckBox> sync;
    public GuiButtonElement<GuiCheckBox> flight;
    public GuiButtonElement<GuiCheckBox> displayPosition;
    public GuiButtonElement<GuiCheckBox> minecrafttpTeleport;
    public GuiButtonElement<GuiCheckBox> tpTeleport;
    public GuiButtonElement<GuiCheckBox> ruleOfThirds;
    public GuiButtonElement<GuiCheckBox> letterBox;
    public GuiTextElement aspectRatio;

    public int max;

    @SuppressWarnings("unchecked")
    public GuiConfigCameraOptions(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.outside = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.outside"), Aperture.proxy.config.camera_outside, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("outside").get("camera_outside");

            prop.set(b.button.isChecked());

            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();

            if (b.button.isChecked())
            {
                ClientProxy.runner.attachOutside();
                this.editor.updatePlayerCurrently(0.0F);
            }
            else
            {
                ClientProxy.runner.detachOutside();
            }
        });

        this.minema = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.minema"), Aperture.proxy.config.camera_minema, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("camera_minema");

            prop.set(b.button.isChecked());
            this.saveConfig();
        });

        this.spectator = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.spectator"), Aperture.proxy.config.camera_spectator, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("camera_spectator");

            prop.set(b.button.isChecked());
            this.saveConfig();
        });

        this.renderPath = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.show_path"), Aperture.proxy.config.camera_profile_render, (b) ->
        {
            ClientProxy.renderer.toggleRender();
            b.button.setIsChecked(Aperture.proxy.config.camera_profile_render);
        });

        this.sync = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.sync"), this.editor.syncing, (b) ->
        {
            this.editor.syncing = b.button.isChecked();
        });

        this.flight = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.flight"), this.editor.flight.enabled, (b) ->
        {
            this.editor.flight.enabled = b.button.isChecked();
            this.editor.haveScrubbed = true;
        });

        this.displayPosition = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.display_info"), this.editor.displayPosition, (b) ->
        {
            this.editor.displayPosition = b.button.isChecked();
        });

        this.minecrafttpTeleport = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.minecrafttp_teleport"), Aperture.proxy.config.minecrafttp_teleport, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("minecrafttp_teleport");

            prop.set(b.button.isChecked());
            this.saveConfig();
        });

        this.tpTeleport = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.tp_teleport"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("tp_teleport");

            prop.set(b.button.isChecked());
            this.saveConfig();
        });

        this.ruleOfThirds = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.rule_of_thirds"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            this.editor.ruleOfThirds = b.button.isChecked();
        });

        this.letterBox = GuiButtonElement.checkbox(mc, I18n.format("aperture.gui.config.letter_box"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            this.editor.letterBox = b.button.isChecked();
        });

        this.aspectRatio = new GuiTextElement(mc, (v) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("aspect_ratio");

            prop.set(v);
            this.editor.setAspectRatio(v);
            this.saveConfig();
        });
        this.aspectRatio.setText(Aperture.proxy.config.aspect_ratio);

        /* Don't show that if Minema mod isn't present */
        if (Loader.isModLoaded("minema"))
        {
            this.children.add(this.minema);
        }

        this.children.add(this.outside, this.spectator, this.renderPath, this.sync, this.flight, this.displayPosition, this.ruleOfThirds, this.letterBox, this.aspectRatio);

        /* Show tp buttons if in multiplayer */
        if (!mc.isSingleplayer())
        {
            this.children.add(this.minecrafttpTeleport, this.tpTeleport);
        }

        int i = 0;

        for (IGuiElement element : this.children.elements)
        {
            if (element instanceof GuiButtonElement)
            {
                GuiButtonElement<GuiCheckBox> button = (GuiButtonElement<GuiCheckBox>) element;

                button.resizer().parent(this.area).set(4, 4 + i * 20 + 20, button.button.width, button.button.height);
                this.max = Math.max(this.max, button.button.width);
            }
            else if (element instanceof GuiElement)
            {
                ((GuiElement) element).resizer().parent(this.area).set(4, 4 + i * 20 + 20, 0, 18).w(1, -8);
            }

            i++;
        }
    }

    private void saveConfig()
    {
        Aperture.proxy.forge.save();
        Aperture.proxy.config.reload();
    }

    @Override
    public void update()
    {
        this.outside.button.setIsChecked(Aperture.proxy.config.camera_outside);
        this.minema.button.setIsChecked(Aperture.proxy.config.camera_minema);
        this.spectator.button.setIsChecked(Aperture.proxy.config.camera_spectator);
        this.renderPath.button.setIsChecked(Aperture.proxy.config.camera_profile_render);
        this.sync.button.setIsChecked(this.editor.syncing);
        this.flight.button.setIsChecked(this.editor.flight.enabled);
        this.displayPosition.button.setIsChecked(this.editor.displayPosition);
        this.minecrafttpTeleport.button.setIsChecked(Aperture.proxy.config.minecrafttp_teleport);
        this.tpTeleport.button.setIsChecked(Aperture.proxy.config.tp_teleport);
        this.ruleOfThirds.button.setIsChecked(this.editor.ruleOfThirds);
        this.letterBox.button.setIsChecked(this.editor.letterBox);
        this.aspectRatio.setText(Aperture.proxy.config.aspect_ratio);
    }

    @Override
    public int getWidth()
    {
        return Math.max(this.max + 8, this.font.getStringWidth(this.title) + 8);
    }

    @Override
    public int getHeight()
    {
        return this.children.elements.size() * 20 + 26;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.getX(1), this.area.y + 20, 0x88000000);
        this.font.drawString(this.title, this.area.x + 6, this.area.y + 7, 0xffffff, true);

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }
}