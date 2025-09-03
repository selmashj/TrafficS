

package trafficcontrol;

import core.Direction;
import core.Identifiable;

/**
 * اینترفیس پایه برای همه تجهیزات کنترل ترافیک (چراغ راهنما، علائم و ...)
 */
public interface TrafficControlDevice extends Identifiable {

    // جهت کنترل‌شده
    Direction getDirectionControlled();

    // به‌روزرسانی وضعیت (چراغ عوض شود، علامت بررسی شود و ...)
    void update();
}























//package trafficcontrol;
//
//import core.*;
//
//public interface TrafficControlDevice extends Identifiable {
//    Direction getDirectionControlled();
//    boolean canProceed(core.Vehicle v);
//}
//
//
//







//package trafficcontrol;
//
//import core.*;
//
//public interface TrafficControlDevice extends Identifiable {
//    Direction getDirectionControlled();
//    boolean canProceed(core.Vehicle v);
//}
