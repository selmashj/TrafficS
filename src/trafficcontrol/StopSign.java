

package trafficcontrol;

import core.Direction;

/**
 * علامت توقف (STOP)
 */
public class StopSign extends TrafficSign {

    public StopSign(String id, Direction direction) {
        super(id, direction);
    }

    @Override
    public void update() {
        // علامت توقف وضعیت دینامیک ندارد
    }
}





















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
