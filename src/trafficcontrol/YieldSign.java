// trafficcontrol/YieldSign.java
package trafficcontrol; // // پکیج

import core.Direction;   // // جهت

public class YieldSign extends TrafficSign {                // // علامت حق تقدم
    public YieldSign(String id, Direction direction) {      // // سازنده
        super(id, direction);                               // // فراخوانی پایه
    }
    // update() از پایه به‌صورت no-op موجود است //
}


//package trafficcontrol; // // پکیج کنترل ترافیک
//
//import core.Direction; // // جهت
//
//public class YieldSign extends TrafficSign { // // تابلوی رعایت حق تقدم
//    private String label;      // // برچسب اختیاری
//    private Direction dir;     // // جهت تابلو
//
//    public YieldSign() { // // سازندهٔ پیش‌فرض
//        // // هیچ
//    }
//
//    public YieldSign(String label, Direction dir) { // // سازندهٔ کمکی
//        this.label = label; // // ذخیره برچسب
//        this.dir   = dir;   // // ذخیره جهت
//    }
//
//    public String getLabel() { return label; } // // گتر برچسب
//    public Direction getDirection() { return dir; } // // گتر جهت
//
//    @Override
//    protected void onUpdate() { // // قلاب — این تابلو هم رفتار زمانی ندارد
//        // // ثابت است؛ کاری لازم نیست
//    }
//}
//














//package trafficcontrol;
//
//import core.*;
//
//public class YieldSign extends TrafficSign implements TrafficControlDevice {
//
//    public YieldSign(String id, Direction directionControlled) {
//        super(id, directionControlled);
//    }
//
//    @Override
//    public boolean canProceed(Vehicle v) {
//        // منطق ساده: اگر سرعت کم باشد یا هیچ وسیله دیگری جلوی او نباشد، اجازه عبور دارد
//        return v.getSpeed() < 10;
//    }
//}
//
//
//
