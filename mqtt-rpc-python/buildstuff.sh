#!/bin/sh

#convenience bash script to build 'source distribution, binary distribution, convert README.md to README.rst'
pandoc -o README.rst README.md
echo 'pandoc translation complete'
python setup.py sdist
python setup.py bdist 
