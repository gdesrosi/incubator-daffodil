/* Copyright (c) 2012-2014 Tresys Technology, LLC. All rights reserved.
 *
 * Developed by: Tresys Technology, LLC
 *               http://www.tresys.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal with
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 *
 *  3. Neither the names of Tresys Technology, nor the names of its contributors
 *     may be used to endorse or promote products derived from this Software
 *     without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE
 * SOFTWARE.
 */

package edu.illinois.ncsa.daffodil.section17.calc_value_properties

import org.junit.Test
import edu.illinois.ncsa.daffodil.tdml.Runner
import org.junit.AfterClass

object TestOutputValueCalc {
  val testDir = "/edu/illinois/ncsa/daffodil/section17/calc_value_properties/"

  val runner = Runner(testDir, "outputValueCalc.tdml")

  @AfterClass def shutDown {
    runner.reset
  }

}

class TestOutputValueCalc {

  import TestOutputValueCalc._

  @Test def test_OutputValueCalc_01() { runner.runOneTest("OutputValueCalc_01") }
  @Test def test_OutputValueCalc_02() { runner.runOneTest("OutputValueCalc_02") }
  @Test def test_OutputValueCalc_03() { runner.runOneTest("OutputValueCalc_03") }
  @Test def test_OutputValueCalc_04() { runner.runOneTest("OutputValueCalc_04") }
  @Test def test_OutputValueCalc_05() { runner.runOneTest("OutputValueCalc_05") }
  //@Test def test_OutputValueCalc_06() { runner.runOneTest("OutputValueCalc_06") }
  @Test def test_OutputValueCalc_07() { runner.runOneTest("OutputValueCalc_07") }
  @Test def test_OutputValueCalc_08() { runner.runOneTest("OutputValueCalc_08") }
  
  @Test def test_binaryInteger_BigEndian() { runner.runOneTest("binaryIntegerBE") }
  @Test def test_binaryInteger_LittleEndian() { runner.runOneTest("binaryIntegerLE") }

}