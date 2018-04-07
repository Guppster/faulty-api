#!/bin/bash

# author: giagulei
# kounelo-extractor

error=1
normal_exit=0

# check script input arguments/
if [ $# -ne 1 ]
  then
    echo
    echo "Kounelaki vale to arxiko tar.gz san INPUT"
    echo "Usage: ./install_extractor.sh <targz_FILE>"
    echo "Your extractor working space is located in: ~/extractor"
    echo
    exit $error
fi

WORKSPACE=~/extractor
mkdir $WORKSPACE; cp $1 $WORKSPACE
cd $WORKSPACE; tar xvzf $1

# extract and install source navigator
bzip2 -d sourcenavigator-NG4.5.tar.bz2
tar xvf sourcenavigator-NG4.5.tar

cd sourcenavigator-NG4.5
./configure --prefix=/opt/sourcenav
make
sudo make install

cd ../
# extract and install fetch progs
echo "********************************************";echo
echo "When promted for paswd, type: reeng"
echo "********************************************";echo

7za x fetch-Cpp.7z -ofetch-Cpp
7za x fetch-Java.7z -ofetch-Java

cd $WORKSPACE/fetch-Cpp/src/pmccabe-2.4.0-fetch/
make pmccabe
mkdir -p $WORKSPACE/fetch/bin; cp pmccabe $WORKSPACE/fetch/bin
cd $WORKSPACE

# export environment variables
echo "export FETCHCPP=$WORKSPACE/fetch-cpp" >> ~/.bashrc
echo "export FETCHJAVA=$WORKSPACE/fetch-Java" >> ~/.bashrc
echo "export SN_HOME=/opt/sourcenav/bin" >> ~/.bashrc
echo "export PMC=$WORKSPACE/fetch/bin" >> ~/.bashrc
echo "export CROCO=$WORKSPACE/fetch/bin" >> ~/.bashrc

source ~/.bashrc

sudo cp $WORKSPACE/sourcenavigator-NG4.5/snavigator/db/dbdump /opt/sourcenav/bin/

echo
echo "Everything is ok now!"
echo "You can create your first .rsf file :-)"
echo

exit $normal_exit
