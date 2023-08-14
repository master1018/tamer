public class ConstantInfiniteWhile {
  int test1() {
    while ( false | true ) {}
  }
  int test2() {
    while ( true & true ) {}
  }
  int test3() {
    while ( false ^ true ) {}
  }
  int test4() {
    while ( false == false ) {}
  }
  int test5() {
    while ( 1 != 0 ) {}
  }
  int test6() {
    while ( 1 + 2 > 0 ) {}
  }
  int test7() {
    while ( true ? true : false ) {}
  }
}
