#!/bin/bash

cd codox
git add .
git commit -am "Add project documentation"
git push -f -u origin gh-pages
cd ..
