# Derrida

*“Every sign, linguistic or nonlinguistic, spoken or written (in the usual sense of this opposition), as a small or large unity, can be cited, put between quotation marks; thereby it can break with every given context, and engender infinitely new contexts in an absolutely nonsaturable fashion. This does not suppose that the mark is valid outside its context, but on the contrary that there are only contexts without any center of absolute anchoring. This citationality, duplication, or duplicity, this iterability of the mark is not an accident or anomaly, but is that (normal/abnormal) without which a mark could no longer even have a so-called “normal” functioning. What would a mark be that one could not cite? And whose origin could not be lost on the way?”*

― Jacques Derrida, Margins of Philosophy

<p align="center">
  <img src="https://raw.githubusercontent.com/TristeFigure/derrida/master/doc/derrida.webp">
</p>

# Usage

```clojure
[derrida "0.1.1"]
```


```clojure
(ns my-ns
  (:require [derrida.core :refer :all]))
```

## [API doc](https://tristefigure.github.io/derrida/index.html)

### `disentangle`

Parses one level of destructuring.

```clojure
(disentangle '[a b & [c]])
=> '{:items [a b], :more [c]}

(disentangle '{:keys [a] b :b [c1 c2] :c :or {d 1} :as m})
=> '{:items [a b [c1 c2]],
     :as m,
     :or {d 1},
     :mapping {a :a, b :b, [c1 c2] :c}}
```

### `entangle`

Undoes what `disentangle` does.

```clojure
(entangle '{:items [a b]})
;; => [a b]
(entangle '{:items [a b] :or {b 1}, :mapping {a :a b :b}})
;; => {a :a, b :b, :or {b 1}}
```

### `deconstruct`

Returns symbols bound in a binding form, either as a flat sequence and in the same order as in the binding form, or as a map if the `:as-map` option is set.

```clojure
(deconstruct '[a & {:keys [x] y :_y :or {x 1} :as m}])
;; => '[a x y m]
(deconstruct '[a & {:keys [x] y :_y :or {x 1} :as m}] :as-map true)
;; => '{:a a :x x :y y :m m}
```

### `reconstruct`

Undoes what `destructure` does.

```clojure
(restructure '[x & {:keys [a b] c :cc d :d :or {d 3}}]
           '{x 0 a 1 b 2 c 3})

(restructure '[x & {:keys [a b] c :cc d :d :or {d 3}}]
           {:x 0 :a 1 :b 2 :c 3})

(restructure '[x & {:keys [a b] c :cc d :d :or {d 3}}]
           '[x 0 a 1 b 2 c 3])

;; => [0 :a 1 :b 2 :cc 3 :d nil]
```

### `efface`
Removes given symbols from a binding form.

```clojure
(efface '[a & more :as all]                 'more 'all) ;; => '[a]
(efface '[[a b] & more :as all]             '[a all])   ;; => '[[b] & more]
(efface '{:keys [a] b :b :or {a 1} :as all} 'a)         ;; => '{b :b :as all}
```

### `efface-except`

Similarly to `efface`, removes all but the given syms from the binding form.

### `destructuring-form?`

Decides on whether `form` is a destructuring or binding form using Clojure official [clojure.core.specs.alpha](https://github.com/clojure/core.specs.alpha)/`binding-form` spec.


## 
License

Copyright © 2021 TristeFigure

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
