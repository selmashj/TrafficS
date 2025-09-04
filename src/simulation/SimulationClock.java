package simulation; // // پکیج شبیه‌سازی

import java.util.List; // // لیست برای updatables قدیمی
import java.util.ArrayList; // // پیاده‌سازی لیست
import java.util.Collections; // // ابزارهای کالکشن
import java.util.concurrent.atomic.AtomicBoolean; // // پرچم‌های اتمی برای نخ
import java.util.concurrent.atomic.AtomicReference; // // مرجع اتمی برای ضریب سرعت

/**
 * ساعت شبیه‌سازی چندنخیِ واقعی: یک نخ پس‌زمینه که world.update(dt) را با نرخ هدف فراخوانی می‌کند. //
 * سازگاری با API قدیمی: register/unregister Updatable حفظ شده (اما World دوبار آپدیت نمی‌شود). //
 */
public class SimulationClock { // // کلاس ساعت

    private final World world; // // مرجع دنیا برای آپدیت
    private final AtomicBoolean running = new AtomicBoolean(false); // // وضعیت اجرا
    private final AtomicBoolean started = new AtomicBoolean(false); // // آیا نخ شروع شده؟
    private final AtomicReference<Double> speedMultiplier = new AtomicReference<Double>(1.0); // // ضریب سرعت

    private Thread loopThread; // // نخ حلقهٔ اصلی
    private final long targetFrameNanos = (long)(1_000_000_000L / 60.0); // // نرخ هدف ~60Hz
    private final List<Updatable> legacyUpdatables = Collections.synchronizedList(new ArrayList<Updatable>()); // // updatables قدیمی (سازگاری)

    public SimulationClock(World w) { // // سازنده
        this.world = w; // // ذخیره دنیا
    }

    public void start() { // // آغاز شبیه‌سازی
        if (started.compareAndSet(false, true)) { // // فقط یکبار نخ ساخته شود
            running.set(true); // // تنظیم به اجرا
            loopThread = new Thread(new Runnable() { // // ساخت Runnable بدون لامبدا
                @Override public void run() { loop(); } // // اجرای حلقه
            }, "SimulationClock-Loop"); // // نام نخ
            loopThread.setDaemon(true); // // نخ daemon تا با خروج UI، بسته شود
            loopThread.start(); // // شروع نخ
        } else { // // اگر قبلاً ساخته شده
            running.set(true); // // فقط اجرا را روشن کن
        }
    }

    public void stop() { // // توقف شبیه‌سازی
        running.set(false); // // درخواست توقف
        // // join اجباری نمی‌کنیم تا UI گیر نکند؛ نخ daemon خودش می‌ایستد.
    }

    public boolean isRunning() { // // وضعیت
        return running.get(); // // خروجی
    }

    public void setSpeedMultiplier(double m) { // // ست ضریب سرعت
        if (m < 0.125) m = 0.125; // // حداقل منطقی
        if (m > 8.0)   m = 8.0;   // // حداکثر منطقی
        speedMultiplier.set(Double.valueOf(m)); // // ست مقدار
    }

    public double getSpeedMultiplier() { // // گتر ضریب سرعت
        return speedMultiplier.get().doubleValue(); // // خروجی
    }

    public void register(Updatable u) { // // ثبت updatable قدیمی (سازگاری)
        if (u != null) legacyUpdatables.add(u); // // افزودن
    }

    public void unregister(Updatable u) { // // لغو ثبت
        legacyUpdatables.remove(u); // // حذف
    }

