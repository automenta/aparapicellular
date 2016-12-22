package com.aparapi.cellular;

import com.aparapi.Range;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Abstract kernel model which computes on a double-buffered byte[] array
 * The top half and the bottom half of the image are swapped each frame
 */
public abstract class BitmapU8Kernel extends BitmapKernel {
    final static int GPU_PARALLELISM = 32;

    public final BufferedImage image;
    public final Graphics gfx;
    protected int fromBase;
    protected int toBase;
    public final byte[] img;

    public BitmapU8Kernel(int w, int h) {
        super(w, h);

        // Buffer is twice the size as the screen.  We will alternate between mutating data from top to bottom
        // and bottom to top in alternate generation passses. The LifeKernel will track which pass is which
        image = new BufferedImage(w, h * 2, BufferedImage.TYPE_BYTE_GRAY);
        image.setAccelerationPriority(1f);

        gfx = image.getGraphics();

        toBase = 0;
        fromBase = height * width;

        range = Range.create(width * height, GPU_PARALLELISM);
        this.img = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    @Override
    public void next() {
        // swap fromBase and toBase
        final int swap = fromBase;
        fromBase = toBase;
        toBase = swap;

        execute(range);

    }

    @Override
    public void draw(Graphics g) {

        // We copy one half of the offscreen buffer to the viewer, we copy the half that we just mutated.
        if (fromBase == 0) {
            g.drawImage(image, 0, 0, width, height, 0, 0, width, height, null);
        } else {
            g.drawImage(image, 0, 0, width, height, 0, height, width, 2 * height, null);
        }

    }


    @Override
    public Graphics gfx() {
        return gfx;
    }
}
