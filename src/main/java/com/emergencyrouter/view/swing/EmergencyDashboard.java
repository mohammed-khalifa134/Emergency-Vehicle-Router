
package com.emergencyrouter.view.swing;

import com.emergencyrouter.controller.MapController;
import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.DispatchResult;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.vehicles.Vehicle;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EmergencyDashboard extends JFrame {

    private final MapController controller;

    private final JXMapViewer mapViewer;

    private JComboBox<EmergencyType> emergencyBox;

    private JTextArea logArea;

    private RoutePainter routePainter;

    private WaypointPainter<Waypoint> waypointPainter;

    public EmergencyDashboard(
            MapController controller
    ) {

        this.controller = controller;

        setTitle("Emergency Router System");

        setSize(1450, 850);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        applyRTL();

        mapViewer = new JXMapViewer();

        initializeMap();

        add(createSidePanel(), BorderLayout.EAST);

        add(mapViewer, BorderLayout.CENTER);

        registerMapClick();
    }

    private void applyRTL() {

        applyComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );
    }

    private JPanel createSidePanel() {

        JPanel panel = new JPanel();

        panel.setPreferredSize(
                new Dimension(360, 0)
        );

        panel.setBackground(
                new Color(20, 24, 30)
        );

        panel.setLayout(
                new BorderLayout(15, 15)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        JLabel title =
                new JLabel(
                        "لوحة إدارة الطوارئ",
                        SwingConstants.CENTER
                );

        title.setForeground(Color.WHITE);

        title.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();

        content.setBackground(
                new Color(20, 24, 30)
        );

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel emergencyLabel =
                createLabel("نوع البلاغ");

        emergencyBox =
                new JComboBox<>(
                        EmergencyType.values()
                );

        styleComboBox(emergencyBox);

        emergencyBox.setRenderer(

                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {

                        JLabel label =
                                (JLabel)
                                        super.getListCellRendererComponent(
                                                list,
                                                value,
                                                index,
                                                isSelected,
                                                cellHasFocus
                                        );

                        label.setBorder(
                                BorderFactory.createEmptyBorder(
                                        8,
                                        12,
                                        8,
                                        12
                                )
                        );

                        label.setFont(
                                new Font(
                                        "Arial",
                                        Font.BOLD,
                                        15
                                )
                        );

                        return label;
                    }
                }
        );

        JTextArea info =
                new JTextArea(
                        """
                        • اضغط على الخريطة لتحديد موقع الحادث
                        
                        • سيتم إرسال أقرب مركبة طوارئ تلقائياً
                        
                        • استخدم عجلة الماوس للتكبير والتصغير
                        
                        • اسحب الخريطة للتحرك بحرية
                        """
                );

        info.setEditable(false);

        info.setLineWrap(true);

        info.setWrapStyleWord(true);

        info.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        info.setForeground(Color.WHITE);

        info.setBackground(
                new Color(35, 40, 48)
        );

        info.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        content.add(emergencyLabel);

        content.add(Box.createVerticalStrut(10));

        content.add(emergencyBox);

        content.add(Box.createVerticalStrut(25));

        content.add(info);

        panel.add(content, BorderLayout.CENTER);

        logArea = new JTextArea();

        logArea.setEditable(false);

        logArea.setLineWrap(true);

        logArea.setWrapStyleWord(true);

        logArea.setBackground(
                new Color(12, 14, 18)
        );

        logArea.setForeground(
                new Color(0, 255, 180)
        );

        logArea.setFont(
                new Font("Consolas", Font.PLAIN, 14)
        );

        JScrollPane scroll =
                new JScrollPane(logArea);

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(70, 70, 70)
                        ),
                        "سجل العمليات"
                )
        );

        scroll.setPreferredSize(
                new Dimension(300, 260)
        );

        panel.add(scroll, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createLabel(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(Color.WHITE);

        label.setFont(
                new Font("Arial", Font.BOLD, 15)
        );

        return label;
    }

    private void styleComboBox(
            JComboBox<?> comboBox
    ) {

        comboBox.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        42
                )
        );

        comboBox.setBackground(
                new Color(40, 45, 55)
        );

        comboBox.setForeground(Color.WHITE);

        comboBox.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );
    }

    /*
     * لا تعدل initializeMap
     */
    private void initializeMap() {

    TileFactoryInfo info =
            new TileFactoryInfo(
                    1,
                    17,
                    17,
                    256,
                    true,
                    true,
                    "https://tile.openstreetmap.org",
                    "x",
                    "y",
                    "z"
            ) {

                @Override
                public String getTileUrl(
                        int x,
                        int y,
                        int zoom
                ) {

                    int osmZoom = 17 - zoom;

                    return baseURL
                            + "/"
                            + osmZoom
                            + "/"
                            + x
                            + "/"
                            + y
                            + ".png";
                }
            };

    DefaultTileFactory tileFactory =
            new DefaultTileFactory(info);

    /*
     * تحسين التحميل
     */
    tileFactory.setThreadPoolSize(8);

    mapViewer.setTileFactory(tileFactory);

    GeoPosition tripoli =
            new GeoPosition(
                    32.8872,
                    13.1913
            );

    mapViewer.setZoom(5);

    mapViewer.setAddressLocation(tripoli);

    /*
     * حركة سلسة
     */
    mapViewer.setRestrictOutsidePanning(false);

    mapViewer.setHorizontalWrapped(false);

    /*
     * تكبير سلس
     */
    mapViewer.addMouseWheelListener(
            new ZoomMouseWheelListenerCursor(
                    mapViewer
            )
    );

    /*
     * سحب الخريطة
     */
    MouseInputListener mouseListener =
            new PanMouseInputListener(
                    mapViewer
            );

    mapViewer.addMouseListener(
            mouseListener
    );

    mapViewer.addMouseMotionListener(
            mouseListener
    );

    /*
     * تحريك بالكيبورد
     */
    mapViewer.addKeyListener(
            new PanKeyListener(
                    mapViewer
            )
    );
}
    private void registerMapClick() {

        mapViewer.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent event
                    ) {

                        if (SwingUtilities.isRightMouseButton(event)) {
                            return;
                        }

                        GeoPosition position =
                                mapViewer.convertPointToGeoPosition(
                                        event.getPoint()
                                );

                        int result =
                                JOptionPane.showConfirmDialog(
                                        EmergencyDashboard.this,
                                        """
                                        هل تريد تسجيل حادث في هذا الموقع؟
                                        
                                        سيتم إرسال أقرب مركبة طوارئ.
                                        """,
                                        "تأكيد الحادث",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE
                                );

                        if (result != JOptionPane.YES_OPTION) {
                            return;
                        }

                        handleEmergency(
                                position.getLatitude(),
                                position.getLongitude()
                        );
                    }
                }
        );
    }

    private void handleEmergency(
            double latitude,
            double longitude
    ) {

        EmergencyType type =
                (EmergencyType)
                        emergencyBox.getSelectedItem();

        DispatchResult result =
                controller.createEmergencyReport(
                        type,
                        latitude,
                        longitude,
                        3
                );

        if (!result.success()) {

            log(
                    "لم يتم العثور على مركبة مناسبة"
            );

            return;
        }

        Vehicle vehicle =
                result.vehicle();

        Route route =
                controller.calculateVehicleRoute(
                        vehicle,
                        result.report()
                );

        drawRoute(route);

        log("========== بلاغ جديد ==========");

        log(
                "نوع البلاغ: "
                        + type
        );

        log(
                "موقع الحادث: "
                        + String.format(
                        "%.5f",
                        latitude
                )
                        + " , "
                        + String.format(
                        "%.5f",
                        longitude
                )
        );

        log(
                "المركبة المرسلة: "
                        + vehicle.getId()
        );

        log(
                "المسافة: "
                        + String.format(
                        "%.2f KM",
                        route.getDistance()
                )
        );

        log(
                "الوقت المتوقع: "
                        + String.format(
                        "%.2f دقيقة",
                        route.getTime()
                )
        );

        log("==============================");
    }

    private void drawRoute(
            Route route
    ) {

        List<Location> locations =
                route.getPath();

        routePainter =
                new RoutePainter(locations);

        Set<Waypoint> waypoints =
                new HashSet<>();

        for (Location location : locations) {

            waypoints.add(
                    new DefaultWaypoint(
                            location.getLatitude(),
                            location.getLongitude()
                    )
            );
        }

        waypointPainter =
                new WaypointPainter<>();

        waypointPainter.setWaypoints(
                waypoints
        );

        CompoundPainter<JXMapViewer> painter =
                new CompoundPainter<>(
                        routePainter,
                        waypointPainter
                );

        mapViewer.setOverlayPainter(
                painter
        );

        if (!locations.isEmpty()) {

            Location last =
                    locations.get(
                            locations.size() - 1
                    );

            mapViewer.setAddressLocation(
                    new GeoPosition(
                            last.getLatitude(),
                            last.getLongitude()
                    )
            );

            mapViewer.repaint();
        }
    }

    private void log(
            String text
    ) {

        logArea.append(
                "• " + text + "\n\n"
        );

        logArea.setCaretPosition(
                logArea.getDocument().getLength()
        );
    }
}
