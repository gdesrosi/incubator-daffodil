package daffodil.processors.charset

import java.nio.charset.{ Charset, CoderResult, CharsetDecoder, CharsetEncoder }
import java.nio.ByteBuffer
import java.nio.CharBuffer
import daffodil.exceptions.Assert
import daffodil.util.Misc

object USASCII7BitPackedCharset
  extends java.nio.charset.Charset("US-ASCII-7-bit-packed", Array()) {

  final def contains(cs: Charset): Boolean = {
    false
  }

  final def newDecoder(): CharsetDecoder = {
    new USASCII7BitPackedDecoder
  }

  final def newEncoder(): CharsetEncoder = {
    new USASCII7BitPackedEncoder
  }
}

/**
 * Mixin for Charsets which support initial bit offsets so that
 * their character codepoints need not be byte-aligned.
 */
trait SupportsInitialBitOffset {

  private var startBitOffset = 0
  private var startBitOffsetHasBeenSet = false
  private var startBitOffsetHasBeenUsed = false

  def setInitialBitOffset(bitOffset0to7: Int) {
    Assert.usage(!startBitOffsetHasBeenSet, "Already set. Cannot set again until decoder is reset().")
    Assert.usage(bitOffset0to7 <= 7 && bitOffset0to7 >= 0)
    startBitOffset = bitOffset0to7
    startBitOffsetHasBeenSet = true
  }

  def getStartBitOffset() = {
    if (startBitOffsetHasBeenUsed) 0 // one time we return the value. After that 0 until a reset.
    else {
      startBitOffsetHasBeenUsed = true
      startBitOffset
    }
  }

  def resetStartBit() {
    startBitOffsetHasBeenUsed = false
    startBitOffset = 0
    startBitOffsetHasBeenSet = false
  }

}

/**
 * You have to initialize one of these for a specific ByteBuffer because
 * the encoding is 7-bits wide, so we need additional state beyond just
 * the byte position and limit that a ByteBuffer provides in order to
 * properly sequence through the data.
 */
