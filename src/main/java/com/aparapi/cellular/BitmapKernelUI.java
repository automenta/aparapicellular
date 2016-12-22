/**
 * This product currently only contains code developed by authors
 * of specific components, as identified by the source code files.
 * <p>
 * Since product implements StAX API, it has dependencies to StAX API
 * classes.
 * <p>
 * For additional credits (generally to people who reported problems)
 * see CREDITS file.
 */
/*
Copyright (c) 2010-2011, Advanced Micro Devices, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following
disclaimer. 

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
disclaimer in the documentation and/or other materials provided with the distribution. 

Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

If you use the software (in whole or in part), you shall adhere to all applicable U.S., European, and other export
laws, including but not limited to the U.S. Export Administration Regulations ("EAR"), (15 C.F.R. Sections 730 through
774), and E.U. Council Regulation (EC) No 1334/2000 of 22 June 2000.  Further, pursuant to Section 740.6 of the EAR,
you hereby certify that, except pursuant to a license granted by the United States Department of Commerce Bureau of 
Industry and Security or as otherwise permitted pursuant to a License Exception under the U.S. Export Administration 
Regulations ("EAR"), you will not (1) export, re-export or release to a national of a country in Country Groups D:1,
E:1 or E:2 any restricted technology, software, or source code you receive hereunder, or (2) export to Country Groups
D:1, E:1 or E:2 the direct product of such technology or software, if such foreign produced direct product is subject
to national security controls as identified on the Commerce Control List (currently found in Supplement 1 to Part 774
of EAR).  For the most current Country Group listings, or for additional information about the EAR or your obligations
under those regulations, please refer to the U.S. Bureau of Industry and Security's website at http://www.bis.doc.gov/. 

*/

package com.aparapi.cellular;

import com.aparapi.Kernel;
import com.aparapi.ProfileInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * An example Aparapi application which demonstrates Conways 'Game Of Life'.
 *
 * Original code from Witold Bolt's site https://github.com/houp/aparapi/tree/master/samples/gameoflife.
 *
 * Converted to use int buffer and some performance tweaks by Gary Frost
 *
 * @author Wiltold Bolt
 * @author Gary Frost
 */
public class BitmapKernelUI {

    final int width;
    final int height;

    static long start = 0L;

    static int generations = 0;

    static double fps = 0;

    public BitmapKernelUI(BitmapKernel kernel) {

        this.width = kernel.width;
        this.height = kernel.height;


        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);



        final JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setIgnoreRepaint(true);


        final String[] choices = new String[]{
                "GPU (OpenCL)",
                "CPU (Java Threads)"
        };
        final JComboBox modeButton = new JComboBox(choices);
        modeButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final String item = (String) modeButton.getSelectedItem();
                if (item.equals(choices[1])) {
                    kernel.setExecutionMode(Kernel.EXECUTION_MODE.JTP);
                } else if (item.equals(choices[0])) {
                    kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
                }
            }

        });
        JLabel fpsLabel = new JLabel();
        fpsLabel.setForeground(Color.ORANGE);
        controlPanel.add(fpsLabel);
        controlPanel.add(modeButton);
        controlPanel.setOpaque(false);


        final JFrame frame = new JFrame("");
        frame.setIgnoreRepaint(true);
        frame.setDefaultLookAndFeelDecorated(false);

        frame.getContentPane().setBackground(Color.DARK_GRAY);



        final ConcurrentLinkedDeque<MouseEvent> spawn = new ConcurrentLinkedDeque<>();
        final BitmapKernelView viewer = new BitmapKernelView(kernel) {
            @Override
            protected void onDrag(MouseEvent e) {
                spawn.add(e);
            }
        };
        frame.getContentPane().add(viewer, BorderLayout.CENTER);
        frame.getContentPane().add(controlPanel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setVisible(true);
        frame.pack();


        start = System.currentTimeMillis();
        while (true) {

            if (!viewer.busy) {

                //apply the mouse-drawn updates between generations
                while (!spawn.isEmpty()) {

                    MouseEvent e = spawn.pollFirst();

                    Graphics gfx = kernel.gfx();
                    gfx.setXORMode(Color.BLACK);
                    gfx.setColor(Color.WHITE);
                    gfx.fillOval(e.getX(), e.getY(), 6, 6);
                }

                final long now = System.currentTimeMillis();
                if ((now - start) > 1000) {
                    fps = (generations * 1000.0) / (now - start);
                    generations = 0;
                    start = now;
                    fpsLabel.setText( String.format("%5.2f fps", fps));
                    //kernel.getTargetDevice().getType() + " " +
                }

                viewer.repaint();

            }

            kernel.next();
            generations++;



        }


    }


    static class BitmapKernelView extends JComponent implements MouseMotionListener {
        //private final Font font = new Font("Monospace", Font.BOLD, 50);
        private final BitmapKernel kernel;

        /** a soft lock that prevents redudant repaint updates during an ongoing one */
        public volatile boolean busy = false;

        int lastX = -1, lastY = -1;

        public BitmapKernelView(BitmapKernel kernel) {
            super();
            this.kernel = kernel;

            addMouseMotionListener(this);

            // Set the default size and add to the frames content pane
            setPreferredSize(new Dimension(kernel.width, kernel.height));


            //setDoubleBuffered(true);
        }

        /** override in subclasses to handle each semi-deduplicated screen coords where the user dragged the mouse */
        protected void onDrag(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {

            if (kernel != null) {
                if (lastX != e.getX() || lastY != e.getY()) {
                    lastX = e.getX();
                    lastY = e.getY();
                    onDrag(e);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }


        @Override
        public void paintComponent(Graphics g) {

            busy = true;

            //g.setFont(font);
            //g.setColor(Color.WHITE);

            final List<ProfileInfo> profileInfo = kernel.getProfileInfo();
            if (profileInfo != null) {
                for (final ProfileInfo p : profileInfo) {
                    System.out.print(" " + p.getType() + " " + p.getLabel() + " " + (p.getStart() / 1000) + " .. "
                            + (p.getEnd() / 1000) + " " + ((p.getEnd() - p.getStart()) / 1000) + "us");
                }
                System.out.println();
            }

            kernel.draw(g);


            busy = false;

        }
    }
}
