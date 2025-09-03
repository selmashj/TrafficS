
package simulation;

import core.Vehicle;
import infrastructure.Lane;

public class OvertakingRules {

    public boolean canOvertake(Vehicle v) {
        Lane currentLane = v.getCurrentLane();

        // اگر لاین کناری سمت چپ وجود نداشته باشد، سبقت مجاز نیست
        Lane leftLane = currentLane.getLeftAdjacentLane();
        if (leftLane == null) return false;

        // اگر سرعت خودرو خیلی کمتر از حداکثر باشد، اجازه سبقت دارد
        return v.getSpeed() < v.getMaxSpeed() * 0.8;
    }
}
