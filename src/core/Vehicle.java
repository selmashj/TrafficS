package core; // // پکیج core

import infrastructure.Lane;                        // // لِین
import infrastructure.Road;                        // // جاده
import infrastructure.Intersection;                // // تقاطع
import simulation.SimulationConfig;                // // ثابت‌ها

public class Vehicle implements Identifiable, simulation.Updatable { // // خودرو

    // ---- هویت/نوع/رفتار ----
    private String id;                             // // شناسه
    private VehicleType type;                      // // نوع
    private VehicleState state;                    // // وضعیت
    private DriverProfile driverProfile;           // // پروفایل راننده (فعلاً نمادین)

    // ---- دینامیک ----
    private double speed;                          // // سرعت فعلی (px/s)
    private double targetSpeed;                    // // سقف لحظه‌ای (از قوانین/چراغ) (px/s)
    private double dtSeconds = SimulationConfig.TICK_DT_SEC; // // گام زمانی

    // ---- IDM params ----
    private double desiredSpeed = SimulationConfig.DESIRED_SPEED_PXPS;   // // v0
    private double lengthPx     = SimulationConfig.VEHICLE_LENGTH_PX;    // // طول
    private double lastAccel    = 0.0;                                   // // شتاب قبلی (برای jerk)
    private boolean brakeLightOn = false;                                // // چراغ ترمز

    // ---- مسیر/موقعیت ----
    private Lane currentLane;                       // // لِین فعلی
    private double positionInLane;                  // // s روی لِین
    private Route route;                            // // مسیر (فعلاً استفاده نمیشود)
    private Intersection destination;               // // مقصد (اختیاری)

    // ================== سازنده‌ها ==================
    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) { // // سازنده اصلی قدیمی
        this.id = id;                               // // ست ID
        this.type = type;                           // // ست نوع
        this.driverProfile = profile;               // // ست پروفایل
        this.desiredSpeed = Math.max(10.0, maxSpeed); // // سرعت آزاد
        this.speed = 0.0;                           // // شروع
        this.targetSpeed = this.desiredSpeed;       // // سقف اولیه
        this.state = VehicleState.STOPPED;          // // وضعیت
    }

    public Vehicle(String id, Point spawn) {        // // سازنده کمکی
        this(id, VehicleType.CAR, SimulationConfig.DESIRED_SPEED_PXPS, DriverProfile.LAW_ABIDING); // // تفویض
    }

    // ================== IDM-lite + Jerk ==================
    @Override
    public void update() {                          // // آپدیت حرکت
        if (currentLane == null) {                  // // اگر لِین نداریم
            state = VehicleState.STOPPED;           // // توقف
            brakeLightOn = false;                   // // چراغ خاموش
            return;                                 // // خروج
        }

        // پارامترها //
        final double v0   = Math.max(1.0, Math.min(desiredSpeed, targetSpeed)); // // سقف مؤثر
        final double aMax = SimulationConfig.MAX_ACCEL_PXPS2;                   // // aMax
        final double b    = SimulationConfig.COMFORT_DECEL_PXPS2;               // // b
        final double T    = SimulationConfig.DESIRED_HEADWAY_S;                 // // T
        final double s0   = SimulationConfig.MIN_GAP_PX;                        // // s0
        final double J    = SimulationConfig.JERK_LIMIT_PXPS3;                  // // jerk limit

        // یافتن لیدر در همین لِین //
        Vehicle leader = currentLane.findLeaderAhead(this);                      // // لیدر
        double gap = 1e9;                                                        // // فاصله طولی
        double relV = 0.0;                                                       // // Δv (v - v_lead)
        if (leader != null) {                                                    // // اگر لیدر داریم
            double leadS = leader.getPositionInLane();                           // // s لیدر
            boolean forward = (currentLane.getDirection()==Direction.EAST || currentLane.getDirection()==Direction.SOUTH); // // جهت
            gap = forward ? (leadS - positionInLane) : (positionInLane - leadS); // // فاصله
            gap -= leader.getLengthPx();                                         // // کسر طول لیدر
            if (gap < 0) gap = 0;                                                // // کلیپ
            relV = speed - leader.getSpeed();                                    // // Δv
        }

        // s* مطلوب //
        double sStar = s0 + speed * T + (speed * relV) / (2.0 * Math.sqrt(aMax * b)); // // IDM s*
        if (sStar < s0) sStar = s0;                                              // // کلیپ

        // ترم‌های IDM //
        double freeTerm = 1.0 - Math.pow(speed / v0, 4.0);                       // // حرکت آزاد
        double interactTerm = (leader == null) ? 0.0 : Math.pow(sStar / Math.max(1.0, gap), 2.0); // // تعامل
        double aDesired = aMax * (freeTerm - interactTerm);                      // // شتاب خام

        // محدودیت jerk //
        double aLow = lastAccel - J * dtSeconds;                                 // // حد پایین
        double aHigh = lastAccel + J * dtSeconds;                                // // حد بالا
        double aEff = Math.max(aLow, Math.min(aDesired, aHigh));                 // // شتاب نهایی

        // بروزرسانی سرعت/مکان //
        speed += aEff * dtSeconds;                                               // // v_new
        if (speed < 0) speed = 0;                                                // // کلیپ پایین
        if (speed > v0) speed = v0;                                              // // کلیپ بالا

        positionInLane += speed * dtSeconds * ((currentLane.getDirection()==Direction.EAST || currentLane.getDirection()==Direction.SOUTH) ? (+1) : (-1)); // // s_new با جهت

        // چراغ ترمز //
        brakeLightOn = (aEff < -0.3 * b);                                        // // فعال در ترمز محسوس

        // وضعیت //
        state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED;      // // وضعیت ساده

        // ذخیره شتاب برای jerk //
        lastAccel = aEff;                                                         // // به‌روزرسانی
    }

    // ================== گتر/ستر ==================
    @Override public String getId(){ return id; }                // // ID
    public VehicleType getType(){ return type; }                 // // نوع
    public VehicleState getState(){ return state; }              // // وضعیت
    public void setState(VehicleState s){ state = s; }           // // ست وضعیت
    public double getSpeed(){ return speed; }                    // // سرعت
    public void setSpeed(double v){ if (v < 0) v = 0; speed = v; } // // ست سرعت
    public double getDesiredSpeed(){ return desiredSpeed; }      // // v0
    public void setDesiredSpeed(double v){ desiredSpeed = Math.max(10.0, v); } // // ست v0
    public double getLengthPx(){ return lengthPx; }              // // طول
    public boolean isBrakeLightOn(){ return brakeLightOn; }      // // چراغ ترمز؟

    public double getTargetSpeed(){ return targetSpeed; }        // // سقف لحظه‌ای
    public void setTargetSpeed(double v){ if (v < 0) v = 0; targetSpeed = v; } // // ست سقف

    public void setDtSeconds(double dt){ dtSeconds = (dt > 0 ? dt : SimulationConfig.TICK_DT_SEC); } // // ست dt

    public Lane getCurrentLane(){ return currentLane; }          // // گتر لِین
    public void setCurrentLane(Lane l){                          // // ستر لِین با ثبت/حذف
        if (this.currentLane == l) return;                       // // اگر تغییری نیست
        if (this.currentLane != null) this.currentLane.unregisterVehicle(this); // // خروج از قبلی
        this.currentLane = l;                                    // // ست جدید
        if (this.currentLane != null) this.currentLane.registerVehicle(this);   // // ورود به جدید
    }

    public double getPositionInLane(){ return positionInLane; }  // // گتر s
    public void setPositionInLane(double p){ positionInLane = p; } // // ست s
    public Route getRoute(){ return route; }                     // // گتر مسیر
    public void setRoute(Route r){ route = r; }                  // // ست مسیر
    public Intersection getDestination(){ return destination; }  // // گتر مقصد
    public void setDestination(Intersection d){ destination = d; } // // ست مقصد

    // زاویه رندر (سازگار با UI) //
    public double getAngle() {                                   // // زاویه
        if (currentLane == null) return 0.0;                     // // پیش‌فرض
        double base = currentLane.getAngleRadians();             // // زاویه A→B
        Direction d = currentLane.getDirection();                // // جهت
        if (d == Direction.WEST || d == Direction.NORTH) base += Math.PI; // // معکوس
        return base;                                             // // خروجی
    }

    // راهنما و ترمز (سازگار قدیمی) //
    public boolean isTurningLeft(){ return false; }              // // (فعلاً بی‌استفاده)
    public boolean isTurningRight(){ return false; }             // // (فعلاً بی‌استفاده)
    public void setTurningLeft(boolean on){}                     // // سازگاری
    public void setTurningRight(boolean on){}                    // // سازگاری
    public void clearIndicators(){}                              // // سازگاری
}




















