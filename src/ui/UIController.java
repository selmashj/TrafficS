package ui; // // پکیج UI

import simulation.World;                 // // دنیا
import simulation.SimulationClock;       // // ساعت شبیه‌سازی (نخِ پس‌زمینه)
import simulation.DemoTraffic;           // // ابزار افزودن خودروی تصادفی
import infrastructure.CityMap;           // // نقشهٔ شهر

/**
 * پل بین دکمه‌های UI و منطق شبیه‌سازی //
 * توجه: این نسخه متد pan(int,int) را برمی‌گرداند تا InputHandler بدون خطا کامپایل شود. //
 */
public class UIController { // // کنترلر UI
    private final World world;              // // مرجع دنیا
    private final SimulationClock clock;    // // مرجع ساعت شبیه‌سازی
    private final Camera camera;            // // مرجع دوربین برای زوم/پن
    private final CityMap map;              // // نقشه برای افزودن خودرو

    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
        this.world  = w;                  // // ذخیره دنیا
        this.clock  = c;                  // // ذخیره ساعت
        this.camera = cam;                // // ذخیره دوربین
        this.map    = w.getMap();         // // نگه‌داشتن نقشه برای DemoTraffic
    }

    // ---------- کنترل اجرا ----------

    public void start() {                  // // شروع شبیه‌سازی
        this.clock.start();                // // استارت نخِ ساعت
    }

    public void stop() {                   // // توقف شبیه‌سازی
        this.clock.stop();                 // // استاپ نخِ ساعت
    }

    // ---------- تغییر سرعت (بر پایهٔ speedMultiplier در SimulationClock) ----------

    public void speedUp2x() {                                           // // ۲ برابر سریع‌تر
        double cur = clock.getSpeedMultiplier();                        // // خواندن ضریب فعلی
        double next = cur * 2.0;                                        // // محاسبهٔ ضریب جدید
        if (next > 16.0) next = 16.0;                                   // // کَپ بالایی منطقی
        clock.setSpeedMultiplier(next);                                 // // اعمال
    }

    public void speedDown2x() {                                         // // ۲ برابر کندتر
        double cur = clock.getSpeedMultiplier();                        // // خواندن ضریب فعلی
        double next = cur / 2.0;                                        // // محاسبهٔ ضریب جدید
        if (next < 0.0625) next = 0.0625;                               // // کَپ پایینی منطقی
        clock.setSpeedMultiplier(next);                                 // // اعمال
    }

    // ---------- افزودن خودروی تصادفی (در صورت وجود DemoTraffic) ----------

    public void addVehicle() {                                          // // افزودن خودرو
        try {                                                           // // ایمنی در برابر نبود کلاس/امضا
            DemoTraffic.addRandomVehicle(world, map);                   // // افزودن یک خودرو روی یک لِین تصادفی
        } catch (Throwable ignored) {                                   // // اگر کلاسی نبود، بی‌صدا رد شو
        }
    }

    // ---------- کنترل دوربین ----------

    public void zoomIn() {                                              // // زوم +
        this.camera.setScale(this.camera.getScale() * 1.10);            // // افزایش مقیاس
    }

    public void zoomOut() {                                             // // زوم -
        this.camera.setScale(this.camera.getScale() / 1.10);            // // کاهش مقیاس
    }

    public void pan(int dx, int dy) {                                   // // جابه‌جایی دوربین (برای InputHandler)
        this.camera.pan(dx, dy);                                        // // پن دوربین به اندازهٔ dx,dy
    }
}




























