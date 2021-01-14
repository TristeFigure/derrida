(ns derrida.core-test
  (:require [clojure.test                    :refer [deftest testing is are]]
            [clojure.set                     :refer [difference]]
            [derrida.core                    :refer [entangle disentangle
                                                     deconstruct restructure
                                                     efface efface-except
                                                     destructuring-form?]]
    #?(:clj [net.cgrand.macrovich            :as macros]))
  #?(:cljs
      (:require-macros [net.cgrand.macrovich :as macros])))

(deftest test-disentangle-entangle
  (are [x y z] (and (= x (disentangle y))
                    (= z (entangle x)))

       ;; - when binding form is a vector
       '{:items [a b]}           '[a b]    '[a b]
       '{:items [[a b]]}         '[[a b]]  '[[a b]]
       '{:items [], :more args}  '[& args] '[& args]

       '{:items [a b], :more {:keys [x], y :_y, :or {x 1}, :as m}}
       '[a b & {:keys [x] y :_y :or {x 1} :as  m}]
       '[a b & {:keys [x] y :_y :or {x 1} :as  m}]

       ;;   edge cases
       {:items []}  []  []
       {:items []}  nil []

       ;; - when binding form is a map
       '{:items [a b [c1 c2]],
         :as m,
         :or {d 1},
         :mapping {a :a, b :b, [c1 c2] :c}}
       '{:keys [a] b :b [c1 c2] :c :or {d 1} :as m}
       '{a :a b :b [c1 c2] :c :or {d 1} :as m}

       ;;   edge case
       {:items [] :mapping {}}  {}  {}))

(deftest test-deconstruct
  (are [x y z] (and (= y (deconstruct x))
                    (= z (deconstruct x :as-map true)))
       ;; - when binding form is a vector
       '[a & {:keys [x] y :_y :or {x 1} :as m}] '[a x y m]'{:a a :x x :y y :m m}
       '[& args]                                '[args]   '{:args args}
       ;;   edge cases
       []                                       []        {}
       nil                                      []        {}

       ;; - when binding form is a map
       '{a :a}                                  '[a]      '{:a a}
       '{:keys [a] b :b :or {a 1} :as m}        '[a b m]  '{:a a :b b :m m}
       ;;   edge ases
       {}                                       []        {}))

(deftest test-restructure
  (testing "when binding form is a vector"
    (is (= [1 2]        (restructure '[a b]               '[a 1 b 2])))
    (is (= [1 2 {:c 3}] (restructure '[a b {:keys [c]}]   '[a 1 b 2 c 3])))
    (is (= [1 2 :c 3]   (restructure '[a b & {:keys [c]}] '[a 1 b 2 c 3])))
    (is (= [[1 2 :x "xx", :_y [:j :k :l]]]
           (restructure '[[a b & {:keys [x] [j k l] :_y :as opts}]]
                        '[a 1 b 2 x "xx"  j :j k :k l :l])))
    (testing "with &"
      (testing "followed by a symbol"
        (testing "and the symbol turns out to be a sequential coll"
          (is (= [1 2 3]       (restructure '[a & args] '[a 1 args [2 3]]))))
        (testing "and the symbol turns out to be a map"
          (is (= [1 :b 2 :c 3] (restructure '[a & args]
                                            '[a 1 args {:b 2 :c 3}])))))
      (testing "followed by more destructuring"
        (is   (= [1 2 3]       (restructure '[a & [b & c]] '[a 1 b 2 c [3]]))))))
  (testing "when binding form is a map"
    (is (= {:a 1 :b 2}  (restructure '{:keys [a b]} '[a 1 b 2])))
    (is (= {:a 1 :bb 2} (restructure '{a :a b :bb}  '[a 1 b 2])))
    (testing "with :or parameter"
      (testing "(absent)"
        (is (= {:a 1, :bb 2}
               (restructure '{:keys [a] b :bb c :c :or {c 3}} '[a 1 b 2]))))
      (testing "(present)"
        (is (= {:a 1, :bb 2 :c 4}
               (restructure '{:keys [a] b :bb c :c :or {c 3}} '[a 1 b 2 c 4]))))))
  (testing "when mapping is a function"
    (is (= {:a "a", :b ["x" "y"]} (restructure '{:keys [a] [x y] :b} str))))
  (testing "when mapping is a map"
    (testing "(symbols as keys)"
      (is (= [1] (restructure '[a] {'a 1}))))
    (testing "(keywords as keys)"
      (is (= [1] (restructure '[a] {:a 1}))))))

#?(:clj (deftest destructure-restructure-roundtrip
          (let [params   '[a b & {:keys [x] [j k l] :_y :as opts}]
                mapping '[a 1 b 2 x "xx" j :j k :k l :l]]
            (is (= (eval `(let [~params (restructure '~params '~mapping)]
                            ~(->> (partition 2 mapping)
                                  (mapcat (fn [[k _v]] `[(quote ~k) ~k]))
                                  vec)))
                   mapping)))))

(deftest test-efface-and-efface-except
  (are [x y z]  (and (= (apply efface x y)
                        z)
                     (= (apply efface-except x (difference (set (deconstruct x))
                                                        (set (flatten y))))
                        z))
       ;; - when binding form is a vector
       '[a b]           '[b]     '[a]
       '[a b]           '[[b]]   '[a]
       '[a b]           '[[[b]]] '[a]
       '[a b]           '[a b]   '[]
       '[a b & c]       '[c]     '[a b]
       '[a b & c]       '[c]     '[a b]
       '[a b & c :as d] '[d]     '[a b & c]
       '[a b & c :as d] '[c d]   '[a b]
       '[[a & b] & c]   '[a]     '[[& b] & c]
       ;;   edge cases
       '[a]             '[]      '[a]
       '[a]             nil      '[a]
       nil              '[a]     nil

       ;; - when binding form is a map
       '{:keys [a b]}    '[b]    '{a :a}
       '{:keys [a] b :b} '[[b]]  '{a :a}
       '{a :a b :b}      '[b]    '{a :a}
       '{a :a b :b}      '[a b]  '{}
       '{a :a :or {a 1}} '[a]    '{}
       '{a :a :as m}     '[m]    '{a :a}
       '{[a b & c] :x}   '[a]    '{[b & c] :x}
       ;;   edge cases
       '{a :a}           '[]     '{a :a}
       '{a :a}           nil     '{a :a}))

(macros/deftime
  (deftest test-destructuring-form?
    (are [x y] (is (= y (destructuring-form? x)))

         ;; When the form is not a coll
         1                    false
         "abc"                false

         ;; When the form is a symbol
         'a                   true

         ;; When the form is a valid binding form
         []                   true
         {}                   true
         '[a & {:keys [b]}]   true

         ;; When the form is an invalid binding form
         #{}                  false
         '[& a b]             false
         '{:keys [a] :oops b} false)))
