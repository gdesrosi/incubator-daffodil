/* Copyright (c) 2017 Tresys Technology, LLC. All rights reserved.
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

package edu.illinois.ncsa.daffodil.infoset

import scala.xml.Node

class ScalaXMLInfosetInputter(node: Node)
  extends InfosetInputter {

  // TODO: This is very inefficient and slow. This entire class should be
  // rewritten, likely using the InfosetInputterCursor interface, similar to
  // XML or Json, and use some sort of node pull cursor library
  private val xmlInfosetInputter = {
    val rdr = new java.io.StringReader(node.toString())
    new XMLTextInfosetInputter(rdr)
  }

  override def getEventType() = xmlInfosetInputter.getEventType()
  override def getLocalName() = xmlInfosetInputter.getLocalName()
  override def getNamespaceURI() = xmlInfosetInputter.getNamespaceURI()
  override def getSimpleText() = xmlInfosetInputter.getSimpleText()
  override def hasNext() = xmlInfosetInputter.hasNext()
  override def isNilled() = xmlInfosetInputter.isNilled()
  override def next() = xmlInfosetInputter.next()
  override val supportsNamespaces = xmlInfosetInputter.supportsNamespaces

  override def fini = xmlInfosetInputter.fini
}