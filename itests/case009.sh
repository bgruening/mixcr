#!/usr/bin/env bash

# Regression Test #2

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

set -e

mixcr analyze --verbose generic-tcr-amplicon \
  --species hs \
  --rna \
  --floating-left-alignment-boundary \
  --floating-right-alignment-boundary C \
  --add-step assembleContigs \
  --split-clones-by V --split-clones-by J --split-clones-by C \
  -M assemble.sortBySequence=true \
  CD4M1_test_R1.fastq.gz CD4M1_test_R2.fastq.gz case9

assert "cat case9.align.report.json | head -n 1 | jq -r .chainUsage.chains.TRA.total" "237736"
assert "cat case9.assemble.report.json | head -n 1 | jq -r .readsInClones" "199564"
assert "cat case9.assembleContigs.report.json | head -n 1 | jq -r .longestContigLength" "277"
assert "cat case9.assembleContigs.report.json | head -n 1 | jq -r .clonesWithAmbiguousLetters" "1168"
assert "cat case9.assembleContigs.report.json | head -n 1 | jq -r .assemblePrematureTerminationEvents" "3"
assert "cat case9.assembleContigs.report.json | head -n 1 | jq -r .finalCloneCount" "25593"
