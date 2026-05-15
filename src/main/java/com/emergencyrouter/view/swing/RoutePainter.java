package com.emergencyrouter.view.swing;

import com.emergencyrouter.interfaces.Location;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public final class RoutePainter
        implements Painter<JXMapViewer> {

    private final List<Location> track;

    public RoutePainter(
            List<Location> track
    ) {

        this.track = track;
    }

    @Override
    public void paint(
            Graphics2D graphics,
            JXMapViewer map,
            int width,
            int height
    ) {

        if (track == null || track.isEmpty()) {
            return;
        }

        Graphics2D g =
                (Graphics2D) graphics.create();

        Rectangle viewport =
                map.getViewportBounds();

        g.translate(
                -viewport.x,
                -viewport.y
        );

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(
                new Color(0, 255, 180)
        );

        g.setStroke(
                new BasicStroke(
                        5,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        for (int index = 0;
             index < track.size() - 1;
             index++) {

            Location first =
                    track.get(index);

            Location second =
                    track.get(index + 1);

            GeoPosition firstPosition =
                    new GeoPosition(
                            first.getLatitude(),
                            first.getLongitude()
                    );

            GeoPosition secondPosition =
                    new GeoPosition(
                            second.getLatitude(),
                            second.getLongitude()
                    );

            Point2D firstPoint =
                    map.getTileFactory()
                            .geoToPixel(
                                    firstPosition,
                                    map.getZoom()
                            );

            Point2D secondPoint =
                    map.getTileFactory()
                            .geoToPixel(
                                    secondPosition,
                                    map.getZoom()
                            );

            g.drawLine(
                    (int) firstPoint.getX(),
                    (int) firstPoint.getY(),
                    (int) secondPoint.getX(),
                    (int) secondPoint.getY()
            );
        }

        g.dispose();
    }
}