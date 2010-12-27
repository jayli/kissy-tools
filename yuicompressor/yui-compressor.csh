#!/bin/tcsh
#
# TCSH script for YUI Compressor
#
# @author mingcheng<i.feelinglucky#gmail.com>
# @date   2009-12-23
# @link   http://www.gracecode.com/

# set compiler script path
set COMPRESSOR_SCRIPT = "`dirname $0`/compressor.csh"

if (-r $COMPRESSOR_SCRIPT != 1) then
    echo "$COMPRESSOR_SCRIPT not exists"
endif

foreach files ($argv)
    if (-r $files) then
        $COMPRESSOR_SCRIPT $files
    else
        echo "$files : not exists"
    endif
end
