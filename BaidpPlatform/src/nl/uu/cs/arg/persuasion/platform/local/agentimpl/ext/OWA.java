package nl.uu.cs.arg.persuasion.platform.local.agentimpl.ext;

/*
 * Copyright (c) 2012, Søren Atmakuri Davidsen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.util.Arrays;

/**
 * Implementation of an Ordered Weighted Averaging (OWA) aggregation operator [1].
 *
 * [1] Yager, R. R., "On ordered weighted averaging aggregation operators in multi-criteria decision making,"
 *  IEEE Transactions on Systems, Man and Cybernetics 18, 183-190, 1988.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class OWA
{
    private final double[] weights;

    /**
     * Construct OWA operator with MEOWA weights.
     *
     * @param n Number of weights
     * @param rho The andness
     * @return OWA with weights calculated
     */
    public static OWA MEOWA_FACTORY(int n, double rho)
    {
        if (rho < 0.0 || rho > 1.0) {
            throw new IllegalArgumentException("andness (rho) must be within [0, 1]");
        }

        if (rho == 1.0) {
            double[] w = new double[n];
            Arrays.fill(w, 0.0);
            w[n-1] = 1.0;
            return new OWA(w);
        }

        double t_m = (-(rho - 0.5) - Math.sqrt(((rho - 0.5) * (rho - 0.5)) - (4 * (rho - 1) * rho))) / (2 * (rho - 1));
        double t_p = (-(rho - 0.5) + Math.sqrt(((rho - 0.5) * (rho - 0.5)) - (4 * (rho - 1) * rho))) / (2 * (rho - 1));
        double t = Math.max(t_m, t_p);

        double[] w = new double[n];
        double s = 0.0;
        for (int i = 0; i < n; ++i) {
            w[i] = Math.pow(t, i);
            s += w[i];
        }
        for (int i = 0; i < n; ++i) {
            w[i] /= s;
        }

        return new OWA(w);
    }

    public OWA(double... weights)
    {
        this.weights = weights;
        double sum = 0.0;
        for (double w : weights) {
            sum += w;
        }
        double diff = Math.abs(1.0 - sum);
        if (diff > 0.0001) {
            throw new IllegalArgumentException(String.format("sum(weights) must be 1.0 (was: %f, %.5f)", sum, diff));
        }
    }

    public double apply(double... values)
    {
        int min = Math.min(values.length, weights.length);

        Arrays.sort(values);
        OWA.reverse(values);

        double s = 0.0;
        for (int i = 0; i < min; ++i) {
            s += values[i] * weights[i];
        }

        return s;
    }

    /**
     * Calculate the orness of this OWA operator.
     *
     * To get the andness use it's definition: <code>double andness = 1.0 - getOrness();</code>
     *
     * @return the orness [0, 1]
     */
    public double getOrness()
    {
        double n = weights.length;
        double s = 0.0;
        for (int i = 0; i < weights.length; i++)
            s += (n - (i+1)) * weights[i];
        return s / (n - 1.0);
    }

    /**
     * The dual of orness. See {@link #getOrness()}
     * @return
     */
    public double getAndness()
    {
        return 1.0 - this.getOrness();
    }

    public double[] getWeights()
    {
        return this.weights;
    }

    /**
     * Calculate the relative dispersion (entropy) of the OWA operator.
     *
     * @return the dispersion [0, ln n]
     */
    public double getDispersion()
    {
        double n = weights.length;
        double s = 0.0;
        for (double w : weights) {
            if (w > 0.0) {
                s += w * Math.log(1.0 / w);
            }
        }
        return s / Math.log(n);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weights.length; ++i) {
            sb.append(" " + weights[i]);
        }

        return "OWA { weights=" + sb.substring(1).toString() + "}";
    }

    /**
     * Reverse an array.
     *
     * @param array the array
     */
    public static void reverse(double[] array)
    {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        double tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            --j;
            ++i;
        }
    }
}