    private void loop() { // // حلقهٔ اصلی نخ
        long last = System.nanoTime(); // // زمان آخرین تیک
        while (true) { // // حلقه بی‌انتها
            if (!running.get()) { // // اگر اجرا خاموش است
                try { Thread.sleep(10); } catch (Throwable ignored) {} // // خواب کوتاه
                last = System.nanoTime(); // // ریست مبنا برای dt
                continue; // // ادامه حلقه
            }

            long frameStart = System.nanoTime(); // // شروع فریم
            double dt = (frameStart - last) / 1_000_000_000.0; // // محاسبه dt ثانیه
            if (dt < 0) dt = 0; // // ایمنی
            last = frameStart; // // به‌روز رسانی مبنا

            dt *= speedMultiplier.get().doubleValue(); // // اعمال ضریب سرعت
            if (dt > 0.25) dt = 0.25; // // سقف dt برای پایداری

            try { // // محافظت از کرش
                world.update(); // // آپدیت دنیا با dt
            } catch (Throwable t) { // // گرفتن خطا
                t.printStackTrace(); // // چاپ جهت دیباگ
            }

            // // سازگاری: updatables قدیمی (به‌جز World) را هم آپدیت کن
            try { // // محافظت
                List<Updatable> snap; // // اسنپ‌شات
                synchronized (legacyUpdatables) { // // قفل
                    snap = new ArrayList<Updatable>(legacyUpdatables); // // کپی
                }
                for (int i = 0; i < snap.size(); i++) { // // حلقه
                    Updatable u = snap.get(i); // // عضو
                    if (u == world) continue; // // جلوگیری از آپدیت دوباره World
                    try { u.update(); } catch (Throwable ignored) {} // // آپدیت بدون توقف حلقه
                }
            } catch (Throwable ignored) {}

            long frameEnd = System.nanoTime(); // // پایان فریم
            long workTime = frameEnd - frameStart; // // زمان مصرف‌شده
            long sleepNanos = targetFrameNanos - workTime; // // باقیمانده تا نرخ هدف
            if (sleepNanos > 0) { // // اگر زمان برای خواب داریم
                long ms = sleepNanos / 1_000_000L; // // میلی‌ثانیه
                int ns = (int)(sleepNanos % 1_000_000L); // // نانوثانیه باقیمانده
                try { Thread.sleep(ms, ns); } catch (Throwable ignored) {} // // خواب دقیق
            } else { // // اگر دیر کردیم
                Thread.yield(); // // واگذاری CPU برای جلوگیری از busy-wait
            }
        }
    }
}





























//package simulation;
//
//// کلاس ساعت شبیه‌سازی با نخ پس‌زمینه
//public class SimulationClock {
//    private final int intervalMs; // فاصله بین تیک‌ها (میلی‌ثانیه)
//    private final java.util.List<Updatable> updatables; // لیست اشیاء قابل آپدیت
//    private Thread clockThread; // نخ شبیه‌سازی
//    private volatile boolean running; // وضعیت اجرا
//
//    // سازنده با فاصله تیک
//    public SimulationClock(int intervalMs) {
//        this.intervalMs = intervalMs; // ذخیره فاصله
//        this.updatables = java.util.Collections.synchronizedList(new java.util.ArrayList<Updatable>()); // لیست ایمن
//        this.running = false; // وضعیت اولیه
//    }
//
//    // ثبت شیء قابل آپدیت
//    public void register(Updatable updatable) {
//        if (updatable != null) { // بررسی نال
//            synchronized (updatables) { // قفل برای ایمنی
//                updatables.add(updatable); // افزودن به لیست
//            }
//        }
//    }
//
//    // حذف شیء قابل آپدیت
//    public void unregister(Updatable updatable) {
//        if (updatable != null) { // بررسی نال
//            synchronized (updatables) { // قفل برای ایمنی
//                updatables.remove(updatable); // حذف از لیست
//            }
//        }
//    }
//
//    // شروع نخ شبیه‌سازی
//    public void start() {
//        if (running) return; // جلوگیری از شروع دوباره
//        running = true; // تنظیم وضعیت
//        clockThread = new Thread(new Runnable() { // نخ جدید بدون لامبدا
//            public void run() {
//                while (running) { // حلقه تا وقتی در حال اجرا
//                    try {
//                        synchronized (updatables) { // قفل لیست
//                            for (int i = 0; i < updatables.size(); i++) { // حلقه روی اشیاء
//                                updatables.get(i).update(); // آپدیت هر شیء
//                            }
//                        }
//                        Thread.sleep(intervalMs); // خواب دقیق
//                    } catch (InterruptedException e) {
//                        running = false; // در صورت قطع، توقف
//                    }
//                }
//            }
//        });
//        clockThread.start(); // شروع نخ
//    }
//
//    // توقف نخ شبیه‌سازی
//    public void stop() {
//        running = false; // تنظیم وضعیت
//        if (clockThread != null) { // بررسی وجود نخ
//            clockThread.interrupt(); // قطع نخ
//            clockThread = null; // پاک‌سازی
//        }
//    }
//
//    // تنظیم فاصله تیک‌ها
//    public void setInterval(int newIntervalMs) {
//        if (newIntervalMs > 0) { // بررسی مثبت بودن
//            try {
//                java.lang.reflect.Field field = SimulationClock.class.getDeclaredField("intervalMs"); // دسترسی به فیلد
//                field.setAccessible(true); // اجازه دسترسی
//                field.set(this, newIntervalMs); // تنظیم مقدار
//            } catch (Exception e) {
//                e.printStackTrace(); // چاپ خطا
//            }
//        }
//    }
//}




























