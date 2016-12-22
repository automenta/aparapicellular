package com.aparapi.cellular;

import com.aparapi.Range;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Abstraact kernel model which Computes directly on a single float[] array
 * that gets copied to a byte[] after each frame for display
 */
public abstract class BitmapFloatKernel extends BitmapKernel {


    public final byte[] img;
    public final float[] fimg;
    public final int width;
    public final int height;
    public final BufferedImage image;
    public final Graphics gfx;


    public BitmapFloatKernel(int w, int h, int blockSize) {
        super(w, h);

        // Buffer is twice the size as the screen.  We will alternate between mutating data from top to bottom
        // and bottom to top in alternate generation passses. The LifeKernel will track which pass is which
        image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        image.setAccelerationPriority(1f);


        gfx = image.getGraphics();

        width = w;
        height = h;

        range = Range.create(width * height, blockSize);
        this.img = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        fimg = new float[width*height];
    }

    @Override
    final public void run() {

        final int at = getGlobalId();

        float next = compute(at);

        fimg[at] = next;
        img[at] = (byte)(256f * next); //round float to 8bit grayscale

    }

    abstract protected float compute(int at);

    @Override
    public Graphics gfx() {
        return gfx;
    }

    @Override
    public void draw(Graphics g) {

        g.drawImage(image, 0, 0, width, height, 0, 0, width, height, null);
    }

    @Override
    public void next() {

        execute(range);
//
//        for (int i = 0 ;i < fimg.length; i++)
//            img[i] = (byte)(fimg[i] * 255f);
    }


}
