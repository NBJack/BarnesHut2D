package org.layfield.graph.barnes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Ryan Layfield on 4/15/2016.
 */
public class EasyBitmap {

    private BufferedImage image;
    private int offX, offY;

    public EasyBitmap(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void setOffset(int offX, int offY) {
        this.offX = offX;
        this.offY = offY;
    }

    public void drawRect(Color c, Vector2D upLeft, Vector2D lowRight) {
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.drawRect((int) upLeft.getX() + offX,(int)  upLeft.getY() + offY,(int)  lowRight.getX() + offX,(int)  lowRight.getY() + offY);
        g.dispose();
    }

    public void drawPoint(Color c, Vector2D point) {
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.drawLine((int) point.getX() + offX,(int)  point.getY() + offY,(int)  point.getX() + offX,(int)  point.getY() + offY);
        g.dispose();
    }

    public void save(File dest) throws IOException {
        ImageIO.write(image, "bmp", dest);
    }

    public void clear(Color c) {
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
    }

    public void drawFillRect(Color c, Vector2D upLeft, Vector2D lowRight) {
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect((int) upLeft.getX() + offX,(int)  upLeft.getY() + offY,(int)  lowRight.getX() + offX,(int)  lowRight.getY() + offY);
        g.dispose();
    }

    public void drawText(Color c, Vector2D point, String msg) {
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.drawString(msg, (int) point.getX() + offX,(int)  point.getY() + offY);
        g.dispose();

    }
}

