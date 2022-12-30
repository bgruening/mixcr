#!/usr/bin/env bash

# Single-cell integration test

assert() {
  expected=$(echo -ne "${2:-}")
  result="$(eval 2>/dev/null $1)" || true
  result="$(sed -e 's/ *$//' -e 's/^ *//' <<<"$result")"
  if [[ "$result" == "$expected" ]]; then
    return
  fi
  result="$(sed -e :a -e '$!N;s/\n/\\n/;ta' <<<"$result")"
  [[ -z "$result" ]] && result="nothing" || result="\"$result\""
  [[ -z "$2" ]] && expected="nothing" || expected="\"$2\""
  echo "expected $expected got $result for" "$1"
  exit 1
}

set -euxo pipefail

mixcr analyze 10x-vdj-bcr \
  --species hs \
  single_cell_vdj_t_subset_R1.fastq.gz \
  single_cell_vdj_t_subset_R2.fastq.gz \
  base_single_cell.raw

mixcr analyze 10x-vdj-bcr \
  --species hs \
  --assemble-contigs-by VDJRegion \
  single_cell_vdj_t_subset_R1.fastq.gz \
  single_cell_vdj_t_subset_R2.fastq.gz \
  base_single_cell.vdjcontigs

assert "cat base_single_cell.vdjcontigs.assembleContigs.report.json | head -n 1 | jq -r .finalCloneCount" "7"

assert "mixcr exportClones --no-header base_single_cell.vdjcontigs.contigs.clns | wc -l" "7"
assert "mixcr exportClones --no-header --split-by-tags Cell base_single_cell.vdjcontigs.contigs.clns | wc -l" "7"
assert "mixcr exportClones --no-header --split-by-tags Molecule base_single_cell.vdjcontigs.contigs.clns | wc -l" "60"
assert "mixcr exportClones --no-header -tags Molecule base_single_cell.vdjcontigs.contigs.clns | wc -l" "60"