//
//package simulation; // // پکیج شبیه‌سازی
//
//import java.util.ArrayList; // // لیست آپدیت‌شونده‌ها
//import java.util.List; // // اینترفیس لیست
//import javax.swing.Timer; // // تایمر سوینگ
//import java.awt.event.ActionListener; // // لیسنر رویداد تایمر
//import java.awt.event.ActionEvent; // // رویداد اکشن
//
//public class SimulationClock { // // ساعت شبیه‌سازی
//    private int tickInterval; // // فاصله تیک (میلی‌ثانیه)
//    private final List<Updatable> updatables; // // لیست آبجکت‌های قابل آپدیت
//    private Timer timer; // // تایمر داخلی
//
//    public SimulationClock(int ms) { // // سازنده
//        this.tickInterval = ms; // // ذخیره تیک اولیه
//        this.updatables = new ArrayList<Updatable>(); // // ساخت لیست
//        this.timer = new Timer(tickInterval, new ActionListener() { // // ساخت تایمر با لیسنر
//            @Override public void actionPerformed(ActionEvent e) { // // روی هر تیک
//                for (int i = 0; i < updatables.size(); i++) { // // حلقه روی آپدیت‌شونده‌ها
//                    updatables.get(i).update(); // // آپدیت هر آبجکت
//                }
//            }
//        });
//    }
//
//    public void register(Updatable u) { // // اضافه‌کردن آبجکت به ساعت
//        if (u != null && !updatables.contains(u)) { // // جلوگیری از تکرار
//            updatables.add(u); // // افزودن
//        }
//    }
//
//    public void unregister(Updatable u) { // // حذف از ساعت
//        updatables.remove(u); // // حذف
//    }
//
//    public void start() { // // شروع ساعت
//        if (!timer.isRunning()) timer.start(); // // اگر اجرا نیست، اجرا کن
//    }
//
//    public void stop() { // // توقف ساعت
//        if (timer.isRunning()) timer.stop(); // // اگر اجراست، متوقف کن
//    }
//
//    public void setInterval(int ms) { // // تغییر فاصله تیک
//        if (ms < 5) ms = 5; // // حداقل‌ایمنی
//        this.tickInterval = ms; // // ذخیره
//        this.timer.setDelay(ms); // // اعمال روی تایمر
//    }
//
//    public int getInterval() { // // گرفتن فاصله تیک
//        return tickInterval; // // خروجی
//    }
//}
































