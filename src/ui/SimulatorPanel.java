package ui; // // پکیج UI

import core.Point;
import simulation.World; // // دنیا
import infrastructure.*; // // Road/Lane/Intersection
import core.*; // // Point/Vehicle/Direction
import trafficcontrol.*; // // چراغ‌ها
import pedestrian.*; // // عابر/گذرگاه

import javax.swing.*; // // Swing
import java.awt.*; // // گرافیک
import java.awt.event.*; // // رویدادها
import java.awt.geom.AffineTransform; // // تبدیل‌ها
import java.awt.geom.Path2D; // // مسیر
import java.util.List; // // لیست

public class SimulatorPanel extends JPanel { // // پنل رسم
    private final World world; // // دنیا
    private final Camera camera; // // دوربین
    private Image carImg; // // تصویر خودرو

    private int lastDragX, lastDragY; // // مختصات آخر درگ
    private boolean dragging = false; // // وضعیت درگ

    public SimulatorPanel(World w, Camera cam){ // // سازنده
        this.world = w; // // ذخیره
        this.camera = cam; // // ذخیره
        setBackground(UIConstants.BACKGROUND); // // پس‌زمینه

        addMouseWheelListener(new MouseWheelListener(){ // // زوم اسکرول
            @Override public void mouseWheelMoved(MouseWheelEvent e){
                double s = camera.getScale(); // // زوم فعلی
                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // // تغییر
                camera.setScale(s); // // اعمال
                repaint(); // // باز‌رسم
            }
        });

        addMouseListener(new MouseAdapter(){ // // برای پن
            @Override public void mousePressed(MouseEvent e){ dragging = true; lastDragX = e.getX(); lastDragY = e.getY(); } // // شروع
            @Override public void mouseReleased(MouseEvent e){ dragging = false; } // // پایان
        });

        addMouseMotionListener(new MouseMotionAdapter(){ // // درگ
            @Override public void mouseDragged(MouseEvent e){
                if (dragging){ // // اگر درحال درگ
                    int dx = e.getX()-lastDragX; // // Δx
                    int dy = e.getY()-lastDragY; // // Δy
                    camera.pan(dx, dy); // // پن دوربین
                    lastDragX = e.getX(); // // آپدیت X
                    lastDragY = e.getY(); // // آپدیت Y
                    repaint(); // // رسم
                }
            }
        });
    }

    public void loadAssets(String carPath){ // // بارگذاری تصویر خودرو
        if (carPath != null){
            java.net.URL url = getClass().getClassLoader().getResource(carPath); // // URL
            if (url != null) carImg = new ImageIcon(url).getImage(); // // تصویر
        }
    }

    @Override protected void paintComponent(Graphics g){ // // رسم
        super.paintComponent(g); // // پاک‌سازی
        Graphics2D g2 = (Graphics2D) g; // // تبدیل به G2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس

        AffineTransform bak = g2.getTransform(); // // ذخیره تبدیل
        g2.scale(camera.getScale(), camera.getScale()); // // زوم
        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // // پن

        drawRoads(g2); // // کشیدن جاده‌ها
        drawCrossings(g2); // // گذرگاه‌ها
        drawTrafficLights(g2); // // چراغ‌ها
        drawVehicles(g2); // // خودروها
        drawPedestrians(g2); // // عابرها

        g2.setTransform(bak); // // بازگردانی
    }

    private int roadTotalWidth(Road r){ // // پهنای کل آسفالت
        int perSide = Math.max(r.getForwardLanes().size(), r.getBackwardLanes().size()); // // بیشینه هر سمت
        int totalLanes = r.isTwoWay() ? perSide * 2 : perSide; // // تعداد کل لاین
        int innerGaps = Math.max(0, totalLanes - 1); // // شکاف‌های داخلی
        int w = totalLanes * UIConstants.LANE_WIDTH + innerGaps * UIConstants.LANE_GAP; // // پهنا
        if (r.isTwoWay()) w += UIConstants.LANE_GAP; // // شکاف وسط
        return Math.max(w, UIConstants.LANE_WIDTH*2 + UIConstants.LANE_GAP); // // حداقل معقول
    }

    private void drawRoads(Graphics2D g2){ // // رسم جاده‌ها
        List<Road> roads = world.getMap().getRoads(); // // لیست جاده‌ها
        for (int i=0;i<roads.size();i++){ // // حلقه
            Road r = roads.get(i); // // جاده
            Path2D path = r.buildPath(); // // مسیر مرکزی

            g2.setColor(UIConstants.ROAD_FILL); // // رنگ آسفالت
            g2.setStroke(new BasicStroke(roadTotalWidth(r), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // // قلم ضخیم
            g2.draw(path); // // کشیدن آسفالت

            if (r.isTwoWay()){ // // اگر دوطرفه
                g2.setColor(UIConstants.DASH); // // رنگ خط‌چین
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f,14f}, 0f)); // // سبک خط‌چین
                g2.draw(path); // // کشیدن خط‌چین روی خم
            }
        }
    }

    private void drawTrafficLights(Graphics2D g2){ // // رسم چراغ‌ها
        List<Intersection> xs = world.getMap().getIntersections(); // // تقاطع‌ها
        int r = 6; // // شعاع
        int halfRoad = 20; // // پهنا برای فاصله چراغ
        if (!world.getMap().getRoads().isEmpty()) halfRoad = roadTotalWidth(world.getMap().getRoads().get(0)) / 2; // // از اولین جاده
        int AHEAD = halfRoad + 6; // // جلو
        int SIDE  = Math.max(8, halfRoad - 8); // // کنار

        for (int i=0;i<xs.size();i++){ // // حلقه
            Intersection it = xs.get(i); // // تقاطع
            Point p = it.getPosition(); // // مرکز
            for (Direction d : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){ // // جهت‌ها
                TrafficControlDevice dev = it.getControl(d); // // کنترل
                if (!(dev instanceof TrafficLight)) continue; // // اگر چراغ نیست
                TrafficLight tl = (TrafficLight) dev; // // چراغ
                int cx=p.getX(), cy=p.getY(); int lx=cx, ly=cy; // // مختصات
                if (d==Direction.NORTH){ lx=cx - SIDE; ly=cy - AHEAD; } // // گوشه مناسب
                if (d==Direction.SOUTH){ lx=cx + SIDE; ly=cy + AHEAD; } // // گوشه مناسب
                if (d==Direction.EAST ){ lx=cx + AHEAD; ly=cy - SIDE; } // // گوشه مناسب
                if (d==Direction.WEST ){ lx=cx - AHEAD; ly=cy + SIDE; } // // گوشه مناسب
                Color c = Color.GRAY; // // رنگ پیش‌فرض
                if (tl.getState()==LightState.RED) c=Color.RED; // // قرمز
                else if (tl.getState()==LightState.YELLOW) c=Color.ORANGE; // // زرد
                else if (tl.getState()==LightState.GREEN) c=Color.GREEN; // // سبز
                g2.setColor(c); g2.fillOval(lx-r, ly-r, r*2, r*2); // // پر
                g2.setColor(Color.BLACK); g2.drawOval(lx-r, ly-r, r*2, r*2); // // کادر
            }
        }
    }

    private void drawVehicles(Graphics2D g2){ // // رسم خودروها
        List<Vehicle> vs = world.getVehicles(); // // خودروها
        for (int i=0;i<vs.size();i++){ // // حلقه
            Vehicle v = vs.get(i); // // خودرو
            if (v.getCurrentLane()==null) continue; // // بدون لاین
            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // // نقطه جهان
            double angle = v.getAngle(); // // زاویه
            int cx = wp.getX(), cy = wp.getY(); // // مرکز
            int w = UIConstants.VEHICLE_LENGTH; // // طول
            int h = UIConstants.VEHICLE_WIDTH; // // عرض

            AffineTransform save = g2.getTransform(); // // ذخیره
            g2.translate(cx, cy); // // انتقال
            g2.rotate(angle); // // چرخش

            if (carImg != null) g2.drawImage(carImg, -w/2, -h/2, w, h, null); // // تصویر
            else { g2.setColor(new Color(30,144,255)); g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); g2.setColor(Color.BLACK); g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); } // // شکل ساده

            g2.setTransform(save); // // بازگشت
        }
    }

    private void drawCrossings(Graphics2D g2){ // // گذرگاه‌ها
        List<PedestrianCrossing> cs = world.getCrossings(); // // لیست
        g2.setColor(new Color(255,255,255,180)); // // سفید نیمه‌شفاف
        for (int i=0;i<cs.size();i++){ // // حلقه
            PedestrianCrossing c = cs.get(i); // // گذرگاه
            Point p = c.getIntersection().getPosition(); // // مرکز
            int stripe=4,len=30,gap=4; // // ابعاد
            if (c.getDirection()==Direction.NORTH || c.getDirection()==Direction.SOUTH){ // // افقی
                for(int k=-2;k<=2;k++) g2.fillRect(p.getX()-len, p.getY()+k*(stripe+gap), len*2, stripe); // // نوارها
            } else { // // عمودی
                for(int k=-2;k<=2;k++) g2.fillRect(p.getX()+k*(stripe+gap), p.getY()-len, stripe, len*2); // // نوارها
            }
        }
    }

    private void drawPedestrians(Graphics2D g2){ // // عابرها
        List<pedestrian.Pedestrian> ps = world.getPedestrians(); // // لیست
        for (int i=0;i<ps.size();i++){ // // حلقه
            pedestrian.Pedestrian p = ps.get(i); // // عابر
            core.Point wp = p.getPosition(); // // موقعیت
            g2.setColor(new Color(80,80,80)); g2.fillOval(wp.getX()-4, wp.getY()-4, 8, 8); // // دایره
            g2.setColor(Color.BLACK); g2.drawOval(wp.getX()-4, wp.getY()-4, 8, 8); // // کادر
        }
    }
}




























