package com.aparapi.cellular;

import java.util.Arrays;

/**
 * SmoothLife - continuous-domain cellular automata
 * translated from: https://github.com/rreusser/regl-smooth-life/blob/master/www/index.js
 *
 */
public class SmoothLifeKernel extends BitmapFloatKernel {

    //parameters
    final float[] birth = new float[] { 0.27f, 0.34f };
    final float[] death = new float[] { 0.52f, 0.75f };
    final float alpha_n = 0.03f;
    final float alpha_m = 0.15f;
    final float initial_fill = 0.51f;
    final float dt = 0.0316f;

    final float ra = 12;
    final float ri = ra / 3f;
    final float b = 1;

    final float areai = (float) (ri * ri * Math.PI);
    final float areaa = (float) (ra * ra * Math.PI - areai);
    float Minv = 1.0f / areai;
    float Ninv = 1.0f / areaa;

    public SmoothLifeKernel(int width, int height, int blockSize) {
        super(width, height, blockSize);
        reset();
    }

    void reset() {
        Arrays.fill(fimg, 0);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                float dx = i - width * 0.5f;
                float dy = j - height * 0.5f;
                int t = i + width * j;
                fimg[t] =
                        (float) (exp((-dx * dx - dy * dy) / ra / ra / 2f) + Math.random() * initial_fill);
            }
        }
    }



    float func_smooth (float x, float a, float ea) {
        return 1.0f / (1.0f + exp(-(x - a) * 4.0f / ea));
    }
    float sigmoid_ab (float sn, float x, float a, float b) {
        return func_smooth(x, a, sn) * (1.0f - func_smooth(x, b, sn));
    }
    float sigmoid_mix (float sm, float min, float max, float m) {
        return min + func_smooth(m, 0.5f, sm) * (max - min);
    }


    @Override protected float compute(int at) {
        int y = at / width;
        int x = at % width;

        float m = 0, n = 0;
        for (float dy = -ra - 2; dy <= ra + 2; dy++) {

            int ty = (int)(y + dy);
            if ((ty < 0) || (ty >= height))
                continue;

            float dydy = dy * dy;
            int yOffset = ty * width;

            for (float dx = -ra - 2; dx <= ra + 2; dx++) {


                int tx = (int)(x + dx);
                if ((tx < 0) || (tx >= width))
                    continue;

                float r = (float) Math.sqrt( dx * dx + dydy );

                float a_interp = (ra + b / 2f - r) / b;

                float value = 0;
                // If we get *anything* here, sample the texture:
                if (a_interp > 0) {
                    value = fimg[yOffset + tx];
                }

                float i_interp = (ri + b / 2f - r) / b;
                if (i_interp > 1f) {
                    // If inside the inner circle, just add:
                    m += value;
                } else if (i_interp > 0f) {
                    // Else if greater than zero, add antialiased:
                    m += value * i_interp;//((ri + b / 2 - r) / b);
                }

                if (i_interp < 1f) {
                    // If outside the inner border of the inner circle:
                    if (1f - i_interp < 1f) {
                        // If inside the outer border of the inner circle, then interpolate according to inner (reversed):
                        n += value * (1f - i_interp); //(ri + b / 2 - r) / b);
                    } else if (a_interp > 1) {
                        // Else if inside the outer circle, just add:
                        n += value;
                    } else if (a_interp > 0) {
                        // Else, if interpolant greater than zero, add:
                        n += value * a_interp; //((ra + b / 2 - r) / b);
                    }


                }
            }
        }

        m *= Minv;
        n *= Ninv;

        float s = sigmoid_ab(
                alpha_n,
                n,
                sigmoid_mix(alpha_m, birth[0], death[0], m),
                sigmoid_mix(alpha_m, birth[1], death[1], m)
        );

        // Update:
        float prev = (fimg[at]);

        //float next = prev + dt * (2.0 * s - 1.0);

        //clamp
//        if (next < 0) next = 0;
//        if (next > 1f) next = 1f;
        return prev + dt * (s - prev);
    }


    public static void main(String[] _args) {


        new BitmapKernelUI(new SmoothLifeKernel(256, 128, 128 ));


    }

}
