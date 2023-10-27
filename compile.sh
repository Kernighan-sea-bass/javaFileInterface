#!/bin/bash

rm *.class
javac -cp ".:./jar/poi-bin-5.1.0/*:./jar/poi-bin-5.1.0/auxiliary/*:./jar/poi-bin-5.1.0/ooxml-lib/*:./jar/poi-bin-5.1.0/lib/*:./jar/*" $*
