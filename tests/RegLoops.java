public class RegLoops {

  int whileLoop(int a) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    int x = 0;
    while (x < a) {
      x += 2;
    }
    __method_has_returned__ = true;
    __RETURN_RESULT__ = x;
    return __RETURN_RESULT__;
  }

  int forLoop(int a, int b) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    int i = 0;
    boolean __loop_breaks_0__ = false;
    while (!__loop_breaks_0__ && i < 100) {
      a -= b;
      b -= i;
      a -= b;
      if (a == b) {
        __loop_breaks_0__ = true;
      }
      /* --- Auto-generated guard statement --- */
      if (!__loop_breaks_0__) {
        ++i;
      }
    }
    __method_has_returned__ = true;
    __RETURN_RESULT__ = a * b;
    return __RETURN_RESULT__;
  }

  int breaks(int x) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    boolean __loop_breaks_0__ = false;
    while (!__loop_breaks_0__ && true) {
      ++x;
      if (x > 0) {
        if (x > 9) {
          --x;
          __loop_breaks_0__ = true;
        }
      }
      /* --- Auto-generated guard statement --- */
      if (!__loop_breaks_0__) {
        if (x > 1) {
          if (x > 9) {
            --x;
            __loop_breaks_0__ = true;
          }
        } else if (x < -20) {
          __loop_breaks_0__ = true;
        }
      }
    }
    __method_has_returned__ = true;
    __RETURN_RESULT__ = x;
    return __RETURN_RESULT__;
  }

  int nestedForLoops(int a, int b) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    int total = 0;
    int i = 0;
    boolean __loop_breaks_0__ = false;
    while (!__loop_breaks_0__ && i < a) {
      int j = 0;
      boolean __loop_breaks_1__ = false;
      while (!__loop_breaks_1__ && j < b) {
        total += 1;
        if (i == (11 + j) * j)
          __loop_breaks_1__ = true;
        /* --- Auto-generated guard statement --- */
        if (!__loop_breaks_1__) {
          ++j;
        }
      }
      if (i == (11 * i) - 11)
        __loop_breaks_0__ = true;
      /* --- Auto-generated guard statement --- */
      if (!__loop_breaks_0__) {
        ++i;
      }
    }
    __method_has_returned__ = true;
    __RETURN_RESULT__ = total;
    return __RETURN_RESULT__;
  }

  // Bad GCD
  int badGcd(int a, int b) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    int gcd = 1;
    int i = 1;
    while (i <= a && i <= b) {
      if (a % i == 0 && b % i == 0) {
        gcd = i;
      }
      i++;
    }
    __method_has_returned__ = true;
    __RETURN_RESULT__ = gcd;
    return __RETURN_RESULT__;
  }

  int gcd(int a, int b) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    while (a != b) {
      if (a > b) {
        a = a - b;
      } else {
        b = b - a;
      }
    }
    __method_has_returned__ = true;
    __RETURN_RESULT__ = b;
    return __RETURN_RESULT__;
  }

  public static boolean isPrime(int n) {
    // Default value to satisfy Javac's flow checking
    boolean __RETURN_RESULT__ = false;
    boolean __method_has_returned__ = false;
    boolean RESULT;
    int i = 2;
    while (i < n) {
      if (n % i == 0) {
        __method_has_returned__ = true;
        __RETURN_RESULT__ = false;
      }
      /* --- Auto-generated guard statement --- */
      if (!__method_has_returned__) {
        i++;
      }
    }
    /* --- Auto-generated guard statement --- */
    if (!__method_has_returned__) {
      __method_has_returned__ = true;
      __RETURN_RESULT__ = true;
    }
    return __RETURN_RESULT__;
  }

  public int randoStuff(int a, int b) {
    // Default value to satisfy Javac's flow checking
    int __RETURN_RESULT__ = -2147483648;
    boolean __method_has_returned__ = false;
    int i = 0;
    while (i < b) {
      int j = 0;
      boolean __loop_breaks_1__ = false;
      /* --- Auto-generated guard statement --- */
      if (!__method_has_returned__) {
        while (!__loop_breaks_1__ || __method_has_returned__ && j < a) {
          if (i == j)
            __loop_breaks_1__ = true;
          /* --- Auto-generated guard statement --- */
          if (!__loop_breaks_1__) {
            if (i + j == a + b - 11) {
              __RETURN_RESULT__ = 12;
              __method_has_returned__ = true;
            }
            /* --- Auto-generated guard statement --- */
            if (!__method_has_returned__) {
              ++j;
            }
          }
        }
        ++i;
      }
    }
    /* --- Auto-generated guard statement --- */
    if (!__method_has_returned__) {
      __method_has_returned__ = true;
      __RETURN_RESULT__ = 1;
    }
    return __RETURN_RESULT__;
  }

  public void blah(boolean a, boolean b) {
    boolean __method_has_returned__ = false;
    boolean x = !(a && b);
    short s = 1;
    byte by = 1;
    char c = 1;
    int i = 1;
  }
}