class USASCII7BitPackedDecoder
  extends java.nio.charset.CharsetDecoder(USASCII7BitPackedCharset, 1, 1)
  with SupportsInitialBitOffset {

  override def implReset() {
    // println("Reset")
    resetStartBit()
    buf = null
    bitLimit = Long.MaxValue
    bitPos = 0
    hasPriorByte = false
    priorByte = 0
    priorByteBitCount = 0
  }

  var bitLimit: Long = Long.MaxValue
  var bitPos = 0

  private var buf: ByteBuffer = _

  private var priorByte = 0
  private var hasPriorByte = false

  /**
   * When there is a prior byte, this gives the number of bits from it
   * that have not yet been consumed. Zero when there is no prior byte.
   *
   * Value ranges from 1 to 7. Can never be 8.
   */
  private var priorByteBitCount = 0

  val widthOfAByte = 7 // the whole point of this class is that we consume this many bits per byte.

  /**
   * Convert signed Byte type to Int that is the unsigned equivalent.
   */
  def asUnsignedByte(n: Byte): Int = {
    if (n < 0) 256 + n else n
  }

  // some constants to make the table dispatch below clearer.
  private val NoData = false
  private val YesData = true
  private val NoSpace = false
  private val YesSpace = true
  private val NoPrior = false
  private val YesPrior = true

  final def decodeLoop(in: ByteBuffer, out: CharBuffer): CoderResult = {

    def output(charCode: Int) {
      // println("charcode = %2x".format(charCode))
      val char = charCode.toChar
      out.put(char)
      bitPos += widthOfAByte
    }

    def asBits(b: Int) = {
      val hex = "%02x".format(b)
      // println(hex)
      Misc.hex2Bits(hex)
    }

    Assert.invariant(bitPos <= bitLimit)

    //
    // Now we have to adjust for the starting bit offset
    //
    val bitOffset0to7 = getStartBitOffset()
    if (bitOffset0to7 != 0) {
      priorByteBitCount = 8 - bitOffset0to7
    }

    while (true) {
      if (bitPos + this.widthOfAByte > bitLimit) {
        // not enough bits to create another character
        return CoderResult.UNDERFLOW
      }

      (hasPriorByte, priorByteBitCount, in.hasRemaining(), out.hasRemaining()) match {
        // 
        // Fresh start, and also every 56 bits we hit a clean
        // byte boundary again
        //
        case (NoPrior, 0, YesData, YesSpace) => {
          val currentByte = asUnsignedByte(in.get())
          // println("no prior byte, current byte = %s".format(asBits(currentByte)))

          priorByte = currentByte
          priorByteBitCount = 8 - widthOfAByte
          hasPriorByte = true

          val currentCharCode = currentByte >> 1 // we take the most significant bits first.

          output(currentCharCode)

        }
        case (NoPrior, 0, NoData, _) => return CoderResult.UNDERFLOW
        case (NoPrior, 0, _, NoSpace) => return CoderResult.OVERFLOW
        case (NoPrior, n, YesData, _) => {
          // This happens if we're starting the decode loop at a startBitOffset that is non-zero.
          // we basically grab one byte and pretend it's the prior byte. 
          priorByte = asUnsignedByte(in.get())
          hasPriorByte = true
        }
        case (NoPrior, n, NoData, _) => return CoderResult.UNDERFLOW
        case (YesPrior, 0, _, _) => Assert.invariantFailed("priorByteBitCount should not be 0 when there is a prior byte")
        case (YesPrior, n, _, _) if (n > 7) => Assert.invariantFailed("priorByteBitCount should be from 1 to 7 when there is a prior byte")
        case (YesPrior, 7, _, YesSpace) => {
          // Case where we previously used only 1 bit from the prior byte
          // so we can produce the next character from the remaining 7 bits of this byte.
          // We don't need more input.
          // println("prior byte = %s, no current byte needed, priorByteBitCount = %d".format(asBits(priorByte), priorByteBitCount))

          val currentByte = priorByte
          val currentCharCode = currentByte & 0x7F // least significant bits for remainder
          output(currentCharCode)

          hasPriorByte = false
          priorByte = 0
          priorByteBitCount = 0
        }
        case (YesPrior, n, NoData, _) => {
          // We have a partial charcode in prior byte, but there are no more bytes to be had from the
          // ByteBuffer so we can't complete it without more data.
          return CoderResult.UNDERFLOW
        }
        case (YesPrior, n, YesData, YesSpace) => {
          // Straddling bytes. We need another input byte to make up a full character.
          val currentByte = asUnsignedByte(in.get())
          // println("prior byte = %s, current byte = %s, priorByteBitCount = %d".format(asBits(priorByte), asBits(currentByte), priorByteBitCount))
          val priorMask = 0xFF >> (8 - priorByteBitCount)
          val priorBits = priorByte & priorMask // keeps LSBs we're going to use
          val currentByteBitCount = widthOfAByte - priorByteBitCount
          val currentBitsAlone = currentByte >> (8 - currentByteBitCount)
          val priorBitsInPosition = priorBits << currentByteBitCount
          val currentCharCode = priorBitsInPosition | currentBitsAlone
          priorByte = currentByte
          hasPriorByte = true // remains true
          priorByteBitCount = 8 - currentByteBitCount
          Assert.invariant(priorByteBitCount > 0)
          Assert.invariant(priorByteBitCount <= 7)

          output(currentCharCode)
        }
        case (_, _, _, NoSpace) => return CoderResult.OVERFLOW
      }

    } // end while loop

    Assert.impossible("Incorrect return from decodeLoop.")

  }
}

class USASCII7BitPackedEncoder
  extends java.nio.charset.CharsetEncoder(USASCII7BitPackedCharset, 1, 1) {

  def encodeLoop(cb: CharBuffer, bb: ByteBuffer): CoderResult = {
    Assert.notYetImplemented()
  }
}
