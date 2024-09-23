package com.chessai.util;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public class BitboardUtilityModule {
  private static final long base = 0b0000000000000000000000000000000000000000000000000000000000000001L;
  public static long generateBitboardFromIndex(int index) {
    return base << index;
  }

  public static Stream<Integer> getIndicesFromBitboard(long bitboard) {
    return LongStream
        .range(0, 64)
        .filter(i -> ((bitboard >> i) & 1) == 1)
        .mapToObj(l -> Integer.valueOf((int) l));
  }

  public static void printBitboard(long bitboard) {
    String horizontalLine = "  +---+---+---+---+---+---+---+---+\n";
    System.out.print(horizontalLine);
    for (int rank = 7; rank >= 0; rank--) {
      System.out.print(rank + 1 + " | ");
      for (int file = 0; file < 8; file++) {
        int square = rank * 8 + file;
        if ((bitboard & (1L << square)) != 0) {
          System.out.print("● | ");
        } else {
          System.out.print("  | ");
        }
      }
      System.out.print(rank + 1 + "\n" + horizontalLine);
    }
    System.out.println("    a   b   c   d   e   f   g   h  ");
  }
}
