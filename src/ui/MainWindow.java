package ui; // // پکیج UI

import simulation.*; // // World/Clock/Config/DemoTraffic/DemoMaps/UIController/Camera
import infrastructure.*; // // CityMap/Road/Intersection
import core.*; // // Direction/Point
import pedestrian.*; // // Crossing/Pedestrian
import javax.swing.*; // // Swing
import java.awt.*; // // Layout
import java.awt.event.*; // // رویدادها
import java.util.List; // // لیست

public class MainWindow extends JFrame { // // پنجره اصلی
    private final World world; // // دنیا
    private final SimulationClock clock; // // ساعت
    private final UIController controller; // // کنترلر
    private final Camera camera; // // دوربین
    private final SimulatorPanel panel; // // پنل

    public MainWindow() { // // سازنده
        super("Traffic Simulator (Pure Swing)"); // // عنوان

        CityMap map = DemoMaps.irregularGrid(8, 8, 220, 320, 180, 180); // // نقشهٔ بزرگ و نامنظم
        this.world = new World(map); // // ساخت دنیا
        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // // ساعت
        this.camera = new Camera(); // // دوربین
        this.controller = new UIController(world, clock, camera); // // کنترلر

        DemoTraffic.installLights(world, map, 3500, 700, 3000); // // نصب چراغ‌ها
        DemoTraffic.seedVehicles(world, map, clock, 14); // // کاشت خودروها

        for (int i = 0; i < map.getIntersections().size(); i++) { // // گذرگاه‌های نمونه
            Intersection it = map.getIntersections().get(i); // // تقاطع
            world.addCrossing(new PedestrianCrossing("PX-" + i + "-N", it, Direction.NORTH, true)); // // گذرگاه
            world.addCrossing(new PedestrianCrossing("PX-" + i + "-E", it, Direction.EAST,  true)); // // گذرگاه
        }

        // ===== قوس نرم خودکار روی بخشی از خیابان‌ها (مثل عکس) =====
        java.util.Random _rnd = new java.util.Random(); // // رندوم
        List<Road> _roads = world.getMap().getRoads(); // // همهٔ جاده‌ها
        for (int i = 0; i < _roads.size(); i++) { // // حلقه
            Road r = _roads.get(i); // // جاده
            core.Point A = r.getStartIntersection().getPosition(); // // A
            core.Point B = r.getEndIntersection().getPosition(); // // B
            if (_rnd.nextDouble() < 0.35) { // // با احتمال ۳۵٪
                double mx = (A.getX() + B.getX()) * 0.5; // // X میانه
                double my = (A.getY() + B.getY()) * 0.5; // // Y میانه
                double dx = B.getX() - A.getX(); // // Δx
                double dy = B.getY() - A.getY(); // // Δy
                double len = Math.hypot(dx, dy); if (len < 1e-6) len = 1; // // طول
                double nx = -dy / len, ny = dx / len; // // نرمال چپ
                double bend = Math.min(70, len * 0.22); // // شدت قوس
                if (_rnd.nextBoolean()) bend = -bend; // // جهت قوس
                core.Point C = new core.Point((int)Math.round(mx + nx * bend), (int)Math.round(my + ny * bend)); // // کنترل
                r.setQuadraticControl(C); // // اعمال قوس
            }
        }

        this.panel = new SimulatorPanel(world, camera); // // پنل رسم
        panel.loadAssets("assets/car1.png"); // // تصویر خودرو (اختیاری)

        camera.setScale(0.8); // // زوم اولیه
        camera.pan(-200, -120); // // پن اولیه

        setLayout(new BorderLayout()); // // چیدمان
        add(panel, BorderLayout.CENTER); // // افزودن پنل
        add(buildControlBar(), BorderLayout.SOUTH); // // نوار کنترل

        InputHandler input = new InputHandler(controller, panel); // // ورودی
        panel.addKeyListener(input); // // اتصال
        panel.setFocusable(true); // // فوکوس‌پذیر
        panel.requestFocusInWindow(); // // درخواست فوکوس

        pack(); // // اندازه‌گذاری
        setSize(1280, 800); // // اندازه پنجره
        setLocationRelativeTo(null); // // وسط صفحه
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // // خروج
        setVisible(true); // // نمایش

        world.setDtSeconds(SimulationConfig.TICK_INTERVAL / 1000.0); // // همگام‌سازی dt
        clock.register(world); // // ثبت دنیا
        clock.start(); // // شروع
    }

