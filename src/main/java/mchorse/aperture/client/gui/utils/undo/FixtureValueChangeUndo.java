package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.utils.undo.IUndo;
import mchorse.mclib.config.values.IConfigValue;

public class FixtureValueChangeUndo implements IUndo<CameraProfile>
{
    public int index;
    public String name;
    public Object oldValue;
    public Object newValue;

    private boolean mergable = true;

    public static FixtureValueChangeUndo create(GuiCameraEditor editor, IConfigValue value, Object newValue)
    {
        return create(editor, value, value.getValue(), newValue);
    }

    public static FixtureValueChangeUndo create(GuiCameraEditor editor, IConfigValue value, Object oldValue, Object newValue)
    {
        int index = editor.getProfile().getAll().indexOf(editor.getFixture());

        return new FixtureValueChangeUndo(index, value, oldValue, newValue);
    }

    public FixtureValueChangeUndo(int index, IConfigValue value, Object oldValue, Object newValue)
    {
        this(index, value.getId(), oldValue, newValue);
    }

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

    public FixtureValueChangeUndo unmergable()
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
        context.get(this.index).getProperty(this.name).setValue(this.oldValue);
    }

    @Override
    public void redo(CameraProfile context)
    {
        context.get(this.index).getProperty(this.name).setValue(this.newValue);
    }
}
