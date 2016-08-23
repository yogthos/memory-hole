(ns memory-hole.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [memory-hole.core-test]))

(doo-tests 'memory-hole.core-test)

