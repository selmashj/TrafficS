package ui;                                                    // پکیج UI

import simulation.World;                                       // دنیا
import simulation.DemoMaps;                                    // سازندهٔ نقشهٔ راست
import simulation.DemoTraffic;                                 // نصب چراغ و افزودن خودرو
import infrastructure.CityMap;                                 // نقشه
import infrastructure.Intersection;                            // تقاطع‌ها
import core.Point;                                             // نقطهٔ صحیح
import javax.swing.*;                                          // سوئینگ
import java.awt.*;                                             // چیدمان/رنگ
import java.awt.event.*;                                       // رویدادها
import java.util.List;                                         // لیست

public class MainWindow extends JFrame {                        // پنجرهٔ اصلی
    private final World world;                                  // دنیا
    private final SimulatorPanel panel;                         // پنل رندر
    private final Camera camera;                                // دوربین
    private final Timer repaintTimer;                           // تایمر فقط برای repaint

    public MainWindow() {                                       // سازنده
        super("Traffic Simulator — Grid (auto-fit)");           // عنوان پنجره

        // 1) ساخت نقشه بزرگ‌تر (مثلاً دو برابر قدیم)
        int[] colRatios = new int[]{8, 4, 14, 6, 10};           // نسبت ستون‌ها
        int[] rowRatios = new int[]{5, 12, 7, 9};               // نسبت ردیف‌ها
        CityMap map = DemoMaps.gridByRatios(                    // تولید شبکهٔ ۱۰۰٪ راست
                rowRatios, colRatios,                           // نسبت‌ها
                2800, 3600,                                     // ⬅ ارتفاع/عرض کل دوبرابرِ قبل
                2                                               // دو لِین در هر جهت
        );

        // 2) دنیا و چراغ‌ها
        this.world = new World(map);                            // ساخت دنیا
        DemoTraffic.installLights(world, map, 12000, 3000, 11000); // نصب چراغ‌ها

        // 3) دوربین و پنل
        this.camera = new Camera();                             // دوربین
        this.panel  = new SimulatorPanel(world, camera);        // پنل رندر

        // 4) چیدمان پنجره
        setLayout(new BorderLayout());                          // چیدمان
        add(panel, BorderLayout.CENTER);                        // افزودن پنل
        add(buildControlBar(), BorderLayout.SOUTH);             // نوار کنترل
        setSize(1280, 820);                                     // اندازهٔ پنجره
        setLocationRelativeTo(null);                            // مرکز
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);         // خروج
        setVisible(true);                                       // نمایش

        // 5) پس از نمایان‌شدن، دوربین را خودکار روی کل نقشه فیت کن
        SwingUtilities.invokeLater(new Runnable(){              // اجرای بعد از نمایش
            @Override public void run(){                        // بدنه
                fitCameraToMap();                               // تنظیم زوم/آفست
                panel.requestFocusInWindow();                   // فوکوس برای ورودی‌ها
            }
        });

        // 6) تایمر 16ms فقط برای repaint
        this.repaintTimer = new Timer(16, new ActionListener(){ // تایمر رسم
            @Override public void actionPerformed(ActionEvent e){
                panel.repaint();                                // فقط بازترسیم
            }
        });
        repaintTimer.start();                                   // استارت تایمر
    }

    private JComponent buildControlBar(){                       // ساخت کنترل‌ها
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); // پنل دکمه‌ها
        JButton btnFit   = new JButton("Fit");                  // دکمهٔ فیت
        JButton btnZoomP = new JButton("Zoom+");                // زوم+
        JButton btnZoomM = new JButton("Zoom-");                // زوم-
        JButton btnAdd   = new JButton("Add Vehicle");          // افزودن خودرو

        btnFit.addActionListener(new ActionListener(){          // لیسنر فیت
            @Override public void actionPerformed(ActionEvent e){
                fitCameraToMap();                               // فیت مجدد
                panel.repaint();                                // رسم
            }
        });
        btnZoomP.addActionListener(new ActionListener(){        // زوم+
            @Override public void actionPerformed(ActionEvent e){
                camera.setScale(camera.getScale()*1.10);        // افزایش زوم
                panel.repaint();                                // رسم
            }
        });
        btnZoomM.addActionListener(new ActionListener(){        // زوم-
            @Override public void actionPerformed(ActionEvent e){
                camera.setScale(camera.getScale()/1.10);        // کاهش زوم
                panel.repaint();                                // رسم
            }
        });
        btnAdd.addActionListener(new ActionListener(){          // افزودن خودرو
            @Override public void actionPerformed(ActionEvent e){
                simulation.DemoTraffic.addRandomVehicle(world, world.getMap()); // اضافه
            }
        });

        p.add(btnFit); p.add(btnZoomP); p.add(btnZoomM); p.add(btnAdd); // چیدمان دکمه‌ها
        return p;                                              // خروجی
    }

    private void fitCameraToMap(){                              // ✅ فیت‌کردن خودکار دوربین روی نقشه
        List<Intersection> xs = world.getMap().getIntersections(); // همهٔ تقاطع‌ها
        if (xs == null || xs.isEmpty()) return;                // اگر نبود، خروج

        int minX = Integer.MAX_VALUE;                           // کمینه X
        int minY = Integer.MAX_VALUE;                           // کمینه Y
        int maxX = Integer.MIN_VALUE;                           // بیشینه X
        int maxY = Integer.MIN_VALUE;                           // بیشینه Y

        for (int i=0;i<xs.size();i++){                         // پیمایش تقاطع‌ها
            Point p = xs.get(i).getPosition();                  // مختصات مرکز
            if (p.getX() < minX) minX = p.getX();               // به‌روزرسانی کمینه X
            if (p.getY() < minY) minY = p.getY();               // به‌روزرسانی کمینه Y
            if (p.getX() > maxX) maxX = p.getX();               // به‌روزرسانی بیشینه X
            if (p.getY() > maxY) maxY = p.getY();               // به‌روزرسانی بیشینه Y
        }

        int mapW = Math.max(100, maxX - minX);                  // عرض نقشه
        int mapH = Math.max(100, maxY - minY);                  // ارتفاع نقشه

        int viewW = Math.max(100, panel.getWidth());            // عرض نما
        int viewH = Math.max(100, panel.getHeight());           // ارتفاع نما

        double margin = 120.0;                                  // حاشیهٔ اطراف
        double sx = (viewW - margin) / (double) mapW;           // اسکیل پیشنهادی X
        double sy = (viewH - margin) / (double) mapH;           // اسکیل پیشنهادی Y
        double s  = Math.min(Math.min(sx, sy), 3.0);            // اسکیل نهایی با سقف

        if (s < 0.15) s = 0.15;                                 // کف اسکیل

        camera.setScale(s);                                     // اعمال زوم

        // محاسبهٔ آفست برای اینکه مرکز نقشه بیاید وسط پنجره
        double centerMapX = (minX + maxX) * 0.5;                // مرکز X نقشه
        double centerMapY = (minY + maxY) * 0.5;                // مرکز Y نقشه
        int centerViewX = viewW / 2;                            // مرکز X پنل
        int centerViewY = viewH / 2;                            // مرکز Y پنل

        int offX = (int) Math.round(centerViewX / s - centerMapX); // آفست X
        int offY = (int) Math.round(centerViewY / s - centerMapY); // آفست Y

        camera.setOffset(offX, offY);                           // ✅ تنظیم مطلق آفست
    }

    public static void main(String[] args){                     // ورود برنامه
        SwingUtilities.invokeLater(new Runnable(){              // اجرای در EDT
            @Override public void run(){ new MainWindow(); }    // ساخت پنجره
        });
    }
}

































