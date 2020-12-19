package mchorse.aperture.client.gui.config;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTextElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTexturePicker;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

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
    public GuiToggleElement crosshair;
    public GuiToggleElement letterBox;
    public GuiTextElement aspectRatio;
    public GuiToggleElement loop;
    public GuiToggleElement overlay;
    public GuiButtonElement pickOverlay;
    public GuiTexturePicker overlayPicker;
    public GuiToggleElement hideChat;

    public GuiConfigCameraOptions(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.outside = new GuiToggleElement(mc, Aperture.outside, (b) ->
        {
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

        this.spectator = new GuiToggleElement(mc, Aperture.spectator);
        this.renderPath = new GuiToggleElement(mc, Aperture.profileRender);
        this.sync = new GuiToggleElement(mc, Aperture.editorSync);

        this.flight = new GuiToggleElement(mc, IKey.lang("aperture.gui.config.flight"), this.editor.flight.isFlightEnabled(), (b) ->
        {
            this.editor.setFlight(b.isToggled());
        });
        this.flight.tooltip(IKey.lang("aperture.gui.config.flight_tooltip"));

        this.displayPosition = new GuiToggleElement(mc, Aperture.editorDisplayPosition);
        this.essentialsTeleport = new GuiToggleElement(mc, Aperture.essentialsTeleport);
        this.ruleOfThirds = new GuiToggleElement(mc, Aperture.editorRuleOfThirds);
        this.crosshair = new GuiToggleElement(mc, Aperture.editorCrosshair);
        this.letterBox = new GuiToggleElement(mc, Aperture.editorLetterbox);
        this.aspectRatio = new GuiTextElement(mc, Aperture.editorLetterboxAspect, this.editor::setAspectRatio);
        this.aspectRatio.setText(Aperture.editorLetterboxAspect.get());

        this.loop = new GuiToggleElement(mc, Aperture.editorLoop);
        this.overlay = new GuiToggleElement(mc, Aperture.editorOverlay);

        this.pickOverlay = new GuiButtonElement(mc, IKey.lang("aperture.gui.config.pick_overlay"), (b) ->
        {
            this.overlayPicker.refresh();
            this.overlayPicker.fill(this.editor.overlayLocation);
            this.overlayPicker.resize();

            this.editor.top.add(this.overlayPicker);
        });

        this.overlayPicker = new GuiTexturePicker(mc, (rl) ->
        {
            Aperture.editorOverlayRL.set(rl);
            this.editor.updateOverlay();
        });
        this.overlayPicker.flex().relative(this.editor.viewport).wh(1F, 1F);
        this.hideChat = new GuiToggleElement(mc, Aperture.editorHideChat);

        this.add(this.outside, this.spectator, this.renderPath, this.sync, this.flight, this.displayPosition, this.ruleOfThirds, this.crosshair, this.letterBox, this.aspectRatio, this.loop, this.overlay, this.pickOverlay, this.hideChat);

        if (!mc.isSingleplayer())
        {
            this.add(this.essentialsTeleport);
        }

        for (IGuiElement element : this.getChildren())
        {
            if (element instanceof GuiToggleElement)
            {
                ((GuiElement) element).flex().h(16);
            }
        }
    }

    @Override
    public void update()
    {
        this.outside.toggled(Aperture.outside.get());
        this.spectator.toggled(Aperture.spectator.get());
        this.renderPath.toggled(Aperture.profileRender.get());
        this.sync.toggled(Aperture.editorSync.get());
        this.loop.toggled(Aperture.editorLoop.get());
        this.flight.toggled(this.editor.flight.isFlightEnabled());
        this.displayPosition.toggled(Aperture.editorDisplayPosition.get());
        this.essentialsTeleport.toggled(Aperture.essentialsTeleport.get());
        this.ruleOfThirds.toggled(Aperture.editorRuleOfThirds.get());
        this.crosshair.toggled(Aperture.editorCrosshair.get());
        this.letterBox.toggled(Aperture.editorLetterbox.get());
        this.aspectRatio.setText(Aperture.editorLetterboxAspect.get());
        this.overlay.toggled(Aperture.editorOverlay.get());
        this.hideChat.toggled(Aperture.editorHideChat.get());
    }

    @Override
    public IKey getTitle()
    {
        return IKey.lang("aperture.gui.config.title");
    }
}