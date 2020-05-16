import java.util.Random;

public class Runner {
  static Loops loops = new Loops();
  static RegLoops regLoops = new RegLoops();
  static Random random = new Random();

  public static void main(String[] arsg) {
    test_whileLoop(5000);
    test_forLoop(5000);
    test_breaks(10000);
    test_nestedForLoops(3000);

  }

  static void test_whileLoop(int tests) {
    for (int iter = 0; iter < tests; ++iter) {
      final int a = random.nextInt(2000) ;
      final int l = loops.whileLoop(a);
      final int r = regLoops.whileLoop(a);
      if (l != r) {
        throw new RuntimeException("whileLoop: " + r + " != " + l + " on input " + a);
      }
    }
    System.out.println("test_whileLoops: ran " + tests + " tests.....OK");
  }

  static void test_forLoop(int tests) {
    for (int iter = 0; iter < tests; ++iter) {
      final int a = random.nextInt(2000) ;
      final int b = random.nextInt(2000) ;
      final int l = loops.forLoop(a, b);
      final int r = regLoops.forLoop(a, b);
      if (l != r) {
        throw new RuntimeException("forLoop: " + r + " != " + l + " on inputs " + a + " " + b);
      }
    }
    System.out.println("test_forLoops: ran " + tests + " tests.....OK");
  }

  static void test_breaks(int tests) {
    for (int iter = 0; iter < tests; ++iter) {
      final int x = random.nextInt() ;
      loops.breaks(x);
      regLoops.breaks(x);
    }
    System.out.println("test_forLoops: ran " + tests + " tests.....OK");
  }


  static void test_nestedForLoops(int tests) {

    for (int iter = 0; iter < tests; ++iter) {
      final int a = random.nextInt(2000) ;
      final int b = random.nextInt(2000) ;
      final int l = loops.nestedForLoops(a, b);
      final int r = regLoops.nestedForLoops(a, b);
      if (l != r) {
        throw new RuntimeException("nestedForLoops: " + r + " != " + l + " on inputs " + a + " " + b);
      }
    }
    System.out.println("test_nestedForLoops: ran " + tests + " tests.....OK");
  }

}