//package simulation;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
///**
// * ساعت شبیه‌سازی که همه‌ی آبجکت‌های Updatable را در هر تیک صدا می‌زند.
// */
//public class SimulationClock {
//
//    private final List<Updatable> listeners = new ArrayList<>();
//    private Timer timer;
//    private int intervalMs;
//    private boolean running = false;
//
//    public SimulationClock(int intervalMs) {
//        this.intervalMs = Math.max(1, intervalMs);
//    }
//
//    // ---- کنترل اجرا ----
//    public synchronized void start() {
//        if (running) return;
//        running = true;
//        timer = new Timer("sim-clock", true);
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override public void run() { tick(); }
//        }, 0, intervalMs);
//    }
//
//    public synchronized void stop() {
//        if (!running) return;
//        running = false;
//        if (timer != null) {
//            timer.cancel();
//            timer.purge();
//            timer = null;
//        }
//    }
//
//    public synchronized void setInterval(int newIntervalMs) {
//        this.intervalMs = Math.max(1, newIntervalMs);
//        if (running) { // ریست با نرخ جدید
//            stop();
//            start();
//        }
//    }
//
//    public int getInterval() { return intervalMs; }
//
//    // ---- مدیریت شنونده‌ها ----
//    public synchronized void register(Updatable u) {
//        if (u != null && !listeners.contains(u)) {
//            listeners.add(u);
//        }
//    }
//
//    public synchronized void unregister(Updatable u) {
//        listeners.remove(u);
//    }
//
//    // ---- تیک ----
//    private void tick() {
//        // از کپی استفاده می‌کنیم تا اگر در حین آپدیت لیست تغییر کرد، ConcurrentModification نگیریم
//        List<Updatable> snapshot;
//        synchronized (this) { snapshot = new ArrayList<>(listeners); }
//        for (int i = 0; i < snapshot.size(); i++) {
//            try { snapshot.get(i).update(); }
//            catch (Throwable t) { t.printStackTrace(); }
//        }
//    }
//}
//
//
//
//
//




















//package simulation;
//
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///** ساعت شبیه‌سازی: تِیک ثابت، نخِ اختصاصی، ایمن برای نخ‌ها */
//public class SimulationClock {
//
//    private volatile int intervalMs;                      // فاصله‌ی تیک (ms)
//    private final List<Updatable> listeners = new CopyOnWriteArrayList<>();
//    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//    private volatile boolean running = false;
//
//    public SimulationClock(int intervalMs) {
//        this.intervalMs = Math.max(1, intervalMs);
//    }
//
//    /** شروع تیک‌زنی (Idempotent) */
//    public synchronized void start() {
//        if (running) return;
//        running = true;
//        executor.scheduleAtFixedRate(this::tick, 0, intervalMs, TimeUnit.MILLISECONDS);
//    }
//
//    /** توقف تیک‌زنی (نخ را نمی‌بندد تا دوباره start بتواند برنامه‌ریزی کند) */
//    public synchronized void stop() {
//        running = false;
//        // هیچ کاری لازم نیست؛ scheduleAtFixedRate همچنان داره صدا می‌خوره اما tick خودش چک می‌کند.
//    }
//
//    /** تغییر سرعت (فاصله‌ی تیک) به صورت امن در حین اجرا */
//    public synchronized void setInterval(int newIntervalMs) {
//        int old = this.intervalMs;
//        this.intervalMs = Math.max(1, newIntervalMs);
//        if (!running) return;
//
//        // ری‌اسکجول برای حفظ نرمی حرکت
//        running = false;
//        // یک «وقفه کوتاه» تا تیک جاری تمام شود
//        try { Thread.sleep(Math.min(old, 5)); } catch (InterruptedException ignored) {}
//
//        running = true;
//        // ابتدا همه‌ی scheduled taskهای قبلی را purge کنیم (فقط پاکسازی داخلی)
//        // سپس یک برنامه‌ریزی تازه با بازه‌ی جدید انجام می‌دهیم
//        executor.scheduleAtFixedRate(this::tick, 0, this.intervalMs, TimeUnit.MILLISECONDS);
//    }
//
//    public int getInterval() { return intervalMs; }
//
//    /** ثبت/حذف شنونده‌ها */
//    public void register(Updatable u) {
//        if (u != null && !listeners.contains(u)) listeners.add(u);
//    }
//
//    public void unregister(Updatable u) {
//        if (u != null) listeners.remove(u);
//    }
//
//    /** تیک: فقط وقتی running=true باشد اجرا می‌شود */
//    private void tick() {
//        if (!running) return;
//        for (Updatable u : listeners) {
//            try { u.update(); } catch (Throwable t) {
//                // برای جلوگیری از مرگ نخ در اثر استثنا
//                t.printStackTrace();
//            }
//        }
//    }
//}





