//package ui; // // پکیج UI
//
//import core.Point;
//import simulation.World;             // // دنیا
//import infrastructure.*;             // // Road/Lane/Intersection
//import core.*;                       // // Point/Vehicle/Direction
//import trafficcontrol.*;             // // چراغ‌ها
//import pedestrian.*;                 // // عابر/گذرگاه
//
//import javax.swing.*;                // // JPanel/ImageIcon
//import java.awt.*;                   // // Graphics2D/Color/Stroke
//import java.awt.event.*;             // // ماوس
//import java.awt.geom.AffineTransform;// // تبدیل‌ها
//import java.awt.geom.Path2D;         // // مسیر برای خیابان خمیده
//import java.util.List;               // // لیست
//
//public class SimulatorPanel extends JPanel { // // پنل رسم
//    private final World world;          // // دنیا
//    private final Camera camera;        // // دوربین
//    private Image carImg;               // // تصویر خودرو (اختیاری)
//
//    private int lastDragX, lastDragY;   // // برای پن با موس
//    private boolean dragging = false;   // // وضعیت درگ
//
//    public SimulatorPanel(World w, Camera cam){ // // سازنده
//        this.world = w; this.camera = cam;                // // ذخیره
//        setBackground(UIConstants.BACKGROUND);            // // پس‌زمینه
//
//        addMouseWheelListener(new MouseWheelListener(){   // // زوم با چرخ موس
//            @Override public void mouseWheelMoved(MouseWheelEvent e){
//                double s = camera.getScale();             // // زوم فعلی
//                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // // تغییر
//                camera.setScale(s); repaint();            // // اعمال
//            }
//        });
//
//        addMouseListener(new MouseAdapter(){              // // شروع/پایان درگ
//            @Override public void mousePressed(MouseEvent e){ dragging = true; lastDragX = e.getX(); lastDragY = e.getY(); }
//            @Override public void mouseReleased(MouseEvent e){ dragging = false; }
//        });
//
//        addMouseMotionListener(new MouseMotionAdapter(){  // // پن با درگ
//            @Override public void mouseDragged(MouseEvent e){
//                if (dragging){ int dx = e.getX()-lastDragX, dy = e.getY()-lastDragY; camera.pan(dx, dy); lastDragX = e.getX(); lastDragY = e.getY(); repaint(); }
//            }
//        });
//    }
//
//    public void loadAssets(String carPath){ // // بارگذاری تصویر خودرو
//        if (carPath != null){
//            java.net.URL url = getClass().getClassLoader().getResource(carPath); // // گرفتن URL
//            if (url != null) carImg = new ImageIcon(url).getImage();             // // خواندن تصویر
//        }
//    }
//
//    @Override protected void paintComponent(Graphics g){ // // رسم
//        super.paintComponent(g);                         // // پاک‌سازی
//        Graphics2D g2 = (Graphics2D) g;                  // // G2D
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
//
//        AffineTransform bak = g2.getTransform();         // // ذخیرهٔ تبدیل
//        g2.scale(camera.getScale(), camera.getScale());  // // زوم
//        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // // پن
//
//        drawRoads(g2);          // // خیابان‌ها (خطی/خمیده)
//        drawCrossings(g2);      // // گذرگاه‌ها
//        drawTrafficLights(g2);  // // چراغ‌ها
//        drawVehicles(g2);       // // خودروها
//        drawPedestrians(g2);    // // عابرها
//
//        g2.setTransform(bak);   // // بازگردانی تبدیل
//    }
//
//    // ====== رسم خیابان‌ها با Path2D و خط‌چین مرکزی ======
//    private int roadTotalWidth(Road r){ // // محاسبهٔ پهنای کل آسفالت
//        int perSide = Math.max(r.getForwardLanes().size(), r.getBackwardLanes().size()); // // بیشینهٔ هر سمت
//        int totalLanes = r.isTwoWay() ? perSide * 2 : perSide;                            // // تعداد کل لِین
//        int innerGaps = Math.max(0, totalLanes - 1);                                      // // فاصله‌های داخلی
//        int w = totalLanes * UIConstants.LANE_WIDTH + innerGaps * UIConstants.LANE_GAP;   // // پهنا
//        if (r.isTwoWay()) w += UIConstants.LANE_GAP;                                      // // شکاف بین دو سمت
//        return Math.max(w, UIConstants.LANE_WIDTH*2 + UIConstants.LANE_GAP);              // // حداقل معقول
//    }
//
//    private void drawRoads(Graphics2D g2){ // // رسم جاده‌ها
//        List<Road> roads = world.getMap().getRoads(); // // همهٔ جاده‌ها
//        for (int i=0;i<roads.size();i++){             // // حلقه
//            Road r = roads.get(i);                    // // جاده
//            Path2D path = r.buildPath();              // // مسیر مرکزی (خطی/خمیده)
//
//            // آسفالت ضخیم //
//            g2.setColor(UIConstants.ROAD_FILL);       // // رنگ آسفالت
//            g2.setStroke(new BasicStroke(roadTotalWidth(r), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // // قلم ضخیم
//            g2.draw(path);                            // // رسم
//
//            // خط‌چین مرکزی (جداکنندهٔ دو جهت) //
//            if (r.isTwoWay()){
//                g2.setColor(UIConstants.DASH);        // // رنگ خط‌چین
//                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f,14f}, 0f)); // // سبک خط‌چین
//                g2.draw(path);                        // // ترسیم روی همان مسیر مرکزی (روی خم هم جواب می‌دهد)
//            }
//        }
//    }
//
//    private void drawTrafficLights(Graphics2D g2){ // // رسم چراغ‌ها در گوشهٔ صحیح
//        List<Intersection> xs = world.getMap().getIntersections(); // // تقاطع‌ها
//        int r = 6; // // شعاع
//        int halfRoad = 20; // // تخمین پهنا برای فاصلهٔ چراغ از مرکز
//        if (!world.getMap().getRoads().isEmpty()) halfRoad = roadTotalWidth(world.getMap().getRoads().get(0)) / 2; // // از اولین راه
//        int AHEAD = halfRoad + 6; // // جلو
//        int SIDE  = Math.max(8, halfRoad - 8); // // کنار
//
//        for (int i=0;i<xs.size();i++){ // // حلقه تقاطع‌ها
//            Intersection it = xs.get(i); // // تقاطع
//            Point p = it.getPosition(); // // مرکز
//            for (Direction d : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){ // // جهت‌ها
//                TrafficControlDevice dev = it.getControl(d); // // کنترل
//                if (!(dev instanceof TrafficLight)) continue; // // اگر چراغ نیست
//                TrafficLight tl = (TrafficLight) dev; // // چراغ
//                int cx=p.getX(), cy=p.getY(); int lx=cx, ly=cy; // // مختصات
//                if (d==Direction.NORTH){ lx=cx - SIDE; ly=cy - AHEAD; } // // گوشه شمال‌غرب
//                if (d==Direction.SOUTH){ lx=cx + SIDE; ly=cy + AHEAD; } // // گوشه جنوب‌شرق
//                if (d==Direction.EAST ){ lx=cx + AHEAD; ly=cy - SIDE; } // // گوشه شمال‌شرق
//                if (d==Direction.WEST ){ lx=cx - AHEAD; ly=cy + SIDE; } // // گوشه جنوب‌غرب
//                Color c = Color.GRAY; if (tl.getState()==LightState.RED) c=Color.RED; else if (tl.getState()==LightState.YELLOW) c=Color.ORANGE; else if (tl.getState()==LightState.GREEN) c=Color.GREEN; // // رنگ
//                g2.setColor(c); g2.fillOval(lx-r, ly-r, r*2, r*2); g2.setColor(Color.BLACK); g2.drawOval(lx-r, ly-r, r*2, r*2); // // دایره
//            }
//        }
//    }
//
//    private void drawVehicles(Graphics2D g2){ // // رسم خودروها
//        List<Vehicle> vs = world.getVehicles(); // // خودروها
//        for (int i=0;i<vs.size();i++){          // // حلقه
//            Vehicle v = vs.get(i);              // // خودرو
//            if (v.getCurrentLane()==null) continue; // // بدون لِین
//            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // // مختصات جهان
//            double angle = v.getAngle();        // // زاویهٔ رندر
//            int cx = wp.getX(), cy = wp.getY(); // // مرکز
//            int w = UIConstants.VEHICLE_LENGTH; // // طول خودرو
//            int h = UIConstants.VEHICLE_WIDTH;  // // عرض خودرو
//
//            AffineTransform save = g2.getTransform(); // // ذخیرهٔ تبدیل
//            g2.translate(cx, cy); g2.rotate(angle);   // // انتقال + چرخش
//
//            if (carImg != null) { g2.drawImage(carImg, -w/2, -h/2, w, h, null); } // // تصویر
//            else { g2.setColor(new Color(30,144,255)); g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); g2.setColor(Color.BLACK); g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); } // // ترسیم ساده
//
//            g2.setTransform(save); // // بازگردانی
//        }
//    }
//
//    private void drawCrossings(Graphics2D g2){ // // گذرگاه‌ها
//        List<PedestrianCrossing> cs = world.getCrossings(); g2.setColor(new Color(255,255,255,180));
//        for (int i=0;i<cs.size();i++){ PedestrianCrossing c = cs.get(i); Point p = c.getIntersection().getPosition(); int stripe=4,len=30,gap=4;
//            if (c.getDirection()==Direction.NORTH || c.getDirection()==Direction.SOUTH){ for(int k=-2;k<=2;k++) g2.fillRect(p.getX()-len, p.getY()+k*(stripe+gap), len*2, stripe); }
//            else { for(int k=-2;k<=2;k++) g2.fillRect(p.getX()+k*(stripe+gap), p.getY()-len, stripe, len*2); }
//        }
//    }
//
//    private void drawPedestrians(Graphics2D g2){ // // عابرها
//        List<pedestrian.Pedestrian> ps = world.getPedestrians(); for (int i=0;i<ps.size();i++){ pedestrian.Pedestrian p = ps.get(i); core.Point wp = p.getPosition(); g2.setColor(new Color(80,80,80)); g2.fillOval(wp.getX()-4, wp.getY()-4, 8, 8); g2.setColor(Color.BLACK); g2.drawOval(wp.getX()-4, wp.getY()-4, 8, 8); }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////package ui; // // پکیج UI
////
////import core.Point;
////import simulation.World; // // دنیا
////import infrastructure.*; // // Road/Lane/Intersection
////import core.*; // // Point/Vehicle/Direction
////import trafficcontrol.*; // // چراغ‌ها
////import pedestrian.*; // // عابر/گذرگاه
////
////import javax.swing.JPanel; // // پنل
////import javax.swing.ImageIcon; // // تصویر
////import java.awt.*; // // گرافیک/رنگ/قلم
////import java.awt.event.*; // // موس
////import java.awt.geom.AffineTransform; // // تبدیل‌ها
////import java.util.List; // // لیست‌ها
////
////public class SimulatorPanel extends JPanel { // // پنل شبیه‌سازی
////    private final World world; // // دنیا
////    private final Camera camera; // // دوربین
////    private Image carImg; // // تصویر خودرو (اختیاری)
////
////    private int lastDragX; // // آخرین X درگ
////    private int lastDragY; // // آخرین Y درگ
////    private boolean dragging = false; // // وضعیت درگ
////
////    public SimulatorPanel(World w, Camera cam) { // // سازنده
////        this.world = w; // // ذخیره دنیا
////        this.camera = cam; // // ذخیره دوربین
////        setBackground(UIConstants.BACKGROUND); // // بک‌گراند
////
////        addMouseWheelListener(new MouseWheelListener(){ @Override public void mouseWheelMoved(MouseWheelEvent e){ // // زوم با چرخ موس
////            double s = camera.getScale(); // // زوم فعلی
////            if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // // تغییر
////            camera.setScale(s); repaint(); // // اعمال
////        }});
////
////        addMouseListener(new MouseAdapter(){ // // شروع/پایان درگ
////            @Override public void mousePressed(MouseEvent e){ dragging = true; lastDragX = e.getX(); lastDragY = e.getY(); } // // شروع
////            @Override public void mouseReleased(MouseEvent e){ dragging = false; } // // پایان
////        });
////
////        addMouseMotionListener(new MouseMotionAdapter(){ @Override public void mouseDragged(MouseEvent e){ // // درگ
////            if (dragging){ int dx = e.getX()-lastDragX, dy = e.getY()-lastDragY; camera.pan(dx, dy); lastDragX = e.getX(); lastDragY = e.getY(); repaint(); } // // پن
////        }});
////    }
////
////    public void loadAssets(String carPath) { // // بارگذاری تصویر خودرو
////        if (carPath != null) {
////            java.net.URL url = getClass().getClassLoader().getResource(carPath); // // URL
////            if (url != null) carImg = new ImageIcon(url).getImage(); // // تصویر
////        }
////    }
////
////    @Override
////    protected void paintComponent(Graphics g) { // // رسم
////        super.paintComponent(g); // // پاک‌کردن
////        Graphics2D g2 = (Graphics2D) g; // // G2D
////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
////
////        AffineTransform old = g2.getTransform(); // // ذخیره تبدیل
////        g2.scale(camera.getScale(), camera.getScale()); // // زوم
////        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // // پن
////
////        drawRoads(g2);         // // راه‌ها
////        drawCrossings(g2);     // // گذرگاه‌ها
////        drawTrafficLights(g2); // // چراغ‌ها
////        drawVehicles(g2);      // // خودروها
////        drawPedestrians(g2);   // // عابرها
////
////        g2.setTransform(old); // // بازگشت
////    }
////
////    // ---------- کمک: محاسبهٔ ضخامت بصری راه ----------
////    private int roadTotalWidth(Road r){ // // کل پهنای راه
////        int perSide = Math.max(r.getForwardLanes().size(), r.getBackwardLanes().size()); // // حداکثر هر سمت
////        int totalLanes = r.isTwoWay() ? perSide * 2 : perSide; // // لِین کل
////        int innerGaps   = Math.max(0, totalLanes - 1); // // تعداد فاصله‌های داخلی
////        int w = totalLanes * UIConstants.LANE_WIDTH + innerGaps * UIConstants.LANE_GAP; // // جمع
////        if (r.isTwoWay()) w += UIConstants.LANE_GAP; // // شکاف وسط بین دو سمت
////        return Math.max(w, UIConstants.LANE_WIDTH * 2 + UIConstants.LANE_GAP); // // حداقل معقول
////    }
////
////    private void drawRoads(Graphics2D g2) { // // رسم جاده‌ها
////        List<Road> roads = world.getMap().getRoads(); // // راه‌ها
////        for (int i = 0; i < roads.size(); i++) { // // حلقه
////            Road r = roads.get(i); // // راه
////            Point A = r.getStartIntersection().getPosition(); // // A
////            Point B = r.getEndIntersection().getPosition();   // // B
////            int x1 = A.getX(), y1 = A.getY(), x2 = B.getX(), y2 = B.getY(); // // مختصات
////
////            // بدنهٔ آسفالت با ضخامت واقعی //
////            g2.setColor(UIConstants.ROAD_FILL); // // رنگ آسفالت
////            g2.setStroke(new BasicStroke(roadTotalWidth(r), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // // قلم ضخیم
////            g2.drawLine(x1, y1, x2, y2); // // خط اصلی
////
////            // خط‌چین‌های لِین: وسط راه (تقسیم جهت‌ها) //
////            g2.setColor(UIConstants.DASH); // // رنگ خط‌چین
////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f)); // // سبک خط‌چین
////            if (r.isTwoWay()) g2.drawLine(x1, y1, x2, y2); // // خط‌چین مرکزی
////
////            // خط‌چین‌های داخل هر سمت (برای ۲ لِین در هر جهت: خط در مرکز هر سمت) //
////            int perSide = Math.max(r.getForwardLanes().size(), r.getBackwardLanes().size()); // // تعداد هر سمت
////            if (perSide >= 2) { // // اگر بیش از یک لِین در هر سمت داریم
////                // نرمال چپ A→B //
////                double len = Math.hypot(x2 - x1, y2 - y1); // // طول
////                if (len > 1e-6) {
////                    double nx = -(y2 - y1) / len; // // نرمال x
////                    double ny =  (x2 - x1) / len; // // نرمال y
////                    // پهنای گروه یک سمت //
////                    double groupWidth = perSide * UIConstants.LANE_WIDTH + (perSide - 1) * UIConstants.LANE_GAP; // // پهنا
////                    double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // مرکز گروه
////                    // فاصله‌های موازی برای دو سمت //
////                    double offF =  sideCenter; // // سمت جلو (E/S)
////                    double offB = -sideCenter; // // سمت برگشت (W/N)
////                    // خطوط موازی //
////                    int fx1 = (int)Math.round(x1 + nx * offF), fy1 = (int)Math.round(y1 + ny * offF); // // نقطه ۱ موازی جلو
////                    int fx2 = (int)Math.round(x2 + nx * offF), fy2 = (int)Math.round(y2 + ny * offF); // // نقطه ۲ موازی جلو
////                    int bx1 = (int)Math.round(x1 + nx * offB), by1 = (int)Math.round(y1 + ny * offB); // // نقطه ۱ موازی عقب
////                    int bx2 = (int)Math.round(x2 + nx * offB), by2 = (int)Math.round(y2 + ny * offB); // // نقطه ۲ موازی عقب
////                    g2.drawLine(fx1, fy1, fx2, fy2); // // خط‌چین مرکز سمت جلو
////                    g2.drawLine(bx1, by1, bx2, by2); // // خط‌چین مرکز سمت عقب
////                }
////            }
////        }
////    }
////
////    private void drawTrafficLights(Graphics2D g2) { // // رسم چراغ‌ها در گوشهٔ رویکرد
////        List<Intersection> xs = world.getMap().getIntersections(); // // تقاطع‌ها
////        int r = 6; // // شعاع دایرهٔ چراغ
////        // تخمین نیم‌پهنای راه برای تعیین فاصلهٔ چراغ از مرکز تقاطع //
////        int halfRoad = Math.max(UIConstants.LANE_WIDTH, (UIConstants.LANE_WIDTH*2 + UIConstants.LANE_GAP)); // // پیش‌فرض
////        if (!world.getMap().getRoads().isEmpty()) halfRoad = roadTotalWidth(world.getMap().getRoads().get(0)) / 2; // // از اولین راه
////
////        int AHEAD = halfRoad + 6; // // جلوتر از مرکز
////        int SIDE  = Math.max(8, halfRoad - 8); // // کنار
////
////        for (int i = 0; i < xs.size(); i++) { // // روی همه تقاطع‌ها
////            Intersection it = xs.get(i); // // تقاطع
////            Point p = it.getPosition(); // // مرکز
////            for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) { // // جهت‌ها
////                TrafficControlDevice dev = it.getControl(d); // // کنترل جهت
////                if (!(dev instanceof TrafficLight)) continue; // // اگر چراغ نیست
////                TrafficLight tl = (TrafficLight) dev; // // چراغ
////
////                int cx = p.getX(), cy = p.getY(); // // مرکز
////                int lx = cx, ly = cy; // // مختصات چراغ
////                if (d == Direction.NORTH) { lx = cx - SIDE; ly = cy - AHEAD; } // // گوشهٔ شمال‌غرب
////                if (d == Direction.SOUTH) { lx = cx + SIDE; ly = cy + AHEAD; } // // گوشهٔ جنوب‌شرق
////                if (d == Direction.EAST)  { lx = cx + AHEAD; ly = cy - SIDE; } // // گوشهٔ شمال‌شرق
////                if (d == Direction.WEST)  { lx = cx - AHEAD; ly = cy + SIDE; } // // گوشهٔ جنوب‌غرب
////
////                Color c = Color.GRAY; // // رنگ
////                if (tl.getState() == LightState.RED)    c = Color.RED;    // // قرمز
////                if (tl.getState() == LightState.YELLOW) c = Color.ORANGE; // // زرد
////                if (tl.getState() == LightState.GREEN)  c = Color.GREEN;  // // سبز
////                g2.setColor(c); g2.fillOval(lx - r, ly - r, r*2, r*2); // // دایرهٔ چراغ
////                g2.setColor(Color.BLACK); g2.drawOval(lx - r, ly - r, r*2, r*2); // // حاشیه
////            }
////        }
////    }
////
////    private void drawVehicles(Graphics2D g2) { // // رسم خودروها
////        List<Vehicle> vs = world.getVehicles(); // // خودروها
////        for (int i = 0; i < vs.size(); i++) { // // حلقه
////            Vehicle v = vs.get(i); // // خودرو
////            if (v.getCurrentLane() == null) continue; // // بدون لِین نرین
////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // // نقطهٔ جهان
////            double angle = v.getAngle(); // // زاویه
////            int cx = wp.getX(), cy = wp.getY(); // // مرکز
////            int w = UIConstants.VEHICLE_LENGTH; // // طول
////            int h = UIConstants.VEHICLE_WIDTH;  // // عرض
////
////            AffineTransform save = g2.getTransform(); // // ذخیره تبدیل
////            g2.translate(cx, cy); // // انتقال
////            g2.rotate(angle); // // چرخش
////
////            if (carImg != null) { g2.drawImage(carImg, -w/2, -h/2, w, h, null); } // // تصویر
////            else { g2.setColor(new Color(30,144,255)); g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); g2.setColor(Color.BLACK); g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); } // // مستطیل
////
////            g2.setTransform(save); // // بازگشت
////        }
////    }
////
////    private void drawCrossings(Graphics2D g2){ // // رسم گذرگاه‌های عابر
////        List<PedestrianCrossing> cs = world.getCrossings(); // // لیست
////        g2.setColor(new Color(255,255,255,180)); // // سفید نیمه‌شفاف
////        for (int i = 0; i < cs.size(); i++) { // // حلقه
////            PedestrianCrossing c = cs.get(i); // // crossing
////            Point p = c.getIntersection().getPosition(); // // مرکز تقاطع
////            int stripe = 4; // // پهنای نوار
////            int len = 30;   // // طول نوار
////            int gap = 4;    // // فاصله نوارها
////            if (c.getDirection() == Direction.NORTH || c.getDirection() == Direction.SOUTH) { // // عبور شرق↔غرب
////                for (int k = -2; k <= 2; k++) g2.fillRect(p.getX() - len, p.getY() + k*(stripe+gap), len*2, stripe); // // نوارها
////            } else { // // عبور شمال↔جنوب
////                for (int k = -2; k <= 2; k++) g2.fillRect(p.getX() + k*(stripe+gap), p.getY() - len, stripe, len*2); // // نوارها
////            }
////        }
////    }
////
////    private void drawPedestrians(Graphics2D g2){ // // رسم عابرها
////        List<pedestrian.Pedestrian> ps = world.getPedestrians(); // // لیست
////        for (int i = 0; i < ps.size(); i++) { // // حلقه
////            pedestrian.Pedestrian p = ps.get(i); // // عابر
////            core.Point wp = p.getPosition(); // // مکان
////            g2.setColor(new Color(80,80,80)); g2.fillOval(wp.getX()-4, wp.getY()-4, 8, 8); // // نقطهٔ عابر
////            g2.setColor(Color.BLACK); g2.drawOval(wp.getX()-4, wp.getY()-4, 8, 8); // // حاشیه
////        }
////    }
////}
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
//////7777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
//////
//////package ui; // پکیج UI //
//////
//////import simulation.World; // دنیا //
//////import infrastructure.*; // Road/Lane/Intersection //
//////import core.*; // Point/Vehicle/Direction //
//////import trafficcontrol.*; // چراغ‌ها //
//////
//////import javax.swing.JPanel; // پنل //
//////import javax.swing.ImageIcon; // بارگذاری تصویر //
//////import java.awt.Graphics; // گرافیک //
//////import java.awt.Graphics2D; // گرافیک۲بعدی //
//////import java.awt.BasicStroke; // قلم خطوط //
//////import java.awt.RenderingHints; // آنتی‌الیاس //
//////import java.awt.event.*; // لیسنرهای موس //
//////import java.awt.geom.AffineTransform; // تبدیل‌ها //
//////import java.awt.Color; // رنگ //
//////import java.util.List; // لیست //
//////
//////public class SimulatorPanel extends JPanel { // پنل شبیه‌سازی //
//////    private final World world; // دنیا //
//////    private final Camera camera; // دوربین //
//////    private java.awt.Image carImg; // تصویر خودرو //
//////
//////    private int lastDragX; // آخرین X درگ //
//////    private int lastDragY; // آخرین Y درگ //
//////    private boolean dragging = false; // وضعیت درگ //
//////
//////    public SimulatorPanel(World w, Camera cam) { // سازنده //
//////        this.world = w; // ذخیره دنیا //
//////        this.camera = cam; // ذخیره دوربین //
//////        setBackground(UIConstants.BACKGROUND); // بک‌گراند //
//////
//////        addMouseWheelListener(new MouseWheelListener() { // لیسنر چرخ موس برای زوم //
//////            @Override public void mouseWheelMoved(MouseWheelEvent e) { // رخداد چرخش //
//////                double s = camera.getScale(); // زوم فعلی //
//////                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // تغییر زوم //
//////                camera.setScale(s); // اعمال زوم //
//////                repaint(); // باز-رسم //
//////            }
//////        });
//////
//////        addMouseListener(new MouseAdapter() { // کلیک برای شروع/پایان درگ //
//////            @Override public void mousePressed(MouseEvent e) { // فشردن موس //
//////                dragging = true; // شروع درگ //
//////                lastDragX = e.getX(); // ذخیره X //
//////                lastDragY = e.getY(); // ذخیره Y //
//////            }
//////            @Override public void mouseReleased(MouseEvent e) { // رها کردن موس //
//////                dragging = false; // پایان درگ //
//////            }
//////        });
//////
//////        addMouseMotionListener(new MouseMotionAdapter() { // حرکت موس //
//////            @Override public void mouseDragged(MouseEvent e) { // درگ موس //
//////                if (dragging) { // اگر درگ فعال //
//////                    int dx = e.getX() - lastDragX; // تغییر X //
//////                    int dy = e.getY() - lastDragY; // تغییر Y //
//////                    camera.pan(dx, dy); // پن دوربین //
//////                    lastDragX = e.getX(); // به‌روز X //
//////                    lastDragY = e.getY(); // به‌روز Y //
//////                    repaint(); // باز-رسم //
//////                }
//////            }
//////        });
//////    }
//////
//////    public void loadAssets(String carPath) { // بارگذاری تصویر //
//////        if (carPath != null) { // اگر مسیر داده شد //
//////            java.net.URL url = getClass().getClassLoader().getResource(carPath); // URL از کلاس‌پث //
//////            if (url != null) carImg = new ImageIcon(url).getImage(); // بارگذاری //
//////        }
//////    }
//////
//////    @Override
//////    protected void paintComponent(Graphics g) { // رسم //
//////        super.paintComponent(g); // پاک‌کردن //
//////        Graphics2D g2 = (Graphics2D) g; // تبدیل به G2D //
//////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // آنتی‌الیاس //
//////
//////        // تبدیل کلی: زوم و پن //
//////        AffineTransform old = g2.getTransform(); // ذخیره تبدیل //
//////        g2.scale(camera.getScale(), camera.getScale()); // مقیاس //
//////        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // جابه‌جایی //
//////
//////        drawRoads(g2); // رسم راه‌ها //
//////        drawTrafficLights(g2); // رسم چراغ‌ها //
//////        drawVehicles(g2); // رسم خودروها //
//////        drawAccidents(g2); // رسم برچسب‌های تصادف //
//////
//////        g2.setTransform(old); // برگرداندن تبدیل //
//////    }
//////
//////    private void drawRoads(Graphics2D g2) { // رسم جاده‌ها //
//////        g2.setStroke(new BasicStroke(1.0f)); // قلم باریک //
//////        for (int i = 0; i < world.getMap().getRoads().size(); i++) { // حلقه راه‌ها //
//////            Road r = world.getMap().getRoads().get(i); // جاده //
//////            Point A = r.getStartIntersection().getPosition(); // A //
//////            Point B = r.getEndIntersection().getPosition();   // B //
//////            int x1 = A.getX(); int y1 = A.getY(); // تبدیل به int //
//////            int x2 = B.getX(); int y2 = B.getY(); // تبدیل به int //
//////
//////            g2.setColor(UIConstants.ROAD_FILL); // رنگ آسفالت //
//////            g2.setStroke(new BasicStroke(UIConstants.LANE_WIDTH * 2 + UIConstants.LANE_GAP, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // ضخامت کل راه //
//////            g2.drawLine(x1, y1, x2, y2); // بدنۀ راه //
//////
//////            // خط‌چین //
//////            g2.setColor(UIConstants.DASH); // رنگ خط‌چین //
//////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f)); // الگوی خط‌چین //
//////            g2.drawLine(x1, y1, x2, y2); // رسم خط‌چین //
//////        }
//////    }
//////
//////    private void drawTrafficLights(Graphics2D g2) { // رسم چراغ‌ها //
//////        for (int i = 0; i < world.getTrafficLights().size(); i++) { // حلقه چراغ‌ها //
//////            TrafficLight tl = world.getTrafficLights().get(i); // چراغ //
//////            Intersection it = findIntersectionByLight(tl); // پیدا کردن تقاطعِ صاحبِ چراغ //
//////            if (it == null) continue; // اگر پیدا نشد رد شو //
//////            Point p = it.getPosition(); // ✅ موقعیت تقاطع؛ نه چراغ //
//////
//////            int r = 6; // شعاع نمایش //
//////            Color c = Color.GRAY; // رنگ پیش‌فرض //
//////            if (tl.getState() == LightState.RED)    c = Color.RED;     // قرمز //
//////            if (tl.getState() == LightState.YELLOW) c = Color.ORANGE;  // زرد //
//////            if (tl.getState() == LightState.GREEN)  c = Color.GREEN;   // سبز //
//////            g2.setColor(c); // ست رنگ //
//////            g2.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2); // دایره چراغ //
//////            g2.setColor(Color.BLACK); // حاشیه //
//////            g2.drawOval(p.getX() - r, p.getY() - r, r * 2, r * 2); // خط دور //
//////        }
//////    }
//////
//////    private Intersection findIntersectionByLight(TrafficLight tl) { // یافتن تقاطع چراغ //
//////        // دقیق و مطمئن: بین تمام تقاطع‌ها بگرد و اگر در controls آن چراغ بود، همان را برگردان //
//////        for (int i = 0; i < world.getMap().getIntersections().size(); i++) { // حلقه تقاطع‌ها //
//////            Intersection it = world.getMap().getIntersections().get(i); // تقاطع //
//////            for (TrafficControlDevice dev : it.getControls().values()) { // همه کنترل‌ها //
//////                if (dev == tl) return it; // اگر همین چراغ است، همین تقاطع //
//////            }
//////        }
//////        return null; // اگر پیدا نشد //
//////    }
//////
//////    private void drawVehicles(Graphics2D g2) { // رسم خودروها //
//////        for (int i = 0; i < world.getVehicles().size(); i++) { // حلقه خودروها //
//////            Vehicle v = world.getVehicles().get(i); // خودرو //
//////            if (v.getCurrentLane() == null) continue; // بدون لِین نرسم //
//////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // نقطه جهان //
//////            double angle = v.getAngle(); // زاویه حرکت //
//////            int cx = wp.getX(); int cy = wp.getY(); // مرکز //
//////            int w = UIConstants.VEHICLE_LENGTH; // طول //
//////            int h = UIConstants.VEHICLE_WIDTH;  // عرض //
//////
//////            AffineTransform save = g2.getTransform(); // ذخیره تبدیل //
//////            g2.translate(cx, cy); // انتقال مبدا //
//////            g2.rotate(angle); // چرخش //
//////
//////            if (carImg != null) { // اگر تصویر داریم //
//////                g2.drawImage(carImg, -w/2, -h/2, w, h, null); // رسم تصویر //
//////            } else { // در غیر این صورت مستطیل //
//////                g2.setColor(new Color(30, 144, 255)); // آبی //
//////                g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); // بدنه //
//////                g2.setColor(Color.BLACK); // حاشیه //
//////                g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); // خط دور //
//////            }
//////            g2.setTransform(save); // بازگرداندن تبدیل //
//////        }
//////    }
//////
//////    private void drawAccidents(Graphics2D g2) { // رسم برچسب تصادف //
//////        List<World.Accident> list = world.getActiveAccidents(); // ✅ گرفتن لیست تصادف‌های فعال //
//////        if (list == null || list.isEmpty()) return; // اگر خالیست //
//////        g2.setColor(Color.RED); // رنگ متن //
//////        for (int i = 0; i < list.size(); i++) { // حلقه //
//////            World.Accident a = list.get(i); // تصادف //
//////            Point p = a.position; // نقطه //
//////            g2.drawString("accident", p.getX() + 6, p.getY() - 6); // متن کنار محل //
//////        }
//////    }
//////}
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
////////package ui;
////////
////////import core.Vehicle;
////////import pedestrian.Pedestrian;
////////import core.Point;
////////import simulation.World;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.LightState;
////////
////////import javax.swing.*;
////////import java.awt.*;
////////
////////public class SimulatorPanel extends JPanel {
////////    private final World world;
////////
////////    public SimulatorPanel(World world) {
////////        this.world = world;
////////        setPreferredSize(new Dimension(1000, 700));
////////        setBackground(new Color(200, 220, 200));
////////
////////        // تایمر: هر 16ms آپدیت و رندر (تقریباً 60fps)
////////        Timer timer = new Timer(16, e -> {
////////            world.update();
////////            repaint();
////////        });
////////        timer.start();
////////    }
////////
////////    @Override
////////    protected void paintComponent(Graphics g) {
////////        super.paintComponent(g);
////////
////////        Graphics2D g2 = (Graphics2D) g;
////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
////////                RenderingHints.VALUE_ANTIALIAS_ON);
////////
////////        // ---------------- رسم ماشین‌ها ----------------
////////        g2.setColor(Color.BLUE);
////////        for (Vehicle v : world.getVehicles()) {
////////            Point pos = v.getPosition();
////////            int x = (int) pos.getX();
////////            int y = (int) pos.getY();
////////
////////            g2.fillRect(x - 5, y - 5, 10, 10);
////////        }
////////
////////        // ---------------- رسم عابرها ----------------
////////        g2.setColor(Color.MAGENTA);
////////        for (Pedestrian p : world.getPedestrians()) {
////////            Point pos = p.getPosition();
////////            int x = (int) pos.getX();
////////            int y = (int) pos.getY();
////////
////////            g2.fillOval(x - 3, y - 3, 6, 6);
////////        }
////////
////////        // ---------------- رسم چراغ‌ها ----------------
////////        for (TrafficLight tl : world.getTrafficLights()) {
////////            Point pos = tl.getPosition();
////////            int x = (int) pos.getX();
////////            int y = (int) pos.getY();
////////
////////            if (tl.getState() == LightState.GREEN) {
////////                g2.setColor(Color.GREEN);
////////            } else if (tl.getState() == LightState.YELLOW) {
////////                g2.setColor(Color.YELLOW);
////////            } else {
////////                g2.setColor(Color.RED);
////////            }
////////
////////            g2.fillOval(x - 6, y - 6, 12, 12);
////////        }
////////
////////        // ---------------- رسم تصادف‌ها (Pop کوتاه) ----------------
////////        world.getActiveAccidents().forEach(acc -> {
////////            int ax = (int) acc.x;
////////            int ay = (int) acc.y;
////////
////////            long remaining = acc.endTimeMs - System.currentTimeMillis();
////////            double scale = 1.0 + (remaining / 2000.0); // افکت پاپ
////////
////////            g2.setFont(getFont().deriveFont(Font.BOLD, (float)(14 * scale)));
////////            g2.setColor(Color.RED);
////////            g2.drawString("ACCIDENT", ax - 20, ay - 15);
////////        });
////////    }
////////}
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
////////package ui;
////////
////////import simulation.World;
////////import infrastructure.*;
////////import core.*;
////////import trafficcontrol.*;
////////import pedestrian.Pedestrian;
////////
////////import javax.swing.JPanel;
////////import javax.swing.ImageIcon;
////////import java.awt.Graphics;
////////import java.awt.Graphics2D;
////////import java.awt.BasicStroke;
////////import java.awt.RenderingHints;
////////import java.awt.event.*;
////////import java.awt.geom.AffineTransform;
////////import java.awt.Color;
////////import java.util.List;
////////
////////public class SimulatorPanel extends JPanel {
////////    private final World world;
////////    private final Camera camera;
////////    private java.awt.Image carImg;
////////
////////    private int lastDragX;
////////    private int lastDragY;
////////    private boolean dragging = false;
////////
////////    public SimulatorPanel(World w, Camera cam) {
////////        this.world = w;
////////        this.camera = cam;
////////        setBackground(UIConstants.BACKGROUND);
////////
////////        addMouseWheelListener(new MouseWheelListener() {
////////            @Override public void mouseWheelMoved(MouseWheelEvent e) {
////////                double s = camera.getScale();
////////                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1;
////////                camera.setScale(s);
////////                repaint();
////////            }
////////        });
////////
////////        addMouseListener(new MouseAdapter() {
////////            @Override public void mousePressed(MouseEvent e) {
////////                dragging = true;
////////                lastDragX = e.getX();
////////                lastDragY = e.getY();
////////            }
////////            @Override public void mouseReleased(MouseEvent e) {
////////                dragging = false;
////////            }
////////        });
////////
////////        addMouseMotionListener(new MouseMotionAdapter() {
////////            @Override public void mouseDragged(MouseEvent e) {
////////                if (dragging) {
////////                    int dx = e.getX() - lastDragX;
////////                    int dy = e.getY() - lastDragY;
////////                    camera.pan(dx, dy);
////////                    lastDragX = e.getX();
////////                    lastDragY = e.getY();
////////                    repaint();
////////                }
////////            }
////////        });
////////    }
////////
////////    public void loadAssets(String carPath) {
////////        if (carPath != null) {
////////            java.net.URL url = getClass().getClassLoader().getResource(carPath);
////////            if (url != null) carImg = new ImageIcon(url).getImage();
////////        }
////////    }
////////
////////    @Override
////////    protected void paintComponent(Graphics g) {
////////        super.paintComponent(g);
////////        Graphics2D g2 = (Graphics2D) g;
////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
////////
////////        AffineTransform old = g2.getTransform();
////////        g2.scale(camera.getScale(), camera.getScale());
////////        g2.translate(camera.getOffsetX(), camera.getOffsetY());
////////
////////        drawRoads(g2);
////////        drawTrafficLights(g2);
////////        drawVehicles(g2);
////////        drawPedestrians(g2);   // ✅ رسم عابر
////////        drawAccidents(g2);     // ✅ رسم تصادف
////////
////////        g2.setTransform(old);
////////    }
////////
////////    private void drawRoads(Graphics2D g2) {
////////        g2.setStroke(new BasicStroke(1.0f));
////////        for (Road r : world.getMap().getRoads()) {
////////            Point A = r.getStartIntersection().getPosition();
////////            Point B = r.getEndIntersection().getPosition();
////////            int x1 = A.getX(); int y1 = A.getY();
////////            int x2 = B.getX(); int y2 = B.getY();
////////
////////            g2.setColor(UIConstants.ROAD_FILL);
////////            g2.setStroke(new BasicStroke(UIConstants.LANE_WIDTH * 2 + UIConstants.LANE_GAP, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
////////            g2.drawLine(x1, y1, x2, y2);
////////
////////            g2.setColor(UIConstants.DASH);
////////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f));
////////            g2.drawLine(x1, y1, x2, y2);
////////        }
////////    }
////////
////////    private void drawTrafficLights(Graphics2D g2) {
////////        for (TrafficLight tl : world.getTrafficLights()) {
////////            infrastructure.Intersection it = findIntersectionByLight(tl);
////////            if (it == null) continue;
////////            Point p = it.getPosition();
////////            int r = 6;
////////            java.awt.Color c = java.awt.Color.GRAY;
////////            if (tl.getState() == LightState.RED)    c = java.awt.Color.RED;
////////            if (tl.getState() == LightState.YELLOW) c = java.awt.Color.ORANGE;
////////            if (tl.getState() == LightState.GREEN)  c = java.awt.Color.GREEN;
////////            g2.setColor(c);
////////            g2.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
////////            g2.setColor(java.awt.Color.BLACK);
////////            g2.drawOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
////////        }
////////    }
////////
////////    private infrastructure.Intersection findIntersectionByLight(TrafficLight tl) {
////////        return null; // فعلاً استفاده نمیشه
////////    }
////////
////////    private void drawVehicles(Graphics2D g2) {
////////        for (Vehicle v : world.getVehicles()) {
////////            if (v.getCurrentLane() == null) continue;
////////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
////////            double angle = v.getAngle();
////////            int cx = wp.getX(); int cy = wp.getY();
////////            int w = UIConstants.VEHICLE_LENGTH;
////////            int h = UIConstants.VEHICLE_WIDTH;
////////
////////            AffineTransform save = g2.getTransform();
////////            g2.translate(cx, cy);
////////            g2.rotate(angle);
////////
////////            if (carImg != null) {
////////                g2.drawImage(carImg, -w/2, -h/2, w, h, null);
////////            } else {
////////                g2.setColor(new Color(30, 144, 255));
////////                g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6);
////////                g2.setColor(Color.BLACK);
////////                g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6);
////////            }
////////
////////            if (v.isOvertaking() && v.isIndicatorVisible()) {
////////                g2.setColor(Color.YELLOW);
////////                g2.fillOval(w/2 - 4, -3, 6, 6);
////////                g2.fillOval(-w/2 - 2, -3, 6, 6);
////////            }
////////
////////            g2.setTransform(save);
////////        }
////////    }
////////
////////    // ---------- رسم عابر ----------
////////    private void drawPedestrians(Graphics2D g2) {
////////        g2.setColor(Color.RED);
////////        for (Pedestrian p : world.getPedestrians()) {
////////            Point pos = p.getPosition();
////////            g2.fillOval(pos.getX() - 4, pos.getY() - 4, 8, 8); // دایره کوچک قرمز
////////        }
////////    }
////////
////////    // ---------- رسم تصادف ----------
////////    private void drawAccidents(Graphics2D g2) {
////////        List<?> accs = world.getActiveAccidents();
////////        g2.setColor(Color.RED);
////////        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
////////        for (Object o : accs) {
////////            if (o instanceof World.Accident) {
////////                World.Accident a = (World.Accident) o;
////////                g2.drawString("ACCIDENT", (int)a.x, (int)a.y - 10);
////////            }
////////        }
////////    }
////////}
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
////////
////////package ui;
////////
////////import simulation.World;
////////import infrastructure.*;
////////import core.*;
////////import trafficcontrol.*;
////////
////////import javax.swing.JPanel;
////////import javax.swing.ImageIcon;
////////import java.awt.Graphics;
////////import java.awt.Graphics2D;
////////import java.awt.BasicStroke;
////////import java.awt.RenderingHints;
////////import java.awt.event.*;
////////import java.awt.geom.AffineTransform;
////////import java.awt.Color;
////////import java.util.List;
////////
////////public class SimulatorPanel extends JPanel {
////////    private final World world;
////////    private final Camera camera;
////////    private java.awt.Image carImg;
////////
////////    private int lastDragX;
////////    private int lastDragY;
////////    private boolean dragging = false;
////////
////////    public SimulatorPanel(World w, Camera cam) {
////////        this.world = w;
////////        this.camera = cam;
////////        setBackground(UIConstants.BACKGROUND);
////////
////////        addMouseWheelListener(new MouseWheelListener() {
////////            @Override public void mouseWheelMoved(MouseWheelEvent e) {
////////                double s = camera.getScale();
////////                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1;
////////                camera.setScale(s);
////////                repaint();
////////            }
////////        });
////////
////////        addMouseListener(new MouseAdapter() {
////////            @Override public void mousePressed(MouseEvent e) {
////////                dragging = true;
////////                lastDragX = e.getX();
////////                lastDragY = e.getY();
////////            }
////////            @Override public void mouseReleased(MouseEvent e) {
////////                dragging = false;
////////            }
////////        });
////////
////////        addMouseMotionListener(new MouseMotionAdapter() {
////////            @Override public void mouseDragged(MouseEvent e) {
////////                if (dragging) {
////////                    int dx = e.getX() - lastDragX;
////////                    int dy = e.getY() - lastDragY;
////////                    camera.pan(dx, dy);
////////                    lastDragX = e.getX();
////////                    lastDragY = e.getY();
////////                    repaint();
////////                }
////////            }
////////        });
////////    }
////////
////////    public void loadAssets(String carPath) {
////////        if (carPath != null) {
////////            java.net.URL url = getClass().getClassLoader().getResource(carPath);
////////            if (url != null) carImg = new ImageIcon(url).getImage();
////////        }
////////    }
////////
////////    @Override
////////    protected void paintComponent(Graphics g) {
////////        super.paintComponent(g);
////////        Graphics2D g2 = (Graphics2D) g;
////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
////////
////////        AffineTransform old = g2.getTransform();
////////        g2.scale(camera.getScale(), camera.getScale());
////////        g2.translate(camera.getOffsetX(), camera.getOffsetY());
////////
////////        drawRoads(g2);
////////        drawTrafficLights(g2);
////////        drawVehicles(g2);
////////        drawAccidents(g2); // ✅ رسم متن تصادف
////////
////////        g2.setTransform(old);
////////    }
////////
////////    private void drawRoads(Graphics2D g2) {
////////        g2.setStroke(new BasicStroke(1.0f));
////////        for (int i = 0; i < world.getMap().getRoads().size(); i++) {
////////            Road r = world.getMap().getRoads().get(i);
////////            Point A = r.getStartIntersection().getPosition();
////////            Point B = r.getEndIntersection().getPosition();
////////            int x1 = A.getX(); int y1 = A.getY();
////////            int x2 = B.getX(); int y2 = B.getY();
////////
////////            g2.setColor(UIConstants.ROAD_FILL);
////////            g2.setStroke(new BasicStroke(UIConstants.LANE_WIDTH * 2 + UIConstants.LANE_GAP, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
////////            g2.drawLine(x1, y1, x2, y2);
////////
////////            g2.setColor(UIConstants.DASH);
////////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f));
////////            g2.drawLine(x1, y1, x2, y2);
////////        }
////////    }
////////
////////    private void drawTrafficLights(Graphics2D g2) {
////////        for (int i = 0; i < world.getTrafficLights().size(); i++) {
////////            TrafficLight tl = world.getTrafficLights().get(i);
////////            infrastructure.Intersection it = findIntersectionByLight(tl);
////////            if (it == null) continue;
////////            Point p = it.getPosition();
////////            int r = 6;
////////            java.awt.Color c = java.awt.Color.GRAY;
////////            if (tl.getState() == LightState.RED)    c = java.awt.Color.RED;
////////            if (tl.getState() == LightState.YELLOW) c = java.awt.Color.ORANGE;
////////            if (tl.getState() == LightState.GREEN)  c = java.awt.Color.GREEN;
////////            g2.setColor(c);
////////            g2.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
////////            g2.setColor(java.awt.Color.BLACK);
////////            g2.drawOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
////////        }
////////    }
////////
////////    private infrastructure.Intersection findIntersectionByLight(TrafficLight tl) {
////////        return null;
////////    }
////////
////////    private void drawVehicles(Graphics2D g2) {
////////        for (int i = 0; i < world.getVehicles().size(); i++) {
////////            Vehicle v = world.getVehicles().get(i);
////////            if (v.getCurrentLane() == null) continue;
////////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
////////            double angle = v.getAngle();
////////            int cx = wp.getX(); int cy = wp.getY();
////////            int w = UIConstants.VEHICLE_LENGTH;
////////            int h = UIConstants.VEHICLE_WIDTH;
////////
////////            AffineTransform save = g2.getTransform();
////////            g2.translate(cx, cy);
////////            g2.rotate(angle);
////////
////////            if (carImg != null) {
////////                g2.drawImage(carImg, -w/2, -h/2, w, h, null);
////////            } else {
////////                g2.setColor(new Color(30, 144, 255));
////////                g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6);
////////                g2.setColor(Color.BLACK);
////////                g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6);
////////            }
////////
////////            // ✅ چراغ راهنما (چشمک زن)
////////            if (v.isOvertaking() && v.isIndicatorVisible()) {
////////                g2.setColor(Color.YELLOW);
////////                g2.fillOval(w/2 - 4, -3, 6, 6); // سمت راست
////////                g2.fillOval(-w/2 - 2, -3, 6, 6); // سمت چپ
////////            }
////////
////////            g2.setTransform(save);
////////        }
////////    }
////////
////////    // ---------- رسم تصادف ----------
////////    private void drawAccidents(Graphics2D g2) {
////////        List<?> accs = world.getActiveAccidents();
////////        g2.setColor(Color.RED);
////////        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
////////        for (Object o : accs) {
////////            if (o instanceof World.Accident) {
////////                World.Accident a = (World.Accident) o;
////////                g2.drawString("ACCIDENT", (int)a.x, (int)a.y - 10);
////////            }
////////        }
////////    }
////////}
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
//////////
//////////package ui; // // پکیج UI
//////////
//////////import simulation.World; // // دنیا
//////////import infrastructure.*; // // Road/Lane/Intersection
//////////import core.*; // // Point/Vehicle/Direction
//////////import trafficcontrol.*; // // چراغ‌ها
//////////
//////////import javax.swing.JPanel; // // پنل
//////////import javax.swing.ImageIcon; // // بارگذاری تصویر
//////////import java.awt.Graphics; // // گرافیک
//////////import java.awt.Graphics2D; // // گرافیک۲بعدی
//////////import java.awt.BasicStroke; // // قلم خطوط
//////////import java.awt.RenderingHints; // // آنتی‌الیاس
//////////import java.awt.event.*; // // لیسنرهای موس
//////////import java.awt.geom.AffineTransform; // // تبدیل‌ها
//////////import java.awt.Color; // // رنگ
//////////
//////////public class SimulatorPanel extends JPanel { // // پنل شبیه‌سازی
//////////    private final World world; // // دنیا
//////////    private final Camera camera; // // دوربین
//////////    private java.awt.Image carImg; // // تصویر خودرو
//////////
//////////    private int lastDragX; // // آخرین X درگ
//////////    private int lastDragY; // // آخرین Y درگ
//////////    private boolean dragging = false; // // وضعیت درگ
//////////
//////////    public SimulatorPanel(World w, Camera cam) { // // سازنده
//////////        this.world = w; // // ذخیره دنیا
//////////        this.camera = cam; // // ذخیره دوربین
//////////        setBackground(UIConstants.BACKGROUND); // // بک‌گراند
//////////
//////////        addMouseWheelListener(new MouseWheelListener() { // // لیسنر چرخ موس برای زوم
//////////            @Override public void mouseWheelMoved(MouseWheelEvent e) { // // رخداد چرخش
//////////                double s = camera.getScale(); // // زوم فعلی
//////////                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // // تغییر زوم
//////////                camera.setScale(s); // // اعمال زوم
//////////                repaint(); // // باز-رسم
//////////            }
//////////        });
//////////
//////////        addMouseListener(new MouseAdapter() { // // لیسنر کلیک برای شروع/پایان درگ
//////////            @Override public void mousePressed(MouseEvent e) { // // فشردن موس
//////////                dragging = true; // // شروع درگ
//////////                lastDragX = e.getX(); // // ذخیره X
//////////                lastDragY = e.getY(); // // ذخیره Y
//////////            }
//////////            @Override public void mouseReleased(MouseEvent e) { // // رها کردن موس
//////////                dragging = false; // // پایان درگ
//////////            }
//////////        });
//////////
//////////        addMouseMotionListener(new MouseMotionAdapter() { // // حرکت موس
//////////            @Override public void mouseDragged(MouseEvent e) { // // درگ موس
//////////                if (dragging) { // // اگر درگ فعال
//////////                    int dx = e.getX() - lastDragX; // // تغییر X
//////////                    int dy = e.getY() - lastDragY; // // تغییر Y
//////////                    camera.pan(dx, dy); // // پن دوربین
//////////                    lastDragX = e.getX(); // // به‌روز رسانی X
//////////                    lastDragY = e.getY(); // // به‌روز رسانی Y
//////////                    repaint(); // // باز-رسم
//////////                }
//////////            }
//////////        });
//////////    }
//////////
//////////    public void loadAssets(String carPath) { // // بارگذاری تصویر
//////////        if (carPath != null) { // // اگر مسیر داده شد
//////////            java.net.URL url = getClass().getClassLoader().getResource(carPath); // // گرفتن URL از کلاس‌پث
//////////            if (url != null) carImg = new ImageIcon(url).getImage(); // // بارگذاری
//////////        }
//////////    }
//////////
//////////    @Override
//////////    protected void paintComponent(Graphics g) { // // رسم
//////////        super.paintComponent(g); // // پاک‌کردن
//////////        Graphics2D g2 = (Graphics2D) g; // // تبدیل به G2D
//////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
//////////
//////////        // تبدیل کلی: زوم و پن
//////////        AffineTransform old = g2.getTransform(); // // ذخیره تبدیل قبلی
//////////        g2.scale(camera.getScale(), camera.getScale()); // // مقیاس
//////////        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // // جابه‌جایی
//////////
//////////        drawRoads(g2); // // رسم راه‌ها
//////////        drawTrafficLights(g2); // // رسم چراغ‌ها
//////////        drawVehicles(g2); // // رسم خودروها
//////////
//////////        g2.setTransform(old); // // برگرداندن تبدیل
//////////    }
//////////
//////////    private void drawRoads(Graphics2D g2) { // // رسم جاده‌ها
//////////        g2.setStroke(new BasicStroke(1.0f)); // // قلم باریک
//////////        for (int i = 0; i < world.getMap().getRoads().size(); i++) { // // حلقه راه‌ها
//////////            Road r = world.getMap().getRoads().get(i); // // جاده
//////////            Point A = r.getStartIntersection().getPosition(); // // A
//////////            Point B = r.getEndIntersection().getPosition();   // // B
//////////            int x1 = A.getX(); int y1 = A.getY(); // // تبدیل به int
//////////            int x2 = B.getX(); int y2 = B.getY(); // // تبدیل به int
//////////
//////////            g2.setColor(UIConstants.ROAD_FILL); // // رنگ آسفالت
//////////            g2.setStroke(new BasicStroke(UIConstants.LANE_WIDTH * 2 + UIConstants.LANE_GAP, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // // ضخامت کل راه
//////////            g2.drawLine(x1, y1, x2, y2); // // خط ضخیم به‌عنوان بدنه راه
//////////
//////////            // خط‌چین وسط هر جهت
//////////            g2.setColor(UIConstants.DASH); // // رنگ خط‌چین
//////////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f)); // // الگوی خط‌چین
//////////            g2.drawLine(x1, y1, x2, y2); // // رسم خط‌چین مرکزی
//////////        }
//////////    }
//////////
//////////    private void drawTrafficLights(Graphics2D g2) { // // رسم چراغ‌ها
//////////        for (int i = 0; i < world.getTrafficLights().size(); i++) { // // حلقه چراغ‌ها
//////////            TrafficLight tl = world.getTrafficLights().get(i); // // چراغ
//////////            infrastructure.Intersection it = findIntersectionByLight(tl); // // حدس موقعیت
//////////            if (it == null) continue; // // اگر پیدا نشد رد شو
//////////            Point p = it.getPosition(); // // مختصات تقاطع
//////////            int r = 6; // // شعاع نمایش
//////////            java.awt.Color c = java.awt.Color.GRAY; // // رنگ پیش‌فرض
//////////            if (tl.getState() == LightState.RED)    c = java.awt.Color.RED; // // قرمز
//////////            if (tl.getState() == LightState.YELLOW) c = java.awt.Color.ORANGE; // // زرد
//////////            if (tl.getState() == LightState.GREEN)  c = java.awt.Color.GREEN; // // سبز
//////////            g2.setColor(c); // // ست رنگ
//////////            g2.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2); // // دایره چراغ
//////////            g2.setColor(java.awt.Color.BLACK); // // حاشیه
//////////            g2.drawOval(p.getX() - r, p.getY() - r, r * 2, r * 2); // // خط دور
//////////        }
//////////    }
//////////
//////////    private infrastructure.Intersection findIntersectionByLight(TrafficLight tl) { // // کمک: یافتن تقاطع چراغ
//////////        // اینجا فرض کردیم id چراغ TL-<I-id>-<dir> باشد؛ اگر فرق دارد، بعداً مپ اتصال نگه می‌داریم
//////////        // برای الان از نزدیک‌ترین تقاطع با همان قطعه id استفاده می‌کنیم.
//////////        return null; // // ساده: نمایش چراغ وسط تقاطع‌ها در drawRoads هم کافیست؛ می‌تونیم بعداً کامل کنیم
//////////    }
//////////
//////////    private void drawVehicles(Graphics2D g2) { // // رسم خودروها
//////////        for (int i = 0; i < world.getVehicles().size(); i++) { // // حلقه خودروها
//////////            Vehicle v = world.getVehicles().get(i); // // خودرو
//////////            if (v.getCurrentLane() == null) continue; // // بدون لِین نرسم
//////////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // // نقطه جهان
//////////            double angle = v.getAngle(); // // زاویه حرکت
//////////            int cx = wp.getX(); int cy = wp.getY(); // // مرکز
//////////            int w = UIConstants.VEHICLE_LENGTH; // // طول نمایشی
//////////            int h = UIConstants.VEHICLE_WIDTH;  // // عرض نمایشی
//////////
//////////            AffineTransform save = g2.getTransform(); // // ذخیره تبدیل
//////////            g2.translate(cx, cy); // // انتقال مبدا
//////////            g2.rotate(angle); // // چرخش به جهت حرکت
//////////
//////////            if (carImg != null) { // // اگر تصویر داریم
//////////                g2.drawImage(carImg, -w/2, -h/2, w, h, null); // // رسم تصویر
//////////            } else { // // در غیر این صورت مستطیل
//////////                g2.setColor(new Color(30, 144, 255)); // // آبی
//////////                g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); // // بدنه
//////////                g2.setColor(Color.BLACK); // // حاشیه
//////////                g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); // // خط دور
//////////            }
//////////            g2.setTransform(save); // // بازگرداندن تبدیل
//////////        }
//////////    }
//////////}
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
////////////package ui; // // پکیج UI
////////////
////////////import simulation.World; // // دنیا
////////////import infrastructure.*; // // CityMap/Road/Intersection/Lane
////////////import core.*; // // Vehicle/Point/Direction
////////////import trafficcontrol.*; // // TrafficLight/LightState
////////////
////////////import javax.swing.JPanel; // // پنل
////////////import javax.swing.ImageIcon; // // بارگذاری تصویر
////////////import java.awt.Graphics; // // گرافیک
////////////import java.awt.Graphics2D; // // گرافیک ۲بعدی
////////////import java.awt.BasicStroke; // // قلم خطوط
////////////import java.awt.RenderingHints; // // آنتی‌الیاس
////////////import java.awt.event.*; // // لیسنرهای موس
////////////import java.awt.geom.AffineTransform; // // تبدیل‌ها
////////////import java.awt.Color; // // رنگ
////////////import java.util.List; // // لیست
////////////
////////////public class SimulatorPanel extends JPanel { // // پنل شبیه‌سازی
////////////    private final World world; // // دنیا
////////////    private final Camera camera; // // دوربین
////////////    private java.awt.Image carImg; // // تصویر خودرو
////////////
////////////    private int lastDragX; // // آخرین X درگ
////////////    private int lastDragY; // // آخرین Y درگ
////////////    private boolean dragging = false; // // وضعیت درگ
////////////
////////////    public SimulatorPanel(World w, Camera cam) { // // سازنده
////////////        this.world = w; // // ذخیره دنیا
////////////        this.camera = cam; // // ذخیره دوربین
////////////        setBackground(UIConstants.BACKGROUND); // // بک‌گراند
////////////
////////////        // --- زوم با اسکرول ---
////////////        addMouseWheelListener(new MouseWheelListener() { // // لیسنر چرخ موس
////////////            @Override public void mouseWheelMoved(MouseWheelEvent e) { // // رخداد چرخش
////////////                double s = camera.getScale(); // // زوم فعلی
////////////                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // // تغییر زوم
////////////                camera.setScale(s); // // اعمال زوم
////////////                repaint(); // // باز-رسم
////////////            }
////////////        });
////////////
////////////        // --- شروع/پایان درگ ---
////////////        addMouseListener(new MouseAdapter() { // // لیسنر کلیک
////////////            @Override public void mousePressed(MouseEvent e) { // // فشردن موس
////////////                dragging = true; // // شروع درگ
////////////                lastDragX = e.getX(); // // ذخیره X
////////////                lastDragY = e.getY(); // // ذخیره Y
////////////            }
////////////            @Override public void mouseReleased(MouseEvent e) { // // رها کردن موس
////////////                dragging = false; // // پایان درگ
////////////            }
////////////        });
////////////
////////////        // --- حرکت درگ ---
////////////        addMouseMotionListener(new MouseMotionAdapter() { // // حرکت موس
////////////            @Override public void mouseDragged(MouseEvent e) { // // درگ موس
////////////                if (dragging) { // // اگر درگ فعال
////////////                    int dx = e.getX() - lastDragX; // // تغییر X
////////////                    int dy = e.getY() - lastDragY; // // تغییر Y
////////////                    camera.pan(dx, dy); // // پن دوربین
////////////                    lastDragX = e.getX(); // // به‌روز رسانی X
////////////                    lastDragY = e.getY(); // // به‌روز رسانی Y
////////////                    repaint(); // // باز-رسم
////////////                }
////////////            }
////////////        });
////////////    }
////////////
////////////    public void loadAssets(String carPath) { // // بارگذاری تصویر خودرو
////////////        if (carPath != null) { // // اگر مسیر داده شد
////////////            java.net.URL url = getClass().getClassLoader().getResource(carPath); // // گرفتن URL از کلاس‌پث
////////////            if (url != null) carImg = new ImageIcon(url).getImage(); // // بارگذاری
////////////        }
////////////    }
////////////
////////////    @Override
////////////    protected void paintComponent(Graphics g) { // // رسم
////////////        super.paintComponent(g); // // پاک‌کردن
////////////        Graphics2D g2 = (Graphics2D) g; // // تبدیل به G2D
////////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
////////////
////////////        // تبدیل کلی: زوم و پن
////////////        AffineTransform old = g2.getTransform(); // // ذخیره تبدیل قبلی
////////////        g2.scale(camera.getScale(), camera.getScale()); // // مقیاس
////////////        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // // جابه‌جایی
////////////
////////////        drawRoads(g2);        // // رسم جاده‌ها
////////////        drawTrafficLights(g2); // // رسم چراغ‌ها
////////////        drawVehicles(g2);     // // رسم خودروها
////////////
////////////        g2.setTransform(old); // // برگرداندن تبدیل
////////////    }
////////////
////////////    // =================== رسم جاده‌ها ===================
////////////    private void drawRoads(Graphics2D g2) { // // رسم جاده‌ها
////////////        CityMap map = this.world.getCityMap(); if (map == null) return; // // نال‌چک
////////////        List<Road> roads = map.getRoads(); // // همهٔ جاده‌ها
////////////        for (int i = 0; i < roads.size(); i++) { // // حلقه
////////////            Road r = roads.get(i); // // جاده
////////////            Point A = r.getStart().getPosition(); // // نقطه A
////////////            Point B = r.getEnd().getPosition();   // // نقطه B
////////////
////////////            int lanesTotal = r.getForwardLanes().size() + r.getBackwardLanes().size(); // // تعداد لِین‌ها
////////////            float asphaltStroke = (float)(lanesTotal * UIConstants.LANE_WIDTH + UIConstants.LANE_GAP); // // ضخامت بدنه راه
////////////
////////////            g2.setColor(UIConstants.ROAD_FILL); // // رنگ آسفالت
////////////            g2.setStroke(new BasicStroke(asphaltStroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // // ضخامت کل راه
////////////            g2.drawLine(A.getX(), A.getY(), B.getX(), B.getY()); // // کشیدن بدنه راه
////////////
////////////            // خط‌چین مرکزی (صرفاً نمایش)
////////////            g2.setColor(UIConstants.DASH); // // رنگ خط‌چین
////////////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f)); // // الگوی خط‌چین
////////////            g2.drawLine(A.getX(), A.getY(), B.getX(), B.getY()); // // خط‌چین
////////////        }
////////////    }
////////////
////////////    // =================== رسم چراغ‌ها (گوشهٔ تقاطع) ===================
////////////    private void drawTrafficLights(Graphics2D g2) { // // رسم چراغ‌ها
////////////        List<TrafficLight> ls = this.world.getTrafficLights(); if (ls == null) return; // // لیست چراغ‌ها
////////////        CityMap map = this.world.getCityMap(); if (map == null) return; // // نقشه
////////////        List<Intersection> xs = map.getIntersections(); // // تقاطع‌ها
////////////
////////////        for (int i = 0; i < ls.size(); i++) { // // حلقه چراغ‌ها
////////////            TrafficLight tl = ls.get(i); // // چراغ
////////////            Intersection it = findIntersectionByLight(tl, xs); if (it == null) continue; // // تقاطع
////////////            Point pos = cornerPosition(it.getPosition(), tl.getDirectionControlled()); // // گوشهٔ مناسب
////////////            int r = 6; // // شعاع دایره
////////////            Color c = Color.GRAY; // // رنگ پیش‌فرض
////////////            if (tl.getState() == LightState.RED)    c = Color.RED;    // // قرمز
////////////            if (tl.getState() == LightState.YELLOW) c = Color.ORANGE; // // زرد
////////////            if (tl.getState() == LightState.GREEN)  c = Color.GREEN;  // // سبز
////////////            g2.setColor(c); g2.fillOval(pos.getX() - r, pos.getY() - r, r * 2, r * 2); // // دایره چراغ
////////////            g2.setColor(Color.BLACK); g2.drawOval(pos.getX() - r, pos.getY() - r, r * 2, r * 2); // // حاشیه
////////////        }
////////////    }
////////////
////////////    private Intersection findIntersectionByLight(TrafficLight tl, List<Intersection> xs) { // // یافتن تقاطع از id
////////////        String id = tl.getId(); if (id == null) return null; // // نال‌چک
////////////        if (!id.startsWith("TL-")) return null; // // الگو
////////////        int dash = id.lastIndexOf('-'); if (dash < 3) return null; // // جداکننده
////////////        String interId = id.substring(3, dash); // // استخراج id تقاطع
////////////        for (int i = 0; i < xs.size(); i++) { // // جست‌وجو
////////////            if (interId.equals(xs.get(i).getId())) return xs.get(i); // // تطابق
////////////        }
////////////        return null; // // پیدا نشد
////////////    }
////////////
////////////    private Point cornerPosition(Point center, Direction d) { // // محاسبه گوشه از مرکز
////////////        int off = UIConstants.LANE_WIDTH * 2 + 6; // // فاصله از مرکز تا گوشه
////////////        int x = center.getX(); int y = center.getY(); // // مرکز
////////////        // گوشهٔ «سبز» نسبت به جهت همان چراغ:
////////////        if (d == Direction.NORTH) { x += off; y -= off; }      // // شمال: بالا-راست
////////////        else if (d == Direction.SOUTH) { x -= off; y += off; } // // جنوب: پایین-چپ
////////////        else if (d == Direction.EAST)  { x += off; y += off; } // // شرق: پایین-راست
////////////        else if (d == Direction.WEST)  { x -= off; y -= off; } // // غرب: بالا-چپ
////////////        return new Point(x, y); // // خروجی
////////////    }
////////////
////////////    // =================== رسم خودروها ===================
////////////    private void drawVehicles(Graphics2D g2) { // // رسم خودروها
////////////        List<Vehicle> vs = this.world.getVehicles(); // // لیست خودروها
////////////        for (int i = 0; i < vs.size(); i++) { // // حلقه
////////////            Vehicle v = vs.get(i); // // خودرو
////////////            Lane ln = v.getCurrentLane(); if (ln == null) continue; // // بدون لِین نرسم
////////////            Point wp = ln.getPositionAt(v.getPositionInLane()); // // نقطه جهان روی لِین
////////////            double angle = v.getAngle(); // // زاویه حرکت
////////////            int cx = wp.getX(); int cy = wp.getY(); // // مرکز
////////////            int w = UIConstants.VEHICLE_LENGTH; // // طول نمایشی
////////////            int h = UIConstants.VEHICLE_WIDTH;  // // عرض نمایشی
////////////
////////////            AffineTransform save = g2.getTransform(); // // ذخیره تبدیل
////////////            g2.translate(cx, cy); // // انتقال مبدا
////////////            g2.rotate(angle); // // چرخش به جهت حرکت
////////////
////////////            if (carImg != null) { // // اگر تصویر داریم
////////////                g2.drawImage(carImg, -w/2, -h/2, w, h, null); // // رسم تصویر
////////////            } else { // // در غیر این صورت مستطیل
////////////                g2.setColor(new Color(30, 144, 255)); // // آبی
////////////                g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); // // بدنه
////////////                g2.setColor(Color.BLACK); // // حاشیه
////////////                g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); // // خط دور
////////////            }
////////////            g2.setTransform(save); // // بازگرداندن تبدیل
////////////        }
////////////    }
////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // // پکیج UI
//////////////
//////////////import simulation.World; // // دنیا
//////////////import infrastructure.*; // // Road/Lane/Intersection/CityMap
//////////////import core.*; // // Vehicle/Point
//////////////import trafficcontrol.*; // // TrafficLight/LightState
//////////////
//////////////import javax.swing.JPanel; // // پنل
//////////////import javax.swing.ImageIcon; // // تصویر
//////////////import java.awt.Graphics; // // گرافیک
//////////////import java.awt.Graphics2D; // // گرافیک۲بعدی
//////////////import java.awt.BasicStroke; // // قلم
//////////////import java.awt.RenderingHints; // // آنتی‌الیاس
//////////////import java.awt.event.*; // // رویدادها
//////////////import java.awt.geom.AffineTransform; // // تبدیل
//////////////import java.awt.Color; // // رنگ
//////////////import java.util.List; // // لیست
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل رسم
//////////////    private final World world; // // دنیا
//////////////    private final Camera camera; // // دوربین
//////////////    private java.awt.Image carImg; // // تصویر خودرو
//////////////    private int lastDragX, lastDragY; private boolean dragging=false; // // درگ
//////////////
//////////////    public SimulatorPanel(World w, Camera cam){ // // سازنده
//////////////        this.world=w; this.camera=cam; setBackground(UIConstants.BACKGROUND); // // ذخیره
//////////////        addMouseWheelListener(new MouseWheelListener(){ @Override public void mouseWheelMoved(MouseWheelEvent e){ double s=camera.getScale(); if(e.getWheelRotation()<0){s*=1.1;}else{s/=1.1;} camera.setScale(s); repaint(); }}); // // زوم
//////////////        addMouseListener(new MouseAdapter(){ @Override public void mousePressed(MouseEvent e){ dragging=true; lastDragX=e.getX(); lastDragY=e.getY(); } @Override public void mouseReleased(MouseEvent e){ dragging=false; }}); // // درگ
//////////////        addMouseMotionListener(new MouseMotionAdapter(){ @Override public void mouseDragged(MouseEvent e){ if(dragging){ int dx=e.getX()-lastDragX; int dy=e.getY()-lastDragY; camera.pan(dx,dy); lastDragX=e.getX(); lastDragY=e.getY(); repaint(); }}}); // // پن
//////////////    }
//////////////
//////////////    public void loadAssets(String carPath){ if(carPath!=null){ java.net.URL u=getClass().getClassLoader().getResource(carPath); if(u!=null){ carImg=new ImageIcon(u).getImage(); } } } // // بارگذاری
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g){ // // رسم
//////////////        super.paintComponent(g); Graphics2D g2=(Graphics2D)g; // // G2D
//////////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
//////////////        AffineTransform old=g2.getTransform(); g2.scale(camera.getScale(),camera.getScale()); g2.translate(camera.getOffsetX(),camera.getOffsetY()); // // تبدیل
//////////////        drawRoads(g2); drawTrafficLights(g2); drawVehicles(g2); // // اجزاء
//////////////        g2.setTransform(old); // // بازگردانی
//////////////    }
//////////////
//////////////    private void drawRoads(Graphics2D g2){ // // جاده‌ها
//////////////        CityMap map=this.world.getCityMap(); if(map==null) return; // // نال‌چک
//////////////        List<Road> roads=map.getRoads(); // // جاده‌ها
//////////////        for(int i=0;i<roads.size();i++){ Road r=roads.get(i); Point A=r.getStart().getPosition(); Point B=r.getEnd().getPosition(); // // A,B
//////////////            int lanesTotal=r.getForwardLanes().size()+r.getBackwardLanes().size(); // // تعداد لِین‌ها
//////////////            float asphaltStroke=(float)(lanesTotal*UIConstants.LANE_WIDTH+UIConstants.LANE_GAP); // // ضخامت
//////////////            g2.setColor(UIConstants.ROAD_FILL); g2.setStroke(new BasicStroke(asphaltStroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); g2.drawLine(A.getX(),A.getY(),B.getX(),B.getY()); // // بدنه
//////////////            g2.setColor(UIConstants.DASH); g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f,14f},0f)); g2.drawLine(A.getX(),A.getY(),B.getX(),B.getY()); // // خط‌چین
//////////////        }
//////////////    }
//////////////
//////////////    private void drawTrafficLights(Graphics2D g2){ // // چراغ‌ها
//////////////        List<TrafficLight> tls=this.world.getTrafficLights(); if(tls==null) return; // // لیست چراغ‌ها
//////////////        for(int i=0;i<tls.size();i++){ TrafficLight tl=tls.get(i); // // چراغ
//////////////            Intersection it=findIntersectionByLight(tl); if(it==null) continue; // // یافتن تقاطع
//////////////            Point p=it.getPosition(); int r=6; // // موقعیت و شعاع
//////////////            Color c=Color.GRAY; // // رنگ پیش‌فرض
//////////////            if(tl.getState()==LightState.RED) c=Color.RED; // // قرمز
//////////////            if(tl.getState()==LightState.YELLOW) c=Color.ORANGE; // // زرد
//////////////            if(tl.getState()==LightState.GREEN) c=Color.GREEN; // // سبز
//////////////            g2.setColor(c); g2.fillOval(p.getX()-r,p.getY()-r,r*2,r*2); // // دایره
//////////////            g2.setColor(Color.BLACK); g2.drawOval(p.getX()-r,p.getY()-r,r*2,r*2); // // حاشیه
//////////////        }
//////////////    }
//////////////
//////////////    private Intersection findIntersectionByLight(TrafficLight tl){ // // اتصال چراغ به تقاطع از روی id
//////////////        // قالب id چراغ: "TL-<INTERSECTION_ID>-<DIR>"
//////////////        String id=tl.getId(); if(id==null) return null; // // نال‌چک
//////////////        if(!id.startsWith("TL-")) return null; // // بررسی پیشوند
//////////////        int dash=id.lastIndexOf('-'); if(dash<3) return null; // // پیدا کردن جداکنندهٔ آخر
//////////////        String interId=id.substring(3, dash); // // استخراج id تقاطع
//////////////        CityMap map=this.world.getCityMap(); if(map==null) return null; // // نقشه
//////////////        List<Intersection> xs=map.getIntersections(); // // تقاطع‌ها
//////////////        for(int i=0;i<xs.size();i++){ if(interId.equals(xs.get(i).getId())) return xs.get(i); } // // جست‌وجو بر اساس id
//////////////        return null; // // پیدا نشد
//////////////    }
//////////////
//////////////    private void drawVehicles(Graphics2D g2){ // // خودروها
//////////////        List<Vehicle> vs=this.world.getVehicles(); for(int i=0;i<vs.size();i++){ Vehicle v=vs.get(i); Lane ln=v.getCurrentLane(); if(ln==null) continue; // // بدون لِین
//////////////            Point wp=ln.getPositionAt(v.getPositionInLane()); double angle=v.getAngle(); int cx=wp.getX(), cy=wp.getY(); int w=UIConstants.VEHICLE_LENGTH, h=UIConstants.VEHICLE_WIDTH; // // مختصات و ابعاد
//////////////            AffineTransform save=g2.getTransform(); g2.translate(cx,cy); g2.rotate(angle); // // تبدیل
//////////////            if(carImg!=null){ g2.drawImage(carImg,-w/2,-h/2,w,h,null); } else { g2.setColor(new Color(30,144,255)); g2.fillRoundRect(-w/2,-h/2,w,h,6,6); g2.setColor(Color.BLACK); g2.drawRoundRect(-w/2,-h/2,w,h,6,6); } // // بدنه
//////////////            g2.setTransform(save); // // بازگردانی
//////////////        }
//////////////    }
//////////////}
//////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // // پکیج UI
//////////////
//////////////import simulation.World; // // دنیا
//////////////import infrastructure.*; // // Road/Lane/Intersection
//////////////import core.*; // // Point/Vehicle/Direction
//////////////import trafficcontrol.*; // // چراغ‌ها
//////////////
//////////////import javax.swing.JPanel; // // پنل
//////////////import javax.swing.ImageIcon; // // بارگذاری تصویر
//////////////import java.awt.Graphics; // // گرافیک
//////////////import java.awt.Graphics2D; // // گرافیک۲بعدی
//////////////import java.awt.BasicStroke; // // قلم خطوط
//////////////import java.awt.RenderingHints; // // آنتی‌الیاس
//////////////import java.awt.event.*; // // لیسنرهای موس
//////////////import java.awt.geom.AffineTransform; // // تبدیل‌ها
//////////////import java.awt.Color; // // رنگ
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل شبیه‌سازی
//////////////    private final World world; // // دنیا
//////////////    private final Camera camera; // // دوربین
//////////////    private java.awt.Image carImg; // // تصویر خودرو
//////////////
//////////////    private int lastDragX; // // آخرین X درگ
//////////////    private int lastDragY; // // آخرین Y درگ
//////////////    private boolean dragging = false; // // وضعیت درگ
//////////////
//////////////    public SimulatorPanel(World w, Camera cam) { // // سازنده
//////////////        this.world = w; // // ذخیره دنیا
//////////////        this.camera = cam; // // ذخیره دوربین
//////////////        setBackground(UIConstants.BACKGROUND); // // بک‌گراند
//////////////
//////////////        addMouseWheelListener(new MouseWheelListener() { // // لیسنر چرخ موس برای زوم
//////////////            @Override public void mouseWheelMoved(MouseWheelEvent e) { // // رخداد چرخش
//////////////                double s = camera.getScale(); // // زوم فعلی
//////////////                if (e.getWheelRotation() < 0) s *= 1.1; else s /= 1.1; // // تغییر زوم
//////////////                camera.setScale(s); // // اعمال زوم
//////////////                repaint(); // // باز-رسم
//////////////            }
//////////////        });
//////////////
//////////////        addMouseListener(new MouseAdapter() { // // لیسنر کلیک برای شروع/پایان درگ
//////////////            @Override public void mousePressed(MouseEvent e) { // // فشردن موس
//////////////                dragging = true; // // شروع درگ
//////////////                lastDragX = e.getX(); // // ذخیره X
//////////////                lastDragY = e.getY(); // // ذخیره Y
//////////////            }
//////////////            @Override public void mouseReleased(MouseEvent e) { // // رها کردن موس
//////////////                dragging = false; // // پایان درگ
//////////////            }
//////////////        });
//////////////
//////////////        addMouseMotionListener(new MouseMotionAdapter() { // // حرکت موس
//////////////            @Override public void mouseDragged(MouseEvent e) { // // درگ موس
//////////////                if (dragging) { // // اگر درگ فعال
//////////////                    int dx = e.getX() - lastDragX; // // تغییر X
//////////////                    int dy = e.getY() - lastDragY; // // تغییر Y
//////////////                    camera.pan(dx, dy); // // پن دوربین
//////////////                    lastDragX = e.getX(); // // به‌روز رسانی X
//////////////                    lastDragY = e.getY(); // // به‌روز رسانی Y
//////////////                    repaint(); // // باز-رسم
//////////////                }
//////////////            }
//////////////        });
//////////////    }
//////////////
//////////////    public void loadAssets(String carPath) { // // بارگذاری تصویر
//////////////        if (carPath != null) { // // اگر مسیر داده شد
//////////////            java.net.URL url = getClass().getClassLoader().getResource(carPath); // // گرفتن URL از کلاس‌پث
//////////////            if (url != null) carImg = new ImageIcon(url).getImage(); // // بارگذاری
//////////////        }
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) { // // رسم
//////////////        super.paintComponent(g); // // پاک‌کردن
//////////////        Graphics2D g2 = (Graphics2D) g; // // تبدیل به G2D
//////////////        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
//////////////
//////////////        // تبدیل کلی: زوم و پن
//////////////        AffineTransform old = g2.getTransform(); // // ذخیره تبدیل قبلی
//////////////        g2.scale(camera.getScale(), camera.getScale()); // // مقیاس
//////////////        g2.translate(camera.getOffsetX(), camera.getOffsetY()); // // جابه‌جایی
//////////////
//////////////        drawRoads(g2); // // رسم راه‌ها
//////////////        drawTrafficLights(g2); // // رسم چراغ‌ها
//////////////        drawVehicles(g2); // // رسم خودروها
//////////////
//////////////        g2.setTransform(old); // // برگرداندن تبدیل
//////////////    }
//////////////
//////////////    private void drawRoads(Graphics2D g2) { // // رسم جاده‌ها
//////////////        g2.setStroke(new BasicStroke(1.0f)); // // قلم باریک
//////////////        for (int i = 0; i < world.getMap().getRoads().size(); i++) { // // حلقه راه‌ها
//////////////            Road r = world.getMap().getRoads().get(i); // // جاده
//////////////            Point A = r.getStartIntersection().getPosition(); // // A
//////////////            Point B = r.getEndIntersection().getPosition();   // // B
//////////////            int x1 = A.getX(); int y1 = A.getY(); // // تبدیل به int
//////////////            int x2 = B.getX(); int y2 = B.getY(); // // تبدیل به int
//////////////
//////////////            g2.setColor(UIConstants.ROAD_FILL); // // رنگ آسفالت
//////////////            g2.setStroke(new BasicStroke(UIConstants.LANE_WIDTH * 2 + UIConstants.LANE_GAP, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); // // ضخامت کل راه
//////////////            g2.drawLine(x1, y1, x2, y2); // // خط ضخیم به‌عنوان بدنه راه
//////////////
//////////////            // خط‌چین وسط هر جهت
//////////////            g2.setColor(UIConstants.DASH); // // رنگ خط‌چین
//////////////            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, new float[]{10f, 14f}, 0f)); // // الگوی خط‌چین
//////////////            g2.drawLine(x1, y1, x2, y2); // // رسم خط‌چین مرکزی
//////////////        }
//////////////    }
//////////////
//////////////    private void drawTrafficLights(Graphics2D g2) { // // رسم چراغ‌ها
//////////////        for (int i = 0; i < world.getTrafficLights().size(); i++) { // // حلقه چراغ‌ها
//////////////            TrafficLight tl = world.getTrafficLights().get(i); // // چراغ
//////////////            infrastructure.Intersection it = findIntersectionByLight(tl); // // حدس موقعیت
//////////////            if (it == null) continue; // // اگر پیدا نشد رد شو
//////////////            Point p = it.getPosition(); // // مختصات تقاطع
//////////////            int r = 6; // // شعاع نمایش
//////////////            java.awt.Color c = java.awt.Color.GRAY; // // رنگ پیش‌فرض
//////////////            if (tl.getState() == LightState.RED)    c = java.awt.Color.RED; // // قرمز
//////////////            if (tl.getState() == LightState.YELLOW) c = java.awt.Color.ORANGE; // // زرد
//////////////            if (tl.getState() == LightState.GREEN)  c = java.awt.Color.GREEN; // // سبز
//////////////            g2.setColor(c); // // ست رنگ
//////////////            g2.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2); // // دایره چراغ
//////////////            g2.setColor(java.awt.Color.BLACK); // // حاشیه
//////////////            g2.drawOval(p.getX() - r, p.getY() - r, r * 2, r * 2); // // خط دور
//////////////        }
//////////////    }
//////////////
//////////////    private infrastructure.Intersection findIntersectionByLight(TrafficLight tl) { // // کمک: یافتن تقاطع چراغ
//////////////        // اینجا فرض کردیم id چراغ TL-<I-id>-<dir> باشد؛ اگر فرق دارد، بعداً مپ اتصال نگه می‌داریم
//////////////        // برای الان از نزدیک‌ترین تقاطع با همان قطعه id استفاده می‌کنیم.
//////////////        return null; // // ساده: نمایش چراغ وسط تقاطع‌ها در drawRoads هم کافیست؛ می‌تونیم بعداً کامل کنیم
//////////////    }
//////////////
//////////////    private void drawVehicles(Graphics2D g2) { // // رسم خودروها
//////////////        for (int i = 0; i < world.getVehicles().size(); i++) { // // حلقه خودروها
//////////////            Vehicle v = world.getVehicles().get(i); // // خودرو
//////////////            if (v.getCurrentLane() == null) continue; // // بدون لِین نرسم
//////////////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // // نقطه جهان
//////////////            double angle = v.getAngle(); // // زاویه حرکت
//////////////            int cx = wp.getX(); int cy = wp.getY(); // // مرکز
//////////////            int w = UIConstants.VEHICLE_LENGTH; // // طول نمایشی
//////////////            int h = UIConstants.VEHICLE_WIDTH;  // // عرض نمایشی
//////////////
//////////////            AffineTransform save = g2.getTransform(); // // ذخیره تبدیل
//////////////            g2.translate(cx, cy); // // انتقال مبدا
//////////////            g2.rotate(angle); // // چرخش به جهت حرکت
//////////////
//////////////            if (carImg != null) { // // اگر تصویر داریم
//////////////                g2.drawImage(carImg, -w/2, -h/2, w, h, null); // // رسم تصویر
//////////////            } else { // // در غیر این صورت مستطیل
//////////////                g2.setColor(new Color(30, 144, 255)); // // آبی
//////////////                g2.fillRoundRect(-w/2, -h/2, w, h, 6, 6); // // بدنه
//////////////                g2.setColor(Color.BLACK); // // حاشیه
//////////////                g2.drawRoundRect(-w/2, -h/2, w, h, 6, 6); // // خط دور
//////////////            }
//////////////            g2.setTransform(save); // // بازگرداندن تبدیل
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // // پکیج ui
//////////////
//////////////import core.Direction; // // جهت‌ها
//////////////import core.Point; // // Point پروژه
//////////////import core.Vehicle; // // وسیله نقلیه
//////////////import infrastructure.CityMap; // // نقشه
//////////////import infrastructure.Intersection; // // تقاطع
//////////////import infrastructure.Lane; // // لِین
//////////////import infrastructure.Road; // // جاده
//////////////import pedestrian.Pedestrian; // // عابر
//////////////import simulation.World; // // دنیا
//////////////import trafficcontrol.LightState; // // وضعیت چراغ
//////////////import trafficcontrol.TrafficControlDevice; // // کنترل ترافیک
//////////////import trafficcontrol.TrafficLight; // // چراغ راهنما
//////////////
//////////////import javax.imageio.ImageIO; // // لود تصویر
//////////////import javax.swing.JPanel; // // پنل
//////////////import java.awt.BasicStroke; // // قلم خطوط
//////////////import java.awt.Color; // // رنگ
//////////////import java.awt.Cursor; // // نشانگر موس
//////////////import java.awt.Dimension; // // اندازه
//////////////import java.awt.Graphics; // // گرافیک
//////////////import java.awt.Graphics2D; // // گرافیک دو بعدی
//////////////import java.awt.RenderingHints; // // آنتی‌الیاس
//////////////import java.awt.event.MouseAdapter; // // آداپتور موس
//////////////import java.awt.event.MouseEvent; // // رویداد موس
//////////////import java.awt.event.MouseWheelEvent; // // چرخ موس
//////////////import java.awt.Image; // // تصویر
//////////////import java.io.File; // // فایل
//////////////import java.io.InputStream; // // استریم
//////////////import java.util.ArrayList; // // اسنپ‌شات لیست
//////////////import java.util.List; // // لیست
//////////////import java.util.Random; // // رندوم
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل رندر
//////////////    private final World world; // // مرجع دنیا
//////////////    private final Camera camera; // // دوربین
//////////////
//////////////    private Image carImage; // // تصویر ماشین
//////////////    private Image pedestrianImage; // // تصویر عابر (اختیاری)
//////////////
//////////////    private final Random decoRnd = new Random(123); // // رندوم تزئینات
//////////////    private int dragStartX, dragStartY; // // شروع درگ موس
//////////////    private int savedOffsetX, savedOffsetY; // // آفست‌های قبل از درگ
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) { // // سازنده
//////////////        this.world = world; // // ذخیره دنیا
//////////////        this.camera = camera; // // ذخیره دوربین
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT)); // // اندازه پنل
//////////////        setBackground(new Color(80, 150, 80)); // // چمن پس‌زمینه
//////////////        setFocusable(true); // // فوکوس‌پذیری
//////////////
//////////////        enableMousePanAndZoom(); // // فعال‌سازی پَن و زوم با موس
//////////////    }
//////////////
//////////////    private void enableMousePanAndZoom() { // // ماوس برای پَن/زوم
//////////////        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); // // نشانگر حرکت
//////////////
//////////////        addMouseListener(new MouseAdapter() { // // لیسنر موس
//////////////            @Override public void mousePressed(MouseEvent e) { // // فشردن
//////////////                dragStartX = e.getX(); dragStartY = e.getY(); // // ذخیره مکان شروع
//////////////                savedOffsetX = camera.getOffsetX(); savedOffsetY = camera.getOffsetY(); // // ذخیره آفست‌ها
//////////////                requestFocusInWindow(); // // گرفتن فوکوس
//////////////            }
//////////////        });
//////////////
//////////////        addMouseMotionListener(new MouseAdapter() { // // حرکت موس
//////////////            @Override public void mouseDragged(MouseEvent e) { // // درگ
//////////////                int dx = e.getX() - dragStartX; // // دلتا X
//////////////                int dy = e.getY() - dragStartY; // // دلتا Y
//////////////                camera.panBy(dx, dy); // // پَن
//////////////                dragStartX = e.getX(); dragStartY = e.getY(); // // به‌روزرسانی شروع
//////////////                repaint(); // // رفرش
//////////////            }
//////////////        });
//////////////
//////////////        addMouseWheelListener(new MouseAdapter() { // // چرخ موس
//////////////            @Override public void mouseWheelMoved(MouseWheelEvent e) { // // رویداد چرخ
//////////////                if (e.getWheelRotation() < 0) camera.zoomIn(e.getX(), e.getY()); // // بالا=زوم داخل
//////////////                else camera.zoomOut(e.getX(), e.getY()); // // پایین=زوم خارج
//////////////                repaint(); // // رفرش
//////////////            }
//////////////        });
//////////////    }
//////////////
//////////////    public void loadAssets(String carPathOrResource, String pedPathOrResource) { // // لود تصاویر
//////////////        this.carImage        = loadImageFlexible(carPathOrResource); // // لود ماشین
//////////////        this.pedestrianImage = loadImageFlexible(pedPathOrResource); // // لود عابر
//////////////    }
//////////////
//////////////    private Image loadImageFlexible(String path) { // // لود از resources یا فایل
//////////////        if (path == null) return null; // // اگر مقدار ندارد
//////////////        try {
//////////////            String p = path.startsWith("/") ? path : ("/" + path); // // مطمئن شدن از داشتن '/'
//////////////            InputStream in = SimulatorPanel.class.getResourceAsStream(p); // // تلاش از کلاس‌پس
//////////////            if (in != null) { try { return ImageIO.read(in); } finally { in.close(); } } // // خواندن
//////////////            File f = new File(path); if (f.exists()) return ImageIO.read(f); // // تلاش فایل محلی
//////////////        } catch (Exception ex) { System.out.println("Asset load failed: " + path + " -> " + ex.getMessage()); } // // گزارش خطا
//////////////        return null; // // بازگشت null
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) { // // رسم
//////////////        super.paintComponent(g); // // پاک کردن پس‌زمینه
//////////////        Graphics2D g2d = (Graphics2D) g; // // تبدیل به G2D
//////////////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
//////////////        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // // اینترپولیشن
//////////////
//////////////        drawDecorations(g2d); // // ساختمان/درخت
//////////////        drawRoads(g2d); // // خیابان‌ها
//////////////        drawTrafficLights(g2d); // // چراغ‌ها
//////////////        drawPedestrians(g2d); // // عابرها
//////////////        drawVehicles(g2d); // // خودروها
//////////////    }
//////////////
//////////////    private void drawDecorations(Graphics2D g2d) { // // تزئینات پس‌زمینه
//////////////        CityMap map = world.getCityMap(); if (map == null) return; // // اگر نقشه نیست
//////////////        List<Intersection> inters = map.getIntersections(); if (inters.size() < 4) return; // // اگر کم است
//////////////        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE; // // حداقل‌ها
//////////////        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE; // // حداکثرها
//////////////        for (int i = 0; i < inters.size(); i++) { // // برای هر تقاطع
//////////////            Point p = inters.get(i).getPosition(); // // موقعیت
//////////////            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX()); // // به‌روزرسانی X
//////////////            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY()); // // به‌روزرسانی Y
//////////////        }
//////////////        decoRnd.setSeed(1); // // ثابت بودن الگو
//////////////        for (int i = 0; i < 40; i++) { // // ۴۰ المان
//////////////            int wx = minX + decoRnd.nextInt(Math.max(1, (maxX - minX))); // // X دنیا
//////////////            int wy = minY + decoRnd.nextInt(Math.max(1, (maxY - minY))); // // Y دنیا
//////////////            Point sp = camera.transform(new Point(wx, wy)); // // به صفحه
//////////////            if (i % 3 == 0) { // // ساختمان
//////////////                int w = 26 + decoRnd.nextInt(30); int h = 26 + decoRnd.nextInt(30); // // اندازه
//////////////                g2d.setColor(new Color(215, 215, 215)); g2d.fillRect(sp.getX(), sp.getY(), w, h); // // پر
//////////////                g2d.setColor(new Color(170,170,170)); g2d.drawRect(sp.getX(), sp.getY(), w, h); // // دور
//////////////            } else { // // درخت
//////////////                int r = 6 + decoRnd.nextInt(10); // // شعاع
//////////////                g2d.setColor(new Color(36, 128, 36)); g2d.fillOval(sp.getX()-r, sp.getY()-r, r*2, r*2); // // پر
//////////////                g2d.setColor(new Color(20, 90, 20)); g2d.drawOval(sp.getX()-r, sp.getY()-r, r*2, r*2); // // دور
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    private void drawRoads(Graphics2D g2d) { // // رسم راه‌ها
//////////////        CityMap map = world.getCityMap(); if (map == null) return; // // اگر نقشه نیست
//////////////        List<Road> roads = map.getRoads(); // // راه‌ها
//////////////        for (int i = 0; i < roads.size(); i++) { // // هر راه
//////////////            Road r = roads.get(i); // // راه
//////////////            Point aW = r.getStartIntersection().getPosition(); // // شروع دنیا
//////////////            Point bW = r.getEndIntersection().getPosition(); // // پایان دنیا
//////////////            Point aS = camera.transform(aW); // // شروع صفحه
//////////////            Point bS = camera.transform(bW); // // پایان صفحه
//////////////
//////////////            double dx = bS.getX() - aS.getX(); double dy = bS.getY() - aS.getY(); // // بردار
//////////////            double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////            double nx = -dy / len, ny = dx / len; // // نرمال واحد
//////////////
//////////////            int lanesA = Math.max(1, r.getForwardLanes().size()); // // تعداد لِین A
//////////////            int lanesB = Math.max(1, r.getBackwardLanes().size()); // // تعداد لِین B
//////////////            float widthA = lanesA * UIConstants.LANE_WIDTH + (lanesA - 1) * UIConstants.LANE_GAP; // // پهنا A
//////////////            float widthB = lanesB * UIConstants.LANE_WIDTH + (lanesB - 1) * UIConstants.LANE_GAP; // // پهنا B
//////////////            float halfSep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f; // // نصف فاصله وسط
//////////////            float offA = (widthA * 0.5f) + halfSep; // // آفست A از مرکز
//////////////            float offB = (widthB * 0.5f) + halfSep; // // آفست B از مرکز
//////////////
//////////////            int aAx = (int)Math.round(aS.getX() + nx * (-offA)); int aAy = (int)Math.round(aS.getY() + ny * (-offA)); // // مرز A1
//////////////            int bAx = (int)Math.round(bS.getX() + nx * (-offA)); int bAy = (int)Math.round(bS.getY() + ny * (-offA)); // // مرز A2
//////////////            int aBx = (int)Math.round(aS.getX() + nx * (+offB)); int aBy = (int)Math.round(aS.getY() + ny * (+offB)); // // مرز B1
//////////////            int bBx = (int)Math.round(bS.getX() + nx * (+offB)); int bBy = (int)Math.round(bS.getY() + ny * (+offB)); // // مرز B2
//////////////
//////////////            g2d.setStroke(new BasicStroke(widthA)); g2d.setColor(UIConstants.ROAD_COLOR); g2d.drawLine(aAx, aAy, bAx, bAy); // // آسفالت A
//////////////            g2d.setStroke(new BasicStroke(widthB)); g2d.setColor(UIConstants.ROAD_COLOR); g2d.drawLine(aBx, aBy, bBx, bBy); // // آسفالت B
//////////////
//////////////            drawEdgeLines(g2d, aAx, aAy, bAx, bAy, widthA); // // خطوط کنار A
//////////////            drawEdgeLines(g2d, aBx, aBy, bBx, bBy, widthB); // // خطوط کنار B
//////////////            if (lanesA >= 2) drawLaneDashes(g2d, aAx, aAy, bAx, bAy, widthA, lanesA); // // خط‌چین داخل A
//////////////            if (lanesB >= 2) drawLaneDashes(g2d, aBx, aBy, bBx, bBy, widthB, lanesB); // // خط‌چین داخل B
//////////////
//////////////            drawDoubleYellow(g2d, aS.getX(), aS.getY(), bS.getX(), bS.getY()); // // دو خط زرد وسط
//////////////        }
//////////////    }
//////////////
//////////////    private void drawEdgeLines(Graphics2D g2d, int ax, int ay, int bx, int by, float width) { // // خط سفید کنار
//////////////        g2d.setColor(UIConstants.EDGE_LINE_COLOR); g2d.setStroke(new BasicStroke(2f)); // // تنظیم قلم
//////////////        double dx = bx - ax, dy = by - ay; double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////        double nx = -dy / len, ny = dx / len; float half = width * 0.5f; // // نرمال و نیم‌پهنای راه
//////////////
//////////////        int axL = (int)Math.round(ax + nx * (-half)); int ayL = (int)Math.round(ay + ny * (-half)); // // لبه چپ
//////////////        int bxL = (int)Math.round(bx + nx * (-half)); int byL = (int)Math.round(by + ny * (-half)); // // انتهای چپ
//////////////        g2d.drawLine(axL, ayL, bxL, byL); // // رسم خط
//////////////
//////////////        int axR = (int)Math.round(ax + nx * (+half)); int ayR = (int)Math.round(ay + ny * (+half)); // // لبه راست
//////////////        int bxR = (int)Math.round(bx + nx * (+half)); int byR = (int)Math.round(by + ny * (+half)); // // انتهای راست
//////////////        g2d.drawLine(axR, ayR, bxR, byR); // // رسم خط
//////////////    }
//////////////
//////////////    private void drawLaneDashes(Graphics2D g2d, int ax, int ay, int bx, int by, float width, int laneCount) { // // خط‌چین داخلی
//////////////        double dx = bx - ax, dy = by - ay; double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////        double nx = -dy / len, ny = dx / len; float half = width * 0.5f; // // نرمال و نیم‌پهنای راه
//////////////        g2d.setColor(UIConstants.LANE_DASH_COLOR); float dash[] = {14f, 10f}; // // رنگ و الگوی خط‌چین
//////////////        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f)); // // قلم خط‌چین
//////////////        for (int i = 1; i <= laneCount - 1; i++) { // // بین لِین‌ها
//////////////            float fromLeft = (i * UIConstants.LANE_WIDTH) + (i - 0.5f) * UIConstants.LANE_GAP - half; // // آفست
//////////////            int axM = (int)Math.round(ax + nx * fromLeft); int ayM = (int)Math.round(ay + ny * fromLeft); // // نقطه شروع
//////////////            int bxM = (int)Math.round(bx + nx * fromLeft); int byM = (int)Math.round(by + ny * fromLeft); // // نقطه پایان
//////////////            g2d.drawLine(axM, ayM, bxM, byM); // // رسم
//////////////        }
//////////////    }
//////////////
//////////////    private void drawDoubleYellow(Graphics2D g2d, int ax, int ay, int bx, int by) { // // دو خط زرد وسط
//////////////        g2d.setColor(UIConstants.CENTER_LINE_COLOR); g2d.setStroke(new BasicStroke(2.2f)); // // رنگ و ضخامت
//////////////        double dx = bx - ax, dy = by - ay; double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////        double nx = -dy / len, ny = dx / len; float sep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f; // // فاصله از مرکز
//////////////        int ax1 = (int)Math.round(ax + nx * (-sep)); int ay1 = (int)Math.round(ay + ny * (-sep)); // // خط ۱
//////////////        int bx1 = (int)Math.round(bx + nx * (-sep)); int by1 = (int)Math.round(by + ny * (-sep)); // // انتها ۱
//////////////        g2d.drawLine(ax1, ay1, bx1, by1); // // رسم
//////////////        int ax2 = (int)Math.round(ax + nx * (+sep)); int ay2 = (int)Math.round(ay + ny * (+sep)); // // خط ۲
//////////////        int bx2 = (int)Math.round(bx + nx * (+sep)); int by2 = (int)Math.round(by + ny * (+sep)); // // انتها 2
//////////////        g2d.drawLine(ax2, ay2, bx2, by2); // // رسم
//////////////    }
//////////////
//////////////    private void drawTrafficLights(Graphics2D g2d) { // // رسم چراغ‌ها
//////////////        CityMap map = world.getCityMap(); if (map == null) return; // // اگر نقشه نیست
//////////////        List<Intersection> inters = map.getIntersections(); if (inters == null) return; // // لیست
//////////////        for (int i = 0; i < inters.size(); i++) { // // هر تقاطع
//////////////            Intersection it = inters.get(i); // // تقاطع
//////////////            Point sp = camera.transform(it.getPosition()); // // موقعیت صفحه
//////////////            drawLightIfPresent(g2d, it, Direction.NORTH, sp.getX(), sp.getY() - 28); // // چراغ بالا
//////////////            drawLightIfPresent(g2d, it, Direction.SOUTH, sp.getX(), sp.getY() + 16); // // چراغ پایین
//////////////            drawLightIfPresent(g2d, it, Direction.EAST,  sp.getX() + 16, sp.getY()); // // چراغ راست
//////////////            drawLightIfPresent(g2d, it, Direction.WEST,  sp.getX() - 28, sp.getY()); // // چراغ چپ
//////////////        }
//////////////    }
//////////////
//////////////    private void drawLightIfPresent(Graphics2D g2d, Intersection it, Direction dir, int x, int y) { // // کمک رسم چراغ
//////////////        try {
//////////////            TrafficControlDevice dev = it.getControl(dir); // // گرفتن کنترل جهت
//////////////            if (dev instanceof TrafficLight) { // // اگر چراغ بود
//////////////                TrafficLight tl = (TrafficLight) dev; // // تبدیل
//////////////                Color c = tl.getState() == LightState.RED ? Color.RED :
//////////////                        (tl.getState() == LightState.YELLOW ? Color.YELLOW : Color.GREEN); // // انتخاب رنگ
//////////////                g2d.setColor(c); g2d.fillOval(x, y, 12, 12); // // پر
//////////////                g2d.setColor(Color.BLACK); g2d.drawOval(x, y, 12, 12); // // دور
//////////////            }
//////////////        } catch (Throwable ignore) { /* سازگاری با نسخه‌های مختلف Intersection */ } // // بی‌اثر
//////////////    }
//////////////
//////////////    private void drawPedestrians(Graphics2D g2d) { // // عابرها
//////////////        List<Pedestrian> ps = world.getPedestrians(); if (ps == null || ps.isEmpty()) return; // // اگر نیست
//////////////        List<Pedestrian> copy = new ArrayList<Pedestrian>(ps); // // اسنپ‌شات برای thread-safety
//////////////        for (int i = 0; i < copy.size(); i++) { // // هر عابر
//////////////            Pedestrian p = copy.get(i); // // عابر
//////////////            Point sp = camera.transform(p.getPosition()); // // موقعیت صفحه
//////////////            if (pedestrianImage != null) { g2d.drawImage(pedestrianImage, sp.getX()-6, sp.getY()-6, 12, 12, null); } // // تصویر
//////////////            else { g2d.setColor(UIConstants.PEDESTRIAN_COLOR); g2d.fillOval(sp.getX()-4, sp.getY()-4, 8, 8); } // // دایره ساده
//////////////        }
//////////////    }
//////////////
//////////////    private void drawVehicles(Graphics2D g2d) { // // خودروها
//////////////        List<Vehicle> vs = world.getVehicles(); if (vs == null || vs.isEmpty()) return; // // اگر نیست
//////////////        List<Vehicle> copy = new ArrayList<Vehicle>(vs); // // اسنپ‌شات
//////////////        for (int i = 0; i < copy.size(); i++) { // // هر خودرو
//////////////            Vehicle v = copy.get(i); // // خودرو
//////////////            Lane lane = v.getCurrentLane(); if (lane == null) continue; // // اگر لِین ندارد
//////////////            Point wp = lane.getPositionAt(v.getPositionInLane()); // // نقطه دنیا
//////////////            Point sp = camera.transform(wp); // // نقطه صفحه
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH; int h = UIConstants.VEHICLE_HEIGHT; // // ابعاد خودرو
//////////////            Graphics2D g2 = (Graphics2D) g2d.create(); // // کپی گرافیک
//////////////            g2.rotate(v.getAngle(), sp.getX(), sp.getY()); // // چرخش حول مرکز
//////////////
//////////////            if (carImage != null) g2.drawImage(carImage, sp.getX()-w/2, sp.getY()-h/2, w, h, null); // // تصویر
//////////////            else { g2.setColor(UIConstants.CAR_COLOR); g2.fillRect(sp.getX()-w/2, sp.getY()-h/2, w, h); } // // مستطیل ساده
//////////////
//////////////            g2.dispose(); // // آزادسازی
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // // پنل رندر
//////////////
//////////////import core.Direction; // // جهت‌ها
//////////////import core.Point; // // نقطه‌ی پروژه خودمان
//////////////import core.Vehicle; // // کلاس وسیله نقلیه
//////////////import infrastructure.CityMap; // // نقشه شهر
//////////////import infrastructure.Intersection; // // تقاطع
//////////////import infrastructure.Lane; // // لِین
//////////////import infrastructure.Road; // // جاده
//////////////import pedestrian.Pedestrian; // // عابر
//////////////import simulation.World; // // دنیا
//////////////import trafficcontrol.LightState; // // وضعیت چراغ
//////////////import trafficcontrol.TrafficControlDevice; // // اینترفیس کنترل
//////////////import trafficcontrol.TrafficLight; // // چراغ راهنمایی
//////////////
//////////////import javax.imageio.ImageIO; // // لود تصویر
//////////////import javax.swing.JPanel; // // پنل
//////////////import java.awt.BasicStroke; // // قلم خطوط
//////////////import java.awt.Color; // // رنگ
//////////////import java.awt.Dimension; // // اندازه
//////////////import java.awt.Graphics; // // گرافیک
//////////////import java.awt.Graphics2D; // // گرافیک 2بعدی
//////////////import java.awt.RenderingHints; // // آنتی‌الیاس
//////////////import java.io.File; // // فایل
//////////////import java.io.InputStream; // // استریم
//////////////import java.util.List; // // لیست
//////////////import java.util.Random; // // رندوم
//////////////import java.awt.Image; // // تصویر
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل رندر
//////////////    private final World world; // // دنیا
//////////////    private final Camera camera; // // دوربین
//////////////
//////////////    private Image carImage; // // تصویر خودرو
//////////////    private Image pedestrianImage; // // تصویر عابر (اختیاری)
//////////////    private Image lightPostImage; // // تصویر پایه چراغ (اختیاری)
//////////////
//////////////    private final Random decoRnd = new Random(123); // // رندوم تزئینات
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) { // // سازنده
//////////////        this.world = world; // // ذخیره دنیا
//////////////        this.camera = camera; // // ذخیره دوربین
//////////////        setBackground(new Color(44, 120, 44)); // // چمن
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT)); // // اندازه
//////////////        setFocusable(true); // // فوکوس‌پذیر
//////////////    }
//////////////
//////////////    public void loadAssets(String carPathOrResource, String pedPathOrResource, String lightPathOrResource) { // // لود تصاویر
//////////////        this.carImage        = loadImageFlexible(carPathOrResource); // // ماشین
//////////////        this.pedestrianImage = loadImageFlexible(pedPathOrResource); // // عابر
//////////////        this.lightPostImage  = loadImageFlexible(lightPathOrResource); // // پایه چراغ
//////////////    }
//////////////
//////////////    private Image loadImageFlexible(String path) { // // لود کلاس‌پس یا فایل
//////////////        if (path == null) return null; // // اگر نداشتیم
//////////////        try {
//////////////            InputStream in = SimulatorPanel.class.getResourceAsStream(path.startsWith("/") ? path : ("/" + path)); // // از resources
//////////////            if (in != null) {
//////////////                try { return ImageIO.read(in); } finally { in.close(); } // // خواندن PNG
//////////////            }
//////////////            File f = new File(path); // // مسیر فایل محلی
//////////////            if (f.exists()) return ImageIO.read(f); // // اگر بود، بخوان
//////////////        } catch (Exception ex) {
//////////////            System.out.println("Asset load failed: " + path + " -> " + ex.getMessage()); // // لاگ خطا
//////////////        }
//////////////        return null; // // در غیر اینصورت null
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) { // // متد رسم
//////////////        super.paintComponent(g); // // پاک‌سازی پس‌زمینه
//////////////
//////////////        Graphics2D g2d = (Graphics2D) g; // // تبدیل به G2D
//////////////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // آنتی‌الیاس
//////////////        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // // اینترپولیشن
//////////////
//////////////        drawMapDecorations(g2d); // // ساختمان/درخت
//////////////        drawRoads(g2d); // // خیابان‌ها و خطوط
//////////////        drawTrafficLights(g2d); // // چراغ‌ها
//////////////        drawPedestrians(g2d); // // عابرها
//////////////        drawVehicles(g2d); // // خودروها
//////////////    }
//////////////
//////////////    private void drawMapDecorations(Graphics2D g2d) { // // تزئینات محیط
//////////////        CityMap map = world.getCityMap(); if (map == null) return; // // اگر نقشه نیست
//////////////        List<Intersection> inters = map.getIntersections(); if (inters.size() < 4) return; // // حداقل چند تقاطع
//////////////
//////////////        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE; // // محدوده x
//////////////        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE; // // محدوده y
//////////////        for (int i = 0; i < inters.size(); i++) { // // بدست آوردن محدوده
//////////////            Point p = inters.get(i).getPosition(); // // موقعیت
//////////////            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX()); // // بروزرسانی x
//////////////            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY()); // // بروزرسانی y
//////////////        }
//////////////
//////////////        decoRnd.setSeed(123); // // ثابت
//////////////        for (int i = 0; i < 30; i++) { // // ۳۰ شکل ساده
//////////////            int wx = minX + decoRnd.nextInt(Math.max(1, (maxX - minX))); // // x تصادفی
//////////////            int wy = minY + decoRnd.nextInt(Math.max(1, (maxY - minY))); // // y تصادفی
//////////////            Point sp = camera.transform(new Point(wx, wy)); // // تبدیل به صفحه
//////////////
//////////////            if (i % 3 == 0) { // // ساختمان
//////////////                int w = 25 + decoRnd.nextInt(35); int h = 25 + decoRnd.nextInt(35); // // ابعاد
//////////////                g2d.setColor(new Color(90, 90, 95)); g2d.fillRect(sp.getX(), sp.getY(), w, h); // // پرکردن
//////////////                g2d.setColor(new Color(60,60,60)); g2d.drawRect(sp.getX(), sp.getY(), w, h); // // خط دور
//////////////            } else { // // درخت
//////////////                int r = 6 + decoRnd.nextInt(10); // // شعاع
//////////////                g2d.setColor(new Color(28, 120, 28)); g2d.fillOval(sp.getX() - r, sp.getY() - r, r*2, r*2); // // تنه
//////////////                g2d.setColor(new Color(12, 80, 12)); g2d.drawOval(sp.getX() - r, sp.getY() - r, r*2, r*2); // // خط دور
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    private void drawRoads(Graphics2D g2d) { // // رسم خیابان‌ها
//////////////        CityMap map = world.getCityMap(); if (map == null) return; // // اگر نقشه نیست
//////////////        List<Road> roads = map.getRoads(); // // راه‌ها
//////////////
//////////////        for (int i = 0; i < roads.size(); i++) { // // روی هر راه
//////////////            Road r = roads.get(i); // // راه
//////////////            Point aW = r.getStartIntersection().getPosition(); // // نقطه شروع (دنیا)
//////////////            Point bW = r.getEndIntersection().getPosition(); // // نقطه انتها (دنیا)
//////////////            Point aS = camera.transform(aW); // // تبدیل به صفحه
//////////////            Point bS = camera.transform(bW); // // تبدیل به صفحه
//////////////
//////////////            double dx = bS.getX() - aS.getX(); double dy = bS.getY() - aS.getY(); // // بردار راه
//////////////            double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////            double nx = -dy / len, ny = dx / len; // // نرمال واحد
//////////////
//////////////            int lanesA = Math.max(1, r.getForwardLanes().size()); // // تعداد لِین جهت A
//////////////            int lanesB = Math.max(1, r.getBackwardLanes().size()); // // تعداد لِین جهت B
//////////////            int gapsA = lanesA - 1; int gapsB = lanesB - 1; // // فاصله‌های داخلی
//////////////
//////////////            float widthA = lanesA * UIConstants.LANE_WIDTH + gapsA * UIConstants.LANE_GAP; // // پهنای جهت A
//////////////            float widthB = lanesB * UIConstants.LANE_WIDTH + gapsB * UIConstants.LANE_GAP; // // پهنای جهت B
//////////////
//////////////            float halfSep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f; // // نصف فاصله وسط
//////////////            float offA = (widthA * 0.5f) + halfSep; // // آفست جهت A
//////////////            float offB = (widthB * 0.5f) + halfSep; // // آفست جهت B
//////////////
//////////////            int aAx = (int)Math.round(aS.getX() + nx * (-offA)); int aAy = (int)Math.round(aS.getY() + ny * (-offA)); // // لبه A1
//////////////            int bAx = (int)Math.round(bS.getX() + nx * (-offA)); int bAy = (int)Math.round(bS.getY() + ny * (-offA)); // // لبه A2
//////////////            int aBx = (int)Math.round(aS.getX() + nx * (+offB)); int aBy = (int)Math.round(aS.getY() + ny * (+offB)); // // لبه B1
//////////////            int bBx = (int)Math.round(bS.getX() + nx * (+offB)); int bBy = (int)Math.round(bS.getY() + ny * (+offB)); // // لبه B2
//////////////
//////////////            g2d.setStroke(new BasicStroke(widthA, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)); // // قلم پهن A
//////////////            g2d.setColor(UIConstants.ROAD_COLOR); g2d.drawLine(aAx, aAy, bAx, bAy); // // آسفالت جهت A
//////////////
//////////////            g2d.setStroke(new BasicStroke(widthB, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)); // // قلم پهن B
//////////////            g2d.setColor(UIConstants.ROAD_COLOR); g2d.drawLine(aBx, aBy, bBx, bBy); // // آسفالت جهت B
//////////////
//////////////            drawEdgeLines(g2d, aAx, aAy, bAx, bAy, widthA); // // خطوط سفید کناری A
//////////////            drawEdgeLines(g2d, aBx, aBy, bBx, bBy, widthB); // // خطوط سفید کناری B
//////////////
//////////////            if (lanesA >= 2) drawLaneDashes(g2d, aAx, aAy, bAx, bAy, widthA, lanesA); // // خط‌چین وسط لاین‌های A
//////////////            if (lanesB >= 2) drawLaneDashes(g2d, aBx, aBy, bBx, bBy, widthB, lanesB); // // خط‌چین وسط لاین‌های B
//////////////
//////////////            drawDoubleYellow(g2d, aS.getX(), aS.getY(), bS.getX(), bS.getY()); // // دو خط زرد بین دو جهت
//////////////        }
//////////////    }
//////////////
//////////////    private void drawEdgeLines(Graphics2D g2d, int ax, int ay, int bx, int by, float width) { // // خطوط کناری سفید
//////////////        float half = width * 0.5f; // // نیم‌پهنای راه
//////////////        g2d.setColor(UIConstants.EDGE_LINE_COLOR); // // رنگ سفید
//////////////        g2d.setStroke(new BasicStroke(2f)); // // ضخامت خط
//////////////        double dx = bx - ax, dy = by - ay; // // بردار
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////        double nx = -dy / len, ny = dx / len; // // نرمال
//////////////
//////////////        int axL = (int)Math.round(ax + nx * (-half)); int ayL = (int)Math.round(ay + ny * (-half)); // // لبه چپ
//////////////        int bxL = (int)Math.round(bx + nx * (-half)); int byL = (int)Math.round(by + ny * (-half)); // // لبه چپ انتها
//////////////        g2d.drawLine(axL, ayL, bxL, byL); // // رسم
//////////////
//////////////        int axR = (int)Math.round(ax + nx * (+half)); int ayR = (int)Math.round(ay + ny * (+half)); // // لبه راست
//////////////        int bxR = (int)Math.round(bx + nx * (+half)); int byR = (int)Math.round(by + ny * (+half)); // // لبه راست انتها
//////////////        g2d.drawLine(axR, ayR, bxR, byR); // // رسم
//////////////    }
//////////////
//////////////    private void drawLaneDashes(Graphics2D g2d, int ax, int ay, int bx, int by, float width, int laneCount) { // // خط‌چین داخلی
//////////////        g2d.setColor(UIConstants.LANE_DASH_COLOR); // // رنگ خط‌چین
//////////////        float dash[] = {14f, 10f}; // // الگوی خط‌چین
//////////////        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f)); // // قلم خط‌چین
//////////////
//////////////        double dx = bx - ax, dy = by - ay; // // بردار
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////        double nx = -dy / len, ny = dx / len; // // نرمال
//////////////
//////////////        float half = width * 0.5f; // // نیم‌پهنای راه
//////////////        int gaps = laneCount - 1; // // تعداد فاصله‌های داخلی
//////////////        for (int i = 1; i <= gaps; i++) { // // هر فاصله
//////////////            float fromLeft = (i * UIConstants.LANE_WIDTH) + (i - 0.5f) * UIConstants.LANE_GAP - half; // // آفست داخلی
//////////////            int axM = (int)Math.round(ax + nx * fromLeft); int ayM = (int)Math.round(ay + ny * fromLeft); // // نقطه شروع
//////////////            int bxM = (int)Math.round(bx + nx * fromLeft); int byM = (int)Math.round(by + ny * fromLeft); // // نقطه انتها
//////////////            g2d.drawLine(axM, ayM, bxM, byM); // // رسم خط‌چین
//////////////        }
//////////////    }
//////////////
//////////////    private void drawDoubleYellow(Graphics2D g2d, int ax, int ay, int bx, int by) { // // دو خط زرد وسط
//////////////        g2d.setColor(UIConstants.CENTER_LINE_COLOR); // // رنگ زرد
//////////////        g2d.setStroke(new BasicStroke(2.2f)); // // ضخامت
//////////////        double dx = bx - ax, dy = by - ay; // // بردار
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy)); // // طول
//////////////        double nx = -dy / len, ny = dx / len; // // نرمال
//////////////        float sep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f; // // نصف فاصله
//////////////
//////////////        int ax1 = (int)Math.round(ax + nx * (-sep)); int ay1 = (int)Math.round(ay + ny * (-sep)); // // خط اول
//////////////        int bx1 = (int)Math.round(bx + nx * (-sep)); int by1 = (int)Math.round(by + ny * (-sep)); // // انتهای خط اول
//////////////        g2d.drawLine(ax1, ay1, bx1, by1); // // رسم
//////////////
//////////////        int ax2 = (int)Math.round(ax + nx * (+sep)); int ay2 = (int)Math.round(ay + ny * (+sep)); // // خط دوم
//////////////        int bx2 = (int)Math.round(bx + nx * (+sep)); int by2 = (int)Math.round(by + ny * (+sep)); // // انتهای خط دوم
//////////////        g2d.drawLine(ax2, ay2, bx2, by2); // // رسم
//////////////    }
//////////////
//////////////    private void drawTrafficLights(Graphics2D g2d) { // // رسم چراغ‌ها
//////////////        CityMap map = world.getCityMap(); if (map == null) return; // // اگر نقشه نیست
//////////////        List<Intersection> inters = map.getIntersections(); if (inters == null || inters.isEmpty()) return; // // اگر تقاطع نیست
//////////////
//////////////        for (int i = 0; i < inters.size(); i++) { // // روی هر تقاطع
//////////////            Intersection it = inters.get(i); // // تقاطع
//////////////            Point wp = it.getPosition(); // // موقعیت دنیا
//////////////            Point sp = camera.transform(wp); // // صفحه
//////////////
//////////////            // برای هر جهت، اگر کنترل چراغ بود، همانجا یک دایره رنگی بکش
//////////////            drawLightIfPresent(g2d, it, Direction.NORTH, sp.getX(), sp.getY() - 30); // // بالا
//////////////            drawLightIfPresent(g2d, it, Direction.SOUTH, sp.getX(), sp.getY() + 18); // // پایین
//////////////            drawLightIfPresent(g2d, it, Direction.EAST,  sp.getX() + 18, sp.getY()); // // راست
//////////////            drawLightIfPresent(g2d, it, Direction.WEST,  sp.getX() - 30, sp.getY()); // // چپ
//////////////        }
//////////////    }
//////////////
//////////////    private void drawLightIfPresent(Graphics2D g2d, Intersection it, Direction dir, int x, int y) { // // اگر چراغ وجود داشت، رسم کن
//////////////        try {
//////////////            TrafficControlDevice dev = it.getControl(dir); // // گرفتن کنترل جهت (نام‌گذاری مطابق setControl)
//////////////            if (dev instanceof TrafficLight) { // // اگر چراغ بود
//////////////                TrafficLight tl = (TrafficLight) dev; // // تبدیل
//////////////                Color c = tl.getState() == LightState.RED ? Color.RED :
//////////////                        (tl.getState() == LightState.YELLOW ? Color.YELLOW : Color.GREEN); // // رنگ چراغ
//////////////                g2d.setColor(c); g2d.fillOval(x, y, 12, 12); // // کشیدن چراغ
//////////////                g2d.setColor(Color.BLACK); g2d.drawOval(x, y, 12, 12); // // خط دور
//////////////            }
//////////////        } catch (Throwable ex) {
//////////////            // اگر getControl وجود نداشت، چیزی نکش (جهت سازگاری با نسخهٔ فعلی کلاس Intersection)
//////////////        }
//////////////    }
//////////////
//////////////    private void drawPedestrians(Graphics2D g2d) { // // رسم عابرها
//////////////        List<Pedestrian> ps = world.getPedestrians(); if (ps == null || ps.isEmpty()) return; // // اگر عابری نیست
//////////////        for (int i = 0; i < ps.size(); i++) {
//////////////            Pedestrian p = ps.get(i); // // عابر
//////////////            Point wp = p.getPosition(); // // موقعیت دنیا
//////////////            Point sp = camera.transform(wp); // // موقعیت صفحه
//////////////            if (pedestrianImage != null) { // // اگر تصویر داشتیم
//////////////                g2d.drawImage(pedestrianImage, sp.getX() - 6, sp.getY() - 6, 12, 12, null); // // کشیدن تصویر
//////////////            } else {
//////////////                g2d.setColor(UIConstants.PEDESTRIAN_COLOR); g2d.fillOval(sp.getX() - 4, sp.getY() - 4, 8, 8); // // دایره ساده
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    private void drawVehicles(Graphics2D g2d) { // // رسم خودروها
//////////////        List<Vehicle> vs = world.getVehicles(); if (vs == null || vs.isEmpty()) return; // // اگر خودرویی نیست
//////////////        for (int i = 0; i < vs.size(); i++) {
//////////////            Vehicle v = vs.get(i); // // خودرو
//////////////            Lane lane = v.getCurrentLane(); if (lane == null) continue; // // اگر لِین ندارد، رد شو
//////////////
//////////////            Point wp = lane.getPositionAt(v.getPositionInLane()); // // موقعیت دنیا روی لِین
//////////////            Point sp = camera.transform(wp); // // تبدیل به صفحه
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH; int h = UIConstants.VEHICLE_HEIGHT; // // ابعاد خودرو
//////////////            int cx = sp.getX(); int cy = sp.getY(); // // مرکز رسم
//////////////
//////////////            Graphics2D g2 = (Graphics2D) g2d.create(); // // کپی گرافیک
//////////////            g2.rotate(v.getAngle(), cx, cy); // // چرخش بر اساس زاویه حرکت
//////////////
//////////////            if (carImage != null) { // // اگر تصویر داریم
//////////////                g2.drawImage(carImage, cx - w/2, cy - h/2, w, h, null); // // کشیدن PNG
//////////////            } else { // // در غیر این صورت مستطیل ساده
//////////////                g2.setColor(UIConstants.CAR_COLOR); g2.fillRect(cx - w/2, cy - h/2, w, h); // // کشیدن خودرو ساده
//////////////            }
//////////////
//////////////            if (v.isBrakeLightOn()) { // // چراغ ترمز
//////////////                g2.setColor(Color.RED); // // قرمز
//////////////                g2.fillRect(cx + w/2 - 5, cy - 3, 4, 4); // // چراغ راست
//////////////                g2.fillRect(cx + w/2 - 5, cy + 1, 4, 4); // // چراغ چپ
//////////////            }
//////////////            if (v.isTurningLeft()) { // // راهنمای چپ
//////////////                g2.setColor(Color.ORANGE); g2.fillRect(cx - w/2, cy - 2, 4, 4); // // مستطیل کوچک
//////////////            }
//////////////            if (v.isTurningRight()) { // // راهنمای راست
//////////////                g2.setColor(Color.ORANGE); g2.fillRect(cx + w/2 - 4, cy - 2, 4, 4); // // مستطیل کوچک
//////////////            }
//////////////
//////////////            g2.dispose(); // // آزادسازی گرافیک
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // پنل رندر
//////////////
//////////////import core.Point;
//////////////import core.Vehicle;
//////////////import infrastructure.*;
//////////////import pedestrian.Pedestrian;
//////////////import simulation.World;
//////////////import trafficcontrol.*;
//////////////
//////////////import javax.imageio.ImageIO;
//////////////import javax.swing.JPanel;
//////////////import java.awt.*;
//////////////import java.awt.geom.AffineTransform;
//////////////import java.io.File;
//////////////import java.io.InputStream;
//////////////import java.util.List;
//////////////import java.util.Random;
//////////////
//////////////public class SimulatorPanel extends JPanel {
//////////////    private final World world;     // مدل
//////////////    private final Camera camera;   // تبدیل جهان→صفحه
//////////////
//////////////    // تصاویر (اختیاری)
//////////////    private Image carImage;
//////////////    private Image pedestrianImage;
//////////////    private Image lightPostImage;
//////////////
//////////////    // تزئینات محیط (ساختمان/درخت) با seed ثابت برای تکرارپذیری
//////////////    private final Random decoRnd = new Random(123);
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) {
//////////////        this.world = world;
//////////////        this.camera = camera;
//////////////        setBackground(new Color(44, 120, 44)); // چمن
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT));
//////////////        setFocusable(true);
//////////////    }
//////////////
//////////////    // لود تصویر از classpath یا فایل
//////////////    public void loadAssets(String carPathOrResource, String pedPathOrResource, String lightPathOrResource) {
//////////////        this.carImage        = loadImageFlexible(carPathOrResource);
//////////////        this.pedestrianImage = loadImageFlexible(pedPathOrResource);
//////////////        this.lightPostImage  = loadImageFlexible(lightPathOrResource);
//////////////    }
//////////////    private Image loadImageFlexible(String path) {
//////////////        if (path == null) return null;
//////////////        try {
//////////////            InputStream in = SimulatorPanel.class.getResourceAsStream(path.startsWith("/") ? path : ("/" + path));
//////////////            if (in != null) {
//////////////                try { return ImageIO.read(in); } finally { in.close(); }
//////////////            }
//////////////            File f = new File(path);
//////////////            if (f.exists()) return ImageIO.read(f);
//////////////        } catch (Exception ex) {
//////////////            System.out.println("Asset load failed: " + path + " -> " + ex.getMessage());
//////////////        }
//////////////        return null;
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) {
//////////////        super.paintComponent(g);
//////////////
//////////////        Graphics2D g2d = (Graphics2D) g;
//////////////        // آنتی‌الیاس و اینترپولیشن بهتر
//////////////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//////////////        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//////////////
//////////////        drawMapDecorations(g2d);   // ساختمان و درخت
//////////////        drawRoads(g2d);            // خیابان‌ها (دو نوار جدا + خطوط)
//////////////        drawTrafficLights(g2d);    // چراغ‌ها
//////////////        drawPedestrians(g2d);      // عابر
//////////////        drawVehicles(g2d);         // خودروها (PNG با چرخش + چراغ‌ها)
//////////////    }
//////////////
//////////////    // تزئینات سبک محیط
//////////////    private void drawMapDecorations(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap(); if (map == null) return;
//////////////        List<Intersection> inters = map.getIntersections(); if (inters.size() < 4) return;
//////////////
//////////////        // محدوده تقریبی نقشه
//////////////        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
//////////////        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
//////////////        for (Intersection it : inters) {
//////////////            Point p = it.getPosition();
//////////////            minX = Math.min(minX, p.getX());
//////////////            maxX = Math.max(maxX, p.getX());
//////////////            minY = Math.min(minY, p.getY());
//////////////            maxY = Math.max(maxY, p.getY());
//////////////        }
//////////////
//////////////        decoRnd.setSeed(123);
//////////////        for (int i = 0; i < 30; i++) {
//////////////            int wx = minX + decoRnd.nextInt(Math.max(1, (maxX - minX)));
//////////////            int wy = minY + decoRnd.nextInt(Math.max(1, (maxY - minY)));
//////////////            Point sp = camera.transform(new Point(wx, wy));
//////////////
//////////////            if (i % 3 == 0) {
//////////////                // ساختمان
//////////////                int w = 25 + decoRnd.nextInt(35);
//////////////                int h = 25 + decoRnd.nextInt(35);
//////////////                g2d.setColor(new Color(90, 90, 95));
//////////////                g2d.fillRect(sp.getX(), sp.getY(), w, h);
//////////////                g2d.setColor(new Color(60,60,60));
//////////////                g2d.drawRect(sp.getX(), sp.getY(), w, h);
//////////////            } else {
//////////////                // درخت
//////////////                int r = 6 + decoRnd.nextInt(10);
//////////////                g2d.setColor(new Color(28, 120, 28));
//////////////                g2d.fillOval(sp.getX() - r, sp.getY() - r, r*2, r*2);
//////////////                g2d.setColor(new Color(12, 80, 12));
//////////////                g2d.drawOval(sp.getX() - r, sp.getY() - r, r*2, r*2);
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // خیابان‌ها: هر جهت یک نوار مستقل + خطوط کناری و داخلی + دو خط زرد وسط
//////////////    private void drawRoads(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap(); if (map == null) return;
//////////////        List<Road> roads = map.getRoads();
//////////////
//////////////        for (int i = 0; i < roads.size(); i++) {
//////////////            Road r = roads.get(i);
//////////////            Point aW = r.getStartIntersection().getPosition();
//////////////            Point bW = r.getEndIntersection().getPosition();
//////////////            Point aS = camera.transform(aW);
//////////////            Point bS = camera.transform(bW);
//////////////
//////////////            double dx = bS.getX() - aS.getX();
//////////////            double dy = bS.getY() - aS.getY();
//////////////            double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////            double nx = -dy / len, ny = dx / len; // نرمال واحد
//////////////
//////////////            int lanesA = Math.max(1, r.getForwardLanes().size());
//////////////            int lanesB = Math.max(1, r.getBackwardLanes().size());
//////////////            int gapsA = lanesA - 1;
//////////////            int gapsB = lanesB - 1;
//////////////
//////////////            float widthA = lanesA * UIConstants.LANE_WIDTH + gapsA * UIConstants.LANE_GAP;
//////////////            float widthB = lanesB * UIConstants.LANE_WIDTH + gapsB * UIConstants.LANE_GAP;
//////////////
//////////////            float halfSep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f;
//////////////            float offA = (widthA * 0.5f) + halfSep;
//////////////            float offB = (widthB * 0.5f) + halfSep;
//////////////
//////////////            int aAx = (int)Math.round(aS.getX() + nx * (-offA)); int aAy = (int)Math.round(aS.getY() + ny * (-offA));
//////////////            int bAx = (int)Math.round(bS.getX() + nx * (-offA)); int bAy = (int)Math.round(bS.getY() + ny * (-offA));
//////////////            int aBx = (int)Math.round(aS.getX() + nx * (+offB)); int aBy = (int)Math.round(aS.getY() + ny * (+offB));
//////////////            int bBx = (int)Math.round(bS.getX() + nx * (+offB)); int bBy = (int)Math.round(bS.getY() + ny * (+offB));
//////////////
//////////////            // بدنه آسفالت جهت A
//////////////            g2d.setStroke(new BasicStroke(widthA, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////            g2d.drawLine(aAx, aAy, bAx, bAy);
//////////////
//////////////            // بدنه آسفالت جهت B
//////////////            g2d.setStroke(new BasicStroke(widthB, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////            g2d.drawLine(aBx, aBy, bBx, bBy);
//////////////
//////////////            // خطوط کناری سفید
//////////////            drawEdgeLines(g2d, aAx, aAy, bAx, bAy, widthA);
//////////////            drawEdgeLines(g2d, aBx, aBy, bBx, bBy, widthB);
//////////////
//////////////            // خط‌چین داخلی بین لاین‌ها
//////////////            if (lanesA >= 2) drawLaneDashes(g2d, aAx, aAy, bAx, bAy, widthA, lanesA);
//////////////            if (lanesB >= 2) drawLaneDashes(g2d, aBx, aBy, bBx, bBy, widthB, lanesB);
//////////////
//////////////            // دو خط زرد وسط برای جداسازی دو جهت
//////////////            drawDoubleYellow(g2d, aS.getX(), aS.getY(), bS.getX(), bS.getY());
//////////////        }
//////////////    }
//////////////
//////////////    private void drawEdgeLines(Graphics2D g2d, int ax, int ay, int bx, int by, float width) {
//////////////        float half = width * 0.5f;
//////////////        g2d.setColor(UIConstants.EDGE_LINE_COLOR);
//////////////        g2d.setStroke(new BasicStroke(2f));
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        int axL = (int)Math.round(ax + nx * (-half)); int ayL = (int)Math.round(ay + ny * (-half));
//////////////        int bxL = (int)Math.round(bx + nx * (-half)); int byL = (int)Math.round(by + ny * (-half));
//////////////        g2d.drawLine(axL, ayL, bxL, byL);
//////////////
//////////////        int axR = (int)Math.round(ax + nx * (+half)); int ayR = (int)Math.round(ay + ny * (+half));
//////////////        int bxR = (int)Math.round(bx + nx * (+half)); int byR = (int)Math.round(by + ny * (+half));
//////////////        g2d.drawLine(axR, ayR, bxR, byR);
//////////////    }
//////////////
//////////////    private void drawLaneDashes(Graphics2D g2d, int ax, int ay, int bx, int by, float width, int laneCount) {
//////////////        g2d.setColor(UIConstants.LANE_DASH_COLOR);
//////////////        float dash[] = {14f, 10f};
//////////////        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f));
//////////////
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        float half = width * 0.5f;
//////////////        int gaps = laneCount - 1;
//////////////        for (int i = 1; i <= gaps; i++) {
//////////////            float fromLeft = (i * UIConstants.LANE_WIDTH) + (i - 0.5f) * UIConstants.LANE_GAP - half;
//////////////            int axM = (int)Math.round(ax + nx * fromLeft);
//////////////            int ayM = (int)Math.round(ay + ny * fromLeft);
//////////////            int bxM = (int)Math.round(bx + nx * fromLeft);
//////////////            int byM = (int)Math.round(by + ny * fromLeft);
//////////////            g2d.drawLine(axM, ayM, bxM, byM);
//////////////        }
//////////////    }
//////////////
//////////////    private void drawDoubleYellow(Graphics2D g2d, int ax, int ay, int bx, int by) {
//////////////        g2d.setColor(UIConstants.CENTER_LINE_COLOR);
//////////////        g2d.setStroke(new BasicStroke(2.2f));
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////        float sep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f;
//////////////
//////////////        int ax1 = (int)Math.round(ax + nx * (-sep)); int ay1 = (int)Math.round(ay + ny * (-sep));
//////////////        int bx1 = (int)Math.round(bx + nx * (-sep)); int by1 = (int)Math.round(by + ny * (-sep));
//////////////        g2d.drawLine(ax1, ay1, bx1, by1);
//////////////
//////////////        int ax2 = (int)Math.round(ax + nx * (+sep)); int ay2 = (int)Math.round(ay + ny * (+sep));
//////////////        int bx2 = (int)Math.round(bx + nx * (+sep)); int by2 = (int)Math.round(by + ny * (+sep));
//////////////        g2d.drawLine(ax2, ay2, bx2, by2);
//////////////    }
//////////////
//////////////    // چراغ‌ها
//////////////    private void drawTrafficLights(Graphics2D g2d) {
//////////////        List<TrafficLight> lights = world.getTrafficLights();
//////////////        if (lights == null || lights.isEmpty()) return;
//////////////
//////////////        for (int i = 0; i < lights.size(); i++) {
//////////////            TrafficLight tl = lights.get(i);
//////////////            Intersection host = findHostIntersection(tl);
//////////////            int x, y;
//////////////            if (host != null) {
//////////////                Point wp = host.getPosition();
//////////////                Point sp = camera.transform(wp);
//////////////                x = sp.getX() - 6;
//////////////                y = sp.getY() - 30;
//////////////            } else {
//////////////                x = 30; y = 30;
//////////////            }
//////////////            if (lightPostImage != null) {
//////////////                g2d.drawImage(lightPostImage, x - 6, y - 16, 16, 32, null);
//////////////            }
//////////////            Color c = tl.getState() == LightState.RED ? Color.RED
//////////////                    : (tl.getState() == LightState.YELLOW ? Color.YELLOW : Color.GREEN);
//////////////            g2d.setColor(c);
//////////////            g2d.fillOval(x, y, 12, 12);
//////////////            g2d.setColor(Color.BLACK);
//////////////            g2d.drawOval(x, y, 12, 12);
//////////////        }
//////////////    }
//////////////
//////////////    private Intersection findHostIntersection(TrafficLight tl) {
//////////////        CityMap map = world.getCityMap(); if (map == null) return null;
//////////////        List<Intersection> list = map.getIntersections();
//////////////        for (int i = 0; i < list.size(); i++) {
//////////////            Intersection it = list.get(i);
//////////////            if (it.hasControl(tl)) return it; // فرض: چنین متدی در Intersection وجود دارد
//////////////        }
//////////////        return null;
//////////////    }
//////////////
//////////////    // عابرها
//////////////    private void drawPedestrians(Graphics2D g2d) {
//////////////        List<Pedestrian> ps = world.getPedestrians(); if (ps == null || ps.isEmpty()) return;
//////////////        for (int i = 0; i < ps.size(); i++) {
//////////////            Pedestrian p = ps.get(i);
//////////////            Point wp = p.getPosition();
//////////////            Point sp = camera.transform(wp);
//////////////            if (pedestrianImage != null) {
//////////////                g2d.drawImage(pedestrianImage, sp.getX() - 6, sp.getY() - 6, 12, 12, null);
//////////////            } else {
//////////////                g2d.setColor(UIConstants.PEDESTRIAN_COLOR);
//////////////                g2d.fillOval(sp.getX() - 4, sp.getY() - 4, 8, 8);
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // خودروها
//////////////    private void drawVehicles(Graphics2D g2d) {
//////////////        List<Vehicle> vs = world.getVehicles(); if (vs == null || vs.isEmpty()) return;
//////////////
//////////////        for (int i = 0; i < vs.size(); i++) {
//////////////            Vehicle v = vs.get(i);
//////////////            Lane lane = v.getCurrentLane();
//////////////            if (lane == null) continue;
//////////////
//////////////            Point wp = lane.getPositionAt(v.getPositionInLane());
//////////////            Point sp = camera.transform(wp);
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH;
//////////////            int h = UIConstants.VEHICLE_HEIGHT;
//////////////            int cx = sp.getX();
//////////////            int cy = sp.getY();
//////////////
//////////////            Graphics2D g2 = (Graphics2D) g2d.create();
//////////////            g2.rotate(v.getAngle(), cx, cy);
//////////////
//////////////            if (carImage != null) {
//////////////                g2.drawImage(carImage, cx - w/2, cy - h/2, w, h, null);
//////////////            } else {
//////////////                g2.setColor(UIConstants.CAR_COLOR);
//////////////                g2.fillRect(cx - w/2, cy - h/2, w, h);
//////////////            }
//////////////
//////////////            // چراغ ترمز
//////////////            if (v.isBrakeLightOn()) {
//////////////                g2.setColor(Color.RED);
//////////////                g2.fillRect(cx + w/2 - 5, cy - 3, 4, 4);
//////////////                g2.fillRect(cx + w/2 - 5, cy + 1, 4, 4);
//////////////            }
//////////////            // راهنما
//////////////            if (v.isTurningLeft()) {
//////////////                g2.setColor(Color.ORANGE);
//////////////                g2.fillRect(cx - w/2, cy - 2, 4, 4);
//////////////            }
//////////////            if (v.isTurningRight()) {
//////////////                g2.setColor(Color.ORANGE);
//////////////                g2.fillRect(cx + w/2 - 4, cy - 2, 4, 4);
//////////////            }
//////////////
//////////////            g2.dispose();
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui;
//////////////
//////////////import core.Point;
//////////////import core.Vehicle;
//////////////import infrastructure.*;
//////////////import pedestrian.Pedestrian;
//////////////import simulation.World;
//////////////import trafficcontrol.*;
//////////////
//////////////import javax.imageio.ImageIO;
//////////////import javax.swing.JPanel;
//////////////import java.awt.*;
//////////////import java.awt.event.*;
//////////////import java.awt.geom.AffineTransform;
//////////////import java.io.File;
//////////////import java.io.InputStream;
//////////////import java.util.List;
//////////////import java.util.Random;
//////////////
//////////////public class SimulatorPanel extends JPanel {
//////////////    private final World world;     // مدل
//////////////    private final Camera camera;   // تبدیل جهان→صفحه
//////////////
//////////////    // تصاویر
//////////////    private Image carImage;
//////////////    private Image pedestrianImage;
//////////////    private Image lightPostImage;
//////////////
//////////////    // تزئینات محیط (ساختمان/درخت) – تولید تنبل/ثابت با seed
//////////////    private final Random decoRnd = new Random(123);
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) {
//////////////        this.world = world;
//////////////        this.camera = camera;
//////////////        setBackground(new Color(44, 120, 44)); // چمن
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT));
//////////////        setFocusable(true);
//////////////    }
//////////////
//////////////    // لود از classpath + fallback فایل
//////////////    public void loadAssets(String carPathOrResource, String pedPathOrResource, String lightPathOrResource) {
//////////////        carImage       = loadImageFlexible(carPathOrResource);
//////////////        pedestrianImage= loadImageFlexible(pedPathOrResource);
//////////////        lightPostImage = loadImageFlexible(lightPathOrResource);
//////////////    }
//////////////    private Image loadImageFlexible(String path) {
//////////////        if (path == null) return null;
//////////////        try {
//////////////            InputStream in = SimulatorPanel.class.getResourceAsStream(path.startsWith("/")? path : ("/"+path));
//////////////            if (in != null) {
//////////////                try { return ImageIO.read(in); } finally { in.close(); }
//////////////            }
//////////////            File f = new File(path);
//////////////            if (f.exists()) return ImageIO.read(f);
//////////////        } catch (Exception ex) {
//////////////            System.out.println("Asset load failed: " + path + " -> " + ex.getMessage());
//////////////        }
//////////////        return null;
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) {
//////////////        super.paintComponent(g);
//////////////
//////////////        // پس‌زمینهٔ سبز (چمن) و کمربندهای خاکی/سبز بین خیابان‌ها را پایین‌تر می‌کشیم
//////////////        Graphics2D g2d = (Graphics2D) g;
//////////////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//////////////        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//////////////
//////////////        drawMapDecorations(g2d);   // ساختمان‌ها و درخت‌ها در فضای بین خیابان‌ها
//////////////        drawRoads(g2d);            // خیابان‌ها (دو جهت جدا + خطوط کناری/میانی/داخلی)
//////////////        drawTrafficLights(g2d);    // چراغ‌های راهنمایی
//////////////        drawPedestrians(g2d);      // عابرها
//////////////        drawVehicles(g2d);         // خودروها (PNG با چرخش + چراغ ترمز/راهنما)
//////////////    }
//////////////
//////////////    // ———————— تزئینات محیط: ساختمان و درخت در فضای بین شبکه ————————
//////////////    private void drawMapDecorations(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap(); if (map == null) return;
//////////////        List<Intersection> inters = map.getIntersections(); if (inters.size() < 4) return;
//////////////
//////////////        // از ساختار گریدی تقریبی استفاده می‌کنیم: بلوک‌ها را بین چهار تقاطع فرض می‌کنیم
//////////////        // ساده: چند مستطیل خاکستری (ساختمان) + چند دایره سبز (درخت)
//////////////        g2d.setStroke(new BasicStroke(1f));
//////////////
//////////////        // محدوده تقریبی نقشه پیدا کنیم
//////////////        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
//////////////        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
//////////////        for (Intersection it : inters) {
//////////////            Point p = it.getPosition();
//////////////            minX = Math.min(minX, p.getX());
//////////////            maxX = Math.max(maxX, p.getX());
//////////////            minY = Math.min(minY, p.getY());
//////////////            maxY = Math.max(maxY, p.getY());
//////////////        }
//////////////        // چند لکه‌ٔ سبز و خاکستری داخل محدوده
//////////////        decoRnd.setSeed(123);
//////////////        for (int i = 0; i < 30; i++) {
//////////////            int wx = minX + decoRnd.nextInt(Math.max(1, (maxX - minX)));
//////////////            int wy = minY + decoRnd.nextInt(Math.max(1, (maxY - minY)));
//////////////            Point sp = camera.transform(new Point(wx, wy));
//////////////
//////////////            if (i % 3 == 0) {
//////////////                // ساختمان
//////////////                int w = 25 + decoRnd.nextInt(35);
//////////////                int h = 25 + decoRnd.nextInt(35);
//////////////                g2d.setColor(new Color(90, 90, 95));
//////////////                g2d.fillRect(sp.getX(), sp.getY(), w, h);
//////////////                g2d.setColor(new Color(60,60,60));
//////////////                g2d.drawRect(sp.getX(), sp.getY(), w, h);
//////////////            } else {
//////////////                // درخت
//////////////                int r = 6 + decoRnd.nextInt(10);
//////////////                g2d.setColor(new Color(28, 120, 28));
//////////////                g2d.fillOval(sp.getX() - r, sp.getY() - r, r*2, r*2);
//////////////                g2d.setColor(new Color(12, 80, 12));
//////////////                g2d.drawOval(sp.getX() - r, sp.getY() - r, r*2, r*2);
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // ———————— خیابان‌ها: دو جهت جدا + خطوط ————————
//////////////    private void drawRoads(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap(); if (map == null) return;
//////////////        List<Road> roads = map.getRoads();
//////////////
//////////////        for (int i = 0; i < roads.size(); i++) {
//////////////            Road r = roads.get(i);
//////////////            Point aW = r.getStartIntersection().getPosition();
//////////////            Point bW = r.getEndIntersection().getPosition();
//////////////            Point aS = camera.transform(aW);
//////////////            Point bS = camera.transform(bW);
//////////////
//////////////            double dx = bS.getX() - aS.getX();
//////////////            double dy = bS.getY() - aS.getY();
//////////////            double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////            double nx = -dy / len, ny = dx / len; // نرمال واحد
//////////////
//////////////            int lanesA = Math.max(1, r.getForwardLanes().size());
//////////////            int lanesB = Math.max(1, r.getBackwardLanes().size());
//////////////            int gapsA = lanesA - 1;
//////////////            int gapsB = lanesB - 1;
//////////////
//////////////            float widthA = lanesA * UIConstants.LANE_WIDTH + gapsA * UIConstants.LANE_GAP;
//////////////            float widthB = lanesB * UIConstants.LANE_WIDTH + gapsB * UIConstants.LANE_GAP;
//////////////
//////////////            float halfSep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f;
//////////////            float offA = (widthA * 0.5f) + halfSep;
//////////////            float offB = (widthB * 0.5f) + halfSep;
//////////////
//////////////            int aAx = (int)Math.round(aS.getX() + nx * (-offA)); int aAy = (int)Math.round(aS.getY() + ny * (-offA));
//////////////            int bAx = (int)Math.round(bS.getX() + nx * (-offA)); int bAy = (int)Math.round(bS.getY() + ny * (-offA));
//////////////            int aBx = (int)Math.round(aS.getX() + nx * (+offB)); int aBy = (int)Math.round(aS.getY() + ny * (+offB));
//////////////            int bBx = (int)Math.round(bS.getX() + nx * (+offB)); int bBy = (int)Math.round(bS.getY() + ny * (+offB));
//////////////
//////////////            // آسفالت جهت A
//////////////            g2d.setStroke(new BasicStroke(widthA, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////            g2d.drawLine(aAx, aAy, bAx, bAy);
//////////////
//////////////            // آسفالت جهت B
//////////////            g2d.setStroke(new BasicStroke(widthB, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////            g2d.drawLine(aBx, aBy, bBx, bBy);
//////////////
//////////////            // خطوط کناری سفید
//////////////            drawEdgeLines(g2d, aAx, aAy, bAx, bAy, widthA);
//////////////            drawEdgeLines(g2d, aBx, aBy, bBx, bBy, widthB);
//////////////
//////////////            // خط‌چین داخلی بین لاین‌ها
//////////////            if (lanesA >= 2) drawLaneDashes(g2d, aAx, aAy, bAx, bAy, widthA, lanesA);
//////////////            if (lanesB >= 2) drawLaneDashes(g2d, aBx, aBy, bBx, bBy, widthB, lanesB);
//////////////
//////////////            // دو خط زرد وسط
//////////////            drawDoubleYellow(g2d, aS.getX(), aS.getY(), bS.getX(), bS.getY());
//////////////        }
//////////////    }
//////////////
//////////////    private void drawEdgeLines(Graphics2D g2d, int ax, int ay, int bx, int by, float width) {
//////////////        float half = width * 0.5f;
//////////////        g2d.setColor(UIConstants.EDGE_LINE_COLOR);
//////////////        g2d.setStroke(new BasicStroke(2f));
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        int axL = (int)Math.round(ax + nx * (-half)); int ayL = (int)Math.round(ay + ny * (-half));
//////////////        int bxL = (int)Math.round(bx + nx * (-half)); int byL = (int)Math.round(by + ny * (-half));
//////////////        g2d.drawLine(axL, ayL, bxL, byL);
//////////////
//////////////        int axR = (int)Math.round(ax + nx * (+half)); int ayR = (int)Math.round(ay + ny * (+half));
//////////////        int bxR = (int)Math.round(bx + nx * (+half)); int byR = (int)Math.round(by + ny * (+half));
//////////////        g2d.drawLine(axR, ayR, bxR, byR);
//////////////    }
//////////////
//////////////    private void drawLaneDashes(Graphics2D g2d, int ax, int ay, int bx, int by, float width, int laneCount) {
//////////////        g2d.setColor(UIConstants.LANE_DASH_COLOR);
//////////////        float dash[] = {14f, 10f};
//////////////        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f));
//////////////
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        float half = width * 0.5f;
//////////////        int gaps = laneCount - 1;
//////////////        for (int i = 1; i <= gaps; i++) {
//////////////            float fromLeft = (i * UIConstants.LANE_WIDTH) + (i - 0.5f) * UIConstants.LANE_GAP - half;
//////////////            int axM = (int)Math.round(ax + nx * fromLeft);
//////////////            int ayM = (int)Math.round(ay + ny * fromLeft);
//////////////            int bxM = (int)Math.round(bx + nx * fromLeft);
//////////////            int byM = (int)Math.round(by + ny * fromLeft);
//////////////            g2d.drawLine(axM, ayM, bxM, byM);
//////////////        }
//////////////    }
//////////////
//////////////    private void drawDoubleYellow(Graphics2D g2d, int ax, int ay, int bx, int by) {
//////////////        g2d.setColor(UIConstants.CENTER_LINE_COLOR);
//////////////        g2d.setStroke(new BasicStroke(2.2f));
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////        float sep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f;
//////////////
//////////////        int ax1 = (int)Math.round(ax + nx * (-sep)); int ay1 = (int)Math.round(ay + ny * (-sep));
//////////////        int bx1 = (int)Math.round(bx + nx * (-sep)); int by1 = (int)Math.round(by + ny * (-sep));
//////////////        g2d.drawLine(ax1, ay1, bx1, by1);
//////////////
//////////////        int ax2 = (int)Math.round(ax + nx * (+sep)); int ay2 = (int)Math.round(ay + ny * (+sep));
//////////////        int bx2 = (int)Math.round(bx + nx * (+sep)); int by2 = (int)Math.round(by + ny * (+sep));
//////////////        g2d.drawLine(ax2, ay2, bx2, by2);
//////////////    }
//////////////
//////////////    // ———————— چراغ‌های راهنمایی ————————
//////////////    private void drawTrafficLights(Graphics2D g2d) {
//////////////        List<TrafficLight> lights = world.getTrafficLights();
//////////////        if (lights == null || lights.isEmpty()) return;
//////////////
//////////////        for (int i = 0; i < lights.size(); i++) {
//////////////            TrafficLight tl = lights.get(i);
//////////////            Intersection host = findHostIntersection(tl);
//////////////            int x, y;
//////////////            if (host != null) {
//////////////                Point wp = host.getPosition();
//////////////                Point sp = camera.transform(wp);
//////////////                x = sp.getX() - 6;
//////////////                y = sp.getY() - 30;
//////////////            } else {
//////////////                x = 30; y = 30;
//////////////            }
//////////////            if (lightPostImage != null) {
//////////////                g2d.drawImage(lightPostImage, x - 6, y - 16, 16, 32, null);
//////////////            }
//////////////            Color c = tl.getState() == LightState.RED ? Color.RED
//////////////                    : (tl.getState() == LightState.YELLOW ? Color.YELLOW : Color.GREEN);
//////////////            g2d.setColor(c);
//////////////            g2d.fillOval(x, y, 12, 12);
//////////////            g2d.setColor(Color.BLACK);
//////////////            g2d.drawOval(x, y, 12, 12);
//////////////        }
//////////////    }
//////////////
//////////////    private Intersection findHostIntersection(TrafficLight tl) {
//////////////        CityMap map = world.getCityMap(); if (map == null) return null;
//////////////        List<Intersection> list = map.getIntersections();
//////////////        for (int i = 0; i < list.size(); i++) {
//////////////            Intersection it = list.get(i);
//////////////            if (it.hasControl(tl)) return it; // فرض: چنین متدی در Intersection داری
//////////////        }
//////////////        return null;
//////////////    }
//////////////
//////////////    // ———————— عابر ————————
//////////////    private void drawPedestrians(Graphics2D g2d) {
//////////////        List<Pedestrian> ps = world.getPedestrians();
//////////////        if (ps == null || ps.isEmpty()) return;
//////////////
//////////////        for (int i = 0; i < ps.size(); i++) {
//////////////            Pedestrian p = ps.get(i);
//////////////            Point wp = p.getPosition();
//////////////            Point sp = camera.transform(wp);
//////////////            if (pedestrianImage != null) {
//////////////                g2d.drawImage(pedestrianImage, sp.getX() - 6, sp.getY() - 6, 12, 12, null);
//////////////            } else {
//////////////                g2d.setColor(UIConstants.PEDESTRIAN_COLOR);
//////////////                g2d.fillOval(sp.getX() - 4, sp.getY() - 4, 8, 8);
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // ———————— خودرو ————————
//////////////    private void drawVehicles(Graphics2D g2d) {
//////////////        List<Vehicle> vs = world.getVehicles(); if (vs == null || vs.isEmpty()) return;
//////////////
//////////////        for (int i = 0; i < vs.size(); i++) {
//////////////            Vehicle v = vs.get(i);
//////////////            Lane lane = v.getCurrentLane();
//////////////            if (lane == null) continue;
//////////////
//////////////            Point wp = lane.getPositionAt(v.getPositionInLane());
//////////////            Point sp = camera.transform(wp);
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH;
//////////////            int h = UIConstants.VEHICLE_HEIGHT;
//////////////            int cx = sp.getX();
//////////////            int cy = sp.getY();
//////////////
//////////////            Graphics2D g2 = (Graphics2D) g2d.create();
//////////////            g2.rotate(v.getAngle(), cx, cy);
//////////////
//////////////            if (carImage != null) {
//////////////                g2.drawImage(carImage, cx - w/2, cy - h/2, w, h, null);
//////////////            } else {
//////////////                g2.setColor(UIConstants.CAR_COLOR);
//////////////                g2.fillRect(cx - w/2, cy - h/2, w, h);
//////////////            }
//////////////
//////////////            // چراغ ترمز
//////////////            if (v.isBrakeLightOn()) {
//////////////                g2.setColor(Color.RED);
//////////////                g2.fillRect(cx + w/2 - 5, cy - 3, 4, 4);
//////////////                g2.fillRect(cx + w/2 - 5, cy + 1, 4, 4);
//////////////            }
//////////////            // راهنما
//////////////            if (v.isTurningLeft()) {
//////////////                g2.setColor(Color.ORANGE);
//////////////                g2.fillRect(cx - w/2, cy - 2, 4, 4);
//////////////            }
//////////////            if (v.isTurningRight()) {
//////////////                g2.setColor(Color.ORANGE);
//////////////                g2.fillRect(cx + w/2 - 4, cy - 2, 4, 4);
//////////////            }
//////////////
//////////////            g2.dispose();
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////
//////////////package ui; // // پکیج UI
//////////////
//////////////import core.Point;          // // نوع نقطه (مختصات جهان)
//////////////import core.Vehicle;        // // موجودیت خودرو
//////////////import infrastructure.CityMap; // // برای دسترسی به راه‌ها و تقاطع‌ها
//////////////import infrastructure.Road;     // // راه
//////////////import infrastructure.Lane;     // // لاین
//////////////import infrastructure.Intersection; // // تقاطع
//////////////import pedestrian.Pedestrian;   // // عابر
//////////////import simulation.World;        // // مدل جهان
//////////////import trafficcontrol.TrafficControlDevice; // // والد کنترل‌ها
//////////////import trafficcontrol.TrafficLight;         // // چراغ راهنما
//////////////import trafficcontrol.LightState;           // // وضعیت چراغ (RED/YELLOW/GREEN)
//////////////
//////////////import javax.imageio.ImageIO;   // // بارگذاری تصویر از فایل
//////////////import javax.swing.JPanel;      // // پنل سوئینگ
//////////////import java.awt.Graphics;       // // گرافیک پایه
//////////////import java.awt.Graphics2D;     // // گرافیک 2بعدی
//////////////import java.awt.RenderingHints; // // آنتی‌الیاس و کیفیت
//////////////import java.awt.BasicStroke;    // // ضخامت خطوط
//////////////import java.awt.Color;          // // رنگ‌ها
//////////////import java.awt.Dimension;      // // اندازه پنل
//////////////import java.awt.Image;          // // نگه‌داری تصویر
//////////////import java.io.File;            // // فایل سیستم
//////////////import java.util.List;          // // لیست‌ها
//////////////import java.util.Map;           // // مپ کنترل‌ها
//////////////import java.util.ArrayList;     // // آرایه پویا
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل رسم کل شبیه‌سازی
//////////////    private final World world;   // // مرجع جهان (خودرو/عابر/چراغ/نقشه)
//////////////    private final Camera camera; // // دوربین (تبدیل مختصات جهان→صفحه)
//////////////
//////////////    // --- تصاویر اختیاری (PNG) ---
//////////////    private Image carImage;        // // تصویر خودرو
//////////////    private Image pedestrianImage; // // تصویر عابر (اگر null باشد، دایره می‌کشیم)
//////////////    private Image lightImage;      // // تصویر پایهٔ چراغ (اختیاری)
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) { // // سازنده
//////////////        this.world = world;               // // نگه‌داری مرجع جهان
//////////////        this.camera = camera;             // // نگه‌داری مرجع دوربین
//////////////        setBackground(new Color(40, 120, 40)); // // پس‌زمینه سبز (چمن)
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT)); // // اندازهٔ پنل
//////////////        setFocusable(true);               // // فعال‌سازی دریافت فوکوس (برای کیبورد)
//////////////    }
//////////////
//////////////    public void loadAssets(String carPng, String pedPng, String lightPostPng) { // // لود تصاویر از مسیر فایل
//////////////        try {
//////////////            if (carPng != null)        carImage = ImageIO.read(new File(carPng));        // // بارگذاری خودرو
//////////////            if (pedPng != null)        pedestrianImage = ImageIO.read(new File(pedPng)); // // بارگذاری عابر
//////////////            if (lightPostPng != null)  lightImage = ImageIO.read(new File(lightPostPng)); // // بارگذاری بدنهٔ چراغ
//////////////        } catch (Exception e) {
//////////////            System.out.println("Failed to load assets: " + e.getMessage()); // // گزارش خطا (اختیاری)
//////////////        }
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) { // // متد اصلی رسم
//////////////        super.paintComponent(g); // // پاک‌سازی با رنگ پس‌زمینه
//////////////
//////////////        Graphics2D g2d = (Graphics2D) g; // // گرفتن Graphics2D برای امکانات بیشتر
//////////////        // بهبود کیفیت: آنتی‌الیاس و بی‌لینیر
//////////////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // نرم‌کردن خطوط
//////////////        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // // بهبود مقیاس تصویر
//////////////
//////////////        drawRoads(g2d);         // // رسم خیابان‌ها (بدنهٔ عریض + خط میانی)
//////////////        drawTrafficLights(g2d); // // رسم چراغ‌های راهنما
//////////////        drawPedestrians(g2d);   // // رسم عابرها
//////////////        drawVehicles(g2d);      // // رسم خودروها با چرخش تصویر و چراغ‌ها
//////////////    }
////////////
//////////////    // ======================= رسم خیابان‌ها (عریض) =======================
//////////////    private void drawRoads(Graphics2D g2d) { // // رسم راه‌ها
//////////////        CityMap map = world.getCityMap(); // // نقشه
//////////////        if (map == null) return;          // // اگر نقشه‌ای نیست، خروج
//////////////
//////////////        List<Road> roads = map.getRoads(); // // همهٔ راه‌ها
//////////////        for (int i = 0; i < roads.size(); i++) { // // پیمایش راه‌ها
//////////////            Road r = roads.get(i); // // یک راه
//////////////            Point aW = r.getStartIntersection().getPosition(); // // مختصات جهانِ سرِ شروع
//////////////            Point bW = r.getEndIntersection().getPosition();   // // مختصات جهانِ سرِ پایان
//////////////            Point a = camera.transform(aW); // // تبدیل به صفحه
//////////////            Point b = camera.transform(bW); // // تبدیل به صفحه
//////////////
//////////////            // محاسبهٔ پهنای کل راه = (تعداد لاین‌ها × پهنای هر لاین) + (تعداد فاصله‌ها × فاصلهٔ بین‌لاین)
//////////////            int lanesDirA = Math.max(1, r.getForwardLanes().size());  // // لاین‌های جهت A→B (معمولاً 2)
//////////////            int lanesDirB = Math.max(1, r.getBackwardLanes().size()); // // لاین‌های جهت B→A (معمولاً 2)
//////////////            int totalLanes = lanesDirA + lanesDirB; // // مجموع لاین‌ها (معمولاً 4)
//////////////            int totalGaps  = totalLanes - 1;       // // تعداد فاصله‌های بین‌لاین (معمولاً 3)
//////////////
//////////////            float totalWidth = totalLanes * UIConstants.LANE_WIDTH   // // پهنای آسفالت برای همهٔ لاین‌ها
//////////////                    + totalGaps  * UIConstants.LANE_GAP;    // // به‌علاوهٔ فاصله‌های بین‌شان
//////////////
//////////////            // بدنهٔ راه: یک خط ضخیم از A تا B
//////////////            g2d.setStroke(new BasicStroke(totalWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)); // // ضخامت = پهنای کل راه
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);  // // رنگ آسفالت
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY()); // // رسم بدنه
//////////////
//////////////            // خط میانی زرد (تزیینی/تقریبی): یک خط باریک دقیقا در مرکز
//////////////            g2d.setStroke(new BasicStroke(2f)); // // ضخامت کم
//////////////            g2d.setColor(UIConstants.CENTER_LINE_COLOR); // // رنگ زرد
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY()); // // رسم خط میانی
//////////////        }
//////////////    }
////////////
////////////
//////////////
//////////////    // داخل ui/SimulatorPanel.java
//////////////    private void drawRoads(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap();
//////////////        if (map == null) return;
//////////////
//////////////        List<Road> roads = map.getRoads();
//////////////        for (int i = 0; i < roads.size(); i++) {
//////////////            Road r = roads.get(i);
//////////////
//////////////            // نقاط جهان و تبدیل به صفحه
//////////////            Point aW = r.getStartIntersection().getPosition();
//////////////            Point bW = r.getEndIntersection().getPosition();
//////////////            Point aS = camera.transform(aW);
//////////////            Point bS = camera.transform(bW);
//////////////
//////////////            // بردار جهت و نرمال واحد (عمود)
//////////////            double dx = bS.getX() - aS.getX();
//////////////            double dy = bS.getY() - aS.getY();
//////////////            double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////            double nx = -dy / len; // نرمال X
//////////////            double ny =  dx / len; // نرمال Y
//////////////
//////////////            // مشخصات هر جهت
//////////////            int lanesA = Math.max(1, r.getForwardLanes().size());   // start -> end
//////////////            int lanesB = Math.max(1, r.getBackwardLanes().size());  // end -> start
//////////////
//////////////            // پهنای یک جهت = تعداد لاین * LANE_WIDTH + فاصله‌های بین‌لاین
//////////////            int gapsA = lanesA - 1;
//////////////            int gapsB = lanesB - 1;
//////////////            float widthA = lanesA * UIConstants.LANE_WIDTH + gapsA * UIConstants.LANE_GAP;
//////////////            float widthB = lanesB * UIConstants.LANE_WIDTH + gapsB * UIConstants.LANE_GAP;
//////////////
//////////////            // فاصلهٔ مرکز هر جهت تا خط مرکزی راه
//////////////            float halfGap = UIConstants.CARRIAGE_CENTER_GAP * 0.5f;
//////////////            float offsetA = (widthA * 0.5f) + halfGap; // جهت A در یک سمت
//////////////            float offsetB = (widthB * 0.5f) + halfGap; // جهت B در سمت مقابل
//////////////
//////////////            // مراکز دو جهت روی صفحه
//////////////            // مرکز خط: aS..bS — با نرمال به چپ/راست می‌رویم
//////////////            int aAx = (int)Math.round(aS.getX() + nx * (-offsetA)); // جهت A را یک سمت می‌گذاریم
//////////////            int aAy = (int)Math.round(aS.getY() + ny * (-offsetA));
//////////////            int bAx = (int)Math.round(bS.getX() + nx * (-offsetA));
//////////////            int bAy = (int)Math.round(bS.getY() + ny * (-offsetA));
//////////////
//////////////            int aBx = (int)Math.round(aS.getX() + nx * (+offsetB)); // جهت B در سمت دیگر
//////////////            int aBy = (int)Math.round(aS.getY() + ny * (+offsetB));
//////////////            int bBx = (int)Math.round(bS.getX() + nx * (+offsetB));
//////////////            int bBy = (int)Math.round(bS.getY() + ny * (+offsetB));
//////////////
//////////////            // 1) آسفالت جهت A
//////////////            g2d.setStroke(new BasicStroke(widthA, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////            g2d.drawLine(aAx, aAy, bAx, bAy);
//////////////
//////////////            // 2) آسفالت جهت B
//////////////            g2d.setStroke(new BasicStroke(widthB, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////            g2d.drawLine(aBx, aBy, bBx, bBy);
//////////////
//////////////            // 3) خطوط کناری سفید برای هر جهت
//////////////            drawEdgeLines(g2d, aAx, aAy, bAx, bAy, widthA);
//////////////            drawEdgeLines(g2d, aBx, aBy, bBx, bBy, widthB);
//////////////
//////////////            // 4) خط‌چین بین لاین‌های جهت A
//////////////            if (lanesA >= 2) drawLaneDashes(g2d, aAx, aAy, bAx, bAy, widthA, lanesA);
//////////////            // 5) خط‌چین بین لاین‌های جهت B
//////////////            if (lanesB >= 2) drawLaneDashes(g2d, aBx, aBy, bBx, bBy, widthB, lanesB);
//////////////
//////////////            // 6) وسط دو جهت: دو خط زرد (Double Yellow)
//////////////            drawDoubleYellow(g2d, aS.getX(), aS.getY(), bS.getX(), bS.getY());
//////////////        }
//////////////    }
//////////////
//////////////    // خط کناری سفید در دو طرف هر جهت
//////////////    private void drawEdgeLines(Graphics2D g2d, int ax, int ay, int bx, int by, float width) {
//////////////        float half = width * 0.5f;
//////////////        g2d.setColor(UIConstants.EDGE_LINE_COLOR);
//////////////        g2d.setStroke(new BasicStroke(2f)); // ضخامت خط کناری
//////////////        // نرمال واحد را دوباره می‌گیریم
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        // لبهٔ چپ
//////////////        int axL = (int)Math.round(ax + nx * (-half));
//////////////        int ayL = (int)Math.round(ay + ny * (-half));
//////////////        int bxL = (int)Math.round(bx + nx * (-half));
//////////////        int byL = (int)Math.round(by + ny * (-half));
//////////////        g2d.drawLine(axL, ayL, bxL, byL);
//////////////
//////////////        // لبهٔ راست
//////////////        int axR = (int)Math.round(ax + nx * (+half));
//////////////        int ayR = (int)Math.round(ay + ny * (+half));
//////////////        int bxR = (int)Math.round(bx + nx * (+half));
//////////////        int byR = (int)Math.round(by + ny * (+half));
//////////////        g2d.drawLine(axR, ayR, bxR, byR);
//////////////    }
//////////////
//////////////    // خط‌چین‌های بین لاین‌ها (داخل یک جهت)
//////////////    private void drawLaneDashes(Graphics2D g2d, int ax, int ay, int bx, int by, float width, int laneCount) {
//////////////        // فاصلهٔ هر خط‌چین را ساده نگه‌می‌داریم
//////////////        g2d.setColor(UIConstants.LANE_DASH_COLOR);
//////////////        float dash[] = {14f, 10f};
//////////////        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f));
//////////////
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        float half = width * 0.5f;
//////////////
//////////////        // برای laneCount لاین، بین هر دو لاین یک خط‌چین
//////////////        int gaps = laneCount - 1;
//////////////        for (int i = 1; i <= gaps; i++) {
//////////////            // فاصلهٔ i‌ام از لبهٔ چپ به سمت راست:
//////////////            float fromLeft = (i * UIConstants.LANE_WIDTH) + (i - 0.5f) * UIConstants.LANE_GAP - half;
//////////////            int axM = (int)Math.round(ax + nx * fromLeft);
//////////////            int ayM = (int)Math.round(ay + ny * fromLeft);
//////////////            int bxM = (int)Math.round(bx + nx * fromLeft);
//////////////            int byM = (int)Math.round(by + ny * fromLeft);
//////////////            g2d.drawLine(axM, ayM, bxM, byM);
//////////////        }
//////////////    }
//////////////
//////////////    // دو خط زرد وسط (Double Yellow) بین دو جهت
//////////////    private void drawDoubleYellow(Graphics2D g2d, int ax, int ay, int bx, int by) {
//////////////        g2d.setColor(UIConstants.CENTER_LINE_COLOR);
//////////////        g2d.setStroke(new BasicStroke(2.2f)); // خط زرد باریک
//////////////
//////////////        double dx = bx - ax, dy = by - ay;
//////////////        double len = Math.max(1.0, Math.hypot(dx, dy));
//////////////        double nx = -dy / len, ny = dx / len;
//////////////
//////////////        // فاصلهٔ هر خط زرد نسبت به مرکز
//////////////        float sep = UIConstants.CARRIAGE_CENTER_GAP * 0.5f;
//////////////
//////////////        // خط زرد سمت A
//////////////        int ax1 = (int)Math.round(ax + nx * (-sep));
//////////////        int ay1 = (int)Math.round(ay + ny * (-sep));
//////////////        int bx1 = (int)Math.round(bx + nx * (-sep));
//////////////        int by1 = (int)Math.round(by + ny * (-sep));
//////////////        g2d.drawLine(ax1, ay1, bx1, by1);
//////////////
//////////////        // خط زرد سمت B
//////////////        int ax2 = (int)Math.round(ax + nx * (+sep));
//////////////        int ay2 = (int)Math.round(ay + ny * (+sep));
//////////////        int bx2 = (int)Math.round(bx + nx * (+sep));
//////////////        int by2 = (int)Math.round(by + ny * (+sep));
//////////////        g2d.drawLine(ax2, ay2, bx2, by2);
//////////////    }
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////    // ======================= رسم چراغ‌های راهنما =======================
//////////////    private void drawTrafficLights(Graphics2D g2d) { // // رسم چراغ‌ها
//////////////        List<TrafficLight> lights = world.getTrafficLights(); // // لیست چراغ‌ها
//////////////        if (lights == null || lights.isEmpty()) return;       // // اگر خالی، خروج
//////////////
//////////////        for (int i = 0; i < lights.size(); i++) { // // روی همه چراغ‌ها
//////////////            TrafficLight tl = lights.get(i); // // چراغ فعلی
//////////////            Intersection host = findIntersectionOfTrafficLight(tl); // // تلاش برای یافتن تقاطع میزبان
//////////////            int x; int y; // // مختصات صفحه برای رسم چراغ
//////////////
//////////////            if (host != null) { // // اگر میزبان پیدا شد
//////////////                Point wp = host.getPosition();  // // مختصات جهان
//////////////                Point sp = camera.transform(wp); // // تبدیل به صفحه
//////////////                x = sp.getX() - 6; // // کمی جابه‌جایی برای دیده‌شدن
//////////////                y = sp.getY() - 30;
//////////////            } else {
//////////////                x = 30; y = 30; // // اگر میزبان پیدا نشد، یک گوشهٔ ثابت
//////////////            }
//////////////
//////////////            // اگر تصویر پایهٔ چراغ داریم، آن را هم بکشیم
//////////////            if (lightImage != null) {
//////////////                g2d.drawImage(lightImage, x - 6, y - 16, 16, 32, null); // // بدنهٔ چراغ
//////////////            }
//////////////
//////////////            // رنگ چراغ بر اساس State
//////////////            Color c;
//////////////            if (tl.getState() == LightState.RED)      c = Color.RED;
//////////////            else if (tl.getState() == LightState.YELLOW) c = Color.YELLOW;
//////////////            else                                      c = Color.GREEN;
//////////////
//////////////            // لامپ چراغ (دایرهٔ رنگی کوچک)
//////////////            g2d.setColor(c);
//////////////            g2d.fillOval(x, y, 12, 12); // // پر کردن دایره
//////////////            g2d.setColor(Color.BLACK);
//////////////            g2d.drawOval(x, y, 12, 12); // // قاب نازک دورش
//////////////        }
//////////////    }
//////////////
//////////////    // کمک: پیدا کردن تقاطعی که این چراغ را در کنترل‌هایش دارد
//////////////    private Intersection findIntersectionOfTrafficLight(TrafficLight tl) { // // یافتن میزبان چراغ
//////////////        CityMap map = world.getCityMap(); // // نقشه
//////////////        if (map == null) return null;     // // ایمنی
//////////////        List<Intersection> intersections = map.getIntersections(); // // همه تقاطع‌ها
//////////////        for (int i = 0; i < intersections.size(); i++) { // // پیمایش
//////////////            Intersection inter = intersections.get(i); // // یک تقاطع
//////////////            Map<core.Direction, TrafficControlDevice> controls = inter.getControls(); // // مپ کنترل‌ها
//////////////            if (controls == null) continue; // // اگر خالی، رد شو
//////////////            for (TrafficControlDevice dev : controls.values()) { // // روی همه کنترل‌های آن تقاطع
//////////////                if (dev == tl) return inter; // // اگر شیء یکسان است، میزبان یافت شد
//////////////            }
//////////////        }
//////////////        return null; // // پیدا نشد
//////////////    }
//////////////
//////////////    // ======================= رسم عابرها =======================
//////////////    private void drawPedestrians(Graphics2D g2d) { // // رسم عابر
//////////////        List<Pedestrian> people = world.getPedestrians(); // // لیست عابرها
//////////////        if (people == null || people.isEmpty()) return;   // // اگر خالی، خروج
//////////////
//////////////        for (int i = 0; i < people.size(); i++) { // // پیمایش عابرها
//////////////            Pedestrian p = people.get(i);      // // عابر فعلی
//////////////            Point wp = p.getPosition();        // // مختصات جهان
//////////////            Point sp = camera.transform(wp);   // // تبدیل به صفحه
//////////////
//////////////            if (pedestrianImage != null) { // // اگر تصویر عابر داریم
//////////////                g2d.drawImage(pedestrianImage, sp.getX() - 6, sp.getY() - 6, 12, 12, null); // // رسم کوچک
//////////////            } else {
//////////////                g2d.setColor(UIConstants.PEDESTRIAN_COLOR); // // رنگ پیش‌فرض عابر
//////////////                g2d.fillOval(sp.getX() - 4, sp.getY() - 4, 8, 8); // // دایرهٔ ساده
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // ======================= رسم خودروها =======================
//////////////    private void drawVehicles(Graphics2D g2d) { // // رسم با چرخش تصویر/مستطیل
//////////////        List<Vehicle> vs = world.getVehicles(); // // لیست خودروها
//////////////        if (vs == null || vs.isEmpty()) return; // // اگر خالی، خروج
//////////////
//////////////        for (int i = 0; i < vs.size(); i++) { // // پیمایش خودروها
//////////////            Vehicle v = vs.get(i);            // // خودرو فعلی
//////////////            Lane lane = v.getCurrentLane();   // // لاین فعلی خودرو
//////////////            if (lane == null) continue;       // // اگر لاینی ندارد، رد شو
//////////////
//////////////            // مکان خودرو روی لاین با احتساب آفست جانبی
//////////////            Point wp = lane.getPositionAt(v.getPositionInLane()); // // مختصات جهان
//////////////            Point sp = camera.transform(wp);                       // // تبدیل به صفحه
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH;   // // عرض نمایش خودرو
//////////////            int h = UIConstants.VEHICLE_HEIGHT;  // // ارتفاع نمایش خودرو
//////////////            int cx = sp.getX();                  // // مرکز X برای چرخش
//////////////            int cy = sp.getY();                  // // مرکز Y برای چرخش
//////////////
//////////////            Graphics2D g2 = (Graphics2D) g2d.create(); // // ساخت کانتکست موقت برای rotate
//////////////            g2.rotate(v.getAngle(), cx, cy);           // // چرخش حول مرکز خودرو (زاویه از Lane)
//////////////
//////////////            if (carImage != null) { // // اگر تصویر داریم
//////////////                g2.drawImage(carImage, cx - w/2, cy - h/2, w, h, null); // // رسم PNG خودرو با مرکزیت
//////////////            } else { // // اگر تصویر نداریم، مستطیل ساده
//////////////                g2.setColor(UIConstants.CAR_COLOR); // // رنگ بدنهٔ خودرو
//////////////                g2.fillRect(cx - w/2, cy - h/2, w, h); // // رسم مستطیل
//////////////            }
//////////////
//////////////            // چراغ ترمز: دو مربع کوچک قرمز در عقب خودرو
//////////////            if (v.isBrakeLightOn()) { // // اگر ترمز فعال است
//////////////                g2.setColor(Color.RED); // // قرمز
//////////////                g2.fillRect(cx + w/2 - 5, cy - 3, 4, 4); // // چراغ عقب بالا
//////////////                g2.fillRect(cx + w/2 - 5, cy + 1, 4, 4); // // چراغ عقب پایین
//////////////            }
//////////////
//////////////            // چراغ راهنما (فاز فعلی ساده): در لبه‌های چپ/راست خودرو
//////////////            // اگر خواستی چشمک‌زن شود، می‌توانیم با یک شمارندهٔ قاب پیاده کنیم.
//////////////            if (v.isTurningLeft()) { // // راهنمای چپ
//////////////                g2.setColor(Color.ORANGE); // // نارنجی
//////////////                g2.fillRect(cx - w/2, cy - 2, 4, 4); // // در لبهٔ چپ
//////////////            }
//////////////            if (v.isTurningRight()) { // // راهنمای راست
//////////////                g2.setColor(Color.ORANGE); // // نارنجی
//////////////                g2.fillRect(cx + w/2 - 4, cy - 2, 4, 4); // // در لبهٔ راست
//////////////            }
//////////////
//////////////            g2.dispose(); // // آزاد کردن کانتکست موقت تا rotate روی رسم‌های بعدی اثر نگذارد
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // // پکیج UI
//////////////
//////////////import core.Point; // // نوع نقطه (مختصات)
//////////////import core.Vehicle; // // برای دسترسی به وسایل نقلیه
//////////////import infrastructure.CityMap; // // نقشه شهر
//////////////import infrastructure.Road; // // راه
//////////////import infrastructure.Lane; // // لِین
//////////////import infrastructure.Intersection; // // تقاطع
//////////////import pedestrian.Pedestrian; // // عابر پیاده
//////////////import simulation.World; // // مدل جهان
//////////////import trafficcontrol.TrafficLight; // // چراغ راهنما
//////////////import trafficcontrol.TrafficControlDevice; // // دستگاه کنترل ترافیک (برای یافتن محل چراغ)
//////////////import trafficcontrol.LightState; // // وضعیت چراغ (RED/YELLOW/GREEN)
//////////////
//////////////import javax.imageio.ImageIO; // // بارگذاری تصویر از فایل
//////////////import javax.swing.JPanel; // // پنل سوئینگ
//////////////import java.awt.Graphics; // // گرافیک پایه
//////////////import java.awt.Graphics2D; // // گرافیک 2بعدی
//////////////import java.awt.RenderingHints; // // بهبود کیفیت رسم
//////////////import java.awt.BasicStroke; // // ضخامت خطوط
//////////////import java.awt.Color; // // رنگ‌ها
//////////////import java.awt.Dimension; // // اندازه پنل
//////////////import java.awt.Image; // // نگهداری تصویر
//////////////import java.io.File; // // دسترسی به فایل‌ها
//////////////import java.util.List; // // لیست‌ها
//////////////import java.util.Map; // // مپ برای Controls
//////////////import java.util.ArrayList; // // آرایه پویا
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل رندر کل شبیه‌سازی
//////////////    private final World world;   // // مرجع مدل جهان (خودروها، عابرها، چراغ‌ها، نقشه)
//////////////    private final Camera camera; // // دوربین برای تبدیل مختصات جهان->صفحه
//////////////
//////////////    // --- تصاویر اختیاری (اگر null باشند از رسم ساده استفاده می‌شود) ---
//////////////    private Image carImage;         // // تصویر خودرو (PNG)
//////////////    private Image pedestrianImage;  // // تصویر عابر (PNG)
//////////////    private Image lightImage;       // // تصویر پایه چراغ راهنما (اختیاری؛ اگر null باشد دایره رنگی می‌کشیم)
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) { // // سازنده
//////////////        this.world = world; // // ذخیره مرجع مدل
//////////////        this.camera = camera; // // ذخیره مرجع دوربین
//////////////        setBackground(new Color(40, 120, 40)); // // پس‌زمینه سبز (چمن)
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT)); // // اندازه پنل
//////////////        setFocusable(true); // // گرفتن فوکوس برای ورودی‌ها
//////////////    }
//////////////
//////////////    // بارگذاری تصاویر از مسیر فایل سیستم؛ اگر هر مسیر null باشد، آن مورد بدون تصویر می‌ماند
//////////////    public void loadAssets(String carPng, String pedPng, String lightPostPng) {
//////////////        try {
//////////////            if (carPng != null) {
//////////////                carImage = ImageIO.read(new File(carPng)); // // بارگذاری تصویر خودرو
//////////////            }
//////////////            if (pedPng != null) {
//////////////                pedestrianImage = ImageIO.read(new File(pedPng)); // // بارگذاری تصویر عابر
//////////////            }
//////////////            if (lightPostPng != null) {
//////////////                lightImage = ImageIO.read(new File(lightPostPng)); // // بارگذاری تصویر چراغ
//////////////            }
//////////////        } catch (Exception e) {
//////////////            System.out.println("Failed to load assets: " + e.getMessage()); // // گزارش خطای لود
//////////////        }
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) { // // متد اصلی رسم که با هر repaint صدا زده می‌شود
//////////////        super.paintComponent(g); // // پاک‌سازی پس‌زمینه بر اساس setBackground
//////////////
//////////////        Graphics2D g2d = (Graphics2D) g; // // تبدیل به Graphics2D برای امکانات بیشتر
//////////////
//////////////        // فعال‌سازی آنتی‌الیاس و بهبود کیفیت تصویر
//////////////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // // نرم‌کردن لبه‌ها
//////////////        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // // بهبود مقیاس‌بندی تصاویر
//////////////
//////////////        drawRoads(g2d);          // // رسم جاده‌ها (بدنه و خط میانی)
//////////////        drawTrafficLights(g2d);  // // رسم چراغ‌های راهنما (تصویر یا دایره رنگی)
//////////////        drawPedestrians(g2d);    // // رسم عابرها (تصویر یا دایره)
//////////////        drawVehicles(g2d);       // // رسم خودروها با چرخش تصویر و چراغ ترمز
//////////////    }
//////////////
//////////////    // ======================= رسم جاده‌ها =======================
//////////////    private void drawRoads(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap(); // // مرجع نقشه
//////////////        if (map == null) return;          // // اگر نقشه‌ای نیست، کاری نکن
//////////////
//////////////        List<Road> roads = map.getRoads(); // // همه راه‌ها
//////////////        for (Road r : roads) { // // برای تک‌تک راه‌ها
//////////////            // گرفتن دو سر راه و تبدیل به مختصات صفحه با دوربین
//////////////            Point a = camera.transform(r.getStartIntersection().getPosition()); // // نقطه شروع راه در صفحه
//////////////            Point b = camera.transform(r.getEndIntersection().getPosition());   // // نقطه پایان راه در صفحه
//////////////
//////////////            // بدنه جاده (خط ضخیم خاکستری تیره)
//////////////            g2d.setStroke(new BasicStroke(UIConstants.LANE_WIDTH)); // // ضخامت معادل پهنای لاین
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);                   // // رنگ بدنه راه
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY());  // // رسم بدنه راه
//////////////
//////////////            // خط میانی نازک (برای زیبایی؛ می‌توان dashed هم کرد)
//////////////            g2d.setStroke(new BasicStroke(1));                     // // ضخامت کم
//////////////            g2d.setColor(UIConstants.LANE_LINE_COLOR);             // // رنگ خط میانی
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY());  // // رسم خط میانی
//////////////        }
//////////////    }
//////////////
//////////////    // ======================= رسم چراغ‌ها =======================
//////////////    private void drawTrafficLights(Graphics2D g2d) {
//////////////        List<TrafficLight> lights = world.getTrafficLights(); // // همه چراغ‌ها
//////////////        if (lights == null || lights.isEmpty()) return;        // // اگر چراغی نداریم، رد شو
//////////////
//////////////        // تلاش می‌کنیم هر چراغ را نزدیک تقاطعی بکشیم که آن را در کنترل‌هایش نگه داشته
//////////////        for (TrafficLight tl : lights) {
//////////////            // پیدا کردن تقاطعی که این چراغ را به عنوان کنترل نگه داشته
//////////////            Intersection host = findIntersectionOfTrafficLight(tl); // // ممکن است null باشد
//////////////            int x, y; // // مختصات صفحه برای رسم چراغ
//////////////
//////////////            if (host != null) {
//////////////                Point wp = host.getPosition();      // // مختصات جهان تقاطع
//////////////                Point sp = camera.transform(wp);     // // تبدیل به صفحه
//////////////                x = sp.getX() - 6;                   // // کمی جابه‌جایی برای دیده‌شدن
//////////////                y = sp.getY() - 30;                  // // بالای تقاطع
//////////////            } else {
//////////////                // اگر پیدا نشد، به صورت نمایشی یک گوشه ثابت بکش
//////////////                x = 30;
//////////////                y = 30;
//////////////            }
//////////////
//////////////            if (lightImage != null) {
//////////////                // اگر تصویر چراغ داریم، آن را رسم کنیم و رویش یک دایره رنگی کوچک بزنیم
//////////////                g2d.drawImage(lightImage, x - 6, y - 16, 16, 32, null); // // رسم بدنه پایه چراغ
//////////////            }
//////////////
//////////////            // انتخاب رنگ چراغ
//////////////            Color c;
//////////////            if (tl.getState() == LightState.RED) c = Color.RED;
//////////////            else if (tl.getState() == LightState.YELLOW) c = Color.YELLOW;
//////////////            else c = Color.GREEN;
//////////////
//////////////            // اگر تصویر چراغ نداریم، فقط دایره رنگی رسم می‌کنیم
//////////////            g2d.setColor(c);
//////////////            g2d.fillOval(x, y, 12, 12); // // رسم لامپ چراغ
//////////////            g2d.setColor(Color.BLACK);
//////////////            g2d.drawOval(x, y, 12, 12); // // قاب نازک دور لامپ
//////////////        }
//////////////    }
//////////////
//////////////    // تلاش برای یافتن تقاطعی که این TrafficLight را در کنترل‌هایش دارد
//////////////    private Intersection findIntersectionOfTrafficLight(TrafficLight tl) {
//////////////        CityMap map = world.getCityMap(); // // نقشه
//////////////        if (map == null) return null;
//////////////
//////////////        List<Intersection> intersections = map.getIntersections(); // // همه تقاطع‌ها
//////////////        for (Intersection inter : intersections) {
//////////////            // فرض: Intersection یک Map<Direction, TrafficControlDevice> دارد
//////////////            Map<core.Direction, TrafficControlDevice> controls = inter.getControls(); // // گرفتن مپ کنترل‌ها
//////////////            if (controls == null) continue;
//////////////
//////////////            for (TrafficControlDevice dev : controls.values()) {
//////////////                if (dev == tl) { // // اگر همان شیء چراغ باشد
//////////////                    return inter; // // تقاطع میزبان همین است
//////////////                }
//////////////            }
//////////////        }
//////////////        return null; // // یافت نشد
//////////////    }
//////////////
//////////////    // ======================= رسم عابرها =======================
//////////////    private void drawPedestrians(Graphics2D g2d) {
//////////////        List<Pedestrian> people = world.getPedestrians(); // // لیست عابرها
//////////////        if (people == null || people.isEmpty()) return;   // // اگر عابری نیست، خروج
//////////////
//////////////        for (Pedestrian p : people) {
//////////////            Point wp = p.getPosition();           // // موقعیت عابر در جهان
//////////////            Point sp = camera.transform(wp);      // // تبدیل به صفحه
//////////////
//////////////            if (pedestrianImage != null) {
//////////////                // اگر تصویر عابر داریم، با اندازه کوچک رسمش می‌کنیم
//////////////                g2d.drawImage(pedestrianImage, sp.getX() - 6, sp.getY() - 6, 12, 12, null); // // تصویر کوچک
//////////////            } else {
//////////////                // در غیر اینصورت، دایره ساده
//////////////                g2d.setColor(UIConstants.PEDESTRIAN_COLOR); // // رنگ عابر
//////////////                g2d.fillOval(sp.getX() - 4, sp.getY() - 4, 8, 8); // // رسم دایره
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // ======================= رسم خودروها =======================
//////////////    private void drawVehicles(Graphics2D g2d) {
//////////////        List<Vehicle> vs = world.getVehicles(); // // لیست خودروها
//////////////        if (vs == null || vs.isEmpty()) return; // // اگر خودرویی نداریم، خروج
//////////////
//////////////        for (Vehicle v : vs) {
//////////////            Lane lane = v.getCurrentLane(); // // لِین فعلی خودرو
//////////////            if (lane == null) continue;     // // اگر لِین ندارد، نمی‌توان نقطه‌اش را مشخص کرد
//////////////
//////////////            // موقعیت خودرو روی لِین بر اساس positionInLane
//////////////            Point wp = lane.getPositionAt(v.getPositionInLane()); // // نقطه جهان
//////////////            Point sp = camera.transform(wp);                       // // تبدیل به صفحه
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH;    // // عرض نمایش خودرو
//////////////            int h = UIConstants.VEHICLE_HEIGHT;   // // ارتفاع نمایش خودرو
//////////////            int cx = sp.getX();                   // // مرکز X برای چرخش
//////////////            int cy = sp.getY();                   // // مرکز Y برای چرخش
//////////////
//////////////            Graphics2D g2 = (Graphics2D) g2d.create(); // // کپی امن از کانتکست برای اعمال rotate
//////////////            g2.rotate(v.getAngle(), cx, cy);           // // چرخش حول مرکز خودرو
//////////////
//////////////            if (carImage != null) {
//////////////                // اگر تصویر داریم، تصویر را طوری رسم می‌کنیم که مرکز آن روی (cx, cy) باشد
//////////////                g2.drawImage(carImage, cx - w/2, cy - h/2, w, h, null); // // رسم خودرو (PNG)
//////////////            } else {
//////////////                // در غیر اینصورت، مستطیل ساده
//////////////                g2.setColor(UIConstants.CAR_COLOR); // // رنگ بدنه
//////////////                g2.fillRect(cx - w/2, cy - h/2, w, h); // // رسم بدنه
//////////////            }
//////////////
//////////////            // چراغ ترمز (در انتهای خودرو)
//////////////            if (v.isBrakeLightOn()) {
//////////////                g2.setColor(Color.RED); // // رنگ چراغ ترمز
//////////////                g2.fillRect(cx + w/2 - 5, cy - 2, 4, 4); // // مربع کوچک قرمز سمت عقب
//////////////                g2.fillRect(cx + w/2 - 5, cy + 2, 4, 4); // // دومی برای دو چراغ عقب
//////////////            }
//////////////
//////////////            // (فاز بعد) چراغ راهنمای چپ/راست در زمان پیچیدن:
//////////////            // if (v.isTurningLeft()) { g2.setColor(Color.ORANGE); g2.fillRect(cx - w/2, cy - 2, 4, 4); }
//////////////            // if (v.isTurningRight()) { g2.setColor(Color.ORANGE); g2.fillRect(cx + w/2 - 4, cy - 2, 4, 4); }
//////////////
//////////////            g2.dispose(); // // آزاد کردن کانتکست تا rotate روی بقیهٔ رسم اثر نگذارد
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui; // // پکیج UI
//////////////
//////////////import core.Point; // // برای مختصات
//////////////import core.Vehicle; // // برای وسایل نقلیه
//////////////import infrastructure.CityMap; // // برای نقشه
//////////////import infrastructure.Road; // // برای جاده
//////////////import infrastructure.Lane; // // برای لِین
//////////////import simulation.World; // // برای دسترسی به مدل
//////////////import trafficcontrol.TrafficLight; // // برای چراغ‌ها
//////////////
//////////////import javax.imageio.ImageIO; // // برای بارگذاری تصویر (اختیاری)
//////////////import javax.swing.JPanel; // // پنل Swing
//////////////import java.awt.*; // // گرافیک 2D
//////////////import java.io.File; // // فایل برای تصویر
//////////////
//////////////public class SimulatorPanel extends JPanel { // // پنل رندر شبیه‌سازی
//////////////    private final World world; // // مدل جهان
//////////////    private final Camera camera; // // دوربین برای جابجایی نما
//////////////
//////////////    private Image carImage; // // تصویر ماشین (اختیاری)
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) { // // سازنده
//////////////        this.world = world; // // مقداردهی مدل
//////////////        this.camera = camera; // // مقداردهی دوربین
//////////////        setBackground(new Color(30, 30, 30)); // // رنگ پس‌زمینه
//////////////        setPreferredSize(new Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT)); // // اندازه پنل
//////////////        setFocusable(true); // // فعال‌سازی فوکوس برای ورودی‌ها
//////////////    }
//////////////
//////////////    public void loadAssets(String carPng, String pedPng, String lightPostPng) { // // بارگذاری تصاویر (دلخواه)
//////////////        try { // // بلاک امن
//////////////            if (carPng != null) carImage = ImageIO.read(new File(carPng)); // // لود عکس ماشین
//////////////        } catch (Exception ignored) {} // // نادیده گرفتن خطا برای تست سریع
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) { // // متد رسم
//////////////        super.paintComponent(g); // // پاک‌سازی زمینه
//////////////        Graphics2D g2d = (Graphics2D) g; // // تبدیل به 2بعدی
//////////////
//////////////        drawRoads(g2d); // // رسم جاده‌ها و لِین‌ها
//////////////        drawTrafficLights(g2d); // // رسم چراغ‌ها
//////////////        drawVehicles(g2d); // // رسم وسایل نقلیه
//////////////    }
//////////////
//////////////    private void drawRoads(Graphics2D g2d) { // // رسم راه‌ها
//////////////        CityMap map = world.getCityMap(); // // گرفتن نقشه
//////////////        if (map == null) return; // // اگر نقشه نبود، خروج
//////////////
//////////////        g2d.setStroke(new BasicStroke(UIConstants.LANE_WIDTH)); // // ضخامت برای پهنای جاده
//////////////        g2d.setColor(UIConstants.ROAD_COLOR); // // رنگ جاده
//////////////
//////////////        for (Road r : map.getRoads()) { // // پیمایش جاده‌ها
//////////////            Point a = camera.transform(r.getStartIntersection().getPosition()); // // نقطه شروع با دوربین
//////////////            Point b = camera.transform(r.getEndIntersection().getPosition()); // // نقطه پایان با دوربین
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY()); // // رسم خود جاده
//////////////
//////////////            g2d.setColor(UIConstants.LANE_LINE_COLOR); // // رنگ خط میانی
//////////////            g2d.setStroke(new BasicStroke(1)); // // ضخامت کم برای خط
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY()); // // خط میانی ساده
//////////////            g2d.setColor(UIConstants.ROAD_COLOR); // // بازگشت به رنگ جاده
//////////////            g2d.setStroke(new BasicStroke(UIConstants.LANE_WIDTH)); // // بازگشت ضخامت جاده
//////////////        }
//////////////    }
//////////////
//////////////    private void drawVehicles(Graphics2D g2d) { // // رسم خودروها
//////////////        for (Vehicle v : world.getVehicles()) { // // پیمایش وسایل
//////////////            if (v.getCurrentLane() == null) continue; // // اگر لِین نداشت، رد شو
//////////////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // // نقطه جهانی روی لِین
//////////////            Point sp = camera.transform(wp); // // تبدیل به مختصات صفحه
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH; // // عرض رسم خودرو
//////////////            int h = UIConstants.VEHICLE_HEIGHT; // // ارتفاع رسم خودرو
//////////////
//////////////            if (carImage != null) { // // اگر عکس داریم
//////////////                g2d.drawImage(carImage, sp.getX() - w/2, sp.getY() - h/2, w, h, null); // // رسم عکس
//////////////            } else { // // در غیر اینصورت، مستطیل ساده
//////////////                g2d.setColor(UIConstants.CAR_COLOR); // // رنگ خودرو
//////////////                g2d.fillRect(sp.getX() - w/2, sp.getY() - h/2, w, h); // // رسم بدنه
//////////////            }
//////////////
//////////////            if (v.isBrakeLightOn()) { // // اگر چراغ ترمز روشن است
//////////////                g2d.setColor(Color.RED); // // رنگ قرمز
//////////////                g2d.fillRect(sp.getX() + w/2 - 4, sp.getY() - 2, 4, 4); // // رسم چراغ عقب
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////
//////////////
//////////////    private void drawVehicles(Graphics2D g2d) {
//////////////        for (core.Vehicle v : world.getVehicles()) {
//////////////            if (v.getCurrentLane() == null) continue;
//////////////
//////////////            core.Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane()); // نقطه جهان
//////////////            core.Point sp = camera.transform(wp);                                     // نقطه صفحه
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH;   // عرض
//////////////            int h = UIConstants.VEHICLE_HEIGHT;  // ارتفاع
//////////////
//////////////            int cx = sp.getX(); // مرکز برای چرخش
//////////////            int cy = sp.getY();
//////////////
//////////////            Graphics2D g2 = (Graphics2D) g2d.create(); // کپی امن از کانتکست
//////////////            g2.rotate(v.getAngle(), cx, cy);           // چرخش حول مرکز
//////////////
//////////////            if (carImage != null) {
//////////////                g2.drawImage(carImage, cx - w/2, cy - h/2, w, h, null); // رسم تصویر
//////////////            } else {
//////////////                g2.setColor(UIConstants.CAR_COLOR);
//////////////                g2.fillRect(cx - w/2, cy - h/2, w, h);                  // مستطیل ساده
//////////////            }
//////////////
//////////////            // چراغ ترمز اگر روشن بود
//////////////            if (v.isBrakeLightOn()) {
//////////////                g2.setColor(java.awt.Color.RED);
//////////////                g2.fillRect(cx + w/2 - 5, cy - 2, 4, 4);
//////////////            }
//////////////
//////////////            g2.dispose(); // آزاد کردن کانتکست
//////////////        }
//////////////    }
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////    private void drawTrafficLights(Graphics2D g2d) { // // رسم چراغ‌ها
//////////////        for (TrafficLight tl : world.getTrafficLights()) { // // پیمایش چراغ‌ها
//////////////            // نسخه مینیمال: فعلاً در گوشه ثابت؛ بعداً کنار تقاطع‌ها قرار می‌دهیم //
//////////////            int x = 30; // // مختصات X نمایشی
//////////////            int y = 30; // // مختصات Y نمایشی
//////////////            Color c; // // رنگ چراغ
//////////////            if (tl.getState() == trafficcontrol.LightState.RED) c = Color.RED; // // قرمز
//////////////            else if (tl.getState() == trafficcontrol.LightState.GREEN) c = Color.GREEN; // // سبز
//////////////            else c = Color.YELLOW; // // زرد
//////////////            g2d.setColor(c); // // تنظیم رنگ
//////////////            g2d.fillOval(x, y, 12, 12); // // رسم چراغ
//////////////        }
//////////////    }
//////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package ui;
//////////////
//////////////import core.Point;
//////////////import core.Vehicle;
//////////////import infrastructure.CityMap;
//////////////import infrastructure.Lane;
//////////////import infrastructure.Road;
//////////////import pedestrian.Pedestrian;
//////////////import simulation.World;
//////////////import trafficcontrol.TrafficLight;
//////////////
//////////////import javax.imageio.ImageIO;
//////////////import javax.swing.JPanel;
//////////////import java.awt.Graphics;
//////////////import java.awt.Graphics2D;
//////////////import java.awt.Color;
//////////////import java.awt.Image;
//////////////import java.io.File;
//////////////
//////////////public class SimulatorPanel extends JPanel {
//////////////    private final World world;
//////////////    private final Camera camera;
//////////////
//////////////    // اگر خواستی با عکس کار کنی اینا رو مقداردهی کن
//////////////    private Image carImage;
//////////////    private Image pedestrianImage;
//////////////    private Image trafficLightPostImage;
//////////////
//////////////    public SimulatorPanel(World world, Camera camera) {
//////////////        this.world = world;
//////////////        this.camera = camera;
//////////////        setBackground(new Color(30, 30, 30));
//////////////        setFocusable(true);
//////////////        setPreferredSize(new java.awt.Dimension(UIConstants.PANEL_WIDTH, UIConstants.PANEL_HEIGHT));
//////////////    }
//////////////
//////////////    public void loadAssets(String carPng, String pedPng, String lightPostPng) {
//////////////        try {
//////////////            if (carPng != null) carImage = ImageIO.read(new File(carPng));
//////////////            if (pedPng != null) pedestrianImage = ImageIO.read(new File(pedPng));
//////////////            if (lightPostPng != null) trafficLightPostImage = ImageIO.read(new File(lightPostPng));
//////////////        } catch (Exception ignored) {}
//////////////    }
//////////////
//////////////    @Override
//////////////    protected void paintComponent(Graphics g) {
//////////////        super.paintComponent(g);
//////////////        camera.updateAutoFollow();
//////////////        Graphics2D g2d = (Graphics2D) g;
//////////////
//////////////        drawRoads(g2d);
//////////////        drawTrafficLights(g2d);
//////////////        drawPedestrians(g2d);
//////////////        drawVehicles(g2d);
//////////////    }
//////////////
//////////////    private void drawRoads(Graphics2D g2d) {
//////////////        CityMap map = world.getCityMap();
//////////////        if (map == null) return;
//////////////
//////////////        g2d.setColor(UIConstants.ROAD_COLOR);
//////////////        for (Road r : map.getRoads()) {
//////////////            var a = camera.transform(r.getStartIntersection().getPosition());
//////////////            var b = camera.transform(r.getEndIntersection().getPosition());
//////////////            g2d.setStroke(new java.awt.BasicStroke(UIConstants.LANE_WIDTH));
//////////////            g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
//////////////
//////////////            g2d.setColor(UIConstants.LANE_LINE_COLOR);
//////////////            g2d.setStroke(new java.awt.BasicStroke(1));
//////////////            for (Lane l : r.getForwardLanes()) {
//////////////                // برای سادگی یک خط مرکزی رسم می‌کنیم
//////////////                g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
//////////////            }
//////////////            for (Lane l : r.getBackwardLanes()) {
//////////////                g2d.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
//////////////            }
//////////////            g2d.setColor(UIConstants.ROAD_COLOR);
//////////////        }
//////////////    }
//////////////
//////////////    private void drawVehicles(Graphics2D g2d) {
//////////////        for (Vehicle v : world.getVehicles()) {
//////////////            if (v.getCurrentLane() == null) continue;
//////////////            Point wp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
//////////////            Point sp = camera.transform(wp);
//////////////
//////////////            int w = UIConstants.VEHICLE_WIDTH;
//////////////            int h = UIConstants.VEHICLE_HEIGHT;
//////////////
//////////////            if (carImage != null) {
//////////////                g2d.drawImage(carImage, sp.getX() - w/2, sp.getY() - h/2, w, h, null);
//////////////            } else {
//////////////                g2d.setColor(UIConstants.CAR_COLOR);
//////////////                g2d.fillRect(sp.getX() - w/2, sp.getY() - h/2, w, h);
//////////////            }
//////////////
//////////////            // چراغ ترمز
//////////////            if (v.isBrakeLightOn()) {
//////////////                g2d.setColor(Color.RED);
//////////////                g2d.fillRect(sp.getX() + w/2 - 5, sp.getY() - 2, 4, 4);
//////////////            }
//////////////
//////////////            // اگر بعداً خواستی راهنما اضافه کنیم: v.isTurningLeft()/isTurningRight()
//////////////            // g2d.setColor(Color.ORANGE); fillRect(...) مطابق سمت دلخواه
//////////////        }
//////////////    }
//////////////
//////////////    private void drawTrafficLights(Graphics2D g2d) {
//////////////        for (TrafficLight tl : world.getTrafficLights()) {
//////////////            var pos = tl.getDirectionControlled(); // جهت کنترل‌شده داریم، برای سادگی فرض می‌کنیم کنار تقاطع است
//////////////            // برای نمایش ساده، چراغ را نزدیک تقاطع انتهایی هر Road می‌گذاریم یا از جایگاه ثابت استفاده می‌کنیم
//////////////            // این نسخه مینیمال: یک چراغ گوشه بالا-چپ
//////////////            int x = 30, y = 30;
//////////////            if (trafficLightPostImage != null) {
//////////////                g2d.drawImage(trafficLightPostImage, x-8, y-20, 16, 40, null);
//////////////            }
//////////////            Color c = switch (tl.getState()) {
//////////////                case RED -> Color.RED;
//////////////                case GREEN -> Color.GREEN;
//////////////                case YELLOW -> Color.YELLOW;
//////////////            };
//////////////            g2d.setColor(c);
//////////////            g2d.fillOval(x, y, 12, 12);
//////////////        }
//////////////    }
//////////////
//////////////    private void drawPedestrians(Graphics2D g2d) {
//////////////        for (Pedestrian p : world.getPedestrians()) {
//////////////            var wp = p.getPosition();
//////////////            var sp = camera.transform(wp);
//////////////            if (pedestrianImage != null) {
//////////////                g2d.drawImage(pedestrianImage, sp.getX() - 6, sp.getY() - 6, 12, 12, null);
//////////////            } else {
//////////////                g2d.setColor(UIConstants.PEDESTRIAN_COLOR);
//////////////                g2d.fillOval(sp.getX() - 4, sp.getY() - 4, 8, 8);
//////////////            }
//////////////        }
//////////////    }
//////////////}