//package ui; // // پکیج UI
//
//import simulation.World; // // دنیا
//import simulation.SimulationClock; // // ساعت
//import simulation.DemoTraffic; // // افزودن خودروی تصادفی
//import infrastructure.CityMap; // // نقشه
//
///**
// * پل بین دکمه‌ها و SimulationClock/World. //
// */
//public class UIController { // // کنترلر UI
//    private final World world; // // دنیا
//    private final SimulationClock clock; // // ساعت
//    private final Camera camera; // // دوربین
//
//    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
//        this.world = w; // // ذخیره دنیا
//        this.clock = c; // // ذخیره ساعت
//        this.camera = cam; // // ذخیره دوربین
//    }
//
//    public void start() { // // شروع
//        clock.start(); // // آغاز نخ شبیه‌سازی
//    }
//
//    public void stop() { // // توقف
//        clock.stop(); // // توقف نخ شبیه‌سازی
//    }
//
//    public void speedUp2x() { // // دو برابر سریع‌تر
//        double m = clock.getSpeedMultiplier(); // // ضریب فعلی
//        clock.setSpeedMultiplier(m * 2.0); // // اعمال ۲×
//    }
//
//    public void speedDown2x() { // // نصف سرعت
//        double m = clock.getSpeedMultiplier(); // // ضریب فعلی
//        clock.setSpeedMultiplier(m / 2.0); // // اعمال ÷۲
//    }
//
//    public void addVehicle() { // // افزودن خودرو
//        CityMap map = world.getMap(); // // گرفتن نقشه
//        DemoTraffic.addRandomVehicle(world, map); // // افزودن خودرو تصادفی
//    }
//
//    public void zoomIn() { // // زوم+
//        camera.setScale(camera.getScale() * 1.10); // // افزایش مقیاس
//    }
//
//    public void zoomOut() { // // زوم-
//        camera.setScale(camera.getScale() / 1.10); // // کاهش مقیاس
//    }
//}




























