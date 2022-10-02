#!/usr/bin/env bash

set -euxo pipefail

mixcr analyze tcr_shotgun \
    +species hs \
    +rna \
    +addStep assembleContigs \
    +imputeGermlineOnExport \
    test_R1.fastq test_R2.fastq case2