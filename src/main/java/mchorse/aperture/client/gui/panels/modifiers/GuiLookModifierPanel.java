package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.LookModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.utils.GuiTextHelpElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiContextMenu;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.RayTracing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class GuiLookModifierPanel extends GuiAbstractModifierPanel<LookModifier>
{
    public static final String TARGET_SELECTOR_HELP = "https://minecraft.gamepedia.com/Commands#Target_selector_arguments";

    public GuiTextHelpElement selector;

    public GuiTrackpadElement blockX;
    public GuiTrackpadElement blockY;
    public GuiTrackpadElement blockZ;

    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;

    public GuiToggleElement relative;
    public GuiToggleElement atBlock;
    public GuiToggleElement forward;

    public GuiElement row;

    public GuiLookModifierPanel(Minecraft mc, LookModifier modifier, GuiModifiersManager modifiers)
    {
        super(mc, modifier, modifiers);

        this.selector = new GuiTextHelpElement(mc, 500, (str) ->
        {
            this.modifier.selector.set(str);
            this.modifier.tryFindingEntity();
            this.modifiers.editor.updateProfile();
        });
        this.selector.link(TARGET_SELECTOR_HELP).tooltip(IKey.lang("aperture.gui.panels.selector"));

        this.blockX = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.block.get().x = value;
            this.modifiers.editor.updateProfile();
        });
        this.blockX.tooltip(IKey.lang("aperture.gui.panels.x"));

        this.blockY = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.block.get().y = value;
            this.modifiers.editor.updateProfile();
        });
        this.blockY.tooltip(IKey.lang("aperture.gui.panels.y"));

        this.blockZ = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.block.get().z = value;
            this.modifiers.editor.updateProfile();
        });
        this.blockZ.tooltip(IKey.lang("aperture.gui.panels.z"));

        this.x = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.offset.get().x = value;
            this.modifiers.editor.updateProfile();
        });
        this.x.tooltip(IKey.lang("aperture.gui.panels.x"));

        this.y = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.offset.get().y = value;
            this.modifiers.editor.updateProfile();
        });
        this.y.tooltip(IKey.lang("aperture.gui.panels.y"));

        this.z = new GuiTrackpadElement(mc, (value) ->
        {
            this.modifier.offset.get().z = value;
            this.modifiers.editor.updateProfile();
        });
        this.z.tooltip(IKey.lang("aperture.gui.panels.z"));

        this.relative = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.relative"), false, (b) ->
        {
            this.modifier.relative.set(b.isToggled());
            this.modifiers.editor.updateProfile();
        });
        this.relative.tooltip(IKey.lang("aperture.gui.modifiers.panels.relative_tooltip"));

        this.atBlock = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.at_block"), false, (b) ->
        {
            this.modifier.atBlock.set(b.isToggled());
            this.updateVisibility(true);
            this.modifiers.editor.updateProfile();
        });

        this.forward = new GuiToggleElement(mc, IKey.lang("aperture.gui.modifiers.panels.forward"), false, (b) ->
        {
            this.modifier.forward.set(b.isToggled());
            this.modifiers.editor.updateProfile();
        });
        this.forward.tooltip(IKey.lang("aperture.gui.modifiers.panels.forward_tooltip"));

        this.row = Elements.row(mc, 5, 0, 20, this.blockX, this.blockY, this.blockZ);
        this.updateVisibility(false);
        this.fields.add(Elements.row(mc, 5, 0, 20,  this.relative, this.atBlock));
        this.fields.add(Elements.row(mc, 5, 0, 20, this.x, this.y, this.z));
        this.fields.add(this.forward);
    }

    @Override
    public void fillData()
    {
        super.fillData();

        this.selector.setText(this.modifier.selector.get());
        this.blockX.setValue(this.modifier.block.get().x);
        this.blockY.setValue(this.modifier.block.get().y);
        this.blockZ.setValue(this.modifier.block.get().z);
        this.x.setValue(this.modifier.offset.get().x);
        this.y.setValue(this.modifier.offset.get().y);
        this.z.setValue(this.modifier.offset.get().z);
        this.relative.toggled(this.modifier.relative.get());
        this.atBlock.toggled(this.modifier.atBlock.get());
        this.forward.toggled(this.modifier.forward.get());

        this.updateVisibility(false);
    }

    @Override
    public GuiContextMenu createContextMenu(GuiContext context)
    {
        if (this.modifier.atBlock.get())
        {
            return new GuiSimpleContextMenu(this.mc)
                .action(Icons.VISIBLE, IKey.lang("aperture.gui.panels.context.look_coords"), () -> this.rayTrace(false))
                .action(Icons.BLOCK, IKey.lang("aperture.gui.panels.context.look_block"), () -> this.rayTrace(true));
        }

        return super.createContextMenu(context);
    }

    private void rayTrace(boolean center)
    {
        RayTraceResult result = center ? RayTracing.rayTrace(this.mc.player, 128, 0F) : RayTracing.rayTraceWithEntity(this.mc.player, 128);

        if (result != null)
        {
            if (center && result.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = result.getBlockPos();

                this.modifier.block.get().set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                this.fillData();
            }
            else if (!center && result.typeOfHit != RayTraceResult.Type.MISS)
            {
                Vec3d vec = result.hitVec;

                this.modifier.block.get().set(vec.x, vec.y, vec.z);
                this.fillData();
            }
        }
    }

    private void updateVisibility(boolean resize)
    {
        boolean atBlock = this.modifier.atBlock.get();

        this.row.removeFromParent();
        this.selector.removeFromParent();

        if (atBlock)
        {
            this.fields.prepend(this.row);
        }
        else
        {
            this.fields.prepend(this.selector);
        }

        if (resize)
        {
            this.getParent().resize();
        }
    }
}