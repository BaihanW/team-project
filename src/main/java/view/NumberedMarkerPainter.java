package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

public class NumberedMarkerPainter implements Painter<JXMapViewer> {
    private List<GeoPosition> positions;

    public NumberedMarkerPainter(List<GeoPosition> positions) {
        this.positions = positions;
    }

    public void setPositions(List<GeoPosition> positions) {
        this.positions = positions;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if (positions == null || positions.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle viewport = map.getViewportBounds();

        for (int i = 0; i < positions.size(); i++) {
            GeoPosition gp = positions.get(i);
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            double x = pt.getX() - viewport.getX();
            double y = pt.getY() - viewport.getY();

            int radius = 12;
            int cx = (int) Math.round(x) - radius;
            int cy = (int) Math.round(y) - radius;

            g2.setColor(new Color(220, 60, 60));
            Ellipse2D circle = new Ellipse2D.Double(cx, cy, radius * 2, radius * 2);
            g2.fill(circle);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(circle);

            String text = String.valueOf(i + 1);
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx + (radius * 2 - fm.stringWidth(text)) / 2;
            int ty = cy + ((radius * 2 - fm.getHeight()) / 2) + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, tx, ty);
        }

        g2.dispose();
    }
}

