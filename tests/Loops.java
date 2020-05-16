public class Loops {

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
      if (a == b) {
        break;
      }
    }
    return a * b;
  }

  int breaks(int x) {
    while (true) {
      ++x;
      if (x > 0) {
        if (x > 9) {
          --x;
          break;
        }

      }
      if (x > 1) {
        if (x > 9) {
          --x;
          break;
        }
      }
      else if (x < -20) {
        break;
      }
    }
    return x;
  }

  int nestedForLoops(int a, int b) {
    int total = 0;
    for (int i = 0; i < a; ++i) {
      for (int j = 0; j < b; ++j) {
        total += 1;
        if (i == (11 + j) * j) break;
      }

      if (i == (11 * i) - 11) break;
    }
    return total;
  }

  // Bad GCD
  int badGcd(int a, int b){
    int gcd = 1;

    for(int i = 1; i <= a && i <= b; i++)
    {
      if(a % i == 0 && b % i == 0) {
        gcd = i;
      }
    }

    return gcd;
  }

  int gcd(int a, int b) {

    while (a != b) {
      if (a > b) {
        a = a - b;
      }
      else {
        b = b - a;
      }
    }
    return b;
  }

  public static boolean isPrime(int n) {
    boolean RESULT;
    for (int i = 2; i < n; i++) {
      if (n % i == 0) {
        return false;
      }
    }
    return true;
  }

  public int randoStuff(int a, int b) {
    for (int i = 0; i < b; ++i) {
      for (int j = 0; j < a; ++j) {
        if (i == j) break;
        if (i + j == a + b - 11) return 12;
      }
    }
    return 1;
  }

  public void blah(boolean a, boolean b) {
    boolean x = !(a && b);
    short s = 1;
    byte by = 1;
    char c = 1;
    int i = 1;
  }
}