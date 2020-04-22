package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.camera.FixtureRegistry;
import mchorse.aperture.camera.FixtureRegistry.FixtureInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

import java.util.function.Consumer;

/**
 * Fixtures popup
 * 
 * Allows to select the type of camera fixture the user wants to create.
 */
public class GuiFixturesPopup extends GuiElement
{
    public Consumer<AbstractFixture> callback;

    public GuiFixturesPopup(Minecraft mc, Consumer<AbstractFixture> callback)
    {
        super(mc);

        this.callback = callback;

        int i = 0;

        for (FixtureInfo info : FixtureRegistry.CLIENT.values())
        {
            byte type = info.type;
            int color = info.color.getRGBAColor();
            GuiButtonElement button = new GuiButtonElement(mc, I18n.format(info.title), (b) ->
            {
                this.actionPerformed(type);
            });

            button.color(color).flex().relative(this).set(2, i * 20 + 2, 0, 20).w(1, -4);
            this.add(button);

            i++;
        }
    }

    @Override
    public void resize()
    {
        this.flex().h(this.getChildren().size() * 20 + 4);

        super.resize();
    }

    /**
     * Select a fixture
     */
    private void actionPerformed(byte type)
    {
        long duration = Aperture.duration.get();
        AbstractFixture fixture = null;

        try
        {
            fixture = FixtureRegistry.fromType(type, duration);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (fixture != null && this.callback != null)
        {
            this.callback.accept(fixture);
        }
    }

    @Override
    public void draw(GuiContext context)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);

        super.draw(context);
    }
}