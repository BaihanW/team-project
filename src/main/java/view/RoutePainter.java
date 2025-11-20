package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RoutePainter implements Painter<JXMapViewer> {

    private List<GeoPosition> route = new ArrayList<>();
    private List<List<GeoPosition>> segments = null;

    public RoutePainter(List<GeoPosition> route) {
        if (route != null) this.route = route;
    }

    public void setRoute(List<GeoPosition> route) {
        this.route = (route == null) ? new ArrayList<>() : route;
        this.segments = null;
    }

    public void setSegments(List<List<GeoPosition>> segments) {
        this.segments = segments;
        this.route = new ArrayList<>();
        if (segments != null) {
            for (List<GeoPosition> seg : segments) {
                if (seg == null) continue;
                if (!this.route.isEmpty() && !seg.isEmpty()) {
                    this.route.remove(this.route.size() - 1);
                }
                this.route.addAll(seg);
            }
        }
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if ((segments == null || segments.isEmpty()) && (route == null || route.size() < 2)) return;

        g = (Graphics2D) g.create();
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle viewport = map.getViewportBounds();

        if (segments != null && !segments.isEmpty()) {
            int sCount = segments.size();
            int alphaStart = 255;
            int alphaEnd = Math.max(1, (int) Math.round(255 * 0.40)); // opacity lower bound

            for (int si = 0; si < sCount; si++) {
                List<GeoPosition> seg = segments.get(si);
                if (seg == null || seg.size() < 2) continue;

                float t = (sCount == 1) ? 0f : ((float) si) / (float) (sCount - 1);
                int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                alpha = Math.max(0, Math.min(255, alpha));

                g.setColor(new Color(0, 120, 255, alpha));

                GeneralPath path = new GeneralPath();
                boolean first = true;
                for (GeoPosition gp : seg) {
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    double x = pt.getX() - viewport.getX();
                    double y = pt.getY() - viewport.getY();
                    if (first) { path.moveTo(x, y); first = false; }
                    else path.lineTo(x, y);
                }
                g.draw(path);
            }
        } else {
            g.setColor(new Color(0, 120, 255, 180));
            GeneralPath path = new GeneralPath();
            boolean first = true;
            for (GeoPosition gp : route) {
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                double x = pt.getX() - viewport.getX();
                double y = pt.getY() - viewport.getY();

                if (first) {
                    path.moveTo(x, y);
                    first = false;
                } else {
                    path.lineTo(x, y);
                }
            }

            g.draw(path);
        }

        g.dispose();
    }
}
