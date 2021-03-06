#!/bin/bash

set -uf -o pipefail

docstring="DESCRIPTION
Test commands work smoothly and do not throw unexpected exceptions.
USAGE
Move to the directory containing integration_test.sh and execute it by giving the path to the 
ASCIIGenome bash script:

    cd /path/to/test/
    ./integration_test.sh /path/to/ASCIIGenome
"

if [[ $1 == "" || $1 == "-h" ]]
then
    echo "$docstring"
    exit 1
fi

# ------------------------------------------------------------------------------

source bashTestFunctions.sh

ASCIIGenome="$1 --debug 2 -ni"

#pprint 'Can highlight pattern'
#$ASCIIGenome ../test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf.gz -r 1:1-400000 -x 'print -hl 200000'

pprint 'Can explain sam flags'
$ASCIIGenome -nf -x 'explainSamFlag 2690' > flags.tmp
grep '^ X ' flags.tmp | grep 'supplementary alignment' > /dev/null
assertEquals 0 $?
grep -v ' X ' flags.tmp | grep 'read is PCR or optical duplicate' > /dev/null
assertEquals 0 $?
rm flags.tmp

pprint 'Can avoid history positions not compatible with current genome'
$ASCIIGenome -x 'goto FOOBAR && open ../test_data/pairs.sam && p' > /dev/null
assertEquals 0 $?

pprint 'find - CASE INSENSITIVE'
$ASCIIGenome -nf -x 'print && find .actb' ../test_data/hg19_genes.gtf.gz | grep LACTB > /dev/null
assertEquals 0 $?

pprint 'find - LITERAL 1'
$ASCIIGenome -nf -x 'print && find -F .ACTB' ../test_data/hg19_genes.gtf.gz | grep LACTB > /dev/null
assertEquals 1 $?

pprint 'find - LITERAL 2' # '+' only as regex is invalid
$ASCIIGenome -nf -x 'goto chr1:1 && print && find -F +' ../test_data/hg19_genes.gtf.gz | grep DDX11L1 > /dev/null
assertEquals 0 $?

pprint 'find - CASE SENSITIVE'
$ASCIIGenome -nf -x 'print && find -c actb' ../test_data/hg19_genes.gtf.gz | grep ACTB > /dev/null
assertEquals 1 $?

pprint 'grep - CASE INSENSITIVE'
$ASCIIGenome -nf -x 'goto chr1:11874 && print && grep -i ddx\d+' ../test_data/hg19_genes.gtf.gz | grep DDX11L1 > /dev/null
assertEquals 0 $?

pprint 'Can read VCF with sequence dictionary but with no records'
$ASCIIGenome ../test_data/norecords.vcf | grep 'test_data/norecords.vcf#' > /dev/null
assertEquals 0 $?

pprint 'Can show read pairs'
$ASCIIGenome -x 'readsAsPairs' ../test_data/pairs.sam > /dev/null

## Init from VCF
$ASCIIGenome ../test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf.gz -x 'show genome' > /dev/null
$ASCIIGenome https://raw.githubusercontent.com/dariober/ASCIIGenome/master/test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf.gz -x 'show genome' > /dev/null

## Genotype matrix
$ASCIIGenome ../test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf.gz -x 'goto 1:1117997-1204429 && genotype -f {HOM} && genotype -n 1' > /dev/null
$ASCIIGenome ../test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf.gz -x 'goto 1:1117997-1204429 && genotype -s HG00096' > /dev/null

## Set color for features
$ASCIIGenome ../test_data/hg19_genes_head.gtf -x "goto chr1:6267-17659 && featureColorForRegex -r DDX11L1 red -r WASH7P blue" > /dev/null

## Test awk with getSamTag()
$ASCIIGenome ../test_data/ds051.actb.bam -x "goto chr7:5570087-5570291 && awk 'getSamTag(\"NM\") > 0'" > /dev/null

## Test awk with VCF functions
$ASCIIGenome ../test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf -x "goto 1:200000-1000000 && awk 'getInfoTag(\"AC\") > 0'" > /dev/null
$ASCIIGenome ../test_data/ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf -x "goto 1:200000-1000000 && awk 'getFmtTag(\"GT\") == \"0|1\"'" > /dev/null

## Test awk with GTF/GFF
$ASCIIGenome ../test_data/hg19_genes_head.gtf -x "awk 'getGtfTag(\"gene_name\") ~ \"DD\"'" > /dev/null
$ASCIIGenome ../test_data/hg19_genes_head.gtf -x "awk 'get(\"gene_name\") ~ \"DD\"'" > /dev/null
$ASCIIGenome ../test_data/Homo_sapiens.GRCh38.86.ENST00000331789.gff3 -x "awk 'getGffTag(\"ID\") ~ \"ENST\"'" > /dev/null
$ASCIIGenome ../test_data/Homo_sapiens.GRCh38.86.ENST00000331789.gff3 -x "awk 'get(\"ID\") ~ \"ENST\"'" > /dev/null

## Test header names. Note escape on $
$ASCIIGenome ../test_data/ds051.actb.bam -x "goto chr7:5570087-5570291 && bookmark && awk '\$POS > 5500000' actb.bam" > /dev/null
$ASCIIGenome ../test_data/ds051.actb.bam -x "goto chr7:5570087-5570291 && bookmark && awk '\$START > 5500000' Book" > /dev/null
$ASCIIGenome ../test_data/CEU.exon.2010_06.genotypes.vcf.gz -x "awk 'length(\$REF) == 1 && length(\$ALT) > 1'" > /dev/null
$ASCIIGenome ../test_data/Homo_sapiens.GRCh38.86.ENST00000331789.gff3 -x "awk '\$SOURCE ~ \"havana\"'" > /dev/null

## Can show/hide track settings
$ASCIIGenome ../test_data/ds051.actb.bam -x 'goto chr7:5568803-5568975 && show genome && show genome' > /dev/null

## Can reset global options
$ASCIIGenome ../test_data/ds051.actb.bam -x 'goto chr7:5568803-5568975 && setConfig max_reads_in_stack 1000 && zo' > /dev/null

## Can handle coords outside chrom limits.
$ASCIIGenome ../test_data/ds051.actb.bam -x 'goto chrM && zo 25 && :chrM:1-1000000' > /dev/null

$ASCIIGenome ../test_data/ds051.actb.bam -x 'goto chr7:5568803-5568975 && zo && zi' > /dev/null

$ASCIIGenome ../test_data/ds051.actb.bam -fa ../test_data/chr7.fa -x 'goto chr7:5568803-5568975 && zo && zi' > /dev/null

## Use of PCT screen coords
$ASCIIGenome ../test_data/ds051.actb.bam  -x 'goto chrM:1 && 0 .2 && 16555 && .1' > /dev/null

## Set gtf attribute for feature name
$ASCIIGenome ../test_data/hg19_genes.gtf.gz -x 'gffNameAttr gene_name' > /dev/null

set +x
echo -e "\033[32mDONE\033[0m"
