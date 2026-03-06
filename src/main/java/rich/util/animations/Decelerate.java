
package rich.util.animations;

import rich.util.animations.Animation;

public class Decelerate
extends Animation {
    @Override
    public double calculation(double value) {
        double x = value / (double)this.ms;
        return 1.0 - (x - 1.0) * (x - 1.0);
    }
}

