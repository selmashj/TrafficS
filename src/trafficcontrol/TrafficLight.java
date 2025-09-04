package trafficcontrol; // // پکیج کنترل ترافیک

import core.Direction;              // // جهت‌ها
import infrastructure.Intersection; // // تقاطع
import javax.swing.Timer;           // // تایمر سوئینگ
import java.awt.event.ActionEvent;  // // رویداد
import java.awt.event.ActionListener; // // شنونده بدون لامبدا
import java.util.ArrayList;         // // لیست
import java.util.HashMap;           // // مپ
import java.util.List;              // // لیست
import java.util.Map;               // // مپ

/**
 * چراغ راهنمایی همگام برای هر تقاطع.
 * «اصل تقاطع»: (N,S) همیشه هم‌رنگ و (E,W) هم‌رنگ؛ و NS ↔ EW همیشه مخالف‌اند.
 * بین دو فاز، مرحلهٔ همه‌قرمز داریم تا تعارض نشود.
 */
public class TrafficLight extends TrafficSign { // // چراغ راهنمایی

    private static final Map<Intersection, PhaseController> CONTROLLERS =
            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر مشترک

    private final Intersection intersection; // // تقاطع میزبان
    private final Direction direction;       // // جهت این چراغ
    private LightState state = LightState.RED; // // وضعیت فعلی

    private final long greenMs;   // // مدت سبز (ms)
    private final long yellowMs;  // // مدت زرد (ms)
    private final long allRedMs;  // // مدت همه‌قرمز (ms)

    public TrafficLight(Intersection at, Direction dir,
                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
        super("TL@" + at.getId() + ":" + dir, dir); // // شناسه خودکار + جهت
        this.intersection = at;   // // ذخیره تقاطع
        this.direction     = dir; // // ذخیره جهت
        this.greenMs       = Math.max(50, greenMs);   // // حداقل ایمنی
        this.yellowMs      = Math.max(30, yellowMs);  // // حداقل ایمنی
        this.allRedMs      = Math.max(30, allRedMs);  // // حداقل ایمنی
        ensureController().register(this);            // // ثبت در کنترلر تقاطع
    }

    public Intersection getIntersection() { return intersection; } // // گتر تقاطع
    @Override public Direction getDirectionControlled() { return direction; } // // گتر جهت
    public LightState getState()          { return state; } // // گتر وضعیت
    public void setState(LightState s)    { if (s!=null) this.state = s; } // // ست وضعیت (توسط کنترلر)

    public long getGreenMs()  { return greenMs;  } // // گتر زمان‌ها
    public long getYellowMs() { return yellowMs; }
    public long getAllRedMs() { return allRedMs; }

    @Override
    public void update() { /* // // چرخه توسط کنترلر داخلی انجام می‌شود. */ } // // نَو-اُپ

    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر تقاطع
        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
        if (pc == null) {
            pc = new PhaseController(intersection); // // ساخت
            CONTROLLERS.put(intersection, pc);      // // ثبت
        }
        return pc; // // خروجی
    }

    // ---------------- کنترل‌کنندهٔ فاز + تضمین هم‌رنگی/مقابله ----------------
    private static final class PhaseController { // // کنترلر داخلی
        private final Intersection owner; // // تقاطع مالک
        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های N/S
        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های E/W

        private int  phase = 0;      // // ۰=NS فعال، ۱=EW فعال
        private int  stage = 0;      // // ۰=سبز، ۱=زرد، ۲=همه‌قرمز
        private long stageLeft = 0;  // // باقیمانده مرحله فعلی (ms)

        private long gMs   = 1200;   // // مدت سبز پیش‌فرض
        private long yMs   = 600;    // // مدت زرد پیش‌فرض
        private long allMs = 1000;   // // مدت همه‌قرمز پیش‌فرض

        private final Timer driver;  // // تایمر

        PhaseController(Intersection it){
            this.owner = it; // // ذخیره تقاطع
            this.driver = new Timer(100, new ActionListener(){ // // تیک 100ms
                @Override public void actionPerformed(ActionEvent e){ advance(100); } // // جلو
            });
            // شروع امن: همه‌قرمز خیلی کوتاه، سپس سبز فاز ۰ //
            this.stage = 2; this.stageLeft = 1; applyStates(); // // همه‌قرمز
            this.driver.start(); // // شروع تایمر
        }

        void register(TrafficLight tl){ // // ثبت چراغ جدید
            Direction d = tl.getDirectionControlled(); // // جهت
            if (d == Direction.NORTH || d == Direction.SOUTH) { // // گروه NS
                if (ns.isEmpty()) { // // اولین عضو → خواندن زمان‌بندی
                    gMs   = Math.max( tl.getGreenMs(),  100 ); // // حداقل 100 تا دیده شود
                    yMs   = Math.max( tl.getYellowMs(), 100 ); // // حداقل 100ms برای زرد
                    allMs = Math.max( tl.getAllRedMs(), 100 ); // // حداقل 100ms
                    stage = 0; stageLeft = gMs;               // // شروع از سبز
                }
                ns.add(tl); // // افزودن
            } else { // // گروه EW
                if (ew.isEmpty()) {
                    gMs   = Math.max( tl.getGreenMs(),  100 );
                    yMs   = Math.max( tl.getYellowMs(), 100 );
                    allMs = Math.max( tl.getAllRedMs(), 100 );
                    stage = 0; stageLeft = gMs;
                }
                ew.add(tl); // // افزودن
            }
            applyStates(); // // اعمال وضعیت فعلی به همه (تضمین هم‌رنگی)
        }

        private void advance(long addMs){ // // پیشروی زمان با تضمین عدم اسکیپ
            long remain = addMs; // // زمان باقیمانده این تیک
            while (remain > 0) { // // ممکن است چند مرز را رد کنیم
                long take = Math.min(stageLeft, remain); // // مصرف تا مرز بعدی
                stageLeft -= take; remain -= take;       // // به‌روزرسانی

                if (stageLeft == 0) { nextStage(); applyStates(); } // // پایان مرحله → اعمال
            }
        }

        private void nextStage(){ // // ۰→۱→۲→(تغییر فاز)→۰
            if (stage == 0) { stage = 1; stageLeft = yMs; }         // // سبز→زرد
            else if (stage == 1) { stage = 2; stageLeft = allMs; }  // // زرد→همه‌قرمز
            else { phase = 1 - phase; stage = 0; stageLeft = gMs; } // // همه‌قرمز→تغییر فاز→سبز
        }

        private void applyStates(){ // // اعمال دقیق رنگ‌ها (اصل تقاطع)
            if (stage == 0) { // // مرحله سبز
                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
            } else if (stage == 1) { // // مرحله زرد
                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
            } else { // // همه‌قرمز
                setAll(LightState.RED);
            }
            // نکته: هر بار که این متد صدا می‌خورد، هم‌رنگی داخل گروه و مخالفتِ بین گروه‌ها تضمین می‌شود. //
        }

        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
            for (int i=0;i<list.size();i++){ list.get(i).setState(s); } // // هم‌رنگی قطعی
        }

        private void setAll(LightState s){ setGroup(ns, s); setGroup(ew, s); } // // همه یکسان
    }
}
























