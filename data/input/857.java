public class Complex {
    private static final long serialVersionUID = -1679819632;
    public static final double ACCURACY = 1e-10;
    public static final Complex ZERO = new Complex(0., 0.);
    public static final Complex ONE = new Complex(1., 0.);
    public static final Complex I = new Complex(.0, 1.);
    static final double[] ZERO_ = { 0., 0. };
    static final double[] ONE_ = { 1., 0. };
    static final double[] I_ = { .0, 1. };
    private double[] z;
    public Complex(double x, double y) {
        z = new double[] { x, y };
    }
    public Complex(double[] z) {
        this.z = new double[z.length];
        for (int i = 0; i < z.length; i++) {
            this.z[i] = z[i];
        }
    }
    public double getRe() {
        return z[0];
    }
    public double getIm() {
        return z[1];
    }
    public static double abs(double[] z) {
        double x = 0.0;
        double h;
        if (Math.abs(z[0]) == 0 && Math.abs(z[1]) == 0) {
            x = 0.0;
        } else if (Math.abs(z[0]) >= Math.abs(z[1])) {
            h = z[1] / z[0];
            x = Math.abs(z[0]) * Math.sqrt(1 + h * h);
        } else {
            h = z[0] / z[1];
            x = Math.abs(z[1]) * Math.sqrt(1 + h * h);
        }
        return x;
    }
    public static double abs(Complex z) {
        return abs(new double[] { z.z[0], z.z[1] });
    }
    public double abs() {
        return abs(new double[] { z[0], z[1] });
    }
    public static double[] add(double[] x, double[] y) {
        double[] result = new double[2];
        result[0] = x[0] + y[0];
        result[1] = x[1] + y[1];
        return result;
    }
    public Complex add(Complex z) {
        return new Complex(this.z[0] + z.z[0], this.z[1] + z.z[1]);
    }
    public static double arg(double[] z) {
        return Math.atan2(z[1], z[0]);
    }
    public static double arg(Complex z) {
        return Math.atan2(z.z[1], z.z[0]);
    }
    public double arg() {
        return arg(this);
    }
    public static double[] cos(double[] z) {
        double[] result = new double[2];
        result[0] = Math.cos(z[0]) * Math.cosh(z[1]);
        result[1] = Math.sin(z[0]) * Math.sinh(z[1]);
        return result;
    }
    public static Complex cos(Complex z) {
        return new Complex(cos(new double[] { z.z[0], z.z[1] }));
    }
    public static double[] divide(double x, double[] y) {
        double[] w = new double[2];
        double h;
        if (Math.abs(y[0]) == 0 && Math.abs(y[1]) == 0) {
            if (x > 0) w[0] = Double.POSITIVE_INFINITY; else if (x < 0) w[0] = Double.NEGATIVE_INFINITY; else w[0] = 1;
            return w;
        }
        if (Math.abs(y[0]) >= Math.abs(y[1])) {
            h = y[1] / y[0];
            w[0] = x / (y[0] + y[1] * h);
            w[1] = -x * h / (y[0] + y[1] * h);
        } else {
            h = y[0] / y[1];
            w[0] = x * h / (y[0] * h + y[1]);
            w[1] = -x / (y[0] * h + y[1]);
        }
        return w;
    }
    public static Complex divide(double x, Complex y) {
        return new Complex(divide(x, new double[] { y.z[0], y.z[1] }));
    }
    public static double[] divide(double[] x, double[] y) {
        double[] w = new double[2];
        double h;
        if (Math.abs(y[0]) == 0 && Math.abs(y[1]) == 0) {
            if (x[0] > 0) w[0] = Double.POSITIVE_INFINITY; else if (x[0] < 0) w[0] = Double.NEGATIVE_INFINITY; else w[0] = 1;
            if (x[1] > 0) w[1] = Double.POSITIVE_INFINITY; else if (x[1] < 0) w[1] = Double.NEGATIVE_INFINITY; else w[1] = 0;
            return w;
        }
        if (Math.abs(y[0]) >= Math.abs(y[1])) {
            h = y[1] / y[0];
            w[0] = (x[0] + x[1] * h) / (y[0] + y[1] * h);
            w[1] = (x[1] - x[0] * h) / (y[0] + y[1] * h);
        } else {
            h = y[0] / y[1];
            w[0] = (x[0] * h + x[1]) / (y[0] * h + y[1]);
            w[1] = (x[1] * h - x[0]) / (y[0] * h + y[1]);
        }
        return w;
    }
    public Complex divide(Complex z) {
        return new Complex(divide(new double[] { this.z[0], this.z[1] }, new double[] { z.z[0], z.z[1] }));
    }
    public static double[] exp(double[] z) {
        if (z[0] > 709) return multiply(Double.POSITIVE_INFINITY, ONE_);
        double[] w = { Math.exp(z[0]) * Math.cos(z[1]), Math.exp(z[0]) * Math.sin(z[1]) };
        return w;
    }
    public static Complex exp(Complex z) {
        return new Complex(exp(new double[] { z.z[0], z.z[1] }));
    }
    public static double[] gamma(double[] z) {
        if (z[0] < 0) {
            double[] w = divide(ONE_, multiply(z, power(Math.E, multiply(Numbers.GAMMA, z))));
            int nMax = (int) (1e-6 / ACCURACY);
            double[] z_n;
            for (int n = 1; n <= nMax; n++) {
                z_n = multiply(1.0 / n, z);
                w = multiply(w, add(ONE_, z_n));
                w = divide(w, power(Math.E, z_n));
            }
            w = divide(1.0, w);
            return w;
        }
        double[] x = { z[0], z[1] };
        double[] c = { 76.18009173, -86.50532033, 24.01409822, -1.231739516, .00120858003, -5.36382e-6 };
        boolean reflec;
        if (x[0] >= 1.) {
            reflec = false;
            x[0] = x[0] - 1.;
        } else {
            reflec = true;
            x[0] = 1. - x[0];
        }
        double[] xh = { x[0] + .5, x[1] };
        double[] xgh = { x[0] + 5.5, x[1] };
        double[] s = ONE_;
        double[] anum = { x[0], x[1] };
        for (int i = 0; i < c.length; ++i) {
            anum = add(anum, ONE_);
            s = add(s, divide(c[i], anum));
        }
        s = multiply(2.506628275, s);
        double[] g = multiply(power(xgh, xh), s);
        g = divide(g, power(Math.E, xgh));
        if (reflec) {
            if (Math.abs(x[1]) > 709) {
                return ZERO_;
            }
            double[] result = multiply(Math.PI, x);
            result = divide(result, multiply(g, sin(multiply(Math.PI, x))));
            return result;
        } else {
            return g;
        }
    }
    public static Complex gamma(Complex z) {
        return new Complex(gamma(new double[] { z.z[0], z.z[1] }));
    }
    public static double[] lnCos(double[] z) {
        double[] result = new double[2];
        if (Math.abs(z[1]) <= 709) {
            result[0] = Math.cos(z[0]) * Math.cosh(z[1]);
            result[1] = Math.sin(z[0]) * Math.sinh(z[1]);
            result = ln(result);
        } else {
            result[0] = Math.abs(z[1]) - Math.log(2);
            if (z[1] < 0) {
                result[1] = Math.atan(-1 / Math.tan(z[0]));
            } else {
                result[1] = Math.atan(1 / Math.tan(z[0]));
            }
        }
        return result;
    }
    public static Complex ln(Complex z) {
        return new Complex(ln(new double[] { z.z[0], z.z[1] }));
    }
    public static double[] ln(double[] z) {
        double[] result = { Math.log(abs(z)), arg(z) };
        return result;
    }
    public static double[] lnGamma(double[] z) {
        if (z[0] < 0) {
            double[] w = add(ln(z), multiply(Numbers.GAMMA, z));
            w = multiply(-1.0, w);
            int nMax = (int) (Math.abs(z[0]) / ACCURACY);
            if (nMax > 10000) nMax = 10000;
            double[] z_n;
            for (int n = 1; n <= nMax; n++) {
                z_n = multiply(1.0 / n, z);
                w = add(w, z_n);
                w = subtract(w, ln(add(ONE_, z_n)));
            }
            return w;
        }
        double[] x = { z[0], z[1] };
        double[] c = { 76.18009173, -86.50532033, 24.01409822, -1.231739516, .00120858003, -5.36382e-6 };
        boolean reflec;
        if (x[0] >= 1.) {
            reflec = false;
            x[0] = x[0] - 1.;
        } else {
            reflec = true;
            x[0] = 1. - x[0];
        }
        double[] xh = { x[0] + .5, x[1] };
        double[] xgh = { x[0] + 5.5, x[1] };
        double[] s = ONE_;
        double[] anum = { x[0], x[1] };
        for (int i = 0; i < c.length; ++i) {
            anum = add(anum, ONE_);
            s = add(s, divide(c[i], anum));
        }
        s = multiply(2.506628275, s);
        double[] g = multiply(xh, ln(xgh));
        g = add(g, ln(s));
        g = subtract(g, xgh);
        if (reflec) {
            double[] result = ln(multiply(Math.PI, x));
            result = subtract(result, g);
            result = subtract(result, lnSin(multiply(Math.PI, x)));
            return result;
        } else {
            return g;
        }
    }
    public static Complex lnGamma(Complex z) {
        return new Complex(lnGamma(new double[] { z.z[0], z.z[1] }));
    }
    public static Complex lnSin(Complex z) {
        return new Complex(lnSin(new double[] { z.z[0], z.z[1] }));
    }
    public static double[] lnSin(double[] z) {
        double[] result = new double[2];
        if (Math.abs(z[1]) <= 709) {
            result[0] = Math.sin(z[0]) * Math.cosh(z[1]);
            result[1] = Math.cos(z[0]) * Math.sinh(z[1]);
            result = ln(result);
        } else {
            result[0] = Math.abs(z[1]) - Math.log(2);
            if (z[1] < 0) {
                result[1] = Math.atan(-1 / Math.tan(z[0]));
            } else {
                result[1] = Math.atan(1 / Math.tan(z[0]));
            }
        }
        return result;
    }
    public Complex minus(Complex z) {
        return new Complex(this.z[0] - z.z[0], this.z[1] - z.z[1]);
    }
    public static double[] multiply(double x, double[] z) {
        double[] w = new double[2];
        w[0] = x * z[0];
        w[1] = x * z[1];
        return w;
    }
    public Complex multiply(double x) {
        return new Complex(x * z[0], x * z[1]);
    }
    public static Complex multiply(Complex x, Complex y) {
        return new Complex(multiply(new double[] { x.z[0], x.z[1] }, new double[] { y.z[0], y.z[1] }));
    }
    public static double[] multiply(double[] x, double[] y) {
        double[] w = new double[2];
        w[0] = x[0] * y[0] - x[1] * y[1];
        w[1] = x[1] * y[0] + x[0] * y[1];
        return w;
    }
    public Complex multiply(Complex z) {
        return new Complex(multiply(new double[] { this.z[0], this.z[1] }, new double[] { z.z[0], z.z[1] }));
    }
    public Complex plus(Complex z) {
        return new Complex(this.z[0] + z.z[0], this.z[1] + z.z[1]);
    }
    public static Complex power(double x, Complex s) {
        return new Complex(power(x, new double[] { s.z[0], s.z[1] }));
    }
    public static double[] power(double x, double[] s) {
        double absX = Math.abs(x);
        double[] w = new double[2];
        if (abs(s) < ACCURACY) {
            w = ONE_;
            return w;
        } else if (absX < ACCURACY) {
            return w;
        }
        w[0] = Math.pow(x, s[0]);
        w[1] = w[0];
        if (x > 0) {
            w[0] *= Math.cos(s[1] * Math.log(absX));
            w[1] *= Math.sin(s[1] * Math.log(absX));
        } else {
            w[0] *= Math.cos(s[1] * Math.log(absX) + s[0] * Math.PI);
            w[1] *= Math.sin(s[1] * Math.log(absX) + s[0] * Math.PI);
        }
        return w;
    }
    public Complex pow(Complex s) {
        double r = abs();
        if (s.abs() < ACCURACY) {
            return ONE;
        } else if (r < ACCURACY) {
            return ZERO;
        }
        double phi = arg();
        double phase = s.z[0] * phi + s.z[1] * Math.log(r);
        double[] w = new double[2];
        w[0] = Math.pow(r, s.z[0]) * Math.exp(-s.z[1] * phi);
        w[1] = w[0];
        w[0] *= Math.cos(phase);
        w[1] *= Math.sin(phase);
        return new Complex(w);
    }
    public static double[] power(double[] z, double[] s) {
        double r = abs(z);
        if (abs(s) < ACCURACY) {
            return ONE_;
        } else if (r < ACCURACY) {
            return ZERO_;
        }
        double phi = arg(z);
        double phase = s[0] * phi + s[1] * Math.log(r);
        double[] w = new double[2];
        w[0] = Math.pow(r, s[0]) * Math.exp(-s[1] * phi);
        w[1] = w[0];
        w[0] *= Math.cos(phase);
        w[1] *= Math.sin(phase);
        return w;
    }
    public static double[] reciprocal(double[] y) {
        double[] w = new double[2];
        double h;
        if (Math.abs(y[0]) == 0 && Math.abs(y[1]) == 0) {
            w[0] = Double.POSITIVE_INFINITY;
            return w;
        }
        if (Math.abs(y[0]) >= Math.abs(y[1])) {
            h = y[1] / y[0];
            w[0] = 1 / (y[0] + y[1] * h);
            w[1] = -h / (y[0] + y[1] * h);
        } else {
            h = y[0] / y[1];
            w[0] = h / (y[0] * h + y[1]);
            w[1] = -1 / (y[0] * h + y[1]);
        }
        return w;
    }
    public Complex reciprocal() {
        double[] y = z;
        double[] w = new double[2];
        double h;
        if (Math.abs(y[0]) == 0 && Math.abs(y[1]) == 0) {
            w[0] = Double.POSITIVE_INFINITY;
            return new Complex(w);
        }
        if (Math.abs(y[0]) >= Math.abs(y[1])) {
            h = y[1] / y[0];
            w[0] = 1 / (y[0] + y[1] * h);
            w[1] = -h / (y[0] + y[1] * h);
        } else {
            h = y[0] / y[1];
            w[0] = h / (y[0] * h + y[1]);
            w[1] = -1 / (y[0] * h + y[1]);
        }
        return new Complex(w);
    }
    public static double[] sin(double[] z) {
        return new double[] { Math.sin(z[0]) * Math.cosh(z[1]), Math.cos(z[0]) * Math.sinh(z[1]) };
    }
    public static Complex sin(Complex z) {
        return new Complex(Math.sin(z.z[0]) * Math.cosh(z.z[1]), Math.cos(z.z[0]) * Math.sinh(z.z[1]));
    }
    public Complex sin() {
        return new Complex(Math.sin(z[0]) * Math.cosh(z[1]), Math.cos(z[0]) * Math.sinh(z[1]));
    }
    public static double[] sqrt(double[] z) {
        double[] y = { 0., 0. };
        double w = 0.;
        double h;
        if (Math.abs(z[0]) != 0 || Math.abs(z[1]) != 0) {
            if (Math.abs(z[0]) >= Math.abs(y[1])) {
                h = z[1] / z[0];
                w = Math.sqrt((1 + Math.sqrt(1 + h * h)) / 2);
                w = Math.sqrt(z[0]) * w;
            } else {
                h = Math.abs(z[0] / z[1]);
                w = Math.sqrt((h + Math.sqrt(1 + h * h)) / 2);
                w = Math.sqrt(z[1]) * w;
            }
            if (z[0] >= 0.) {
                y[0] = w;
                y[1] = z[1] / (2 * w);
            } else if (z[0] < 0 && z[1] >= 0) {
                y[0] = Math.abs(z[1]) / (2 * w);
                y[1] = w;
            } else if (z[0] < 0 && z[1] < 0) {
                y[0] = Math.abs(z[1]) / (2 * w);
                y[1] = -w;
            }
        }
        return y;
    }
    public static Complex sqrt(Complex z) {
        return new Complex(sqrt(new double[] { z.z[0], z.z[1] }));
    }
    public static double[] subtract(double[] x, double[] y) {
        double[] result = new double[2];
        result[0] = x[0] - y[0];
        result[1] = x[1] - y[1];
        return result;
    }
    public Complex subtract(Complex z) {
        return new Complex(this.z[0] - z.z[0], this.z[1] - z.z[1]);
    }
    @Override
    public String toString() {
        return toString(new double[] { z[0], z[1] });
    }
    public static String toString(Complex z) {
        return toString(new double[] { z.z[0], z.z[1] }, new java.text.DecimalFormat("#,###.########"));
    }
    public static String toString(double[] z) {
        java.text.DecimalFormat digit = new java.text.DecimalFormat("#,###.########");
        return toString(z, digit);
    }
    public static String toString(Complex z, java.text.DecimalFormat digit) {
        return toString(new double[] { z.z[0], z.z[1] }, digit);
    }
    public static String toString(double[] z, java.text.DecimalFormat digit) {
        java.text.DecimalFormat scientific = new java.text.DecimalFormat("0.########E0");
        double upLimit = 1e9, lowLimit = 1e-9;
        boolean formatCondition = true;
        String output = "";
        if (Double.toString(z[0]).equals("NaN") || Double.toString(z[1]).equals("NaN")) {
            output += "NaN";
        } else if (Math.abs(z[0]) < ACCURACY && Math.abs(z[1]) < ACCURACY) {
            output += "0";
        } else if (Math.abs(z[0]) >= ACCURACY && Math.abs(z[1]) < ACCURACY) {
            formatCondition = (Math.abs(z[0]) < upLimit && Math.abs(z[0]) > lowLimit);
            output += formatCondition ? digit.format(z[0]) : scientific.format(z[0]);
        } else if (Math.abs(z[0]) >= ACCURACY && z[1] > 0) {
            formatCondition = (Math.abs(z[0]) < upLimit && Math.abs(z[0]) > lowLimit);
            output += formatCondition ? digit.format(z[0]) : scientific.format(z[0]);
            output += " + ";
            if (Math.abs(z[1] - 1) >= ACCURACY) {
                formatCondition = (Math.abs(z[1]) < upLimit && Math.abs(z[1]) > lowLimit);
                output += formatCondition ? digit.format(Math.abs(z[1])) : scientific.format(z[1]);
                output += " ";
            }
            output += "i";
        } else if (Math.abs(z[0]) >= ACCURACY && z[1] < 0) {
            formatCondition = (Math.abs(z[0]) < upLimit && Math.abs(z[0]) > lowLimit);
            output += formatCondition ? digit.format(z[0]) : scientific.format(z[0]);
            output += " - ";
            if (Math.abs(z[1] + 1) >= ACCURACY) {
                formatCondition = (Math.abs(z[1]) < upLimit && Math.abs(z[1]) > lowLimit);
                output += formatCondition ? digit.format(Math.abs(z[1])) : scientific.format(Math.abs(z[1]));
                output += " ";
            }
            output += "i";
        } else {
            if (Math.abs(z[1] + 1) < ACCURACY) {
                output += "- ";
            } else if (Math.abs(z[1] - 1) >= ACCURACY) {
                formatCondition = (Math.abs(z[1]) < upLimit && Math.abs(z[1]) > lowLimit);
                output += formatCondition ? digit.format(z[1]) : scientific.format(z[1]);
                output += " ";
            }
            output += "i";
        }
        return output;
    }
}
