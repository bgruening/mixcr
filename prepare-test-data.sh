#!/bin/bash

set -e
set -o pipefail

# Linux readlink -f alternative for Mac OS X
function readlinkUniversal() {
  targetFile=$1

  cd $(dirname $targetFile)
  targetFile=$(basename $targetFile)

  # iterate down a (possible) chain of symlinks
  while [ -L "$targetFile" ]; do
    targetFile=$(readlink $targetFile)
    cd $(dirname $targetFile)
    targetFile=$(basename $targetFile)
  done

  # compute the canonicalized name by finding the physical path
  # for the directory we're in and appending the target file.
  phys_dir=$(pwd -P)
  result=$phys_dir/$targetFile
  echo $result
}

os=$(uname)

dir=""

case $os in
Darwin)
  dir=$(dirname "$(readlinkUniversal "$0")")
  ;;
Linux)
  dir="$(dirname "$(readlink -f "$0")")"
  ;;
FreeBSD)
  dir=$(dirname "$(readlinkUniversal "$0")")
  ;;
*)
  echo "Unknown OS."
  exit 1
  ;;
esac

cd "$dir/src/test/resources/sequences/big/"

cd yf_sample_data
parallel -j5 "${dir}/mixcr" -Xmx500m analyze amplicon -f \
  -s hs --starting-material rna --contig-assembly --5-end v-primers --3-end j-primers --adapters adapters-present \
  --assemble '-OseparateByC=true' --assemble '-OseparateByV=true' --assemble '-OseparateByJ=true' \
  --assemble '--sort-by-sequence' \
  --impute-germline-on-export --json-report "{}" \
  "{}_L001_R1.fastq.gz" "{}_L001_R2.fastq.gz" "{}" ::: Ig-2_S2 Ig-3_S3 Ig-4_S4 Ig-5_S5 Ig1_S1 Ig2_S2 Ig3_S3 Ig4_S4 Ig5_S5