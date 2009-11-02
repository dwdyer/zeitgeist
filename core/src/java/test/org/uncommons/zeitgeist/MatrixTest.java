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
}
