package mchorse.aperture.client.gui;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.curves.AbstractCurve;
import mchorse.aperture.camera.values.ValueCurves;
import mchorse.aperture.client.gui.utils.GuiCameraEditorKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiSearchListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.keyframes.KeyframeChannel;
import net.minecraft.client.Minecraft;

public class GuiCurves extends GuiElement
{
    public GuiCameraEditor editor;
    public GuiCameraEditorKeyframesGraphEditor keyframes;
    public GuiButtonElement curveSelector;
    public GuiSearchCurveList curveList;
    
    public String selected;

    public GuiCurves(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        
        this.curveSelector = new GuiButtonElement(mc, IKey.str("Curves"), btn -> this.toggleCurveList());
        this.curveSelector.flex().relative(this).y(1f).w(1f).anchorY(1f);
        
        this.keyframes = new GuiCameraEditorKeyframesGraphEditor(mc, editor);
        this.keyframes.graph.global = true;
        this.keyframes.flex().relative(this).w(1F).hTo(this.curveSelector.area);
        
        this.curveList = new GuiSearchCurveList(mc, list -> this.selectCurve(list.get(0)));
        this.curveList.flex().relative(this.keyframes).wh(1F, 1F);
        this.curveList.list.background(0xC0000000);
        this.curveList.list.scroll.scrollSpeed = 20;

        this.add(this.curveSelector, this.keyframes, this.curveList);
    }

    public void updateKeyframeEditor()
    {
        this.keyframes.updateConverter();
    }

    public void updateDuration()
    {
        this.keyframes.graph.duration = (int) this.editor.getProfile().getDuration();
    }

    public void update()
    {
        this.updateCurveList();
        
        if (!this.curveList.list.getList().contains(this.selected))
        {
            selectCurve(this.curveList.list.getList().get(0));
        }
        else
        {
            selectCurve(this.selected);
        }
    }

    @Override
    public void draw(GuiContext context)
    {


        super.draw(context);
    }

    public void selectCurve(String id)
    {
        this.selected = id;
        
        this.curveList.list.setCurrent(this.selected);
        this.curveList.setVisible(false);
        this.curveSelector.label.set(ClientProxy.curveManager.curves.get(id).getTranslatedName());

        CameraProfile profile = this.editor.getProfile();
        ValueCurves channels = profile.curves;
        KeyframeChannel channel = channels.get(id);

        if (channel == null)
        {
            channel = new KeyframeChannel();
            channels.put(id, channel);
        }

        updateDuration();

        this.keyframes.setChannel(profile.getProperty(profile.curves.id + "." + id), Color.HSBtoRGB(new Random().nextFloat(), 1.0f, 1.0f));
    }
    
    public void updateCurveList()
    {
        this.curveList.list.clear();
        this.curveList.list.add(ClientProxy.curveManager.curves.keySet());
        this.curveList.list.sort();
        this.curveList.resize();
    }
    
    public void toggleCurveList()
    {
        this.curveList.toggleVisible();

        if (this.curveList.isVisible())
        {
            this.updateCurveList();
            this.curveList.list.setCurrentScroll(this.selected);
            this.curveList.list.filter(this.curveList.search.field.getText());
        }
    }
    
    public class GuiSearchCurveList extends GuiSearchListElement<String>
    {
        public GuiSearchCurveList(Minecraft mc, Consumer<List<String>> callback)
        {
            super(mc, callback);
        }

        @Override
        protected GuiListElement<String> createList(Minecraft mc, Consumer<List<String>> callback)
        {
            return new GuiCurveList(mc, callback);
        }
    }
    
    public class GuiCurveList extends GuiListElement<String>
    {
        public ValueCurves curves;
        
        public GuiCurveList(Minecraft mc, Consumer<List<String>> callback)
        {
            super(mc, callback);
        }

        @Override
        protected String elementToString(String element)
        {
            KeyframeChannel channel = GuiCurves.this.editor.getProfile().curves.get(element);
            String hasKey = channel != null && !channel.isEmpty() ? "*" : "";
            
            AbstractCurve curve = ClientProxy.curveManager.curves.get(element);
            
            if (curve == null)
            {
                return hasKey + element;
            }
            else
            {
                return hasKey + curve.getTranslatedName();
            }
        }

        @Override
        protected boolean sortElements()
        {
            this.list.sort((a, b) ->
            {
                KeyframeChannel channel;
                
                channel = GuiCurves.this.editor.getProfile().curves.get(a);
                int aa = channel != null && !channel.isEmpty() ? 0 : 1;
                
                channel = GuiCurves.this.editor.getProfile().curves.get(b);
                int bb = channel != null && !channel.isEmpty() ? 0 : 1;
                
                return aa - bb;
            });
            
            return true;
        }
    }
}