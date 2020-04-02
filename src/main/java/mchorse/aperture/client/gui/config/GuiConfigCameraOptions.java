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
import net.minecraftforge.common.config.Property;

public class GuiConfigCameraOptions extends GuiAbstractConfigOptions
{
    private String title = I18n.format("aperture.gui.config.title");

    public GuiToggleElement outside;
    public GuiToggleElement spectator;
    public GuiToggleElement renderPath;
    public GuiToggleElement sync;
    public GuiToggleElement flight;
    public GuiToggleElement displayPosition;
    public GuiToggleElement minecrafttpTeleport;
    public GuiToggleElement tpTeleport;
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

        this.outside = new GuiToggleElement(mc, I18n.format("aperture.gui.config.outside"), Aperture.proxy.config.camera_outside, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("outside").get("camera_outside");

            prop.set(b.isToggled());

            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();

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

        this.spectator = new GuiToggleElement(mc, I18n.format("aperture.gui.config.spectator"), Aperture.proxy.config.camera_spectator, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("camera_spectator");

            prop.set(b.isToggled());
            this.saveConfig();
        });

        this.renderPath = new GuiToggleElement(mc, I18n.format("aperture.gui.config.show_path"), Aperture.proxy.config.camera_profile_render, (b) ->
        {
            ClientProxy.renderer.toggleRender();
            b.toggled(Aperture.proxy.config.camera_profile_render);
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

        this.minecrafttpTeleport = new GuiToggleElement(mc, I18n.format("aperture.gui.config.minecrafttp_teleport"), Aperture.proxy.config.minecrafttp_teleport, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("minecrafttp_teleport");

            prop.set(b.isToggled());
            this.saveConfig();
        });

        this.tpTeleport = new GuiToggleElement(mc, I18n.format("aperture.gui.config.tp_teleport"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("tp_teleport");

            prop.set(b.isToggled());
            this.saveConfig();
        });

        this.ruleOfThirds = new GuiToggleElement(mc, I18n.format("aperture.gui.config.rule_of_thirds"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            this.editor.ruleOfThirds = b.isToggled();
        });

        this.letterBox = new GuiToggleElement(mc, I18n.format("aperture.gui.config.letter_box"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            this.editor.letterBox = b.isToggled();
        });

        this.aspectRatio = new GuiTextElement(mc, (v) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("aspect_ratio");

            prop.set(v);
            this.editor.setAspectRatio(v);
            this.saveConfig();
        });
        this.aspectRatio.setText(Aperture.proxy.config.aspect_ratio);

        this.repeat = new GuiToggleElement(mc, I18n.format("aperture.gui.config.repeat"), this.editor.repeat, (b) ->
        {
            this.editor.repeat = b.isToggled();
        });

        this.overlay = new GuiToggleElement(mc, I18n.format("aperture.gui.config.overlay"), Aperture.proxy.config.tp_teleport, (b) ->
        {
            Property prop = Aperture.proxy.forge.getCategory("overlay").get("camera_editor_overlay");

            prop.set(b.isToggled());
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
            Property prop = Aperture.proxy.forge.getCategory("overlay").get("camera_editor_overlay_rl");
            String texture = "";

            if (tag != JsonNull.INSTANCE)
            {
                texture = tag.toString();
            }

            prop.set(texture);
            this.saveConfig();
            this.editor.updateOverlay();
        });
        this.overlayPicker.setVisible(false);
        this.overlayPicker.flex().parent(this.editor.viewport).wh(1F, 1F);

        this.add(this.outside, this.spectator, this.renderPath, this.sync, this.flight, this.displayPosition, this.ruleOfThirds, this.letterBox, this.aspectRatio, this.repeat, this.overlay, this.pickOverlay);

        /* Show tp buttons if in multiplayer */
        if (!mc.isSingleplayer())
        {
            this.add(this.minecrafttpTeleport, this.tpTeleport);
        }

        for (IGuiElement element : this.getChildren())
        {
            ((GuiElement) element).flex().h(20);
        }

        ColumnResizer.apply(this, 5).vertical().stretch().padding(10);
    }

    private void saveConfig()
    {
        Aperture.proxy.forge.save();
        Aperture.proxy.config.reload();
    }

    @Override
    public void update()
    {
        this.outside.toggled(Aperture.proxy.config.camera_outside);
        this.spectator.toggled(Aperture.proxy.config.camera_spectator);
        this.renderPath.toggled(Aperture.proxy.config.camera_profile_render);
        this.sync.toggled(this.editor.syncing);
        this.flight.toggled(this.editor.flight.enabled);
        this.displayPosition.toggled(this.editor.displayPosition);
        this.minecrafttpTeleport.toggled(Aperture.proxy.config.minecrafttp_teleport);
        this.tpTeleport.toggled(Aperture.proxy.config.tp_teleport);
        this.ruleOfThirds.toggled(this.editor.ruleOfThirds);
        this.letterBox.toggled(this.editor.letterBox);
        this.aspectRatio.setText(Aperture.proxy.config.aspect_ratio);
        this.overlay.toggled(Aperture.proxy.config.camera_editor_overlay);
    }
}