
// trafficcontrol/StopSign.java
package trafficcontrol; // // پکیج

import core.Direction;   // // جهت

public class StopSign extends TrafficSign {                 // // علامت توقف
    public StopSign(String id, Direction direction) {       // // سازنده
        super(id, direction);                               // // فراخوانی پایه
    }
    // update() از پایه به‌صورت no-op موجود است //
}

























//package trafficcontrol;
//
//import core.Direction;
//import core.Vehicle;
//
//public class StopSign extends TrafficSign { // // تابلوی توقف
//
//    public StopSign(String id, Direction directionControlled) {
//        super(id, directionControlled);
//    }
//
//    @Override
//    public boolean canProceed(Vehicle v) {
//        // قانون ساده: فقط وقتی سرعت خودرو صفر است اجازه عبور می‌دهد
//        return v.getSpeed() == 0.0;
//    }
//}


//package trafficcontrol; // // پکیج کنترل ترافیک
//
//import core.Direction; // // جهت
//
//public class StopSign extends TrafficSign { // // تابلوی ایست که از TrafficSign ارث می‌برد
//    private String label;      // // برچسب اختیاری
//    private Direction dir;     // // جهت تابلوی ایست (در صورت نیاز گرافیک/منطق)
//
//    public StopSign() { // // سازندهٔ بدون آرگومان مطابق پایه
//        // // هیچ
//    }
//
//    public StopSign(String label, Direction dir) { // // سازندهٔ کمکی (بدون فراخوانی super با آرگومان)
//        this.label = label; // // ذخیره برچسب
//        this.dir   = dir;   // // ذخیره جهت
//    }
//
//    public String getLabel() { return label; } // // گتر برچسب
//    public Direction getDirection() { return dir; } // // گتر جهت
//
//    @Override
//    protected void onUpdate() { // // پیاده‌سازی قلاب — تابلوی ایست رفتار زمانی ندارد
//        // // ثابت است؛ کاری لازم نیست
//    }
//}
//
//
//
//
//
//
//













//package trafficcontrol;
//
//import core.*;
//
//public class StopSign extends TrafficSign implements TrafficControlDevice {
//
//    public StopSign(String id, Direction directionControlled) {
//        super(id, directionControlled);
//    }
//
//    @Override
//    public boolean canProceed(Vehicle v) {
//        // منطق ساده: فرض کنیم همیشه توقف لازم است، و پس از توقف، راننده می‌تواند عبور کند
//        return v.getSpeed() == 0;
//    }
//}























//package trafficcontrol;
//
//import core.*;
//
//public class StopSign extends TrafficSign implements TrafficControlDevice {
//
//    public StopSign(String id, Direction directionControlled) {
//        super(id, directionControlled);
//    }
//
//    @Override
//    public boolean canProceed(Vehicle v) {
//        // منطق ساده: فرض کنیم همیشه توقف لازم است، و پس از توقف، راننده می‌تواند عبور کند
//        return v.getSpeed() == 0;
//    }
//}