//package simulation; // // پکیج simulation
//
//import java.util.ArrayList; // // لیست
//import java.util.List; // // اینترفیس لیست
//import java.util.Timer; // // تایمر جاوا
//import java.util.TimerTask; // // تسک زمان‌بندی
//
//import simulation.Updatable; // // اینترفیس آپدیت
//
//public class SimulationClock { // // ساعت شبیه‌سازی
//    private final List<Updatable> listeners; // // لیست آبجکت‌های قابل‌به‌روزرسانی
//    private Timer timer; // // تایمر داخلی
//    private int intervalMs; // // فاصله‌ی تیک (میلی‌ثانیه)
//    private boolean running; // // وضعیت اجرا
//
//    public SimulationClock(int intervalMs) { // // سازنده
//        this.listeners = new ArrayList<Updatable>(); // // ایجاد لیست
//        this.intervalMs = Math.max(1, intervalMs); // // ست فاصله تیک
//        this.running = false; // // ابتدا متوقف
//    }
//
//    public synchronized void register(Updatable u) { // // ثبت شنونده
//        if (u != null && !this.listeners.contains(u)) { // // جلوگیری از تکرار
//            this.listeners.add(u); // // افزودن
//        }
//    }
//
//    public synchronized void unregister(Updatable u) { // // حذف شنونده
//        this.listeners.remove(u); // // حذف
//    }
//
//    public synchronized int getInterval() { return this.intervalMs; } // // گتر فاصله تیک
//
//    public synchronized void setInterval(int newIntervalMs) { // // تغییر فاصله تیک
//        int clamped = newIntervalMs; // // مقدار موقت
//        if (clamped < SimulationConfig.MIN_INTERVAL) { clamped = SimulationConfig.MIN_INTERVAL; } // // حداقل
//        if (clamped > SimulationConfig.MAX_INTERVAL) { clamped = SimulationConfig.MAX_INTERVAL; } // // حداکثر
//        this.intervalMs = clamped; // // ست مقدار
//        if (this.running) { // // اگر در حال اجرا
//            restartTimer(); // // ری‌استارت تایمر با فاصله جدید
//        }
//    }
//
//    public synchronized void start() { // // شروع ساعت
//        if (this.running) { return; } // // اگر قبلاً روشن است کاری نکن
//        this.running = true; // // وضعیت اجرا
//        this.timer = new Timer("SimClock", true); // // تایمر Daemon
//        scheduleTask(); // // زمان‌بندی تسک
//    }
//
//    public synchronized void stop() { // // توقف ساعت
//        if (!this.running) { return; } // // اگر از قبل متوقف است کاری نکن
//        this.running = false; // // وضعیت
//        if (this.timer != null) { // // اگر تایمر داریم
//            try { this.timer.cancel(); } catch (Exception ignore) {} // // لغو تایمر
//            this.timer = null; // // خالی کردن
//        }
//    }
//
//    private synchronized void restartTimer() { // // ری‌استارت با فاصله جدید
//        stop(); // // توقف تایمر فعلی
//        start(); // // شروع مجدد با interval جدید
//    }
//
//    private void scheduleTask() { // // زمان‌بندی تسک دوره‌ای
//        final SimulationClock self = this; // // ارجاع برای کلاس داخلی
//        TimerTask task = new TimerTask() { // // کلاس ناشناس (بدون لامبدا)
//            @Override public void run() { // // اجرای هر تیک
//                self.tick(); // // فراخوانی تیک
//            }
//        };
//        this.timer.scheduleAtFixedRate(task, 0, this.intervalMs); // // اجرای دوره‌ای
//    }
//
//    private void tick() { // // بدنه‌ی هر تیک
//        Updatable[] arr; // // آرایه موقت برای Thread-Safe ساده
//        synchronized (this) { // // قفل برای خواندن لیست
//            arr = this.listeners.toArray(new Updatable[0]); // // کپی ایمن
//        }
//        for (int i = 0; i < arr.length; i++) { // // حلقه روی شنونده‌ها
//            try { arr[i].update(); } // // به‌روزرسانی هرکدام
//            catch (Throwable t) { /* // // جلوگیری از توقف تایمر در خطا */ }
//        }
//    }
//}


























