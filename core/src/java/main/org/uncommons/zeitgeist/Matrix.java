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

/**
 * @author Daniel Dyer
 */
public final class Matrix
{
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
    public Matrix multiplyTransposeRight(Matrix m)
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
    public Matrix multiplyTransposeLeft(Matrix m)
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


    public void elementMultiplyAndDivide(Matrix multiplier, Matrix divisor)
    {
        for (int i = 0; i < data.length; i++)
        {
            // Add very small value to the divisor to avoid division by zero.
            this.data[i] = this.data[i] * multiplier.data[i] / (divisor.data[i] + Double.MIN_VALUE);
        }
    }
}
