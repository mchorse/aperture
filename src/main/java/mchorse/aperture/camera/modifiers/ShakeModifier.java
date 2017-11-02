package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

public class ShakeModifier implements ICameraModifier
{
    @Expose
    public float shake;

    @Expose
    public float shakeAmount;

    public ShakeModifier(float shake, float shakeAmount)
    {
        this.shake = shake;
        this.shakeAmount = shakeAmount;
    }

    @Override
    public void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos)
    {
        float x = ticks / this.shake;

        float swingX = (float) (Math.sin(x) + Math.cos(x + Math.sin(x)));
        float swingY = (float) (Math.sin(x) + Math.cos(x + Math.sin(x)));

        pos.angle.yaw += swingX * this.shakeAmount;
        pos.angle.pitch += swingY * this.shakeAmount;
    }
}