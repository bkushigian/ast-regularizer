public class Loops {

  boolean ands(boolean b, boolean c) {
    return b && c;
  }

  int whileLoop(int a) {
    int x = 0;
    while (x < a) {
      x += 2;
    }
    return x;
  }

  int forLoop(int a, int b) {
    for (int i = 0; i < 100; ++i) {
      a -= b;
      b -= i;
      a -= b;
    }
    return a * b;
  }

  void blocks() {
    int x = 1;
    {
      int y = 2;
      {
        int z = 3;
        x = 12;
      }
    }
  }

  void breaks(int x) {
    while (true) {
      ++x;
      if (x > 0) {
        if (x > 9) {
          break;
        }

      }
      System.out.println("x = " + x);
      if (x > 1) {
        if (x > 9) {
          break;
        }
        else if (x < -20) {
          break;
        }
      }
      System.out.println("x is still " + x);
    }
  }
//  int nestedForLoops(int a, int b) {
//    int total = 0;
//    for (int i = 0; i < a; ++i) {
//      for (int j = 0; j < b; ++j) {
//        total += 1;
//      }
//    }
//    return total;
//  }
}