    private JComponent buildControlBar() { // // نوار کنترل
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل

        JButton btnStart = new JButton("Start"); // // شروع
        JButton btnStop  = new JButton("Stop"); // // توقف
        JButton btnFwd   = new JButton("Speed x2"); // // تندتر
        JButton btnSlw   = new JButton("Speed /2"); // // کندتر
        JButton btnAdd   = new JButton("Add Vehicle"); // // افزودن خودرو
        JButton btnZoomP = new JButton("Zoom+"); // // زوم+
        JButton btnZoomM = new JButton("Zoom-"); // // زوم-

        btnStart.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.start(); }}); // // اکشن
        btnStop .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.stop();  }}); // // اکشن
        btnFwd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(Math.max(10, clock.getInterval()/2)); }}); // // اکشن
        btnSlw  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(clock.getInterval()*2); }}); // // اکشن
        btnAdd  .addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.addRandomVehicle(); }}); // // اکشن
        btnZoomP.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomIn(); panel.repaint(); }}); // // اکشن
        btnZoomM.addActionListener(new ActionListener(){ @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); }}); // // اکشن

        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // // چیدمان
        return bar; // // خروجی
    }

    public static void main(String[] args) { // // ورود
        SwingUtilities.invokeLater(new Runnable(){ @Override public void run(){ new MainWindow(); }}); // // اجرای روی EDT
    }
}






























