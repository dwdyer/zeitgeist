// ============================================================================
//   Copyright 2009 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package org.uncommons.zeitgeist;

import java.util.Random;
import java.util.List;
import java.util.Arrays;

/**
 * Simple matrix implementation that supports specific operations required by the
 * non-negative matrix factorisation algorithm.
 * @author Daniel Dyer
 */
public final class Matrix
{
    private static final Random RNG = new Random();
    
    private final int rowCount;
    private final int columnCount;
    private final double[] data;

    public Matrix(int rowCount, int columnCount)
    {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.data = new double[rowCount * columnCount];
    }


    public Matrix(int rowCount, int columnCount, Random rng)
    {
        this(rowCount, columnCount);
        for (int i = 0; i < data.length; i++)
        {
            data[i] = rng.nextDouble();
        }
    }


    public Matrix(double[][] values)
    {
        this(values.length, values[0].length);
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[row].length; column++)
            {
                set(row, column, values[row][column]);
            }
        }        
    }


    public final double get(int row, int column)
    {
        return data[row * columnCount + column];
    }


    public final void set(int row, int column, double value)
    {
        data[row * columnCount + column] = value;
    }


    public final int getRowCount()
    {
        return rowCount;
    }


    public final int getColumnCount()
    {
        return columnCount;
    }


    /**
     * Perform non-negative factorisation on this matrix.
     * The result is a pair of matrices (weights and features) that,
     * when multiplied, approximate this matrix.
     * @param featureCount An estimate of how many distinct features there are to be
     * discovered.  This is used as the number of rows in the feature matrix and the
     * number of columns in the weight matrix.
     * @return A 2-element list containing a matrix of weights (first element) and
     * a matrix of features (second element).
     */
    public List<Matrix> factorise(int featureCount)
    {
        Matrix weights = new Matrix(getRowCount(), featureCount, RNG);
        Matrix features = new Matrix(featureCount, getColumnCount(), RNG);

        double oldCost = Double.MAX_VALUE;
        Matrix product = weights.multiply(features);
        double cost = diffCost(product);
        while (cost / oldCost < 0.99) // Once improvement is less than 1%, stop iterating.
        {
            Matrix hn = weights.multiplyTransposeLeft(this);
            Matrix hd = weights.multiplyTransposeLeft(weights).multiply(features);
            features.elementMultiplyAndDivide(hn, hd);

            Matrix wn = multiplyTransposeRight(features);
            Matrix wd = weights.multiply(features).multiplyTransposeRight(features);
            weights.elementMultiplyAndDivide(wn, wd);

            product = weights.multiply(features);
            oldCost = cost;
            cost = diffCost(product);
        }
        System.out.println(cost);

        return Arrays.asList(weights, features);
    }


    private double diffCost(Matrix other)
    {
        double diff = 0;
        for (int i = 0; i < data.length; i++)
        {
            double delta = data[i] - other.data[i];
            diff += delta * delta;
        }
        return diff;
    }


    /**
     * Multiply this matrix (row-by-column) by the specified matrix.
     * @return A new matrix that is the result of the multiplication.
     */
    public Matrix multiply(Matrix m)
    {
        Matrix result = new Matrix(rowCount, m.getColumnCount());
        for (int row = 0; row < result.getRowCount(); row++)
        {
            for (int column = 0; column < result.getColumnCount(); column++)
            {
                double value = 0;
                for (int i = 0; i < columnCount; i++)
                {
                    value += get(row, i) * m.get(i, column);
                }
                result.set(row, column, value);
            }
        }
        return result;
    }


    /**
     * Multiply this matrix (row-by-column) by the transpose of the specified matrix.
     * @return A new matrix that is the result of the multiplication.
     */
    Matrix multiplyTransposeRight(Matrix m)
    {
        Matrix result = new Matrix(rowCount, m.getRowCount());
        for (int row = 0; row < result.getRowCount(); row++)
        {
            for (int column = 0; column < result.getColumnCount(); column++)
            {
                double value = 0;
                for (int i = 0; i < columnCount; i++)
                {
                    value += get(row, i) * m.get(column, i);
                }
                result.set(row, column, value);
            }
        }
        return result;
    }


    /**
     * Transpose this matrix and multiply (row-by-column) by the specified matrix.
     * @return A new matrix that is the result of the multiplication.
     */
    Matrix multiplyTransposeLeft(Matrix m)
    {
        Matrix result = new Matrix(columnCount, m.getColumnCount());
        for (int row = 0; row < result.getRowCount(); row++)
        {
            for (int column = 0; column < result.getColumnCount(); column++)
            {
                double value = 0;
                for (int i = 0; i < rowCount; i++)
                {
                    value += get(i, row) * m.get(i, column);
                }
                result.set(row, column, value);
            }
        }
        return result;
    }


    /**
     * Multiply each value in this matrix by the corresponding value in the first specified
     * matrix, then divide by the corresponding value in the second specified matrix.  This
     * is done in-place.
     */
    void elementMultiplyAndDivide(Matrix multiplier, Matrix divisor)
    {
        for (int i = 0; i < data.length; i++)
        {
            // Avoid division by zero.
            double divisorValue = divisor.data[i] > 0 ? divisor.data[i] : Double.MIN_VALUE;
            this.data[i] = this.data[i] * multiplier.data[i] / divisorValue;
        }
    }
}
