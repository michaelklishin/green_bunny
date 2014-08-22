package green.bunny

import groovy.transform.TypeChecked

@TypeChecked
class Fn {
  static Closure noOpFn() {
    return {}
  }
}
