package mchorse.aperture.client.gui.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTexturePicker;
import mchorse.mclib.client.gui.utils.resizers.layout.ColumnResizer;
import mchorse.mclib.utils.resources.RLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiConfigCameraOptions extends GuiAbstractConfigOptions
{
    public GuiToggleElement outside;
    public GuiToggleElement spectator;
    public GuiToggleElement renderPath;
    public GuiToggleElement sync;
    public GuiToggleElement flight;
    public GuiToggleElement displayPosition;
    public GuiToggleElement essentialsTeleport;
    public GuiToggleElement ruleOfThirds;
    public GuiToggleElement letterBox;
    public GuiTextElement aspectRatio;
    public GuiToggleElement repeat;
    public GuiToggleElement overlay;
    public GuiButtonElement pickOverlay;
    public GuiTexturePicker overlayPicker;

    public GuiConfigCameraOptions(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.outside = new GuiToggleElement(mc, I18n.format("aperture.gui.config.outside"), Aperture.outside.get(), (b) ->
        {
            Aperture.outside.set(b.isToggled());
            this.saveConfig();

            if (b.isToggled())
            {
                ClientProxy.runner.attachOutside();
                this.editor.updatePlayerCurrently();
            }
            else
            {
                ClientProxy.runner.detachOutside();
            }
        });

        this.spectator = new GuiToggleElement(mc, I18n.format("aperture.gui.config.spectator"), Aperture.spectator.get(), (b) ->
        {
            Aperture.spectator.set(b.isToggled());
            this.saveConfig();
        });

        this.renderPath = new GuiToggleElement(mc, I18n.format("aperture.gui.config.show_path"), Aperture.profileRender.get(), (b) ->
        {
            ClientProxy.renderer.toggleRender();
            b.toggled(Aperture.profileRender.get());
        });

        this.sync = new GuiToggleElement(mc, I18n.format("aperture.gui.config.sync"), this.editor.syncing, (b) ->
        {
            this.editor.syncing = b.isToggled();
        });

        this.flight = new GuiToggleElement(mc, I18n.format("aperture.gui.config.flight"), this.editor.flight.enabled, (b) ->
        {
            this.editor.setFlight(b.isToggled());
        });

        this.displayPosition = new GuiToggleElement(mc, I18n.format("aperture.gui.config.display_info"), this.editor.displayPosition, (b) ->
        {
            this.editor.displayPosition = b.isToggled();
        });

        this.essentialsTeleport = new GuiToggleElement(mc, I18n.format("aperture.gui.config.minecrafttp_teleport"), Aperture.essentialsTeleport.get(), (b) ->
        {
            Aperture.essentialsTeleport.set(b.isToggled());
            this.saveConfig();
        });

        this.ruleOfThirds = new GuiToggleElement(mc, I18n.format("aperture.gui.config.rule_of_thirds"), this.editor.ruleOfThirds, (b) ->
        {
            this.editor.ruleOfThirds = b.isToggled();
        });

        this.letterBox = new GuiToggleElement(mc, I18n.format("aperture.gui.config.letter_box"), this.editor.letterBox, (b) ->
        {
            this.editor.letterBox = b.isToggled();
        });

        this.aspectRatio = new GuiTextElement(mc, (v) ->
        {
            Aperture.editorLetterboxAspect.set(v);
            this.editor.setAspectRatio(v);
            this.saveConfig();
        });
        this.aspectRatio.setText(Aperture.editorLetterboxAspect.get());

        this.repeat = new GuiToggleElement(mc, I18n.format("aperture.gui.config.repeat"), this.editor.repeat, (b) ->
        {
            this.editor.repeat = b.isToggled();
        });

        this.overlay = new GuiToggleElement(mc, I18n.format("aperture.gui.config.overlay"), Aperture.editorOverlay.get(), (b) ->
        {
            Aperture.editorOverlay.set(b.isToggled());
            this.saveConfig();
        });

        this.pickOverlay = new GuiButtonElement(mc, I18n.format("aperture.gui.config.pick_overlay"), (b) ->
        {
            this.overlayPicker.refresh();
            this.overlayPicker.fill(this.editor.overlayLocation);
            this.overlayPicker.setVisible(true);
        });

        this.overlayPicker = new GuiTexturePicker(mc, (rl) ->
        {
            JsonElement tag = RLUtils.writeJson(rl);
            String texture = "";

            if (tag != JsonNull.INSTANCE)
            {
                texture = tag.toString();
            }

            Aperture.editorOverlayRL.set(texture);
            this.saveConfig();
            this.editor.updateOverlay();
        });
        this.overlayPicker.setVisible(false);
        this.overlayPicker.flex().parent(this.editor.viewport).wh(1F, 1F);

        this.add(this.outside, this.spectator, this.renderPath, this.sync, this.flight, this.displayPosition, this.ruleOfThirds, this.letterBox, this.aspectRatio, this.repeat, this.overlay, this.pickOverlay);

        /* Show tp buttons if in multiplayer */
        if (!mc.isSingleplayer())
        {
            this.add(this.essentialsTeleport);
        }

        for (IGuiElement element : this.getChildren())
        {
            if (element instanceof GuiToggleElement)
            {
                ((GuiElement) element).flex().h(10);
            }
        }
    }

    private void saveConfig()
    {
        Aperture.outside.category.config.save();
    }

    @Override
    public void update()
    {
        this.outside.toggled(Aperture.outside.get());
        this.spectator.toggled(Aperture.spectator.get());
        this.renderPath.toggled(Aperture.profileRender.get());
        this.sync.toggled(this.editor.syncing);
        this.flight.toggled(this.editor.flight.enabled);
        this.displayPosition.toggled(this.editor.displayPosition);
        this.essentialsTeleport.toggled(Aperture.essentialsTeleport.get());
        this.ruleOfThirds.toggled(this.editor.ruleOfThirds);
        this.letterBox.toggled(this.editor.letterBox);
        this.aspectRatio.setText(Aperture.editorLetterboxAspect.get());
        this.overlay.toggled(Aperture.editorOverlay.get());
    }

    @Override
    public String getTitle()
    {
        return I18n.format("aperture.gui.config.title");
    }
}