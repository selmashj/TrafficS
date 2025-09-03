package ui; // // پکیج UI

import simulation.*; // // World/Clock
import infrastructure.CityMap; // // نقشه
import core.Vehicle; // // خودرو

public class UIController { // // کنترلر UI
    private final World world; // // دنیا
    private final SimulationClock clock; // // ساعت
    private final Camera camera; // // دوربین
    private boolean paused = false; // // وضعیت توقف
    private final CityMap map; // // نقشه

    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
        this.world = w; // // ذخیره دنیا
        this.clock = c; // // ذخیره ساعت
        this.camera = cam; // // ذخیره دوربین
        this.map = w.getMap(); // // ذخیره نقشه
    }

    public void start() { // // شروع اجرا
        clock.start(); // // استارت
    }

    public void stop() { // // توقف اجرا
        clock.stop(); // // استاپ
    }

    public void changeSpeed(int newIntervalMs) { // // تغییر سرعت
        clock.setInterval(newIntervalMs); // // اعمال
        world.setDtSeconds(newIntervalMs / 1000.0); // // هماهنگی dt
    }

    public Vehicle addRandomVehicle() { // // افزودن خودرو رندوم
        return DemoTraffic.addRandomVehicle(world, map); // // استفاده از دمو
    }

    public void zoomIn() { // // بزرگ‌نمایی
        camera.setScale(camera.getScale() * 1.1); // // افزایش زوم
    }

    public void zoomOut() { // // کوچک‌نمایی
        camera.setScale(camera.getScale() / 1.1); // // کاهش زوم
    }

    public void pan(int dx, int dy) { // // پن دستی
        camera.pan(dx, dy); // // اعمال پن
    }
}


































