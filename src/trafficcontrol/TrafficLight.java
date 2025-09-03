
package trafficcontrol;

import core.Direction;

// چراغ راهنمایی //
public class TrafficLight implements TrafficControlDevice { //
    private final String id; // شناسه //
    private final Direction direction; // جهت کنترل‌شده //
    private final int greenDuration; // مدت سبز (تیک) //
    private final int yellowDuration; // مدت زرد //
    private final int redDuration; // مدت قرمز //
    private LightState state; // وضعیت فعلی //
    private int timer; // شمارنده تیک در وضعیت فعلی //

    // کانستراکتور کامل با وضعیت اولیه //
    public TrafficLight(String id, Direction direction, int green, int yellow, int red, LightState initialState) { //
        if (id == null || direction == null || initialState == null) { // اعتبارسنجی //
            throw new IllegalArgumentException("TrafficLight params cannot be null"); //
        }
        if (green <= 0 || yellow <= 0 || red <= 0) { // اعتبارسنجی //
            throw new IllegalArgumentException("Durations must be positive"); //
        }
        this.id = id; //
        this.direction = direction; //
        this.greenDuration = green; //
        this.yellowDuration = yellow; //
        this.redDuration = red; //
        this.state = initialState; //
        this.timer = 0; //
    }

    // کانستراکتور ساده (شروع از قرمز) //
    public TrafficLight(String id, Direction direction, int green, int yellow, int red) { //
        this(id, direction, green, yellow, red, LightState.RED); //
    }

    @Override
    public String getId() { //
        return id; //
    }

    @Override
    public Direction getDirectionControlled() { //
        return direction; //
    }

    public LightState getState() { //
        return state; //
    }

    // برای هماهنگ‌سازی گروه چراغ‌ها در World //
    public void setState(LightState newState) { //
        if (newState != null) { //
            this.state = newState; //
            this.timer = 0; // ریست شمارنده بعد از ست مستقیم //
        }
    }

    public int getGreenDuration() { return greenDuration; } //
    public int getYellowDuration() { return yellowDuration; } //
    public int getRedDuration() { return redDuration; } //

    @Override
    public void update() { // آپدیت چرخه //
        timer++; // تیک جدید //
        switch (state) { // بررسی وضعیت //
            case GREEN: //
                if (timer >= greenDuration) { // پایان سبز؟ //
                    state = LightState.YELLOW; // رفتن به زرد //
                    timer = 0; //
                }
                break; //
            case YELLOW: //
                if (timer >= yellowDuration) { // پایان زرد؟ //
                    state = LightState.RED; // رفتن به قرمز //
                    timer = 0; //
                }
                break; //
            case RED: //
                if (timer >= redDuration) { // پایان قرمز؟ //
                    state = LightState.GREEN; // رفتن به سبز //
                    timer = 0; //
                }
                break; //
        }
    }
}


