//package core; // // پکیج core
//
//import infrastructure.Lane; // // لِین
//import infrastructure.Intersection; // // تقاطع
//import infrastructure.Road; // // جاده
//import simulation.Updatable; // // برای update()
//
//public class Vehicle implements Identifiable, Updatable { // // خودرو
//
//    // ---- هویت/نوع/رفتار ----
//    private String id; // // شناسه
//    private VehicleType type; // // نوع
//    private VehicleState state; // // وضعیت
//    private DriverProfile driverProfile; // // پروفایل راننده
//
//    // ---- دینامیک ----
//    private double speed;        // // سرعت فعلی (px/s)
//    private double acceleration; // // گاز (px/s^2)
//    private double deceleration; // // ترمز (px/s^2)
//    private double maxSpeed;     // // سقف سرعت (px/s)
//    private double targetSpeed;  // // سرعت هدف (px/s)
//    private double dtSeconds = 0.1; // // گام زمانی
//
//    // ---- سیگنال‌ها ----
//    private boolean brakeLightOn;  // // چراغ ترمز
//    private boolean turningLeft;   // // راهنمای چپ
//    private boolean turningRight;  // // راهنمای راست
//
//    // ---- مسیر/موقعیت ----
//    private Lane currentLane;      // // لِین فعلی
//    private double positionInLane; // // فاصله از ابتدای A→B (px)
//    private Route route;           // // مسیر
//    private Intersection destination; // // مقصد
//
//    // ================== سازنده‌ها ==================
//    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) { // // سازنده
//        this.id = id; // // ست ID
//        this.type = type; // // ست نوع
//        this.driverProfile = profile; // // ست پروفایل
//        this.maxSpeed = Math.max(10.0, maxSpeed); // // سقف حداقلی
//        this.speed = 0.0; // // شروع با صفر
//        this.targetSpeed = Math.min(this.maxSpeed, 30.0); // // هدف اولیه
//        this.acceleration = 60.0;   // // شتاب
//        this.deceleration = 120.0;  // // ترمز
//        this.state = VehicleState.STOPPED; // // وضعیت
//    }
//
//    public Vehicle(String id, Point spawn) { // // سازنده کمکی
//        this(id, VehicleType.CAR, 80.0, DriverProfile.LAW_ABIDING); // // فراخوانی اصلی
//    }
//
//    // ================== حلقهٔ حرکت ==================
//    @Override
//    public void update() { // // آپدیت هر تیک
//        if (currentLane == null) { // // اگر لِین نداریم
//            state = VehicleState.STOPPED; // // توقف
//            brakeLightOn = false; // // چراغ ترمز خاموش
//            return; // // خروج
//        }
//
//        // نزدیک‌کردن سرعت به هدف با گاز/ترمز //
//        double diff = targetSpeed - speed; // // اختلاف با هدف
//        double a = (diff > 0) ? acceleration : -deceleration; // // انتخاب شتاب
//        double dv = a * dtSeconds; // // تغییر سرعت
//        if (Math.abs(dv) > Math.abs(diff)) speed = targetSpeed; else speed += dv; // // جلوگیری از overshoot
//        if (speed < 0) speed = 0; if (speed > maxSpeed) speed = maxSpeed; // // کلیپ سرعت
//        brakeLightOn = (diff < -1e-6); // // چراغ ترمز هنگام کاهش
//
//        // جهتِ فیزیکی حرکت در این لِین //
//        int dirSign = (currentLane.getDirection() == Direction.EAST || currentLane.getDirection() == Direction.SOUTH) ? (+1) : (-1); // // علامت جهت
//
//        // پیشروی روی لِین (A→B برای dirSign=+1 ، و B→A برای dirSign=-1) //
//        positionInLane += dirSign * speed * dtSeconds; // // به‌روزرسانی موضع
//
//        // طول لِین //
//        Road road = currentLane.getParentRoad(); // // جاده
//        double laneLength = currentLane.getLength(); // // طول
//
//        // گذر به لِین بعدی یا توقف در انتها //
//        if (dirSign > 0) { // // حرکت A→B
//            if (positionInLane >= laneLength) { // // به انتها رسیدیم
//                double overflow = positionInLane - laneLength; // // سرریز
//                Lane next = (route != null) ? route.getNextLane(currentLane) : null; // // لِین بعدی
//                if (next != null) { // // اگر هست
//                    currentLane = next; // // سوییچ
//                    positionInLane = Math.max(0.0, overflow); // // موضع جدید
//                    state = VehicleState.TURNING; // // وضعیت گذار
//                    turningLeft = turningRight = false; // // خاموشی راهنما
//                } else { // // پایان مسیر
//                    positionInLane = laneLength; targetSpeed = 0; speed = 0; brakeLightOn = true; state = VehicleState.STOPPED; // // توقف
//                }
//            } else { state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED; } // // وضعیت جاری
//        } else { // // حرکت B→A
//            if (positionInLane <= 0) { // // به ابتدای A رسیدیم (از سمت B)
//                double overflow = positionInLane; // // سرریز منفی
//                Lane next = (route != null) ? route.getNextLane(currentLane) : null; // // لِین بعدی
//                if (next != null) { // // اگر هست
//                    currentLane = next; // // سوییچ
//                    positionInLane = Math.max(0.0, laneLength + overflow); // // موضع در لِین بعدی
//                    state = VehicleState.TURNING; // // وضعیت
//                    turningLeft = turningRight = false; // // راهنما
//                } else { // // پایان مسیر
//                    positionInLane = 0; targetSpeed = 0; speed = 0; brakeLightOn = true; state = VehicleState.STOPPED; // // توقف
//                }
//            } else { state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED; } // // وضعیت
//        }
//    }
//
//    // ================== کنترل زمان/سرعت ==================
//    public void setTargetSpeed(double v){ if (v < 0) v = 0; if (v > maxSpeed) v = maxSpeed; this.targetSpeed = v; } // // ست هدف
//    public double getTargetSpeed(){ return targetSpeed; } // // گتر هدف
//    public void setDtSeconds(double dt){ this.dtSeconds = (dt > 0 ? dt : 0.05); } // // ست dt
//
//    // ================== زاویه برای رندر (منطبق با جهت حرکت) ==================
//    public double getAngle() { // // زاویه رندر
//        if (currentLane == null) return 0.0; // // پیش‌فرض
//        double base = currentLane.getAngleRadians(); // // زاویه A→B
//        Direction d = currentLane.getDirection(); // // جهت لِین
//        if (d == Direction.WEST || d == Direction.NORTH) base += Math.PI; // // چرخش ۱۸۰ درجه برای B→A
//        return base; // // خروجی
//    }
//
//    // ================== گتر/سترهای عمومی ==================
//    @Override public String getId(){ return id; } // // ID
//    public VehicleType getType(){ return type; } // // نوع
//    public VehicleState getState(){ return state; } // // وضعیت
//    public void setState(VehicleState s){ state = s; } // // ست وضعیت
//    public double getSpeed(){ return speed; } // // گتر سرعت
//    public void setSpeed(double v){ if (v < 0) v = 0; if (v > maxSpeed) v = maxSpeed; speed = v; } // // ست سرعت
//    public double getAcceleration(){ return acceleration; } // // گتر گاز
//    public void setAcceleration(double a){ acceleration = Math.max(0, a); } // // ست گاز
//    public double getDeceleration(){ return deceleration; } // // گتر ترمز
//    public void setDeceleration(double d){ deceleration = Math.max(10, d); } // // ست ترمز
//    public double getMaxSpeed(){ return maxSpeed; } // // گتر سقف
//    public void setMaxSpeed(double m){ maxSpeed = Math.max(10, m); if (targetSpeed > m) targetSpeed = m; if (speed > m) speed = m; } // // ست سقف
//    public Lane getCurrentLane(){ return currentLane; } // // گتر لِین
//    public void setCurrentLane(Lane l){ currentLane = l; } // // ست لِین
//    public double getPositionInLane(){ return positionInLane; } // // گتر موضع
//    public void setPositionInLane(double p){ positionInLane = p; } // // ست موضع
//    public Route getRoute(){ return route; } // // گتر مسیر
//    public void setRoute(Route r){ route = r; } // // ست مسیر
//    public Intersection getDestination(){ return destination; } // // گتر مقصد
//    public void setDestination(Intersection d){ destination = d; } // // ست مقصد
//
//    public boolean isBrakeLightOn(){ return brakeLightOn; } // // چراغ ترمز؟
//    public boolean isTurningLeft(){ return turningLeft; }   // // راهنمای چپ؟
//    public boolean isTurningRight(){ return turningRight; } // // راهنمای راست؟
//    public void setTurningLeft(boolean on){ turningLeft = on; if (on) turningRight = false; } // // ست چپ
//    public void setTurningRight(boolean on){ turningRight = on; if (on) turningLeft = false; } // // ست راست
//    public void clearIndicators(){ turningLeft = turningRight = false; } // // خاموشی
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
////777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
////package core;
////
////import infrastructure.Lane;
////import infrastructure.Intersection;
////import simulation.Updatable;
////
////public class Vehicle implements Identifiable, Locatable, Updatable {
////    private final String id;
////    private final VehicleType type;
////    private VehicleState state;
////    private DriverProfile driverProfile;  // پروفایل راننده
////
////    private double speed;          // سرعت فعلی
////    private double targetSpeed;    // سرعت هدف
////    private final double acceleration;   // شتاب مثبت
////    private final double deceleration;   // شتاب منفی
////    private final double maxSpeed;       // حداکثر سرعت
////    private boolean brakeLightOn;
////
////    private Lane currentLane;
////    private double positionInLane; // موقعیت ماشین داخل لاین
////    private Route route;
////    private Intersection destination;
////
////    private double dtSeconds = 0.1;
////
////    // سبقت
////    private boolean overtaking = false;
////    private Lane originalLane;
////    private boolean indicatorVisible = false;
////    private long indicatorLastToggle = 0;
////
////    // ---------------- سازنده‌ها ----------------
////    public Vehicle(String id, VehicleType type, Lane lane) {
////        this.id = id;
////        this.type = type;
////        this.currentLane = lane;
////        this.state = VehicleState.MOVING;
////        this.positionInLane = 0;
////
////        this.speed = 0;
////        this.targetSpeed = 42.0;
////        this.acceleration = 3.0;
////        this.deceleration = 5.0;
////        this.maxSpeed = 60.0;
////        this.driverProfile = DriverProfile.LAW_ABIDING; // پیش‌فرض
////    }
////
////    public Vehicle(String id, VehicleType type) {
////        this(id, type, null);
////    }
////
////    // سازنده جدید برای DriverProfile و موقعیت اولیه
////    public Vehicle(String id, VehicleType type, int positionInLane, DriverProfile profile) {
////        this.id = id;
////        this.type = type;
////        this.currentLane = null;   // بعداً ست میشه
////        this.state = VehicleState.MOVING;
////        this.positionInLane = positionInLane;
////
////        this.speed = 0;
////        this.targetSpeed = 42.0;
////        this.acceleration = 3.0;
////        this.deceleration = 5.0;
////        this.maxSpeed = 60.0;
////
////        this.driverProfile = profile;
////    }
////
////    // ---------------- Getter/Setter ----------------
////    @Override
////    public String getId() { return id; }
////
////    @Override
////    public Point getPosition() {
////        if (currentLane == null) return new Point(0, 0);
////        return currentLane.getPositionAt(positionInLane);
////    }
////
////    public void setDtSeconds(double dt) { this.dtSeconds = dt; }
////    public void setTargetSpeed(double t) { this.targetSpeed = t; }
////    public void setSpeed(double s) { this.speed = s; }
////    public double getSpeed() { return speed; }
////
////    public Lane getCurrentLane() { return currentLane; }
////    public void setCurrentLane(Lane lane) { this.currentLane = lane; }
////
////    public double getPositionInLane() { return positionInLane; }
////    public void setPositionInLane(double pos) { this.positionInLane = pos; }
////
////    public double getMaxSpeed() { return maxSpeed; }
////    public VehicleType getType() { return type; }
////    public VehicleState getState() { return state; }
////    public DriverProfile getDriverProfile() { return driverProfile; }
////
////    // ---------------- سبقت ----------------
////    public boolean isOvertaking() { return overtaking; }
////    public boolean isIndicatorVisible() { return indicatorVisible; }
////
////    public void startOvertaking(Lane newLane) {
////        if (overtaking) return;
////        this.originalLane = this.currentLane;
////        this.currentLane = newLane;
////        this.overtaking = true;
////        this.targetSpeed *= 1.1; // سرعت ۱۰٪ بیشتر
////        this.indicatorVisible = true;
////    }
////
////    public void finishOvertaking() {
////        if (!overtaking) return;
////        this.currentLane = this.originalLane;
////        this.originalLane = null;
////        this.overtaking = false;
////        this.targetSpeed = 42.0;
////        this.indicatorVisible = false;
////    }
////
////    // ---------------- آپدیت ----------------
////    @Override
////    public void update() {
////        // ۱) تنظیم سرعت
////        if (speed < targetSpeed) {
////            speed += acceleration * dtSeconds;
////            if (speed > targetSpeed) speed = targetSpeed;
////        } else if (speed > targetSpeed) {
////            speed -= deceleration * dtSeconds;
////            if (speed < targetSpeed) speed = targetSpeed;
////        }
////        if (speed < 0) speed = 0;
////        if (speed > maxSpeed) speed = maxSpeed;
////
////        // ۲) آپدیت موقعیت روی لاین
////        positionInLane += speed * dtSeconds;
////
////        // ۳) مدیریت چراغ راهنما (چشمک زن)
////        if (overtaking) {
////            long now = System.currentTimeMillis();
////            if (now - indicatorLastToggle > 500) { // هر نیم ثانیه
////                indicatorVisible = !indicatorVisible;
////                indicatorLastToggle = now;
////            }
////        }
////    }
////
////    // ---------------- زاویه (فعلاً غیرفعال) ----------------
////    public double getAngle() {
////        return 0; // موقت: همیشه صفر
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
//////package core;
//////
//////import infrastructure.Lane;
//////import infrastructure.Intersection;
//////import simulation.Updatable;
//////
//////public class Vehicle implements Identifiable, Locatable, Updatable {
//////    private final String id;
//////    private final VehicleType type;
//////    private VehicleState state;
//////
//////    private double speed;          // سرعت فعلی
//////    private double targetSpeed;    // سرعت هدف
//////    private final double acceleration;   // شتاب مثبت
//////    private final double deceleration;   // شتاب منفی
//////    private final double maxSpeed;       // حداکثر سرعت
//////    private boolean brakeLightOn;
//////
//////    private Lane currentLane;
//////    private double positionInLane; // موقعیت ماشین داخل لاین
//////    private Route route;
//////    private Intersection destination;
//////
//////    private double dtSeconds = 0.1;
//////
//////    // سبقت
//////    private boolean overtaking = false;
//////    private Lane originalLane;
//////    private boolean indicatorVisible = false;
//////    private long indicatorLastToggle = 0;
//////
//////    // ---------------- سازنده‌ها ----------------
//////    public Vehicle(String id, VehicleType type, Lane lane) {
//////        this.id = id;
//////        this.type = type;
//////        this.currentLane = lane;
//////        this.state = VehicleState.MOVING;
//////        this.positionInLane = 0;
//////
//////        this.speed = 0;
//////        this.targetSpeed = 42.0;
//////        this.acceleration = 3.0;
//////        this.deceleration = 5.0;
//////        this.maxSpeed = 60.0;
//////    }
//////
//////    public Vehicle(String id, VehicleType type) {
//////        this(id, type, null);
//////    }
//////
//////    // ---------------- Getter/Setter ----------------
//////    @Override
//////    public String getId() { return id; }
//////
//////    @Override
//////    public Point getPosition() {
//////        if (currentLane == null) return new Point(0, 0);
//////        return currentLane.getPositionAt(positionInLane);
//////    }
//////
//////    public void setDtSeconds(double dt) { this.dtSeconds = dt; }
//////    public void setTargetSpeed(double t) { this.targetSpeed = t; }
//////    public void setSpeed(double s) { this.speed = s; }
//////    public double getSpeed() { return speed; }
//////
//////    public Lane getCurrentLane() { return currentLane; }
//////    public void setCurrentLane(Lane lane) { this.currentLane = lane; }
//////
//////    public double getPositionInLane() { return positionInLane; }
//////    public void setPositionInLane(double pos) { this.positionInLane = pos; }
//////
//////    public double getMaxSpeed() { return maxSpeed; }
//////    public VehicleType getType() { return type; }
//////    public VehicleState getState() { return state; }
//////
//////    // ---------------- سبقت ----------------
//////    public boolean isOvertaking() { return overtaking; }
//////    public boolean isIndicatorVisible() { return indicatorVisible; }
//////
//////    public void startOvertaking(Lane newLane) {
//////        if (overtaking) return;
//////        this.originalLane = this.currentLane;
//////        this.currentLane = newLane;
//////        this.overtaking = true;
//////        this.targetSpeed *= 1.1; // سرعت ۱۰٪ بیشتر
//////        this.indicatorVisible = true;
//////    }
//////
//////    public void finishOvertaking() {
//////        if (!overtaking) return;
//////        this.currentLane = this.originalLane;
//////        this.originalLane = null;
//////        this.overtaking = false;
//////        this.targetSpeed = 42.0;
//////        this.indicatorVisible = false;
//////    }
//////
//////    // ---------------- آپدیت ----------------
//////    @Override
//////    public void update() {
//////        // ۱) تنظیم سرعت
//////        if (speed < targetSpeed) {
//////            speed += acceleration * dtSeconds;
//////            if (speed > targetSpeed) speed = targetSpeed;
//////        } else if (speed > targetSpeed) {
//////            speed -= deceleration * dtSeconds;
//////            if (speed < targetSpeed) speed = targetSpeed;
//////        }
//////        if (speed < 0) speed = 0;
//////        if (speed > maxSpeed) speed = maxSpeed;
//////
//////        // ۲) آپدیت موقعیت روی لاین
//////        positionInLane += speed * dtSeconds;
//////
//////        // ۳) مدیریت چراغ راهنما (چشمک زن)
//////        if (overtaking) {
//////            long now = System.currentTimeMillis();
//////            if (now - indicatorLastToggle > 500) { // هر نیم ثانیه
//////                indicatorVisible = !indicatorVisible;
//////                indicatorLastToggle = now;
//////            }
//////        }
//////    }
//////
//////    // ---------------- زاویه (فعلاً غیرفعال) ----------------
//////    public double getAngle() {
//////        return 0; // موقت: همیشه صفر
//////    }
//////}
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
//////package core;
//////
//////import infrastructure.Lane;
//////import infrastructure.Intersection;
//////import simulation.Updatable;
//////
///////**
////// * Vehicle: منطق حرکت، سبقت، چراغ ترمز/راهنما
////// */
//////public class Vehicle implements Identifiable, Updatable {
//////
//////    // ---- هویت/نوع/رفتار ----
//////    private String id;
//////    private VehicleType type;
//////    private VehicleState state;
//////    private DriverProfile driverProfile;
//////
//////    // ---- دینامیک حرکت ----
//////    private double speed;           // px/s
//////    private double acceleration;    // px/s^2 (گاز)
//////    private double deceleration;    // px/s^2 (ترمز)
//////    private double maxSpeed;        // px/s
//////    private double targetSpeed;     // px/s (هدف)
//////    private double dtSeconds = 0.1; // گام زمانی
//////
//////    // ---- سیگنال‌ها ----
//////    private boolean brakeLightOn;
//////    private boolean turningLeft;
//////    private boolean turningRight;
//////
//////    // ---- سبقت ----
//////    private boolean overtaking;        // آیا در حال سبقت هست؟
//////    private Lane originalLane;         // لاین اصلی قبل از سبقت
//////    private double blinkTimer;         // تایمر برای چشمک راهنما
//////    private boolean indicatorVisible;  // وضعیت روشن/خاموش در چشمک‌زن
//////
//////    // ---- مسیر/موقعیت ----
//////    private Lane currentLane;
//////    private double positionInLane;
//////    private Route route;
//////    private Intersection destination;
//////
//////    // ================== سازنده‌ها ==================
//////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) {
//////        this.id = id;
//////        this.type = type;
//////        this.driverProfile = profile;
//////
//////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////        this.speed = 0.0;
//////        this.targetSpeed = Math.min(this.maxSpeed, 30.0);
//////
//////        this.acceleration = 15.0;
//////        this.deceleration = 30.0;
//////
//////        this.state = VehicleState.STOPPED;
//////        this.brakeLightOn = false;
//////
//////        this.turningLeft = false;
//////        this.turningRight = false;
//////
//////        this.overtaking = false;
//////        this.originalLane = null;
//////        this.blinkTimer = 0.0;
//////        this.indicatorVisible = false;
//////
//////        this.currentLane = null;
//////        this.positionInLane = 0.0;
//////        this.route = null;
//////        this.destination = null;
//////    }
//////
//////    public Vehicle(String id, Point spawn) {
//////        this(id, VehicleType.CAR, 80.0, DriverProfile.LAW_ABIDING);
//////    }
//////
//////    // ================== حلقهٔ حرکت هر تیک ==================
//////    @Override
//////    public void update() {
//////        if (currentLane == null) {
//////            this.state = VehicleState.STOPPED;
//////            this.brakeLightOn = false;
//////            return;
//////        }
//////
//////        // 1) نزدیک‌کردن سرعت به هدف با حرکت نرم
//////        double diff = targetSpeed - speed;
//////        double a = (diff > 0) ? acceleration : -deceleration;
//////        double dv = a * dtSeconds;
//////        speed += Math.signum(diff) * Math.min(Math.abs(dv), Math.abs(diff));
//////
//////        if (speed < 0) speed = 0;
//////        if (speed > maxSpeed) speed = maxSpeed;
//////
//////        brakeLightOn = (diff < -1e-3);
//////
//////        // 2) پیشروی روی لِین
//////        positionInLane += speed * dtSeconds;
//////
//////        // طول هندسی لِین
//////        Point A = currentLane.getParentRoad().getStartIntersection().getPosition();
//////        Point B = currentLane.getParentRoad().getEndIntersection().getPosition();
//////        double dx = B.getX() - A.getX();
//////        double dy = B.getY() - A.getY();
//////        double laneLength = Math.sqrt(dx * dx + dy * dy);
//////
//////        if (positionInLane >= laneLength) {
//////            double overflow = positionInLane - laneLength;
//////            Lane next = (route != null) ? route.getNextLane(currentLane) : null;
//////
//////            if (next != null) {
//////                currentLane = next;
//////                positionInLane = Math.max(0.0, overflow);
//////                turningLeft = false;
//////                turningRight = false;
//////                state = VehicleState.TURNING;
//////            } else {
//////                positionInLane = laneLength;
//////                targetSpeed = 0.0;
//////                speed = 0.0;
//////                brakeLightOn = true;
//////                state = VehicleState.STOPPED;
//////            }
//////        } else {
//////            state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED;
//////        }
//////
//////        // 3) آپدیت سبقت (چراغ راهنما چشمک‌زن)
//////        if (overtaking) {
//////            blinkTimer += dtSeconds;
//////            if (blinkTimer >= 0.5) { // هر ۰.۵ ثانیه تغییر وضعیت
//////                indicatorVisible = !indicatorVisible;
//////                blinkTimer = 0.0;
//////            }
//////        } else {
//////            indicatorVisible = false;
//////        }
//////    }
//////
//////    // ================== منطق سبقت ==================
//////    public void startOvertaking(Lane leftLane) {
//////        if (leftLane == null || overtaking) return;
//////
//////        this.originalLane = currentLane;
//////        this.currentLane = leftLane;
//////        this.overtaking = true;
//////
//////        // چراغ راهنمای چپ فعال
//////        setTurningLeft(true);
//////
//////        // سرعت ۱۰٪ بیشتر
//////        this.targetSpeed = Math.min(this.maxSpeed, this.targetSpeed * 1.1);
//////    }
//////
//////    public void finishOvertaking() {
//////        if (!overtaking || originalLane == null) return;
//////
//////        this.currentLane = originalLane;
//////        this.overtaking = false;
//////        this.originalLane = null;
//////
//////        // خاموش کردن چراغ راهنما
//////        clearIndicators();
//////    }
//////
//////    public boolean isOvertaking() { return overtaking; }
//////
//////    public boolean isIndicatorVisible() { return indicatorVisible; }
//////
//////    // ================== کنترل سرعت/زمان ==================
//////    public void setTargetSpeed(double targetPxPerSec) {
//////        if (targetPxPerSec < 0) targetPxPerSec = 0;
//////        if (targetPxPerSec > maxSpeed) targetPxPerSec = maxSpeed;
//////        this.targetSpeed = targetPxPerSec;
//////    }
//////    public double getTargetSpeed() { return targetSpeed; }
//////
//////    public void setDtSeconds(double dtSeconds) {
//////        if (dtSeconds <= 0) dtSeconds = 0.05;
//////        this.dtSeconds = dtSeconds;
//////    }
//////
//////    public double getAngle() {
//////        if (currentLane == null) return 0.0;
//////        return currentLane.getAngleRadians();
//////    }
//////
//////    // ================== راهنما/ترمز ==================
//////    public boolean isBrakeLightOn() { return brakeLightOn; }
//////
//////    public boolean isTurningLeft()  { return turningLeft; }
//////    public boolean isTurningRight() { return turningRight; }
//////
//////    public void setTurningLeft(boolean on)  { this.turningLeft = on;  if (on) this.turningRight = false; }
//////    public void setTurningRight(boolean on) { this.turningRight = on; if (on) this.turningLeft  = false; }
//////    public void clearIndicators() { this.turningLeft = false; this.turningRight = false; }
//////
//////    // ================== گتر/ستر ==================
//////    @Override public String getId() { return id; }
//////
//////    public VehicleType getType() { return type; }
//////
//////    public VehicleState getState() { return state; }
//////    public void setState(VehicleState state) { this.state = state; }
//////
//////    public double getSpeed() { return speed; }
//////    public void setSpeed(double speed) {
//////        if (speed < 0) speed = 0;
//////        if (speed > maxSpeed) speed = maxSpeed;
//////        this.speed = speed;
//////    }
//////
//////    public double getAcceleration() { return acceleration; }
//////    public void setAcceleration(double acceleration) { this.acceleration = Math.max(0.0, acceleration); }
//////
//////    public double getDeceleration() { return deceleration; }
//////    public void setDeceleration(double deceleration) { this.deceleration = Math.max(10.0, deceleration); }
//////
//////    public double getMaxSpeed() { return maxSpeed; }
//////    public void setMaxSpeed(double maxSpeed) {
//////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////        if (targetSpeed > this.maxSpeed) targetSpeed = this.maxSpeed;
//////        if (speed > this.maxSpeed) speed = this.maxSpeed;
//////    }
//////
//////    public Lane getCurrentLane() { return currentLane; }
//////    public void setCurrentLane(Lane currentLane) { this.currentLane = currentLane; }
//////
//////    public double getPositionInLane() { return positionInLane; }
//////    public void setPositionInLane(double positionInLane) { this.positionInLane = Math.max(0.0, positionInLane); }
//////
//////    public Route getRoute() { return route; }
//////    public void setRoute(Route route) { this.route = route; }
//////
//////    public Intersection getDestination() { return destination; }
//////    public void setDestination(Intersection destination) { this.destination = destination; }
//////
//////    public DriverProfile getDriverProfile() { return driverProfile; }
//////}
//////
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
////
////
////
////
////
////
//////
//////package core;
//////
//////import infrastructure.Lane;
//////import infrastructure.Intersection;
//////import simulation.Updatable;
//////
///////**
////// * Vehicle: منطق حرکت با شتاب/ترمز، هدف‌گذاری سرعت، چراغ ترمز/راهنما،
////// * و سوئیچ بین لِین‌ها طبق Route.
////// * واحدها در این نسخه «پیکسل/ثانیه» و «پیکسل/ثانیه²» هستند تا با رندر هماهنگ باشد.
////// * dt از World/Clock ست می‌شود (setDtSeconds).
////// */
//////public class Vehicle implements Identifiable, Updatable {
//////
//////    // ---- هویت/نوع/رفتار ----
//////    private String id;
//////    private VehicleType type;
//////    private VehicleState state;
//////    private DriverProfile driverProfile;
//////
//////    // ---- دینامیک حرکت ----
//////    private double speed;           // px/s
//////    private double acceleration;    // px/s^2 (گاز)
//////    private double deceleration;    // px/s^2 (ترمز)
//////    private double maxSpeed;        // px/s
//////    private double targetSpeed;     // px/s (هدف)
//////
//////    private double dtSeconds = 0.1; // گام زمانی؛ با SimulationClock هماهنگ شود
//////
//////    // ---- سیگنال‌ها ----
//////    private boolean brakeLightOn;
//////    private boolean turningLeft;
//////    private boolean turningRight;
//////
//////    // ---- مسیر/موقعیت ----
//////    private Lane currentLane;
//////    private double positionInLane;  // 0..length
//////    private Route route;
//////    private Intersection destination;
//////
//////    // ================== سازنده‌ها ==================
//////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) {
//////        this.id = id;
//////        this.type = type;
//////        this.driverProfile = profile;
//////
//////        // اگر maxSpeed را بر حسب km/h داده‌ای، آن را بیرون از این کلاس به px/s تبدیل کن.
//////        // فعلاً فرض: ورودی همین px/s است. برای راحتی می‌توانی ~ (km/h)/3.6*scale انجام دهی.
//////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////
//////        this.speed = 0.0;
//////        this.targetSpeed = Math.min(this.maxSpeed, 30.0);
//////
//////        // شتاب‌های معقول برای حرکت نرم
//////        this.acceleration = 60.0;   // طی ~1s به 60 px/s
//////        this.deceleration = 120.0;  // ترمز قوی‌تر
//////
//////        this.state = VehicleState.STOPPED;
//////        this.brakeLightOn = false;
//////        this.turningLeft = false;
//////        this.turningRight = false;
//////
//////        this.currentLane = null;
//////        this.positionInLane = 0.0;
//////        this.route = null;
//////        this.destination = null;
//////    }
//////
//////    public Vehicle(String id, Point spawn) {
//////        this(id, VehicleType.CAR, 80.0, DriverProfile.LAW_ABIDING);
//////        // موقعیت فضایی از هندسهٔ Lane استخراج می‌شود؛ این سازنده فقط برای سازگاری است.
//////    }
//////
//////    // ================== حلقهٔ حرکت هر تیک ==================
//////    @Override
//////    public void update() {
//////        if (currentLane == null) {
//////            this.state = VehicleState.STOPPED;
//////            this.brakeLightOn = false;
//////            return;
//////        }
//////
//////        // 1) نزدیک‌کردن سرعت به هدف با شتاب/ترمز
//////        double diff = targetSpeed - speed;
//////        double a = (diff > 0) ? acceleration : -deceleration;
//////        double dv = a * dtSeconds;
//////
//////        if (Math.abs(dv) > Math.abs(diff)) {
//////            speed = targetSpeed;          // جلوگیری از overshoot
//////        } else {
//////            speed += dv;
//////        }
//////
//////        if (speed < 0) speed = 0;
//////        if (speed > maxSpeed) speed = maxSpeed;
//////
//////        // چراغ ترمز وقتی کاهش سرعت داریم
//////        brakeLightOn = (diff < -1e-6);
//////
//////        // 2) پیشروی روی لِین
//////        positionInLane += speed * dtSeconds;
//////
//////        // طول هندسی لِین از Road
//////        Point A = currentLane.getParentRoad().getStartIntersection().getPosition();
//////        Point B = currentLane.getParentRoad().getEndIntersection().getPosition();
//////        double dx = B.getX() - A.getX();
//////        double dy = B.getY() - A.getY();
//////        double laneLength = Math.sqrt(dx * dx + dy * dy);
//////
//////        // 3) انتهای لِین → رفتن به لِین بعدی Route
//////        if (positionInLane >= laneLength) {
//////            double overflow = positionInLane - laneLength;
//////            Lane next = (route != null) ? route.getNextLane(currentLane) : null;
//////
//////            if (next != null) {
//////                // ورود به لِین بعدی
//////                currentLane = next;
//////                positionInLane = Math.max(0.0, overflow);
//////                // راهنماها در این نسخه دستی/خارجی ست می‌شوند:
//////                turningLeft = false;
//////                turningRight = false;
//////                state = VehicleState.TURNING; // اختیاری برای یک فریم
//////            } else {
//////                // پایان مسیر
//////                positionInLane = laneLength;
//////                targetSpeed = 0.0;
//////                speed = 0.0;
//////                brakeLightOn = true;
//////                state = VehicleState.STOPPED;
//////            }
//////        } else {
//////            state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED;
//////        }
//////    }
//////
//////    // ================== کنترل سرعت/زمان ==================
//////    public void setTargetSpeed(double targetPxPerSec) {
//////        if (targetPxPerSec < 0) targetPxPerSec = 0;
//////        if (targetPxPerSec > maxSpeed) targetPxPerSec = maxSpeed;
//////        this.targetSpeed = targetPxPerSec;
//////    }
//////    public double getTargetSpeed() { return targetSpeed; }
//////
//////    /**
//////     * World/Clock باید dtSeconds را قبل از شروع شبیه‌سازی ست کند:
//////     *   v.setDtSeconds(clock.getTickInterval()/1000.0)
//////     */
//////    public void setDtSeconds(double dtSeconds) {
//////        if (dtSeconds <= 0) dtSeconds = 0.05;
//////        this.dtSeconds = dtSeconds;
//////    }
//////
//////    // ================== زاویه برای رندر ==================
//////    public double getAngle() {
//////        if (currentLane == null) return 0.0;
//////        return currentLane.getAngleRadians();
//////    }
//////
//////    // ================== راهنما/ترمز ==================
//////    public boolean isBrakeLightOn() { return brakeLightOn; }
//////
//////    public boolean isTurningLeft()  { return turningLeft; }
//////    public boolean isTurningRight() { return turningRight; }
//////
//////    public void setTurningLeft(boolean on)  { this.turningLeft = on;  if (on) this.turningRight = false; }
//////    public void setTurningRight(boolean on) { this.turningRight = on; if (on) this.turningLeft  = false; }
//////    public void clearIndicators() { this.turningLeft = false; this.turningRight = false; }
//////
//////    // ================== گتر/ستر ==================
//////    @Override public String getId() { return id; }
//////
//////    public VehicleType getType() { return type; }
//////
//////    public VehicleState getState() { return state; }
//////    public void setState(VehicleState state) { this.state = state; }
//////
//////    public double getSpeed() { return speed; }
//////    public void setSpeed(double speed) {
//////        if (speed < 0) speed = 0;
//////        if (speed > maxSpeed) speed = maxSpeed;
//////        this.speed = speed;
//////    }
//////
//////    public double getAcceleration() { return acceleration; }
//////    public void setAcceleration(double acceleration) { this.acceleration = Math.max(0.0, acceleration); }
//////
//////    public double getDeceleration() { return deceleration; }
//////    public void setDeceleration(double deceleration) { this.deceleration = Math.max(10.0, deceleration); }
//////
//////    public double getMaxSpeed() { return maxSpeed; }
//////    public void setMaxSpeed(double maxSpeed) {
//////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////        if (targetSpeed > this.maxSpeed) targetSpeed = this.maxSpeed;
//////        if (speed > this.maxSpeed) speed = this.maxSpeed;
//////    }
//////
//////    public Lane getCurrentLane() { return currentLane; }
//////    public void setCurrentLane(Lane currentLane) { this.currentLane = currentLane; }
//////
//////    public double getPositionInLane() { return positionInLane; }
//////    public void setPositionInLane(double positionInLane) { this.positionInLane = Math.max(0.0, positionInLane); }
//////
//////    public Route getRoute() { return route; }
//////    public void setRoute(Route route) { this.route = route; }
//////
//////    public Intersection getDestination() { return destination; }
//////    public void setDestination(Intersection destination) { this.destination = destination; }
//////
//////    public DriverProfile getDriverProfile() { return driverProfile; }
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
////////
////////// core/Vehicle.java
////////package core;
////////
////////import infrastructure.*;
////////        import simulation.Updatable;
////////
////////public class Vehicle implements Identifiable, Updatable {
////////    private String id;
////////    private VehicleType type;
////////    private VehicleState state;
////////    private DriverProfile driverProfile;
////////
////////    private double speed;           // px/s
////////    private double acceleration;    // px/s^2
////////    private double deceleration;    // px/s^2
////////    private double maxSpeed;        // px/s
////////    private double targetSpeed;     // px/s
////////    private double dtSeconds = 0.1; // simulation step
////////
////////    private boolean brakeLightOn;
////////    private boolean turningLeft;
////////    private boolean turningRight;
////////
////////    private Lane currentLane;
////////    private double positionInLane;
////////    private Route route;
////////    private Intersection destination;
////////
////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile){
////////        this.id = id; this.type = type; this.driverProfile = profile;
////////        this.maxSpeed = Math.max(10.0, maxSpeed);
////////        this.speed = 0; this.targetSpeed = Math.min(this.maxSpeed, 30.0);
////////        this.acceleration = 60; this.deceleration = 120;
////////        this.state = VehicleState.STOPPED;
////////    }
////////
////////    @Override public String getId(){ return id; }
////////
////////    @Override
////////    public void update(){
////////        if(currentLane==null){ state=VehicleState.STOPPED; brakeLightOn=false; return; }
////////
////////        double diff = targetSpeed - speed;
////////        double a = (diff > 0) ? acceleration : -deceleration;
////////        double dv = a * dtSeconds;
////////        if(Math.abs(dv)>Math.abs(diff)) speed = targetSpeed;
////////        else speed += dv;
////////        if(speed<0) speed=0; if(speed>maxSpeed) speed=maxSpeed;
////////        brakeLightOn = (diff<0);
////////
////////        positionInLane += speed * dtSeconds;
////////        double laneLength = currentLane.getLength();
////////
////////        if(positionInLane >= laneLength){
////////            double overflow = positionInLane - laneLength;
////////            Lane next = (route!=null) ? route.getNextLane(currentLane) : null;
////////            if(next!=null){
////////                currentLane = next;
////////                positionInLane = Math.max(0, overflow);
////////                state = VehicleState.TURNING;
////////            } else {
////////                positionInLane = laneLength;
////////                targetSpeed = 0; speed=0; brakeLightOn=true;
////////                state=VehicleState.STOPPED;
////////            }
////////        } else {
////////            state = (speed>0.1) ? VehicleState.MOVING : VehicleState.STOPPED;
////////        }
////////    }
////////
////////    public void setDtSeconds(double dt){ this.dtSeconds = dt; }
////////    public double getSpeed(){ return speed; }
////////    public void setTargetSpeed(double s){ targetSpeed=Math.max(0,Math.min(s,maxSpeed)); }
////////    public double getCruiseSpeed(){ return maxSpeed*0.7; }
////////    public double getPositionInLane(){ return positionInLane; }
////////    public Lane getCurrentLane(){ return currentLane; }
////////    public void setCurrentLane(Lane l){ this.currentLane=l; }
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
////////
////////
////////
////////
////////
//////////package core;
//////////
//////////import infrastructure.Lane;
//////////import simulation.*;
//////////
//////////
//////////
//////////
//////////public class Vehicle implements Identifiable, Locatable, Updatable {
//////////    private final String id;
//////////    private final VehicleType type;
//////////    private DriverProfile driverProfile = DriverProfile.LAW_ABIDING;
//////////
//////////    private double speed;         // px/s
//////////    private double acceleration;  // px/s^2
//////////    private double deceleration;  // px/s^2 (قدر مطلق)
//////////    private double cruiseSpeed;   // هدف
//////////
//////////    private Lane currentLane;
//////////    private double positionInLane; // px
//////////
//////////    public Vehicle(String id, VehicleType type, Lane lane, double cruiseSpeed) {
//////////        this.id = id;
//////////        this.type = type;
//////////        this.currentLane = lane;
//////////        this.cruiseSpeed = cruiseSpeed;
//////////        this.speed = 0;
//////////    }
//////////
//////////    @Override public String getId() { return id; }
//////////    public VehicleType getType() { return type; }
//////////
//////////    public void setAcceleration(double a){ this.acceleration = a; }
//////////    public void setDeceleration(double d){ this.deceleration = Math.abs(d); }
//////////    public void setCruiseSpeed(double v){ this.cruiseSpeed = Math.max(0, v); }
//////////
//////////    public Lane getCurrentLane(){ return currentLane; }
//////////    public double getPositionInLane(){ return positionInLane; }
//////////    public void setDtSeconds(double dt){} // سازگاری با کد قبلی (بدون اثر)
//////////
//////////    public void setTargetSpeed(double v){ this.cruiseSpeed = Math.max(0, v); }
//////////    public double getTargetSpeed(){ return cruiseSpeed; }
//////////    public double getSpeed(){ return speed; }
//////////    public double getCruiseSpeed(){ return cruiseSpeed; }
//////////
//////////    @Override
//////////    public Point getPosition() {
//////////        if (currentLane == null) return new Point(0,0);
//////////        return currentLane.getPositionAt(positionInLane);
//////////    }
//////////
//////////    @Override
//////////    public void update() {
//////////        // آپدیت مینیمال برای سازگاری؛ می‌تونیم بعداً مدل حرکت رو کامل کنیم
//////////        double dv = (speed < cruiseSpeed ? acceleration : -deceleration);
//////////        speed += dv * 0.016; // فرض 16ms
//////////        if (speed < 0) speed = 0;
//////////        if (speed > cruiseSpeed) speed = cruiseSpeed;
//////////        positionInLane += speed * 0.016;
//////////        double len = (currentLane != null ? currentLane.getLength() : 0);
//////////        if (len > 0 && positionInLane > len) positionInLane = len;
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
//////////package core;
//////////
//////////import infrastructure.Lane;
//////////import core.VehicleType;
//////////import core.VehicleState;
//////////
//////////public class Vehicle implements Identifiable, Locatable, simulation.Updatable {
//////////    private final String id;
//////////    private VehicleType type = VehicleType.CAR;
//////////    private VehicleState state = VehicleState.MOVING;
//////////
//////////    private double speed = 0.0;          // m/s یا px/s
//////////    private double acceleration = 0.0;
//////////    private double deceleration = 0.0;
//////////    private double maxSpeed = 20.0;
//////////    private double cruiseSpeed = 12.0;
//////////
//////////    private Lane currentLane;
//////////    private double positionInLane = 0.0; // فاصله از ابتدای لاین
//////////    private double dtSeconds = 0.016;    // برای آپدیت
//////////
//////////    private Route route;
//////////    private infrastructure.Intersection destination;
//////////    private DriverProfile driverProfile = DriverProfile.LAW_ABIDING;
//////////
//////////    public Vehicle(String id) { this.id = id; }
//////////    public Vehicle(String id, VehicleType type, DriverProfile profile) {
//////////        this.id = id; this.type = type; this.driverProfile = profile;
//////////    }
//////////
//////////    @Override public String getId() { return id; }
//////////
//////////    // === سازگاری با کد موجود ===
//////////    public void setDtSeconds(double dt)      { this.dtSeconds = Math.max(1e-6, dt); }
//////////    public void setAcceleration(double a)    { this.acceleration = a; }
//////////    public void setDeceleration(double d)    { this.deceleration = d; }
//////////    public void setCruiseSpeed(double c)     { this.cruiseSpeed = c; }
//////////    public double getCruiseSpeed()           { return cruiseSpeed; }
//////////
//////////    public double getSpeed()                 { return speed; }
//////////    public void setSpeed(double s)           { this.speed = s; }
//////////
//////////    public Lane getCurrentLane()             { return currentLane; }
//////////    public void setCurrentLane(Lane ln)      { this.currentLane = ln; }
//////////    public double getPositionInLane()        { return positionInLane; }
//////////    public void setPositionInLane(double s)  { this.positionInLane = Math.max(0.0, s); }
//////////
//////////    /** برای رندر/منطق: موقعیت جهان از روی لاین فعلی. */
//////////    @Override public Point getPosition() {
//////////        if (currentLane == null) return new Point(0,0);
//////////        return currentLane.getPositionAt(positionInLane);
//////////    }
//////////    /** آلیاس برای جاهایی که مستقیماً از Vehicle می‌خوان: */
//////////    public Point getPositionAt(double s) {
//////////        if (currentLane == null) return new Point(0,0);
//////////        return currentLane.getPositionAt(s);
//////////    }
//////////
//////////    public void setTargetSpeed(double v)     { this.maxSpeed = Math.max(0.0, v); }
//////////    public double getTargetSpeed()           { return maxSpeed; }
//////////
//////////    // ساده‌شده‌ی آپدیت (برای جلوگیری از لگ/ایست نابجا فقط پایه‌ها را می‌چرخاند)
//////////    @Override public void update() {
//////////        // نزدیک شدن به هدف‌سرعت
//////////        double desired = Math.min(maxSpeed, cruiseSpeed);
//////////        double dv = desired - speed;
//////////        double a = (dv >= 0 ? acceleration : -Math.max(deceleration, 0.0));
//////////        speed += a * dtSeconds;
//////////        if ((dv >= 0 && speed > desired) || (dv < 0 && speed < desired)) speed = desired;
//////////
//////////        // حرکت روی لاین
//////////        if (currentLane != null) {
//////////            positionInLane += speed * dtSeconds;
//////////            double len = currentLane.getLength();
//////////            if (positionInLane > len) positionInLane = len; // فعلاً ساده
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
////////
////////
////////
////////
//////////
//////////
//////////package core;
//////////
//////////import infrastructure.Lane;
//////////import simulation.*;
//////////
//////////
//////////public class Vehicle implements Identifiable, Locatable, Updatable {
//////////
//////////    private final String id;
//////////    private VehicleType type = VehicleType.CAR;
//////////    private VehicleState state = VehicleState.MOVING;
//////////
//////////    private double speed;           // m/s
//////////    private double acceleration;    // m/s^2
//////////    private double deceleration;    // m/s^2
//////////    private double maxSpeed = 20;   // m/s ~ 72km/h
//////////
//////////    private double cruiseSpeed = 13.9; // ~50km/h
//////////    private boolean brakeLightOn;
//////////
//////////    private Lane currentLane;
//////////    private double positionInLane; // متر
//////////
//////////    public Vehicle(String id) { this.id = id; }
//////////
//////////    @Override public String getId() { return id; }
//////////    @Override public Point getPosition() { return (currentLane == null) ? new Point(0,0) : currentLane.getPositionAt(positionInLane); }
//////////
//////////    public Lane getCurrentLane() { return currentLane; }
//////////    public void setCurrentLane(Lane lane) { this.currentLane = lane; }
//////////
//////////    public double getPositionInLane() { return positionInLane; }
//////////    public void setPositionInLane(double s) { this.positionInLane = Math.max(0, s); }
//////////
//////////    public double getSpeed() { return speed; }
//////////    public void setSpeed(double v) { this.speed = Math.max(0, v); }
//////////
//////////    public double getCruiseSpeed() { return cruiseSpeed; }
//////////    public void setCruiseSpeed(double v) { this.cruiseSpeed = Math.max(0, Math.min(v, maxSpeed)); }
//////////
//////////    public double getTargetSpeed() { return cruiseSpeed; }     // برای سازگاری با کد فعلی
//////////    public void   setTargetSpeed(double v) { setCruiseSpeed(v); }
//////////
//////////    @Override public void update() {
//////////        // فیزیک بسیار ساده
//////////        if (speed < cruiseSpeed) speed = Math.min(cruiseSpeed, speed + 0.5);
//////////        else if (speed > cruiseSpeed) speed = Math.max(cruiseSpeed, speed - 0.7);
//////////        positionInLane += speed * 0.1; // فرض dt=0.1s
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
//////////package core; // // پکیج هسته
//////////
//////////import infrastructure.Lane; // // لِین
//////////import infrastructure.Intersection; // // تقاطع
//////////import simulation.Updatable; // // آپدیت‌پذیر
//////////
//////////public class Vehicle implements Identifiable, Updatable { // // کلاس خودرو
//////////    private String id; // // شناسه
//////////    private VehicleType type; // // نوع
//////////    private VehicleState state; // // وضعیت
//////////    private DriverProfile driverProfile; // // پروفایل
//////////
//////////    private double speed; // // px/s
//////////    private double acceleration; // // px/s^2
//////////    private double deceleration; // // px/s^2
//////////    private double maxSpeed; // // px/s
//////////    private double targetSpeed; // // px/s
//////////    private double cruiseSpeed; // // px/s - سرعت «کروز» برای برگشت بعد از توقف
//////////
//////////    private double dtSeconds = 0.1; // // گام زمانی
//////////
//////////    private boolean brakeLightOn; // // چراغ ترمز
//////////    private boolean turningLeft; // // راهنمای چپ
//////////    private boolean turningRight; // // راهنمای راست
//////////
//////////    private Lane currentLane; // // لِین فعلی
//////////    private double positionInLane; // // موقعیت طولی
//////////    private Route route; // // مسیر
//////////    private Intersection destination; // // مقصد
//////////
//////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) { // // سازنده
//////////        this.id = id; // // ست id
//////////        this.type = type; // // ست نوع
//////////        this.driverProfile = profile; // // ست پروفایل
//////////        this.maxSpeed = Math.max(10.0, maxSpeed); // // سقف منطقی
//////////        this.speed = 0.0; // // شروع از سکون
//////////        this.targetSpeed = Math.min(this.maxSpeed, 30.0); // // هدف اولیه
//////////        this.cruiseSpeed = Math.max(30.0, Math.min(this.maxSpeed, 55.0)); // // کروز پیش‌فرض
//////////
//////////        this.acceleration = 60.0; // // گاز
//////////        this.deceleration = 120.0; // // ترمز
//////////
//////////        this.state = VehicleState.STOPPED; // // وضعیت
//////////        this.brakeLightOn = false; // // خاموش
//////////        this.turningLeft = false; // // خاموش
//////////        this.turningRight = false; // // خاموش
//////////
//////////        this.currentLane = null; // // هنوز روی لِین نیست
//////////        this.positionInLane = 0.0; // // صفر
//////////        this.route = null; // // مسیر خالی
//////////        this.destination = null; // // مقصد خالی
//////////    }
//////////
//////////    public Vehicle(String id, Point spawn) { // // سازنده سازگاری
//////////        this(id, VehicleType.CAR, 80.0, DriverProfile.LAW_ABIDING); // // دیفالت‌ها
//////////    }
//////////
//////////    @Override
//////////    public void update() { // // آپدیت حرکت
//////////        if (currentLane == null) { // // بدون لِین
//////////            this.state = VehicleState.STOPPED; // // ایست
//////////            this.brakeLightOn = false; // // چراغ ترمز خاموش
//////////            return; // // خروج
//////////        }
//////////
//////////        double diff = targetSpeed - speed; // // اختلاف سرعت
//////////        double a = (diff > 0) ? acceleration : -deceleration; // // شتاب یا ترمز
//////////        double dv = a * dtSeconds; // // تغییر سرعت
//////////
//////////        if (Math.abs(dv) > Math.abs(diff)) { speed = targetSpeed; } else { speed += dv; } // // نزدیک کردن
//////////
//////////        if (speed < 0) speed = 0; // // کلمپ پایین
//////////        if (speed > maxSpeed) speed = maxSpeed; // // سقف
//////////
//////////        brakeLightOn = (diff < -1e-6); // // چراغ ترمز در کاهش سرعت
//////////
//////////        positionInLane += speed * dtSeconds; // // پیشروی روی لِین
//////////
//////////        // طول هندسی لِین (از جاده)
//////////        Point A = currentLane.getParentRoad().getStartIntersection().getPosition(); // // A
//////////        Point B = currentLane.getParentRoad().getEndIntersection().getPosition(); // // B
//////////        double dx = B.getX() - A.getX(); // // Δx
//////////        double dy = B.getY() - A.getY(); // // Δy
//////////        double laneLength = Math.sqrt(dx * dx + dy * dy); // // طول
//////////
//////////        if (positionInLane >= laneLength) { // // انتهای لِین
//////////            double overflow = positionInLane - laneLength; // // باقیمانده
//////////            Lane next = (route != null) ? route.getNextLane(currentLane) : null; // // لِین بعدی
//////////
//////////            if (next != null) { // // اگر لِین بعدی داریم
//////////                currentLane = next; // // سوئیچ لِین
//////////                positionInLane = Math.max(0.0, overflow); // // ورود با باقیمانده
//////////                turningLeft = false; turningRight = false; // // راهنماها خاموش
//////////                state = VehicleState.TURNING; // // یک فریم
//////////                // بعد از عبور از تقاطع، هدف‌سرعت را به کروز برگردان (اگر صفر شده بود)
//////////                if (targetSpeed < 1e-6) { targetSpeed = cruiseSpeed; } // // بازگشت هدف به کروز
//////////            } else { // // پایان مسیر
//////////                positionInLane = laneLength; // // سرِ لِین
//////////                targetSpeed = 0.0; speed = 0.0; brakeLightOn = true; state = VehicleState.STOPPED; // // ایست
//////////            }
//////////        } else {
//////////            state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED; // // تعیین وضعیت
//////////        }
//////////    }
//////////
//////////    public void setTargetSpeed(double targetPxPerSec) { // // ست هدف سرعت
//////////        if (targetPxPerSec < 0) targetPxPerSec = 0; // // کلمپ
//////////        if (targetPxPerSec > maxSpeed) targetPxPerSec = maxSpeed; // // سقف
//////////        this.targetSpeed = targetPxPerSec; // // ست
//////////        if (targetPxPerSec > 1e-6) { this.cruiseSpeed = targetPxPerSec; } // // اگر هدف غیرصفر، آن را کروز فرض کن
//////////    }
//////////    public double getTargetSpeed() { return targetSpeed; } // // گتر هدف
//////////    public double getCruiseSpeed() { return cruiseSpeed; } // // گتر کروز
//////////
//////////    public void setDtSeconds(double dtSeconds) { if (dtSeconds <= 0) dtSeconds = 0.05; this.dtSeconds = dtSeconds; } // // ست dt
//////////    public double getAngle() { if (currentLane == null) return 0.0; return currentLane.getAngleRadians(); } // // زاویه برای رندر
//////////
//////////    public boolean isBrakeLightOn() { return brakeLightOn; } // // چراغ ترمز
//////////    public boolean isTurningLeft()  { return turningLeft; } // // راهنمای چپ
//////////    public boolean isTurningRight() { return turningRight; } // // راهنمای راست
//////////    public void setTurningLeft(boolean on)  { this.turningLeft = on;  if (on) this.turningRight = false; } // // ست چپ
//////////    public void setTurningRight(boolean on) { this.turningRight = on; if (on) this.turningLeft  = false; } // // ست راست
//////////    public void clearIndicators() { this.turningLeft = false; this.turningRight = false; } // // خاموش کردن
//////////
//////////    @Override public String getId() { return id; } // // گتر id
//////////    public VehicleType getType() { return type; } // // گتر نوع
//////////    public VehicleState getState() { return state; } // // گتر وضعیت
//////////    public void setState(VehicleState state) { this.state = state; } // // ست وضعیت
//////////    public double getSpeed() { return speed; } // // گتر سرعت
//////////    public void setSpeed(double speed) { if (speed < 0) speed = 0; if (speed > maxSpeed) speed = maxSpeed; this.speed = speed; } // // ست سرعت
//////////
//////////    public double getAcceleration() { return acceleration; } // // گتر شتاب
//////////    public void setAcceleration(double acceleration) { this.acceleration = Math.max(0.0, acceleration); } // // ست شتاب
//////////    public double getDeceleration() {
//////////        return deceleration; }// // گتر ترمز
//////////
//////////    public void setDeceleration(double deceleration) {
//////////        this.deceleration = Math.max(10.0, deceleration); } // // ست ترمز
//////////
//////////    public double getMaxSpeed() {
//////////        return maxSpeed; }// // گتر سقف
//////////
//////////    public void setMaxSpeed(double maxSpeed) {
//////////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////////
//////////        if (targetSpeed > this.maxSpeed)
//////////            targetSpeed = this.maxSpeed;
//////////
//////////        if (speed > this.maxSpeed)
//////////            speed = this.maxSpeed; } // // ست سقف
//////////
//////////    public Lane getCurrentLane() {
//////////        return currentLane; } // // گتر لِین
//////////
//////////    public void setCurrentLane(Lane currentLane) {
//////////        this.currentLane = currentLane; } // // ست لِین
//////////
//////////    public double getPositionInLane() {
//////////        return positionInLane; } // // گتر موقعیت
//////////
//////////    public void setPositionInLane(double positionInLane) {
//////////        this.positionInLane = Math.max(0.0, positionInLane); } // // ست موقعیت
//////////    public Route getRoute() {
//////////        return route; } // // گتر مسیر
//////////
//////////    public void setRoute(Route route) {
//////////        this.route = route; } // // ست مسیر
//////////
//////////    public Intersection getDestination() {
//////////        return destination; } // // گتر مقصد
//////////
//////////    public void setDestination(Intersection destination) {
//////////        this.destination = destination; } // // ست مقصد
//////////
//////////    public DriverProfile getDriverProfile() {
//////////        return driverProfile; } // // گتر پروفایل
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
////////
////////
////////
//////////package core;
//////////
//////////import infrastructure.Lane;
//////////import infrastructure.Intersection;
//////////import simulation.Updatable;
////////
///////////**
////////// * Vehicle: منطق حرکت با شتاب/ترمز، هدف‌گذاری سرعت، چراغ ترمز/راهنما،
////////// * و سوئیچ بین لِین‌ها طبق Route.
////////// * واحدها در این نسخه «پیکسل/ثانیه» و «پیکسل/ثانیه²» هستند تا با رندر هماهنگ باشد.
////////// * dt از World/Clock ست می‌شود (setDtSeconds).
////////// */
//////////public class Vehicle implements Identifiable, Updatable {
//////////
//////////    // ---- هویت/نوع/رفتار ----
//////////    private String id;
//////////    private VehicleType type;
//////////    private VehicleState state;
//////////    private DriverProfile driverProfile;
//////////
//////////    // ---- دینامیک حرکت ----
//////////    private double speed;           // px/s
//////////    private double acceleration;    // px/s^2 (گاز)
//////////    private double deceleration;    // px/s^2 (ترمز)
//////////    private double maxSpeed;        // px/s
//////////    private double targetSpeed;     // px/s (هدف)
//////////
//////////    private double dtSeconds = 0.1; // گام زمانی؛ با SimulationClock هماهنگ شود
//////////
//////////    // ---- سیگنال‌ها ----
//////////    private boolean brakeLightOn;
//////////    private boolean turningLeft;
//////////    private boolean turningRight;
//////////
//////////    // ---- مسیر/موقعیت ----
//////////    private Lane currentLane;
//////////    private double positionInLane;  // 0..length
//////////    private Route route;
//////////    private Intersection destination;
//////////
//////////    // ================== سازنده‌ها ==================
//////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) {
//////////        this.id = id;
//////////        this.type = type;
//////////        this.driverProfile = profile;
//////////
//////////        // اگر maxSpeed را بر حسب km/h داده‌ای، آن را بیرون از این کلاس به px/s تبدیل کن.
//////////        // فعلاً فرض: ورودی همین px/s است. برای راحتی می‌توانی ~ (km/h)/3.6*scale انجام دهی.
//////////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////////
//////////        this.speed = 0.0;
//////////        this.targetSpeed = Math.min(this.maxSpeed, 30.0);
//////////
//////////        // شتاب‌های معقول برای حرکت نرم
//////////        this.acceleration = 60.0;   // طی ~1s به 60 px/s
//////////        this.deceleration = 120.0;  // ترمز قوی‌تر
//////////
//////////        this.state = VehicleState.STOPPED;
//////////        this.brakeLightOn = false;
//////////        this.turningLeft = false;
//////////        this.turningRight = false;
//////////
//////////        this.currentLane = null;
//////////        this.positionInLane = 0.0;
//////////        this.route = null;
//////////        this.destination = null;
//////////    }
//////////
//////////    public Vehicle(String id, Point spawn) {
//////////        this(id, VehicleType.CAR, 80.0, DriverProfile.LAW_ABIDING);
//////////        // موقعیت فضایی از هندسهٔ Lane استخراج می‌شود؛ این سازنده فقط برای سازگاری است.
//////////    }
//////////
//////////    // ================== حلقهٔ حرکت هر تیک ==================
//////////    @Override
//////////    public void update() {
//////////        if (currentLane == null) {
//////////            this.state = VehicleState.STOPPED;
//////////            this.brakeLightOn = false;
//////////            return;
//////////        }
//////////
//////////        // 1) نزدیک‌کردن سرعت به هدف با شتاب/ترمز
//////////        double diff = targetSpeed - speed;
//////////        double a = (diff > 0) ? acceleration : -deceleration;
//////////        double dv = a * dtSeconds;
//////////
//////////        if (Math.abs(dv) > Math.abs(diff)) {
//////////            speed = targetSpeed;          // جلوگیری از overshoot
//////////        } else {
//////////            speed += dv;
//////////        }
//////////
//////////        if (speed < 0) speed = 0;
//////////        if (speed > maxSpeed) speed = maxSpeed;
//////////
//////////        // چراغ ترمز وقتی کاهش سرعت داریم
//////////        brakeLightOn = (diff < -1e-6);
//////////
//////////        // 2) پیشروی روی لِین
//////////        positionInLane += speed * dtSeconds;
//////////
//////////        // طول هندسی لِین از Road
//////////        Point A = currentLane.getParentRoad().getStartIntersection().getPosition();
//////////        Point B = currentLane.getParentRoad().getEndIntersection().getPosition();
//////////        double dx = B.getX() - A.getX();
//////////        double dy = B.getY() - A.getY();
//////////        double laneLength = Math.sqrt(dx * dx + dy * dy);
//////////
//////////        // 3) انتهای لِین → رفتن به لِین بعدی Route
//////////        if (positionInLane >= laneLength) {
//////////            double overflow = positionInLane - laneLength;
//////////            Lane next = (route != null) ? route.getNextLane(currentLane) : null;
//////////
//////////            if (next != null) {
//////////                // ورود به لِین بعدی
//////////                currentLane = next;
//////////                positionInLane = Math.max(0.0, overflow);
//////////                // راهنماها در این نسخه دستی/خارجی ست می‌شوند:
//////////                turningLeft = false;
//////////                turningRight = false;
//////////                state = VehicleState.TURNING; // اختیاری برای یک فریم
//////////            } else {
//////////                // پایان مسیر
//////////                positionInLane = laneLength;
//////////                targetSpeed = 0.0;
//////////                speed = 0.0;
//////////                brakeLightOn = true;
//////////                state = VehicleState.STOPPED;
//////////            }
//////////        } else {
//////////            state = (speed > 0.1) ? VehicleState.MOVING : VehicleState.STOPPED;
//////////        }
//////////    }
//////////
//////////    // ================== کنترل سرعت/زمان ==================
//////////    public void setTargetSpeed(double targetPxPerSec) {
//////////        if (targetPxPerSec < 0) targetPxPerSec = 0;
//////////        if (targetPxPerSec > maxSpeed) targetPxPerSec = maxSpeed;
//////////        this.targetSpeed = targetPxPerSec;
//////////    }
//////////    public double getTargetSpeed() { return targetSpeed; }
//////////
//////////    /**
//////////     * World/Clock باید dtSeconds را قبل از شروع شبیه‌سازی ست کند:
//////////     *   v.setDtSeconds(clock.getTickInterval()/1000.0)
//////////     */
//////////    public void setDtSeconds(double dtSeconds) {
//////////        if (dtSeconds <= 0) dtSeconds = 0.05;
//////////        this.dtSeconds = dtSeconds;
//////////    }
//////////
//////////    // ================== زاویه برای رندر ==================
//////////    public double getAngle() {
//////////        if (currentLane == null) return 0.0;
//////////        return currentLane.getAngleRadians();
//////////    }
//////////
//////////    // ================== راهنما/ترمز ==================
//////////    public boolean isBrakeLightOn() { return brakeLightOn; }
//////////
//////////    public boolean isTurningLeft()  { return turningLeft; }
//////////    public boolean isTurningRight() { return turningRight; }
//////////
//////////    public void setTurningLeft(boolean on)  { this.turningLeft = on;  if (on) this.turningRight = false; }
//////////    public void setTurningRight(boolean on) { this.turningRight = on; if (on) this.turningLeft  = false; }
//////////    public void clearIndicators() { this.turningLeft = false; this.turningRight = false; }
//////////
//////////    // ================== گتر/ستر ==================
//////////    @Override public String getId() { return id; }
//////////
//////////    public VehicleType getType() { return type; }
//////////
//////////    public VehicleState getState() { return state; }
//////////    public void setState(VehicleState state) { this.state = state; }
//////////
//////////    public double getSpeed() { return speed; }
//////////    public void setSpeed(double speed) {
//////////        if (speed < 0) speed = 0;
//////////        if (speed > maxSpeed) speed = maxSpeed;
//////////        this.speed = speed;
//////////    }
//////////
//////////    public double getAcceleration() { return acceleration; }
//////////    public void setAcceleration(double acceleration) { this.acceleration = Math.max(0.0, acceleration); }
//////////
//////////    public double getDeceleration() { return deceleration; }
//////////    public void setDeceleration(double deceleration) { this.deceleration = Math.max(10.0, deceleration); }
//////////
//////////    public double getMaxSpeed() { return maxSpeed; }
//////////    public void setMaxSpeed(double maxSpeed) {
//////////        this.maxSpeed = Math.max(10.0, maxSpeed);
//////////        if (targetSpeed > this.maxSpeed) targetSpeed = this.maxSpeed;
//////////        if (speed > this.maxSpeed) speed = this.maxSpeed;
//////////    }
//////////
//////////    public Lane getCurrentLane() { return currentLane; }
//////////    public void setCurrentLane(Lane currentLane) { this.currentLane = currentLane; }
//////////
//////////    public double getPositionInLane() { return positionInLane; }
//////////    public void setPositionInLane(double positionInLane) { this.positionInLane = Math.max(0.0, positionInLane); }
//////////
//////////    public Route getRoute() { return route; }
//////////    public void setRoute(Route route) { this.route = route; }
//////////
//////////    public Intersection getDestination() { return destination; }
//////////    public void setDestination(Intersection destination) { this.destination = destination; }
//////////
//////////    public DriverProfile getDriverProfile() { return driverProfile; }
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
//////////////package core; // // پکیج core
//////////////
//////////////import infrastructure.Lane; // // نیاز برای فیلد currentLane
//////////////import infrastructure.Intersection; // // مقصد احتمالی
//////////////import simulation.Updatable; // // به‌روزرسانی در هر تیک
//////////////
//////////////public class Vehicle implements Identifiable, Updatable { // // کلاس وسیله نقلیه
//////////////    private String id;                       // // شناسه یکتا
//////////////    private VehicleType type;               // // نوع وسیله (CAR/BUS/...)
//////////////    private VehicleState state;             // // وضعیت (MOVING/STOPPED/...)
//////////////    private double speed;                   // // سرعت فعلی (واحد: پیکسل بر ثانیه یا هر واحد داخلی)
//////////////    private double acceleration;            // // شتاب مثبت (برای افزایش سرعت)
//////////////    private double deceleration;            // // شتاب منفی (برای کاهش سرعت/ترمز)
//////////////    private double maxSpeed;                // // بیشینه سرعت مجاز وسیله
//////////////    private boolean brakeLightOn;           // // وضعیت چراغ ترمز
//////////////    private boolean turningLeft;            // // وضعیت راهنمای چپ (برای فاز بعد)
//////////////    private boolean turningRight;           // // وضعیت راهنمای راست (برای فاز بعد)
//////////////    private Lane currentLane;               // // لِین فعلی
//////////////    private double positionInLane;          // // موقعیت طی‌شده روی لِین (۰..طول لِین)
//////////////    private Route route;                    // // مسیر (لیست لِین‌ها)
//////////////    private Intersection destination;       // // مقصد نهایی (اختیاری)
//////////////    private DriverProfile driverProfile;    // // پروفایل راننده (رفتار احتمالی)
//////////////
//////////////    // --- پارامترهای دینامیک حرکت ---
//////////////    private double targetSpeed;             // // سرعت هدف (براساس قوانین/چراغ/ترافیک)
//////////////    private double dtSeconds = 0.1;         // // زمان هر تیک بر حسب ثانیه (برای محاسبات حرکت؛ با Clock هماهنگ کن)
//////////////
//////////////
//////////////    // --- سازنده کامل ---
//////////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) {
//////////////        this.id = id;                       // // ذخیره شناسه
//////////////        this.type = type;                   // // ذخیره نوع
//////////////        this.maxSpeed = maxSpeed;           // // ذخیره بیشینه سرعت
//////////////        this.driverProfile = profile;       // // ذخیره پروفایل
//////////////        this.speed = 0;                     // // شروع از سرعت صفر
//////////////        this.acceleration = 0.8;            // // شتاب پیش‌فرض (قابل تنظیم)
//////////////        this.deceleration = 1.5;            // // ترمزگیری پیش‌فرض (قابل تنظیم)
//////////////        this.state = VehicleState.STOPPED;  // // وضعیت اولیه: ایستاده
//////////////        this.brakeLightOn = false;          // // چراغ ترمز خاموش
//////////////        this.turningLeft = false;           // // راهنما خاموش
//////////////        this.turningRight = false;          // // راهنما خاموش
//////////////        this.currentLane = null;            // // فعلاً لِین ندارد
//////////////        this.positionInLane = 0;            // // موقعیت صفر
//////////////        this.targetSpeed = Math.min(30, maxSpeed); // // سرعت هدف اولیه (پایین‌تر از بیشینه)
//////////////    }
//////////////
//////////////    // --- سازنده ساده سازگار با کدهای قبلی (اختیاری) ---
//////////////    public Vehicle(String id, Point spawn) {
//////////////        this(id, VehicleType.CAR, 60.0, DriverProfile.LAW_ABIDING); // // پیش‌فرض‌ها
//////////////        // توجه: در طراحی فعلی، position را از Lane می‌گیریم؛ این سازنده فقط برای سازگاری است.
//////////////    }
//////////////
//////////////    // ------------------------ متدهای اصلی حرکت ------------------------
//////////////
//////////////    @Override
//////////////    public void update() {
//////////////        // // این متد در هر «تیک شبیه‌سازی» صدا زده می‌شود.
//////////////        // // هدف: نزدیک کردن speed به targetSpeed با شتاب/ترمز و پیشروی روی لِین.
//////////////
//////////////        if (currentLane == null) { // // اگر لِین نداریم، کاری نکن
//////////////            state = VehicleState.STOPPED; // // وضعیت ایست
//////////////            brakeLightOn = false; // // چراغ ترمز خاموش
//////////////            return; // // خروج
//////////////        }
//////////////
//////////////        // محاسبه اختلاف سرعت فعلی تا سرعت هدف
//////////////        double diff = targetSpeed - speed; // // اگر مثبت باشد باید گاز بدهیم، اگر منفی ترمز کنیم
//////////////
//////////////        // تعیین شتاب مورد نیاز بسته به جهت تغییر
//////////////        double a = (diff > 0) ? acceleration : -deceleration; // // شتاب مناسب
//////////////
//////////////        // تغییر سرعت بر حسب dt
//////////////        double dv = a * dtSeconds; // // مقدار تغییر سرعت در این تیک
//////////////
//////////////        // اگر dv از diff بزرگتر شد، مستقیماً به هدف برسیم تا overshoot نکنیم
//////////////        if (Math.abs(dv) > Math.abs(diff)) {
//////////////            speed = targetSpeed; // // رسیدن به هدف
//////////////        } else {
//////////////            speed += dv; // // به‌روزرسانی سرعت
//////////////        }
//////////////
//////////////        // محدود کردن سرعت در بازه [0, maxSpeed]
//////////////        if (speed < 0) speed = 0; // // سرعت منفی معنی ندارد
//////////////        if (speed > maxSpeed) speed = maxSpeed; // // نباید از بیشینه تجاوز کند
//////////////
//////////////        // روشن/خاموش کردن چراغ ترمز: اگر داریم کم می‌کنیم یا target کمتر از speed است
//////////////        brakeLightOn = (diff < 0); // // ساده: وقتی در جهت کاهش سرعت هستیم
//////////////
//////////////        // پیشروی روی لِین بر اساس سرعت فعلی
//////////////        positionInLane += speed * dtSeconds; // // جابجایی بر حسب dt
//////////////
//////////////        // اگر به انتهای لِین رسیدیم، آمادهٔ تغییر لِین/تقاطع بعدی
//////////////        // توجه: طول لِین را از فاصله بین دو تقاطع حساب می‌کنیم
//////////////        Point A = currentLane.getParentRoad().getStartIntersection().getPosition();
//////////////        Point B = currentLane.getParentRoad().getEndIntersection().getPosition();
//////////////        double dx = B.getX() - A.getX();
//////////////        double dy = B.getY() - A.getY();
//////////////        double laneLength = Math.sqrt(dx*dx + dy*dy);
//////////////
//////////////        if (positionInLane >= laneLength) { // // انتهای لِین
//////////////            positionInLane = positionInLane - laneLength; // // باقی‌مانده را به لِین بعدی منتقل کن (اگر وجود دارد)
//////////////            Lane next = (route != null) ? route.getNextLane(currentLane) : null; // // گرفتن لِین بعدی از Route
//////////////            if (next != null) {
//////////////                currentLane = next; // // تغییر لِین
//////////////                // راهنماها را بسته به جهت تغییر مسیر می‌توانیم فعال/غیرفعال کنیم (فاز بعد)
//////////////                turningLeft = false; // // خاموش
//////////////                turningRight = false; // // خاموش
//////////////                state = VehicleState.TURNING; // // برای یک فریم (اختیاری)
//////////////            } else {
//////////////                // اگر مسیری نبود، متوقف شویم
//////////////                state = VehicleState.STOPPED; // // ایست
//////////////                speed = 0; // // سرعت صفر
//////////////                targetSpeed = 0; // // هدف صفر
//////////////                brakeLightOn = true; // // ترمز روشن چون توقف کردیم
//////////////            }
//////////////        } else {
//////////////            // اگر هنوز داخل لِین هستیم و سرعت > 0، حالت در حرکت
//////////////            state = (speed > 0) ? VehicleState.MOVING : VehicleState.STOPPED;
//////////////        }
//////////////    }
//////////////
//////////////    // ------------------------ متدهای کمکی برای منطق هدف سرعت ------------------------
//////////////
//////////////    public void setTargetSpeed(double target) { // // تنظیم سرعت هدف از بیرون (مثلاً World با توجه به چراغ‌ها)
//////////////        if (target < 0) target = 0; // // محدود کردن پایین
//////////////        if (target > maxSpeed) target = maxSpeed; // // محدود کردن بالا
//////////////        this.targetSpeed = target; // // ذخیره
//////////////    }
//////////////
//////////////    public double getTargetSpeed() { // // گرفتن سرعت هدف
//////////////        return targetSpeed;
//////////////    }
//////////////
//////////////    public void setDtSeconds(double dtSeconds) { // // تنظیم dt (ثانیه) برای هماهنگ‌سازی با SimulationClock
//////////////        if (dtSeconds <= 0) dtSeconds = 0.1; // // جلوگیری از مقدار نامعتبر
//////////////        this.dtSeconds = dtSeconds;
//////////////    }
//////////////
//////////////    // ------------------------ زاویه برای رندر (rotate تصویر) ------------------------
//////////////
//////////////    public double getAngle() { // // زاویه فعلی حرکت (از لِین)
//////////////        if (currentLane == null) return 0.0; // // اگر لِین نداریم، 0
//////////////        return currentLane.getAngleRadians(); // // زاویه هندسی لِین
//////////////    }
//////////////
//////////////    // ------------------------ Getter / Setter های معمول ------------------------
//////////////
//////////////    @Override
//////////////    public String getId() { // // شناسه
//////////////        return id;
//////////////    }
//////////////
//////////////    public VehicleType getType() { // // نوع
//////////////        return type;
//////////////    }
//////////////
//////////////    public VehicleState getState() { // // وضعیت
//////////////        return state;
//////////////    }
//////////////
//////////////    public void setState(VehicleState state) { // // تنظیم وضعیت
//////////////        this.state = state;
//////////////    }
//////////////
//////////////    public double getSpeed() { // // سرعت فعلی
//////////////        return speed;
//////////////    }
//////////////
//////////////    public void setSpeed(double speed) { // // تنظیم سرعت (با احتیاط استفاده شود)
//////////////        this.speed = speed;
//////////////    }
//////////////
//////////////    public double getAcceleration() { // // گرفتن شتاب
//////////////        return acceleration;
//////////////    }
//////////////
//////////////    public void setAcceleration(double acceleration) { // // تنظیم شتاب
//////////////        this.acceleration = acceleration;
//////////////    }
//////////////
//////////////    public double getDeceleration() { // // گرفتن ترمزگیری
//////////////        return deceleration;
//////////////    }
//////////////
//////////////    public void setDeceleration(double deceleration) { // // تنظیم ترمزگیری
//////////////        this.deceleration = deceleration;
//////////////    }
//////////////
//////////////    public double getMaxSpeed() { // // بیشینه سرعت
//////////////        return maxSpeed;
//////////////    }
//////////////
//////////////    public boolean isBrakeLightOn() { // // وضعیت چراغ ترمز
//////////////        return brakeLightOn;
//////////////    }
//////////////
//////////////    public boolean isTurningLeft() { // // وضعیت راهنمای چپ
//////////////        return turningLeft;
//////////////    }
//////////////
//////////////    public void setTurningLeft(boolean turningLeft) { // // تنظیم راهنمای چپ
//////////////        this.turningLeft = turningLeft;
//////////////    }
//////////////
//////////////    public boolean isTurningRight() { // // وضعیت راهنمای راست
//////////////        return turningRight;
//////////////    }
//////////////
//////////////    public void setTurningRight(boolean turningRight) { // // تنظیم راهنمای راست
//////////////        this.turningRight = turningRight;
//////////////    }
//////////////
//////////////    public Lane getCurrentLane() { // // لِین فعلی
//////////////        return currentLane;
//////////////    }
//////////////
//////////////    public void setCurrentLane(Lane currentLane) { // // تنظیم لِین فعلی
//////////////        this.currentLane = currentLane;
//////////////    }
//////////////
//////////////    public double getPositionInLane() { // // موقعیت طی‌شده روی لِین
//////////////        return positionInLane;
//////////////    }
//////////////
//////////////    public void setPositionInLane(double positionInLane) { // // تنظیم موقعیت طی‌شده
//////////////        this.positionInLane = positionInLane;
//////////////    }
//////////////
//////////////    public Route getRoute() { // // مسیر
//////////////        return route;
//////////////    }
//////////////
//////////////    public void setRoute(Route route) { // // تنظیم مسیر
//////////////        this.route = route;
//////////////    }
//////////////
//////////////    public Intersection getDestination() { // // مقصد
//////////////        return destination;
//////////////    }
//////////////
//////////////    public void setDestination(Intersection destination) { // // تنظیم مقصد
//////////////        this.destination = destination;
//////////////    }
//////////////
//////////////    public DriverProfile getDriverProfile() { // // پروفایل راننده
//////////////        return driverProfile;
//////////////    }
//////////////}
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
//////////////package core;
//////////////
//////////////import infrastructure.Lane; // // نیاز برای فیلد currentLane
//////////////
//////////////public class Vehicle implements Identifiable, simulation.Updatable { // // وسیله نقلیه
//////////////    private String id;                       //da bune bilirix da
//////////////    private VehicleType type;               // // نوع وسیله
//////////////    private VehicleState state;             // // وضعیت فعلی
//////////////    private double speed;                   // // سرعت فعلی
//////////////    private double acceleration;            // //گاز
//////////////    private double deceleration;            // //  (ترمز)
//////////////    private double maxSpeed;                // // بیشینه سرعت
//////////////    private boolean brakeLightOn;           // // چراغ ترمز
//////////////    private Lane currentLane;               // // لِین(راه) فعلی
//////////////    private double positionInLane;          // // موقعیت روی لِین
//////////////    private Route route;                    // // مسیر
//////////////    private infrastructure.Intersection destination; // // مقصد
//////////////    private DriverProfile driverProfile;    // // پروفایل راننده ///optional******************&&&&&&&
//////////////
//////////////    // --- سازنده اصلی (کامل)
//////////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) { // // سازنده کامل
//////////////        this.id = id;
//////////////        this.type = type;                   // // مقداردهی نوع
//////////////        this.maxSpeed = maxSpeed;           // // مقداردهی حداکثر سرعت
//////////////        this.driverProfile = profile;       // // مقداردهی پروفایل
//////////////        this.speed = 0;                     // // شروع از سرعت صفر
//////////////        this.acceleration = 0.5;            // // پیش‌فرض شتاب
//////////////        this.deceleration = 1.0;            // // پیش‌فرض ترمز
//////////////        this.state = VehicleState.STOPPED;  // // وضعیت اولیه
//////////////        this.brakeLightOn = false;          // // چراغ ترمز خاموش
//////////////        this.currentLane = null;            // // فعلاً لِین ندارد
//////////////        this.positionInLane = 0;            // // موقعیت اولیه صفر
//////////////    }
//////////////
//////////////    // --- سازنده ساده برای راحتی (id + نقطه شروع فرضی)
//////////////    public Vehicle(String id, Point spawn) { // // سازنده ساده مطابق کدی که در Main استفاده کردی
//////////////        this(id, VehicleType.CAR, 50.0, DriverProfile.LAW_ABIDING); // // دیفالت: ماشین سواری، سرعت 50، راننده قانون‌مدار
//////////////        // توجه: فعلاً Point را نگه نمی‌داریم چون سیستم موقعیت از Lane + positionInLane می‌آید //
//////////////        // اگر خواستی نگه داری، می‌تونیم یه فیلد optional اضافه کنیم. //
//////////////    }
//////////////
//////////////    // --- متدهای لازم برای باقی کدها
//////////////    @Override
//////////////    public void update() {                  // // به‌روزرسانی هر تیک (بعداً منطق حرکت اضافه می‌شود)
//////////////        // فعلاً خالی //
//////////////    }
//////////////
////////////    @Override
////////////    public String getId() {                 // // گرفتن شناسه
////////////        return id;                          // // برگرداندن شناسه
////////////    }
//////////////
//////////////    public VehicleType getType() {          // // گرفتن نوع
//////////////        return type;                        // // برگرداندن نوع
//////////////    }
//////////////
//////////////    public VehicleState getState() {        // // گرفتن وضعیت
//////////////        return state;                       // // برگرداندن وضعیت
//////////////    }
//////////////
//////////////    public void setState(VehicleState state) { // // تنظیم وضعیت
//////////////        this.state = state;                 // // مقداردهی وضعیت
//////////////    }
//////////////
//////////////    public double getSpeed() {              // // گرفتن سرعت
//////////////        return speed;                       // // برگرداندن سرعت
//////////////    }
//////////////
//////////////    public void setSpeed(double speed) {    // // تنظیم سرعت
//////////////        this.speed = speed;                 // // مقداردهی سرعت
//////////////    }
//////////////
//////////////    public double getMaxSpeed() {           // // گرفتن حداکثر سرعت
//////////////        return maxSpeed;                    // // برگرداندن بیشینه
//////////////    }
//////////////
//////////////    public boolean isBrakeLightOn() {       // // وضعیت چراغ ترمز
//////////////        return brakeLightOn;                // // برگرداندن وضعیت
//////////////    }
//////////////
//////////////    public void setBrakeLightOn(boolean on) { // // تنظیم چراغ ترمز
//////////////        this.brakeLightOn = on;             // // مقداردهی چراغ
//////////////    }
//////////////
//////////////    public Lane getCurrentLane() {          // // گرفتن لِین فعلی
//////////////        return currentLane;                 // // برگرداندن لِین
//////////////    }
//////////////
//////////////    public void setCurrentLane(Lane lane) { // // تنظیم لِین فعلی
//////////////        this.currentLane = lane;            // // مقداردهی لِین
//////////////    }
//////////////
//////////////    public double getPositionInLane() {     // // گرفتن موقعیت روی لِین
//////////////        return positionInLane;              // // برگرداندن موقعیت
//////////////    }
//////////////
//////////////    public void setPositionInLane(double p) { // // تنظیم موقعیت روی لِین
//////////////        this.positionInLane = p;            // // مقداردهی موقعیت
//////////////    }
//////////////
//////////////    public Route getRoute() {               // // گرفتن مسیر
//////////////        return route;                       // // برگرداندن مسیر
//////////////    }
//////////////
//////////////    public void setRoute(Route route) {     // // تنظیم مسیر
//////////////        this.route = route;                 // // مقداردهی مسیر
//////////////    }
//////////////
//////////////    public DriverProfile getDriverProfile() { // // گرفتن پروفایل راننده
//////////////        return driverProfile;               // // برگرداندن پروفایل
//////////////    }
//////////////
//////////////
//////////////    public double getAngle() {
//////////////        if (currentLane == null) return 0.0;
//////////////        return currentLane.getAngleRadians(); // فعلاً زاویه از لاین فعلی
//////////////    }
//////////////
//////////////
//////////////}
////////
////////
////////
////////
////////
////////
////////
////////
////////
//////////////package core; // // پکیج
//////////////
//////////////import infrastructure.Lane; // // نیاز برای فیلد lane
//////////////import infrastructure.Intersection; // // نیاز برای فیلد destination
//////////////import core.Route; // // نیاز برای فیلد route
//////////////import simulation.Updatable; // // چون Vehicle آپدیت می‌شود
//////////////
//////////////public class Vehicle implements Identifiable, Locatable, Updatable { // // الان Locatable هم پیاده‌سازی می‌شود
//////////////    private String id; // // شناسه
//////////////    private VehicleType type; // // نوع خودرو
//////////////    private VehicleState state; // // وضعیت خودرو
//////////////    private double speed; // // سرعت فعلی
//////////////    private double acceleration; // // شتاب
//////////////    private double deceleration; // // کاهش سرعت
//////////////    private double maxSpeed; // // حداکثر سرعت
//////////////    private boolean brakeLightOn; // // چراغ ترمز
//////////////    private Lane currentLane; // // لین فعلی
//////////////    private double positionInLane; // // موقعیت طولی روی لین
//////////////    private Route route; // // مسیر
//////////////    private Intersection destination; // // مقصد
//////////////    private DriverProfile driverProfile; // // پروفایل راننده
//////////////
//////////////    // --- اضافه برای UI ---
//////////////    private Point position; // // موقعیت دوبعدی برای رسم در SimulatorPanel
//////////////
//////////////    // سازنده‌ی موجود پروژه‌ی شما
//////////////
//////////////
//////////////    public Vehicle(String id, Point position) {
//////////////        this.id = id;
//////////////        this.type = VehicleType.CAR; // پیش‌فرض: ماشین سواری
//////////////        this.state = VehicleState.STOPPED;
//////////////        this.speed = 0;
//////////////        this.maxSpeed = 50; // سرعت مجاز پیش‌فرض
//////////////        this.brakeLightOn = false;
//////////////        this.currentLane = null; // فعلاً بدون لاین
//////////////        this.positionInLane = 0;
//////////////        // position رو مستقیماً ذخیره نمی‌کنیم چون Vehicle توی طراحی فعلی
//////////////        // موقعیت رو از Lane و positionInLane محاسبه می‌کنه
//////////////    }
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) { // // امضای قبلی حفظ شده
////////////////        this.id = id; // // ست فیلد
////////////////        this.type = type; // // ست فیلد
////////////////        this.maxSpeed = maxSpeed; // // ست فیلد
////////////////        this.driverProfile = profile; // // ست فیلد
////////////////        this.speed = 0; // // سرعت اولیه
////////////////        this.acceleration = 0.5; // // شتاب پیش‌فرض
////////////////        this.deceleration = 1.0; // // ترمز پیش‌فرض
////////////////        this.state = VehicleState.STOPPED; // // وضعیت اولیه
////////////////        this.brakeLightOn = false; // // چراغ ترمز خاموش
////////////////        this.position = null; // // فعلاً موقعیت تعیین نشده (اگر با لین کار می‌کنی)
////////////////    }
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////    // سازنده‌ی کمکی برای تست/UI
//////////////    public Vehicle(String id, Point start) { // // ساخت سریع با نقطه شروع
//////////////        this(id, VehicleType.CAR, 4.0, DriverProfile.LAW_ABIDING); // // استفاده از سازنده‌ی اصلی با مقادیر معقول
//////////////        this.position = start; // // تعیین موقعیت اولیه
//////////////        this.state = VehicleState.MOVING; // // برای اینکه حرکت تستی داشته باشد
//////////////        this.speed = 1.0; // // سرعت ملایم
//////////////    }
//////////////
//////////////    // --- پیاده‌سازی اینترفیس‌ها ---
//////////////    @Override
//////////////    public String getId() { return id; } // // برگرداندن شناسه
//////////////
//////////////    @Override
//////////////    public Point getPosition() { // // موردنیاز UI برای رسم
//////////////        if (position != null) return position; // // اگر ست شده برگردان
//////////////        // اگر از مدل لین استفاده می‌کنی بعداً world-position را از لین محاسبه کن // // یادداشت
//////////////        return new Point(0, 0); // // فعلاً صفر تا نول نشود
//////////////    }
//////////////
//////////////    public void setPosition(Point p) { this.position = p; } // // امکان تغییر دستی موقعیت
//////////////
//////////////    // --- منطق به‌روزرسانی ساده (برای تست) ---
//////////////    @Override
//////////////    public void update() { // // هر تیک
//////////////        if (position != null && state == VehicleState.MOVING) { // // اگر مدل مختصات داریم و در حال حرکتیم
//////////////            int nx = position.getX() + (int)Math.max(1, speed); // // حرکت افقی ساده
//////////////            int ny = position.getY(); // // بدون تغییر عمودی
//////////////            position = new Point(nx, ny); // // ذخیره موقعیت جدید
//////////////        }
//////////////        // TODO: اگر با لین کار می‌کنی، اینجا بر اساس positionInLane و هندسه‌ی Road به‌روز کن // // یادداشت
//////////////    }
//////////////
//////////////    // --- getter/setter های قبلی شما بدون تغییر ---
//////////////    public Lane getCurrentLane() {
//////////////        return currentLane; } // // ...
//////////////
//////////////    public void setCurrentLane(Lane lane) {
//////////////        this.currentLane = lane; } // // ...
//////////////
//////////////    public double getPositionInLane() {
//////////////        return positionInLane; } // // ...
//////////////
//////////////    public void setPositionInLane(double position) {
//////////////        this.positionInLane = position; } // // ...
//////////////
//////////////    public double getSpeed() {
//////////////        return speed; } // // ...
//////////////
//////////////    public void setSpeed(double speed) {
//////////////        this.speed = speed; } // // ...
//////////////
//////////////    public VehicleState getState() {
//////////////        return state; } // // ...
//////////////
//////////////    public void setState(VehicleState state) {
//////////////        this.state = state; } // // ...
//////////////
//////////////    public boolean isBrakeLightOn() {
//////////////        return brakeLightOn; } // // ...
//////////////
//////////////    public void setBrakeLightOn(boolean brakeLightOn) {
//////////////        this.brakeLightOn = brakeLightOn; } // // ...
//////////////
//////////////    public void setRoute(Route route) {
//////////////        this.route = route; } // // ...
//////////////
//////////////    public Route getRoute() {
//////////////        return route; } // // ...
//////////////
//////////////
//////////////    public Intersection getDestination() {
//////////////        return destination; } // // ...
//////////////
//////////////
//////////////    public void setDestination(Intersection destination) { this.destination = destination; } // // ...
//////////////    public VehicleType getType() { return type; } // // ...
//////////////    public DriverProfile getDriverProfile() { return driverProfile; } // // ...
//////////////    public double getMaxSpeed() { return maxSpeed; } // // ...
//////////////}
//////////////
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
//////////////package core;
//////////////
//////////////
//////////////import infrastructure.Lane;
//////////////import infrastructure.Intersection;
//////////////import simulation.Updatable;
//////////////
//////////////public class Vehicle implements Identifiable, Updatable {
//////////////    private String id;
//////////////    private VehicleType type;
//////////////    private VehicleState state;
//////////////    private double speed;
//////////////    private double acceleration;
//////////////    private double deceleration;
//////////////    private double maxSpeed;
//////////////    private boolean brakeLightOn;
//////////////    private Lane currentLane;
//////////////    private double positionInLane;
//////////////    private Route route;
//////////////    private Intersection destination;
//////////////    private DriverProfile driverProfile;
//////////////
//////////////    public Vehicle(String id, VehicleType type, double maxSpeed, DriverProfile profile) {
//////////////        this.id = id;
//////////////        this.type = type;
//////////////        this.maxSpeed = maxSpeed;
//////////////        this.driverProfile = profile;
//////////////        this.speed = 0;
//////////////        this.acceleration = 0.5;
//////////////        this.deceleration = 1.0;
//////////////        this.state = VehicleState.STOPPED;
//////////////        this.brakeLightOn = false;
//////////////    }
//////////////
//////////////    // متد اصلی به‌روزرسانی وضعیت خودرو
//////////////    @Override
//////////////    public void update() {
//////////////        // اینجا بعداً رفتار رانندگی، سرعت، بررسی مسیر و ... پیاده‌سازی میشه
//////////////    }
//////////////
//////////////    @Override
//////////////    public String getId() {
//////////////        return id;
//////////////    }
//////////////
//////////////    // متدهای getter/setter لازم
//////////////
//////////////    public Lane getCurrentLane() {
//////////////        return currentLane;
//////////////    }
//////////////
//////////////    public void setCurrentLane(Lane lane) {
//////////////        this.currentLane = lane;
//////////////    }
//////////////
//////////////    public double getPositionInLane() {
//////////////        return positionInLane;
//////////////    }
//////////////
//////////////    public void setPositionInLane(double position) {
//////////////        this.positionInLane = position;
//////////////    }
//////////////
//////////////    public double getSpeed() {
//////////////        return speed;
//////////////    }
//////////////
//////////////    public void setSpeed(double speed) {
//////////////        this.speed = speed;
//////////////    }
//////////////
//////////////    public VehicleState getState() {
//////////////        return state;
//////////////    }
//////////////
//////////////    public void setState(VehicleState state) {
//////////////        this.state = state;
//////////////    }
//////////////
//////////////    public boolean isBrakeLightOn() {
//////////////        return brakeLightOn;
//////////////    }
//////////////
//////////////    public void setBrakeLightOn(boolean brakeLightOn) {
//////////////        this.brakeLightOn = brakeLightOn;
//////////////    }
//////////////
//////////////    public void setRoute(Route route) {
//////////////        this.route = route;
//////////////    }
//////////////
//////////////    public Route getRoute() {
//////////////        return route;
//////////////    }
//////////////
//////////////    public Intersection getDestination() {
//////////////        return destination;
//////////////    }
//////////////
//////////////    public void setDestination(Intersection destination) {
//////////////        this.destination = destination;
//////////////    }
//////////////
//////////////    public VehicleType getType() {
//////////////        return type;
//////////////    }
//////////////
//////////////    public DriverProfile getDriverProfile() {
//////////////        return driverProfile;
//////////////    }
//////////////
//////////////
//////////////    public double getMaxSpeed() {
//////////////        return maxSpeed;
//////////////    }
//////////////
//////////////}