//package ui; // // پکیج UI
//
//import simulation.World;                         // // دنیا
//import simulation.DemoMaps;                      // // ساخت نقشه
//import simulation.SimulationConfig;              // // ثابت‌ها
//import infrastructure.CityMap;                   // // مدل نقشهٔ شهر
//
//import javax.swing.*;                            // // سوئینگ
//import java.awt.*;                               // // چیدمان/رنگ
//import java.awt.event.*;                         // // رویدادها
//
//public class MainWindow extends JFrame {         // // پنجرهٔ اصلی
//    private final World world;                   // // مرجع دنیا
//    private final SimulatorPanel panel;          // // پنل رندر
//    private final Camera camera;                 // // دوربین
//    private final Timer repaintTimer;            // // تایمر فقط برای repaint
//    private int repaintMs = 16;                  // // 16ms برای ~60FPS
//
//    public MainWindow() {                        // // سازنده بدون ورودی
//        super("Traffic Simulator — Scaled Map"); // // عنوان
//
//        // 1) ساخت نقشهٔ کاملاً راست با نسبت‌ها (ابعاد × MAP_SCALE)
//        int[] colRatios = new int[]{8, 4, 14, 6, 10};     // // نسبت ستون‌ها
//        int[] rowRatios = new int[]{5, 12, 7, 9};         // // نسبت ردیف‌ها
//
//        // // ابعاد پایه (قبلی): 1400×1800 → حالا ضربدر SimulationConfig.MAP_SCALE در DemoMaps
//        CityMap map = DemoMaps.gridByRatios(              // // ساخت نقشه از نسبت‌ها
//                rowRatios, colRatios,                     // // نسبت‌ها
//                1400, 1800,                               // // اندازهٔ کل پایه (خود DemoMaps آن را × MAP_SCALE می‌کند)
//                2                                         // // لِین در هر جهت
//        );                                                // // خروجی نقشه
//
//        // 2) دنیا با همان نقشه
//        this.world = new World(map);                      // // ساخت دنیا
//
//        // 3) دوربین و پنل
//        this.camera = new Camera();                       // // دوربین
//        this.panel  = new SimulatorPanel(world, camera);  // // پنل رندر
//
//        // // تنظیمات اولیهٔ زوم و پن برای نقشهٔ بزرگ‌تر
//        camera.setScale(0.8);                             // // زوم اولیه
//        camera.pan(-350, -260);                           // // پن اولیه
//
//        // 4) تایمر برای repaint تنها (بدون منطق شبیه‌سازی داخل EDT)
//        this.repaintTimer = new Timer(repaintMs, new ActionListener(){ // // تایمر 16ms
//            @Override public void actionPerformed(ActionEvent e){      // // رویداد تیک
//                panel.repaint();                                       // // فقط رندر
//            }
//        });
//
//        // 5) نوار کنترل (برای مراحل بعد)
//        JComponent controls = buildControlBar();          // // ساخت کنترل‌ها
//
//        // 6) چیدمان پنجره
//        setLayout(new BorderLayout());                    // // چیدمان
//        add(panel, BorderLayout.CENTER);                  // // افزودن پنل
//        add(controls, BorderLayout.SOUTH);                // // افزودن کنترل
//        setSize(1280, 820);                               // // اندازه پنجره
//        setLocationRelativeTo(null);                      // // مرکز
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // // خروج
//        setVisible(true);                                 // // نمایش
//
//        // 7) شروع فقط repaint (نخ شبیه‌سازی مستقل در SimulationClock/World اجرا می‌شود)
//        repaintTimer.start();                             // // آغاز تایمر رندر
//        panel.requestFocusInWindow();                     // // فوکوس
//    }
//
//    private JComponent buildControlBar(){                 // // ساخت کنترل‌ها
//        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل
//        JButton btnStart = new JButton("Start");          // // شروع
//        JButton btnStop  = new JButton("Stop");           // // توقف
//        JButton btnFwd   = new JButton("Speed x2");       // // تندتر
//        JButton btnSlw   = new JButton("Speed /2");       // // کندتر
//        JButton btnAdd   = new JButton("Add Vehicle");    // // خودرو جدید
//        JButton btnZoomP = new JButton("Zoom+");          // // زوم+
//        JButton btnZoomM = new JButton("Zoom-");          // // زوم-
//
//        // // دکمه‌ها در این نسخه فقط نمایشی‌اند (منطقشان را قبلاً در UIController داشتی)
//        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ /* hook به UIController.start() در صورت نیاز */ }});
//        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ /* hook به UIController.stop()  در صورت نیاز */ }});
//        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ /* hook به UIController.speedUp2x() */ }});
//        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ /* hook به UIController.speedDown2x() */ }});
//        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ /* hook به UIController.addVehicle() */ }});
//        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ camera.setScale(camera.getScale()*1.10); panel.repaint(); }});
//        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ camera.setScale(camera.getScale()/1.10); panel.repaint(); }});
//
//        p.add(btnStart); p.add(btnStop); p.add(btnFwd); p.add(btnSlw); p.add(btnAdd); p.add(btnZoomP); p.add(btnZoomM); // // چیدمان
//        return p;                                           // // خروجی
//    }
//
//    public static void main(String[] args){                 // // ورود برنامه
//        SwingUtilities.invokeLater(new Runnable(){          // // اجرا در EDT
//            @Override public void run(){ new MainWindow(); } // // ساخت پنجره
//        });
//    }
//}
//





