//package trafficcontrol;
//
//import core.Direction;
//
///**
// * چراغ راهنمایی (Traffic Light)
// */
//public class TrafficLight implements TrafficControlDevice {
//    private final String id;          // شناسه یکتا
//    private final Direction direction; // جهت کنترل‌شده
//
//    private final int greenDuration;   // مدت چراغ سبز (تیک)
//    private final int yellowDuration;  // مدت چراغ زرد (تیک)
//    private final int redDuration;     // مدت چراغ قرمز (تیک)
//
//    private LightState state;         // وضعیت فعلی چراغ
//    private int timer;                // شمارنده زمان در وضعیت فعلی
//
//    // --- کانستراکتور کامل (با حالت اولیه مشخص)
//    public TrafficLight(String id, Direction direction,
//                        int green, int yellow, int red,
//                        LightState initialState) {
//        if (id == null || direction == null || initialState == null) {
//            throw new IllegalArgumentException("TrafficLight params cannot be null");
//        }
//        if (green <= 0 || yellow <= 0 || red <= 0) {
//            throw new IllegalArgumentException("Durations must be positive");
//        }
//
//        this.id = id;
//        this.direction = direction;
//        this.greenDuration = green;
//        this.yellowDuration = yellow;
//        this.redDuration = red;
//        this.state = initialState;
//        this.timer = 0;
//    }
//
//    // --- کانستراکتور ساده (شروع از RED)
//    public TrafficLight(String id, Direction direction, int green, int yellow, int red) {
//        this(id, direction, green, yellow, red, LightState.RED);
//    }
//
//    // --- Getter ها ---
//    @Override
//    public String getId() {
//        return id;
//    }
//
//    @Override
//    public Direction getDirectionControlled() {
//        return direction;
//    }
//
//    public LightState getState() {
//        return state;
//    }
//
//    // برای هماهنگ‌سازی با گروه (World.sync)
//    public void setState(LightState newState) {
//        if (newState != null) {
//            this.state = newState;
//            this.timer = 0; // وقتی دستی ست شد، شمارنده ریست میشه
//        }
//    }
//
//    public int getGreenDuration() {
//        return greenDuration;
//    }
//
//    public int getYellowDuration() {
//        return yellowDuration;
//    }
//
//    public int getRedDuration() {
//        return redDuration;
//    }
//
//    // --- آپدیت چراغ در هر تیک ---
//    @Override
//    public void update() {
//        timer++;
//
//        switch (state) {
//            case GREEN:
//                if (timer >= greenDuration) {
//                    state = LightState.YELLOW;
//                    timer = 0;
//                }
//                break;
//
//            case YELLOW:
//                if (timer >= yellowDuration) {
//                    state = LightState.RED;
//                    timer = 0;
//                }
//                break;
//
//            case RED:
//                if (timer >= redDuration) {
//                    state = LightState.GREEN;
//                    timer = 0;
//                }
//                break;
//        }
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
//
//
//
//
////package trafficcontrol;
////
////import core.Direction;
////
////public class TrafficLight implements TrafficControlDevice {
////    private final String id;
////    private final Direction direction;
////    private final int greenDuration;
////    private final int yellowDuration;
////    private final int redDuration;
////    private LightState state;
////    private int timer;
////
////    // --- کانستراکتور کامل
////    public TrafficLight(String id, Direction direction, int green, int yellow, int red, LightState initialState) {
////        this.id = id;
////        this.direction = direction;
////        this.greenDuration = green;
////        this.yellowDuration = yellow;
////        this.redDuration = red;
////        this.state = initialState;
////        this.timer = 0;
////    }
////
////    // --- کانستراکتور ساده (دیفالت = RED)
////    public TrafficLight(String id, Direction direction, int green, int yellow, int red) {
////        this(id, direction, green, yellow, red, LightState.RED);
////    }
////
////    public String getId() { return id; }
////    public Direction getDirectionControlled() { return direction; }
////    public LightState getState() { return state; }
////
////    public void update() {
////        timer++;
////        switch (state) {
////            case GREEN:
////                if (timer >= greenDuration) {
////                    state = LightState.YELLOW;
////                    timer = 0;
////                }
////                break;
////            case YELLOW:
////                if (timer >= yellowDuration) {
////                    state = LightState.RED;
////                    timer = 0;
////                }
////                break;
////            case RED:
////                if (timer >= redDuration) {
////                    state = LightState.GREEN;
////                    timer = 0;
////                }
////                break;
////        }
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
////
////package trafficcontrol;
////
////import core.Direction;
////import core.Point;
////import simulation.Updatable;
////
////public class TrafficLight extends TrafficControlDevice implements Updatable {
////
////    private LightState state;       // وضعیت چراغ
////    private final int greenDuration;
////    private final int yellowDuration;
////    private final int redDuration;
////
////    private long lastSwitchTime;    // آخرین زمان تغییر حالت
////    private Point position;         // محل چراغ در نقشه (برای رندر)
////
////    public TrafficLight(String id,
////                        Direction direction,
////                        int greenDuration,
////                        int yellowDuration,
////                        int redDuration,
////                        LightState initialState) {
////        super(id, direction);
////        this.greenDuration = greenDuration;
////        this.yellowDuration = yellowDuration;
////        this.redDuration = redDuration;
////        this.state = initialState;
////        this.lastSwitchTime = System.currentTimeMillis();
////        this.position = new Point(0, 0); // مقدار پیش‌فرض (بعداً ست می‌شود)
////    }
////
////    public LightState getState() {
////        return state;
////    }
////
////    public void setPosition(Point p) {
////        this.position = p;
////    }
////
////    public Point getPosition() {
////        return position;
////    }
////
////    @Override
////    public void update() {
////        long now = System.currentTimeMillis();
////        long elapsed = now - lastSwitchTime;
////
////        switch (state) {
////            case GREEN -> {
////                if (elapsed >= greenDuration) {
////                    state = LightState.YELLOW;
////                    lastSwitchTime = now;
////                }
////            }
////            case YELLOW -> {
////                if (elapsed >= yellowDuration) {
////                    state = LightState.RED;
////                    lastSwitchTime = now;
////                }
////            }
////            case RED -> {
////                if (elapsed >= redDuration) {
////                    state = LightState.GREEN;
////                    lastSwitchTime = now;
////                }
////            }
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
//////package trafficcontrol; // // پکیج کنترل ترافیک
//////
//////import core.Direction; // // جهت کنترل چراغ
//////import simulation.Updatable; // // برای آپدیت در هر تیک
//////
//////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
//////    private LightState state;      // // وضعیت فعلی چراغ (RED/YELLOW/GREEN)
//////    private int greenDuration;     // // مدت سبز (بر حسب تعداد تیک)
//////    private int yellowDuration;    // // مدت زرد (بر حسب تعداد تیک)
//////    private int redDuration;       // // مدت قرمز (بر حسب تعداد تیک)
//////    private int timeRemaining;     // // زمان باقی‌ماندهٔ وضعیت فعلی (تعداد تیک)
//////
//////    public TrafficLight(String id, Direction dir, int greenMs, int yellowMs, int redMs, int tickIntervalMs) { // // سازنده
//////        super(id, dir);                           // // صدا زدن سازندهٔ والد: TrafficSign(String, Direction)
//////        this.greenDuration  = Math.max(1, greenMs  / tickIntervalMs); // // تبدیل ms به تیک (سبز)
//////        this.yellowDuration = Math.max(1, yellowMs / tickIntervalMs); // // تبدیل ms به تیک (زرد)
//////        this.redDuration    = Math.max(1, redMs    / tickIntervalMs); // // تبدیل ms به تیک (قرمز)
//////        this.state = LightState.GREEN;            // // شروع از سبز
//////        this.timeRemaining = this.greenDuration;  // // تنظیم تایمر اولیه برای سبز
//////    }
//////
//////    public LightState getState() { // // گتر وضعیت فعلی چراغ
//////        return state; // // خروجی
//////    }
//////
//////    @Override
//////    public Direction getDirectionControlled() { // // جهت کنترلی چراغ
//////        return super.getDirectionControlled(); // // استفاده از گتر والد (TrafficSign)
//////    }
//////
//////    @Override
//////    public boolean canProceed(core.Vehicle v) { // // اجازه عبور برای خودرو (ساده)
//////        return state == LightState.GREEN; // // فقط در حالت سبز اجازه عبور می‌دهیم
//////    }
//////
//////    @Override
//////    public void update() { // // آپدیت چرخهٔ چراغ در هر تیک
//////        timeRemaining--;                       // // کم کردن یک تیک از زمان باقی‌مانده
//////        if (timeRemaining > 0) return;         // // اگر هنوز وقت باقیست، کاری نکن
//////
//////        if (state == LightState.GREEN) {       // // اگر در حالت سبز بودیم
//////            state = LightState.YELLOW;         // // رفتن به زرد
//////            timeRemaining = yellowDuration;    // // تنظیم زمان زرد
//////        } else if (state == LightState.YELLOW) { // // اگر در حالت زرد بودیم
//////            state = LightState.RED;            // // رفتن به قرمز
//////            timeRemaining = redDuration;       // // تنظیم زمان قرمز
//////        } else {                                // // اگر در حالت قرمز بودیم
//////            state = LightState.GREEN;          // // برگشت به سبز
//////            timeRemaining = greenDuration;     // // تنظیم زمان سبز
//////        }
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
//////// trafficcontrol/TrafficLight.java
//////package trafficcontrol; // // پکیج کنترل ترافیک
//////
//////import core.Direction; // // جهت
//////import simulation.Updatable; // // برای ثبت در ساعت
//////
//////public class TrafficLight implements TrafficControlDevice, Updatable { // // کلاس چراغ راهنمایی
//////    private final String id; // // شناسه
//////    private final Direction approach; // // جهتِ بهره‌بردار (سمتی که وارد تقاطع می‌شود)
//////
//////    private int greenMs;   // // مدت سبز (میلی‌ثانیه)
//////    private int yellowMs;  // // مدت زرد
//////    private int redMs;     // // مدت قرمز
//////
//////    private LightState state; // // وضعیت فعلی
//////    private int elapsedMs;    // // زمان سپری‌شده در وضعیت فعلی
//////
//////    private int tickIntervalMs = 16; // // برای update: برآورد 16ms؛ می‌تونی از SimulationClock پاس بدی
//////
//////    public TrafficLight(String id, Direction approach, int greenMs, int yellowMs, int redMs, int initialIndex) { // // سازنده سازگار با کدی که داشتی
//////        this.id = id; // // ست id
//////        this.approach = approach; // // ست جهت
//////        this.greenMs = Math.max(1000, greenMs); // // حداقل ۱ثانیه
//////        this.yellowMs = Math.max(500, yellowMs); // // حداقل ۰.۵ثانیه
//////        this.redMs = Math.max(1000, redMs); // // حداقل ۱ثانیه
//////        this.state = indexToState(initialIndex); // // تعیین وضعیت اولیه
//////        this.elapsedMs = 0; // // شروع از صفر
//////    }
//////
//////    private LightState indexToState(int idx) { // // تبدیل ایندکس به وضعیت
//////        if (idx == LightState.YELLOW.ordinal()) return LightState.YELLOW; // // زرد
//////        if (idx == LightState.RED.ordinal())    return LightState.RED;    // // قرمز
//////        return LightState.GREEN; // // پیش‌فرض سبز
//////    }
//////
//////    public void setTickIntervalMs(int ms) { // // ست بازه تیک
//////        if (ms > 0) this.tickIntervalMs = ms; // // به‌روزرسانی
//////    }
//////
//////    @Override public String getId() { return id; } // // گتر id
//////    public Direction getApproach() { return approach; } // // گتر جهت
//////
//////    @Override public boolean canProceed(core.Direction d) { // // اجازه عبور؟
//////        if (d != approach) return true; // // اگر از سمت دیگری نگاه می‌کنی، این چراغ کنترل‌کننده تو نیست
//////        return state == LightState.GREEN; // // فقط در سبز
//////    }
//////
//////    @Override public void update() { // // آپدیت چراغ در هر تیک
//////        elapsedMs += tickIntervalMs; // // افزایش زمان
//////        switch (state) { // // بررسی وضعیت
//////            case GREEN: // // سبز
//////                if (elapsedMs >= greenMs) { state = LightState.YELLOW; elapsedMs = 0; } // // رفتن به زرد
//////                break; // // پایان
//////            case YELLOW: // // زرد
//////                if (elapsedMs >= yellowMs) { state = LightState.RED; elapsedMs = 0; } // // رفتن به قرمز
//////                break; // // پایان
//////            case RED: // // قرمز
//////                if (elapsedMs >= redMs) { state = LightState.GREEN; elapsedMs = 0; } // // رفتن به سبز
//////                break; // // پایان
//////        }
//////    }
//////
//////    @Override public LightState getState() { return state; } // // وضعیت فعلی
//////
//////    // متدهای کمکی برای تغییر زمان‌بندی در حین اجرا
//////    public void setDurations(int gMs, int yMs, int rMs) { // // تغییر بازه‌ها
//////        this.greenMs  = Math.max(500, gMs); // // حداقل
//////        this.yellowMs = Math.max(300, yMs); // // حداقل
//////        this.redMs    = Math.max(500, rMs); // // حداقل
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
////////
////////package trafficcontrol; // // پکیج کنترل ترافیک
////////
////////import core.Direction; // // جهت‌ها
////////import core.Identifiable; // // شناسه
////////import core.Vehicle; // // خودرو
////////import simulation.Updatable; // // آپدیت‌پذیر
////////
/////////**
//////// * TrafficLight: چراغ سه‌فازی با فازبندی مستقل و تصادفی.
//////// * واحد زمان در این کلاس «تیک» است و در هر update یک تیک کم می‌شود.
//////// */
////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // کلاس چراغ
////////
////////    private LightState state;        // // وضعیت فعلی
////////    private int greenDuration;       // // طول فاز سبز (به تیک)
////////    private int yellowDuration;      // // طول فاز زرد (به تیک)
////////    private int redDuration;         // // طول فاز قرمز (به تیک)
////////    private int timeRemaining;       // // باقیمانده‌ی فاز فعلی (به تیک)
////////
////////    public TrafficLight(String id, Direction dir, int green, int yellow, int red, int startPhaseIndex) { // // سازنده
////////        this.id = id;                               // // ست id
////////        this.directionControlled = dir;             // // ست جهت کنترل
////////        this.greenDuration = Math.max(5, green);    // // حداقل منطقی
////////        this.yellowDuration = Math.max(2, yellow);  // // حداقل منطقی
////////        this.redDuration = Math.max(5, red);        // // حداقل منطقی
////////
////////        // --- فازبندی تصادفی مستقل برای هر چراغ ---
////////        int cycle = this.greenDuration + this.yellowDuration + this.redDuration; // // طول یک سیکل
////////        int offset = (int) (Math.random() * cycle); // // افست تصادفی داخل سیکل
////////        // // تعیین state و timeRemaining بر اساس افست
////////        if (offset < this.greenDuration) { // // داخل سبز
////////            this.state = LightState.GREEN;               // // سبز
////////            this.timeRemaining = this.greenDuration - offset; // // باقیمانده سبز
////////        } else if (offset < this.greenDuration + this.yellowDuration) { // // داخل زرد
////////            int passed = offset - this.greenDuration;    // // مقدار طی‌شده در زرد
////////            this.state = LightState.YELLOW;              // // زرد
////////            this.timeRemaining = this.yellowDuration - passed; // // باقیمانده زرد
////////        } else { // // داخل قرمز
////////            int passed = offset - (this.greenDuration + this.yellowDuration); // // طی‌شده در قرمز
////////            this.state = LightState.RED;                // // قرمز
////////            this.timeRemaining = this.redDuration - passed; // // باقیمانده قرمز
////////        }
////////    }
////////
////////    @Override
////////    public void update() { // // یک تیک جلو رفتن
////////        this.timeRemaining--;                            // // کم کردن باقیمانده
////////        if (this.timeRemaining > 0) return;              // // اگر هنوز زمان دارد، برگرد
////////        // // سوییچ فاز
////////        if (this.state == LightState.GREEN) {            // // اگر سبز بود
////////            this.state = LightState.YELLOW;              // // برو زرد
////////            this.timeRemaining = this.yellowDuration;    // // زمان زرد
////////        } else if (this.state == LightState.YELLOW) {    // // اگر زرد بود
////////            this.state = LightState.RED;                 // // برو قرمز
////////            this.timeRemaining = this.redDuration;       // // زمان قرمز
////////        } else {                                         // // اگر قرمز بود
////////            this.state = LightState.GREEN;               // // برو سبز
////////            this.timeRemaining = this.greenDuration;     // // زمان سبز
////////        }
////////    }
////////
////////    @Override
////////    public boolean canProceed(Vehicle v) { // // آیا عبور مجاز است؟
////////        return this.state == LightState.GREEN;          // // فقط در سبز
////////    }
////////
////////    @Override
////////    public Direction getDirectionControlled() { // // گتر جهت کنترل
////////        return this.directionControlled;               // // بازگرداندن جهت
////////    }
////////
////////    public LightState getState() { return this.state; } // // گتر وضعیت برای UI
////////    public int getTimeRemaining() { return this.timeRemaining; } // // گتر باقیمانده (اختیاری)
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
//////////package trafficcontrol; // // پکیج کنترل ترافیک
//////////
//////////import simulation.Updatable; // // اینترفیس آپدیت از پکیج شبیه‌سازی
//////////import core.Direction; // // جهت کنترل‌شده از پکیج core
//////////import core.Vehicle; // // وسیله نقلیه از پکیج core
//////////
//////////// توجه مهم:
//////////// // لطفاً فایل‌های LightState.java ، TrafficSign.java و TrafficControlDevice.java را جداگانه نگه دارید
//////////// // و در این فایل دیگر آن‌ها را تعریف نکنید تا خطای Duplicate class رفع شود.
//////////
//////////// کلاس چراغ راهنمایی
//////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
//////////    private LightState state; // // وضعیت فعلی چراغ
//////////    private int greenDurationMs; // // مدت سبز (ms)
//////////    private int yellowDurationMs; // // مدت زرد (ms)
//////////    private int redDurationMs; // // مدت قرمز (ms)
//////////    private int timeRemainingMs; // // زمان باقی‌مانده تا سوییچ وضعیت (ms)
//////////    private int tickIntervalMs; // // طول هر تیک شبیه‌سازی (ms)
//////////
//////////    public TrafficLight( // // سازندهٔ کامل
//////////                         String id, // // شناسه یکتا
//////////                         Direction direction, // // جهت کنترل‌شده
//////////                         int greenDurationMs, // // طول فاز سبز
//////////                         int yellowDurationMs, // // طول فاز زرد
//////////                         int redDurationMs, // // طول فاز قرمز
//////////                         int tickIntervalMs // // طول هر تیک (ms)
//////////    ) {
//////////        this.id = id; // // مقداردهی شناسه (از TrafficSign)
//////////        this.directionControlled = direction; // // مقداردهی جهت (از TrafficSign)
//////////        this.greenDurationMs = greenDurationMs; // // ذخیره مدت سبز
//////////        this.yellowDurationMs = yellowDurationMs; // // ذخیره مدت زرد
//////////        this.redDurationMs = redDurationMs; // // ذخیره مدت قرمز
//////////        this.tickIntervalMs = tickIntervalMs; // // ذخیره طول تیک
//////////        this.state = LightState.GREEN; // // شروع از وضعیت سبز
//////////        this.timeRemainingMs = greenDurationMs; // // تایمر اولیه = مدت سبز
//////////    }
//////////
//////////    @Override
//////////    public String getId() { // // پیاده‌سازی شناسه (برگرفته از TrafficSign)
//////////        return this.id; // // برگرداندن id
//////////    }
//////////
//////////    @Override
//////////    public Direction getDirectionControlled() { // // جهت کنترل‌شده
//////////        return this.directionControlled; // // برگرداندن جهت
//////////    }
//////////
//////////    public LightState getState() { // // گرفتن وضعیت فعلی چراغ
//////////        return this.state; // // برگرداندن وضعیت
//////////    }
//////////
//////////    @Override
//////////    public void update() { // // آپدیت چراغ در هر تیک
//////////        this.timeRemainingMs -= this.tickIntervalMs; // // کم شدن تایمر به اندازه یک تیک
//////////        if (this.timeRemainingMs <= 0) { // // اگر زمان فاز تمام شد
//////////            switch (this.state) { // // تعیین فاز بعدی
//////////                case GREEN: // // اگر سبز بود
//////////                    this.state = LightState.YELLOW; // // به زرد برو
//////////                    this.timeRemainingMs = this.yellowDurationMs; // // تایمر زرد
//////////                    break; // // پایان case
//////////                case YELLOW: // // اگر زرد بود
//////////                    this.state = LightState.RED; // // به قرمز برو
//////////                    this.timeRemainingMs = this.redDurationMs; // // تایمر قرمز
//////////                    break; // // پایان case
//////////                case RED: // // اگر قرمز بود
//////////                    this.state = LightState.GREEN; // // به سبز برگرد
//////////                    this.timeRemainingMs = this.greenDurationMs; // // تایمر سبز
//////////                    break; // // پایان case
//////////                default: // // حالت پیش‌فرض (نباید برسیم)
//////////                    this.state = LightState.RED; // // ایمن: قرمز
//////////                    this.timeRemainingMs = this.redDurationMs; // // تایمر قرمز
//////////                    break; // // پایان case
//////////            }
//////////        }
//////////    }
//////////
//////////    @Override
//////////    public boolean canProceed(Vehicle v) { // // آیا اجازه عبور می‌دهد؟
//////////        return this.state == LightState.GREEN; // // نسخهٔ ساده: فقط در سبز مجاز
//////////    }
//////////}
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
//////////import core.Direction; // // جهت کنترل چراغ
//////////import simulation.Updatable; // // برای آپدیت در هر تیک
//////////
//////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
//////////    private LightState state;      // // وضعیت فعلی چراغ (RED/YELLOW/GREEN)
//////////    private int greenDuration;     // // مدت سبز (بر حسب تعداد تیک)
//////////    private int yellowDuration;    // // مدت زرد (بر حسب تعداد تیک)
//////////    private int redDuration;       // // مدت قرمز (بر حسب تعداد تیک)
//////////    private int timeRemaining;     // // زمان باقی‌ماندهٔ وضعیت فعلی (تعداد تیک)
//////////
//////////    public TrafficLight(String id, Direction dir, int greenMs, int yellowMs, int redMs, int tickIntervalMs) { // // سازنده
//////////        super(id, dir);                           // // صدا زدن سازندهٔ والد: TrafficSign(String, Direction)
//////////        this.greenDuration  = Math.max(1, greenMs  / tickIntervalMs); // // تبدیل ms به تیک (سبز)
//////////        this.yellowDuration = Math.max(1, yellowMs / tickIntervalMs); // // تبدیل ms به تیک (زرد)
//////////        this.redDuration    = Math.max(1, redMs    / tickIntervalMs); // // تبدیل ms به تیک (قرمز)
//////////        this.state = LightState.GREEN;            // // شروع از سبز
//////////        this.timeRemaining = this.greenDuration;  // // تنظیم تایمر اولیه برای سبز
//////////    }
//////////
//////////    public LightState getState() { // // گتر وضعیت فعلی چراغ
//////////        return state; // // خروجی
//////////    }
//////////
//////////    @Override
//////////    public Direction getDirectionControlled() { // // جهت کنترلی چراغ
//////////        return super.getDirectionControlled(); // // استفاده از گتر والد (TrafficSign)
//////////    }
//////////
//////////    @Override
//////////    public boolean canProceed(core.Vehicle v) { // // اجازه عبور برای خودرو (ساده)
//////////        return state == LightState.GREEN; // // فقط در حالت سبز اجازه عبور می‌دهیم
//////////    }
//////////
//////////    @Override
//////////    public void update() { // // آپدیت چرخهٔ چراغ در هر تیک
//////////        timeRemaining--;                       // // کم کردن یک تیک از زمان باقی‌مانده
//////////        if (timeRemaining > 0) return;         // // اگر هنوز وقت باقیست، کاری نکن
//////////
//////////        if (state == LightState.GREEN) {       // // اگر در حالت سبز بودیم
//////////            state = LightState.YELLOW;         // // رفتن به زرد
//////////            timeRemaining = yellowDuration;    // // تنظیم زمان زرد
//////////        } else if (state == LightState.YELLOW) { // // اگر در حالت زرد بودیم
//////////            state = LightState.RED;            // // رفتن به قرمز
//////////            timeRemaining = redDuration;       // // تنظیم زمان قرمز
//////////        } else {                                // // اگر در حالت قرمز بودیم
//////////            state = LightState.GREEN;          // // برگشت به سبز
//////////            timeRemaining = greenDuration;     // // تنظیم زمان سبز
//////////        }
//////////    }
//////////}
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
//////////import core.Direction; // // جهت‌ها
//////////import simulation.Updatable; // // برای آپدیت هر تیک
//////////
//////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable { // // چراغ راهنمایی
//////////    private LightState state; // // وضعیت فعلی چراغ
//////////    private int greenDuration;  // // طول سبز بر حسب تیک
//////////    private int yellowDuration; // // طول زرد بر حسب تیک
//////////    private int redDuration;    // // طول قرمز بر حسب تیک
//////////    private int timeRemaining;  // // زمان باقی‌مانده وضعیت فعلی
//////////
//////////    public TrafficLight(String id, Direction dir, int greenMs, int yellowMs, int redMs, int tickIntervalMs) { // // سازنده
//////////        this.id = id; // // ست شناسه از کلاس والد
//////////        this.directionControlled = dir; // // جهت تحت کنترل
//////////        this.greenDuration  = Math.max(1, greenMs  / tickIntervalMs); // // تبدیل میلی‌ثانیه به تعدادتیک
//////////        this.yellowDuration = Math.max(1, yellowMs / tickIntervalMs); // // تبدیل میلی‌ثانیه به تعدادتیک
//////////        this.redDuration    = Math.max(1, redMs    / tickIntervalMs); // // تبدیل میلی‌ثانیه به تعدادتیک
//////////        this.state = LightState.GREEN; // // شروع از سبز
//////////        this.timeRemaining = greenDuration; // // زمان باقیمانده اولیه
//////////    }
//////////
//////////    public LightState getState() { return state; } // // گتر وضعیت
//////////
//////////    @Override
//////////    public Direction getDirectionControlled() { // // گتر جهت کنترل
//////////        return directionControlled; // // خروجی
//////////    }
//////////
//////////    @Override
//////////    public boolean canProceed(core.Vehicle v) { // // اجازه عبور (فعلاً پایه)
//////////        return state == LightState.GREEN; // // فقط سبز اجازه می‌دهد
//////////    }
//////////
//////////    @Override
//////////    public void update() { // // آپدیت هر تیک
//////////        timeRemaining--; // // کم کردن یک تیک
//////////        if (timeRemaining > 0) return; // // اگر هنوز زمان باقیست، خروج
//////////
//////////        if (state == LightState.GREEN) { // // اگر سبز بود
//////////            state = LightState.YELLOW; // // رفتن به زرد
//////////            timeRemaining = yellowDuration; // // مدت زرد
//////////        } else if (state == LightState.YELLOW) { // // اگر زرد بود
//////////            state = LightState.RED; // // رفتن به قرمز
//////////            timeRemaining = redDuration; // // مدت قرمز
//////////        } else { // // اگر قرمز بود
//////////            state = LightState.GREEN; // // رفتن به سبز
//////////            timeRemaining = greenDuration; // // مدت سبز
//////////        }
//////////    }
//////////}
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
//////////package trafficcontrol;
//////////
//////////import core.*;
//////////import simulation.Updatable;
//////////
//////////public class TrafficLight extends TrafficSign implements TrafficControlDevice, Updatable {
//////////    private LightState state;
//////////    private int greenDuration;
//////////    private int yellowDuration;
//////////    private int redDuration;
//////////    private int timeRemaining;
//////////
//////////    public TrafficLight(String id, Direction directionControlled,
//////////                        int greenDuration, int yellowDuration, int redDuration) {
//////////        super(id, directionControlled);
//////////        this.greenDuration = greenDuration;
//////////        this.yellowDuration = yellowDuration;
//////////        this.redDuration = redDuration;
//////////        this.state = LightState.RED;
//////////        this.timeRemaining = redDuration;
//////////    }
//////////
//////////    @Override
//////////    public void update() {
//////////        timeRemaining--;
//////////        if (timeRemaining <= 0) {
//////////            switch (state) {
//////////                case RED:
//////////                    state = LightState.GREEN;
//////////                    timeRemaining = greenDuration;
//////////                    break;
//////////                case GREEN:
//////////                    state = LightState.YELLOW;
//////////                    timeRemaining = yellowDuration;
//////////                    break;
//////////                case YELLOW:
//////////                    state = LightState.RED;
//////////                    timeRemaining = redDuration;
//////////                    break;
//////////            }
//////////        }
//////////    }
//////////
//////////    @Override
//////////    public boolean canProceed(Vehicle v) {
//////////        return state == LightState.GREEN;
//////////    }
//////////
//////////    public LightState getState() {
//////////        return state;
//////////    }
//////////}
