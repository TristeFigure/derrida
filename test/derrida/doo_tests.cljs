(ns derrida.doo-test
  (:require [cljs.test  :as           test]
            [doo.runner :refer-macros [doo-tests]]
            [derrida.core-test]))

(enable-console-print!)
(doo-tests 'derrida.core-test)