//package trafficcontrol; // // پکیج کنترل ترافیک
//
//import core.Direction;              // // جهت‌ها
//import infrastructure.Intersection; // // تقاطع
//import javax.swing.Timer;           // // تایمر سوئینگ
//import java.awt.event.ActionEvent;  // // رویداد
//import java.awt.event.ActionListener; // // شنونده بدون لامبدا
//import java.util.ArrayList;         // // لیست
//import java.util.HashMap;           // // مپ
//import java.util.List;              // // لیست
//import java.util.Map;               // // مپ
//
///**
// * چراغ راهنمایی همگام برای هر تقاطع.
// * «اصل تقاطع»: (N,S) همیشه هم‌رنگ و (E,W) هم‌رنگ؛ و NS ↔ EW همیشه مخالف‌اند.
// * بین دو فاز، مرحلهٔ همه‌قرمز داریم تا تعارض نشود.
// */
//public class TrafficLight extends TrafficSign { // // چراغ راهنمایی
//
//    private static final Map<Intersection, PhaseController> CONTROLLERS =
//            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر مشترک
//
//    private final Intersection intersection; // // تقاطع میزبان
//    private final Direction direction;       // // جهت این چراغ
//    private LightState state = LightState.RED; // // وضعیت فعلی
//
//    private final long greenMs;   // // مدت سبز (ms)
//    private final long yellowMs;  // // مدت زرد (ms)
//    private final long allRedMs;  // // مدت همه‌قرمز (ms)
//
//    public TrafficLight(Intersection at, Direction dir,
//                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
//        super("TL@" + at.getId() + ":" + dir, dir); // // شناسه خودکار + جهت
//        this.intersection = at;   // // ذخیره تقاطع
//        this.direction     = dir; // // ذخیره جهت
//        this.greenMs       = Math.max(50, greenMs);   // // حداقل ایمنی
//        this.yellowMs      = Math.max(30, yellowMs);  // // حداقل ایمنی
//        this.allRedMs      = Math.max(30, allRedMs);  // // حداقل ایمنی
//        ensureController().register(this);            // // ثبت در کنترلر تقاطع
//    }
//
//    public Intersection getIntersection() { return intersection; } // // گتر تقاطع
//    @Override public Direction getDirectionControlled() { return direction; } // // گتر جهت
//    public LightState getState()          { return state; } // // گتر وضعیت
//    public void setState(LightState s)    { if (s!=null) this.state = s; } // // ست وضعیت (توسط کنترلر)
//
//    public long getGreenMs()  { return greenMs;  } // // گتر زمان‌ها
//    public long getYellowMs() { return yellowMs; }
//    public long getAllRedMs() { return allRedMs; }
//
//    @Override
//    public void update() { /* // // چرخه توسط کنترلر داخلی انجام می‌شود. */ } // // نَو-اُپ
//
//    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر تقاطع
//        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
//        if (pc == null) {
//            pc = new PhaseController(intersection); // // ساخت
//            CONTROLLERS.put(intersection, pc);      // // ثبت
//        }
//        return pc; // // خروجی
//    }
//
//    // ---------------- کنترل‌کنندهٔ فاز + تضمین هم‌رنگی/مقابله ----------------
//    private static final class PhaseController { // // کنترلر داخلی
//        private final Intersection owner; // // تقاطع مالک
//        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های N/S
//        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های E/W
//
//        private int  phase = 0;      // // ۰=NS فعال، ۱=EW فعال
//        private int  stage = 0;      // // ۰=سبز، ۱=زرد، ۲=همه‌قرمز
//        private long stageLeft = 0;  // // باقیمانده مرحله فعلی (ms)
//
//        private long gMs   = 1200;   // // مدت سبز پیش‌فرض
//        private long yMs   = 600;    // // مدت زرد پیش‌فرض
//        private long allMs = 1000;   // // مدت همه‌قرمز پیش‌فرض
//
//        private final Timer driver;  // // تایمر
//
//        PhaseController(Intersection it){
//            this.owner = it; // // ذخیره تقاطع
//            this.driver = new Timer(100, new ActionListener(){ // // تیک 100ms
//                @Override public void actionPerformed(ActionEvent e){ advance(100); } // // جلو
//            });
//            // شروع امن: همه‌قرمز خیلی کوتاه، سپس سبز فاز ۰ //
//            this.stage = 2; this.stageLeft = 1; applyStates(); // // همه‌قرمز
//            this.driver.start(); // // شروع تایمر
//        }
//
//        void register(TrafficLight tl){ // // ثبت چراغ جدید
//            Direction d = tl.getDirectionControlled(); // // جهت
//            if (d == Direction.NORTH || d == Direction.SOUTH) { // // گروه NS
//                if (ns.isEmpty()) { // // اولین عضو → خواندن زمان‌بندی
//                    gMs   = Math.max( tl.getGreenMs(),  100 ); // // حداقل 100 تا دیده شود
//                    yMs   = Math.max( tl.getYellowMs(), 100 ); // // حداقل 100ms برای زرد
//                    allMs = Math.max( tl.getAllRedMs(), 100 ); // // حداقل 100ms
//                    stage = 0; stageLeft = gMs;               // // شروع از سبز
//                }
//                ns.add(tl); // // افزودن
//            } else { // // گروه EW
//                if (ew.isEmpty()) {
//                    gMs   = Math.max( tl.getGreenMs(),  100 );
//                    yMs   = Math.max( tl.getYellowMs(), 100 );
//                    allMs = Math.max( tl.getAllRedMs(), 100 );
//                    stage = 0; stageLeft = gMs;
//                }
//                ew.add(tl); // // افزودن
//            }
//            applyStates(); // // اعمال وضعیت فعلی به همه (تضمین هم‌رنگی)
//        }
//
//        private void advance(long addMs){ // // پیشروی زمان با تضمین عدم اسکیپ
//            long remain = addMs; // // زمان باقیمانده این تیک
//            while (remain > 0) { // // ممکن است چند مرز را رد کنیم
//                long take = Math.min(stageLeft, remain); // // مصرف تا مرز بعدی
//                stageLeft -= take; remain -= take;       // // به‌روزرسانی
//
//                if (stageLeft == 0) { nextStage(); applyStates(); } // // پایان مرحله → اعمال
//            }
//        }
//
//        private void nextStage(){ // // ۰→۱→۲→(تغییر فاز)→۰
//            if (stage == 0) { stage = 1; stageLeft = yMs; }         // // سبز→زرد
//            else if (stage == 1) { stage = 2; stageLeft = allMs; }  // // زرد→همه‌قرمز
//            else { phase = 1 - phase; stage = 0; stageLeft = gMs; } // // همه‌قرمز→تغییر فاز→سبز
//        }
//
//        private void applyStates(){ // // اعمال دقیق رنگ‌ها (اصل تقاطع)
//            if (stage == 0) { // // مرحله سبز
//                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
//                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
//            } else if (stage == 1) { // // مرحله زرد
//                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
//                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
//            } else { // // همه‌قرمز
//                setAll(LightState.RED);
//            }
//            // نکته: هر بار که این متد صدا می‌خورد، هم‌رنگی داخل گروه و مخالفتِ بین گروه‌ها تضمین می‌شود. //
//        }
//
//        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
//            for (int i=0;i<list.size();i++){ list.get(i).setState(s); } // // هم‌رنگی قطعی
//        }
//
//        private void setAll(LightState s){ setGroup(ns, s); setGroup(ew, s); } // // همه یکسان
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
////package trafficcontrol; // // پکیج کنترل ترافیک
////
////import core.Direction;              // // جهت‌ها
////import infrastructure.Intersection; // // تقاطع
////import javax.swing.Timer;           // // تایمر سوئینگ
////import java.awt.event.ActionEvent;  // // رویداد
////import java.awt.event.ActionListener; // // شنونده بدون لامبدا
////import java.util.ArrayList;         // // لیست
////import java.util.HashMap;           // // مپ
////import java.util.List;              // // لیست
////import java.util.Map;               // // مپ
////
/////**
//// * چراغ راهنمایی همگام برای هر تقاطع.
//// * «اصل تقاطع»: (N,S) همیشه هم‌رنگ و (E,W) هم‌رنگ؛ و NS ↔ EW همیشه مخالف‌اند.
//// * بین دو فاز، مرحلهٔ همه‌قرمز داریم تا تعارض نشود.
//// */
////public class TrafficLight extends TrafficSign { // // چراغ راهنمایی
////
////    private static final Map<Intersection, PhaseController> CONTROLLERS =
////            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر مشترک
////
////    private final Intersection intersection; // // تقاطع میزبان
////    private final Direction direction;       // // جهت این چراغ
////    private LightState state = LightState.RED; // // وضعیت فعلی
////
////    private final long greenMs;   // // مدت سبز (ms)
////    private final long yellowMs;  // // مدت زرد (ms)
////    private final long allRedMs;  // // مدت همه‌قرمز (ms)
////
////    public TrafficLight(Intersection at, Direction dir,
////                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
////        super("TL@" + at.getId() + ":" + dir, dir); // // شناسه خودکار + جهت
////        this.intersection = at;   // // ذخیره تقاطع
////        this.direction     = dir; // // ذخیره جهت
////        this.greenMs       = Math.max(50, greenMs);   // // حداقل ایمنی
////        this.yellowMs      = Math.max(30, yellowMs);  // // حداقل ایمنی
////        this.allRedMs      = Math.max(30, allRedMs);  // // حداقل ایمنی
////        ensureController().register(this);            // // ثبت در کنترلر تقاطع
////    }
////
////    public Intersection getIntersection() { return intersection; } // // گتر تقاطع
////    @Override public Direction getDirectionControlled() { return direction; } // // گتر جهت
////    public LightState getState()          { return state; } // // گتر وضعیت
////    public void setState(LightState s)    { if (s!=null) this.state = s; } // // ست وضعیت (توسط کنترلر)
////
////    public long getGreenMs()  { return greenMs;  } // // گتر زمان‌ها
////    public long getYellowMs() { return yellowMs; }
////    public long getAllRedMs() { return allRedMs; }
////
////    @Override
////    public void update() { /* // // چرخه توسط کنترلر داخلی انجام می‌شود. */ } // // نَو-اُپ
////
////    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر تقاطع
////        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
////        if (pc == null) {
////            pc = new PhaseController(intersection); // // ساخت
////            CONTROLLERS.put(intersection, pc);      // // ثبت
////        }
////        return pc; // // خروجی
////    }
////
////    // ---------------- کنترل‌کنندهٔ فاز + تضمین هم‌رنگی/مقابله ----------------
////    private static final class PhaseController { // // کنترلر داخلی
////        private final Intersection owner; // // تقاطع مالک
////        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های N/S
////        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های E/W
////
////        private int  phase = 0;      // // ۰=NS فعال، ۱=EW فعال
////        private int  stage = 0;      // // ۰=سبز، ۱=زرد، ۲=همه‌قرمز
////        private long stageLeft = 0;  // // باقیمانده مرحله فعلی (ms)
////
////        private long gMs   = 1200;   // // مدت سبز پیش‌فرض
////        private long yMs   = 600;    // // مدت زرد پیش‌فرض
////        private long allMs = 1000;   // // مدت همه‌قرمز پیش‌فرض
////
////        private final Timer driver;  // // تایمر
////
////        PhaseController(Intersection it){
////            this.owner = it; // // ذخیره تقاطع
////            this.driver = new Timer(100, new ActionListener(){ // // تیک 100ms
////                @Override public void actionPerformed(ActionEvent e){ advance(100); } // // جلو
////            });
////            // شروع امن: همه‌قرمز خیلی کوتاه، سپس سبز فاز ۰ //
////            this.stage = 2; this.stageLeft = 1; applyStates(); // // همه‌قرمز
////            this.driver.start(); // // شروع تایمر
////        }
////
////        void register(TrafficLight tl){ // // ثبت چراغ جدید
////            Direction d = tl.getDirectionControlled(); // // جهت
////            if (d == Direction.NORTH || d == Direction.SOUTH) { // // گروه NS
////                if (ns.isEmpty()) { // // اولین عضو → خواندن زمان‌بندی
////                    gMs   = Math.max( tl.getGreenMs(),  100 ); // // حداقل 100 تا دیده شود
////                    yMs   = Math.max( tl.getYellowMs(), 100 ); // // حداقل 100ms برای زرد
////                    allMs = Math.max( tl.getAllRedMs(), 100 ); // // حداقل 100ms
////                    stage = 0; stageLeft = gMs;               // // شروع از سبز
////                }
////                ns.add(tl); // // افزودن
////            } else { // // گروه EW
////                if (ew.isEmpty()) {
////                    gMs   = Math.max( tl.getGreenMs(),  100 );
////                    yMs   = Math.max( tl.getYellowMs(), 100 );
////                    allMs = Math.max( tl.getAllRedMs(), 100 );
////                    stage = 0; stageLeft = gMs;
////                }
////                ew.add(tl); // // افزودن
////            }
////            applyStates(); // // اعمال وضعیت فعلی به همه (تضمین هم‌رنگی)
////        }
////
////        private void advance(long addMs){ // // پیشروی زمان با تضمین عدم اسکیپ
////            long remain = addMs; // // زمان باقیمانده این تیک
////            while (remain > 0) { // // ممکن است چند مرز را رد کنیم
////                long take = Math.min(stageLeft, remain); // // مصرف تا مرز بعدی
////                stageLeft -= take; remain -= take;       // // به‌روزرسانی
////
////                if (stageLeft == 0) { nextStage(); applyStates(); } // // پایان مرحله → اعمال
////            }
////        }
////
////        private void nextStage(){ // // ۰→۱→۲→(تغییر فاز)→۰
////            if (stage == 0) { stage = 1; stageLeft = yMs; }         // // سبز→زرد
////            else if (stage == 1) { stage = 2; stageLeft = allMs; }  // // زرد→همه‌قرمز
////            else { phase = 1 - phase; stage = 0; stageLeft = gMs; } // // همه‌قرمز→تغییر فاز→سبز
////        }
////
////        private void applyStates(){ // // اعمال دقیق رنگ‌ها (اصل تقاطع)
////            if (stage == 0) { // // مرحله سبز
////                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
////                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
////            } else if (stage == 1) { // // مرحله زرد
////                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
////                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
////            } else { // // همه‌قرمز
////                setAll(LightState.RED);
////            }
////            // نکته: هر بار که این متد صدا می‌خورد، هم‌رنگی داخل گروه و مخالفتِ بین گروه‌ها تضمین می‌شود. //
////        }
////
////        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
////            for (int i=0;i<list.size();i++){ list.get(i).setState(s); } // // هم‌رنگی قطعی
////        }
////
////        private void setAll(LightState s){ setGroup(ns, s); setGroup(ew, s); } // // همه یکسان
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
//
//
//
//
//
//
////package trafficcontrol; // // پکیج کنترل ترافیک
////
////import core.Direction;              // // جهت‌ها
////import infrastructure.Intersection; // // تقاطع
////import javax.swing.Timer;           // // تایمر سوئینگ
////import java.awt.event.ActionEvent;  // // رویداد
////import java.awt.event.ActionListener; // // شنونده بدون لامبدا
////import java.util.ArrayList;         // // لیست
////import java.util.HashMap;           // // مپ
////import java.util.List;              // // لیست
////import java.util.Map;               // // مپ
////
////public class TrafficLight extends TrafficSign { // // چراغ راهنمایی
////
////    private static final Map<Intersection, PhaseController> CONTROLLERS =
////            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر
////
////    private final Intersection intersection; // // تقاطع میزبان
////    private final Direction direction;       // // جهت این رویکرد
////    private LightState state = LightState.RED; // // وضعیت فعلی چراغ
////
////    private final long greenMs;   // // زمان سبز(ms)
////    private final long yellowMs;  // // زمان زرد(ms)
////    private final long allRedMs;  // // زمان همه‌قرمز(ms)
////
////    public TrafficLight(Intersection at, Direction dir,
////                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
////        super("TL@" + at.getId() + ":" + dir, dir); // // شناسهٔ خودکار
////        this.intersection = at;   // // ذخیره تقاطع
////        this.direction     = dir; // // ذخیره جهت
////        this.greenMs       = Math.max(50, greenMs);   // // حداقل ایمن
////        this.yellowMs      = Math.max(30, yellowMs);  // // حداقل ایمن
////        this.allRedMs      = Math.max(30, allRedMs);  // // حداقل ایمن
////        ensureController().register(this);            // // ثبت در کنترلر تقاطع
////    }
////
////    public Intersection getIntersection() { return intersection; } // // گتر تقاطع
////    @Override public Direction getDirectionControlled() { return direction; } // // گتر جهت
////    public LightState getState()          { return state; } // // گتر وضعیت
////    public void setState(LightState s)    { if (s!=null) this.state = s; } // // ست وضعیت
////
////    public long getGreenMs()  { return greenMs;  } // // گتر زمان‌ها
////    public long getYellowMs() { return yellowMs; }
////    public long getAllRedMs() { return allRedMs; }
////
////    @Override
////    public void update() {
////        // چرخه توسط کنترلر داخلی مدیریت می‌شود. //
////    }
////
////    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر تقاطع
////        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
////        if (pc == null) {
////            pc = new PhaseController(intersection); // // ساخت
////            CONTROLLERS.put(intersection, pc);      // // ثبت
////        }
////        return pc; // // خروجی
////    }
////
////    // ---------------- کنترل‌کنندهٔ فاز (به‌روز شده) ----------------
////    private static final class PhaseController { // // کنترلر داخلی
////        private final Intersection owner; // // تقاطع مالک
////        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های N/S
////        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های E/W
////
////        private int phase = 0;      // // ۰=NS فعال، ۱=EW فعال
////        private int stage = 0;      // // ۰=سبز، ۱=زرد، ۲=همه‌قرمز
////        private long stageLeft = 0; // // زمان باقیمانده مرحله فعلی (ms)
////
////        private long gMs = 1200;    // // پیش‌فرض سبز (ms)
////        private long yMs = 600;     // // پیش‌فرض زرد (ms)
////        private long rAllMs = 1000; // // پیش‌فرض همه‌قرمز (ms)
////
////        private final Timer driver; // // تایمر داخلی
////
////        PhaseController(Intersection it){
////            this.owner = it; // // ذخیره تقاطع
////            this.driver = new Timer(100, new ActionListener(){ // // تیک 100ms
////                @Override public void actionPerformed(ActionEvent e){
////                    advance(100); // // جلو بردن 100ms
////                }
////            });
////            // شروع در حالت امن: همه قرمز یک لحظه، سپس به سبزِ فاز ۰ //
////            this.stage = 2; this.stageLeft = 1; applyStates(); // // یک ms همه‌قرمز
////            this.driver.start(); // // شروع تایمر
////        }
////
////        void register(TrafficLight tl){ // // ثبت چراغ جدید
////            Direction d = tl.getDirectionControlled(); // // جهت
////            if (d == Direction.NORTH || d == Direction.SOUTH) {
////                if (ns.isEmpty()) { // // اولین NS → خواندن زمان‌بندی
////                    gMs   = Math.max( tl.getGreenMs(),  100 ); // // حداقل 100 تا با تیک 100ms قابل مشاهده باشد
////                    yMs   = Math.max( tl.getYellowMs(), 100 ); // // حداقل 100ms برای دیده شدن زرد
////                    rAllMs= Math.max( tl.getAllRedMs(),100 );  // // حداقل 100ms
////                    stage = 0; stageLeft = gMs;               // // شروع از سبزِ فاز ۰
////                }
////                ns.add(tl); // // افزودن
////            } else {
////                if (ew.isEmpty()) {
////                    gMs   = Math.max( tl.getGreenMs(),  100 ); // // همان حداقل‌ها
////                    yMs   = Math.max( tl.getYellowMs(), 100 );
////                    rAllMs= Math.max( tl.getAllRedMs(),100 );
////                    stage = 0; stageLeft = gMs;               // // شروع از سبزِ فاز ۰
////                }
////                ew.add(tl); // // افزودن
////            }
////            applyStates(); // // اعمال وضعیت فعلی به همه
////        }
////
////        private void advance(long addMs){ // // پیشروی مطمئن (بدون اسکیپ مرحله)
////            long remain = addMs; // // مقدار باقی‌مانده از این تیک
////            while (remain > 0) { // // ممکن است چند مرحله را در یک تیک عبور دهیم
////                long take = Math.min(stageLeft, remain); // // تا مرز بعدی
////                stageLeft -= take; // // کم‌کردن از مرحله
////                remain    -= take; // // کم‌کردن از تیک
////
////                if (stageLeft == 0) { // // مرحلهٔ فعلی تمام شد → رفتن به بعدی
////                    nextStage();      // // تعویض مرحله (و احتمالاً فاز)
////                    applyStates();    // // اعمال رنگ‌ها
////                } else {
////                    // هنوز در همین مرحله هستیم؛ رنگ‌ها تغییری ندارند. //
////                }
////            }
////        }
////
////        private void nextStage(){ // // سوییچ مرحله (۰→۱→۲→۰ ...)
////            if (stage == 0) {          // // سبز → زرد
////                stage = 1; stageLeft = yMs; // // مدت زرد
////            } else if (stage == 1) {   // // زرد → همه‌قرمز
////                stage = 2; stageLeft = rAllMs; // // مدت همه‌قرمز
////            } else {                    // // همه‌قرمز → تغییر فاز و شروع سبز
////                phase = 1 - phase;      // // تغییر فاز (NS↔EW)
////                stage = 0; stageLeft = gMs; // // شروع سبز فاز جدید
////            }
////        }
////
////        private void applyStates(){ // // ست رنگ‌ها به‌تناسب فاز و مرحله
////            if (stage == 0) { // // سبز
////                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
////                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
////            } else if (stage == 1) { // // زرد
////                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
////                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
////            } else { // // همه‌قرمز
////                setAll(LightState.RED);
////            }
////        }
////
////        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
////            for (int i=0;i<list.size();i++){ list.get(i).setState(s); } // // حلقه
////        }
////
////        private void setAll(LightState s){ // // اعمال حالت به هر دو گروه
////            setGroup(ns, s); setGroup(ew, s);
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
//////package trafficcontrol; // // پکیج کنترل ترافیک
//////
//////import core.Direction;              // // جهت‌ها
//////import infrastructure.Intersection; // // تقاطع
//////import javax.swing.Timer;           // // تایمر سوئینگ
//////import java.awt.event.ActionEvent;  // // رویداد
//////import java.awt.event.ActionListener; // // شنونده بدون لامبدا
//////import java.util.ArrayList;         // // لیست
//////import java.util.HashMap;           // // مپ
//////import java.util.List;              // // لیست
//////import java.util.Map;               // // مپ
//////
///////**
////// * چراغ راهنمایی همگام‌شونده برای هر تقاطع //
////// * یک کنترلر داخلی (PhaseController) دارد که تمام چراغ‌های یک تقاطع را با هم مدیریت می‌کند.
////// * امضا دقیقاً همان است که DemoTraffic صدا می‌زند: (Intersection, Direction, long, long, long)
////// */
//////public class TrafficLight extends TrafficSign { // // چراغ راهنمایی (از TrafficSign)
//////
//////    private static final Map<Intersection, PhaseController> CONTROLLERS =
//////            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر مشترک
//////
//////    private final Intersection intersection; // // تقاطع میزبان
//////    private final Direction direction;       // // جهت این رویکرد
//////    private LightState state = LightState.RED; // // وضعیت فعلی چراغ
//////
//////    private final long greenMs;   // // مدت سبز (ms)
//////    private final long yellowMs;  // // مدت زرد (ms)
//////    private final long allRedMs;  // // مدت همه‌قرمز (ms)
//////
//////    // ----------- سازنده -----------
//////    public TrafficLight(Intersection at, Direction dir,
//////                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
//////        super("TL@" + at.getId() + ":" + dir, dir); // // شناسهٔ خودکار + ذخیره جهت در پایه
//////        this.intersection = at;   // // ست تقاطع
//////        this.direction     = dir; // // ست جهت
//////        this.greenMs       = Math.max(50, greenMs);   // // حداقل ایمن
//////        this.yellowMs      = Math.max(30, yellowMs);  // // حداقل ایمن
//////        this.allRedMs      = Math.max(30, allRedMs);  // // حداقل ایمن
//////        ensureController().register(this);            // // ثبت در کنترلر تقاطع
//////    }
//////
//////    public Intersection getIntersection() { return intersection; } // // گتر تقاطع
//////    @Override public Direction getDirectionControlled() { return direction; } // // گتر جهت
//////    public LightState getState()          { return state;        } // // گتر وضعیت
//////    public void setState(LightState s)    { if (s!=null) this.state = s; } // // ست وضعیت (توسط کنترلر)
//////
//////    public long getGreenMs()  { return greenMs;  } // // گتر زمان‌ها
//////    public long getYellowMs() { return yellowMs; }
//////    public long getAllRedMs() { return allRedMs; }
//////
//////    @Override
//////    public void update() {
//////        // چرخه توسط PhaseController اجرا می‌شود؛ اینجا نَو-اُپ باقی می‌ماند. //
//////    }
//////
//////    // گرفتن/ساخت کنترلر آن تقاطع //
//////    private PhaseController ensureController() {
//////        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
//////        if (pc == null) {
//////            pc = new PhaseController(intersection); // // ساخت کنترلر جدید
//////            CONTROLLERS.put(intersection, pc);      // // ثبت
//////        }
//////        return pc; // // خروجی
//////    }
//////
//////    /**
//////     * کنترل‌کنندهٔ فاز برای یک تقاطع:
//////     * فاز ۰ = NS فعال، فاز ۱ = EW فعال. بین فازها دورهٔ allRed اعمال می‌شود.
//////     * یک Timer داخلی 100ms زمان را جلو می‌برد و وضعیت چراغ‌ها را ست می‌کند.
//////     */
//////    private static final class PhaseController { // // کنترلر داخلی
//////        private final Intersection owner; // // تقاطع مالک
//////        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های N/S
//////        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های E/W
//////
//////        private int  phase = 0; // // ۰=NS فعال، ۱=EW فعال
//////        private long t     = 0; // // زمان سپری‌شده در فاز فعلی (ms)
//////
//////        // زمان‌های لحظه‌ای (از اولین چراغ هر گروه خوانده می‌شود) //
//////        private long greenMs  = 120; // // پیش‌فرض‌های امن
//////        private long yellowMs = 30;
//////        private long allRedMs = 110;
//////
//////        private final Timer driver; // // تایمر داخلی
//////
//////        PhaseController(Intersection it){
//////            this.owner = it; // // ذخیره تقاطع
//////            this.driver = new Timer(100, new ActionListener() { // // هر 100ms
//////                @Override
//////                public void actionPerformed(ActionEvent e) { // // هر تیک تایمر
//////                    advance(100); // // پیشروی زمان و تعیین وضعیت‌ها
//////                }
//////            });
//////            this.driver.start(); // // شروع تایمر به‌محض ساخت کنترلر
//////        }
//////
//////        void register(TrafficLight tl){ // // ثبت چراغ جدید در گروه NS/EW
//////            Direction d = tl.getDirectionControlled(); // // جهت چراغ
//////            if (d == Direction.NORTH || d == Direction.SOUTH) { // // گروه NS
//////                if (ns.isEmpty()) { // // اولین NS → زمان‌بندی را از آن بردار
//////                    greenMs  = tl.getGreenMs();
//////                    yellowMs = tl.getYellowMs();
//////                    allRedMs = tl.getAllRedMs();
//////                }
//////                ns.add(tl); // // افزودن چراغ به NS
//////            } else { // // گروه EW
//////                if (ew.isEmpty()) {
//////                    greenMs  = tl.getGreenMs();
//////                    yellowMs = tl.getYellowMs();
//////                    allRedMs = tl.getAllRedMs();
//////                }
//////                ew.add(tl); // // افزودن چراغ به EW
//////            }
//////            setAll(LightState.RED); // // شروع امن: همه قرمز
//////        }
//////
//////        private void advance(long addMs){ // // جلو بردن زمان و اعمال حالت‌ها
//////            if (ns.isEmpty() && ew.isEmpty()) return; // // اگر چراغی نیست
//////
//////            long spanG = greenMs;             // // انتهای بازه سبز
//////            long spanY = spanG + yellowMs;    // // انتهای بازه زرد
//////            long spanR = spanY + allRedMs;    // // انتهای بازه همه‌قرمز
//////
//////            t += addMs; // // جمع زمانِ سپری شده در فاز فعلی
//////
//////            if (t <= spanG) { // // بازهٔ سبز
//////                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
//////                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
//////            } else if (t <= spanY) { // // بازهٔ زرد
//////                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
//////                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
//////            } else if (t <= spanR) { // // بازهٔ همه‌قرمز
//////                setAll(LightState.RED);
//////            } else { // // پایان فاز و سوییچ
//////                phase = 1 - phase; // // NS ↔ EW
//////                t = 0;             // // ریست شمارنده فاز
//////            }
//////        }
//////
//////        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به لیست
//////            for (int i=0;i<list.size();i++){ list.get(i).setState(s); } // // حلقه ساده
//////        }
//////
//////        private void setAll(LightState s){ // // اعمال حالت به همه چراغ‌ها
//////            setGroup(ns, s); setGroup(ew, s); // // هر دو گروه
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
////////
////////// trafficcontrol/TrafficLight.java
////////package trafficcontrol;                // // پکیج
////////
////////import core.Direction;                 // // جهت
////////import infrastructure.Intersection;    // // تقاطع
////////
/////////**
//////// * نسخهٔ همگام با سناریوی شما:
//////// * سازنده: (Intersection, Direction, long greenMs, long yellowMs, long allRedMs)
//////// * World از طریق setControl روی Intersection به آن دسترسی دارد.
//////// */
////////public class TrafficLight extends TrafficSign {      // // چراغ راهنمایی
////////    private final Intersection intersection;         // // تقاطع میزبان
////////    private LightState state = LightState.RED;       // // وضعیت فعلی
////////    private final long greenMs;                      // // زمان سبز (ms یا تیک)
////////    private final long yellowMs;                     // // زمان زرد
////////    private final long allRedMs;                     // // زمان همه‌قرمز
////////
////////    public TrafficLight(Intersection at, Direction dir, long greenMs, long yellowMs, long allRedMs) { // // سازنده
////////        super("TL@" + at.getId() + ":" + dir, dir); // // شناسهٔ خودکار + ست جهت
////////        this.intersection = at;                     // // ذخیره تقاطع
////////        this.greenMs = greenMs;                     // // ست
////////        this.yellowMs = yellowMs;                   // // ست
////////        this.allRedMs = allRedMs;                   // // ست
////////    }
////////
////////    public Intersection getIntersection(){ return intersection; } // // گتر تقاطع
////////    public LightState getState(){ return state; }                 // // گتر وضعیت
////////    public void setState(LightState s){ if (s!=null) this.state = s; } // // ست وضعیت
////////
////////    public long getGreenMs(){ return greenMs; }     // // گتر سبز
////////    public long getYellowMs(){ return yellowMs; }   // // گتر زرد
////////    public long getAllRedMs(){ return allRedMs; }   // // گتر همه‌قرمز
////////
////////    @Override
////////    public void update() { /* // // این نسخه چرخهٔ داخلی تایمردار ندارد؛ World مدیریت می‌کند */ } // // نَو-اُپ
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
////////package trafficcontrol; // // پکیج کنترل ترافیک
////////
////////import core.Direction;              // // جهت‌ها
////////import core.Vehicle;                // // خودرو
////////import infrastructure.Intersection; // // تقاطع
////////import javax.swing.Timer;           // // تایمر سوئینگ
////////import java.awt.event.ActionEvent;  // // رویداد
////////import java.awt.event.ActionListener; // // شنونده بدون لامبدا
////////import java.util.ArrayList;         // // لیست
////////import java.util.HashMap;           // // مپ
////////import java.util.List;              // // لیست
////////import java.util.Map;               // // مپ
////////
////////public class TrafficLight extends TrafficSign { // // چراغ راهنمایی
////////
////////    private static final Map<Intersection, PhaseController> CONTROLLERS =
////////            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر
////////
////////    private final Intersection intersection; // // تقاطع میزبان
////////    private final Direction direction;       // // جهت این رویکرد
////////    private LightState state = LightState.RED; // // وضعیت فعلی چراغ
////////
////////    private final long greenMs;   // // زمان سبز(ms)
////////    private final long yellowMs;  // // زمان زرد(ms)
////////    private final long allRedMs;  // // زمان همه‌قرمز(ms)
////////
////////    public TrafficLight(Intersection at, Direction dir,
////////                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
////////        super();                         // // سازندهٔ والد (بدون پارامتر)
////////        this.intersection = at;          // // ذخیره تقاطع
////////        this.direction     = dir;        // // ذخیره جهت
////////        this.greenMs       = greenMs;    // // زمان سبز
////////        this.yellowMs      = yellowMs;   // // زمان زرد
////////        this.allRedMs      = allRedMs;   // // زمان همه‌قرمز
////////
////////        this.id = "TL-" + at.getId() + "-" + dir; // // تولید ID یکتا
////////        this.directionControlled = dir;           // // ست جهت کنترل‌شونده
////////
////////        ensureController().register(this); // // ثبت در کنترلر تقاطع
////////    }
////////
////////    public Intersection getIntersection() { return intersection; } // // دسترسی به تقاطع
////////    public Direction getDirection()       { return direction;    } // // دسترسی به جهت
////////    public LightState getState()          { return state;        } // // خواندن وضعیت
////////    public void setState(LightState s)    { this.state = s;      } // // تنظیم وضعیت
////////
////////    public long getGreenMs()  { return greenMs;  } // // زمان سبز
////////    public long getYellowMs() { return yellowMs; } // // زمان زرد
////////    public long getAllRedMs() { return allRedMs; } // // زمان همه‌قرمز
////////
////////    @Override
////////    public boolean canProceed(Vehicle v) { // // اجازه عبور
////////        return state == LightState.GREEN; // // فقط در سبز
////////    }
////////
////////    @Override
////////    protected void onUpdate() {
////////        // // منطق زمانی در PhaseController با Timer انجام می‌شود.
////////    }
////////
////////    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر تقاطع
////////        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
////////        if (pc == null) { pc = new PhaseController(intersection); CONTROLLERS.put(intersection, pc); } // // ساخت/ثبت
////////        return pc; // // خروجی
////////    }
////////
////////    // ===== کنترلر فاز NS/EW برای هر تقاطع =====
////////    private static final class PhaseController { // // کنترلر داخلی
////////        private final Intersection owner; // // مالک
////////        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های شمال-جنوب
////////        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های شرق-غرب
////////
////////        private int  phase = 0; // // ۰=NS فعال، ۱=EW فعال
////////        private long t     = 0; // // زمان سپری‌شده (ms)
////////
////////        private long greenMs  = 10000; // // پیش‌فرض
////////        private long yellowMs = 3000;  // // پیش‌فرض
////////        private long allRedMs = 1000;  // // پیش‌فرض
////////
////////        private final Timer driver; // // تایمر داخلی
////////
////////        PhaseController(Intersection it){
////////            this.owner = it; // // ذخیره تقاطع
////////            this.driver = new Timer(100, new ActionListener() { // // تایمر 100ms
////////                @Override public void actionPerformed(ActionEvent e) { advance(100); } // // هر تیک
////////            });
////////            this.driver.start(); // // شروع
////////        }
////////
////////        void register(TrafficLight tl){ // // ثبت چراغ جدید
////////            Direction d = tl.getDirection(); // // جهت چراغ
////////            if (d == Direction.NORTH || d == Direction.SOUTH) { // // گروه NS
////////                if (ns.isEmpty()) { greenMs=tl.getGreenMs(); yellowMs=tl.getYellowMs(); allRedMs=tl.getAllRedMs(); } // // خواندن زمان‌ها
////////                ns.add(tl); // // افزودن
////////            } else { // // گروه EW
////////                if (ew.isEmpty()) { greenMs=tl.getGreenMs(); yellowMs=tl.getYellowMs(); allRedMs=tl.getAllRedMs(); } // // خواندن زمان‌ها
////////                ew.add(tl); // // افزودن
////////            }
////////            setAll(LightState.RED); // // شروع امن
////////        }
////////
////////        private void advance(long addMs){ // // پیشروی زمان فاز
////////            if (ns.isEmpty() && ew.isEmpty()) return; // // اگر چراغی نیست
////////
////////            long spanG = greenMs;          // // انتهای سبز
////////            long spanY = spanG + yellowMs; // // انتهای زرد
////////            long spanR = spanY + allRedMs; // // انتهای همه‌قرمز
////////
////////            t += addMs; // // جمع زمان
////////
////////            if (t <= spanG) { // // بازه سبز
////////                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
////////                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
////////            } else if (t <= spanY) { // // بازه زرد
////////                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
////////                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
////////            } else if (t <= spanR) { // // بازه همه‌قرمز
////////                setAll(LightState.RED);
////////            } else { // // سویچ فاز
////////                phase = 1 - phase; // // NS ↔ EW
////////                t = 0;             // // ریست
////////            }
////////        }
////////
////////        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
////////            for (int i=0;i<list.size();i++){ list.get(i).setState(s); } // // حلقه ساده
////////        }
////////        private void setAll(LightState s){ setGroup(ns, s); setGroup(ew, s); } // // اعمال به همه
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
//////////package trafficcontrol; // // پکیج کنترل ترافیک
//////////
//////////import core.Direction;              // // جهت‌ها
//////////import infrastructure.Intersection; // // تقاطع
//////////import javax.swing.Timer;           // // تایمر سوئینگ
//////////import java.awt.event.ActionEvent;  // // رویداد
//////////import java.awt.event.ActionListener; // // شنونده بدون لامبدا
//////////import java.util.ArrayList;         // // لیست
//////////import java.util.HashMap;           // // مپ
//////////import java.util.List;              // // لیست
//////////import java.util.Map;               // // مپ
//////////
///////////**
////////// * چراغ راهنمایی همگام‌شونده در سطح هر تقاطع //
////////// * از enum سطح-بالا trafficcontrol.LightState استفاده می‌کند (نه enum داخلی). //
////////// */
//////////public class TrafficLight extends TrafficSign { // // چراغ راهنمایی
//////////
//////////    // ***** توجه: دیگر enum داخلی نداریم؛ از همین استفاده می‌کنیم: trafficcontrol.LightState *****
//////////
//////////    private static final Map<Intersection, PhaseController> CONTROLLERS =
//////////            new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع→کنترلر
//////////
//////////    private final Intersection intersection; // // تقاطع میزبان
//////////    private final Direction direction;       // // جهت این رویکرد
//////////    private LightState state = LightState.RED; // // وضعیت فعلی چراغ
//////////
//////////    private final long greenMs;   // // زمان سبز(ms)
//////////    private final long yellowMs;  // // زمان زرد(ms)
//////////    private final long allRedMs;  // // زمان همه‌قرمز(ms)
//////////
//////////    public TrafficLight(Intersection at, Direction dir,
//////////                        long greenMs, long yellowMs, long allRedMs) { // // سازنده
//////////        this.intersection = at;   // // ذخیره تقاطع
//////////        this.direction     = dir; // // ذخیره جهت
//////////        this.greenMs       = greenMs;   // // زمان سبز
//////////        this.yellowMs      = yellowMs;  // // زمان زرد
//////////        this.allRedMs      = allRedMs;  // // زمان همه‌قرمز
//////////        ensureController().register(this); // // ثبت در کنترلر تقاطع
//////////    }
//////////
//////////    public Intersection getIntersection() { return intersection; } // // دسترسی به تقاطع
//////////    public Direction getDirection()       { return direction;    } // // دسترسی به جهت
//////////    public LightState getState()          { return state;        } // // خواندن وضعیت
//////////    public void setState(LightState s)    { this.state = s;      } // // تنظیم وضعیت
//////////
//////////    public long getGreenMs()  { return greenMs;  } // // زمان سبز
//////////    public long getYellowMs() { return yellowMs; } // // زمان زرد
//////////    public long getAllRedMs() { return allRedMs; } // // زمان همه‌قرمز
//////////
//////////    @Override
//////////    protected void onUpdate() {
//////////        // زمان‌بندی در کنترلر داخلیِ تقاطع با Timer انجام می‌شود؛ اینجا کاری نیست. //
//////////    }
//////////
//////////    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر تقاطع
//////////        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
//////////        if (pc == null) {
//////////            pc = new PhaseController(intersection); // // ساخت
//////////            CONTROLLERS.put(intersection, pc); // // ثبت
//////////        }
//////////        return pc; // // خروجی
//////////    }
//////////
//////////    /**
//////////     * کنترل‌کنندهٔ فاز (NS/EW) برای هر تقاطع //
//////////     */
//////////    private static final class PhaseController { // // کنترلر داخلی
//////////        private final Intersection owner; // // تقاطع مالک
//////////        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های شمال-جنوب
//////////        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های شرق-غرب
//////////
//////////        private int  phase = 0; // // ۰=NS فعال، ۱=EW فعال
//////////        private long t     = 0; // // زمان سپری‌شده در فاز فعلی(ms)
//////////
//////////        private long greenMs  = 10000; // // پیش‌فرض
//////////        private long yellowMs = 3000;  // // پیش‌فرض
//////////        private long allRedMs = 1000;  // // پیش‌فرض
//////////
//////////        private final Timer driver; // // تایمر داخلی
//////////
//////////        PhaseController(Intersection it){
//////////            this.owner = it; // // ذخیره تقاطع
//////////            this.driver = new Timer(100, new ActionListener() { // // تایمر 100ms
//////////                @Override
//////////                public void actionPerformed(ActionEvent e) { // // هر تیک
//////////                    advance(100); // // پیشروی 100ms
//////////                }
//////////            });
//////////            this.driver.start(); // // شروع تایمر
//////////        }
//////////
//////////        void register(TrafficLight tl){ // // ثبت چراغ جدید
//////////            Direction d = tl.getDirection(); // // جهت چراغ
//////////            if (d == Direction.NORTH || d == Direction.SOUTH) { // // گروه NS
//////////                if (ns.isEmpty()) { // // اولین NS → خواندن زمان‌ها
//////////                    greenMs  = tl.getGreenMs();
//////////                    yellowMs = tl.getYellowMs();
//////////                    allRedMs = tl.getAllRedMs();
//////////                }
//////////                ns.add(tl); // // افزودن به NS
//////////            } else { // // گروه EW
//////////                if (ew.isEmpty()) {
//////////                    greenMs  = tl.getGreenMs();
//////////                    yellowMs = tl.getYellowMs();
//////////                    allRedMs = tl.getAllRedMs();
//////////                }
//////////                ew.add(tl); // // افزودن به EW
//////////            }
//////////            setAll(LightState.RED); // // شروع امن: همه قرمز
//////////        }
//////////
//////////        private void advance(long addMs){ // // پیشروی زمان فاز
//////////            if (ns.isEmpty() && ew.isEmpty()) return; // // اگر چراغی نیست
//////////
//////////            long spanG = greenMs;             // // انتهای بازه سبز
//////////            long spanY = spanG + yellowMs;    // // انتهای بازه زرد
//////////            long spanR = spanY + allRedMs;    // // انتهای بازه همه‌قرمز
//////////
//////////            t += addMs; // // جمع زمان
//////////
//////////            if (t <= spanG) { // // بازهٔ سبز
//////////                if (phase == 0) { setGroup(ns, LightState.GREEN);  setGroup(ew, LightState.RED); }
//////////                else             { setGroup(ew, LightState.GREEN);  setGroup(ns, LightState.RED); }
//////////            } else if (t <= spanY) { // // بازهٔ زرد
//////////                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }
//////////                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }
//////////            } else if (t <= spanR) { // // بازهٔ همه‌قرمز
//////////                setAll(LightState.RED);
//////////            } else { // // پایان فاز و سویچ
//////////                phase = 1 - phase; // // NS ↔ EW
//////////                t = 0;             // // ریست تایمر
//////////            }
//////////        }
//////////
//////////        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
//////////            int i; for (i=0;i<list.size();i++){ list.get(i).setState(s); } // // حلقه ساده
//////////        }
//////////
//////////        private void setAll(LightState s){ // // اعمال حالت به همه
//////////            setGroup(ns, s); setGroup(ew, s); // // هر دو گروه
//////////        }
//////////    }
//////////}
//////////
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
//////////
//////////package trafficcontrol; // // پکیج کنترل ترافیک
//////////
//////////import core.Direction;              // // جهت‌ها
//////////import infrastructure.Intersection; // // تقاطع
//////////import simulation.Updatable;        // // اینترفیس آپدیت
//////////import java.util.*;                 // // لیست/مپ
//////////
///////////**
////////// * چراغ راهنمایی با زمان‌بندی داخلی و همگام‌سازی در سطح هر تقاطع
////////// * بدون هیچ کلاس زمان‌بند اضافی //
////////// */
//////////public class TrafficLight extends TrafficSign { // // چراغ راهنمایی
//////////
//////////    // ---------- حالت چراغ ----------
//////////    public enum LightState { // // حالت‌ها
//////////        RED, YELLOW, GREEN // // قرمز، زرد، سبز
//////////    }
//////////
//////////    // ---------- رجیستری کنترل‌کنندهٔ هر تقاطع ----------
//////////    private static final Map<Intersection, PhaseController> CONTROLLERS = new HashMap<Intersection, PhaseController>(); // // نگاشت تقاطع به کنترلر
//////////
//////////    // ---------- مشخصات این چراغ ----------
//////////    private final Intersection intersection; // // تقاطع میزبان
//////////    private final Direction direction;       // // جهت این رویکرد
//////////    private LightState state = LightState.RED; // // وضعیت فعلی
//////////
//////////    // ---------- پارامترهای زمانی چرخه (میلی‌ثانیه) ----------
//////////    private final long greenMs;   // // سبز
//////////    private final long yellowMs;  // // زرد
//////////    private final long allRedMs;  // // همه‌قرمز
//////////
//////////    public TrafficLight(Intersection at, Direction dir, long greenMs, long yellowMs, long allRedMs) { // // سازنده
//////////        this.intersection = at;   // // ذخیره تقاطع
//////////        this.direction = dir;     // // ذخیره جهت
//////////        this.greenMs = greenMs;   // // زمان سبز
//////////        this.yellowMs = yellowMs; // // زمان زرد
//////////        this.allRedMs = allRedMs; // // زمان قرمز مشترک
//////////        ensureController().register(this); // // ثبت در کنترل‌کنندهٔ تقاطع
//////////    }
//////////
//////////    public Intersection getIntersection() { // // دسترسی به تقاطع
//////////        return intersection; // // خروجی
//////////    }
//////////
//////////    public Direction getDirection() { // // دسترسی به جهت
//////////        return direction; // // خروجی
//////////    }
//////////
//////////    public LightState getState() { // // خواندن وضعیت
//////////        return state; // // خروجی
//////////    }
//////////
//////////    public void setState(LightState s) { // // تغییر وضعیت
//////////        this.state = s; // // اعمال
//////////    }
//////////
//////////    public long getGreenMs()  { return greenMs;  } // // زمان سبز
//////////    public long getYellowMs() { return yellowMs; } // // زمان زرد
//////////    public long getAllRedMs() { return allRedMs; } // // زمان همه‌قرمز
//////////
//////////    @Override
//////////    protected void onUpdate(double dt) { // // هر فریم صدا زده می‌شود
//////////        // منطق زمان‌بندی در کنترل‌کننده است؛ همین کافیست که به کنترلر اجازه بدهیم جلو برود //
//////////        ensureController().advance((long)(dt * 1000.0)); // // پیشروی زمان فاز (ms)
//////////    }
//////////
//////////    // ---------- گروه‌بندی و کنترل فاز در سطح هر تقاطع ----------
//////////    private PhaseController ensureController() { // // گرفتن/ساخت کنترلر
//////////        PhaseController pc = CONTROLLERS.get(intersection); // // جستجو
//////////        if (pc == null) { // // اگر نبود
//////////            pc = new PhaseController(intersection); // // بساز
//////////            CONTROLLERS.put(intersection, pc); // // ثبت
//////////        }
//////////        return pc; // // خروجی
//////////    }
//////////
//////////    /**
//////////     * کنترل‌کنندهٔ فاز یک تقاطع: چراغ‌های NS با هم و چراغ‌های EW با هم //
//////////     */
//////////    private static final class PhaseController implements Updatable { // // کنترلر داخلی
//////////        private final Intersection owner; // // تقاطع مالک
//////////
//////////        private final List<TrafficLight> ns = new ArrayList<TrafficLight>(); // // چراغ‌های شمال-جنوب
//////////        private final List<TrafficLight> ew = new ArrayList<TrafficLight>(); // // چراغ‌های شرق-غرب
//////////
//////////        private int phase = 0; // // ۰ = NS در حال حرکت، ۱ = EW در حال حرکت
//////////        private long t = 0;    // // زمان سپری‌شده در فاز فعلی (ms)
//////////
//////////        // مدت‌ها از اولین چراغ ثبت‌شده خوانده می‌شود //
//////////        private long greenMs  = 10000; // // پیش‌فرض
//////////        private long yellowMs = 3000;  // // پیش‌فرض
//////////        private long allRedMs = 1000;  // // پیش‌فرض
//////////
//////////        PhaseController(Intersection it){ // // سازنده کنترلر
//////////            this.owner = it; // // ذخیره تقاطع
//////////        }
//////////
//////////        void register(TrafficLight tl){ // // ثبت چراغ
//////////            // تعیین گروه NS/EW بر اساس جهت //
//////////            Direction d = tl.getDirection(); // // جهت
//////////            if (d == Direction.NORTH || d == Direction.SOUTH) { // // NS
//////////                if (ns.isEmpty()) { // // اولین NS
//////////                    greenMs  = tl.getGreenMs();  // // خواندن زمان‌ها
//////////                    yellowMs = tl.getYellowMs(); // // …
//////////                    allRedMs = tl.getAllRedMs(); // // …
//////////                }
//////////                ns.add(tl); // // افزودن به گروه
//////////            } else { // // EW
//////////                if (ew.isEmpty()) { // // اولین EW
//////////                    greenMs  = tl.getGreenMs();  // // خواندن زمان‌ها
//////////                    yellowMs = tl.getYellowMs(); // // …
//////////                    allRedMs = tl.getAllRedMs(); // // …
//////////                }
//////////                ew.add(tl); // // افزودن به گروه
//////////            }
//////////            // شروع فاز اولیه: همه قرمز تا آغاز سیکل //
//////////            setAll(LightState.RED); // // ایمنی
//////////        }
//////////
//////////        @Override
//////////        public void update(double dt){ // // برای سازگاری؛ استفادهٔ بیرونی ندارد
//////////            advance((long)(dt*1000.0)); // // تبدیل به ms
//////////        }
//////////
//////////        void advance(long addMs){ // // پیشروی زمان
//////////            if (ns.isEmpty() && ew.isEmpty()) return; // // اگر هیچ چراغی نیست
//////////
//////////            long spanG = greenMs;             // // انتهای بازهٔ سبز
//////////            long spanY = spanG + yellowMs;    // // انتهای بازهٔ زرد
//////////            long spanR = spanY + allRedMs;    // // انتهای بازهٔ قرمزِ همه
//////////
//////////            t += addMs; // // جمع زمان
//////////
//////////            if (t <= spanG) { // // در بازه سبز
//////////                if (phase == 0) { setGroup(ns, LightState.GREEN); setGroup(ew, LightState.RED);   } // // NS سبز
//////////                else             { setGroup(ew, LightState.GREEN); setGroup(ns, LightState.RED);   } // // EW سبز
//////////            } else if (t <= spanY) { // // در بازه زرد
//////////                if (phase == 0) { setGroup(ns, LightState.YELLOW); setGroup(ew, LightState.RED); }  // // NS زرد
//////////                else             { setGroup(ew, LightState.YELLOW); setGroup(ns, LightState.RED); }  // // EW زرد
//////////            } else if (t <= spanR) { // // در بازه قرمز همه
//////////                setAll(LightState.RED); // // ایمنی همه قرمز
//////////            } else { // // پایان فاز؛ جابجایی
//////////                phase = 1 - phase; // // NS↔EW
//////////                t = 0;             // // ریست زمان فاز
//////////                // فاز جدید از سبز شروع می‌شود؛ در نوبت بعدی advance اعمال می‌شود //
//////////            }
//////////        }
//////////
//////////        private void setGroup(List<TrafficLight> list, LightState s){ // // اعمال حالت به یک گروه
//////////            int i; for (i=0;i<list.size();i++){ list.get(i).setState(s); } // // حلقه ساده
//////////        }
//////////
//////////        private void setAll(LightState s){ // // اعمال حالت به همه چراغ‌های تقاطع
//////////            setGroup(ns, s); setGroup(ew, s); // // هر دو گروه
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
////////////package trafficcontrol;
////////////
////////////import core.Direction;
////////////
////////////// چراغ راهنمایی //
////////////public class TrafficLight implements TrafficControlDevice { //
////////////    private final String id; // شناسه //
////////////    private final Direction direction; // جهت کنترل‌شده //
////////////    private final int greenDuration; // مدت سبز (تیک) //
////////////    private final int yellowDuration; // مدت زرد //
////////////    private final int redDuration; // مدت قرمز //
////////////    private LightState state; // وضعیت فعلی //
////////////    private int timer; // شمارنده تیک در وضعیت فعلی //
////////////
////////////    // کانستراکتور کامل با وضعیت اولیه //
////////////    public TrafficLight(String id, Direction direction, int green, int yellow, int red, LightState initialState) { //
////////////        if (id == null || direction == null || initialState == null) { // اعتبارسنجی //
////////////            throw new IllegalArgumentException("TrafficLight params cannot be null"); //
////////////        }
////////////        if (green <= 0 || yellow <= 0 || red <= 0) { // اعتبارسنجی //
////////////            throw new IllegalArgumentException("Durations must be positive"); //
////////////        }
////////////        this.id = id; //
////////////        this.direction = direction; //
////////////        this.greenDuration = green; //
////////////        this.yellowDuration = yellow; //
////////////        this.redDuration = red; //
////////////        this.state = initialState; //
////////////        this.timer = 0; //
////////////    }
////////////
////////////    // کانستراکتور ساده (شروع از قرمز) //
////////////    public TrafficLight(String id, Direction direction, int green, int yellow, int red) { //
////////////        this(id, direction, green, yellow, red, LightState.RED); //
////////////    }
////////////
////////////    @Override
////////////    public String getId() { //
////////////        return id; //
////////////    }
////////////
////////////    @Override
////////////    public Direction getDirectionControlled() { //
////////////        return direction; //
////////////    }
////////////
////////////    public LightState getState() { //
////////////        return state; //
////////////    }
////////////
////////////    // برای هماهنگ‌سازی گروه چراغ‌ها در World //
////////////    public void setState(LightState newState) { //
////////////        if (newState != null) { //
////////////            this.state = newState; //
////////////            this.timer = 0; // ریست شمارنده بعد از ست مستقیم //
////////////        }
////////////    }
////////////
////////////    public int getGreenDuration() { return greenDuration; } //
////////////    public int getYellowDuration() { return yellowDuration; } //
////////////    public int getRedDuration() { return redDuration; } //
////////////
////////////    @Override
////////////    public void update() { // آپدیت چرخه //
////////////        timer++; // تیک جدید //
////////////        switch (state) { // بررسی وضعیت //
////////////            case GREEN: //
////////////                if (timer >= greenDuration) { // پایان سبز؟ //
////////////                    state = LightState.YELLOW; // رفتن به زرد //
////////////                    timer = 0; //
////////////                }
////////////                break; //
////////////            case YELLOW: //
////////////                if (timer >= yellowDuration) { // پایان زرد؟ //
////////////                    state = LightState.RED; // رفتن به قرمز //
////////////                    timer = 0; //
////////////                }
////////////                break; //
////////////            case RED: //
////////////                if (timer >= redDuration) { // پایان قرمز؟ //
////////////                    state = LightState.GREEN; // رفتن به سبز //
////////////                    timer = 0; //
////////////                }
////////////                break; //
////////////        }
////////////    }
////////////}
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
////////////package trafficcontrol;
////////////
////////////import core.Direction;
////////////
/////////////**
//////////// * چراغ راهنمایی (Traffic Light)
//////////// */
////////////public class TrafficLight implements TrafficControlDevice {
////////////    private final String id;          // شناسه یکتا
////////////    private final Direction direction; // جهت کنترل‌شده
////////////
////////////    private final int greenDuration;   // مدت چراغ سبز (تیک)
////////////    private final int yellowDuration;  // مدت چراغ زرد (تیک)
////////////    private final int redDuration;     // مدت چراغ قرمز (تیک)
////////////
////////////    private LightState state;         // وضعیت فعلی چراغ
////////////    private int timer;                // شمارنده زمان در وضعیت فعلی
////////////
////////////    // --- کانستراکتور کامل (با حالت اولیه مشخص)
////////////    public TrafficLight(String id, Direction direction,
////////////                        int green, int yellow, int red,
////////////                        LightState initialState) {
////////////        if (id == null || direction == null || initialState == null) {
////////////            throw new IllegalArgumentException("TrafficLight params cannot be null");
////////////        }
////////////        if (green <= 0 || yellow <= 0 || red <= 0) {
////////////            throw new IllegalArgumentException("Durations must be positive");
////////////        }
////////////
////////////        this.id = id;
////////////        this.direction = direction;
////////////        this.greenDuration = green;
////////////        this.yellowDuration = yellow;
////////////        this.redDuration = red;
////////////        this.state = initialState;
////////////        this.timer = 0;
////////////    }
////////////
////////////    // --- کانستراکتور ساده (شروع از RED)
////////////    public TrafficLight(String id, Direction direction, int green, int yellow, int red) {
////////////        this(id, direction, green, yellow, red, LightState.RED);
////////////    }
////////////
////////////    // --- Getter ها ---
////////////    @Override
////////////    public String getId() {
////////////        return id;
////////////    }
////////////
////////////    @Override
////////////    public Direction getDirectionControlled() {
////////////        return direction;
////////////    }
////////////
////////////    public LightState getState() {
////////////        return state;
////////////    }
////////////
////////////    // برای هماهنگ‌سازی با گروه (World.sync)
////////////    public void setState(LightState newState) {
////////////        if (newState != null) {
////////////            this.state = newState;
////////////            this.timer = 0; // وقتی دستی ست شد، شمارنده ریست میشه
////////////        }
////////////    }
////////////
////////////    public int getGreenDuration() {
////////////        return greenDuration;
////////////    }
////////////
////////////    public int getYellowDuration() {
////////////        return yellowDuration;
////////////    }
////////////
////////////    public int getRedDuration() {
////////////        return redDuration;
////////////    }
////////////
////////////    // --- آپدیت چراغ در هر تیک ---
////////////    @Override
////////////    public void update() {
////////////        timer++;
////////////
////////////        switch (state) {
////////////            case GREEN:
////////////                if (timer >= greenDuration) {
////////////                    state = LightState.YELLOW;
////////////                    timer = 0;
////////////                }
////////////                break;
////////////
////////////            case YELLOW:
////////////                if (timer >= yellowDuration) {
////////////                    state = LightState.RED;
////////////                    timer = 0;
////////////                }
////////////                break;
////////////
////////////            case RED:
////////////                if (timer >= redDuration) {
////////////                    state = LightState.GREEN;
////////////                    timer = 0;
////////////                }
////////////                break;
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
//////////////package trafficcontrol;
//////////////
//////////////import core.Direction;
//////////////
//////////////public class TrafficLight implements TrafficControlDevice {
//////////////    private final String id;
//////////////    private final Direction direction;
//////////////    private final int greenDuration;
//////////////    private final int yellowDuration;
//////////////    private final int redDuration;
//////////////    private LightState state;
//////////////    private int timer;
//////////////
//////////////    // --- کانستراکتور کامل
//////////////    public TrafficLight(String id, Direction direction, int green, int yellow, int red, LightState initialState) {
//////////////        this.id = id;
//////////////        this.direction = direction;
//////////////        this.greenDuration = green;
//////////////        this.yellowDuration = yellow;
//////////////        this.redDuration = red;
//////////////        this.state = initialState;
//////////////        this.timer = 0;
//////////////    }
//////////////
//////////////    // --- کانستراکتور ساده (دیفالت = RED)
//////////////    public TrafficLight(String id, Direction direction, int green, int yellow, int red) {
//////////////        this(id, direction, green, yellow, red, LightState.RED);
//////////////    }
//////////////
//////////////    public String getId() { return id; }
//////////////    public Direction getDirectionControlled() { return direction; }
//////////////    public LightState getState() { return state; }
//////////////
//////////////    public void update() {
//////////////        timer++;
//////////////        switch (state) {
//////////////            case GREEN:
//////////////                if (timer >= greenDuration) {
//////////////                    state = LightState.YELLOW;
//////////////                    timer = 0;
//////////////                }
//////////////                break;
//////////////            case YELLOW:
//////////////                if (timer >= yellowDuration) {
//////////////                    state = LightState.RED;
//////////////                    timer = 0;
//////////////                }
//////////////                break;
//////////////            case RED:
//////////////                if (timer >= redDuration) {
//////////////                    state = LightState.GREEN;
//////////////                    timer = 0;
//////////////                }
//////////////                break;
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
////////////
////////////
////////////
////////////
////////////
////////////
////////////
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
//////////////package trafficcontrol;
//////////////
//////////////import core.Direction;
//////////////import core.Point;
//////////////import simulation.Updatable;
//////////////
//////////////public class TrafficLight extends TrafficControlDevice implements Updatable {
//////////////
//////////////    private LightState state;       // وضعیت چراغ
//////////////    private final int greenDuration;
//////////////    private final int yellowDuration;
//////////////    private final int redDuration;
//////////////
//////////////    private long lastSwitchTime;    // آخرین زمان تغییر حالت
//////////////    private Point position;         // محل چراغ در نقشه (برای رندر)
//////////////
//////////////    public TrafficLight(String id,
//////////////                        Direction direction,
//////////////                        int greenDuration,
//////////////                        int yellowDuration,
//////////////                        int redDuration,
//////////////                        LightState initialState) {
//////////////        super(id, direction);
//////////////        this.greenDuration = greenDuration;
//////////////        this.yellowDuration = yellowDuration;
//////////////        this.redDuration = redDuration;
//////////////        this.state = initialState;
//////////////        this.lastSwitchTime = System.currentTimeMillis();
//////////////        this.position = new Point(0, 0); // مقدار پیش‌فرض (بعداً ست می‌شود)
//////////////    }
//////////////
//////////////    public LightState getState() {
//////////////        return state;
//////////////    }
//////////////
//////////////    public void setPosition(Point p) {
//////////////        this.position = p;
//////////////    }
//////////////
//////////////    public Point getPosition() {
//////////////        return position;
//////////////    }
//////////////
//////////////    @Override
//////////////    public void update() {
//////////////        long now = System.currentTimeMillis();
//////////////        long elapsed = now - lastSwitchTime;
//////////////
//////////////        switch (state) {
//////////////            case GREEN -> {
//////////////                if (elapsed >= greenDuration) {
//////////////                    state = LightState.YELLOW;
//////////////                    lastSwitchTime = now;
//////////////                }
//////////////            }
//////////////            case YELLOW -> {
//////////////                if (elapsed >= yellowDuration) {
//////////////                    state = LightState.RED;
//////////////                    lastSwitchTime = now;
//////////////                }
//////////////            }
//////////////            case RED -> {
//////////////                if (elapsed >= redDuration) {
//////////////                    state = LightState.GREEN;
//////////////                    lastSwitchTime = now;
//////////////                }
//////////////            }
//////////////        }
//////////////    }
//////////////}
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package trafficcontrol; // // پکیج کنترل ترافیک
////////////////
////////////////import core.Direction; // // جهت کنترل چراغ
////////////////import simulation.Updatable; // // برای آپدیت در هر تیک
////////////////
////////////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
////////////////    private LightState state;      // // وضعیت فعلی چراغ (RED/YELLOW/GREEN)
////////////////    private int greenDuration;     // // مدت سبز (بر حسب تعداد تیک)
////////////////    private int yellowDuration;    // // مدت زرد (بر حسب تعداد تیک)
////////////////    private int redDuration;       // // مدت قرمز (بر حسب تعداد تیک)
////////////////    private int timeRemaining;     // // زمان باقی‌ماندهٔ وضعیت فعلی (تعداد تیک)
////////////////
////////////////    public TrafficLight(String id, Direction dir, int greenMs, int yellowMs, int redMs, int tickIntervalMs) { // // سازنده
////////////////        super(id, dir);                           // // صدا زدن سازندهٔ والد: TrafficSign(String, Direction)
////////////////        this.greenDuration  = Math.max(1, greenMs  / tickIntervalMs); // // تبدیل ms به تیک (سبز)
////////////////        this.yellowDuration = Math.max(1, yellowMs / tickIntervalMs); // // تبدیل ms به تیک (زرد)
////////////////        this.redDuration    = Math.max(1, redMs    / tickIntervalMs); // // تبدیل ms به تیک (قرمز)
////////////////        this.state = LightState.GREEN;            // // شروع از سبز
////////////////        this.timeRemaining = this.greenDuration;  // // تنظیم تایمر اولیه برای سبز
////////////////    }
////////////////
////////////////    public LightState getState() { // // گتر وضعیت فعلی چراغ
////////////////        return state; // // خروجی
////////////////    }
////////////////
////////////////    @Override
////////////////    public Direction getDirectionControlled() { // // جهت کنترلی چراغ
////////////////        return super.getDirectionControlled(); // // استفاده از گتر والد (TrafficSign)
////////////////    }
////////////////
////////////////    @Override
////////////////    public boolean canProceed(core.Vehicle v) { // // اجازه عبور برای خودرو (ساده)
////////////////        return state == LightState.GREEN; // // فقط در حالت سبز اجازه عبور می‌دهیم
////////////////    }
////////////////
////////////////    @Override
////////////////    public void update() { // // آپدیت چرخهٔ چراغ در هر تیک
////////////////        timeRemaining--;                       // // کم کردن یک تیک از زمان باقی‌مانده
////////////////        if (timeRemaining > 0) return;         // // اگر هنوز وقت باقیست، کاری نکن
////////////////
////////////////        if (state == LightState.GREEN) {       // // اگر در حالت سبز بودیم
////////////////            state = LightState.YELLOW;         // // رفتن به زرد
////////////////            timeRemaining = yellowDuration;    // // تنظیم زمان زرد
////////////////        } else if (state == LightState.YELLOW) { // // اگر در حالت زرد بودیم
////////////////            state = LightState.RED;            // // رفتن به قرمز
////////////////            timeRemaining = redDuration;       // // تنظیم زمان قرمز
////////////////        } else {                                // // اگر در حالت قرمز بودیم
////////////////            state = LightState.GREEN;          // // برگشت به سبز
////////////////            timeRemaining = greenDuration;     // // تنظیم زمان سبز
////////////////        }
////////////////    }
////////////////}
////////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////
////////////////// trafficcontrol/TrafficLight.java
////////////////package trafficcontrol; // // پکیج کنترل ترافیک
////////////////
////////////////import core.Direction; // // جهت
////////////////import simulation.Updatable; // // برای ثبت در ساعت
////////////////
////////////////public class TrafficLight implements TrafficControlDevice, Updatable { // // کلاس چراغ راهنمایی
////////////////    private final String id; // // شناسه
////////////////    private final Direction approach; // // جهتِ بهره‌بردار (سمتی که وارد تقاطع می‌شود)
////////////////
////////////////    private int greenMs;   // // مدت سبز (میلی‌ثانیه)
////////////////    private int yellowMs;  // // مدت زرد
////////////////    private int redMs;     // // مدت قرمز
////////////////
////////////////    private LightState state; // // وضعیت فعلی
////////////////    private int elapsedMs;    // // زمان سپری‌شده در وضعیت فعلی
////////////////
////////////////    private int tickIntervalMs = 16; // // برای update: برآورد 16ms؛ می‌تونی از SimulationClock پاس بدی
////////////////
////////////////    public TrafficLight(String id, Direction approach, int greenMs, int yellowMs, int redMs, int initialIndex) { // // سازنده سازگار با کدی که داشتی
////////////////        this.id = id; // // ست id
////////////////        this.approach = approach; // // ست جهت
////////////////        this.greenMs = Math.max(1000, greenMs); // // حداقل ۱ثانیه
////////////////        this.yellowMs = Math.max(500, yellowMs); // // حداقل ۰.۵ثانیه
////////////////        this.redMs = Math.max(1000, redMs); // // حداقل ۱ثانیه
////////////////        this.state = indexToState(initialIndex); // // تعیین وضعیت اولیه
////////////////        this.elapsedMs = 0; // // شروع از صفر
////////////////    }
////////////////
////////////////    private LightState indexToState(int idx) { // // تبدیل ایندکس به وضعیت
////////////////        if (idx == LightState.YELLOW.ordinal()) return LightState.YELLOW; // // زرد
////////////////        if (idx == LightState.RED.ordinal())    return LightState.RED;    // // قرمز
////////////////        return LightState.GREEN; // // پیش‌فرض سبز
////////////////    }
////////////////
////////////////    public void setTickIntervalMs(int ms) { // // ست بازه تیک
////////////////        if (ms > 0) this.tickIntervalMs = ms; // // به‌روزرسانی
////////////////    }
////////////////
////////////////    @Override public String getId() { return id; } // // گتر id
////////////////    public Direction getApproach() { return approach; } // // گتر جهت
////////////////
////////////////    @Override public boolean canProceed(core.Direction d) { // // اجازه عبور؟
////////////////        if (d != approach) return true; // // اگر از سمت دیگری نگاه می‌کنی، این چراغ کنترل‌کننده تو نیست
////////////////        return state == LightState.GREEN; // // فقط در سبز
////////////////    }
////////////////
////////////////    @Override public void update() { // // آپدیت چراغ در هر تیک
////////////////        elapsedMs += tickIntervalMs; // // افزایش زمان
////////////////        switch (state) { // // بررسی وضعیت
////////////////            case GREEN: // // سبز
////////////////                if (elapsedMs >= greenMs) { state = LightState.YELLOW; elapsedMs = 0; } // // رفتن به زرد
////////////////                break; // // پایان
////////////////            case YELLOW: // // زرد
////////////////                if (elapsedMs >= yellowMs) { state = LightState.RED; elapsedMs = 0; } // // رفتن به قرمز
////////////////                break; // // پایان
////////////////            case RED: // // قرمز
////////////////                if (elapsedMs >= redMs) { state = LightState.GREEN; elapsedMs = 0; } // // رفتن به سبز
////////////////                break; // // پایان
////////////////        }
////////////////    }
////////////////
////////////////    @Override public LightState getState() { return state; } // // وضعیت فعلی
////////////////
////////////////    // متدهای کمکی برای تغییر زمان‌بندی در حین اجرا
////////////////    public void setDurations(int gMs, int yMs, int rMs) { // // تغییر بازه‌ها
////////////////        this.greenMs  = Math.max(500, gMs); // // حداقل
////////////////        this.yellowMs = Math.max(300, yMs); // // حداقل
////////////////        this.redMs    = Math.max(500, rMs); // // حداقل
////////////////    }
////////////////}
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
////////////////
//////////////////
//////////////////package trafficcontrol; // // پکیج کنترل ترافیک
//////////////////
//////////////////import core.Direction; // // جهت‌ها
//////////////////import core.Identifiable; // // شناسه
//////////////////import core.Vehicle; // // خودرو
//////////////////import simulation.Updatable; // // آپدیت‌پذیر
//////////////////
///////////////////**
////////////////// * TrafficLight: چراغ سه‌فازی با فازبندی مستقل و تصادفی.
////////////////// * واحد زمان در این کلاس «تیک» است و در هر update یک تیک کم می‌شود.
////////////////// */
//////////////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // کلاس چراغ
//////////////////
//////////////////    private LightState state;        // // وضعیت فعلی
//////////////////    private int greenDuration;       // // طول فاز سبز (به تیک)
//////////////////    private int yellowDuration;      // // طول فاز زرد (به تیک)
//////////////////    private int redDuration;         // // طول فاز قرمز (به تیک)
//////////////////    private int timeRemaining;       // // باقیمانده‌ی فاز فعلی (به تیک)
//////////////////
//////////////////    public TrafficLight(String id, Direction dir, int green, int yellow, int red, int startPhaseIndex) { // // سازنده
//////////////////        this.id = id;                               // // ست id
//////////////////        this.directionControlled = dir;             // // ست جهت کنترل
//////////////////        this.greenDuration = Math.max(5, green);    // // حداقل منطقی
//////////////////        this.yellowDuration = Math.max(2, yellow);  // // حداقل منطقی
//////////////////        this.redDuration = Math.max(5, red);        // // حداقل منطقی
//////////////////
//////////////////        // --- فازبندی تصادفی مستقل برای هر چراغ ---
//////////////////        int cycle = this.greenDuration + this.yellowDuration + this.redDuration; // // طول یک سیکل
//////////////////        int offset = (int) (Math.random() * cycle); // // افست تصادفی داخل سیکل
//////////////////        // // تعیین state و timeRemaining بر اساس افست
//////////////////        if (offset < this.greenDuration) { // // داخل سبز
//////////////////            this.state = LightState.GREEN;               // // سبز
//////////////////            this.timeRemaining = this.greenDuration - offset; // // باقیمانده سبز
//////////////////        } else if (offset < this.greenDuration + this.yellowDuration) { // // داخل زرد
//////////////////            int passed = offset - this.greenDuration;    // // مقدار طی‌شده در زرد
//////////////////            this.state = LightState.YELLOW;              // // زرد
//////////////////            this.timeRemaining = this.yellowDuration - passed; // // باقیمانده زرد
//////////////////        } else { // // داخل قرمز
//////////////////            int passed = offset - (this.greenDuration + this.yellowDuration); // // طی‌شده در قرمز
//////////////////            this.state = LightState.RED;                // // قرمز
//////////////////            this.timeRemaining = this.redDuration - passed; // // باقیمانده قرمز
//////////////////        }
//////////////////    }
//////////////////
//////////////////    @Override
//////////////////    public void update() { // // یک تیک جلو رفتن
//////////////////        this.timeRemaining--;                            // // کم کردن باقیمانده
//////////////////        if (this.timeRemaining > 0) return;              // // اگر هنوز زمان دارد، برگرد
//////////////////        // // سوییچ فاز
//////////////////        if (this.state == LightState.GREEN) {            // // اگر سبز بود
//////////////////            this.state = LightState.YELLOW;              // // برو زرد
//////////////////            this.timeRemaining = this.yellowDuration;    // // زمان زرد
//////////////////        } else if (this.state == LightState.YELLOW) {    // // اگر زرد بود
//////////////////            this.state = LightState.RED;                 // // برو قرمز
//////////////////            this.timeRemaining = this.redDuration;       // // زمان قرمز
//////////////////        } else {                                         // // اگر قرمز بود
//////////////////            this.state = LightState.GREEN;               // // برو سبز
//////////////////            this.timeRemaining = this.greenDuration;     // // زمان سبز
//////////////////        }
//////////////////    }
//////////////////
//////////////////    @Override
//////////////////    public boolean canProceed(Vehicle v) { // // آیا عبور مجاز است؟
//////////////////        return this.state == LightState.GREEN;          // // فقط در سبز
//////////////////    }
//////////////////
//////////////////    @Override
//////////////////    public Direction getDirectionControlled() { // // گتر جهت کنترل
//////////////////        return this.directionControlled;               // // بازگرداندن جهت
//////////////////    }
//////////////////
//////////////////    public LightState getState() { return this.state; } // // گتر وضعیت برای UI
//////////////////    public int getTimeRemaining() { return this.timeRemaining; } // // گتر باقیمانده (اختیاری)
//////////////////}
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
////////////////////package trafficcontrol; // // پکیج کنترل ترافیک
////////////////////
////////////////////import simulation.Updatable; // // اینترفیس آپدیت از پکیج شبیه‌سازی
////////////////////import core.Direction; // // جهت کنترل‌شده از پکیج core
////////////////////import core.Vehicle; // // وسیله نقلیه از پکیج core
////////////////////
////////////////////// توجه مهم:
////////////////////// // لطفاً فایل‌های LightState.java ، TrafficSign.java و TrafficControlDevice.java را جداگانه نگه دارید
////////////////////// // و در این فایل دیگر آن‌ها را تعریف نکنید تا خطای Duplicate class رفع شود.
////////////////////
////////////////////// کلاس چراغ راهنمایی
////////////////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
////////////////////    private LightState state; // // وضعیت فعلی چراغ
////////////////////    private int greenDurationMs; // // مدت سبز (ms)
////////////////////    private int yellowDurationMs; // // مدت زرد (ms)
////////////////////    private int redDurationMs; // // مدت قرمز (ms)
////////////////////    private int timeRemainingMs; // // زمان باقی‌مانده تا سوییچ وضعیت (ms)
////////////////////    private int tickIntervalMs; // // طول هر تیک شبیه‌سازی (ms)
////////////////////
////////////////////    public TrafficLight( // // سازندهٔ کامل
////////////////////                         String id, // // شناسه یکتا
////////////////////                         Direction direction, // // جهت کنترل‌شده
////////////////////                         int greenDurationMs, // // طول فاز سبز
////////////////////                         int yellowDurationMs, // // طول فاز زرد
////////////////////                         int redDurationMs, // // طول فاز قرمز
////////////////////                         int tickIntervalMs // // طول هر تیک (ms)
////////////////////    ) {
////////////////////        this.id = id; // // مقداردهی شناسه (از TrafficSign)
////////////////////        this.directionControlled = direction; // // مقداردهی جهت (از TrafficSign)
////////////////////        this.greenDurationMs = greenDurationMs; // // ذخیره مدت سبز
////////////////////        this.yellowDurationMs = yellowDurationMs; // // ذخیره مدت زرد
////////////////////        this.redDurationMs = redDurationMs; // // ذخیره مدت قرمز
////////////////////        this.tickIntervalMs = tickIntervalMs; // // ذخیره طول تیک
////////////////////        this.state = LightState.GREEN; // // شروع از وضعیت سبز
////////////////////        this.timeRemainingMs = greenDurationMs; // // تایمر اولیه = مدت سبز
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public String getId() { // // پیاده‌سازی شناسه (برگرفته از TrafficSign)
////////////////////        return this.id; // // برگرداندن id
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public Direction getDirectionControlled() { // // جهت کنترل‌شده
////////////////////        return this.directionControlled; // // برگرداندن جهت
////////////////////    }
////////////////////
////////////////////    public LightState getState() { // // گرفتن وضعیت فعلی چراغ
////////////////////        return this.state; // // برگرداندن وضعیت
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public void update() { // // آپدیت چراغ در هر تیک
////////////////////        this.timeRemainingMs -= this.tickIntervalMs; // // کم شدن تایمر به اندازه یک تیک
////////////////////        if (this.timeRemainingMs <= 0) { // // اگر زمان فاز تمام شد
////////////////////            switch (this.state) { // // تعیین فاز بعدی
////////////////////                case GREEN: // // اگر سبز بود
////////////////////                    this.state = LightState.YELLOW; // // به زرد برو
////////////////////                    this.timeRemainingMs = this.yellowDurationMs; // // تایمر زرد
////////////////////                    break; // // پایان case
////////////////////                case YELLOW: // // اگر زرد بود
////////////////////                    this.state = LightState.RED; // // به قرمز برو
////////////////////                    this.timeRemainingMs = this.redDurationMs; // // تایمر قرمز
////////////////////                    break; // // پایان case
////////////////////                case RED: // // اگر قرمز بود
////////////////////                    this.state = LightState.GREEN; // // به سبز برگرد
////////////////////                    this.timeRemainingMs = this.greenDurationMs; // // تایمر سبز
////////////////////                    break; // // پایان case
////////////////////                default: // // حالت پیش‌فرض (نباید برسیم)
////////////////////                    this.state = LightState.RED; // // ایمن: قرمز
////////////////////                    this.timeRemainingMs = this.redDurationMs; // // تایمر قرمز
////////////////////                    break; // // پایان case
////////////////////            }
////////////////////        }
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public boolean canProceed(Vehicle v) { // // آیا اجازه عبور می‌دهد؟
////////////////////        return this.state == LightState.GREEN; // // نسخهٔ ساده: فقط در سبز مجاز
////////////////////    }
////////////////////}
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
////////////////////package trafficcontrol; // // پکیج کنترل ترافیک
////////////////////
////////////////////import core.Direction; // // جهت کنترل چراغ
////////////////////import simulation.Updatable; // // برای آپدیت در هر تیک
////////////////////
////////////////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
////////////////////    private LightState state;      // // وضعیت فعلی چراغ (RED/YELLOW/GREEN)
////////////////////    private int greenDuration;     // // مدت سبز (بر حسب تعداد تیک)
////////////////////    private int yellowDuration;    // // مدت زرد (بر حسب تعداد تیک)
////////////////////    private int redDuration;       // // مدت قرمز (بر حسب تعداد تیک)
////////////////////    private int timeRemaining;     // // زمان باقی‌ماندهٔ وضعیت فعلی (تعداد تیک)
////////////////////
////////////////////    public TrafficLight(String id, Direction dir, int greenMs, int yellowMs, int redMs, int tickIntervalMs) { // // سازنده
////////////////////        super(id, dir);                           // // صدا زدن سازندهٔ والد: TrafficSign(String, Direction)
////////////////////        this.greenDuration  = Math.max(1, greenMs  / tickIntervalMs); // // تبدیل ms به تیک (سبز)
////////////////////        this.yellowDuration = Math.max(1, yellowMs / tickIntervalMs); // // تبدیل ms به تیک (زرد)
////////////////////        this.redDuration    = Math.max(1, redMs    / tickIntervalMs); // // تبدیل ms به تیک (قرمز)
////////////////////        this.state = LightState.GREEN;            // // شروع از سبز
////////////////////        this.timeRemaining = this.greenDuration;  // // تنظیم تایمر اولیه برای سبز
////////////////////    }
////////////////////
////////////////////    public LightState getState() { // // گتر وضعیت فعلی چراغ
////////////////////        return state; // // خروجی
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public Direction getDirectionControlled() { // // جهت کنترلی چراغ
////////////////////        return super.getDirectionControlled(); // // استفاده از گتر والد (TrafficSign)
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public boolean canProceed(core.Vehicle v) { // // اجازه عبور برای خودرو (ساده)
////////////////////        return state == LightState.GREEN; // // فقط در حالت سبز اجازه عبور می‌دهیم
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public void update() { // // آپدیت چرخهٔ چراغ در هر تیک
////////////////////        timeRemaining--;                       // // کم کردن یک تیک از زمان باقی‌مانده
////////////////////        if (timeRemaining > 0) return;         // // اگر هنوز وقت باقیست، کاری نکن
////////////////////
////////////////////        if (state == LightState.GREEN) {       // // اگر در حالت سبز بودیم
////////////////////            state = LightState.YELLOW;         // // رفتن به زرد
////////////////////            timeRemaining = yellowDuration;    // // تنظیم زمان زرد
////////////////////        } else if (state == LightState.YELLOW) { // // اگر در حالت زرد بودیم
////////////////////            state = LightState.RED;            // // رفتن به قرمز
////////////////////            timeRemaining = redDuration;       // // تنظیم زمان قرمز
////////////////////        } else {                                // // اگر در حالت قرمز بودیم
////////////////////            state = LightState.GREEN;          // // برگشت به سبز
////////////////////            timeRemaining = greenDuration;     // // تنظیم زمان سبز
////////////////////        }
////////////////////    }
////////////////////}
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
////////////////////package trafficcontrol; // // پکیج کنترل ترافیک
////////////////////
////////////////////import core.Direction; // // جهت‌ها
////////////////////import simulation.Updatable; // // برای آپدیت هر تیک
////////////////////
////////////////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
////////////////////    private LightState state; // // وضعیت فعلی چراغ
////////////////////    private int greenDuration;  // // طول سبز بر حسب تیک
////////////////////    private int yellowDuration; // // طول زرد بر حسب تیک
////////////////////    private int redDuration;    // // طول قرمز بر حسب تیک
////////////////////    private int timeRemaining;  // // زمان باقی‌مانده وضعیت فعلی
////////////////////
////////////////////    public TrafficLight(String id, Direction dir, int greenMs, int yellowMs, int redMs, int tickIntervalMs) { // // سازنده
////////////////////        this.id = id; // // ست شناسه از کلاس والد
////////////////////        this.directionControlled = dir; // // جهت تحت کنترل
////////////////////        this.greenDuration  = Math.max(1, greenMs  / tickIntervalMs); // // تبدیل میلی‌ثانیه به تعدادتیک
////////////////////        this.yellowDuration = Math.max(1, yellowMs / tickIntervalMs); // // تبدیل میلی‌ثانیه به تعدادتیک
////////////////////        this.redDuration    = Math.max(1, redMs    / tickIntervalMs); // // تبدیل میلی‌ثانیه به تعدادتیک
////////////////////        this.state = LightState.GREEN; // // شروع از سبز
////////////////////        this.timeRemaining = greenDuration; // // زمان باقیمانده اولیه
////////////////////    }
////////////////////
////////////////////    public LightState getState() { return state; } // // گتر وضعیت
////////////////////
////////////////////    @Override
////////////////////    public Direction getDirectionControlled() { // // گتر جهت کنترل
////////////////////        return directionControlled; // // خروجی
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public boolean canProceed(core.Vehicle v) { // // اجازه عبور (فعلاً پایه)
////////////////////        return state == LightState.GREEN; // // فقط سبز اجازه می‌دهد
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public void update() { // // آپدیت هر تیک
////////////////////        timeRemaining--; // // کم کردن یک تیک
////////////////////        if (timeRemaining > 0) return; // // اگر هنوز زمان باقیست، خروج
////////////////////
////////////////////        if (state == LightState.GREEN) { // // اگر سبز بود
////////////////////            state = LightState.YELLOW; // // رفتن به زرد
////////////////////            timeRemaining = yellowDuration; // // مدت زرد
////////////////////        } else if (state == LightState.YELLOW) { // // اگر زرد بود
////////////////////            state = LightState.RED; // // رفتن به قرمز
////////////////////            timeRemaining = redDuration; // // مدت قرمز
////////////////////        } else { // // اگر قرمز بود
////////////////////            state = LightState.GREEN; // // رفتن به سبز
////////////////////            timeRemaining = greenDuration; // // مدت سبز
////////////////////        }
////////////////////    }
////////////////////}
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
//////////////////
////////////////////package trafficcontrol;
////////////////////
////////////////////import core.*;
////////////////////import simulation.Updatable;
////////////////////
////////////////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable {
////////////////////    private LightState state;
////////////////////    private int greenDuration;
////////////////////    private int yellowDuration;
////////////////////    private int redDuration;
////////////////////    private int timeRemaining;
////////////////////
////////////////////    public TrafficLight(String id, Direction directionControlled,
////////////////////                        int greenDuration, int yellowDuration, int redDuration) {
////////////////////        super(id, directionControlled);
////////////////////        this.greenDuration = greenDuration;
////////////////////        this.yellowDuration = yellowDuration;
////////////////////        this.redDuration = redDuration;
////////////////////        this.state = LightState.RED;
////////////////////        this.timeRemaining = redDuration;
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public void update() {
////////////////////        timeRemaining--;
////////////////////        if (timeRemaining <= 0) {
////////////////////            switch (state) {
////////////////////                case RED:
////////////////////                    state = LightState.GREEN;
////////////////////                    timeRemaining = greenDuration;
////////////////////                    break;
////////////////////                case GREEN:
////////////////////                    state = LightState.YELLOW;
////////////////////                    timeRemaining = yellowDuration;
////////////////////                    break;
////////////////////                case YELLOW:
////////////////////                    state = LightState.RED;
////////////////////                    timeRemaining = redDuration;
////////////////////                    break;
////////////////////            }
////////////////////        }
////////////////////    }
////////////////////
////////////////////    @Override
////////////////////    public boolean canProceed(Vehicle v) {
////////////////////        return state == LightState.GREEN;
////////////////////    }
////////////////////
////////////////////    public LightState getState() {
////////////////////        return state;
////////////////////    }
////////////////////}