//
//package ui; // // پکیج UI
//
//import simulation.*; // // World/Clock
//import infrastructure.CityMap; // // نقشه
//import core.Vehicle; // // خودرو
//
//public class UIController { // // کنترلر UI
//    private final World world; // // دنیا
//    private final SimulationClock clock; // // ساعت
//    private final Camera camera; // // دوربین
//    private boolean paused = false; // // وضعیت توقف
//    private final CityMap map; // // نقشه
//
//    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
//        this.world = w; // // ذخیره دنیا
//        this.clock = c; // // ذخیره ساعت
//        this.camera = cam; // // ذخیره دوربین
//        this.map = w.getMap(); // // ذخیره نقشه
//    }
//
//    public void start() { // // شروع اجرا
//        clock.start(); // // استارت
//    }
//
//    public void stop() { // // توقف اجرا
//        clock.stop(); // // استاپ
//    }
//
//    public void changeSpeed(int newIntervalMs) { // // تغییر سرعت
//        clock.setInterval(newIntervalMs); // // اعمال
//        world.setDtSeconds(newIntervalMs / 1000.0); // // هماهنگی dt
//    }
//
//    public Vehicle addRandomVehicle() { // // افزودن خودرو رندوم
//        return DemoTraffic.addRandomVehicle(world, map); // // استفاده از دمو
//    }
//
//    public void zoomIn() { // // بزرگ‌نمایی
//        camera.setScale(camera.getScale() * 1.1); // // افزایش زوم
//    }
//
//    public void zoomOut() { // // کوچک‌نمایی
//        camera.setScale(camera.getScale() / 1.1); // // کاهش زوم
//    }
//
//    public void pan(int dx, int dy) { // // پن دستی
//        camera.pan(dx, dy); // // اعمال پن
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
////package ui; // // پکیج UI
////
////import core.Vehicle; // // خودرو
////import simulation.SimulationClock; // // ساعت
////import simulation.World; // // جهان
////
////public class UIController { // // کنترلر UI
////    private final World world; // // مدل جهان
////    private final SimulationClock clock; // // ساعت
////    private final Camera camera; // // دوربین
////    private boolean paused; // // وضعیت توقف
////    private int tickInterval; // // فاصله تیک
////
////    public UIController(World world, SimulationClock clock, Camera camera) { // // سازنده
////        this.world = world; // // مقداردهی
////        this.clock = clock; // // مقداردهی
////        this.camera = camera; // // مقداردهی
////        this.tickInterval = 100; // // مقدار پیش‌فرض
////    }
////
////    public void pause() { // // توقف
////        clock.stop(); // // توقف ساعت
////        paused = true; // // علامت توقف
////    }
////
////    public void resume() { // // ادامه
////        if (!clock.isRunning()) clock.start(); // // اگر ساعت خاموش بود روشن کن
////        paused = false; // // علامت ادامه
////    }
////
//////    public void changeSpeed(int interval) { // // تغییر سرعت
//////        tickInterval = Math.max(10, interval); // // محدودیت مینیمم
//////        clock.setTickInterval(tickInterval); // // اعمال روی ساعت
//////    }
////
////    public void followVehicle(Vehicle v) { // // دنبال‌کردن خودرو
////        camera.follow(v); // // تنظیم دوربین
////    }
////
////    public void changeSpeed(int interval) {
////        tickInterval = Math.max(10, interval);        // // حداقل 10ms
////        clock.setTickInterval(tickInterval);          // // اعمال روی Clock
////        // ↓↓↓ این خط را اضافه کن تا فیزیک حرکت هم سریع/کند شود
////        world.setDtSeconds(tickInterval / 1000.0);    // // تبدیل ms به ثانیه برای محاسبه حرکت
////    }
////
////
////
////    public boolean isPaused() { return paused; } // // گرفتن وضعیت توقف
////    public int getTickInterval() { return tickInterval; } // // گرفتن فاصله تیک
////}
////
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
////package ui;
////
////import core.Vehicle;
////import simulation.SimulationClock;
////import simulation.World;
////
////public class UIController {
////    private final World world;
////    private final SimulationClock clock;
////    private final Camera camera;
////
////    private Vehicle followedVehicle;
////    private boolean paused;
////    private int tickInterval;
////
////    public UIController(World world, SimulationClock clock, Camera camera) {
////        this.world = world;
////        this.clock = clock;
////        this.camera = camera;
////        this.tickInterval = 100;
////    }
////
////    public void pause() {
////        clock.stop();
////        paused = true;
////    }
////
////    public void resume() {
////        if (!clock.isRunning()) clock.start();
////        paused = false;
////    }
////
////    public void changeSpeed(int interval) {
////        tickInterval = Math.max(10, interval);
////        clock.setTickInterval(tickInterval);
////    }
////
////    public void followVehicle(Vehicle v) {
////        followedVehicle = v;
////        camera.follow(v);
////    }
////
////    public void toggleFollow() {
////        camera.toggleFollow();
////    }
////
////    public void selectVehicleById(String id) {
////        for (var v : world.getVehicles()) {
////            if (v.getId().equals(id)) {
////                followVehicle(v);
////                break;
////            }
////        }
////    }
////
////    public void showStats() {
////        // در آینده: نمایش پنجره آمار
////    }
////
////    public void issueManualFine(Vehicle v) {
////        // در آینده: تعامل با پلیس
////    }
////
////    public void resetSimulation() {
////        // در آینده: پاکسازی و بارگذاری دوباره world
////    }
////
////    public boolean isPaused() { return paused; }
////    public int getTickInterval() { return tickInterval; }
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
////package ui;
////
////public class UIController {
////    private boolean paused = false;
////
////    public void pause()  { paused = true;  /* بعداً clock.stop() */ }
////    public void resume() { paused = false; /* بعداً clock.start() */ }
////    public boolean isPaused() { return paused; }
////
////    public void changeSpeed(int intervalMs) {
////        // TODO: وقتی SimulationClock داری، اینجا سرعت تیک رو تغییر بده
////    }
////
////    public void followVehicleById(String id) {
////        // TODO: بعداً دوربین رو روی خودروی انتخابی قفل کن
////    }
////
////    public void resetSimulation() {
////        // TODO: بعداً ریست دنیای شبیه‌ساز
////    }
////}
