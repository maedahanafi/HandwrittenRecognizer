package recognizergui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
/**
 * Maeda Hanafi
 * The downsampled grid
 */
public class Sample extends JPanel {

	SampleData data;

	Sample(final int width, final int height) {
		this.data = new SampleData(' ', width, height);
	}

	SampleData getData() {
		return this.data;
	}

	@Override
	public void paint(final Graphics g) {
            if (this.data == null) {
                    return;
            }

            int x, y;
            final int vcell = getHeight() / this.data.getHeight();
            final int hcell = getWidth() / this.data.getWidth();

            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.black);
            for (y = 0; y < this.data.getHeight(); y++) {
                g.drawLine(0, y * vcell, getWidth(), y * vcell);
            }
            for (x = 0; x < this.data.getWidth(); x++) {
                g.drawLine(x * hcell, 0, x * hcell, getHeight());
            }

            for (y = 0; y < this.data.getHeight(); y++) {
                for (x = 0; x < this.data.getWidth(); x++) {
                    if (this.data.getData(x, y)) {
                            g.fillRect(x * hcell, y * vcell, hcell, vcell);
                    }
                }
            }

            g.setColor(Color.black);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

	}

        void setData(final SampleData data) {
		this.data = data;
	}

}