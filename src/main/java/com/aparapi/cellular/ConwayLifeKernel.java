package com.aparapi.cellular;

import java.util.Arrays;

/**
 * LifeKernel represents the data parallel algorithm describing by Conway's game of life.
 *
 * http://en.wikipedia.org/wiki/Conway's_Game_of_Life
 *
 * We examine the state of each pixel and its 8 neighbors and apply the following rules.
 *
 * if pixel is dead (off) and number of neighbors == 3 {
 *       pixel is turned on
 * } else if pixel is alive (on) and number of neighbors is neither 2 or 3
 *       pixel is turned off
 * }
 *
 * We use an image buffer which is 2*width*height the size of screen and we use fromBase and toBase to track which half of the buffer is being mutated for each pass. We basically
 * copy from getGlobalId()+fromBase to getGlobalId()+toBase;
 *
 *
 * Prior to each pass the values of fromBase and toBase are swapped.
 *
 */

public class ConwayLifeKernel extends BitmapU8Kernel {

    public static final byte ALIVE = (byte) 0xff;
    public static final byte DEAD = 0;

    public ConwayLifeKernel(int w, int h, int blockSize) {
        super(w, h, blockSize);

        //setExplicit(true); // This gives us a performance boost for GPU mode.

        Arrays.fill(this.img, ConwayLifeKernel.DEAD);


        /** initial pattern: draw a line across the image **/
        for (int i = (width * (height / 2)) + (width / 10); i < ((width * ((height / 2) + 1)) - (width / 10)); i++) {
            this.img[toBase + i] = ConwayLifeKernel.ALIVE;
            this.img[fromBase + i] = ConwayLifeKernel.ALIVE;
        }


    }


    @Override
    public void run() {
        final int gid = getGlobalId();
        final int to = gid + toBase;
        final int from = gid + fromBase;
        final int x = gid % width;
        final int y = gid / width;

        if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
            // This pixel is on the border of the view, just keep existing value
            img[to] = img[from];
        } else {
            // Count the number of neighbors.  We use (value&1x) to turn pixel value into either 0 or 1
            final int neighbors = (img[from - 1] & 1) + // EAST
                    (img[from + 1] & 1) + // WEST
                    (img[from - width - 1] & 1) + // NORTHEAST
                    (img[from - width] & 1) + // NORTH
                    (img[(from - width) + 1] & 1) + // NORTHWEST
                    (img[(from + width) - 1] & 1) + // SOUTHEAST
                    (img[from + width] & 1) + // SOUTH
                    (img[from + width + 1] & 1); // SOUTHWEST

            // The game of life logic
            if ((neighbors == 3) || ((neighbors == 2) && (img[from] == ALIVE))) {
                img[to] = ALIVE;
            } else {
                img[to] = DEAD;
            }

        }

    }


    public static void main(String[] _args) {


        new BitmapKernelUI(new ConwayLifeKernel(512, 512, 128));


    }

}
