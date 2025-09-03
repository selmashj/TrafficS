

package trafficcontrol;

import core.Direction;

/**
 * علامت حق تقدم (YIELD)
 */
public class YieldSign extends TrafficSign {

    public YieldSign(String id, Direction direction) {
        super(id, direction);
    }

    @Override
    public void update() {
        // علامت YIELD وضعیت دینامیک ندارد
    }
}


















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





























//// trafficcontrol/YieldSign.java
//package trafficcontrol; // // پکیج کنترل ترافیک
//
//import core.Direction; // // جهت
//
//public class YieldSign implements TrafficControlDevice { // // تابلو رعایت حق تقدم
//    private final String id; // // شناسه
//    public YieldSign(String id){ this.id=id; } // // سازنده
//    @Override public String getId(){ return id; } // // گتر id
//    @Override public boolean canProceed(Direction approach){ return true; } // // اجازه عبور (تقدم در Ruleها)
//    @Override public void update(){ /* no-op */ } // // بدون آپدیت
//    @Override public LightState getState(){ return null; } // // حالت چراغ ندارد
//}
//
//
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