//package simulation; // // پکیج شبیه‌سازی
//
//import java.util.ArrayList; // // لیست آپدیت‌شونده‌ها
//import java.util.List; // // اینترفیس لیست
//import javax.swing.Timer; // // تایمر سوینگ
//import java.awt.event.ActionListener; // // لیسنر رویداد تایمر
//import java.awt.event.ActionEvent; // // رویداد اکشن
//
//public class SimulationClock { // // ساعت شبیه‌سازی
//    private int tickInterval; // // فاصله تیک (میلی‌ثانیه)
//    private final List<Updatable> updatables; // // لیست آبجکت‌های قابل آپدیت
//    private Timer timer; // // تایمر داخلی
//
//    public SimulationClock(int ms) { // // سازنده
//        this.tickInterval = ms; // // ذخیره تیک اولیه
//        this.updatables = new ArrayList<Updatable>(); // // ساخت لیست
//        this.timer = new Timer(tickInterval, new ActionListener() { // // ساخت تایمر با لیسنر
//            @Override public void actionPerformed(ActionEvent e) { // // روی هر تیک
//                for (int i = 0; i < updatables.size(); i++) { // // حلقه روی آپدیت‌شونده‌ها
//                    updatables.get(i).update(); // // آپدیت هر آبجکت
//                }
//            }
//        });
//    }
//
//    public void register(Updatable u) { // // اضافه‌کردن آبجکت به ساعت
//        if (u != null && !updatables.contains(u)) { // // جلوگیری از تکرار
//            updatables.add(u); // // افزودن
//        }
//    }
//
//    public void unregister(Updatable u) { // // حذف از ساعت
//        updatables.remove(u); // // حذف
//    }
//
//    public void start() { // // شروع ساعت
//        if (!timer.isRunning()) timer.start(); // // اگر اجرا نیست، اجرا کن
//    }
//
//    public void stop() { // // توقف ساعت
//        if (timer.isRunning()) timer.stop(); // // اگر اجراست، متوقف کن
//    }
//
//    public void setInterval(int ms) { // // تغییر فاصله تیک
//        if (ms < 5) ms = 5; // // حداقل‌ایمنی
//        this.tickInterval = ms; // // ذخیره
//        this.timer.setDelay(ms); // // اعمال روی تایمر
//    }
//
//    public int getInterval() { // // گرفتن فاصله تیک
//        return tickInterval; // // خروجی
//    }
//}
//








































//package simulation;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
//public class  SimulationClock {
//    private int tickInterval; // به میلی‌ثانیه
//    private List<Updatable> updatables;
//    private Timer timer;
//    private boolean running;
//
//    public SimulationClock(int tickInterval) {
//        this.tickInterval = tickInterval;
//        this.updatables = new ArrayList<>();
//        this.running = false;
//    }
//
//    public void register(Updatable u) {
//        updatables.add(u);
//    }
//
//    public void start() {
//        if (running) return;
//
//        running = true;
//        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                for (Updatable u : updatables) {
//                    u.update();
//                }
//            }
//        }, 0, tickInterval);
//    }
//
//    public void stop() {
//        if (timer != null) {
//            timer.cancel();
//        }
//        running = false;
//    }
//
//    public boolean isRunning() {
//        return running;
//    }
//
//    public int getTickInterval() { // // برگرداندن فاصله زمانی هر تیک (ms)
//        return tickInterval;
//    }
//
//
//    public void setTickInterval(int interval) {
//        this.tickInterval = interval;
//        if (running) {
//            stop();
//            start();
//        }
//    }
//}
