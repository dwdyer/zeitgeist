// ============================================================================
//   Copyright 2009-2012 Daniel W. Dyer
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

import java.util.List;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link Matrix} type.
 * @author Daniel Dyer
 */
public class MatrixTest
{
    @Test
    public void testMultiply()
    {
        Matrix a = new Matrix(new double[][]{{2, 0, -1, 1}});
        Matrix b = new Matrix(new double[][]{{1, 5, -7}, {1, 1, 0}, {0, -1, 1}, {2, 0, 0}});

        Matrix product = a.multiply(b);
        assert product.getRowCount() == 1 : "Product should have 1 row, has " + product.getRowCount();
        assert product.getColumnCount() == 3 : "Product should have 3 columns, has " + product.getColumnCount();

        assert product.get(0, 0) == 4 : "Wrong value at (0, 0): " + product.get(0, 0);
        assert product.get(0, 1) == 11 : "Wrong value at (0, 1): " + product.get(0, 1);
        assert product.get(0, 2) == -15 : "Wrong value at (0, 2): " + product.get(0, 2);
    }


    @Test
    public void testMultiplyTransposeRight()
    {
        Matrix a = new Matrix(new double[][]{{2, 0, -1, 1}});
        Matrix b = new Matrix(new double[][]{{1, 1, 0, 2}, {5, 1, -1, 0}, {-7, 0, 1, 0}});

        Matrix product = a.multiplyTransposeRight(b);
        assert product.getRowCount() == 1 : "Product should have 1 row, has " + product.getRowCount();
        assert product.getColumnCount() == 3 : "Product should have 3 columns, has " + product.getColumnCount();

        assert product.get(0, 0) == 4 : "Wrong value at (0, 0): " + product.get(0, 0);
        assert product.get(0, 1) == 11 : "Wrong value at (0, 1): " + product.get(0, 1);
        assert product.get(0, 2) == -15 : "Wrong value at (0, 2): " + product.get(0, 2);
    }


    @Test
    public void testMultiplyTransposeLeft()
    {
        Matrix a = new Matrix(new double[][]{{2}, {0}, {-1}, {1}});
        Matrix b = new Matrix(new double[][]{{1, 5, -7}, {1, 1, 0}, {0, -1, 1}, {2, 0, 0}});

        Matrix product = a.multiplyTransposeLeft(b);
        assert product.getRowCount() == 1 : "Product should have 1 row, has " + product.getRowCount();
        assert product.getColumnCount() == 3 : "Product should have 3 columns, has " + product.getColumnCount();

        assert product.get(0, 0) == 4 : "Wrong value at (0, 0): " + product.get(0, 0);
        assert product.get(0, 1) == 11 : "Wrong value at (0, 1): " + product.get(0, 1);
        assert product.get(0, 2) == -15 : "Wrong value at (0, 2): " + product.get(0, 2);
    }


    @Test
    public void testElementMultiplyAndDivide()
    {
        Matrix a = new Matrix(new double[][]{{1, 2, 3}, {4, 5, 6}});
        Matrix b = new Matrix(new double[][]{{6, 6, 6}, {8, 8, 8}});
        Matrix c = new Matrix(new double[][]{{3, 3, 3}, {2, 2, 2}});

        a.elementMultiplyAndDivide(b, c);
        assert a.get(0, 0) == 2 : "Wrong value at (0, 0):" + a.get(0, 0);
        assert a.get(0, 1) == 4 : "Wrong value at (0, 1):" + a.get(0, 1);
        assert a.get(0, 2) == 6 : "Wrong value at (0, 2):" + a.get(0, 2);
        assert a.get(1, 0) == 16 : "Wrong value at (1, 0):" + a.get(1, 0);
        assert a.get(1, 1) == 20 : "Wrong value at (1, 1):" + a.get(1, 1);
        assert a.get(1, 2) == 24 : "Wrong value at (1, 2):" + a.get(1, 2);
    }


    @Test
    public void testElementMultiplyZeroDivisor()
    {
        Matrix a = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix b = new Matrix(new double[][]{{1, 1}, {1, 1}});
        Matrix c = new Matrix(new double[][]{{1, 1}, {1, 0}});

        a.elementMultiplyAndDivide(b, c);
        // Should be no divide by zero exception and value should be sensible.
        double value = a.get(1, 1);
        assert value > 4 && value <= Double.POSITIVE_INFINITY : "Wrong value at (1, 1): " + value;
    }


    @Test
    public void testFactorise()
    {
        Matrix a = new Matrix(new double[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}});
        List<Matrix> factors = a.factorise(2);
        Matrix weights = factors.get(0);
        Matrix features = factors.get(1);
        assert features.getRowCount() == 2 : "Wrong number of features: " + features.getRowCount();
        assert weights.getColumnCount() == 2 : "Wrong number of weights: " + weights.getColumnCount();
    }
}
