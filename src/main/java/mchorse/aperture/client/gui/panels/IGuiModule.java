package mchorse.aperture.client.gui.panels;

/**
 * GUI module interface
 *
 * This interface provides methods for ready-to-use signatures of methods which
 * are usually used in GUI screens for input handling, drawing and initiation.
 */
public interface IGuiModule
{
    /**
     * Mouse was clicked
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton);

    /**
     * Mouse was released
     */
    public void mouseReleased(int mouseX, int mouseY, int state);

    /**
     * Key was typed
     */
    public void keyTyped(char typedChar, int keyCode);

    /**
     * Draw its components on the screen
     */
    public void draw(int mouseX, int mouseY, float partialTicks);
}