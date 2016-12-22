package com.aparapi.cellular;

import com.aparapi.Kernel;
import com.aparapi.Range;

import java.awt.*;

/**
 * Created by me on 12/22/16.
 */
public abstract class BitmapKernel extends Kernel {

    public final int width;
    public final int height;
    protected Range range;

    public BitmapKernel(int w, int h) {
        width = w;
        height = h;
    }

//    protected void commit() {
//        put(img); // Because we are using explicit buffer management we must put the imageData array
//    }

    @Override
    public abstract void run();

    public abstract void next();


    public abstract Graphics gfx();

    public abstract void draw(Graphics g);
}
