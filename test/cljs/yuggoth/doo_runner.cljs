(ns yuggoth.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [yuggoth.core-test]))

(doo-tests 'yuggoth.core-test)