//9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
//package ui; // // پکیج UI
//
//import simulation.*;            // // World / SimulationClock / SimulationConfig / DemoTraffic / DemoMaps
//import infrastructure.*;        // // CityMap / Intersection / Road / Lane
//import core.*;                  // // Direction / Point
//import pedestrian.*;            // // Pedestrian / PedestrianCrossing
//
//import javax.swing.*;           // // اجزای Swing
//import java.awt.*;              // // Layout و گرافیک
//import java.awt.event.*;        // // لیسنرها
//
//public class MainWindow extends JFrame { // // پنجرهٔ اصلی برنامه
//    private final World world;          // // دنیای شبیه‌سازی
//    private final SimulationClock clock;// // ساعت شبیه‌سازی
//    private final UIController controller; // // کنترلر UI
//    private final Camera camera;        // // دوربین (زوم/پن)
//    private final SimulatorPanel panel; // // پنل رسم
//
//    public MainWindow() { // // سازندهٔ پنجره
//        super("Traffic Simulator (Pure Swing)"); // // عنوان پنجره
//
//        CityMap map = DemoMaps.irregularGrid(7, 7, 90, 170, 120, 120); // // تولید نقشهٔ نامنظم برای تست
//        this.world = new World(map);                                  // // ایجاد دنیا
//        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // // ساخت ساعت با تیک پیش‌فرض
//        this.camera = new Camera();                                    // // ایجاد دوربین
//        this.controller = new UIController(world, clock, camera);      // // ساخت کنترلر
//
//        // نصب چراغ‌ها برای هر تقاطع و هر جهت:
//        // توجه: نسخهٔ فعلی DemoTraffic.installLights فقط 5 آرگومان می‌پذیرد (green,yellow,red)
//        DemoTraffic.installLights(world, map, 3500, 700, 3000); // // CHANGED: حذف آرگومان ششمِ tickInterval
//
//        // کاشت چند خودروی اولیه روی لِین‌های تصادفی
//        DemoTraffic.seedVehicles(world, map, clock, 12); // // افزودن ۱۲ خودرو برای شروع
//
//        // چند گذرگاهِ نمونه برای نمایش عابر (اختیاری)
//        for (int i = 0; i < map.getIntersections().size(); i++) {            // // پیمایش تقاطع‌ها
//            Intersection it = map.getIntersections().get(i);                 // // گرفتن تقاطع
//            world.addCrossing(new PedestrianCrossing("PX-" + i + "-N", it, Direction.NORTH, true)); // // گذرگاه شمال
//            world.addCrossing(new PedestrianCrossing("PX-" + i + "-E", it, Direction.EAST,  true)); // // گذرگاه شرق
//        }
//
//        this.panel = new SimulatorPanel(world, camera); // // ساخت پنل رسم
//        panel.loadAssets("assets/car1.png");            // // بارگذاری تصویر خودرو (در صورت وجود)
//
//        setLayout(new BorderLayout());                  // // چیدمان کلی
//        add(panel, BorderLayout.CENTER);                // // افزودن پنل به مرکز
//        add(buildControlBar(), BorderLayout.SOUTH);     // // نوار کنترل پایین
//
//        InputHandler input = new InputHandler(controller, panel); // // ساخت ورودی کیبورد/ماوس
//        panel.addKeyListener(input);                                  // // اتصال لیسنر کیبورد
//        panel.setFocusable(true);                                     // // فوکوس‌پذیر کردن پنل
//        panel.requestFocusInWindow();                                 // // درخواست فوکوس
//
//        pack();                                // // محاسبهٔ اندازهٔ مناسب
//        setSize(1100, 700);                    // // اندازهٔ اولیهٔ پنجره
//        setLocationRelativeTo(null);           // // وسط صفحه
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // // بستن برنامه با بستن پنجره
//        setVisible(true);                      // // نمایش پنجره
//
//        world.setDtSeconds(SimulationConfig.TICK_INTERVAL / 1000.0); // // هماهنگ‌سازی dt با ساعت
//        clock.register(world);                                       // // ثبت دنیا در ساعت
//        clock.start();                                               // // شروع تیک‌ها
//    }
//
//    private JComponent buildControlBar() { // // ساخت نوار کنترل پایین
//        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // // پنل با چیدمان سمت چپ
//
//        JButton btnStart = new JButton("Start");      // // شروع
//        JButton btnStop  = new JButton("Stop");       // // توقف
//        JButton btnFwd   = new JButton("Speed x2");   // // تندتر
//        JButton btnSlw   = new JButton("Speed /2");   // // کندتر
//        JButton btnAdd   = new JButton("Add Vehicle");// // افزودن خودرو
//        JButton btnZoomP = new JButton("Zoom+");      // // زوم +
//        JButton btnZoomM = new JButton("Zoom-");      // // زوم -
//
//        // بدون لامبدا — به صورت کلاس بی‌نام:
//        btnStart.addActionListener(new ActionListener() { // // کلیک Start
//            @Override public void actionPerformed(ActionEvent e) { controller.start(); } // // شروع ساعت
//        });
//        btnStop.addActionListener(new ActionListener() {  // // کلیک Stop
//            @Override public void actionPerformed(ActionEvent e) { controller.stop(); }  // // توقف ساعت
//        });
//        btnFwd.addActionListener(new ActionListener() {   // // کلیک Speed x2
//            @Override public void actionPerformed(ActionEvent e) {
//                int half = Math.max(10, clock.getInterval() / 2); // // نصف کردن بازهٔ تیک
//                controller.changeSpeed(half);                      // // اعمال
//            }
//        });
//        btnSlw.addActionListener(new ActionListener() {   // // کلیک Speed /2
//            @Override public void actionPerformed(ActionEvent e) {
//                controller.changeSpeed(clock.getInterval() * 2);  // // دو برابر کردن بازهٔ تیک
//            }
//        });
//        btnAdd.addActionListener(new ActionListener() {   // // کلیک Add Vehicle
//            @Override public void actionPerformed(ActionEvent e) { controller.addRandomVehicle(); } // // افزودن خودرو
//        });
//        btnZoomP.addActionListener(new ActionListener() { // // کلیک Zoom+
//            @Override public void actionPerformed(ActionEvent e) { controller.zoomIn(); panel.repaint(); } // // زوم به داخل
//        });
//        btnZoomM.addActionListener(new ActionListener() { // // کلیک Zoom-
//            @Override public void actionPerformed(ActionEvent e) { controller.zoomOut(); panel.repaint(); } // // زوم به بیرون
//        });
//
//        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // // چیدمان دکمه‌ها
//        return bar; // // برگرداندن نوار
//    }
//
//    public static void main(String[] args) { // // نقطهٔ ورود
//        SwingUtilities.invokeLater(new Runnable() { // // اجرای UI روی EDT (بدون لامبدا)
//            @Override public void run() { new MainWindow(); } // // ساخت و نمایش پنجره
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
////7777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
////
////package ui; // پکیج UI //
////
////import simulation.*;           // World, SimulationClock, SimulationConfig, DemoTraffic, DemoMaps //
////import infrastructure.*;       // CityMap //
////import javax.swing.*;          // Swing //
////import java.awt.*;             // Layout //
////import java.awt.event.*;       // Listeners //
////
////public class MainWindow extends JFrame { // پنجره اصلی //
////    private final World world;           // دنیا //
////    private final SimulationClock clock; // ساعت //
////    private final UIController controller; // کنترلر //
////    private final Camera camera;         // دوربین //
////    private final SimulatorPanel panel;  // پنل رسم //
////
////    public MainWindow() { // سازنده //
////        super("Traffic Simulator"); // عنوان //
////
////        CityMap map = DemoMaps.irregularGrid(7, 7, 90, 170, 120, 120); // ساخت نقشه آزمایشی //
////        this.world = new World(map); // ✅ فیکس: World(map) چون سازنده‌اش CityMap می‌گیرد //
////        this.clock = new SimulationClock(SimulationConfig.TICK_INTERVAL); // ساعت //
////        this.camera = new Camera(); // دوربین //
////        this.controller = new UIController(world, clock, camera); // کنترلر //
////
////        DemoTraffic.setup(world, map, clock); // چراغ‌ها + ۷۰ خودرو + عابر //
////
////        this.panel = new SimulatorPanel(world, camera); // ✅ فیکس: سازنده‌ی پنل (world, camera) //
////        // اگر متد loadAssets داری، بازش کن. اگر نداری، همین‌طور بماند. //
////        // panel.loadAssets("assets/car1.png"); //
////
////        setLayout(new BorderLayout()); // Layout //
////        add(panel, BorderLayout.CENTER); // پنل وسط //
////        add(buildControlBar(), BorderLayout.SOUTH); // کنترل‌ها //
////
////        InputHandler input = new InputHandler(controller, panel); // ورودی //
////        panel.addKeyListener(input); // لیسنر //
////        panel.setFocusable(true); // فوکوس //
////
////        pack(); // اندازه //
////        setSize(1100, 700); // سایز //
////        setLocationRelativeTo(null); // مرکز //
////        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // خروج //
////        setVisible(true); // نمایش //
////
////        world.setDtSeconds(SimulationConfig.TICK_INTERVAL / 1000.0); // تنظیم dt //
////        clock.register(world); // ثبت دنیا در ساعت //
////        clock.start(); // شروع //
////    }
////
////    private JComponent buildControlBar() { // ساخت نوار کنترل //
////        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // پنل دکمه‌ها //
////
////        JButton btnStart = new JButton("Start"); // شروع //
////        JButton btnStop  = new JButton("Stop");  // توقف //
////        JButton btnFwd   = new JButton("Speed x2"); // دوبرابر //
////        JButton btnSlw   = new JButton("Speed /2"); // نصف //
////        JButton btnAdd   = new JButton("Add Vehicle"); // افزودن خودرو //
////        JButton btnZoomP = new JButton("Zoom+"); // زوم +
////        JButton btnZoomM = new JButton("Zoom-"); // زوم -
////
////        btnStart.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.start(); } }); // //
////        btnStop .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.stop(); } }); // //
////        btnFwd  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(Math.max(10, clock.getInterval()/2)); } }); // //
////        btnSlw  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.changeSpeed(clock.getInterval()*2); } }); // //
////        btnAdd  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.addRandomVehicle(); } }); // //
////        btnZoomP.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.zoomIn(); panel.repaint(); } }); // //
////        btnZoomM.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){ controller.zoomOut(); panel.repaint(); } }); // //
////
////        bar.add(btnStart); bar.add(btnStop); bar.add(btnFwd); bar.add(btnSlw); bar.add(btnAdd); bar.add(btnZoomP); bar.add(btnZoomM); // //
////        return bar; // //
////    }
////
////    public static void main(String[] args) { // main //
////        SwingUtilities.invokeLater(new Runnable() { @Override public void run() { new MainWindow(); } }); // //
////    }
////}
