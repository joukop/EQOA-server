package tv.kiekko.eqoa.udp;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class Message {

  /*
  
  Run length encoding is used when sending State channels as follows (comment written from memory so might not be 100% correct):
  
  - Zero byte means end.

  - If the byte has the highest bit set (0x80), then the rest of the byte is the number of bytes to copy,
  and the next byte contains the number of zeroes.

  - If the highest bit is not set, then the high nibble is the number of bytes to copy,
  and the low nibble is the number of zeroes.
  
  Zeroes are inserted first, bytes copied after that.
  
  */
  
  
  
  static ByteBuf runLengthEncode(ByteBuf buf,int len) {
    ByteBuf ret=Unpooled.buffer(64).order(ByteOrder.LITTLE_ENDIAN);
    int start=buf.readerIndex();
    int end=start+len;
    int i=start;
    int j;
    while(i<end) {
      j=i;
      while(j < end && buf.getByte(j) == 0 && j-i < 0x7f)
        j++;
      int zeroes=j-i;
      while(j < end && (buf.getByte(j) != 0 || j == end-1 || buf.getByte(j+1) != 0) && j-i-zeroes < 0x7f)
        j++;
      int copy=j-zeroes-i;
      if (copy == 0 && zeroes == 0)
        break;
      ret.writeByte(copy|0x80);
      ret.writeByte(zeroes);
      ByteBuf slice = buf.slice(i+zeroes,copy);
      ret.writeBytes(slice);
      i+=copy+zeroes;
    }
    ret.writeByte(0);
    return ret;
  }

}
