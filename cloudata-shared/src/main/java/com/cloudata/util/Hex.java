package com.cloudata.util;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.ByteString.ByteIterator;

public class Hex {

  static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  private static final int MAX_DEBUG_LENGTH = 32;

  public static String toHex(ByteString key) {
    StringBuilder sb = new StringBuilder();

    ByteIterator iterator = key.iterator();
    while (iterator.hasNext()) {
      int v = iterator.nextByte() & 0xff;
      sb.append(HEX[v >> 4]);
      sb.append(HEX[v & 0xf]);
    }

    return sb.toString();
  }

  public static String toHex(byte[] bytes, char separator) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      if (i != 0) {
        sb.append(separator);
      }

      int v = bytes[i] & 0xff;
      sb.append(HEX[v >> 4]);
      sb.append(HEX[v & 0xf]);
    }

    return sb.toString();
  }

  public static String toHex(ByteBuffer key) {
    StringBuilder sb = new StringBuilder();

    for (int i = key.position(); i < key.limit(); i++) {
      int v = key.get(i) & 0xff;
      sb.append(HEX[v >> 4]);
      sb.append(HEX[v & 0xf]);
    }

    return sb.toString();
  }

  public static String forDebug(ByteString key) {
    if (key == null) {
      return "(null)";
    }
    String s = toHex(key);
    if (s.length() > MAX_DEBUG_LENGTH) {
      s = s.substring(0, MAX_DEBUG_LENGTH) + "...";
    }
    return s;
  }

  public static String forDebug(ByteBuffer key) {
    if (key == null) {
      return "(null)";
    }
    String s = toHex(key);
    if (s.length() > MAX_DEBUG_LENGTH) {
      s = s.substring(0, MAX_DEBUG_LENGTH) + "...";
    }
    return s;
  }

}
