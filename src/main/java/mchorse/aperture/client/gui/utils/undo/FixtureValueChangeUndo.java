package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.undo.IUndo;

public class FixtureValueChangeUndo extends CameraProfileUndo
{
    public int index;
    public String name;
    public Object oldValue;
    public Object newValue;

    private boolean mergable = true;

    public FixtureValueChangeUndo(int index, String name, Object oldValue, Object newValue)
    {
        this.index = index;
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getIndex()
    {
        return this.index;
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public IUndo<CameraProfile> noMerging()
    {
        this.mergable = false;

        return this;
    }

    @Override
    public boolean isMergeable(IUndo<CameraProfile> undo)
    {
        if (!this.mergable)
        {
            return false;
        }

        if (undo instanceof FixtureValueChangeUndo)
        {
            FixtureValueChangeUndo valueUndo = (FixtureValueChangeUndo) undo;

            return this.name.equals(valueUndo.getName()) && this.index == valueUndo.getIndex();
        }

        return false;
    }

    @Override
    public void merge(IUndo<CameraProfile> undo)
    {
        if (undo instanceof FixtureValueChangeUndo)
        {
            FixtureValueChangeUndo prop = (FixtureValueChangeUndo) undo;

            this.newValue = prop.newValue;
        }
    }

    @Override
    public void undo(CameraProfile context)
    {
        Value value = context.getProperty(this.name);

        value.setValue(this.oldValue);
    }

    @Override
    public void redo(CameraProfile context)
    {
        Value value = context.getProperty(this.name);

        value.setValue(this.newValue);
    }
}
