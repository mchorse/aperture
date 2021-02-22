package mchorse.aperture.utils.undo;

import mchorse.aperture.client.gui.utils.undo.FixtureValueChangeUndo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CompoundUndo <T> implements IUndo<T>
{
    private List<IUndo<T>> undos = new ArrayList<IUndo<T>>();

    public CompoundUndo(IUndo<T>... undos)
    {
        for (IUndo<T> undo : undos)
        {
            if (undo == null)
            {
                continue;
            }

            this.undos.add(undo);
        }
    }

    public List<IUndo<T>> getUndos()
    {
        return this.undos;
    }

    public boolean has(Class<FixtureValueChangeUndo> clazz)
    {
        for (IUndo<T> undo : this.undos)
        {
            if (undo.getClass() == clazz)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isMergeable(IUndo<T> undo)
    {
        return undo instanceof CompoundUndo && ((CompoundUndo<T>) undo).undos.size() == this.undos.size();
    }

    @Override
    public void merge(IUndo<T> undo)
    {
        CompoundUndo<T> theUndo = (CompoundUndo<T>) undo;

        for (int i = 0, c = this.undos.size(); i < c; i++)
        {
            IUndo<T> otherChildUndo = theUndo.undos.get(i);
            IUndo<T> myUndo = this.undos.get(i);

            if (myUndo.isMergeable(otherChildUndo))
            {
                myUndo.merge(otherChildUndo);
            }
        }
    }

    @Override
    public void undo(T context)
    {
        for (int i = this.undos.size() - 1; i >= 0; i--)
        {
            this.undos.get(i).undo(context);
        }
    }

    @Override
    public void redo(T context)
    {
        for (IUndo<T> undo : this.undos)
        {
            undo.redo(context);
        }
    }
}