//package ui;
//
//import simulation.*;
//import infrastructure.*;
//import core.*;
//import java.util.*;
//
//// کنترلر رابط کاربری
//public class UIController {
//    private final World world; // دنیا
//    private final SimulationClock clock; // ساعت شبیه‌سازی
//    private final Camera camera; // دوربین
//    private boolean paused; // وضعیت توقف
//    private final CityMap map; // نقشه شهر
//
//    // سازنده
//    public UIController(World w, SimulationClock c, Camera cam) {
//        this.world = w; // ذخیره دنیا
//        this.clock = c; // ذخیره ساعت
//        this.camera = cam; // ذخیره دوربین
//        this.map = w.getMap(); // ذخیره نقشه
//        this.paused = false; // وضعیت اولیه
//        clock.register(w); // ثبت دنیا در ساعت
//    }
//
//    // شروع شبیه‌سازی
//    public void start() {
//        if (paused) { // اگر متوقف بود
//            paused = false; // به‌روزرسانی وضعیت
//            clock.start(); // شروع ساعت
//        }
//    }
//
//    // توقف شبیه‌سازی
//    public void stop() {
//        if (!paused) { // اگر در حال اجرا بود
//            paused = true; // به‌روزرسانی وضعیت
//            clock.stop(); // توقف ساعت
//        }
//    }
//
//    // تغییر سرعت شبیه‌سازی
//    public void changeSpeed(int newIntervalMs) {
//        if (newIntervalMs < 1) newIntervalMs = 1; // حداقل منطقی
//        clock.setInterval(newIntervalMs); // اعمال روی ساعت
//        world.setDtSeconds(newIntervalMs / 1000.0); // تنظیم dt دنیا
//    }
//
//    // افزودن خودرو تصادفی
//    public Vehicle addRandomVehicle() {
//        DemoTraffic.addRandomVehicle(world, map); // افزودن خودرو
//        List<Vehicle> list = world.getVehicles(); // گرفتن لیست
//        if (list == null || list.isEmpty()) return null; // بررسی خالی بودن
//        return list.get(list.size() - 1); // آخرین خودرو
//    }
//
//    // بزرگ‌نمایی
//    public void zoomIn() {
//        camera.setScale(camera.getScale() * 1.10); // افزایش زوم
//    }
//
//    // کوچک‌نمایی
//    public void zoomOut() {
//        camera.setScale(camera.getScale() / 1.10); // کاهش زوم
//    }
//
//    // جابه‌جایی دوربین
//    public void pan(int dx, int dy) {
//        camera.pan(dx, dy); // اعمال پن
//    }
//
//    // بررسی وضعیت توقف
//    public boolean isPaused() {
//        return paused; // خروجی وضعیت
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
////package ui; // // پکیج UI
////
////import simulation.*;            // // World, SimulationClock, DemoTraffic و ...
////import infrastructure.CityMap;  // // نقشهٔ شهر
////import core.Vehicle;            // // کلاس خودرو
////import java.util.List;          // // لیست برای برداشتن آخرین خودرو
////
////public class UIController {                 // // کنترلر UI
////    private final World world;              // // دنیا
////    private final SimulationClock clock;    // // ساعت شبیه‌سازی
////    private final Camera camera;            // // دوربین
////    private boolean paused = false;         // // وضعیت توقف/اجرا (در صورت نیاز)
////    private final CityMap map;              // // نقشهٔ شهر
////
////    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
////        this.world  = w;                   // // ذخیره دنیا
////        this.clock  = c;                   // // ذخیره ساعت
////        this.camera = cam;                 // // ذخیره دوربین
////        this.map    = w.getMap();          // // ذخیره نقشه
////    }
////
////    public void start() {                  // // شروع اجرا
////        if (paused) paused = false;        // // به‌روزرسانی وضعیت
////        clock.start();                     // // استارت ساعت
////    }
////
////    public void stop() {                   // // توقف اجرا
////        if (!paused) paused = true;        // // به‌روزرسانی وضعیت
////        clock.stop();                      // // استاپ ساعت
////    }
////
////    public void changeSpeed(int newIntervalMs) {          // // تغییر سرعت (ms بین تیک‌ها)
////        if (newIntervalMs < 1) newIntervalMs = 1;         // // حداقل منطقی
////        clock.setInterval(newIntervalMs);                 // // اعمال روی ساعت
////        // اگر World متدی برای ست‌کردن dt داشت (setDtSeconds)، با Reflection صدا بزن //
////        try {
////            world.getClass()
////                    .getMethod("setDtSeconds", double.class) // // پیدا کردن متد setDtSeconds(double)
////                    .invoke(world, (double)newIntervalMs / 1000.0); // // مقدار dt به ثانیه
////        } catch (Throwable ignored) {
////            // // اگر چنین متدی نبود، مشکلی نیست؛ همین تغییر interval کفایت می‌کند
////        }
////    }
////
////    public Vehicle addRandomVehicle() {     // // افزودن خودرو تصادفی و برگرداندن آبجکت
////        DemoTraffic.addRandomVehicle(world, map); // // افزودن خودرو (خروجی void)
////        List<Vehicle> list = world.getVehicles(); // // گرفتن لیست خودروها بعد از افزودن
////        if (list == null || list.isEmpty()) {     // // اگر به هر دلیل خالی بود
////            return null;                          // // خروجی null
////        }
////        return list.get(list.size() - 1);         // // آخرین خودرو (همان که تازه اضافه شده)
////    }
////
////    public void zoomIn() {                        // // بزرگ‌نمایی
////        camera.setScale(camera.getScale() * 1.10);// // افزایش زوم
////    }
////
////    public void zoomOut() {                       // // کوچک‌نمایی
////        camera.setScale(camera.getScale() / 1.10);// // کاهش زوم
////    }
////
////    public void pan(int dx, int dy) {             // // پن دستی
////        camera.pan(dx, dy);                        // // اعمال پن
////    }
////
////    public boolean isPaused() {                   // // دسترسی به وضعیت توقف
////        return paused;                            // // خروجی وضعیت
////    }
////}
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
////import simulation.*; // // World/Clock
////import infrastructure.CityMap; // // نقشه
////import core.Vehicle; // // خودرو
////
////public class UIController { // // کنترلر UI
////    private final World world; // // دنیا
////    private final SimulationClock clock; // // ساعت
////    private final Camera camera; // // دوربین
////    private boolean paused = false; // // وضعیت توقف
////    private final CityMap map; // // نقشه
////
////    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
////        this.world = w; // // ذخیره دنیا
////        this.clock = c; // // ذخیره ساعت
////        this.camera = cam; // // ذخیره دوربین
////        this.map = w.getMap(); // // ذخیره نقشه
////    }
////
////    public void start() { // // شروع اجرا
////        clock.start(); // // استارت
////    }
////
////    public void stop() { // // توقف اجرا
////        clock.stop(); // // استاپ
////    }
////
////    public void changeSpeed(int newIntervalMs) { // // تغییر سرعت
////        clock.setInterval(newIntervalMs); // // اعمال
////        world.setDtSeconds(newIntervalMs / 1000.0); // // هماهنگی dt
////    }
////
////    public Vehicle addRandomVehicle() { // // افزودن خودرو رندوم
////        return DemoTraffic.addRandomVehicle(world, map); // // استفاده از دمو
////    }
////
////    public void zoomIn() { // // بزرگ‌نمایی
////        camera.setScale(camera.getScale() * 1.1); // // افزایش زوم
////    }
////
////    public void zoomOut() { // // کوچک‌نمایی
////        camera.setScale(camera.getScale() / 1.1); // // کاهش زوم
////    }
////
////    public void pan(int dx, int dy) { // // پن دستی
////        camera.pan(dx, dy); // // اعمال پن
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
//////
//////package ui; // // پکیج UI
//////
//////import simulation.*; // // World/Clock
//////import infrastructure.CityMap; // // نقشه
//////import core.Vehicle; // // خودرو
//////
//////public class UIController { // // کنترلر UI
//////    private final World world; // // دنیا
//////    private final SimulationClock clock; // // ساعت
//////    private final Camera camera; // // دوربین
//////    private boolean paused = false; // // وضعیت توقف
//////    private final CityMap map; // // نقشه
//////
//////    public UIController(World w, SimulationClock c, Camera cam) { // // سازنده
//////        this.world = w; // // ذخیره دنیا
//////        this.clock = c; // // ذخیره ساعت
//////        this.camera = cam; // // ذخیره دوربین
//////        this.map = w.getMap(); // // ذخیره نقشه
//////    }
//////
//////    public void start() { // // شروع اجرا
//////        clock.start(); // // استارت
//////    }
//////
//////    public void stop() { // // توقف اجرا
//////        clock.stop(); // // استاپ
//////    }
//////
//////    public void changeSpeed(int newIntervalMs) { // // تغییر سرعت
//////        clock.setInterval(newIntervalMs); // // اعمال
//////        world.setDtSeconds(newIntervalMs / 1000.0); // // هماهنگی dt
//////    }
//////
//////    public Vehicle addRandomVehicle() { // // افزودن خودرو رندوم
//////        return DemoTraffic.addRandomVehicle(world, map); // // استفاده از دمو
//////    }
//////
//////    public void zoomIn() { // // بزرگ‌نمایی
//////        camera.setScale(camera.getScale() * 1.1); // // افزایش زوم
//////    }
//////
//////    public void zoomOut() { // // کوچک‌نمایی
//////        camera.setScale(camera.getScale() / 1.1); // // کاهش زوم
//////    }
//////
//////    public void pan(int dx, int dy) { // // پن دستی
//////        camera.pan(dx, dy); // // اعمال پن
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
////////package ui; // // پکیج UI
////////
////////import core.Vehicle; // // خودرو
////////import simulation.SimulationClock; // // ساعت
////////import simulation.World; // // جهان
////////
////////public class UIController { // // کنترلر UI
////////    private final World world; // // مدل جهان
////////    private final SimulationClock clock; // // ساعت
////////    private final Camera camera; // // دوربین
////////    private boolean paused; // // وضعیت توقف
////////    private int tickInterval; // // فاصله تیک
////////
////////    public UIController(World world, SimulationClock clock, Camera camera) { // // سازنده
////////        this.world = world; // // مقداردهی
////////        this.clock = clock; // // مقداردهی
////////        this.camera = camera; // // مقداردهی
////////        this.tickInterval = 100; // // مقدار پیش‌فرض
////////    }
////////
////////    public void pause() { // // توقف
////////        clock.stop(); // // توقف ساعت
////////        paused = true; // // علامت توقف
////////    }
////////
////////    public void resume() { // // ادامه
////////        if (!clock.isRunning()) clock.start(); // // اگر ساعت خاموش بود روشن کن
////////        paused = false; // // علامت ادامه
////////    }
////////
//////////    public void changeSpeed(int interval) { // // تغییر سرعت
//////////        tickInterval = Math.max(10, interval); // // محدودیت مینیمم
//////////        clock.setTickInterval(tickInterval); // // اعمال روی ساعت
//////////    }
////////
////////    public void followVehicle(Vehicle v) { // // دنبال‌کردن خودرو
////////        camera.follow(v); // // تنظیم دوربین
////////    }
////////
////////    public void changeSpeed(int interval) {
////////        tickInterval = Math.max(10, interval);        // // حداقل 10ms
////////        clock.setTickInterval(tickInterval);          // // اعمال روی Clock
////////        // ↓↓↓ این خط را اضافه کن تا فیزیک حرکت هم سریع/کند شود
////////        world.setDtSeconds(tickInterval / 1000.0);    // // تبدیل ms به ثانیه برای محاسبه حرکت
////////    }
////////
////////
////////
////////    public boolean isPaused() { return paused; } // // گرفتن وضعیت توقف
////////    public int getTickInterval() { return tickInterval; } // // گرفتن فاصله تیک
////////}
////////
////////
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
////////import simulation.SimulationClock;
////////import simulation.World;
////////
////////public class UIController {
////////    private final World world;
////////    private final SimulationClock clock;
////////    private final Camera camera;
////////
////////    private Vehicle followedVehicle;
////////    private boolean paused;
////////    private int tickInterval;
////////
////////    public UIController(World world, SimulationClock clock, Camera camera) {
////////        this.world = world;
////////        this.clock = clock;
////////        this.camera = camera;
////////        this.tickInterval = 100;
////////    }
////////
////////    public void pause() {
////////        clock.stop();
////////        paused = true;
////////    }
////////
////////    public void resume() {
////////        if (!clock.isRunning()) clock.start();
////////        paused = false;
////////    }
////////
////////    public void changeSpeed(int interval) {
////////        tickInterval = Math.max(10, interval);
////////        clock.setTickInterval(tickInterval);
////////    }
////////
////////    public void followVehicle(Vehicle v) {
////////        followedVehicle = v;
////////        camera.follow(v);
////////    }
////////
////////    public void toggleFollow() {
////////        camera.toggleFollow();
////////    }
////////
////////    public void selectVehicleById(String id) {
////////        for (var v : world.getVehicles()) {
////////            if (v.getId().equals(id)) {
////////                followVehicle(v);
////////                break;
////////            }
////////        }
////////    }
////////
////////    public void showStats() {
////////        // در آینده: نمایش پنجره آمار
////////    }
////////
////////    public void issueManualFine(Vehicle v) {
////////        // در آینده: تعامل با پلیس
////////    }
////////
////////    public void resetSimulation() {
////////        // در آینده: پاکسازی و بارگذاری دوباره world
////////    }
////////
////////    public boolean isPaused() { return paused; }
////////    public int getTickInterval() { return tickInterval; }
////////}
////////
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
////////public class UIController {
////////    private boolean paused = false;
////////
////////    public void pause()  { paused = true;  /* بعداً clock.stop() */ }
////////    public void resume() { paused = false; /* بعداً clock.start() */ }
////////    public boolean isPaused() { return paused; }
////////
////////    public void changeSpeed(int intervalMs) {
////////        // TODO: وقتی SimulationClock داری، اینجا سرعت تیک رو تغییر بده
////////    }
////////
////////    public void followVehicleById(String id) {
////////        // TODO: بعداً دوربین رو روی خودروی انتخابی قفل کن
////////    }
////////
////////    public void resetSimulation() {
////////        // TODO: بعداً ریست دنیای شبیه‌ساز
////////    }
////////}
