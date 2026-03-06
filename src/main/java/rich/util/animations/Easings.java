
package rich.util.animations;

import lombok.Generated;
import rich.util.animations.Easing;

public final class Easings {
    public static final Easing LINEAR = value -> value;
    public static final Easing QUAD_OUT = value -> 1.0 - Math.pow(1.0 - value, 2.0);
    public static final Easing CUBIC_OUT = value -> 1.0 - Math.pow(1.0 - value, 3.0);
    public static final Easing EXPO_IN = value -> value == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * value - 10.0);
    public static final Easing EXPO_OUT = value -> value == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * value);
    public static final Easing EXPO_IN_OUT = value -> {
        if (value == 0.0 || value == 1.0) {
            return value;
        }
        return value < 0.5 ? Math.pow(2.0, 20.0 * value - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * value + 10.0)) / 2.0;
    };
    public static final Easing SINE_OUT = value -> Math.sin(value * Math.PI / 2.0);
    public static final Easing BACK_OUT = value -> {
        double c1 = 1.70158;
        double c3 = c1 + 1.0;
        return 1.0 + c3 * Math.pow(value - 1.0, 3.0) + c1 * Math.pow(value - 1.0, 2.0);
    };

    @Generated
    private Easings() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