//package ui; // // پکیج UI
//
//import simulation.World; // // دنیا
//import simulation.DemoMaps; // // ساخت نقشه
//import simulation.DemoTraffic; // // نصب چراغ/خودرو دمو
//import simulation.SimulationClock; // // ساعت
//import infrastructure.CityMap; // // نقشه
//import javax.swing.*; // // سوئینگ
//import java.awt.*; // // چیدمان
//import java.awt.event.*; // // رویدادها
//
///**
// * پنجره اصلی: تایمر Swing ۱۶ms فقط برای repaint(). //
// * شبیه‌سازی در نخِ جدا (SimulationClock). //
// */
//public class MainWindow extends JFrame { // // پنجره اصلی
//
//    private final World world; // // دنیا
//    private final SimulatorPanel panel; // // پنل رندر
//    private final Camera camera; // // دوربین
//    private final SimulationClock clock; // // ساعت شبیه‌سازی (نخ جدا)
//    private final UIController controller; // // کنترلر UI
//    private final Timer repaintTimer; // // تایمر ۱۶ms مخصوص repaint
//
//    public MainWindow() { // // سازنده
//        super("Traffic Simulator — Multithreaded Stage 1"); // // عنوان
//
//        // 1) ساخت نقشه شبکه‌ای (خیابان‌های راست) //
//        int[] colRatios = new int[]{8, 4, 14, 6, 10}; // // نسبت ستون‌ها
//        int[] rowRatios = new int[]{5, 12, 7, 9}; // // نسبت ردیف‌ها
//        CityMap map = DemoMaps.gridByRatios(rowRatios, colRatios, 1400, 1800, 2); // // تولید نقشه
//
//        // 2) دنیا و نصب چراغ‌ها //
//        this.world = new World(map); // // ساخت دنیا
//        DemoTraffic.installLights(world, map, 120, 100, 0); // // نصب چراغ‌ها (allRed=0 طبق قاعدهٔ جدید)
//
//        // 3) دوربین و پنل //
//        this.camera = new Camera(); // // ساخت دوربین
//        this.panel  = new SimulatorPanel(world, camera); // // ساخت پنل
//        camera.setScale(0.95); // // زوم اولیه
//        camera.pan(-280, -200); // // پن اولیه
//
//        // 4) ساعت چندنخی و کنترلر //
//        this.clock = new SimulationClock(world); // // ساخت ساعت
//        this.controller = new UIController(world, clock, camera); // // ساخت کنترلر
//
//        // 5) تایمر Swing فقط برای repaint (۱۶ms ≈ 60fps) //
//        this.repaintTimer = new Timer(16, new ActionListener(){ // // ساخت تایمر
//            @Override public void actionPerformed(ActionEvent e){ panel.repaint(); } // // فقط رندر
//        });
//
//        // 6) نوار کنترل //
//        JComponent controls = buildControlBar(); // // ساخت کنترل‌ها
//
//        // 7) چیدمان پنجره //
//        setLayout(new BorderLayout()); // // چیدمان
//        add(panel, BorderLayout.CENTER); // // افزودن پنل
//        add(controls, BorderLayout.SOUTH); // // افزودن کنترل‌ها
//        setSize(1280, 820); // // اندازه
//        setLocationRelativeTo(null); // // مرکز
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // // بستن برنامه
//        setVisible(true); // // نمایش
//
//        // 8) شروع repaint و چند خودروی اولیه (اختیاری) //
//        repaintTimer.start(); // // آغاز تایمر رندر
//        for (int i=0;i<12;i++){ DemoTraffic.addRandomVehicle(world, world.getMap()); } // // افزودن خودرو
//        panel.requestFocusInWindow(); // // فوکوس
//        // توجه: شبیه‌سازی واقعی با دکمه Start آغاز می‌شود (نخ جدا). //
//    }
//
//    private JComponent buildControlBar(){ // // ساخت نوار کنترل
//        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل
//        JButton btnStart = new JButton("Start"); // // شروع
//        JButton btnStop  = new JButton("Stop"); // // توقف
//        JButton btnFwd   = new JButton("Speed x2"); // // تندتر
//        JButton btnSlw   = new JButton("Speed /2"); // // کندتر
//        JButton btnAdd   = new JButton("Add Vehicle"); // // افزودن خودرو
//        JButton btnZoomP = new JButton("Zoom+"); // // زوم+
//        JButton btnZoomM = new JButton("Zoom-"); // // زوم-
//
//        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.start(); } }); // // شروع نخ
//        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.stop();  } }); // // توقف نخ
//        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.speedUp2x(); } }); // // سرعت×۲
//        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.speedDown2x(); } }); // // سرعت÷۲
//        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.addVehicle(); } }); // // افزودن خودرو
//        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomIn(); panel.repaint(); } }); // // زوم+
//        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); } }); // // زوم-
//
//        p.add(btnStart); p.add(btnStop); p.add(btnFwd); p.add(btnSlw); // // چیدمان
//        p.add(btnAdd); p.add(btnZoomP); p.add(btnZoomM); // // چیدمان
//        return p; // // خروجی
//    }
//
//    public static void main(String[] args){ // // نقطهٔ شروع
//        SwingUtilities.invokeLater(new Runnable(){ // // اجرای امن در EDT
//            @Override public void run(){ new MainWindow(); } // // ساخت پنجره
//        });
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
////import simulation.World;                         // // دنیا
////import simulation.DemoMaps;                      // // ساخت نقشه
////import simulation.DemoTraffic;                   // // نصب چراغ/افزودن خودرو
////import infrastructure.CityMap;                   // // نقشه شهر
////import javax.swing.*;                            // // سوئینگ
////import java.awt.*;                               // // چیدمان
////import java.awt.event.*;                         // // رویدادها
////
////public class MainWindow extends JFrame {         // // پنجره اصلی
////    private final World world;                   // // دنیا
////    private final SimulatorPanel panel;          // // پنل
////    private final Camera camera;                 // // دوربین
////    private final Timer tick;                    // // تایمر رندر/آپدیت
////    private int tickMs = 33;                     // // گام زمانی (ms)
////
////    public MainWindow() {                        // // سازنده
////        super("Traffic Simulator — Straight Roads"); // // عنوان
////
////        // 1) نقشه شبکه‌ای با نسبت‌ها (خیابان‌های 100% مستقیم) //
////        int[] colRatios = new int[]{8, 4, 14, 6, 10};       // // نسبت ستون‌ها
////        int[] rowRatios = new int[]{5, 12, 7, 9};           // // نسبت ردیف‌ها
////        CityMap map = DemoMaps.gridByRatios(                // // ساخت نقشه
////                rowRatios, colRatios,
////                1400, 1800,
////                2
////        );
////
////        // 2) ساخت دنیا //
////        this.world = new World(map);                        // // دنیا
////
////        // 3) نصب چراغ‌ها با زمان‌های کوچک برای تست سریع //
////        DemoTraffic.installLights(world, map, 1200, 1000, 1200); // // سبز=1.2s، زرد=0.3s، همه‌قرمز=1.1s
////
////        // 4) دوربین و پنل //
////        this.camera = new Camera();                         // // دوربین
////        this.panel  = new SimulatorPanel(world, camera);    // // پنل
////        camera.setScale(0.95);                              // // زوم اولیه
////        camera.pan(-280, -200);                             // // پن اولیه
////
////        // 5) تایمر Swing برای شبیه‌سازی (بدون لامبدا) //
////        this.tick = new Timer(tickMs, new ActionListener(){ // // تایمر
////            @Override public void actionPerformed(ActionEvent e){
////                world.update();              // // یک تیک شبیه‌سازی
////                panel.repaint();             // // بازترسیم
////            }
////        });
////
////        // 6) نوار کنترل //
////        JComponent controls = buildControlBar();            // // ساخت کنترل‌ها
////
////        // 7) چیدمان پنجره //
////        setLayout(new BorderLayout());                      // // چیدمان
////        add(panel, BorderLayout.CENTER);                    // // پنل
////        add(controls, BorderLayout.SOUTH);                  // // کنترل
////        setSize(1280, 820);                                 // // اندازه
////        setLocationRelativeTo(null);                        // // مرکز
////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     // // خروج
////        setVisible(true);                                   // // نمایش
////
////        // 8) شروع شبیه‌سازی و چند خودرو اولیه //
////        tick.start();                                       // // شروع
////        for (int i=0;i<12;i++){                             // // چند خودرو
////            DemoTraffic.addRandomVehicle(world, world.getMap());
////        }
////        panel.requestFocusInWindow();                       // // فوکوس
////    }
////
////    private JComponent buildControlBar(){                   // // ساخت کنترل‌ها
////        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل
////        JButton btnStart = new JButton("Start");            // // شروع
////        JButton btnStop  = new JButton("Stop");             // // توقف
////        JButton btnFwd   = new JButton("Speed x2");         // // تندتر
////        JButton btnSlw   = new JButton("Speed /2");         // // کندتر
////        JButton btnAdd   = new JButton("Add Vehicle");      // // خودرو جدید
////        JButton btnZoomP = new JButton("Zoom+");            // // زوم+
////        JButton btnZoomM = new JButton("Zoom-");            // // زوم-
////
////        btnStart.addActionListener(new ActionListener(){    // // استارت
////            @Override public void actionPerformed(ActionEvent e){
////                if(!tick.isRunning()) tick.start();        // // اگر متوقف بود، راه‌اندازی
////            }
////        });
////        btnStop .addActionListener(new ActionListener(){    // // توقف
////            @Override public void actionPerformed(ActionEvent e){
////                if( tick.isRunning()) tick.stop();         // // اگر در حال اجراست، متوقف کن
////            }
////        });
////        btnFwd  .addActionListener(new ActionListener(){    // // دو برابر سریع‌تر
////            @Override public void actionPerformed(ActionEvent e){
////                tickMs = Math.max(5, tickMs/2);            // // حداقل 5ms
////                tick.setDelay(tickMs);                     // // اعمال
////            }
////        });
////        btnSlw  .addActionListener(new ActionListener(){    // // نصف سرعت
////            @Override public void actionPerformed(ActionEvent e){
////                tickMs = Math.min(200, tickMs*2);          // // سقف 200ms
////                tick.setDelay(tickMs);                     // // اعمال
////            }
////        });
////        btnAdd  .addActionListener(new ActionListener(){    // // افزودن خودرو
////            @Override public void actionPerformed(ActionEvent e){
////                DemoTraffic.addRandomVehicle(world, world.getMap());
////            }
////        });
////        btnZoomP.addActionListener(new ActionListener(){    // // زوم+
////            @Override public void actionPerformed(ActionEvent e){
////                camera.setScale(camera.getScale()*1.10);
////                panel.repaint();
////            }
////        });
////        btnZoomM.addActionListener(new ActionListener(){    // // زوم-
////            @Override public void actionPerformed(ActionEvent e){
////                camera.setScale(camera.getScale()/1.10);
////                panel.repaint();
////            }
////        });
////
////        p.add(btnStart); p.add(btnStop); p.add(btnFwd); p.add(btnSlw);
////        p.add(btnAdd); p.add(btnZoomP); p.add(btnZoomM);    // // چیدمان
////        return p;                                           // // خروجی
////    }
////
////    public static void main(String[] args){
////        SwingUtilities.invokeLater(new Runnable(){          // // اجرای امن در EDT
////            @Override public void run(){ new MainWindow(); }
////        });
////    }
////}
////
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
//
//
//
//
//
//
//
//
////package ui;
////
////import simulation.*;
////import infrastructure.*;
////import javax.swing.*;
////import java.awt.*;
////import java.awt.event.*;
////
////// پنجره اصلی شبیه‌سازی
////public class MainWindow extends JFrame {
////    private final World world; // مرجع دنیا
////    private final SimulatorPanel panel; // پنل رندر
////    private final Camera camera; // دوربین
////    private final UIController controller; // کنترلر
////    private final Timer repaintTimer; // تایمر رندر
////
////    // سازنده
////    public MainWindow() {
////        super("Traffic Simulator — Straight Roads"); // عنوان
////        int[] colRatios = new int[]{8, 4, 14, 6, 10}; // نسبت ستون‌ها
////        int[] rowRatios = new int[]{5, 12, 7, 9}; // نسبت ردیف‌ها
////        CityMap map = DemoMaps.gridByRatios(rowRatios, colRatios, 1400, 1800, 2); // نقشه
////        this.world = new World(map); // ساخت دنیا
////
////        DemoTraffic.installLights(world, map, 12, 3, 11);
////
////
//////        DemoTraffic.installLights(world, map, 12, 3, 11); // نصب چراغ‌ها
////        this.camera = new Camera(); // دوربین
////        this.panel = new SimulatorPanel(world, camera); // پنل رندر
////        this.controller = new UIController(world, new SimulationClock(SimulationConfig.TICK_INTERVAL), camera); // کنترلر
////        camera.setScale(0.9); // زوم اولیه
////        camera.pan(-280, -200); // پن اولیه
////        this.repaintTimer = new Timer(16, new ActionListener() { // تایمر رندر 16ms
////            public void actionPerformed(ActionEvent e) {
////                panel.repaint(); // بازترسیم
////            }
////        });
////        JComponent controls = buildControlBar(); // نوار کنترل
////        setLayout(new BorderLayout()); // چیدمان
////        add(panel, BorderLayout.CENTER); // افزودن پنل
////        add(controls, BorderLayout.SOUTH); // افزودن کنترل
////        setSize(1280, 820); // اندازه
////        setLocationRelativeTo(null); // مرکز
////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // خروج
////        setVisible(true); // نمایش
////        controller.start(); // شروع شبیه‌سازی
////        repaintTimer.start(); // شروع رندر
////        for (int i = 0; i < 20; i++) { // 20 خودرو
////            DemoTraffic.addRandomVehicle(world, map); // افزودن خودرو
////        }
////        panel.requestFocusInWindow(); // فوکوس
////    }
////
////    // ساخت نوار کنترل
////    private JComponent buildControlBar() {
////        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); // پنل
////        JButton btnStart = new JButton("Start"); // دکمه شروع
////        JButton btnStop = new JButton("Stop"); // دکمه توقف
////        JButton btnFwd = new JButton("Speed x2"); // دکمه تندتر
////        JButton btnSlw = new JButton("Speed /2"); // دکمه کندتر
////        JButton btnAdd = new JButton("Add Vehicle"); // دکمه افزودن خودرو
////        JButton btnZoomP = new JButton("Zoom+"); // دکمه زوم+
////        JButton btnZoomM = new JButton("Zoom-"); // دکمه زوم-
////
////        btnStart.addActionListener(new ActionListener() { // لیسنر شروع
////            public void actionPerformed(ActionEvent e) {
////                controller.start(); // شروع شبیه‌سازی
////            }
////        });
////        btnStop.addActionListener(new ActionListener() { // لیسنر توقف
////            public void actionPerformed(ActionEvent e) {
////                controller.stop(); // توقف شبیه‌سازی
////            }
////        });
////        btnFwd.addActionListener(new ActionListener() { // لیسنر تندتر
////            public void actionPerformed(ActionEvent e) {
////                controller.changeSpeed(Math.max(5, SimulationConfig.TICK_INTERVAL / 2)); // افزایش سرعت
////            }
////        });
////        btnSlw.addActionListener(new ActionListener() { // لیسنر کندتر
////            public void actionPerformed(ActionEvent e) {
////                controller.changeSpeed(Math.min(200, SimulationConfig.TICK_INTERVAL * 2)); // کاهش سرعت
////            }
////        });
////        btnAdd.addActionListener(new ActionListener() { // لیسنر افزودن
////            public void actionPerformed(ActionEvent e) {
////                controller.addRandomVehicle(); // افزودن خودرو
////            }
////        });
////        btnZoomP.addActionListener(new ActionListener() { // لیسنر زوم+
////            public void actionPerformed(ActionEvent e) {
////                controller.zoomIn(); // زوم
////                panel.repaint(); // بازترسیم
////            }
////        });
////        btnZoomM.addActionListener(new ActionListener() { // لیسنر زوم-
////            public void actionPerformed(ActionEvent e) {
////                controller.zoomOut(); // کاهش زوم
////                panel.repaint(); // بازترسیم
////            }
////        });
////
////        p.add(btnStart); // افزودن دکمه
////        p.add(btnStop); // افزودن دکمه
////        p.add(btnFwd); // افزودن دکمه
////        p.add(btnSlw); // افزودن دکمه
////        p.add(btnAdd); // افزودن دکمه
////        p.add(btnZoomP); // افزودن دکمه
////        p.add(btnZoomM); // افزودن دکمه
////        return p; // خروجی پنل
////    }
////
////    // نقطه ورود برنامه
////    public static void main(String[] args) {
////        SwingUtilities.invokeLater(new Runnable() { // اجرا در EDT
////            public void run() {
////                new MainWindow(); // ساخت پنجره
////            }
////        });
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
//////package ui; // // پکیج UI
//////
//////import simulation.World;                         // // دنیا
//////import simulation.DemoMaps;                      // // ساخت نقشه با نسبت‌ها
//////import simulation.DemoTraffic;                   // // نصب چراغ و افزودن خودرو
//////import infrastructure.CityMap;                   // // مدل نقشهٔ شهر
//////import javax.swing.*;                            // // سوئینگ
//////import java.awt.*;                               // // چیدمان/رنگ
//////import java.awt.event.*;                         // // رویدادها
//////
//////public class MainWindow extends JFrame {         // // پنجرهٔ اصلی
//////    private final World world;                   // // مرجع دنیا
//////    private final SimulatorPanel panel;          // // پنل رندر
//////    private final Camera camera;                 // // دوربین
//////    private final Timer tick;                    // // تایمر شبیه‌سازی
//////    private int tickMs = 33;                     // // گام زمانی (ms)
//////
//////    public MainWindow() {                        // // سازندهٔ بدون ورودی
//////        super("Traffic Simulator — Straight Roads"); // // عنوان
//////
//////        // 1) ساخت نقشهٔ کاملاً راست با نسبت‌های متفاوت بلوک‌ها //
//////        int[] colRatios = new int[]{8, 4, 14, 6, 10};       // // نسبت ستون‌ها
//////        int[] rowRatios = new int[]{5, 12, 7, 9};           // // نسبت ردیف‌ها
//////        CityMap map = DemoMaps.gridByRatios(                // // نقشه از نسبت‌ها
//////                rowRatios, colRatios,                       // // نسبت‌ها
//////                1400, 1800,                                 // // اندازهٔ کل شبکه
//////                2                                           // // لِین در هر جهت
//////        );                                                  // // خروجی نقشه
//////
//////        // 2) دنیا با همان نقشه //
//////        this.world = new World(map);                        // // ساخت دنیا
//////
//////        // 3) چراغ‌های راهنما (در گوشهٔ رویکرد) //
//////        DemoTraffic.installLights(world, map, 12000, 3000, 11000); // // G/Y/R
//////
//////        // 4) دوربین و پنل //
//////        this.camera = new Camera();                         // // دوربین
//////        this.panel  = new SimulatorPanel(world, camera);    // // پنل رندر ← دقت کن: دو ورودی دارد
//////        camera.setScale(0.9);                               // // زوم اولیه
//////        camera.pan(-280, -200);                             // // پن اولیه
//////
//////        // 5) تایمر (Swing) — بدون لامبدا //
//////        this.tick = new Timer(tickMs, new ActionListener(){ // // ساخت تایمر
//////            @Override public void actionPerformed(ActionEvent e){
//////                world.update();              // // آپدیت دنیا
//////                panel.repaint();                            // // رندر
//////            }
//////        });
//////
//////        // 6) نوار کنترل //
//////        JComponent controls = buildControlBar();            // // ساخت کنترل‌ها
//////
//////        // 7) چیدمان پنجره //
//////        setLayout(new BorderLayout());                      // // چیدمان
//////        add(panel, BorderLayout.CENTER);                    // // افزودن پنل
//////        add(controls, BorderLayout.SOUTH);                  // // افزودن کنترل
//////        setSize(1280, 820);                                 // // اندازه
//////        setLocationRelativeTo(null);                        // // مرکز
//////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     // // خروج
//////        setVisible(true);                                   // // نمایش
//////
//////        // 8) شروع شبیه‌سازی و چند خودرو اولیه //
//////        tick.start();                                       // // شروع تایمر (بدون ورودی!)
//////        int i; for (i=0;i<20;i++){                          // // ۲۰ خودرو
//////            DemoTraffic.addRandomVehicle(world, world.getMap()); // // افزودن
//////        }
//////        panel.requestFocusInWindow();                       // // فوکوس
//////    }
//////
//////    private JComponent buildControlBar(){                   // // ساخت کنترل‌ها
//////        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل
//////        JButton btnStart = new JButton("Start");            // // شروع
//////        JButton btnStop  = new JButton("Stop");             // // توقف
//////        JButton btnFwd   = new JButton("Speed x2");         // // تندتر
//////        JButton btnSlw   = new JButton("Speed /2");         // // کندتر
//////        JButton btnAdd   = new JButton("Add Vehicle");      // // خودرو جدید
//////        JButton btnZoomP = new JButton("Zoom+");            // // زوم+
//////        JButton btnZoomM = new JButton("Zoom-");            // // زوم-
//////
//////        // لیسنرها — بدون لامبدا //
//////        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ if(!tick.isRunning()) tick.start(); }});
//////        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ if( tick.isRunning()) tick.stop();  }});
//////        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ tickMs = Math.max(5, tickMs/2);  tick.setDelay(tickMs); }});
//////        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ tickMs = Math.min(200, tickMs*2); tick.setDelay(tickMs); }});
//////        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ DemoTraffic.addRandomVehicle(world, world.getMap()); }});
//////        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ camera.setScale(camera.getScale()*1.10); panel.repaint(); }});
//////        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ camera.setScale(camera.getScale()/1.10); panel.repaint(); }});
//////
//////        p.add(btnStart); p.add(btnStop); p.add(btnFwd); p.add(btnSlw); p.add(btnAdd); p.add(btnZoomP); p.add(btnZoomM); // // چیدمان
//////        return p;                                           // // خروجی
//////    }
//////
//////    public static void main(String[] args){                 // // ورود برنامه
//////        SwingUtilities.invokeLater(new Runnable(){          // // اجرا در EDT
//////            @Override public void run(){ new MainWindow(); } // // ساخت پنجره
//////        });
//////    }
//////}
//////
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
//////package ui; // // پکیج UI
//////
//////import simulation.*;            // // World/Clock/Config/DemoTraffic/DemoMaps
//////import infrastructure.*;        // // CityMap/Road/Intersection
//////import core.*;                  // // Direction/Point
//////import pedestrian.*;            // // گذرگاه
//////import javax.swing.*;           // // سوئینگ
//////import java.awt.*;              // // چیدمان
//////import java.awt.event.*;        // // رویداد
//////import java.util.List;          // // لیست
//////
//////public class MainWindow extends JFrame { // // پنجره اصلی
//////    private final World world;           // // دنیا
//////    private final SimulationClock clock; // // ساعت شبیه‌سازی
//////    private final UIController controller; // // کنترلر
//////    private final Camera camera;         // // دوربین
//////    private final SimulatorPanel panel;  // // پنل رندر
//////
//////    public MainWindow(){                 // // سازنده
//////        super("Traffic Simulator — Swing"); // // عنوان
//////
//////        // 1) ساخت نقشه با بلوک‌های «بلندتر» و «عریض‌تر»
//////        CityMap map = DemoMaps.variableGrid( // // تولید گرید نامنظم
//////                8, 8,           // // rows, cols
//////                300, 520,       // // minBlockW, maxBlockW  ← عرض بلوک‌ها بیشتر شد
//////                260, 420,       // // minBlockH, maxBlockH  ← ارتفاع بلوک‌ها بیشتر شد
//////                2               // // lanesPerDir
//////        );
//////
//////        // 2) اعمال قوس نرم Bezier روی بخشی از خیابان‌ها (بدون لامبدا)
//////        java.util.Random rnd = new java.util.Random(); // // رندوم
//////        List<Road> roads = map.getRoads();             // // همه راه‌ها
//////        int i;                                         // // شمارنده
//////        for (i=0;i<roads.size();i++){                  // // حلقه
//////            Road r = roads.get(i);                     // // راه
//////            if (rnd.nextDouble() < 0.35){              // // ۳۵٪ راه‌ها خمیده شوند
//////                core.Point A = r.getStartIntersection().getPosition(); // // A
//////                core.Point B = r.getEndIntersection().getPosition();   // // B
//////                double mx = (A.getX()+B.getX())*0.5;   // // X میانه
//////                double my = (A.getY()+B.getY())*0.5;   // // Y میانه
//////                double dx = B.getX()-A.getX(), dy = B.getY()-A.getY(); // // Δ
//////                double len = Math.hypot(dx,dy);        // // طول
//////                if (len < 1e-6) len = 1;               // // جلوگیری از صفر
//////                double nx = -dy/len, ny = dx/len;      // // نرمال چپ
//////                double bend = Math.min(90, len*0.24);  // // شدّت قوس
//////                if (rnd.nextBoolean()) bend = -bend;   // // جهت قوس
//////                core.Point C = new core.Point(         // // نقطه کنترل
//////                        (int)Math.round(mx + nx*bend),
//////                        (int)Math.round(my + ny*bend)
//////                );
//////                r.setQuadraticControl(C);              // // اعمال قوس
//////            }
//////        }
//////
//////        // 3) ساخت دنیا/ساعت/دوربین/کنترلر
//////        this.world = new World(map);                   // // دنیا
//////        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // // ساعت
//////        this.camera = new Camera();                    // // دوربین
//////        this.controller = new UIController(world, clock, camera); // // کنترلر
//////
//////        // 4) نصب چراغ‌ها (مقادیر نمونه—در صورت بلند بودن تیک، کوچک‌تر کن)
//////        DemoTraffic.installLights(world, map, 12000, 3000, 11000); // // چراغ‌ها
//////
//////        // 5) چند گذرگاه نمونه (فقط برای تست)
//////        for (i=0;i<map.getIntersections().size();i++){             // // حلقه
//////            Intersection it = map.getIntersections().get(i);       // // تقاطع
//////            world.addCrossing(new PedestrianCrossing("PX-"+i+"-N", it, Direction.NORTH, true)); // // گذرگاه شمال
//////            world.addCrossing(new PedestrianCrossing("PX-"+i+"-E", it, Direction.EAST,  true)); // // گذرگاه شرق
//////        }
//////
//////        // 6) پنل رندر و تنظیمات
//////        this.panel = new SimulatorPanel(world, camera);            // // پنل
//////        panel.loadAssets("assets/car1.png");                       // // تصویر خودرو
//////        camera.setScale(0.8);                                      // // زوم اولیه
//////        camera.pan(-240, -160);                                    // // پن اولیه
//////
//////        setLayout(new BorderLayout());                             // // چیدمان
//////        add(panel, BorderLayout.CENTER);                           // // افزودن پنل
//////        add(buildControlBar(), BorderLayout.SOUTH);                // // نوار کنترل
//////
//////        InputHandler input = new InputHandler(controller, panel);  // // ورودی صفحه‌کلید
//////        panel.addKeyListener(input);                               // // افزودن لیسنر
//////        panel.setFocusable(true); panel.requestFocusInWindow();    // // فوکوس
//////
//////        pack(); setSize(1280, 800); setLocationRelativeTo(null);   // // اندازه و موقعیت
//////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setVisible(true); // // نمایش
//////
//////        world.setDtSeconds(SimulationConfig.TICK_INTERVAL/1000.0); // // همگام‌سازی dt
//////        clock.register(world); clock.start();                      // // شروع تیک
//////    }
//////
//////    private JComponent buildControlBar(){                          // // نوار کنترل پایین
//////        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));  // // پنل
//////        JButton btnStart = new JButton("Start");                   // // شروع
//////        JButton btnStop  = new JButton("Stop");                    // // توقف
//////        JButton btnFwd   = new JButton("Speed x2");                // // سریع‌تر
//////        JButton btnSlw   = new JButton("Speed /2");                // // کندتر
//////        JButton btnAdd   = new JButton("Add Vehicle");             // // افزودن خودرو
//////        JButton btnZoomP = new JButton("Zoom+");                   // // زوم +
//////        JButton btnZoomM = new JButton("Zoom-");                   // // زوم -
//////
//////        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.start(); }}); // // اکشن
//////        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.stop();  }}); // // اکشن
//////        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(Math.max(10, clock.getInterval()/2)); }}); // // اکشن
//////        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(clock.getInterval()*2); }}); // // اکشن
//////        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.addRandomVehicle(); }}); // // اکشن
//////        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomIn();  panel.repaint(); }}); // // اکشن
//////        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); }}); // // اکشن
//////
//////        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // // چیدمان
//////        return bar; // // خروجی
//////    }
//////
//////    public static void main(String[] args){                        // // ورود
//////        SwingUtilities.invokeLater(new Runnable(){                 // // اجرای UI روی EDT
//////            @Override public void run(){ new MainWindow(); }       // // ساخت پنجره
//////        });
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
//////
//////
//////
//////
////////package ui; // // پکیج UI
////////
////////import simulation.*;            // // World/Clock/Config/DemoTraffic/DemoMaps
////////import infrastructure.*;        // // CityMap/Road/Intersection
////////import core.*;                  // // Direction/Point
////////import pedestrian.*;            // // PedestrianCrossing
////////import javax.swing.*;           // // Swing
////////import java.awt.*;              // // Layout
////////import java.awt.event.*;        // // رویداد
////////import java.util.List;          // // لیست
////////
////////public class MainWindow extends JFrame { // // پنجره اصلی
////////    private final World world;           // // دنیا
////////    private final SimulationClock clock; // // ساعت
////////    private final UIController controller;// // کنترلر
////////    private final Camera camera;         // // دوربین
////////    private final SimulatorPanel panel;  // // پنل
////////
////////    public MainWindow(){ // // سازنده
////////        super("Traffic Simulator (Curved Roads, Pure Swing)"); // // عنوان
////////
////////        // نقشهٔ گرید با بلوک‌های طول/عرض مختلف (ساده و مستقیم)
////////        CityMap map = DemoMaps.variableGrid(8, 8, 180, 320, 140, 260, 2); // // گرید نامنظم
////////
////////        // روی بخشی از جاده‌ها قوس نرم بزن (Bezier درجه۲) — بدون لامبدا
////////        java.util.Random rnd = new java.util.Random();                   // // رندوم
////////        List<Road> roads = map.getRoads();                               // // همهٔ راه‌ها
////////        for (int i=0;i<roads.size();i++){                                // // حلقه
////////            Road r = roads.get(i);                                       // // راه
////////            if (rnd.nextDouble() < 0.35){                                // // احتمال ۳۵٪
////////                core.Point A = r.getStartIntersection().getPosition();   // // A
////////                core.Point B = r.getEndIntersection().getPosition();     // // B
////////                double mx = (A.getX()+B.getX())*0.5;                      // // X میانه
////////                double my = (A.getY()+B.getY())*0.5;                      // // Y میانه
////////                double dx = B.getX()-A.getX(), dy = B.getY()-A.getY();    // // Δ
////////                double len=Math.hypot(dx,dy); if (len<1e-6) len=1;        // // طول
////////                double nx = -dy/len, ny = dx/len;                         // // نرمال چپ
////////                double bend = Math.min(70, len*0.22);                     // // شدت قوس
////////                if (rnd.nextBoolean()) bend = -bend;                      // // جهت قوس
////////                core.Point C = new core.Point((int)Math.round(mx + nx*bend), (int)Math.round(my + ny*bend)); // // کنترل
////////                r.setQuadraticControl(C);                                 // // اعمال خم
////////            }
////////        }
////////
////////        this.world = new World(map);                                      // // دنیا
////////        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // // ساعت
////////        this.camera = new Camera();                                       // // دوربین
////////        this.controller = new UIController(world, clock, camera);         // // کنترلر
////////
////////        DemoTraffic.installLights(world, map, 3500, 700, 3000);           // // چراغ‌ها
////////        DemoTraffic.seedVehicles(world, map, clock, 14);                  // // چند خودرو
////////
////////        for (int i=0;i<map.getIntersections().size();i++){                // // چند گذرگاه نمونه
////////            Intersection it = map.getIntersections().get(i);              // // تقاطع
////////            world.addCrossing(new PedestrianCrossing("PX-"+i+"-N", it, Direction.NORTH, true)); // // گذرگاه
////////            world.addCrossing(new PedestrianCrossing("PX-"+i+"-E", it, Direction.EAST,  true)); // // گذرگاه
////////        }
////////
////////        this.panel = new SimulatorPanel(world, camera);                    // // پنل
////////        panel.loadAssets("assets/car1.png");                               // // تصویر خودرو
////////
////////        camera.setScale(0.8);                                              // // زوم اولیه
////////        camera.pan(-200, -120);                                            // // پن اولیه
////////
////////        setLayout(new BorderLayout());                                     // // چیدمان
////////        add(panel, BorderLayout.CENTER);                                    // // افزودن پنل
////////        add(buildControlBar(), BorderLayout.SOUTH);                         // // نوار کنترل
////////
////////        InputHandler input = new InputHandler(controller, panel);           // // ورودی
////////        panel.addKeyListener(input); panel.setFocusable(true); panel.requestFocusInWindow(); // // فوکوس
////////
////////        pack(); setSize(1280, 800); setLocationRelativeTo(null);           // // اندازه و موقعیت
////////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setVisible(true);   // // نمایش
////////
////////        world.setDtSeconds(SimulationConfig.TICK_INTERVAL/1000.0);         // // همگام‌سازی dt
////////        clock.register(world); clock.start();                               // // شروع تیک
////////    }
////////
////////    private JComponent buildControlBar(){                                  // // نوار کنترل
////////        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));          // // پنل
////////        JButton btnStart=new JButton("Start");                             // // شروع
////////        JButton btnStop =new JButton("Stop");                              // // توقف
////////        JButton btnFwd  =new JButton("Speed x2");                          // // تندتر
////////        JButton btnSlw  =new JButton("Speed /2");                          // // کندتر
////////        JButton btnAdd  =new JButton("Add Vehicle");                       // // افزودن خودرو
////////        JButton btnZoomP=new JButton("Zoom+");                             // // زوم+
////////        JButton btnZoomM=new JButton("Zoom-");                             // // زوم-
////////
////////        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.start(); }}); // // اکشن
////////        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.stop();  }}); // // اکشن
////////        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(Math.max(10, clock.getInterval()/2)); }}); // // اکشن
////////        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(clock.getInterval()*2); }}); // // اکشن
////////        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.addRandomVehicle(); }}); // // اکشن
////////        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomIn(); panel.repaint(); }}); // // اکشن
////////        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); }}); // // اکشن
////////
////////        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // // چیدمان
////////        return bar; // // خروجی
////////    }
////////
////////    public static void main(String[] args){ SwingUtilities.invokeLater(new Runnable(){ @Override public void run(){ new MainWindow(); }}); } // // ورود (بدون لامبدا)
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
////////package ui; // // پکیج UI
////////
////////import simulation.*; // // World/Clock/Config/DemoTraffic/DemoMaps/UIController/Camera
////////import infrastructure.*; // // CityMap/Road/Intersection
////////import core.*; // // Direction/Point
////////import pedestrian.*; // // Crossing/Pedestrian
////////import javax.swing.*; // // Swing
////////import java.awt.*; // // Layout
////////import java.awt.event.*; // // رویدادها
////////import java.util.List; // // لیست
////////
////////public class MainWindow extends JFrame { // // پنجره اصلی
////////    private final World world; // // دنیا
////////    private final SimulationClock clock; // // ساعت
////////    private final UIController controller; // // کنترلر
////////    private final Camera camera; // // دوربین
////////    private final SimulatorPanel panel; // // پنل
////////
////////    public MainWindow() { // // سازنده
////////        super("Traffic Simulator (Pure Swing)"); // // عنوان
////////
////////        CityMap map = DemoMaps.irregularGrid(8, 8, 220, 320, 180, 180); // // نقشهٔ بزرگ و نامنظم
////////        this.world = new World(map); // // ساخت دنیا
////////        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // // ساعت
////////        this.camera = new Camera(); // // دوربین
////////        this.controller = new UIController(world, clock, camera); // // کنترلر
////////
////////        DemoTraffic.installLights(world, map, 3500, 700, 3000); // // نصب چراغ‌ها
////////        DemoTraffic.seedVehicles(world, map, clock, 14); // // کاشت خودروها
////////
////////        for (int i = 0; i < map.getIntersections().size(); i++) { // // گذرگاه‌های نمونه
////////            Intersection it = map.getIntersections().get(i); // // تقاطع
////////            world.addCrossing(new PedestrianCrossing("PX-" + i + "-N", it, Direction.NORTH, true)); // // گذرگاه
////////            world.addCrossing(new PedestrianCrossing("PX-" + i + "-E", it, Direction.EAST,  true)); // // گذرگاه
////////        }
////////
////////        // ===== قوس نرم خودکار روی بخشی از خیابان‌ها (مثل عکس) =====
////////        java.util.Random _rnd = new java.util.Random(); // // رندوم
////////        List<Road> _roads = world.getMap().getRoads(); // // همهٔ جاده‌ها
////////        for (int i = 0; i < _roads.size(); i++) { // // حلقه
////////            Road r = _roads.get(i); // // جاده
////////            core.Point A = r.getStartIntersection().getPosition(); // // A
////////            core.Point B = r.getEndIntersection().getPosition(); // // B
////////            if (_rnd.nextDouble() < 0.35) { // // با احتمال ۳۵٪
////////                double mx = (A.getX() + B.getX()) * 0.5; // // X میانه
////////                double my = (A.getY() + B.getY()) * 0.5; // // Y میانه
////////                double dx = B.getX() - A.getX(); // // Δx
////////                double dy = B.getY() - A.getY(); // // Δy
////////                double len = Math.hypot(dx, dy); if (len < 1e-6) len = 1; // // طول
////////                double nx = -dy / len, ny = dx / len; // // نرمال چپ
////////                double bend = Math.min(70, len * 0.22); // // شدت قوس
////////                if (_rnd.nextBoolean()) bend = -bend; // // جهت قوس
////////                core.Point C = new core.Point((int)Math.round(mx + nx * bend), (int)Math.round(my + ny * bend)); // // کنترل
////////                r.setQuadraticControl(C); // // اعمال قوس
////////            }
////////        }
////////
////////        this.panel = new SimulatorPanel(world, camera); // // پنل رسم
////////        panel.loadAssets("assets/car1.png"); // // تصویر خودرو (اختیاری)
////////
////////        camera.setScale(0.8); // // زوم اولیه
////////        camera.pan(-200, -120); // // پن اولیه
////////
////////        setLayout(new BorderLayout()); // // چیدمان
////////        add(panel, BorderLayout.CENTER); // // افزودن پنل
////////        add(buildControlBar(), BorderLayout.SOUTH); // // نوار کنترل
////////
////////        InputHandler input = new InputHandler(controller, panel); // // ورودی
////////        panel.addKeyListener(input); // // اتصال
////////        panel.setFocusable(true); // // فوکوس‌پذیر
////////        panel.requestFocusInWindow(); // // درخواست فوکوس
////////
////////        pack(); // // اندازه‌گذاری
////////        setSize(1280, 800); // // اندازه پنجره
////////        setLocationRelativeTo(null); // // وسط صفحه
////////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // // خروج
////////        setVisible(true); // // نمایش
////////
////////        world.setDtSeconds(SimulationConfig.TICK_INTERVAL / 1000.0); // // همگام‌سازی dt
////////        clock.register(world); // // ثبت دنیا
////////        clock.start(); // // شروع
////////    }
////////
////////    private JComponent buildControlBar() { // // نوار کنترل
////////        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل
////////
////////        JButton btnStart = new JButton("Start"); // // شروع
////////        JButton btnStop  = new JButton("Stop"); // // توقف
////////        JButton btnFwd   = new JButton("Speed x2"); // // تندتر
////////        JButton btnSlw   = new JButton("Speed /2"); // // کندتر
////////        JButton btnAdd   = new JButton("Add Vehicle"); // // افزودن خودرو
////////        JButton btnZoomP = new JButton("Zoom+"); // // زوم+
////////        JButton btnZoomM = new JButton("Zoom-"); // // زوم-
////////
////////        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.start(); }}); // // اکشن
////////        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.stop();  }}); // // اکشن
////////        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(Math.max(10, clock.getInterval()/2)); }}); // // اکشن
////////        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(clock.getInterval()*2); }}); // // اکشن
////////        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.addRandomVehicle(); }}); // // اکشن
////////        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomIn(); panel.repaint(); }}); // // اکشن
////////        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); }}); // // اکشن
////////
////////        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // // چیدمان
////////        return bar; // // خروجی
////////    }
////////
////////    public static void main(String[] args) { // // ورود
////////        SwingUtilities.invokeLater(new Runnable(){ @Override public void run(){ new MainWindow(); }}); // // اجرای روی EDT
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
//////
//////
////////9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
////////package ui; // // پکیج UI
////////
////////import simulation.*;            // // World / SimulationClock / SimulationConfig / DemoTraffic / DemoMaps
////////import infrastructure.*;        // // CityMap / Intersection / Road / Lane
////////import core.*;                  // // Direction / Point
////////import pedestrian.*;            // // Pedestrian / PedestrianCrossing
////////
////////import javax.swing.*;           // // اجزای Swing
////////import java.awt.*;              // // Layout و گرافیک
////////import java.awt.event.*;        // // لیسنرها
////////
////////public class MainWindow extends JFrame { // // پنجرهٔ اصلی برنامه
////////    private final World world;          // // دنیای شبیه‌سازی
////////    private final SimulationClock clock;// // ساعت شبیه‌سازی
////////    private final UIController controller; // // کنترلر UI
////////    private final Camera camera;        // // دوربین (زوم/پن)
////////    private final SimulatorPanel panel; // // پنل رسم
////////
////////    public MainWindow() { // // سازندهٔ پنجره
////////        super("Traffic Simulator (Pure Swing)"); // // عنوان پنجره
////////
////////        CityMap map = DemoMaps.irregularGrid(7, 7, 90, 170, 120, 120); // // تولید نقشهٔ نامنظم برای تست
////////        this.world = new World(map);                                  // // ایجاد دنیا
////////        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // // ساخت ساعت با تیک پیش‌فرض
////////        this.camera = new Camera();                                    // // ایجاد دوربین
////////        this.controller = new UIController(world, clock, camera);      // // ساخت کنترلر
////////
////////        // نصب چراغ‌ها برای هر تقاطع و هر جهت:
////////        // توجه: نسخهٔ فعلی DemoTraffic.installLights فقط 5 آرگومان می‌پذیرد (green,yellow,red)
////////        DemoTraffic.installLights(world, map, 3500, 700, 3000); // // CHANGED: حذف آرگومان ششمِ tickInterval
////////
////////        // کاشت چند خودروی اولیه روی لِین‌های تصادفی
////////        DemoTraffic.seedVehicles(world, map, clock, 12); // // افزودن ۱۲ خودرو برای شروع
////////
////////        // چند گذرگاهِ نمونه برای نمایش عابر (اختیاری)
////////        for (int i = 0; i < map.getIntersections().size(); i++) {            // // پیمایش تقاطع‌ها
////////            Intersection it = map.getIntersections().get(i);                 // // گرفتن تقاطع
////////            world.addCrossing(new PedestrianCrossing("PX-" + i + "-N", it, Direction.NORTH, true)); // // گذرگاه شمال
////////            world.addCrossing(new PedestrianCrossing("PX-" + i + "-E", it, Direction.EAST,  true)); // // گذرگاه شرق
////////        }
////////
////////        this.panel = new SimulatorPanel(world, camera); // // ساخت پنل رسم
////////        panel.loadAssets("assets/car1.png");            // // بارگذاری تصویر خودرو (در صورت وجود)
////////
////////        setLayout(new BorderLayout());                  // // چیدمان کلی
////////        add(panel, BorderLayout.CENTER);                // // افزودن پنل به مرکز
////////        add(buildControlBar(), BorderLayout.SOUTH);     // // نوار کنترل پایین
////////
////////        InputHandler input = new InputHandler(controller, panel); // // ساخت ورودی کیبورد/ماوس
////////        panel.addKeyListener(input);                                  // // اتصال لیسنر کیبورد
////////        panel.setFocusable(true);                                     // // فوکوس‌پذیر کردن پنل
////////        panel.requestFocusInWindow();                                 // // درخواست فوکوس
////////
////////        pack();                                // // محاسبهٔ اندازهٔ مناسب
////////        setSize(1100, 700);                    // // اندازهٔ اولیهٔ پنجره
////////        setLocationRelativeTo(null);           // // وسط صفحه
////////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // // بستن برنامه با بستن پنجره
////////        setVisible(true);                      // // نمایش پنجره
////////
////////        world.setDtSeconds(SimulationConfig.TICK_INTERVAL / 1000.0); // // هماهنگ‌سازی dt با ساعت
////////        clock.register(world);                                       // // ثبت دنیا در ساعت
////////        clock.start();                                               // // شروع تیک‌ها
////////    }
////////
////////    private JComponent buildControlBar() { // // ساخت نوار کنترل پایین
////////        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل با چیدمان سمت چپ
////////
////////        JButton btnStart = new JButton("Start");      // // شروع
////////        JButton btnStop  = new JButton("Stop");       // // توقف
////////        JButton btnFwd   = new JButton("Speed x2");   // // تندتر
////////        JButton btnSlw   = new JButton("Speed /2");   // // کندتر
////////        JButton btnAdd   = new JButton("Add Vehicle");// // افزودن خودرو
////////        JButton btnZoomP = new JButton("Zoom+");      // // زوم +
////////        JButton btnZoomM = new JButton("Zoom-");      // // زوم -
////////
////////        // بدون لامبدا — به صورت کلاس بی‌نام:
////////        btnStart.addActionListener(new ActionListener() { // // کلیک Start
////////            @Override public void actionPerformed(ActionEvent e) { controller.start(); } // // شروع ساعت
////////        });
////////        btnStop.addActionListener(new ActionListener() {  // // کلیک Stop
////////            @Override public void actionPerformed(ActionEvent e) { controller.stop(); }  // // توقف ساعت
////////        });
////////        btnFwd.addActionListener(new ActionListener() {   // // کلیک Speed x2
////////            @Override public void actionPerformed(ActionEvent e) {
////////                int half = Math.max(10, clock.getInterval() / 2); // // نصف کردن بازهٔ تیک
////////                controller.changeSpeed(half);                      // // اعمال
////////            }
////////        });
////////        btnSlw.addActionListener(new ActionListener() {   // // کلیک Speed /2
////////            @Override public void actionPerformed(ActionEvent e) {
////////                controller.changeSpeed(clock.getInterval() * 2);  // // دو برابر کردن بازهٔ تیک
////////            }
////////        });
////////        btnAdd.addActionListener(new ActionListener() {   // // کلیک Add Vehicle
////////            @Override public void actionPerformed(ActionEvent e) { controller.addRandomVehicle(); } // // افزودن خودرو
////////        });
////////        btnZoomP.addActionListener(new ActionListener() { // // کلیک Zoom+
////////            @Override public void actionPerformed(ActionEvent e) { controller.zoomIn(); panel.repaint(); } // // زوم به داخل
////////        });
////////        btnZoomM.addActionListener(new ActionListener() { // // کلیک Zoom-
////////            @Override public void actionPerformed(ActionEvent e) { controller.zoomOut(); panel.repaint(); } // // زوم به بیرون
////////        });
////////
////////        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // // چیدمان دکمه‌ها
////////        return bar; // // برگرداندن نوار
////////    }
////////
////////    public static void main(String[] args) { // // نقطهٔ ورود
////////        SwingUtilities.invokeLater(new Runnable() { // // اجرای UI روی EDT (بدون لامبدا)
////////            @Override public void run() { new MainWindow(); } // // ساخت و نمایش پنجره
////////        });
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
//////////7777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
//////////
//////////package ui; // پکیج UI //
//////////
//////////import simulation.*;           // World, SimulationClock, SimulationConfig, DemoTraffic, DemoMaps //
//////////import infrastructure.*;       // CityMap //
//////////import javax.swing.*;          // Swing //
//////////import java.awt.*;             // Layout //
//////////import java.awt.event.*;       // Listeners //
//////////
//////////public class MainWindow extends JFrame { // پنجره اصلی //
//////////    private final World world;           // دنیا //
//////////    private final SimulationClock clock; // ساعت //
//////////    private final UIController controller; // کنترلر //
//////////    private final Camera camera;         // دوربین //
//////////    private final SimulatorPanel panel;  // پنل رسم //
//////////
//////////    public MainWindow() { // سازنده //
//////////        super("Traffic Simulator"); // عنوان //
//////////
//////////        CityMap map = DemoMaps.irregularGrid(7, 7, 90, 170, 120, 120); // ساخت نقشه آزمایشی //
//////////        this.world = new World(map); // ✅ فیکس: World(map) چون سازنده‌اش CityMap می‌گیرد //
//////////        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // ساعت //
//////////        this.camera = new Camera(); // دوربین //
//////////        this.controller = new UIController(world, clock, camera); // کنترلر //
//////////
//////////        DemoTraffic.setup(world, map, clock); // چراغ‌ها + ۷۰ خودرو + عابر //
//////////
//////////        this.panel = new SimulatorPanel(world, camera); // ✅ فیکس: سازنده‌ی پنل (world, camera) //
//////////        // اگر متد loadAssets داری، بازش کن. اگر نداری، همین‌طور بماند. //
//////////        // panel.loadAssets("assets/car1.png"); //
//////////
//////////        setLayout(new BorderLayout()); // Layout //
//////////        add(panel, BorderLayout.CENTER); // پنل وسط //
//////////        add(buildControlBar(), BorderLayout.SOUTH); // کنترل‌ها //
//////////
//////////        InputHandler input = new InputHandler(controller, panel); // ورودی //
//////////        panel.addKeyListener(input); // لیسنر //
//////////        panel.setFocusable(true); // فوکوس //
//////////
//////////        pack(); // اندازه //
//////////        setSize(1100, 700); // سایز //
//////////        setLocationRelativeTo(null); // مرکز //
//////////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // خروج //
//////////        setVisible(true); // نمایش //
//////////
//////////        world.setDtSeconds(SimulationConfig.TICK_INTERVAL / 1000.0); // تنظیم dt //
//////////        clock.register(world); // ثبت دنیا در ساعت //
//////////        clock.start(); // شروع //
//////////    }
//////////
//////////    private JComponent buildControlBar() { // ساخت نوار کنترل //
//////////        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // پنل دکمه‌ها //
//////////
//////////        JButton btnStart = new JButton("Start"); // شروع //
//////////        JButton btnStop  = new JButton("Stop");  // توقف //
//////////        JButton btnFwd   = new JButton("Speed x2"); // دوبرابر //
//////////        JButton btnSlw   = new JButton("Speed /2"); // نصف //
//////////        JButton btnAdd   = new JButton("Add Vehicle"); // افزودن خودرو //
//////////        JButton btnZoomP = new JButton("Zoom+"); // زوم +
//////////        JButton btnZoomM = new JButton("Zoom-"); // زوم -
//////////
//////////        btnStart.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.start(); } }); // //
//////////        btnStop .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.stop(); } }); // //
//////////        btnFwd  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(Math.max(10, clock.getInterval()/2)); } }); // //
//////////        btnSlw  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(clock.getInterval()*2); } }); // //
//////////        btnAdd  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.addRandomVehicle(); } }); // //
//////////        btnZoomP.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.zoomIn(); panel.repaint(); } }); // //
//////////        btnZoomM.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); } }); // //
//////////
//////////        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // //
//////////        return bar; // //
//////////    }
//////////
//////////    public static void main(String[] args) { // main //
//////////        SwingUtilities.invokeLater(new Runnable() { @Override public void run() { new MainWindow(); } }); // //
//////////    }
//////////}
